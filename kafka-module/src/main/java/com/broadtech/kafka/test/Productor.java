package com.broadtech.kafka.test;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;

public class Productor {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.95.235:9092"); // slave03:9092,slave04:9092,slave05:9092
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

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
        product(props, topic);
//        KafkaProducer<Long, String> productor = new KafkaProducer(props);
//        List<PartitionInfo> ps = productor.partitionsFor(topic);
    }

    private static void product(Properties props, String topic) {
        KafkaProducer<String, String> productor = new KafkaProducer(props);
        for (int i = 0; i < 10; i++) {
            String svr_id = String.valueOf(new Random().nextInt());
            long server_time = System.currentTimeMillis();
            String cmd = "default";
            String platform_id = String.valueOf(i);
            StringBuilder builder = new StringBuilder();
            builder.append(svr_id).append("03ff")
                    .append("\"server_time\":").append(server_time).append("03ff")
                    .append(cmd).append("03ff")
                    .append(platform_id);
            productor.send(new ProducerRecord<>(topic, String.valueOf(i), builder.toString()));
        }
        productor.close();
    }
}
