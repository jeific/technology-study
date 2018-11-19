package com.broadtech.qp.index.test.concurrent;

import com.broadtech.bdp.api.ICallback;
import com.broadtech.bdp.common.ctl.CtlConfig;
import com.broadtech.bdp.common.ctl.RichCtlConfig;
import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.ThreadAssistant;
import com.broadtech.bdp.common.util.TimeCounter;
import com.broadtech.bdp.common.util.UnitHelper;
import com.broadtech.qp.index.ResourcesUtil;
import com.broadtech.qp.index.status.RuntimeStatus;
import com.broadtech.qp.index.test.DataQueue;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2017/7/25.
 */
public class Dispatcher {
    private final static Logger logger = Logger.getLogger(Dispatcher.class);
    private final static List<ProcessorByShareWriter> processors = new ArrayList<>();
    public final static AtomicLong totalBytes = new AtomicLong(0);
    private final static long sysStartTime = Clock.systemUTC().millis();
    private static IndexWriter indexWriter;
    private static CtlConfig ctl;
    private static Path indexPath;
    private final static AtomicBoolean isPause = new AtomicBoolean(false);
    private final static ArgMode argMode = new ArgMode();

    private static class ArgMode {
        int threshold;
        double ramBufferMB;
        boolean cfs;
    }

    public static void stopService() {
        TimeCounter timeCounter = new TimeCounter();
        while (isPause.get()) {
            ThreadAssistant.sleep(10, TimeUnit.MILLISECONDS);
        }
        logger.info("开始关闭Dispatcher服务 processor数: " + processors.size()
                + " 当前索引目录大小: " + UnitHelper.getHumanSize0(DocIndexBuilderUtil.getIndexPathSize(indexPath))
                + " 检查是否暂停耗时: " + timeCounter.humanCost());
        timeCounter.reset();
        CountDownLatch cdl = new CountDownLatch(processors.size());
        processors.forEach(p -> p.close(cdl, new ICallback<ProcessorByShareWriter>() {
            @Override
            public ProcessorByShareWriter callback(String arg0, Object... args) throws Exception {
                ProcessorByShareWriter p = (ProcessorByShareWriter) args[0];
                processors.remove(p);
                return null;
            }

            @Override
            public void exceptionCaught(Exception e) {
                logger.error("[关闭调度服务] 从缓存中移除已经关闭Processor(" + Thread.currentThread().getName() + ")异常", e);
            }
        }));
        try {
            logger.info("等待所有processor关闭(timeout:30s) 向Processor发送关闭命令耗时: " + timeCounter.humanCost());
            timeCounter.reset();
            cdl.await(30, TimeUnit.SECONDS);
            logger.info("开始关闭IndexWriter 等待所有Processor关闭(timeout:30s) 时长: " + timeCounter.humanCost());
            timeCounter.reset();
            if (indexWriter != null) indexWriter.close();
            logger.info("Dispatcher服务 已关闭 关闭IndexWriter耗时: " + timeCounter.humanCost());
        } catch (Exception e) {
            logger.error("Dispatcher服务关闭异常", e);
        }
    }

    public static void init(ResourcesUtil resources, Path basePath) {
        String specificTable = "MLTE_S1U_HTTPLOG_F2";
        for (RichCtlConfig _ctl : resources.getCtlList()) {
            if (_ctl.getCtlConfig().tableName.contains(specificTable)) {
                ctl = _ctl.getCtlConfig();
                specificTable = ctl.tableName;
            }
        }
        if (ctl == null) {
            logger.info(specificTable + "表没有找到");
            System.exit(0);
            return;
        }

        indexPath = Paths.get(basePath.toString(), ctl.tableName);
        logger.info("注册ShutdownHook");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopService();
            }
        });
