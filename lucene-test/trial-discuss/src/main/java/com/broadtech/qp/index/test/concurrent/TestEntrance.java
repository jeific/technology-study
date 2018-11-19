package com.broadtech.qp.index.test.concurrent;

import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.ThreadAssistant;
import com.broadtech.qp.index.ResourcesUtil;
import com.broadtech.qp.index.status.RuntimeStatus;
import com.broadtech.qp.index.test.DataQueue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/7/25.
 * 测试单张表多线程性能
 */
public class TestEntrance {
    private static final Logger LOGGER = Logger.getLogger(TestEntrance.class);
    private static final String[] useCaseDescs = {
            "Only documents are indexed", // indexType: 0
            "Only documents are indexed and stored",// indexType: 1
            "Only documents and term frequencies are indexed and stored",// indexType: 2
            "Indexes documents, frequencies and positions and stored",// indexType: 3
            "Indexes documents, frequencies, positions and offsets and stored",// indexType: 4
            "Indexes documents, frequencies and positions and docValues",// indexType: 5
            "Only documents are indexed and docValues"// indexType: 6
    };

    public static void main(String[] args) {
        productIndexByMinute(args);
    }

    private static void productIndexByMinute(String[] args) {
        try {
            if (args.length != 5) {
                String msg = "参数错误 USAGE: <dataDirs> <indexType> <runtimeMin> <parallel> <ramSize> \n\tindexType";
                for (int i = 0; i < useCaseDescs.length; i++) {
                    msg += "\t" + i + " " + useCaseDescs[i] + "\n\t\t";
                }
                LOGGER.error(msg);
                return;
            }
            String[] dirs = args[0].split(",");
            int indexType = Integer.valueOf(args[1]);
            int intervalByMin = Integer.valueOf(args[2]);
            RuntimeStatus status = new RuntimeStatus();
            ResourcesUtil resourcesUtil = new ResourcesUtil(status);
            DataQueue dataQueue = new DataQueue(resourcesUtil, dirs);
            initEnv(resourcesUtil, dataQueue);

            int threshold = Integer.valueOf(args[3]);
            double ramBufferSizeMB = Double.valueOf(args[4]);;
            String useCaseDesc = useCaseDescs[indexType];
            boolean cfs = false;
            int halfMin = intervalByMin / 2;
            LOGGER.info("开始测试 [" + useCaseDesc + "]性能 单表 " + threshold + " 线程构建索引; 参数: threshold: " + threshold
                    + " RAM: " + ramBufferSizeMB + " CFS: " + cfs + " 运行时间: " + intervalByMin + "m");

            Path basePath = Paths.get(DataQueue.BASE_PATH, "productIndexByMinute_indexes_" + indexType);
            Dispatcher.init(resourcesUtil, basePath);
            while (!Dispatcher.startService(dataQueue, status, indexType, threshold, ramBufferSizeMB, cfs)) {
                LOGGER.info("调度服务启动失败 等待1s后重启");
                ThreadAssistant.sleep(1, TimeUnit.SECONDS);
            }
            ThreadAssistant.sleep(halfMin, TimeUnit.MINUTES);
            DocIndexBuilderUtil.collectStatistic(useCaseDesc + "\n" + status.getLashStatus());
            ThreadAssistant.sleep((intervalByMin - halfMin), TimeUnit.MINUTES);
            Dispatcher.stopService();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        System.exit(0);
    }

    private static void loopTest(String[] args) {
        LOGGER.info("USAGE 1: <dataDirs> <indexType> <threshold> <ramSizeMB>");
        LOGGER.info("USAGE 2: <dataDirs> <threshold> <ramSizeMB>");

        if (args.length != 1 && args.length != 3 && args.length != 4) {
            LOGGER.error("参数错误 : " + Arrays.toString(args));
            return;
        }
        try {
            String[] dirs = args[0].split(",");
            RuntimeStatus status = new RuntimeStatus();
            ResourcesUtil resourcesUtil = new ResourcesUtil(status);
            DataQueue dataQueue = new DataQueue(resourcesUtil, dirs);
            initEnv(resourcesUtil, dataQueue);

            LOGGER.info("开始启动索引构建服务...");
            Path basePath = Paths.get(DataQueue.BASE_PATH, "concurrent_indexes");
            Dispatcher.init(resourcesUtil, basePath);
            if (args.length == 1) {
                double[] ramSizeMB = {16, 64, 128, 256, 512};
                multiCombine(status, resourcesUtil, dataQueue, ramSizeMB, true);
                multiCombine(status, resourcesUtil, dataQueue, ramSizeMB, false);
            }
            if (args.length == 3) {
                double ramSizeMB = Double.parseDouble(args[2]);
                for (int indexType = 0; indexType <= 4; indexType++) {
                    buildIndex(dataQueue, status, indexType, useCaseDescs[indexType], Integer.parseInt(args[1]), ramSizeMB, false);
                }
            }
            if (args.length == 4) {
                double ramSizeMB = Double.parseDouble(args[2]);
                Dispatcher.startService(dataQueue, status, Integer.parseInt(args[1])
                        , Integer.parseInt(args[1]), ramSizeMB, false);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void initEnv(final ResourcesUtil resourcesUtil, DataQueue dataQueue) {
        Thread statusTh = new Thread(() -> {
            LOGGER.info("开始启动定时status检查服务");
            Logger statusLogger = Logger.getLogger(RuntimeStatus.class);
            while (true) {
                ThreadAssistant.sleep(1, TimeUnit.MINUTES);
                resourcesUtil.getStatus().printStatus(statusLogger);
                statusLogger.info(dataQueue.desc());
            }
        }, "status");
        statusTh.setDaemon(true);
        statusTh.start();
    }

    private static void multiCombine(RuntimeStatus status, ResourcesUtil resourcesUtil, DataQueue dataQueue, double[] ramSizeMB, boolean cfs) {
        for (double ram : ramSizeMB) { // RAM Buffer容量
            for (int indexType = 0; indexType <= 4; indexType++) { // 索引构建类型
                for (int threshold = 1; threshold <= 24; threshold++) {
                    buildIndex(dataQueue, status, indexType, useCaseDescs[indexType], threshold, ram, cfs);
                }
            }
        }
    }

    private static void buildIndex(DataQueue dataQueue, RuntimeStatus status
            , int indexType, String useCaseDesc, int threshold, double ramBufferSizeMB, boolean cfs) {
        long exeTimeSec = 5l * 60 * 60; // 5h
        exeTimeSec = 5 * 60; // 5m

        LOGGER.info("开始测试 [" + useCaseDesc + "]性能 单表 " + threshold + " 线程构建索引; 参数: threshold: " + threshold + " RAM: " + ramBufferSizeMB + " CFS: " + cfs);
        while (!Dispatcher.startService(dataQueue, status, indexType, threshold, ramBufferSizeMB, cfs)) {
            LOGGER.info("调度服务启动失败 等待1s后重启");
            ThreadAssistant.sleep(1, TimeUnit.SECONDS);
        }
        ThreadAssistant.sleep(exeTimeSec / 2, TimeUnit.SECONDS);
        DocIndexBuilderUtil.collectStatistic(useCaseDesc + "\n" + status.getLashStatus());
        ThreadAssistant.sleep(exeTimeSec / 2, TimeUnit.SECONDS);
        Dispatcher.stopService();
    }

    private static void onlyIndexed(DataQueue dataQueue
            , RuntimeStatus status, int threshold, double ramBufferSizeMB, boolean cfs) {
        long exeTimeSec = 5l * 60 * 60; // 5h
        exeTimeSec = 5 * 60; // 5m

        String desc = "Only documents are indexed";
        LOGGER.info("开始测试 [" + desc + "]性能 单表 " + threshold + " 线程构建索引");
        Dispatcher.startService(dataQueue, status, 0, threshold, ramBufferSizeMB, cfs);
        ThreadAssistant.sleep(exeTimeSec / 2, TimeUnit.SECONDS);
        DocIndexBuilderUtil.collectStatistic(desc + "\n" + status.getLashStatus());
        ThreadAssistant.sleep(exeTimeSec / 2, TimeUnit.SECONDS);
        Dispatcher.stopService();
    }
}
