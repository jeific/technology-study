package com.broadtech.qp.index.test;

import com.broadtech.bdp.common.ctl.RichCtlConfig;
import com.broadtech.bdp.common.util.GreatLogger;
import com.broadtech.bdp.common.util.Logger;
import com.broadtech.qp.index.ResourcesUtil;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jeifi on 2017/7/24.
 */
public class DataQueue {
    public static final String BASE_PATH = "/data/lunce_indexes";
    private static final Logger logger = Logger.getLogger(DataQueue.class);
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Path>> dataQueue = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> queueSize = new ConcurrentHashMap<>();
    private final ResourcesUtil resourceUtil;

    public DataQueue(ResourcesUtil rsUtil, String... dirs) {
        this.resourceUtil = rsUtil;
        startResourceScan(dirs);
    }

    public String desc() {
        StringBuilder builder = new StringBuilder("队列情况");
        queueSize.forEach((k, v) -> builder.append(k).append(":").append(v).append(" "));
        return builder.toString();
    }

    public void enqueue(Path path) {
        RichCtlConfig ctl = resourceUtil.getCtl(path);
        if (ctl != null) {
            String ctlId = ctl.getCtlConfig().ctlId;
            ConcurrentLinkedQueue<Path> queue = dataQueue.get(ctlId);
            if (queue == null) {
                synchronized (this) {
                    queue = dataQueue.get(ctlId);
                    if (queue == null) {
                        dataQueue.putIfAbsent(ctlId, new ConcurrentLinkedQueue<>());
                        queue = dataQueue.get(ctlId);
                        queueSize.putIfAbsent(ctlId, new AtomicInteger());
                    }
                }
            }
            AtomicInteger size = queueSize.get(ctlId);
            if (size.get() < 50) {
                queue.offer(path);
                size.incrementAndGet();
            }
        }
    }

    public Path dequeue(String ctlId) {
        ConcurrentLinkedQueue<Path> queue = dataQueue.get(ctlId);
        if (queue != null) {
            Path path = queue.poll();
            if (path != null) queueSize.get(ctlId).decrementAndGet();
            return path;
        }
        return null;
    }

    private void startResourceScan(String... dirs) {
        for (String dir : dirs) {
            Path path = Paths.get(dir);
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    logger.info("资源scan服务已启动");
                    while (true) {
                        try {
                            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    if (file.getFileName().toString().endsWith(".dat")) {
                                        enqueue(file);
                                    }
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                        } catch (IOException e) {
                            GreatLogger.error(GreatLogger.Level.fatal, DataQueue.class
                                    , path.getFileName().toString(), "scan " + path.toString(), null, e);
                        }
                    }
                }
            }, "scan_" + path.getFileName().toString());
            th.setDaemon(true);
            th.start();
        }
    }
}