//        logger.info("启动索引目录空间检查服务");
//        startCheckDiskRemainSpaceService(indexPath);
    }

    public static boolean startService(DataQueue dataQueue, RuntimeStatus status
            , int indexType, int threshold, double ramSizeMB, boolean cfs) {
        TimeCounter timeCounter = new TimeCounter();
        while (isPause.get()) {
            ThreadAssistant.sleep(10, TimeUnit.MILLISECONDS);
        }
        logger.info("开始启动调度服务: " + indexType + "," + threshold + ", " + ramSizeMB + ", cfs: " + cfs
                + " 检查调度服务是否被暂停时长: " + timeCounter.humanCost());
        timeCounter.reset();
        if (ctl == null) {
            logger.info("没有可建立索引的biao, 若确定配置无误, 请务必调用init(ResourcesUtil, Path)接口初始化");
            System.exit(0);
            return false;
        }
        argMode.cfs = cfs;
        argMode.ramBufferMB = ramSizeMB;
        argMode.threshold = threshold;

        DocIndexBuilderUtil.collectStatistic("\n用例 => 索引类型: " + indexType + ", 并发数: " + threshold + ", RAMBuffer: " + ramSizeMB + ", cfs: " + cfs
                + " 索引目录当前大小: " + UnitHelper.getHumanSize0(DocIndexBuilderUtil.getIndexPathSize(indexPath)));
        indexWriter = create(indexPath);
        if (indexWriter == null) {
            logger.info("调度服务启动失败 时长: " + timeCounter.humanCost());
            return false;
        }
        for (int i = 0; i < threshold; i++) {
            ProcessorByShareWriter writer = new ProcessorByShareWriter(IndexType.parse(indexType), status);
            writer.setDataQueue(dataQueue);
            writer.setIndexWriter(ctl, indexWriter);
            Thread th = new Thread(writer, "IndexWriter_" + i);
            th.start();
            processors.add(writer);
        }
        logger.info("调度服务启动成功 时长: " + timeCounter.humanCost());
        Thread statusTh = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ThreadAssistant.sleep(30, TimeUnit.SECONDS);
                    logger.info("总计 => 处理总数据量" + UnitHelper.getHumanSize(totalBytes.get())
                            + " 时长" + (Clock.systemUTC().millis() - sysStartTime) / 1000
                            + " 构建能力: " + (UnitHelper.getHumanSize(totalBytes.get() / ((Clock.systemUTC().millis() - sysStartTime) / 1000))) + "/s");
                }
            }
        }, "statusTh");
        statusTh.setDaemon(true);
        statusTh.start();
        return true;
    }

    private static IndexWriter create(Path indexPath) {
        try {
            IndexWriterConfig iwc = new IndexWriterConfig();
            iwc.setRAMBufferSizeMB(argMode.ramBufferMB); // default 16.0
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            iwc.setUseCompoundFile(argMode.cfs);
            Directory dir = FSDirectory.open(indexPath);
            return new IndexWriter(dir, iwc);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private static void startCheckDiskRemainSpaceService(final Path indexPath) {
        Thread th = new Thread(() -> {
            logger.info("索引目录空间检查已启动");
            long lastTime = Clock.systemUTC().millis();
            TimeCounter timeCounter = new TimeCounter();
            while (true) {
                ThreadAssistant.sleep(30, TimeUnit.SECONDS);
                if (Clock.systemUTC().millis() - lastTime > 10 * 60 * 1000) {
                    timeCounter.reset();
                    logger.info("开始检查索引目录容量, 当前Processor数: " + processors.size() + ", " + indexPath.toString());
                    lastTime = Clock.systemUTC().millis();
                    if (processors.size() == 0) {
                        logger.info("索引目录容量检查 => 没有有效的索引构建处理器 " + timeCounter.humanCost());
                        continue;
                    }
                    isPause.set(true);
                    CountDownLatch pauseCDL = new CountDownLatch(processors.size());
                    processors.forEach(p -> p.pause(pauseCDL)); // 暂停索引构建
                    try {
                        timeCounter.reset();
                        pauseCDL.await(30, TimeUnit.SECONDS);
                        logger.info("[索引目录容量] 等待所有Processor暂停(Timeout:30s) 时长: " + timeCounter.humanCost());
                    } catch (InterruptedException e) {
                        processors.forEach(p -> p.setIndexWriter(p.getCtl(), p.getIndexWriter())); // 恢复索引构建
                        logger.error("[索引目录容量] 暂停全部的索引构建线程异常, 已取消暂停 " + timeCounter.humanCost(), e);
                        isPause.set(false);
                        continue;
                    }
                    IndexWriter indexWriterByProcessor = processors.get(0).getIndexWriter();
                    if (indexWriterByProcessor == null) {
                        logger.info("[索引目录容量] 索引目录容量检查 => 索引构建处理器IndexWriter实例为Null " + timeCounter.humanCost());
                        isPause.set(false);
                        continue;
                    }
                    final IndexWriter indexWriter = DocIndexBuilderUtil.checkDiskRemainSpace(indexPath
                            , indexWriterByProcessor, new ICallback<IndexWriter>() {
                        @Override
                        public IndexWriter callback(String arg0, Object... args) throws Exception {
                            return create((Path) args[0]);
                        }

                        @Override
                        public void exceptionCaught(Exception e) {
                            logger.error("[索引目录容量] 创建新IndexWrter异常", e);
                        }
                    });
                    if (indexWriter != null) {
                        processors.forEach(p -> p.setIndexWriter(p.getCtl(), indexWriter)); // 恢复索引构建
                    } else {
                        logger.info("索引目录容量检查导致 [indexWriter]实例为Null " + timeCounter.humanCost());
                    }
                    isPause.set(false);
                    logger.info("索引目录容量检查和处理完毕 " + timeCounter.humanCost());
                }
            }
        }, "checkDiskRemainSpace");
        th.setDaemon(true);
        th.start();
    }
}
