package com.broadtech.metric;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 研究:
 */
public class SelfTest {
    private static final MetricRegistry metrics = new MetricRegistry();
    private static final Histogram self = metrics.histogram(MetricRegistry.name(SelfTest.class, "histogram"));

    private static void handle(double v) {
        self.update((int) (v * 100));
    }

    public static void main(String[] args) throws InterruptedException {
        ConsoleReporter.forRegistry(metrics).build().start(5, TimeUnit.SECONDS);
        Random random = new Random();
        while (true) {
            handle(random.nextDouble());
            Thread.sleep(1000);
        }
    }
}
