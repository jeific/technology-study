package com.broadtech.embed.jetty;

import org.eclipse.jetty.server.Server;

public class SimplestServer {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8010);
        server.start();
        server.dumpStdErr();
        server.join();
    }
}
