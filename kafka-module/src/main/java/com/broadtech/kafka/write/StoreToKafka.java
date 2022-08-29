package com.broadtech.kafka.write;

import com.broadtech.kafka.SysConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class StoreToKafka {
    private static final Logger logger = LoggerFactory.getLogger(StoreToKafka.class);
    private static Map<String, LongAdder> cnt = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        Properties conf = SysConfig.loadConfig("write_to_kafka.properties");
        Properties kafkaConfig = SysConfig.getKafkaPro(conf);
        String dataDir = conf.getProperty("data.dir");
        if (dataDir == null || !new File(dataDir).exists()) {
            logger.error("必须配置data.dir参数，表示数据文件的路径，然后再该路径下，以子目录的方式放置主题数据，子目录名等于topic");
            return;
        }
        File[] files = new File(dataDir).listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                File[] partitions = f.listFiles();
                String topic = f.getName();
                for (int i = 0; partitions != null && i < partitions.length; i++) {
                    run(topic, partitions[i].toPath(), kafkaConfig);
                }
            }
        }
    }

    private static void run(String topic, Path path, Properties kafkaConfig) {
        new Thread(() -> {
            try {
                logger.info("Topic:{}, 开始写入{}到kafka,{}", topic, path, kafkaConfig);
                write(topic, path, kafkaConfig);
                logger.info("Topic:{}, {}写出结束, 当前所有主体输出记录数:{}", topic, path, cnt);
            } catch (Exception e) {
                logger.error(path.toString(), e);
            }
        }).start();
    }

    private static void write(String topic, Path path, Properties kafkaConfig) throws IOException {
        KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaConfig);
        long time = System.currentTimeMillis();
        LongAdder count = cnt.computeIfAbsent(topic, k -> new LongAdder());
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line = reader.readLine(); // 排除第一行
            String value;
            while ((line = reader.readLine()) != null) {
                value = getVale(line);
                if (value != null) {
                    count.increment();
                    producer.send(new ProducerRecord<>(topic, null, value));
                }
                if (System.currentTimeMillis() - time > 5000) {
                    logger.info("{} 发送记录数:{}", topic, count.sum());
                    time = System.currentTimeMillis();
                }
            }
        }
    }

    /**
     * line结构: partition,offset,timestamp,timestamp_ms,key,value
     */
    private static String getVale(String line) {
        int cnt = 0;
        int index = 0;
        for (char ch : line.toCharArray()) {
            index++;
            if (ch == ',')
                cnt++;
            if (cnt == 5) {
                return line.substring(index);
            }
        }
        return null;
    }
}
