package com.snowlake.java.io;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Zookeeper工具类
 * 记得调用 close()
 */
public class ZookeeperClientHelper {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClientHelper.class);

    private CuratorFramework client;

    public ZookeeperClientHelper(String zookeeperQuorum) {
        client = CuratorFrameworkFactory.newClient(zookeeperQuorum, new RetryNTimes(3, 5000));
        client.start();
        try {
            logger.info("CuratorFramework => " + client
                    + "  ZooKeeper => " + client.getZookeeperClient().getZooKeeper()
                    + " ZooKeeper.hashCode: " + client.getZookeeperClient().getZooKeeper().hashCode());
        } catch (Exception e) {
            logger.info("打开ZooKeeper会话遭遇异常", e);
        }
    }

    public CuratorFramework getClient() {
        return client;
    }

    public boolean createPersistent(String path, byte[] data) {
        try {
            if (!exists(path)) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path, data);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean createEphemeral(String path, byte[] data) {
        try {
            return exists(path) ||
                    client.create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.EPHEMERAL)
                            .forPath(path, data) != null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean delete(String path) {
        if (path == null) return true;
        try {
            if (exists(path))
                client.delete().forPath(path);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean recursiveDelete(String path) {
        if (path == null || !exists(path)) return true;
        String[] children = getChildrenNode(path);
        for (String child : children) {
            recursiveDelete(path + "/" + child);
        }
        return delete(path); // 在子节点删除完毕后删除自己
    }

    public byte[] getData(String path) {
        boolean isExist = exists(path);
        if (isExist) {
            try {
                return client.getData().forPath(path);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return new byte[0];
    }

    public boolean setData(String path, String data) {
        try {
            if (!exists(path)) {
                createPersistent(path, data == null ? null : data.getBytes(UTF_8));
            }
            client.setData().forPath(path, data == null ? null : data.getBytes(UTF_8));
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean exists(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            if (stat != null) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public String[] getChildrenNode(String path) {
        if (exists(path)) {
            try {
                return client.getChildren().forPath(path).toArray(new String[0]);
            } catch (Exception e) {
                logger.error(e.toString(), e);
                return new String[0];
            }
        }
        return new String[0];
    }

    public void close() {
        if (client != null)
            client.close();
    }
}
