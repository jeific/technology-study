package com.broadtech.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchServer {
    private static final Logger logger = LoggerFactory.getLogger(BenchServer.class);

    public static void main(String[] args) throws Exception {
        int port = 2505;
        Server server = new Server(port);
        HandlerList handlerList = new HandlerList();
        if (args.length > 0 && args[0].equals("online")) {
            handlerList.addHandler(loadWebAppWithPath());
        } else {
            handlerList.addHandler(loadWebApp());
        }
        server.setHandler(handlerList);
        server.start();
        logger.info("start web server : http://localhost:" + port);
    }


    /**
     * 用于以war格式部署模式： 主要本地测试
     */
    private static WebAppContext loadWebApp() {
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setWelcomeFiles(new String[]{"index.html"});
        context.setWar("bdp-bench-1.0-SNAPSHOT.war");
        return context;
    }

    /**
     * 用于以非war格式部署的online模式
     */
    private static WebAppContext loadWebAppWithPath() {
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setWelcomeFiles(new String[]{"index.html"});
        context.setResourceBase("webapp");
        return context;
    }
}
