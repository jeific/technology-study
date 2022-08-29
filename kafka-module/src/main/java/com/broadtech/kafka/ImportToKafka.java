package com.broadtech.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ImportToKafka {
    private final Logger logger = LoggerFactory.getLogger(ImportToKafka.class);
    private final Properties kafkaPros;
    private final String topic;
    private final Properties useConf;
    private final AtomicLong total = new AtomicLong(0);
    private final AtomicInteger cnt = new AtomicInteger(0);
    private long lastTime = System.currentTimeMillis();

    public ImportToKafka(Properties kafkaPros, Properties properties, String topic) {
        this.kafkaPros = kafkaPros;
        this.topic = topic;
        this.useConf = properties;
    }

    public void run() {
        KafkaProducer<String, String> producer = new KafkaProducer<>(this.kafkaPros);
        try {
            int batchSize = Integer.parseInt(useConf.getProperty("import.topic.batch.size", "100"));
            int intervalMs = Integer.parseInt(useConf.getProperty("import.topic.batch.interval.ms", "0"));
            List<String> records = new ArrayList<>();
            File path = new File("export", topic);
            File[] files = path.listFiles();
            if (files != null) {
                for (File f : files) {
                    logger.info("导入文件{}", f.getAbsolutePath());
                    try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                        reader.readLine(); // 排除标题行：partition,offset,timestamp,timestamp_ms,key,value
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.substring(line.indexOf(",") + 1);//offset
                            line = line.substring(line.indexOf(",") + 1);//timestamp
                            line = line.substring(line.indexOf(",") + 1);//timestamp_ms
                            line = line.substring(line.indexOf(",") + 1);//key
                            line = line.substring(line.indexOf(",") + 1);// value
                            records.add(line);
                            if (records.size() >= batchSize) {
                                batchSend(records, producer, topic);
                                if (intervalMs > 0) {
                                    TimeUnit.MILLISECONDS.sleep(intervalMs);
                                }
                            }
                        }
                        batchSend(records, producer, topic);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            logger.info("{}导入完成，最近{}s，写入kafka数据{}条; total:{}条"
                    , topic, (System.currentTimeMillis() - lastTime) / 1000, cnt.getAndSet(0), total.getAndSet(0));
        } finally {
            producer.close();
        }
    }

    private void batchSend(List<String> records, KafkaProducer<String, String> producer, String topic) {
        for (String record : records) {
            producer.send(new ProducerRecord<>(topic, null, record));
            cnt.incrementAndGet();
            total.incrementAndGet();
        }
        records.clear();
        if (System.currentTimeMillis() - lastTime >= 5000) {
            logger.info("{}最近{}s，写入kafka数据{}条", topic, (System.currentTimeMillis() - lastTime) / 1000, cnt.getAndSet(0));
            lastTime = System.currentTimeMillis();
        }
    }
}
