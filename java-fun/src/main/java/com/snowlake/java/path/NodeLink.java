package com.snowlake.java.path;

import java.io.Serializable;

/**
 * 音频啊节点关系描述
 * 描述访问关系和节点属性，不记录路径数据
 */
public class NodeLink implements Serializable {
    private int level = 1;
    private final String videoId;
    private final String videoName;
    private final boolean isHotVideo;
    private NodeLink root = this;
    private String spId;
    private String userId;
    private String clientCatalog;
    private int uv = 1;
    private int count;
    private long payOk = 0;
    private long payCancel = 0;
    private NodeLink parent;
    private NodeLink next;
    private NodeLink cursor; // 当前节点（游标）

    public NodeLink(String videoId, String videoName, boolean isHotVideo, long payOk, long payCancel) {
        this.videoId = videoId;
        this.videoName = videoName;
        this.isHotVideo = isHotVideo;
        count++;
        this.payOk = payOk;
        this.payCancel = payCancel;
    }

    public int getLevel() {
        return level;
    }

    public NodeLink getRoot() {
        return root;
    }

    public String getSpId() {
        return spId;
    }

    public String getUserId() {
        return userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getClientCatalog() {
        return clientCatalog;
    }

    public int getUv() {
        return uv;
    }

    public int getCount() {
        return count;
    }

    private NodeLink getParent() {
        return parent;
    }

    public NodeLink getNext() {
        return next;
    }

    public NodeLink getCursor() {
        return cursor;
    }

    public long getPayOk() {
        return payOk;
    }

    public long getPayCancel() {
        return payCancel;
    }

    public boolean isEnd() {
        return next == null;
    }

    NodeLink set(String userId, String spId, String clientCatalog) {
        getRoot().userId = userId;
        getRoot().spId = spId;
        getRoot().clientCatalog = clientCatalog;
        return this;
    }

    /**
     * 添加新节点
     *
     * @param isHotVideo 是否是热门影片
     * @return true 添加成功 false不能添加当前节点
     */
    boolean addNode(String videoId, String videoName, boolean isHotVideo, long payOk, long payCancel) {
        cursor = this;
        if (videoId.equals(this.videoId)) {
            count++;
            return true;
        }
        if (!isHotVideo && !this.isHotVideo) {
            return false;
        }
        // 判定播放方向
        if (getParent() != null && videoId.equals(getParent().videoId)) { // 向后
            cursor = getParent();
            getParent().count++;
            return true;
        } else if (getNext() != null) { // 判定路径是否重合，否则斩断
            if (!videoId.equals(getNext().videoId)) {
                return false;
            }
            cursor = getNext();
            getNext().count++;
            return true;
        } else if (checkCycle(videoId)) { // 检查回环, 存在则将游标移到回环处
            getCursor().count++;
            return true;
        } else { // 新建向前节点
            next = new NodeLink(videoId, videoName, isHotVideo, payOk, payCancel);
            getRoot().level += 1;
            next.parent = this;
            next.root = getRoot();
            cursor = next;
            return true;
        }
    }

    private boolean checkCycle(String video) {
        if (getParent() == null) return false;
        NodeLink node = getCycleNode(getParent().getParent(), video);
        if (node != null) {
            cursor = node;
            return true;
        }
        return false;
    }

    private NodeLink getCycleNode(NodeLink parent, String video) {
        if (parent != null) {
            if (parent.videoId.equals(video)) {
                return parent;
            } else {
                return getCycleNode(parent.getParent(), video);
            }
        }
        return null;
    }

    /**
     * 计算路径：如果影片发生支付信息，则附加字符信息
     */
    public String getPath() {
        NodeLink node = getRoot();
        StringBuilder builder = new StringBuilder();
        builder.append(node.getSpId()).append(":").append(node.getVideoId())
                .append(".").append(getOrElse(getPayOk())).append(".").append(getOrElse(getPayCancel()));
        while (((node = node.next)) != null) {
            builder.append("-").append(node.getVideoId())
                    .append(".").append(getOrElse(node.getPayOk())).append(".").append(getOrElse(node.getPayCancel()));
        }
        return builder.toString();
    }

    private String getOrElse(long l) {
        return l > 0 ? "Y" : "N";
    }

    public NodeLink merge(NodeLink node) {
        if (!node.getUserId().equals(getUserId())) { // 用户不同
            this.uv += node.uv; // 合并uv
            set(node.getUserId(), node.getSpId(), node.getClientCatalog());
        }
        NodeLink tmp1 = getRoot();
        NodeLink tmp2 = node.getRoot();
        merge(tmp1, tmp2);
        while (((tmp1 = tmp1.getNext())) != null) {
            tmp2 = tmp2.getNext();
            merge(tmp1, tmp2);
        }
        return getRoot();
    }

    private void merge(NodeLink tmp1, NodeLink tmp2) {
        tmp1.count += tmp2.count;
        tmp1.payOk += tmp2.payOk;
        tmp1.payCancel += tmp2.payCancel;
    }

    @Override
    public String toString() {
        NodeLink node = getRoot();
        StringBuilder builder = new StringBuilder();
        builder.append(node.getSpId()).append(":")
                .append(node.getVideoId()).append(":")
                .append(node.getVideoName())
                .append(".").append(getCount())
                .append(".").append(getPayOk()).append(".").append(getPayCancel());
        while (((node = node.next)) != null) {
            builder.append("-").append(node.getVideoId()).append(":")
                    .append(node.getVideoName())
                    .append(".").append(node.getCount())
                    .append(".").append(node.getPayOk()).append(".").append(node.getPayCancel());
        }
        return builder.toString();
    }

    public int total() {
        NodeLink node = getRoot();
        int total = node.getCount();
        while ((node = node.getNext()) != null) {
            total += node.getCount();
        }
        return total;
    }
}
