package com.broadtech.qp.index.test.concurrent;

import com.broadtech.bdp.api.ICallback;
import com.broadtech.bdp.common.ctl.CtlConfig;
import com.broadtech.bdp.common.util.*;
import com.broadtech.qp.index.freqLog.FrequentlyLogger;
import com.broadtech.qp.index.status.RuntimeStatus;
import com.broadtech.qp.index.test.DataQueue;
import org.apache.lucene.index.IndexWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeifi on 2017/7/25.
 * 共享{@link org.apache.lucene.index.IndexWriter}
 */
public class ProcessorByShareWriter implements Runnable {
    protected final Logger logger = Logger.getLogger(ProcessorByShareWriter.class);
    protected final Logger loggerByFreq = Logger.getLogger(FrequentlyLogger.class);
    private final RuntimeStatus status;
    private IndexWriter indexWriter;
    private CtlConfig ctl;
    private DataQueue dataQueue;
    private boolean pause = false, stopped = false;
    private CountDownLatch pauseCDL = null, closeCDL;
    private ICallback<ProcessorByShareWriter> closeCallback;
    private final IndexType indexType;

    public ProcessorByShareWriter(IndexType indexType, RuntimeStatus status) {
        this.indexType = indexType;
        this.status = status;
    }

    public void setDataQueue(DataQueue dataQueue) {
        this.dataQueue = dataQueue;
    }

    public void setIndexWriter(CtlConfig ctl, IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
        this.ctl = ctl;
        this.pauseCDL = null;
        pause = false;
        logger.info("开始 " + ctl.toString() + "索引构建 构建逻辑: " + this.getClass()
                + "ctl: " + ctl + " IndexWriter: " + indexWriter);
    }

    public void pause(CountDownLatch cdl) {
        if (stopped) {
            if (cdl != null) cdl.countDown();
            return;
        }
        this.pauseCDL = cdl;
        pause = true;
    }

    public void close(CountDownLatch cdl, ICallback<ProcessorByShareWriter> closeCallback) {
        logger.info("开始准备关闭 action: " + Thread.currentThread().getName());
        this.closeCDL = cdl;
        this.closeCallback = closeCallback;
        stopped = true;
        logger.info("关闭资源准备就绪 action: " + Thread.currentThread().getName());
    }

    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    public CtlConfig getCtl() {
        return ctl;
    }

    @Override
    public void run() {
        try {

            logger.info(Thread.currentThread().getName() + " 已启动");
            Path file;
            TimeCounter timeCounter = new TimeCounter();
            byte[] data = new byte[64 * 1024];
            List<byte[]> linesCache = new ArrayList<>(100);
            status.incBusiness(ctl);
            while (!stopped) {
                if (pause) {
                    if (pauseCDL != null) {
                        pauseCDL.countDown();
                        pauseCDL = null;
                        logger.info(Thread.currentThread().getName() + "已暂停索引构建");
                    }
                    ThreadAssistant.sleep(10, TimeUnit.MILLISECONDS);
                    continue;
                }
                if (dataQueue == null || indexWriter == null || ctl == null) {
                    ThreadAssistant.sleep(10, TimeUnit.MILLISECONDS);
                    continue;
                }
                file = dataQueue.dequeue(ctl.ctlId);
                if (file == null) {
                    ThreadAssistant.sleep(10, TimeUnit.MILLISECONDS);
                    continue;
                }
                timeCounter.reset();
                indexDoc(file, data, ctl, indexWriter, linesCache);
                Dispatcher.totalBytes.addAndGet(file.toFile().length());
                loggerByFreq.info(CommonUtil.getPID() + " => build " + file.toString() + " "
                        + UnitHelper.getHumanSize(file.toFile().length()) + " index, cost time: " + timeCounter.humanCost()
                        + " 平均耗时: " + UnitHelper.getHumanSize(file.toFile().length() / (timeCounter.cost() / 1000.0)) + "/s");
            }
            status.decBusiness(ctl);
            if (closeCDL != null) closeCDL.countDown();
            if (pause && pauseCDL != null) pauseCDL.countDown();
            if (closeCallback != null)
                try {
                    closeCallback.callback(null, this);
                } catch (Exception e) {
                    closeCallback.exceptionCaught(e);
                }
            logger.info(Thread.currentThread().getName() + " (processor)索引构建器已退出");

        } catch (Throwable e) {
            logger.error(Thread.currentThread().getName() + " (processor)索引构建器执行遇到未知异常", e);
        }
    }

    private void indexDoc(Path file, byte[] data, CtlConfig ctl, IndexWriter indexWriter, List<byte[]> lines) {
        try {
            int count, offset = 0;
            try (FileInputStream ins = new FileInputStream(file.toFile())) {
                while ((count = ins.read(data, offset, data.length - offset)) != -1) {
                    lines.clear();
                    offset = TokenUtils.tokens(data, count, ctl.lineSep, lines);
                    for (byte[] line : lines) {
                        indexDoc(indexWriter, line, ctl);
                    }
                    status.addLine(ctl, count, lines.size());
                }
            }
        } catch (IOException e) {
            logger.error("构建 " + file.toString() + " 异常", e);
        }
    }

    protected void indexDoc(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
        switch (indexType) {
            case DOCS:
                DocIndexBuilderUtil.onlyDocumentsIndexed(indexWriter, line, ctl);
                break;
            case DOCS_STORED:
                DocIndexBuilderUtil.docIndexedAndStored(indexWriter, line, ctl);
                break;
            case DOCS_AND_FREQS_STORE:
                DocIndexBuilderUtil.dFIndexedAndStored(indexWriter, line, ctl);
                break;
            case DOCS_AND_FREQS_AND_POSITIONS_STORE:
                DocIndexBuilderUtil.dFPIndexedAndStored(indexWriter, line, ctl);
                break;
            case DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS_STORE:
                DocIndexBuilderUtil.dFPOIndexedAndStored(indexWriter, line, ctl);
                break;
            case DOCS_AND_FREQS_AND_POSITIONS_DOCVALUES:
                DocIndexBuilderUtil.docValueBuild(indexWriter, line, ctl);
                break;
            case DOCS_DOCVALUES:
                DocIndexBuilderUtil.docValueBuildByDocs(indexWriter, line, ctl);
                break;
        }
    }
}
