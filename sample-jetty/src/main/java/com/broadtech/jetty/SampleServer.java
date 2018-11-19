package com.broadtech.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class SampleServer {
    private static final Logger logger = LoggerFactory.getLogger(SampleServer.class);

    public static void main(String[] args) throws Exception {
        int port = 2502;
        Server server = new Server(port);
        HandlerList handlerList = new HandlerList();
        initHandler(handlerList);
        server.setHandler(handlerList);
        server.start();
        logger.info("start web server : http://localhost:" + port);
        list("http://localhost:" + port);
    }

    /**
     * 使用{@link WebAppContext}加载管理静态资源<br>
     */
    private static void initHandler(HandlerList handlerList) {
        WebAppContext context = new WebAppContext();
        handlerList.addHandler(context);
        context.setContextPath("/");
        context.setResourceBase("webapp"); // 基于安装目录（运行目录）设置相对地址
        //context.setDescriptor("webapp/WEB_INF/web.xml");
        // context.setWar(""); 使用war格式载入应用
        context.addServlet(SampleServlet.class, "/hello");
    }

    private static void list(String site) {
        Arrays.stream(Objects.requireNonNull(new File("webapp").listFiles()))
                .forEach(f -> {
                    if (f.isDirectory() && !f.getName().equals("WEB-INF")) {
                        if (!f.getName().equals("demo")) {
                            logger.info(site + "/" + f.getName());
                        } else {
                            Arrays.stream(Objects.requireNonNull(f.listFiles())).forEach(cf -> {
                                try {
                                    String indexHtml = new String(Files.readAllBytes(new File(cf, "index.html").toPath()));
                                    int startIndex = indexHtml.indexOf("<title>");
                                    int endIndex = indexHtml.indexOf("</title>");
                                    String title = indexHtml.substring(startIndex + 7, endIndex);
                                    logger.info(site + "/demo/" + cf.getName() + "\t" + title);
                                } catch (IOException e) {
                                    logger.error(e.toString(), e);
                                }
                            });
                        }
                    }
                });
    }
}
