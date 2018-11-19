package com.broadtech.qp.solr;

import com.broadtech.bdp.common.util.Logger;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.params.ModifiableSolrParams;

/**
 * Created by Chen Yuanjun on 2017/7/25.
 */
public class SolrJUsageExample {
    private static final Logger logger = Logger.getLogger(SolrJUsageExample.class);

    /**
     * 【solrJ是Java连接solr进行查询检索和索引更新维护的jar包】<br>s
     * {@link SolrClient} 是所有类基类，里面定义了更新维护索引、搜索相关的接口；<br>
     * LBHttpSolrClient 用于有多个solr服务器时实现负载均衡的情况；<br>
     * ConcurrentUpdateSolrClient 类是线程安全类，推荐在更新维护索引时用；<br>
     * HttpSolrClient 用于独立工作模式的solr的查询；<br>
     * CloudSorlClient 用于solrCould模式<br>
     * ------------------------------------------------<br>
     * 以HttpSolrClient为例说明主要接口的使用。
     * <p>
     * 初始化SolrClient对象,直接指定solr的URL和core1，只能查询或更新core1内容<br>
     * SolrClient client = new HttpSolrClient("http://my-solr-server:8983/solr/core1");<br>
     * QueryResponse resp = client.query(new SolrQuery("*:*"));<br>
     * </p>
     * <p>
     * //指定solr的URL，查询或更新时要指定core<br>
     * SolrClient client = new HttpSolrClient("http://my-solr-server:8983/solr");
     * QueryResponse resp = client.query("core1", new SolrQuery("*:*"));
     * </p>
     * <p>
     * 更新维护索引的主要接口,该函数有多个重载形式，obj是要加入索引的实体对象，collection指定要操作的core，commitWithinMs要提交的毫秒数，默认为-1，add后不会更新，要调用
     * commit(String collection)提交后才能更新查询到。<br>
     * addBean(Object obj)<br>
     * addBean(Object obj, int commitWithinMs)<br>
     * addBean(String collection, Object obj, int commitWithinMs)<br>
     * add(String collection, Collection<SolrInputDocument> docs, int commitWithinMs)<br>
     * add(String collection, SolrInputDocument doc, int commitWithinMs)<br>
     * </p>
     * <p>
     * SolrInputDocument和Object之间转换<br>
     * doc = getBinder().toSolrInputDocument(obj);<br>
     * objList =solr.getBinder().getBeans(CaseEntity.class, resp.getResults());
     * </p>
     * ------------------------ bin/post -c solr-cloud-learn example/exampledocs/books.csv ----------------------------------
     * <p>
     * /usr/java/jdk1.8.0_91/bin/java -classpath /cluster/solr-6.6.0/dist/solr-core-6.6.0.jar -Dauto=yes -Dc=solr-cloud-learn -Ddata=files org.apache.solr.util.SimplePostTool example/exampledocs/books.csv
     * <br>SimplePostTool version 5.0.0
     * <br>Posting files to [base] url http://localhost:8983/solr/solr-cloud-learn/update...
     * <br>Entering auto mode. File endings considered are xml,json,jsonl,csv,pdf,doc,docx,ppt,pptx,xls,xlsx,odt,odp,ods,ott,otp,ots,rtf,htm,html,txt,log
     * <br>POSTing file books.csv (text/csv) to [base]
     * <br>1 files indexed.
     * <br>COMMITting Solr index changes to http://localhost:8983/solr/solr-cloud-learn/update...
     * <br>Time spent: 0:00:02.806
     * <p>
     * </p>
     */
    public static void main(String[] args) throws Exception {
        SolrClient solrClient;
        HttpClient httpClient;
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, 128);
        params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST, 32);
        params.set(HttpClientUtil.PROP_FOLLOW_REDIRECTS, false);
        httpClient = HttpClientUtil.createClient(params);

//        HttpSolrClient.Builder httpSolrClientBuilder = new HttpSolrClient.Builder()
//                .withBaseSolrUrl("http://192.168.5.204:8983/solr/solr-cloud-learn")
//                .withHttpClient(httpClient)
//                .allowCompression(true);
//        solrClient = httpSolrClientBuilder.build();

//        solrClient = new LBHttpSolrClient(httpSolrClientBuilder, httpClient, "http://192.168.5.204:8983/solr");
//        solrClient = new HttpSolrClient("http://192.168.5.200:8983/solr", httpClient, new XMLResponseParser(), true);


        CloudSolrClient.Builder builder = new CloudSolrClient.Builder();
        solrClient = builder.withZkHost("192.168.5.204:2191")
                .withHttpClient(httpClient)
                .withLBHttpSolrClient(new LBHttpSolrClient.Builder().build())
                .build();

        UpdateResponse response;

//        SolrInputDocument doc = new SolrInputDocument();
//        doc.addField("id", "223457");
//        doc.addField("name", "jeiifc_2");
//        doc.addField("age", 25);
//        doc.addField("enjoy", "i like read books.");
//        doc.addField("phone", "1234567891");
//        response = solrClient.add(doc);
//        if (response.getStatus() == 0) System.out.println("插入索引成功");

//        response = solrClient.deleteById("223457", 5);
//        if (response.getStatus() == 0) System.out.println("删除索引成功");

        response = solrClient.commit("solr-cloud-learn");
        System.out.println(response);

        solrClient.close();
    }


}
