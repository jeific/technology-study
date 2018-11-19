package com.broadtech.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettySpringServer {
    private static final Logger logger = LoggerFactory.getLogger(JettySpringServer.class);

    public static void main(String[] args) throws Exception {
        int port = 2501;
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
        context.addServlet(HelloWorldServlet.class, "/servlet");
        context.setWar("jetty-spring-1.0-SNAPSHOT.war");
        return context;
    }

    /**
     * 用于以非war格式部署的online模式
     */
    private static WebAppContext loadWebAppWithPath() {
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setWelcomeFiles(new String[]{"index.html"});
        context.addServlet(HelloWorldServlet.class, "/servlet");
        context.setResourceBase("webapp");
        return context;
    }

    private static Handler loadResource() {
        ResourceHandler resourceHandler = new ResourceHandler();  //静态资源处理的handler
        resourceHandler.setResourceBase("static");
        resourceHandler.setDirectoriesListed(true);
        return resourceHandler;
    }
}
