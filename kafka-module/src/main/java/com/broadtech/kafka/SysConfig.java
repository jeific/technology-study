package com.broadtech.kafka;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SysConfig {

    public static Properties loadConfig() throws IOException {
        return loadConfig("conf.properties");
    }

    public static Properties loadConfig(String fileName) throws IOException {
        Properties conf = new Properties();
        try (InputStream ins = DataRangeRewrite.class.getResourceAsStream("/" + fileName)) {
            conf.load(ins);
        }
        return conf;
    }

    public static Properties getKafkaPro(Properties conf) {
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
        return props;
    }
}
