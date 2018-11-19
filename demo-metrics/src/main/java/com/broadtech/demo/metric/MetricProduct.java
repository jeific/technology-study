package com.broadtech.demo.metric;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.codahale.metrics.jvm.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.util.JSONPObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Metric结构：自定义 + VM
 */
class MetricProduct {
    private ObjectMapper mapper;
    private ObjectMapper dropWizardMapper;
    private final MetricRegistry useMetricRegistry;
    private final MetricRegistry classLoading = new MetricRegistry();
    private final MetricRegistry memory = new MetricRegistry();
    private final MetricRegistry threadStates = new MetricRegistry();
    private final MetricRegistry jvmAttribute = new MetricRegistry();
    private final MetricRegistry garbageCollector = new MetricRegistry();
    private final MetricRegistry cachedThreadStates = new MetricRegistry();
    private final MetricRegistry bufferPool = new MetricRegistry();

    MetricProduct(MetricRegistry useMetricRegistry, MetricsModule module) {
        this.useMetricRegistry = useMetricRegistry;
        this.mapper = new ObjectMapper();
        this.dropWizardMapper = new ObjectMapper().registerModule(module);
        registerVm();
    }

    private void registerVm() {
        ClassLoadingGaugeSet classLoadingGaugeSet = new ClassLoadingGaugeSet(ManagementFactory.getClassLoadingMXBean());
        MemoryUsageGaugeSet memoryUsageGaugeSet = new MemoryUsageGaugeSet();
        ThreadStatesGaugeSet threadStatesGaugeSet = new ThreadStatesGaugeSet();
        JvmAttributeGaugeSet jvmAttributeGaugeSet = new JvmAttributeGaugeSet();
        GarbageCollectorMetricSet garbageCollectorMetricSet = new GarbageCollectorMetricSet();
        CachedThreadStatesGaugeSet cachedThreadStatesGaugeSet = new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS);
        BufferPoolMetricSet bufferPoolMetricSet = new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer());

        classLoading.register(MetricRegistry.name("java.lang.ClassLoading"), classLoadingGaugeSet);
        memory.register(MetricRegistry.name("java.lang.Memory"), memoryUsageGaugeSet);
        threadStates.register(MetricRegistry.name("java.lang.ThreadStates"), threadStatesGaugeSet);
        jvmAttribute.register(MetricRegistry.name("java.lang.JvmAttribute"), jvmAttributeGaugeSet);
        garbageCollector.register(MetricRegistry.name("java.lang.GarbageCollector"), garbageCollectorMetricSet);
        cachedThreadStates.register(MetricRegistry.name("java.util.CachedThreadStates"), cachedThreadStatesGaugeSet);
        bufferPool.register(MetricRegistry.name("java.nio.BufferPool"), bufferPoolMetricSet);
    }

    private ObjectWriter getWriter(ObjectMapper mapper, boolean prettyPrint) {
        return prettyPrint ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
    }

    void output(OutputStream out, boolean pretty, String function) throws IOException {
        JSONPObject obj = new JSONPObject(function, this.useMetricRegistry);
        output(obj, out, pretty);
    }

    void output(OutputStream out, boolean pretty) throws IOException {
        output(this.useMetricRegistry, out, pretty);
    }

    private void output(Object userMetrics, OutputStream out, boolean pretty) throws IOException {
        ArrayList<Object> items = new ArrayList<>();
        normal(items, userMetrics);
        String langCategory = "java.lang";
        normalVm(items, this.classLoading, langCategory, "classLoading");
        normalVm(items, this.memory, langCategory, "memory");
        normalVm(items, this.threadStates, langCategory, "threadStates");
        normalVm(items, this.jvmAttribute, langCategory, "jvmAttribute");
        normalVm(items, this.garbageCollector, langCategory, "garbageCollector");
        normalVm(items, this.cachedThreadStates, "java.util", "cachedThreadStates");
        normalVm(items, this.bufferPool, "java.nio", "bufferPool");
        Map<String, Object> json = new HashMap<>();
        json.put("beans", items);
        getWriter(mapper, pretty).writeValue(out, json);
    }

    private void normalVm(ArrayList<Object> items, MetricRegistry metricRegistry, String name, String type) throws IOException {
        JsonNode metricsNode = getJsonNode(metricRegistry);
        JsonNode gauges = metricsNode.path("gauges");
        String baseName = name + "." + type;
        Map<String, Object> map = new HashMap<>();
        items.add(map);
        map.put("name", name + ":type=" + type);
        map.put("modelerType", "JvmMetrics");
        Iterator<Map.Entry<String, JsonNode>> itr = gauges.fields();
        while (itr.hasNext()) {
            Map.Entry<String, JsonNode> next = itr.next();
            String[] splits = next.getKey().substring(baseName.length() + 1).split("\\.");
            Map<String, Object> child = map;
            for (int i = 0; i < splits.length - 1; i++) {
                child = (Map<String, Object>) child.computeIfAbsent(splits[i], k -> new HashMap<String, Map<String, Object>>());
            }
            child.put(splits[splits.length - 1], asValue(next.getValue().findValue("value")));
        }
    }

    private void normal(ArrayList<Object> result, Object metricRegistry) throws IOException {
        JsonNode metricsNode = getJsonNode(metricRegistry);
        tranGauges(result, metricsNode.path("gauges"));
        trans(result, metricsNode.path("counters"));
        trans(result, metricsNode.path("histograms"));
        trans(result, metricsNode.path("meters"));
        trans(result, metricsNode.path("timers"));
    }

    private JsonNode getJsonNode(Object metricRegistry) throws IOException {
        ByteArrayOutputStream byteArrayOuts = new ByteArrayOutputStream();
        dropWizardMapper.writeValue(byteArrayOuts, metricRegistry);
        byte[] original = byteArrayOuts.toByteArray();
        String metricJson = new String(original, StandardCharsets.UTF_8);
        return this.mapper.reader().readTree(metricJson);
    }

    private void tranGauges(ArrayList<Object> metricList, JsonNode gauges) {
        Map<String, Map<String, Object>> metricCache = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> itr = gauges.fields();
        while (itr.hasNext()) {
            Map.Entry<String, JsonNode> next = itr.next();
            String[] split = next.getKey().split("\\.");
            String key = split[split.length - 1];
            if (key.equalsIgnoreCase("name")) key += "_";
            getGauges(metricCache, next.getKey()).put(key, asValue(next.getValue().findValue("value")));
        }
        metricCache.forEach((k, v) -> {
            if (!k.isEmpty()) metricList.add(v);
        });
    }

    private void trans(ArrayList<Object> metricList, JsonNode node) {
    }

    private Map<String, Object> getGauges(Map<String, Map<String, Object>> metricCache, String name) {
        String[] strs = name.split("\\.");
        String key = "";
        if (strs.length > 2) {
            String[] newName = Arrays.copyOf(strs, strs.length - 2);
            key = MetricRegistry.name("", newName) + ":type=" + strs[strs.length - 2];
        } else if (strs.length == 2) {
            key = strs[0] + ":type=default";
        }
        return metricCache.computeIfAbsent(key, k -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", k);
            return map;
        });
    }

    private Object asValue(JsonNode node) {
        if (node.isInt()) return node.asInt();
        else if (node.isDouble()) return node.asDouble();
        else return node.asText();
    }
}
