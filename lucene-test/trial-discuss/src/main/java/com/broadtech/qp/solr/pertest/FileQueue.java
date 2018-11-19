package com.broadtech.qp.solr.pertest;

import com.broadtech.bdp.common.ctl.RichCtlConfig;
import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.ThreadAssistant;
import com.broadtech.qp.index.ResourcesUtil;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jeifi on 2017/8/9.
 */
public class FileQueue {
    private final static Logger logger = Logger.getLogger(FileQueue.class);
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Path>> dataQueue = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AtomicInteger> dataQueueSize = new ConcurrentHashMap<>();

    public FileQueue(String[] dataDirs, ResourcesUtil resources) {
        for (RichCtlConfig ctl : resources.getCtlList()) {
            dataQueue.putIfAbsent(ctl.getCtlConfig().ctlId, new ConcurrentLinkedQueue<>());
            dataQueueSize.putIfAbsent(ctl.getCtlConfig().ctlId, new AtomicInteger(0));
        }
        startScanner(dataDirs, resources);
        logger.info("数据文件扫描服务启动 成功");
    }

    public Path dequeue(RichCtlConfig ctl) {
        String key = ctl.getCtlConfig().ctlId;
        Path file = dataQueue.get(key).poll();
        if (file != null) dataQueueSize.get(key).decrementAndGet();
        return file;
    }

    private void startScanner(final String[] dataDirs, final ResourcesUtil resources) {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        for (String dir : dataDirs) {
                            Path path = Paths.get(dir);
                            if (Files.exists(path)) {
                                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                                        RichCtlConfig ctl = resources.getCtl(file);
                                        RichCtlConfig ctl = resources.getCtlByCtlId("01_1");
                                        if (ctl != null) {
                                            String key = ctl.getCtlConfig().ctlId;
                                            if (dataQueueSize.get(key).get() < 50) {
                                                dataQueue.get(key).offer(file);
                                                dataQueueSize.get(key).incrementAndGet();
                                            }
                                        }
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                            }
                        }
                        ThreadAssistant.sleep(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }, "File_Scanner");
        th.setDaemon(true);
        th.start();
    }

}
