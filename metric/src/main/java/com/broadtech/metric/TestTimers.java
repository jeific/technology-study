package com.broadtech.metric;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * User: hzwangxx
 * Date: 14-2-17
 * Time: 18:34
 * 测试Timers<br>
 * ==================================================================
 * Timers主要是用来统计某一块代码段的执行时间以及其分布情况，具体是基于Histograms和Meters来实现的
 */
public class TestTimers {
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
    private static final Timer requests = metrics.timer(MetricRegistry.name(TestTimers.class, "request"));

    public static void handleRequest(int sleep) {
        Timer.Context context = requests.time();
        try {
            //some operator
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            context.stop();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        JmxReporter.forRegistry(metrics).build().start();
        reporter.start(3, TimeUnit.SECONDS);
        Random random = new Random();
        while (true) {
            handleRequest(random.nextInt(1000));
        }
    }

}

/*
14-2-18 9:31:54 ================================================================

-- Timers ----------------------------------------------------------------------
com.netease.test.metrics.TestTimers.request
             count = 4
         mean rate = 1.33 calls/second
     1-minute rate = 0.00 calls/second
     5-minute rate = 0.00 calls/second
    15-minute rate = 0.00 calls/second
               min = 483.07 milliseconds
               max = 901.92 milliseconds
              mean = 612.64 milliseconds
            stddev = 196.32 milliseconds
            median = 532.79 milliseconds
              75% <= 818.31 milliseconds
              95% <= 901.92 milliseconds
              98% <= 901.92 milliseconds
              99% <= 901.92 milliseconds
            99.9% <= 901.92 milliseconds


14-2-18 9:31:57 ================================================================

-- Timers ----------------------------------------------------------------------
com.netease.test.metrics.TestTimers.request
             count = 8
         mean rate = 1.33 calls/second
     1-minute rate = 1.40 calls/second
     5-minute rate = 1.40 calls/second
    15-minute rate = 1.40 calls/second
               min = 41.07 milliseconds
               max = 968.19 milliseconds
              mean = 639.50 milliseconds
            stddev = 306.12 milliseconds
            median = 692.77 milliseconds
              75% <= 885.96 milliseconds
              95% <= 968.19 milliseconds
              98% <= 968.19 milliseconds
              99% <= 968.19 milliseconds
            99.9% <= 968.19 milliseconds


14-2-18 9:32:00 ================================================================

-- Timers ----------------------------------------------------------------------
com.netease.test.metrics.TestTimers.request
             count = 15
         mean rate = 1.67 calls/second
     1-minute rate = 1.40 calls/second
     5-minute rate = 1.40 calls/second
    15-minute rate = 1.40 calls/second
               min = 41.07 milliseconds
               max = 968.19 milliseconds
              mean = 591.35 milliseconds
            stddev = 302.96 milliseconds
            median = 650.56 milliseconds
              75% <= 838.07 milliseconds
              95% <= 968.19 milliseconds
              98% <= 968.19 milliseconds
              99% <= 968.19 milliseconds
            99.9% <= 968.19 milliseconds

*/
