package com.broadtech.qp.solr.pertest;

import com.broadtech.bdp.common.ctl.RichCtlConfig;
import com.broadtech.bdp.common.util.Logger;
import com.broadtech.qp.index.ResourcesUtil;
import com.broadtech.qp.index.status.RuntimeStatus;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeifi on 2017/8/10.
 */
public class SolrRESTEntrance implements Closeable {
    private final static Logger logger = Logger.getLogger(SolrRESTEntrance.class);
    private final ResourcesUtil resources;
    private final String[] collectionURLS;
    private List<SolrRESTBuilder> processors = new ArrayList<>();

    public static void main(String[] args) {
        RuntimeStatus status = new RuntimeStatus();
        ResourcesUtil resources;
        try {
            resources = new ResourcesUtil(status);
        } catch (Exception e) {
            logger.error("初始化资源异常 程序即将关闭", e);
            return;
        }
        String usage = "<dataDirList> <solrBaseURLs> <tableProcessorNum> <ctlId:collection>[,ctlId:collection] <TestUnitDesc>";
        if (args.length != 5) {
            logger.error("启动命令格式错误 \n" + usage);
            return;
        }
        try {
            String dataDirs = args[0];
            String solrBaseURLs = args[1];
            int processorNumPerTable = Integer.parseInt(args[2]);
            Map<String, String> tableOptions = new HashMap<>();
            for (String item : args[3].split(",")) {
                tableOptions.put(item.split(":")[0], item.split(":")[1]);
            }

            SolrRESTEntrance instance = new SolrRESTEntrance(dataDirs, solrBaseURLs
                    , processorNumPerTable, tableOptions, args[4], resources);
            TestStatisticUtil.ready(resources, instance);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    public SolrRESTEntrance(String dataDirs, String solrServerHosts, int processorNumPerTable,
                            Map<String, String> tableOptions, String testDesc, ResourcesUtil resources) {
        String[] solrBaseURLs = solrServerHosts.split(",");
        this.collectionURLS = new String[solrBaseURLs.length];
        for (int i = 0; i < solrBaseURLs.length; i++) {
            this.collectionURLS[i] = String.format(Locale.ROOT, "http://%s/solr/##/update", solrBaseURLs[i]);
        }

        this.resources = resources;
        String msg = "本轮测试载入可处理的表数: " + resources.getCtlList().size()
                + "，每张表分配: " + processorNumPerTable + "索引构建线程，约定的处理数据: " + tableOptions + ", 用例简介: " + testDesc;
        TestStatisticUtil.printStatistic(msg);
        logger.info("开始准备启动solr性能测试服务，" + msg);
        FileQueue fileQueue = new FileQueue(dataDirs.split(","), resources);
        tableOptions.forEach((k, v) -> {
            RichCtlConfig ctl = resources.getCtlByCtlId(k);
            if (ctl != null) {
                for (int i = 0; i < processorNumPerTable; i++) {
                    SolrRESTBuilder builder = new SolrRESTBuilder(collectionURLS[i % collectionURLS.length], ctl, v, fileQueue, resources.getStatus());
                    processors.add(builder);
                    builder.start(i);
                }
            }
        });
        logger.info("solr性能测试服务，" + msg + " 已启动");
    }

    @Override
    public void close() throws IOException {
        TestStatisticUtil.printStatistic(resources.getStatus().getLashStatus());
        CountDownLatch cdl = new CountDownLatch(processors.size());
        processors.forEach(p -> p.stop(cdl));
        try {
            cdl.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            Runtime.getRuntime().halt(0);
        }
    }
}