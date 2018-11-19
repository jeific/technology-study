package com.snowlake.java.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 使用管道在父子进程间通讯
 */
public class TestCommunication {
    private static final Logger logger = LoggerFactory.getLogger(TestCommunication.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("JAVA_TOOL_OPTIONS:" + System.getProperty("file.encoding"));
        System.setProperty("new_env", "Hello world");
        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String cp = System.getProperty("java.class.path");
        cp += File.pathSeparator + ClassLoader.getSystemResource("").getPath();
        String[] cmd = new String[]{java, "-cp", cp, "-Dfile.encoding=UTF-8", TestCommand.class.getName()}; // 命令保险的使用方式：数组
        // 启动子进程，并合并错误流
//        Thread th = new Thread() {
//            @Override
//            public void run() {
//                try {
//            Process p = Runtime.getRuntime().exec(cmd);
//                    Process p = new ProcessBuilder().redirectErrorStream(true).command(cmd).start();
//                    // 使用Process管道流交换数据
//                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
//                    bw.write("999999");
//                    bw.flush();
//                    bw.close();
//                    BufferedInputStream in = new BufferedInputStream(p.getInputStream());
//                    String s;
//                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
//                    logger.info("read child thread data。 开始接收");
//                    while ((s = br.readLine()) != null)
//                        logger.info(s);
//                } catch (IOException e) {
//                    logger.error(e.toString(), e);
//                }
//            }
//        };
//        th.setDaemon(true);
//        th.start();
        Thread.sleep(10000);
        logger.info("main over");
    }
}
