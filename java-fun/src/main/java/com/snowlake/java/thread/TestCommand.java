package com.snowlake.java.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 子进程
 */
public class TestCommand {
    private static final Logger logger = LoggerFactory.getLogger(TestCommand.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("JAVA_TOOL_OPTIONS:" + System.getProperty("file.encoding"));

        System.out.println("-----------------");
        logger.info("start child thread: " + TestCommand.class.getSimpleName());
        logger.info("receive parent thread env: " + System.getProperty("new_env")); // 不可读取
        // 接入父进程数据输出管道
        BufferedReader s = new BufferedReader(new InputStreamReader(System.in));
        logger.info("receive 接入父进程数据输出管道");
        String line;
        StringBuilder all = new StringBuilder();
        while ((line = s.readLine()) != null) {
            all.append(line);
        }
        logger.info("all: {}", all);
        s.close();
        Thread.sleep(20000);
        logger.info("child thread over");
    }
}
