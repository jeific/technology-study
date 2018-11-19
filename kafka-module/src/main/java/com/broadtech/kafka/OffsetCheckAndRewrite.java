package com.broadtech.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OffsetCheckAndRewrite {
    private static final Logger logger = LoggerFactory.getLogger(OffsetCheckAndRewrite.class);
    public static final int TIMEOUT = 5000;
    private static final Map<String, LongAdder> partitionStatistic = new ConcurrentHashMap<>();
    private final Properties kafkaPros;
    private final long[][] timeRanges;
    private final String timeUnit;
    private final String model;
    private final Properties useConf;
    private TopicPartition topicPartition;

    public OffsetCheckAndRewrite(Properties kafkaPros, TopicPartition topicPartition, long[][] timeRanges, String model, String timeUnit, Properties properties) {
        this.kafkaPros = kafkaPros;
        this.topicPartition = topicPartition;
        this.timeRanges = timeRanges;
        this.timeUnit = timeUnit;
        this.model = model;
        this.useConf = properties;
    }


    public void run() {
        logger.info("开始检查。。。。" + topicPartition);

        KafkaProducer<String, String> producer = null;
        OutputStream outs = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd/HH:mm:ss");
        ConsumerRecord<String, String> recordSimple = null;
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(this.kafkaPros)) {
            // 时间提取表达式
            String timeRegexpConf = this.useConf.getProperty("time.regexp", null);
            if (timeRegexpConf == null)
                throw new IllegalArgumentException("time.regexp必须配置; configurations: " + this.kafkaPros.toString());
            else {
                logger.info("时间提取表达式: " + timeRegexpConf);
            }
            Pattern timeRegex = Pattern.compile(timeRegexpConf, Pattern.CASE_INSENSITIVE);
            String timeOrigin = this.useConf.getProperty("time.origin", null);
            if (!("value".equals(timeOrigin) || "kafka-timestamp".equals(timeOrigin))) {
                throw new IllegalArgumentException("time.origin仅支持:value, kafka-timestamp; configurations: " + this.kafkaPros.toString());
            }

            if (model.equals("rewrite")) producer = new KafkaProducer<>(this.kafkaPros);
            else if (model.equals("export")) {
                File file = new File("logs/" + topicPartition.topic(), topicPartition.topic() + "_" + topicPartition.partition() + ".csv");
                if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                outs = new FileOutputStream(file);
                outs.write("partition,offset,timestamp,timestamp_ms,key,value\n".getBytes(StandardCharsets.UTF_8));
            } else if (!("check".equals(model))) {
                throw new IllegalAccessException("run.model错误，仅支持 rewrite,export,check");
            }

            long rewriteCount = 0;
            String timeRangeStr = toString(df, timeRanges);
            long lastTime = System.currentTimeMillis();
            consumer.assign(Collections.singleton(topicPartition));
            long timeout;
            boolean over = false;
            boolean found = false;
            String dataPartition = "";
            int retryCount = 5;
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
                        long recordTime = "kafka-timestamp".equals(timeOrigin) ? record.timestamp() : parseTime(record, timeRegex, timeUnit);

                        if (System.currentTimeMillis() - lastTime > 10000) {
                            lastTime = System.currentTimeMillis();
                            logger.info(printHealth(record, recordTime, timeRangeStr, rewriteCount, df));
                        }
                        over = false;
                        for (long[] timeRange : timeRanges) {
                            if (recordTime >= timeRange[0] && recordTime <= timeRange[1]) {
                                found = true;
                            }
                            if (!found) continue;
                            if (recordTime == -1 || found) {
                                rewriteCount++;
                                partitionStatistic.computeIfAbsent(dataPartition, k -> new LongAdder()).increment();
                                if ("export".equals(model)) {
                                    dataPartition = dataPartition(record.topic(), dataPartition, recordTime);
                                    write(outs, df, record, recordTime);
                                } else if ("rewrite".equals(model)) {
                                    producer.send(new ProducerRecord<>(record.topic(), record.partition(), record.key(), record.value()));
                                } // check模式
                            }
                            if (recordTime > timeRange[1]) over = true;
                        }
                        if (over) {
                            logger.info("=>检查到数据大于所有条件【" + model + "】，检查结束 " + printHealth(record, recordTime, timeRangeStr, rewriteCount, df));
                            break over_break;
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
            logger.info(topicPartition.toString() + "分区记录统计" + partitionStatistic.toString());
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

    private void write(OutputStream outs, SimpleDateFormat df, ConsumerRecord<String, String> record, long recordTime) throws IOException {
        String line = record.partition() + "," +
                record.offset() + "," +
                df.format(recordTime) + "," +
                recordTime + "," +
                record.key() + "," +
                record.value() + "\n";
        outs.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private String dataPartition(String topic, String dataPartition, long recordTime) {
        if (recordTime == -1) return dataPartition + "_";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(recordTime);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE, minute / 10 * 10);
        return topic + "_" + calendar.get(Calendar.YEAR)
                + (calendar.get(Calendar.MONTH) + 1)
                + calendar.get(Calendar.DAY_OF_MONTH)
                + calendar.get(Calendar.HOUR_OF_DAY)
                + calendar.get(Calendar.MINUTE);
    }

    private long parseTime(ConsumerRecord<String, String> record, Pattern pattern, String timeUnit) {
        Matcher m = pattern.matcher(record.value());
        if (m.find()) {
            long recordTime = Long.parseLong(m.group(1));
            return timeUnit.equals("s") ? recordTime * 1000 : recordTime;
        } else {
            return -2;
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
