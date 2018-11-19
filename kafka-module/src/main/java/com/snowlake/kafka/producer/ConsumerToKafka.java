package com.snowlake.kafka.producer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Collections;
import java.util.Properties;

public class ConsumerToKafka {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "master01:9092,master02:9092,slave01:9092");
        props.put("group.id", "test_test_2");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest"); //latest earliest

        String topic = "guizhou-test_2";
        byteArray(props, topic);
    }

    private static void string(Properties props, String topic) {
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            int count = 0;
            long start = System.currentTimeMillis();
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                if (System.currentTimeMillis() - start > 60000) break;
                for (ConsumerRecord<String, String> record : records)
                    System.out.printf("%d offset = %d, key = %s, value = %s\n"
                            , count++
                            , record.offset()
                            , record.key(), record.value());
            }
            System.out.println("END");
        }
    }

    private static void byteArray(Properties props, String topic) {
        props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        try (KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            int count = 0;
            long start = System.currentTimeMillis();
            while (true) {
                ConsumerRecords<byte[], byte[]> records = consumer.poll(100);
                if (System.currentTimeMillis() - start > 60000) break;
                for (ConsumerRecord<byte[], byte[]> record : records)
                    System.out.printf("%d offset = %d, key = %s, value = %s\n"
                            , count++
                            , record.offset()
                            , "", new String(record.value()));
            }
            System.out.println("END");
        }
    }
}
