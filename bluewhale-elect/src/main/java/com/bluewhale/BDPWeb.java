package com.bluewhale;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BDPWeb {
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        String quorum = System.getenv("ZK_QUORUM");
        String nodePath = System.getenv("CLR_ZK_PATH");
        long deleteBound = new Date().getTime() - 10 * 60 * 1000;
        long deleteJobBound = new Date().getTime() - 48 * 60 * 60 * 1000;
        ZooKeeper zooKeeper = new ZooKeeper(quorum, 5000, getWatcher());
        List<String> jobNodes = zooKeeper.getChildren(nodePath, getWatcher());
        for (String jobNode : jobNodes) {
            String jobPath = nodePath + '/' + jobNode;
            List<String> jobElectNodes = zooKeeper.getChildren(jobPath, getWatcher());
            for (String electNode : jobElectNodes) {
                String electPath = jobPath + '/' + electNode;
                if (expire(zooKeeper, electPath, deleteBound)) {
                    recursionDel(zooKeeper, electPath);
                }
            }
            jobElectNodes = zooKeeper.getChildren(jobPath, getWatcher());
            if (jobElectNodes.isEmpty() && expire(zooKeeper, jobPath, deleteJobBound)) {
                recursionDel(zooKeeper, jobPath);
            }
        }
        zooKeeper.close();
    }

    private static Watcher getWatcher() {
        return watchedEvent -> {
        };
    }

    private static void recursionDel(ZooKeeper zooKeeper, String path) throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(path, getWatcher());
        if (children.isEmpty()) {
            System.out.println("删除zk路径:" + path + "\t创建时间:" + format(zooKeeper.exists(path, getWatcher()).getCtime()));
            zooKeeper.delete(path, 0);
        } else {
            for (String child : children) {
                recursionDel(zooKeeper, path + '/' + child);
            }
        }
    }

    private static String format(long ctime) {
        return df.format(ctime);
    }

    private static boolean expire(ZooKeeper zk, String path, long deleteBound) throws KeeperException, InterruptedException {
        Stat state = zk.exists(path, getWatcher());
        if (state != null && state.getCtime() < deleteBound) {
            System.out.println("节点" + path + "已经存在超过" + formatTime(deleteBound) + "，将被删除");
            return true;
        }
        // System.out.println("节点" + path + "未过期");
        return false;
    }

    private static String formatTime(long timePoint) {
        long mins = (System.currentTimeMillis() - timePoint) / 60000;
        if (mins < 60) return mins + "分钟";
        else return mins / 60 + "小时";
    }
}
