package com.broadtech.demo.metric;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jvm.*;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Demo {

    public static void main(String[] args) throws Exception {
        MetricRegistry metrics = initMetricEnv(); // 初始化metric容器
        KeyPerMetric keyPerMetric = new KeyPerMetric(metrics); // 实例化自定义metric
        List<Processor> funcs = new ArrayList<>(5);
        for (int i = 0; i < 10; i++) {
            Processor p = new Processor(keyPerMetric); // 更新指标
            new Thread(p).start();
            funcs.add(p);
        }
        MetricsJettyServer jettyServer = new MetricsJettyServer();
        jettyServer.start(metrics);
        // Utils.sleep(5, TimeUnit.MINUTES)
        Thread.currentThread().join();
        funcs.forEach(Processor::close);
        jettyServer.stop();
    }

    private static MetricRegistry initMetricEnv() {
        MetricRegistry metric = new MetricRegistry();
        JmxReporter.forRegistry(metric).build().start();
        // ConsoleReporter.forRegistry(metric).build().start(3, TimeUnit.SECONDS);
        return metric;
    }
}
