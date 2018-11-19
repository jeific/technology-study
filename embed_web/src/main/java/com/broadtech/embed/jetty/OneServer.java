package com.broadtech.embed.jetty;

import com.broadtech.embed.jetty.handler.HelloHandler;
import org.eclipse.jetty.server.Server;

public class OneServer {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8010);

        server.setHandler(new HelloHandler());

        server.start();
        server.join();
    }
}
