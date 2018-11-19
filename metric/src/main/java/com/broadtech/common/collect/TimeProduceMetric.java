package com.broadtech.common.collect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TimeProduceMetric implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CollectServer.class);
    private final KeyPerMetric metric;
    private boolean running;

    public TimeProduceMetric(KeyPerMetric metric) {
        running = true;
        this.metric = metric;
    }

    public void close() {
        running = false;
    }

    @Override
    public void run() {
        Random random = new Random();
        while (running) {
            metric.addBytes(random.nextInt(1000));
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (Exception e) {
                logger.error(e.toString(), e);
            }
        }
    }
}
