package com.broadtech.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OffsetCheckAndRewrite {
    private static final Logger logger = LoggerFactory.getLogger(OffsetCheckAndRewrite.class);
    public static final int TIMEOUT = 5000;
    private static final Map<String, LongAdder> partitionStatistic = new ConcurrentHashMap<>();
    private final Properties kafkaPros;
    private final long[][] timeRanges;
    private final TimeUnit timeUnit;
    private final String model;
    private final Properties useConf;
    private final TopicPartition topicPartition;

    public OffsetCheckAndRewrite(Properties kafkaPros, TopicPartition topicPartition, long[][] timeRanges, String model, String timeUnit, Properties properties) {
        this.kafkaPros = kafkaPros;
        this.topicPartition = topicPartition;
        this.timeRanges = timeRanges;
        this.timeUnit = "s".equals(timeUnit) ? TimeUnit.SECONDS : TimeUnit.MILLISECONDS;
        this.model = model;
        this.useConf = properties;
    }


    public void run() {
        logger.info("开始检查。。。。" + topicPartition);

        KafkaProducer<String, String> producer = null;
        OutputStream outs = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd/HH:mm:ss");
        ConsumerRecord<String, String> recordSimple = null;
        long validCount = 0;     // 满足区间的有效数据条数
        long invalidCnt = 0; // 满足区间的无效数据条数
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(this.kafkaPros)) {
            // 时间提取表达式
            String timeRegexpConf = this.useConf.getProperty("time.regexp", null);
            if (timeRegexpConf == null)
                throw new IllegalArgumentException("time.regexp必须配置; configurations: " + this.kafkaPros.toString());
            else {
                logger.info("时间提取表达式: " + timeRegexpConf);
            }
            long timeReduceMs = Long.parseLong(this.useConf.getProperty("time.reduce.ms", "600000"));
            logger.info("有效数据定义。数据时间和kafka的时间戳（要kafka设置打开）相隔时间差 {} ms", timeReduceMs);
            Pattern timeRegex = Pattern.compile(timeRegexpConf, Pattern.CASE_INSENSITIVE);
            String timeOrigin = this.useConf.getProperty("time.origin", null);
            if (!("value".equals(timeOrigin) || "kafka-timestamp".equals(timeOrigin))) {
                throw new IllegalArgumentException("time.origin仅支持:value, kafka-timestamp; configurations: " + this.kafkaPros.toString());
            }

            if (model.equals("rewrite")) producer = new KafkaProducer<>(this.kafkaPros);
            else if (model.equals("export")) {
                File file = new File("export/" + topicPartition.topic(), topicPartition.topic() + "_" + topicPartition.partition() + ".csv");
                if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                outs = Files.newOutputStream(file.toPath());
                outs.write("partition,offset,timestamp,timestamp_ms,key,value\n".getBytes(StandardCharsets.UTF_8));
            } else if (!("check".equals(model))) {
                throw new IllegalAccessException("run.model错误，仅支持 rewrite,export,check");
            }

            String timeRangeStr = toString(df, timeRanges);
            long lastTime = System.currentTimeMillis();
            consumer.assign(Collections.singleton(topicPartition));
            long timeout;
            int retryCount = 5;
            long maxTimeMS = getMaxTime(timeRanges);
            over_break:
            while (true) {
                try {
                    timeout = System.currentTimeMillis();
                    ConsumerRecords<String, String> records = consumer.poll(TIMEOUT);
                    if (records.count() == 0 && (System.currentTimeMillis() - timeout >= TIMEOUT)) {
                        if (retryCount-- < 0) {
                            logger.info("=>retry 5 times，fetch timeout: " + (System.currentTimeMillis() - timeout));
                            break;
                        }
                        continue;
                    } else if (records.count() == 0) break;
                    retryCount = 5;
                    for (ConsumerRecord<String, String> record : records) {
                        recordSimple = record;
                        long recordTimeMS = "kafka-timestamp".equals(timeOrigin) ? record.timestamp() : parseTime(record, timeRegex, timeUnit);
                        // 计算时间是否大于最大的检查时间范围(不包含上限)
                        if (recordTimeMS >= maxTimeMS) {
                            logger.info("=>检查到数据大于所有条件【{}】,MaxTime:{}，满足条件的有效数据{}，满足条件的无效数据{}，检查结束 {}"
                                    , model, maxTimeMS, validCount, invalidCnt, printHealth(record, recordTimeMS, timeRangeStr, validCount, df));
                            break over_break;
                        }
                        // 判断数据是否有效（解决指定时间范围可能存在多次的补录记录，区分中里面合理数据）
                        if (!isEffectiveData(record.timestamp(), recordTimeMS, timeReduceMs)) {
                            invalidCnt++;
                            continue;
                        }
                        if (System.currentTimeMillis() - lastTime > 10000) {
                            lastTime = System.currentTimeMillis();
                            logger.info(printHealth(record, recordTimeMS, timeRangeStr, validCount, df));
                        }

                        // 判定每条记录是否合法，时间出现在指定区间，出现则根据配置将数据输出到相应sink
                        if (recordTimeMS == -1 || isMath(recordTimeMS, timeRanges)) {
                            validCount++;
                            partitionStatistic.computeIfAbsent(dataPartition(record.topic(), recordTimeMS), k -> new LongAdder()).increment();
                            if ("export".equals(model)) {
                                write(outs, df, record, recordTimeMS);
                            } else if ("rewrite".equals(model)) {
                                producer.send(new ProducerRecord<>(record.topic(), record.partition(), record.key(), record.value()));
                            } // check模式
                        }
                    }
                } catch (Throwable e) {
                    logger.error("TopicPartition: " + topicPartition.toString() + " ERROR: " + e.toString(), e);
                }
            }
            logger.info("检查结束");
        } catch (Exception e) {
            logger.error(e.toString() + " Sample: " + recordSimple, e);
        } finally {
            logger.info("分区{} 已扫描区域基于10分钟分区记录统计详情{}", topicPartition, printPartitionStatic(partitionStatistic));
            logger.info("分区{} 分区满足条件的有效数据{}，分区满足条件的无效数据{}, 主题{}内满足条件的有效数量总计{}"
                    , topicPartition, validCount, invalidCnt, topicPartition.topic()
                    , partitionStatistic.values().stream().map(LongAdder::sum).reduce(Long::sum).orElse(0L));
            if (producer != null) producer.close();
            if (outs != null) {
                try {
                    outs.close();
                } catch (IOException e) {
                    logger.error(e.toString(), e);
                }
            }
        }
    }

    private String printPartitionStatic(Map<String, LongAdder> map) {
        List<String> list = new ArrayList<>(map.keySet()).stream().sorted().collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String str : list) {
            sb.append("\"").append(str).append("\"").append(":").append(map.get(str)).append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    /**
     * 有效数据定义。数据时间和kafka的时间戳（要kafka设置打开）相隔时间差是否在指定的时间内
     *
     * @param timestamp    kafka的时间戳(ms)
     * @param recordTime   记录时间(ms)
     * @param timeReduceMs 时间差值范围
     * @return true 有效 ，反之无效
     */
    private boolean isEffectiveData(long timestamp, long recordTime, long timeReduceMs) {
        return Math.abs(timestamp - recordTime) < timeReduceMs;
    }

    private boolean isMath(long recordTime, long[][] timeRanges) {
        for (long[] timeRange : timeRanges) {
            if (recordTime >= timeRange[0] && recordTime < timeRange[1]) {
                return true;
            }
        }
        return false;
    }

    private long getMaxTime(long[][] timeRanges) {
        long maxTime = 0;
        for (long[] t : timeRanges) {
            maxTime = Math.max(t[1], maxTime);
        }
        StringBuilder builder = new StringBuilder();
        Arrays.stream(timeRanges).forEach(i -> builder.append(Arrays.toString(i)));
        logger.info("MaxTime:{}, Ranges:{}", maxTime, builder.toString());
        return maxTime;
    }

    private void write(OutputStream outs, SimpleDateFormat df, ConsumerRecord<String, String> record, long recordTime) throws IOException {
        String line = record.partition() + "," +
                record.offset() + "," +
                df.format(recordTime) + "," +
                recordTime + "," +
                record.key() + "," +
                record.value() + "\n";
        outs.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private String dataPartition(String topic, long recordTime) {
        if (recordTime == -1) return topic + "_-1";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(recordTime);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE, minute / 10 * 10);
        return topic + "_" + calendar.get(Calendar.YEAR)
                + tow(calendar.get(Calendar.MONTH) + 1)
                + tow(calendar.get(Calendar.DAY_OF_MONTH))
                + tow(calendar.get(Calendar.HOUR_OF_DAY))
                + tow(calendar.get(Calendar.MINUTE));
    }

    private String tow(int num) {
        return num < 10 ? "0" + num : String.valueOf(num);
    }

    /**
     * 根据正则表达式计算数据时间
     *
     * @param record   kafka一条记录
     * @param pattern  提取时间的正则表达式
     * @param timeUnit 数据时间单位
     * @return ms时间
     */
    private long parseTime(ConsumerRecord<String, String> record, Pattern pattern, TimeUnit timeUnit) {
        Matcher m = pattern.matcher(record.value());
        if (m.find()) {
            long recordTime = Long.parseLong(m.group(1));
            return timeUnit.equals(TimeUnit.SECONDS) ? recordTime * 1000 : recordTime;
        } else {
            throw new RuntimeException("从数据中抽取时间算法错误，未抽取到时间");
        }
    }

    private String printHealth(ConsumerRecord<String, String> record, long recordTime, String timeRangeStr, long rewriteCount, SimpleDateFormat df) {
        return Thread.currentThread().getName() + "=>" +
                " topic:" + record.topic() +
                " partition:" + record.partition() +
                " offset:" + record.offset() +
                " timestamp:" + record.timestamp() +
                " recordTime:" + recordTime +
                " recordTime:" + df.format(recordTime) +
                " rewriteCount:" + rewriteCount +
                " timeRange:" + timeRangeStr +
                " key:" + record.key() +
                " @record@:" + record.toString() +
                " Configuration: " + this.useConf.toString();
    }

    private String toString(SimpleDateFormat df, long[][] timeRanges) {
        StringBuilder builder = new StringBuilder();
        for (long[] t : timeRanges) {
            builder.append(df.format(t[0])).append("-").append(df.format(t[1])).append(" ");
        }
        return builder.toString();
    }

}
