package com.broadtech.qp.index;

import com.broadtech.bdp.common.ctl.RichCtlConfig;
import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.ThreadAssistant;
import com.broadtech.qp.index.status.RuntimeStatus;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/7/10.
 */
public class Indexer {
    private static Map<String, IndexBuilder> builders = new HashMap<>();
    private static ResourcesUtil resourcesUtil;
    private static Logger logger = Logger.getLogger(Indexer.class);

    public static void main(String[] args) {
        init();
        Path resourceDir = Paths.get(args[0]);
        while (true) {
            try {
                if (resourceDir.toFile().list().length == 0) {
                    ThreadAssistant.sleep(1, TimeUnit.SECONDS);
                    continue;
                }
                if (Files.isDirectory(resourceDir)) {
                    Files.walkFileTree(resourceDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (file.getFileName().toString().endsWith(".dat")) {
                                try {
                                    indexFile(file);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else
                    try {
                        indexFile(resourceDir);
                    } catch (Exception e) {
                        Logger.getLogger(Indexer.class).error(e.getMessage(), e);
                    }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static void init() {
        try {
            resourcesUtil = new ResourcesUtil(new RuntimeStatus());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Thread statusTh = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ThreadAssistant.sleep(10, TimeUnit.SECONDS);
                    resourcesUtil.getStatus().printStatus(logger);
                }
            }
        }, "status");
        statusTh.setDaemon(true);
        statusTh.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                builders.values().forEach(IndexBuilder::stop);
                resourcesUtil.getStatus().printStatus(logger);
            }
        }));
    }

    private static void indexFile(Path file) throws Exception {
        RichCtlConfig ctl = resourcesUtil.getCtl(file);
        if (ctl == null) {
            Files.delete(file);
            return;
        }
        String ctlId = ctl.getCtlConfig().ctlId;
        IndexBuilder builder = builders.get(ctlId);
        if (builder == null) {
            builders.put(ctlId, new IndexBuilder(ctl, resourcesUtil.getStatus()));
            resourcesUtil.getStatus().incBusiness();
            builder = builders.get(ctlId);
            Thread th = new Thread(builder, "IndexBuilder_" + ctl.toString());
            th.start();
        }
        builder.build(file);
    }
}
