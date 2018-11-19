package com.broadtech.qp.index.test;

import com.broadtech.bdp.common.ctl.CtlConfig;
import com.broadtech.bdp.common.util.*;
import com.broadtech.qp.index.ResourcesUtil;
import com.broadtech.qp.index.status.RuntimeStatus;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 索引构建基类
 */
public abstract class BaseIndexProcessor implements Runnable {
    protected final Logger logger = Logger.getLogger(BaseIndexProcessor.class);
    private final RuntimeStatus status;
    private final CtlConfig ctl;
    private DataQueue dataQueue;
    private boolean stopped;
    private CountDownLatch cdl;

    public BaseIndexProcessor(RuntimeStatus status, CtlConfig ctl) {
        this.status = status;
        this.ctl = ctl;
    }

    public void setDataQueue(DataQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    public void close(CountDownLatch cdl) {
        this.cdl = cdl;
        stopped = true;
    }

    protected abstract String getBaseIndexPath();

    @Override
    public void run() {
        logger.info("已启动 " + ctl.toString() + "索引构建 构建逻辑: " + this.getClass());
        TimeCounter timeCounter = new TimeCounter();
        byte[] data = new byte[64 * 1024];
        int count = 0, offset = 0;
        long lastCheckTime = Clock.systemUTC().millis();
        List<byte[]> lines = new ArrayList<>();
        IndexWriter indexWriter = null;
        try {
            indexWriter = createIndexWriter();
            status.incBusiness(ctl);
            while (!stopped) {
                Path file = dataQueue.dequeue(ctl.ctlId);
                if (file == null) {
                    ThreadAssistant.sleep(10, TimeUnit.MILLISECONDS);
                    continue;
                }
                timeCounter.reset();
                try (InputStream ins = new FileInputStream(file.toFile())) {
                    while ((count = ins.read(data, offset, data.length - offset)) != -1) {
                        lines.clear();
                        offset = TokenUtils.tokens(data, count, ctl.lineSep, lines);
                        for (byte[] line : lines) {
                            indexDoc(indexWriter, line, ctl);
                        }
                        status.addLine(ctl, count, lines.size());
                    }
                    logger.info(CommonUtil.getPID() + " => build " + file.toString() + " " + UnitHelper.getHumanSize(file.toFile().length()) + " index, cost time: " + timeCounter.humanCost());
                } catch (Exception e) {
                    logger.error("构建数据文件" + file.toString() + "异常", e);
                } finally {
                    if (Clock.systemUTC().millis() - lastCheckTime > 5 * 60 * 1000) {
                        lastCheckTime = Clock.systemUTC().millis();
                        logger.info("checkDiskRemainSpace start => " + ctl.tableName);
                        indexWriter = checkDiskRemainSpace(indexWriter);
                        logger.info("checkDiskRemainSpace end => " + ctl.tableName);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("打开IndexWriter遇到异常", e);
        } finally {
            if (Objects.nonNull(indexWriter)) try {
                indexWriter.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            status.decBusiness(ctl);
        }
        if (cdl != null) cdl.countDown();
        logger.info("索引构建器已退出");
    }

    private IndexWriter createIndexWriter() throws IOException {
        Path indexPath = Paths.get(getBaseIndexPath(), ctl.tableName);
        Directory dir = FSDirectory.open(indexPath);
        IndexWriterConfig iwc = new IndexWriterConfig();
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        iwc.setUseCompoundFile(false); // TODO 测试点
        return new IndexWriter(dir, iwc);
    }

    /**
     * 目录到达指定上限后 删除当前表的索引文件
     */
    private IndexWriter checkDiskRemainSpace(IndexWriter indexWriter) {
        long size = 0;
        int times = 0;
        while (times < 3) {
            try {
                size = ResourcesUtil.getPathSize(Paths.get(getBaseIndexPath()));
                break;
            } catch (IOException e) {
                times++;
            }
        }
        try {
            if (size > 100l * 1024 * 1024 * 1024) { // 100g
                Path indexes = Paths.get(getBaseIndexPath(), ctl.tableName);
                logger.info("索引目录超过100g, 删除索引: " + indexes.toString());
                indexWriter.close();
                try {
                    ResourcesUtil.delete(indexes);
                } catch (Exception e) {
                    logger.error("删除索引目录异常 => " + indexes.toString(), e);
                }
                logger.info("索引目录超过100g, 删除索引" + indexes.toString() + "后重建");
                return createIndexWriter();
            }
        } catch (IOException e) {
            logger.error("删除重建索引异常,返回{@param indexWriter}", e);
        }
        return indexWriter;
    }

    protected abstract void indexDoc(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException;
}
