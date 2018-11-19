package com.broadtech.common.collect;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 提供log和metric收集服务
 */
public class CollectServer {
    private static final Logger logger = LoggerFactory.getLogger(CollectServer.class);

    public static void main(String[] args) {
        List<TimeProduceMetric> funcs = new ArrayList<>(5);
        try {
            Properties conf = load();
            MetricRegistry metrics = initMetricEnv(); // 初始化metric容器
            KeyPerMetric keyPerMetric = new KeyPerMetric(metrics); // 实例化自定义metric
            for (int i = 0; i < 10; i++) {
                TimeProduceMetric p = new TimeProduceMetric(keyPerMetric); // 更新指标
                new Thread(p).start();
                funcs.add(p);
            }
            EmbedJettyServer jettyServer = new EmbedJettyServer(Integer.parseInt(conf.getProperty("bdp.http.metrics.port")));
            jettyServer.start(metrics);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                funcs.forEach(TimeProduceMetric::close);
                jettyServer.stop();
            }));
            logger.info("collector server stared");
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }

    private static Properties load() throws IOException {
        try (InputStream ins = CollectServer.class.getResourceAsStream("/bdp-monitoring.properties")) {
            Properties pro = new Properties();
            pro.load(ins);
            return pro;
        }
    }

    private static MetricRegistry initMetricEnv() {
        MetricRegistry metric = new MetricRegistry();
        JmxReporter.forRegistry(metric).build().start();
        //ConsoleReporter.forRegistry(metric).build().start(3, TimeUnit.SECONDS);
        return metric;
    }
}
