package com.starcor.biz.demo.common;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class AppAsyncListener implements AsyncListener {

    private String queryid;
    private final Log log = LogFactory.getLog(this.getClass());

    public AppAsyncListener(String queryid) {
        this.queryid = queryid;
    }

    @Override
    public void onComplete(AsyncEvent asyncEvent) throws IOException {
        //System.out.println(queryid+" onComplete,查询结束");
        log.info("Query:" + queryid + " onComplete");
        //在这里处理正常结束的逻辑
        // 在这里可以做一些资源清理工作
    }

    @Override
    public void onError(AsyncEvent asyncEvent) throws IOException {
        //在这里处理出错的逻辑
        //这里可以抛出错误信息
        log.error("Query:" + queryid + " 出现严重错误 ");
    }

    @Override
    public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        //在这里处理开始异步线程的逻辑
        //可以记录相关日志
        //System.out.println("onStartAsync");
        log.info("Query:" + queryid + " onStartAsync");
    }

    /**
     * 超时逻辑
     */
    @Override
    public void onTimeout(AsyncEvent asyncEvent) throws IOException {
        //在这里处理超时的返回的逻辑，写入 datamap表，然后返回query id 使其下一次再来

        //ApiHelper.googleCache.put(queryid, "-1"); // -1 缓存
        log.error("ERROR:  任务处理超时 query:" + this.queryid);
        String msg = "{\"ret\":2,\"message:\":\"查询超时\",\"query_id\":\"" + queryid + "\"}";

        ServletResponse response = asyncEvent.getAsyncContext().getResponse();
        PrintWriter out = response.getWriter();
        out.write(msg);
        out.close();

        //ServletResponse response = asyncEvent.getAsyncContext().getResponse();
        //response.getWriter().write(msg);
        //response.getWriter().append(msg).close();
    }

}
