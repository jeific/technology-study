package com.broadtech.kafka.test;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Collections;
import java.util.Properties;

public class Seek {

    private final Properties conf;
    private final TopicPartition topicPartition;

    public Seek(Properties conf, TopicPartition topicPartition) {
        this.conf = conf;
        this.topicPartition = topicPartition;
    }

    public void seek(int offset) {
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(this.conf);
        try {
            consumer.assign(Collections.singleton(this.topicPartition));
            consumer.seek(this.topicPartition, offset);
        } finally {
            consumer.close();
        }
    }

}
