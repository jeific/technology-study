package com.broadtech.kafka.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Properties;

public class Consumer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.95.235:9092");
        props.put("group.id", "test6");
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        /**
         * Append into table n603_c_11 FIELDS TERMINATED BY X'03ff' TRAILING NULLCOLS
         * (
         * svr_id string,
         * server_time bigint,
         * cmd string,
         * platform_id string
         * )
         */
        String topic = "n603_c_11";
        read(props, new TopicPartition(topic, 0));
    }

    private static void read(Properties props, TopicPartition topicPartition) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd/HH:mm:ss");
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
        try {
            consumer.assign(Collections.singleton(topicPartition));
            ConsumerRecords<Long, String> records = consumer.poll(1000);
            for (ConsumerRecord<Long, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s time:%d timeType:%s %n", record.offset(),
                        record.key(), record.value(), record.timestamp(), record.timestampType());
                printHealth(record, df);
            }
            System.out.println("==========" + records.count());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    private static void printHealth(ConsumerRecord<Long, String> record, SimpleDateFormat df) {
        StringBuilder builder = new StringBuilder();
        builder.append(Thread.currentThread().getName()).append("=>");
        builder.append(" topic:").append(record.topic());
        builder.append(" partition:").append(record.partition());
        builder.append(" offset:").append(record.offset());
        builder.append(" timestamp:").append(record.timestamp());
        builder.append(" timestamp:").append(df.format(record.timestamp()));
        builder.append(" key:").append(record.key());
        System.out.println(builder.toString());
    }
}
