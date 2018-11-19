package com.broadtech.demo.metric;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Processor implements Runnable {
    private final KeyPerMetric metric;
    private boolean running;

    public Processor(KeyPerMetric metric) {
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
            Utils.sleep(100, TimeUnit.MILLISECONDS);
        }
    }
}
