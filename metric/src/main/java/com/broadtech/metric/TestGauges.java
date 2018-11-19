package com.broadtech.metric;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * User: hzwangxx
 * Date: 14-2-17
 * Time: 14:47
 * 测试Gauges，实时统计pending状态的job个数<br>
 * Gauges是一个最简单的计量，一般用来统计瞬时状态的数据信息，比如系统中处于pending状态的job<br>
 * ------------------------------------------------------------------------------<br>
 * 通过以上步骤将会向MetricsRegistry容器中注册一个名字为com.netease.test.metrics .TestGauges.pending-job.size的metrics，
 * 实时获取队列长度的指标。另外，Core包种还扩展了几种特定的Gauge：
 * JMX Gauges—提供给第三方库只通过JMX将指标暴露出来。
 * Ratio Gauges—简单地通过创建一个gauge计算两个数的比值。
 * Cached Gauges—对某些计量指标提供缓存
 * Derivative Gauges—提供Gauge的值是基于其他Gauge值的接口。
 * =============================================================================<br>
 * com.codahale.metrics:metric-core包主要提供如下核心功能：
 * Metrics Registries类似一个metrics容器，维护一个Map，可以是一个服务一个实例。
 * 支持五种metric类型：Gauges、Counters、Meters、Histograms和Timers。
 * 可以将metrics值通过JMX、Console，CSV文件和SLF4J loggers发布出来。
 * =============================================================================<br>
 * 其他支持:
 * metrics提供了对Ehcache、Apache HttpClient、JDBI、Jersey、Jetty、Log4J、Logback、JVM等的集成，可
 * 以方便地将Metrics输出到Ganglia、Graphite中，供用户图形化展示。
 * =================================================
 * 参考资料:
 * http://metrics.codahale.com/
 * https://github.com/dropwizard/metrics
 * http://blog.csdn.net/scutshuxue/article/details/8350135
 * http://blog.synyx.de/2013/09/yammer-metrics-made-easy-part-i/
 * http://blog.synyx.de/2013/09/yammer-metrics-made-easy-part-ii/
 * http://wiki.apache.org/hadoop/HADOOP-6728-MetricsV2
 */
public class TestGauges {
    /**
     * 实例化一个registry，最核心的一个模块，相当于一个应用程序的metrics系统的容器，维护一个Map
     */
    private static final MetricRegistry metrics = new MetricRegistry();

    private static Queue<String> queue = new LinkedBlockingDeque<String>();

    /**
     * 在控制台上打印输出
     */
    private static ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).build();

    public static void main(String[] args) throws InterruptedException {
        reporter.start(3, TimeUnit.SECONDS);

        //实例化一个Gauge
        Gauge<Integer> gauge = () -> queue.size();

        //注册到容器中
        metrics.register(MetricRegistry.name(TestGauges.class, "pending-job", "size"), gauge);

        //测试JMX
        JmxReporter jmxReporter = JmxReporter.forRegistry(metrics).build();
        jmxReporter.start();

        //模拟数据
        for (int i = 0; i < 120; i++) {
            queue.add("a");
            Thread.sleep(1000);
        }

    }
}

/*
console output:
18-5-28 21:50:20 ===============================================================

-- Gauges ----------------------------------------------------------------------
com.broadtech.metric.TestGauges.pending-job.size
             value = 3


18-5-28 21:50:23 ===============================================================

-- Gauges ----------------------------------------------------------------------
com.broadtech.metric.TestGauges.pending-job.size
             value = 6


18-5-28 21:50:26 ===============================================================

...
 */