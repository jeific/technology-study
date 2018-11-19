package com.snowlake.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ImportDataToKafka {

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "master01:9092,master02:9092,slave01:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);

        String data = "D:\\测试\\贵州\\n603_c_4_session.txt";
        importString(props, data);
    }

    private static void importString(Properties props, String data) throws IOException {
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        int count = 0;
        String topic = "guizhou-test_2";
        try (Producer<String, String> producer = new KafkaProducer<>(props);
             BufferedReader reader = new BufferedReader(new FileReader(data))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String json = "{\"session\":\"" + line + "\"}";
                producer.send(new ProducerRecord<>(topic, null, json));
                System.out.println(++count + "\t:" + json);
            }
        }
        System.out.println("END");
    }

    private static void importByteArray(Properties props, String data) throws IOException {
        props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        int count = 0;
        String topic = "guizhou-test";
        try (Producer<byte[], byte[]> producer = new KafkaProducer<>(props);
             BufferedReader reader = new BufferedReader(new FileReader(data))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String json = "{\"session\":\"" + line + "\"}";
                producer.send(new ProducerRecord<>(topic, null, json.getBytes(StandardCharsets.UTF_8)));
                System.out.println(++count + "\t:" + json);
            }
        }
        System.out.println("END");
    }
}
