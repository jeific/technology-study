package com.starcor.biz.demo.servlet;

import com.broadtech.kpiserver.spi.Job;
import com.broadtech.kpiserver.spi.QueryTask;
import com.broadtech.kpiserver.spi.WebAppService;
import com.broadtech.kpiserver.spi.context.Context;
import com.starcor.biz.demo.common.AppAsyncListener;
import com.starcor.biz.demo.common.AsyncRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Scanner;

@WebServlet(urlPatterns = "/query", asyncSupported = true)
public class AppMainServlet extends WebAppService {
    private Logger log;
    private Context context;

    @Override
    public void init() throws ServletException {
        super.init();
        log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public WebAppService setContext(Context context) {
        this.context = context;
        return this;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*"); //允许跨域访问
        StringBuilder stringBuilder = new StringBuilder(2000);
        Scanner scanner = new Scanner(request.getInputStream(), "UTF-8");
        while (scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
        }
        String body = stringBuilder.toString().trim(); // 获取post的body
        if (body.equals("")) {
            body = "{}";
        }

        String action = request.getParameter("action");            //用来区分不同的查询接口
        if (action == null) {
            String msg = "{\"ret\":2,\"reason\":\"sorry: action 不能为空\"}";
            response.getWriter().println(msg);
            return;
        }
        //------action----------switch------------
        Job<QueryTask> job;
        try {
            job = context.createJob(action)
                    .option("body", body)
                    .option("action", action)
                    .build();
        } catch (Exception e) {  ////action 不存在或者job创建失败
            response.getWriter().append(e.getMessage()).close();
            return;
        }
        log.info("action:{} 生成一个task 查询计划 queryId={} Task: {} Body: {}"
                , action, job.getJobId(), job.getTask().getClass().getName(), body);

        final AsyncContext asyncContext = request.startAsync();
        //添加监听器监听异步的执行结果
        asyncContext.addListener(new AppAsyncListener(job.getJobId()));
        //设置超时的时间，到了时间以后，会回调onTimeout的方法   //超时时间设置为30秒
        asyncContext.setTimeout(1000 * 120);        //30秒
        //在这里启动，传入一个Runnable对象，服务器会把此Runnable对象放在线程池里面执行
        asyncContext.start(new AsyncRunnable(asyncContext, job));
    }
}
