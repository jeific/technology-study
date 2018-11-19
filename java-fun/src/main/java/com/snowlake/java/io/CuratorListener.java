package com.snowlake.java.io;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.TimeUnit;

public class CuratorListener {

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("master01:2181", new RetryNTimes(3, 5000));
        client.start();
        String path = "/snow_lake_test_conntect";
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

        // 添加链接状态监听事件
        client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> {
            System.out.println("curatorFramework.connectionState => " + connectionState);
            if (ConnectionState.RECONNECTED.equals(connectionState)) {
                try {
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
                    System.out.println("curatorFramework.connectionState => create node.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 节点改变监听
        NodeCache nodeCache = new NodeCache(client, path, false);
        nodeCache.getListenable().addListener(() -> {
            System.out.println("node.change => " + new String(nodeCache.getCurrentData().getData()));
        });
        nodeCache.start();

        // CuratorEventType
        // client.getCuratorListenable().addListener(new org.apache.curator.framework.api.CuratorListener() {})

        while (true) {
            TimeUnit.SECONDS.sleep(3);
        }
    }

}
