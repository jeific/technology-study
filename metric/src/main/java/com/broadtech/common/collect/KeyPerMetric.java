package com.broadtech.common.collect;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.atomic.LongAdder;

/**
 * 自定义指标: 关键性能指标, 含有5个metric
 */
public class KeyPerMetric {
    private static final double ONE_MB = 1024 * 1024d;
    private final LongAdder request = new LongAdder();
    private final LongAdder bytes = new LongAdder();

    public KeyPerMetric(MetricRegistry metricRegistry) {
        metricRegistry.register(MetricRegistry.name("Metrics.KeyPerMetric", "request"), getRequest());
        metricRegistry.register(MetricRegistry.name("Metrics.KeyPerMetric", "bytes"), getBytes());
        metricRegistry.register(MetricRegistry.name("Metrics.KeyPerMetric", "bluewhale_collector_freememory"), getFreeMemory());
        metricRegistry.register(MetricRegistry.name("Metrics.KeyPerMetric", "bluewhale_collector_usablememory"), getUsableMemory());
        metricRegistry.register(MetricRegistry.name("Metrics.KeyPerMetric", "useMemory"), getUseMemory());
    }

    public void addBytes(long addBytes) {
        request.increment();
        bytes.add(addBytes);
    }

    private Gauge<Long> getRequest() {
        return request::longValue;
    }

    private Gauge<Long> getBytes() {
        return bytes::longValue;
    }

    private Gauge<Double> getFreeMemory() {
        return () -> (Runtime.getRuntime().freeMemory() / ONE_MB);
    }

    private Gauge<Double> getUseMemory() {
        return () -> {
            Runtime run = Runtime.getRuntime();
            long total = run.totalMemory();
            long free = run.freeMemory();
            long useMem = total - free;
            return useMem / ONE_MB;
        };
    }

    private Gauge<Double> getUsableMemory() {
        return () -> {
            Runtime run = Runtime.getRuntime();
            long max = run.maxMemory();
            long total = run.totalMemory();
            long free = run.freeMemory();
            long usable = max - total + free;
            return usable / ONE_MB;
        };
    }
}
