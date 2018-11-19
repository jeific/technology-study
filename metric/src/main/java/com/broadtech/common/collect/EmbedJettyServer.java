package com.broadtech.common.collect;

import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;

public class EmbedJettyServer {
    private static final Logger logger = LoggerFactory.getLogger(EmbedJettyServer.class);
    private final Server server;
    private final int port;

    public EmbedJettyServer(int port) {
        this.port = port;
        server = new Server(port);
    }

    public void start(MetricRegistry metrics) throws Exception {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        MetricsServlet metricsServlet = new MetricsServlet(metrics);
        contextHandler.addServlet(new ServletHolder(metricsServlet), "/jmx");  // 语法允许
        contextHandler.addServlet(ThreadDumpServlet.class, "/thread");

        HandlerList handlerList = new HandlerList();
        handlerList.addHandler(contextHandler);
        server.setHandler(handlerList);
        try {
            server.start();
            logger.info("jmx server: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/jmx");
        } catch (BindException e) {
            throw new IOException(this.port + e.getMessage(), e);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }
}
