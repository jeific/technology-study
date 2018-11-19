package com.broadtech.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class DataRangeRewrite {
    private static final Logger logger = LoggerFactory.getLogger(OffsetCheckAndRewrite.class);

    public static void main(String[] args) throws ParseException, IOException {
        Properties conf = new Properties();
        try (InputStream ins = DataRangeRewrite.class.getResourceAsStream("/conf.properties")) {
            conf.load(ins);
        }
        String[] topic = conf.getProperty("topics").split(",");
        String model = conf.getProperty("run.model");
        String timeUnit = conf.getProperty("time.unit");
        if (model == null) throw new IllegalArgumentException("必须配置run.model参数,取值:export,rewrite,check");
        Properties props = new Properties();
        props.put("bootstrap.servers", conf.getProperty("bootstrap.servers")); // slave03:9092,slave04:9092,slave05:9092
        props.put("group.id", conf.getProperty("group.id"));
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String[] timeRangeConf = conf.getProperty("timeRange").split(",");
        long[][] timeRanges = new long[timeRangeConf.length][];
        for (int i = 0; i < timeRangeConf.length; i++) {
            String[] startEnd = timeRangeConf[i].split("-");
            timeRanges[i] = new long[]{df.parse(startEnd[0]).getTime(), df.parse(startEnd[1]).getTime()};
        }
        logger.info("config time range: " + Arrays.toString(timeRangeConf));
        logger.info("check time range: " + Arrays.toString(timeRanges));

        for (String miniTopic : topic) {
            List<PartitionInfo> partitionInfos = get(props, miniTopic);
            logger.info(miniTopic + " Partitions: " + partitionInfos.size());

            try {
                CountDownLatch cdl = new CountDownLatch(partitionInfos.size());
                for (int i = 0; i < partitionInfos.size(); i++) {
                    run(cdl, props, new TopicPartition(miniTopic, i), timeRanges, model, timeUnit, conf);
                }
                cdl.await();
            } catch (Exception e) {
                logger.error(e.toString(), e);
            }
        }
        logger.info("检查完毕，立即结束");
    }

    private static void run(CountDownLatch cdl, Properties props, TopicPartition topicPartition, long[][] timeRanges
            , String model, String timeUnit, Properties conf) {
        new Thread(() -> {
            try {
                OffsetCheckAndRewrite process = new OffsetCheckAndRewrite(props, topicPartition, timeRanges, model, timeUnit, conf);
                process.run();
            } finally {
                cdl.countDown();
            }
        }, "run_" + topicPartition.partition()).start();
    }

    private static List<PartitionInfo> get(Properties props, String topic) {
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        try {
            return producer.partitionsFor(topic);
        } finally {
            producer.close();
        }
    }
}
