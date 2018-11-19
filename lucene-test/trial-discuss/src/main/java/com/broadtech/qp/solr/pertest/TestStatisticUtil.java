package com.broadtech.qp.solr.pertest;

import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.ThreadAssistant;
import com.broadtech.qp.index.ResourcesUtil;
import com.broadtech.qp.index.status.RuntimeStatus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeifi on 2017/8/9.
 */
public class TestStatisticUtil {
    private final static Logger logger = Logger.getLogger(TestStatisticUtil.class);

    public static void printStatistic(String desc) {
        if (Objects.isNull(desc)) return;
        Path statisticPath = Paths.get("statistic.out");
        int times = 0;
        logger.info("开始搜集统计数据 => " + desc);
        try {
            Files.write(statisticPath, desc.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            logger.error("收集统计信息遇到异常", e);
        }
    }

    public static void ready(final ResourcesUtil resourcess, Closeable stopApp) {
        Thread statusTh = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger statusLogger = Logger.getLogger(RuntimeStatus.class);
                while (true) {
                    ThreadAssistant.sleep(1, TimeUnit.MINUTES);
                    resourcess.getStatus().printStatus(statusLogger);
                }
            }
        }, "status");
        statusTh.setDaemon(true);
        statusTh.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stopApp.close();
                } catch (IOException e) {
                    logger.error("关闭 >> " + stopApp.getClass() + " 异常", e);
                }
            }
        }));
    }
}
