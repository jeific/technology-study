package com.broadtech.util;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jeifi on 2017/8/13.
 */
public class ZKUtils {

    public static void main(String[] args) {
        String USAGE = "USAGE: <zkQuorum> <-d> <nodePath>";
        OutputStream out = System.out;
        if (args.length != 3) {
            writeLine(USAGE, out);
            return;
        }
        String zookeeperQuorum = args[0];
        String command = args[1];
        String arg = args[2];
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperQuorum, new RetryNTimes(3, 5000));
        try {
            client.start();
            switch (command) {
                case "-d":
                    writeLine("delete node => " + arg, out);
                    client.delete().deletingChildrenIfNeeded().forPath(arg); // 支持删除不为空的节点
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            client.close();
        }
    }

    private static void writeLine(String str, OutputStream out) {
        try {
            out.write(str.getBytes());
            out.write('\n');
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
