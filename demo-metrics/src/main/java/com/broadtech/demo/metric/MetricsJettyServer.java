package com.broadtech.demo.metric;

import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class MetricsJettyServer {
    private final Server server = new Server(1900);

    public void start(MetricRegistry metrics) throws Exception {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        MetricsServlet metricsServlet = new MetricsServlet(metrics);
        contextHandler.addServlet(new ServletHolder(metricsServlet), "/jmx");  // 语法允许
        contextHandler.addServlet(ThreadDumpServlet.class, "/thread");

        HandlerList handlerList = new HandlerList();
        handlerList.addHandler(contextHandler);
        server.setHandler(handlerList);
        server.start();
        System.out.println("jmx server: http://localhost:1900/jmx");
    }

    public void stop() throws Exception {
        server.stop();
    }
}
