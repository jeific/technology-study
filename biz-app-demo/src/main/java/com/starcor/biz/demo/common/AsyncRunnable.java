package com.starcor.biz.demo.common;

import com.broadtech.kpiserver.spi.Job;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.AsyncContext;
import java.io.IOException;
import java.io.PrintWriter;

public class AsyncRunnable implements Runnable {
    private AsyncContext asyncContext = null;
    private Job job = null;
    private String queryId;
    private final Log log = LogFactory.getLog(AsyncRunnable.class);

    public AsyncRunnable(AsyncContext asyncContext, Job job) {
        this.asyncContext = asyncContext;
        this.job = job;
        queryId = String.valueOf(System.currentTimeMillis());
    }

    @Override
    public void run() {
        try {
            String outjson = job.runJob();            //查询
            PrintWriter out = asyncContext.getResponse().getWriter();
            out.write(outjson);
        } catch (IOException | IllegalStateException e) {
            log.warn("Query:" + queryId + " 查询完毕，但超时了");
            //任务 失败状态码2, -1:超时查询中
            //ApiHelper.googleCache.put(queryId,outjson);
        } catch (Exception e) {
            log.error("未预料的错误:", e);
            //ApiHelper.googleCache.put(queryId,"-2");  //超时的同时还失败了
        } finally {
            //在这里做耗时的操作，如果做完，则调用complete方法通知回调，异步处理结束了
            try {
                asyncContext.complete();
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }

    }
}
