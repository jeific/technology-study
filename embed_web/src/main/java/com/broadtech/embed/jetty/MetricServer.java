package com.broadtech.embed.jetty;

import com.codahale.metrics.servlets.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class MetricServer {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8010);
        addHandler(server);
        server.start(); // server启动后必须显示关闭，否则将会持续运行
        server.dumpStdErr();
        Thread.sleep(10000);
        System.out.println("准备关闭程序");
        System.exit(0);  // 经测试，在kill程序后，可以不显示调用server.stop()，亦可停止程序
    }

    private static void addHandler(Server server) {
        HandlerList handlerList = new HandlerList();
        ServletContextHandler handler = new ServletContextHandler();
        handlerList.addHandler(handler);
        handler.setContextPath("/");
        handler.addServlet(ThreadDumpServlet.class, "/th");
        handler.addServlet(CpuProfileServlet.class, "/cpu");
        handler.addServlet(AdminServlet.class, "/admin");
        handler.addServlet(HealthCheckServlet.class, "/health");
        handler.addServlet(PingServlet.class, "/ping");

//        MetricRegistry metricRegistry = new MetricRegistry();
//        InstrumentedHandler instrumentedHandler = new InstrumentedHandler(metricRegistry);
//        handlerList.addHandler(instrumentedHandler);

        server.setHandler(handlerList);
    }
}
