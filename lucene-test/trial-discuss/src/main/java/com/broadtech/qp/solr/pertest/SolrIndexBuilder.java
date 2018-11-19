package com.broadtech.qp.solr.pertest;

import com.broadtech.bdp.common.ctl.RichCtlConfig;
import com.broadtech.bdp.common.util.*;
import com.broadtech.qp.index.status.RuntimeStatus;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeifi on 2017/8/9.
 */
public class SolrIndexBuilder implements Runnable {
    private final static Logger logger = Logger.getLogger(SolrIndexBuilder.class);
    private final RuntimeStatus status;
    private final FileQueue fileQueue;
    private final RichCtlConfig ctlConfig;
    private final SolrClient solrClient;
    private final String[] fileds;
    private final String collection;
    private boolean stopped = false;
    private CountDownLatch cdl = null;
    private long seq = 0;
    private String idPrefix = "";

    public SolrIndexBuilder(String zkHosts, RichCtlConfig ctl, String collection, FileQueue fileQueue, RuntimeStatus status) {
        this.ctlConfig = ctl;
        this.fileQueue = fileQueue;
        this.status = status;
        this.collection = collection;
        this.fileds = ctl.getCtlConfig().fieldNames.toArray(new String[ctl.getCtlConfig().fieldNames.size()]);
        this.solrClient = createSolrClient(zkHosts);
    }

    public void start(int processorSeq) {
        idPrefix += processorSeq + "_" + CommonUtil.getPID() + "-";
        String name = "Build_" + processorSeq + "-" + collection;
        new Thread(this, name).start();
    }

    public void stop(CountDownLatch stopCdl) {
        this.cdl = stopCdl;
        this.stopped = true;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        logger.info("solr索引构建处理器 " + name + " 已启动");
        Path file;
        TimeCounter timeCounter = new TimeCounter();
        byte[] buffer = new byte[64 * 1024];
        List<byte[]> linesCache = new ArrayList<>();
        status.incBusiness(ctlConfig.getCtlConfig());
        while (!stopped) {
            try {
                file = fileQueue.dequeue(ctlConfig);
                if (file == null) {
                    ThreadAssistant.sleep(10, TimeUnit.MILLISECONDS);
                    continue;
                }
                timeCounter.reset();
                postFile(file, buffer, linesCache);
                logger.info("build " + file.toString() + " " + UnitHelper.getHumanSize(file.toFile().length()) + " index, cost time: " + timeCounter.humanCost());
            } catch (Throwable e) {
                logger.error(name + " 索引处理器遇到异常", e);
            }
        }
        try {
            solrClient.commit();
        } catch (Exception e) {
            logger.error("SolrClient::commit()", e);
        }
        if (cdl != null) cdl.countDown();
        status.decBusiness(ctlConfig.getCtlConfig());
        logger.info("solr索引构建处理器 " + name + " 已停止");
    }

    private SolrClient createSolrClient(String zkHosts) {
        HttpClient httpClient;
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, 128);
        params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST, 32);
        params.set(HttpClientUtil.PROP_FOLLOW_REDIRECTS, false);
        httpClient = HttpClientUtil.createClient(params);

        return new CloudSolrClient.Builder()
                .withZkHost(Arrays.asList(zkHosts.split(",")))
                .withHttpClient(httpClient)
                .withLBHttpSolrClient(new LBHttpSolrClient.Builder().build())
                .build();
    }

    private void postFile(Path file, byte[] buffer, List<byte[]> linesCache) {
        byte[] lineSeq = ctlConfig.getCtlConfig().lineSep;
        byte[] fieldSeq = ctlConfig.getCtlConfig().fieldSep;
        try (InputStream ins = new FileInputStream(file.toFile())) {
            int count, offset = 0;
            while ((count = ins.read(buffer, offset, buffer.length - offset)) != -1) {
                linesCache.clear();
                offset = TokenUtils.tokens(buffer, offset + count, lineSeq, linesCache);
                List<SolrInputDocument> docs = new ArrayList<>(linesCache.size());
                for (byte[] line : linesCache) {
                    docs.add(transToDocument(line, fieldSeq));
                }
                solrClient.add(collection, docs);
                status.addLine(ctlConfig.getCtlConfig(), count, linesCache.size());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private SolrInputDocument transToDocument(byte[] line, byte[] fieldSeq) {
//        List<byte[]> fields = TokenUtils.tokensFromLine(line, fieldSeq, ctlConfig.getCtlConfig().fieldNames.size());
//        Object fieldValue;
//        SolrInputDocument doc = new SolrInputDocument(fileds);
//        for (int i = 0; i < fields.size(); i++) {
//            try {
//                fieldValue = ctlConfig.getConcreteFieldValue(fields.get(i), (short) i);
//                doc.addField(ctlConfig.getCtlConfig().fieldNames.get(i), fieldValue);
//            } catch (Exception e) {
//                GreatLogger.error(GreatLogger.Level.plain, SolrIndexBuilder.class, "LineTransToDocument", "字段类型化失败", fields.get(i), e);
//            }
//        }
//        return doc;
        return null;
    }
}
