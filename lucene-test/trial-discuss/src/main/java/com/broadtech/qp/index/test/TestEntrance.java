package com.broadtech.qp.index.test;

import com.broadtech.bdp.common.ctl.RichCtlConfig;
import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.ThreadAssistant;
import com.broadtech.qp.index.ResourcesUtil;
import com.broadtech.qp.index.status.RuntimeStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 */
public class TestEntrance {
    private final static Logger logger = Logger.getLogger(TestEntrance.class);
    private final static List<BaseIndexProcessor> processors = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String[] dirs = args[0].split(",");
        RuntimeStatus status = new RuntimeStatus();
        ResourcesUtil resourcesUtil = new ResourcesUtil(status);
        DataQueue dataQueue = new DataQueue(resourcesUtil, dirs);

        initEnv(resourcesUtil);
        printStatistic("args: " + Arrays.toString(args));

        if (args.length > 1) {
            specificTest(Integer.parseInt(args[1]), dataQueue, resourcesUtil, status);
        } else {
            scheduleTest(dataQueue, resourcesUtil, status);
        }
    }

    private static void specificTest(int type, DataQueue dataQueue, ResourcesUtil resourcesUtil, RuntimeStatus status) {
        BaseIndexProcessor indexProcessor = null;
        List<RichCtlConfig> ctls = resourcesUtil.getCtlList();
        for (RichCtlConfig ctl : ctls) {
            switch (type) {
                case 0:
                    indexProcessor = new DocsIndexProcessor(status, ctl.getCtlConfig());
                    break;
                case 1:
                    indexProcessor = new DocsStoredIndexProcessor(status, ctl.getCtlConfig());
                    break;
                case 2:
                    indexProcessor = new DocsFreqsIndexProcessor(status, ctl.getCtlConfig());
                    break;
                case 3:
                    indexProcessor = new DocsFreqsPosIndexProcessor(status, ctl.getCtlConfig());
                    break;
                case 4:
                    indexProcessor = new AllProcessor(status, ctl.getCtlConfig());
                    break;
                case 5:
                    indexProcessor = new AllAndDocValuesProcessor(status, ctl.getCtlConfig());
                    break;
                default:
                    indexProcessor = new DocsIndexProcessor(status, ctl.getCtlConfig());
            }
            indexProcessor.setDataQueue(dataQueue);
            processors.add(indexProcessor); // TODO
            new Thread(indexProcessor, "indexProcessor_" + ctl.getCtlConfig().ctlId).start();
        }
    }

    private static void scheduleTest(DataQueue dataQueue, ResourcesUtil resourcesUtil, RuntimeStatus status) {
        long exeTimeLen = 5l * 60 * 60 * 1000; // 5h
        exeTimeLen = 5l * 60 * 1000; // 5m
        String desc = "Only documents are indexed";
        logger.info("开始测试 [" + desc + "]性能");
        specificTest(0, dataQueue, resourcesUtil, status);
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        printStatistic(desc + "\n" + status.getLashStatus() + "\n");
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        stopProcessors(desc);

        desc = "Only documents are indexed and stored";
        logger.info("开始测试 [" + desc + "]性能");
        specificTest(1, dataQueue, resourcesUtil, status);
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        printStatistic(desc + "\n" + status.getLashStatus() + "\n");
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        stopProcessors(desc);

        desc = "Only documents and term frequencies are indexed";
        logger.info("开始测试 [" + desc + "]性能");
        specificTest(2, dataQueue, resourcesUtil, status);
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        printStatistic(desc + "\n" + status.getLashStatus() + "\n");
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        stopProcessors(desc);

        desc = "Indexes documents, frequencies and positions";
        logger.info("开始测试 [" + desc + "]性能");
        specificTest(3, dataQueue, resourcesUtil, status);
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        printStatistic(desc + "\n" + status.getLashStatus() + "\n");
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        stopProcessors(desc);

        desc = "Indexes documents, frequencies, positions and offsets";
        logger.info("开始测试 [" + desc + "]性能");
        specificTest(4, dataQueue, resourcesUtil, status);
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        printStatistic(desc + "\n" + status.getLashStatus() + "\n");
        ThreadAssistant.sleep(exeTimeLen / 2, TimeUnit.MILLISECONDS);
        stopProcessors(desc);
    }

    private static void stopProcessors(String desc) {
        logger.info("准备关闭 [" + desc + "] 索引构建处理器");
        CountDownLatch cdl = new CountDownLatch(processors.size());
        processors.forEach(p -> p.close(cdl));
        try {
            cdl.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            processors.clear();
        }
    }

    private static void printStatistic(String desc) {
        Path statisticPath = Paths.get("statistic.out");
        int times = 0;
        logger.info("开始搜集统计数据 => " + desc);
        while (times < 3)
            try {
                Files.write(statisticPath, desc.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                times++;
                ThreadAssistant.sleep(65, TimeUnit.SECONDS);
            } catch (IOException e) {
                logger.error("收集统计信息遇到异常", e);
            }
    }

    private static void initEnv(final ResourcesUtil resourcesUtil) {
        Thread statusTh = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger statusLogger = Logger.getLogger(RuntimeStatus.class);
                while (true) {
                    ThreadAssistant.sleep(1, TimeUnit.MINUTES);
                    resourcesUtil.getStatus().printStatus(statusLogger);
                }
            }
        }, "status");
        statusTh.setDaemon(true);
        statusTh.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                CountDownLatch cdl = new CountDownLatch(processors.size());
                processors.forEach(p -> p.close(cdl));
                try {
                    cdl.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    Runtime.getRuntime().halt(0);
                }
            }
        }));
    }
}
