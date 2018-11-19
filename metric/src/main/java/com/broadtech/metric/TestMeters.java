package com.broadtech.metric;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * User: hzwangxx
 * Date: 14-2-17
 * Time: 18:34
 * 测试Meters<br>
 * <p>
 * Meters用来度量某个时间段的平均处理次数（request per second），每1、5、15分钟的TPS。
 * 比如一个service的请求数，通过metrics.meter()实例化一个Meter之后，
 * 然后通过meter.mark()方法就能将本次请求记录下来。统计结果有总的请求数，
 * 平均每秒的请求数，以及最近的1、5、15分钟的平均TPS<br>
 * <p>
 * =================================================
 * Metrics是一个给JAVA服务的各项指标提供度量工具的包，在JAVA代码中嵌入Metrics代码，
 * 可以方便的对业务代码的各个指标进行监控，同时，Metrics能够很好的跟Ganlia、Graphite结合，方便的提供图形化接口
 */
public class TestMeters {
    /**
     * 实例化一个registry，最核心的一个模块，相当于一个应用程序的metrics系统的容器，维护一个Map
     */
    private static final MetricRegistry metrics = new MetricRegistry();

    /**
     * 在控制台上打印输出
     */
    private static ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).build();

    /**
     * 实例化一个Meter
     */
    private static final Meter requests = metrics.meter(name(TestMeters.class, "request"));

    public static void handleRequest() {
        requests.mark();
    }

    public static void main(String[] args) throws InterruptedException {
        JmxReporter.forRegistry(metrics).build().start();
        reporter.start(3, TimeUnit.SECONDS);
        while (true) {
            handleRequest();
            Thread.sleep(100);
        }
    }

}

/*
14-2-17 18:43:08 ===============================================================

-- Meters ----------------------------------------------------------------------
com.netease.test.metrics.TestMeters.request
             count = 30
         mean rate = 9.95 events/second
     1-minute rate = 0.00 events/second
     5-minute rate = 0.00 events/second
    15-minute rate = 0.00 events/second


14-2-17 18:43:11 ===============================================================

-- Meters ----------------------------------------------------------------------
com.netease.test.metrics.TestMeters.request
             count = 60
         mean rate = 9.99 events/second
     1-minute rate = 10.00 events/second
     5-minute rate = 10.00 events/second
    15-minute rate = 10.00 events/second


14-2-17 18:43:14 ===============================================================

-- Meters ----------------------------------------------------------------------
com.netease.test.metrics.TestMeters.request
             count = 90
         mean rate = 9.99 events/second
     1-minute rate = 10.00 events/second
     5-minute rate = 10.00 events/second
    15-minute rate = 10.00 events/second
*/
