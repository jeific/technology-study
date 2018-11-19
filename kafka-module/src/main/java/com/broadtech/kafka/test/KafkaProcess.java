package com.broadtech.kafka.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Properties;

public class KafkaProcess extends Thread {
    public static final int TIMEOUT = 10000;
    private final Properties conf;
    private final long start;
    private final long end;
    private TopicPartition topicPartition;

    public KafkaProcess(Properties conf, TopicPartition topicPartition, long start, long end) {
        this.conf = conf;
        this.topicPartition = topicPartition;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        check();
    }

    private void check() {
        System.out.println(Thread.currentThread().getName() + "开始检查。。。。");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(this.conf);
        try {
            consumer.assign(Collections.singleton(this.topicPartition));
            long start_offset = -1;
            long start_time = 0;
            long end_offset = -1;
            long end_time = 0;
            long firstOffset = -1;
            long lastTime = System.currentTimeMillis();
            over:
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(TIMEOUT);
                for (ConsumerRecord<String, String> record : records) {
                    if (firstOffset == -1) firstOffset = record.offset();
                    if (start_offset == -1 && record.timestamp() >= start) {
                        start_offset = record.offset();
                        start_time = record.timestamp();
                    }
                    if (end_offset == -1 && record.timestamp() >= end) {
                        end_offset = record.offset();
                        end_time = record.timestamp();
                    }
                    if (start_offset != -1 && end_offset != -1) break over;
                    if (System.currentTimeMillis() - lastTime > 10000) {
                        lastTime = System.currentTimeMillis();
                        System.out.println(Thread.currentThread().getName() + ": firstOffset:" + firstOffset + " currOffset：" + record.offset()
                                + toString(start_offset, start_time, end_offset, end_time));
                    }
                }
                if (firstOffset == -1) {
                    System.out.println(Thread.currentThread().getName() + "没有读到记录");
                    break over;
                }
            }
            System.out.println(toString(start_offset, start_time, end_offset, end_time));
            System.out.println(Thread.currentThread().getName() + "检查结束");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    private void checkAndRewrite() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(this.conf);
        KafkaProducer<String, String> producer = new KafkaProducer<>(this.conf);
        try {
            consumer.assign(Collections.singleton(this.topicPartition));
            ConsumerRecords<String, String> records = consumer.poll(TIMEOUT);
            long start_offset = -1;
            long end_offset = -1;
            for (ConsumerRecord<String, String> record : records) {
                if (record.timestamp() >= start && record.timestamp() <= end) {
                    producer.send(new ProducerRecord<>(record.topic(), record.partition(), record.key(), record.value()));
                    if (start_offset == -1) start_offset = record.offset();
                    end_offset = record.offset();
                }
            }
            System.out.println("start_offset=" + start_offset + ", end_offset=" + end_offset);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
            producer.close();
        }
    }

    private String toString(long start_offset, long start_time, long end_offset, long end_time) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        return " start_offset=" + start_offset + " " + start_time + " " + df.format(start_time)
                + "\nend_offset  =" + end_offset + " " + end_time + " " + df.format(end_time);
    }

    private void read() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(this.conf);
        try {
            consumer.assign(Collections.singleton(this.topicPartition));
            ConsumerRecords<String, String> records = consumer.poll(TIMEOUT);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s time:%d timeType:%s %n", record.offset(),
                        record.key(), record.value(), record.timestamp(), record.timestampType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    private void rewrite() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(this.conf);
        KafkaProducer<String, String> producer = new KafkaProducer<>(this.conf);
        try {
            consumer.assign(Collections.singleton(this.topicPartition));
            ConsumerRecords<String, String> records = consumer.poll(TIMEOUT);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s time:%d timeType:%s %n", record.offset(),
                        record.key(), record.value(), record.timestamp(), record.timestampType());
                producer.send(new ProducerRecord<>(record.topic(), record.partition(), record.key(), record.value()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
            producer.close();
        }
    }
}
