package com.snowlake.java.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JavaBase {
    protected long total = -1;

    public static void main(String[] args) throws IOException, InterruptedException {
        /**
         * 位移运算符： 左目运算符
         */
        System.out.println(8 << 1);   // 左移一位 === * 2
        System.out.println(8 >> 1);   // 右移一位 === /2
        System.out.println((byte) 255 >> 1);
        System.out.println((byte) 255 >>> 1); // 无符号右移，忽略符号位，空位都以0补齐; 无符号右移一位 === /2
        new JavaBase().m();
        Logger logger = LoggerFactory.getLogger(JavaBase.class);
        logger.info("=========测试子进程如果存在标准输出，若不消费子进程的输出，" +
                "将导致输出流满而阻塞========");
        ProcessBuilder processBuilder = new ProcessBuilder(
                "/bin/bash", "-c",
                "java -cp ./conf:lib/* com.snowlake.java.util.SubProcess");
        Process process = processBuilder.redirectErrorStream(true).start();
        //获取进程是否在执行
        int count = 1;
        while (process.isAlive()) {
            TimeUnit.SECONDS.sleep(1);
            logger.info("{} 等待子进程停止", count);
        }
        logger.info("========== END ===========");
    }

    public void m() {
        for (int i = 1; i <= 12; i++) {
            int quarter = (i - 1) / 3 * 3 + 1;
            System.out.print(quarter + " "); // 1 4 7 10
        }
    }

}
