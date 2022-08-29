package com.snowlake.java.path;

import java.util.ArrayList;
import java.util.List;

public class PathRel {
    private PathRel root = this;
    private int depth = 1; // 链条深度
    private int uv = 1;
    private String spId;
    private String userId;
    private String clientCatalog;

    private final String source;
    private String target;
    private PathRel parent = null; // 表示当前是root节点
    private PathRel next;
    private PathRel cycle;
    private PathRel cycleEndpoint; // 回环的目标端点
    private int forwardCount;  // 向前移动次数
    private int backwardCount; // 向后移动次数
    private boolean backward = false; // 标记从target回到source情况
    private boolean isEnd = true; // 表示路径的终点，在回环的情况下可以标识结束位置
    private List<Integer> outForwardCountsByPath = null; // 路径上向前移动次数集合
    private List<Integer> outBackwardCountsByPath = null; // 路径上向后移动次数集合

    PathRel(String source, String target) {
        this.source = source;
        this.target = target;
    }

    PathRel set(String userId, String spId, String clientCatalog) {
        getRoot().userId = userId;
        getRoot().spId = spId;
        getRoot().clientCatalog = clientCatalog;
        return this;
    }

    protected void setTarget(String target) {
        this.target = target;
    }

    private void setParent(PathRel parent) {
        this.parent = parent;
    }

    PathRel incrementCount() {
        this.backward = false;
        forwardCount++;
        return this;
    }

    private void setRoot(PathRel root) {
        this.root = root;
    }

    PathRel incrementBackwardCount() {
        this.backward = true;
        backwardCount++;
        return this;
    }

    PathRel getBackPathRel() {
        return this;
    }

    /**
     * 路径向前特性： source等于上一个target，target不等于上一个的source
     */
    boolean isForward(String nextSource, String target) {
        if (backward) {
            return nextSource.equalsIgnoreCase(this.source) && target.equalsIgnoreCase(this.target);
        } else {
            if (next != null) {
                return nextSource.equalsIgnoreCase(this.target) && target.equalsIgnoreCase(next.target);
            } else {
                return nextSource.equalsIgnoreCase(this.target) && !target.equalsIgnoreCase(this.source);
            }
        }
    }

    /**
     * 返回到父节点
     * 路径返回特性： source等于上一个target，target等于上一个的source
     */
    boolean isBackward(String source, String target) {
        return target.equalsIgnoreCase(this.source) && source.equals(this.target);
    }

    public PathRel getParent() {
        return parent;
    }

    /**
     * 环至少由3个角色构成
     */
    boolean isCycle(String source, String target) {
        // 回环必须与当前尾部节点相连
        if (!this.target.equalsIgnoreCase(source)) return false;
        if (next != null) return false; // 回还的起点必须是路径的终点，否则节点将存在2条出口
        if (getParent() == null) return false;
        cycleEndpoint = getParent().findCycleEndpoint(source, target);
        return cycleEndpoint != null;
    }

    /**
     * 向回寻找环路节点，即找到target等于节点的source，source不等于节点的target这样的节点
     *
     * @return null表示未搜索到
     */
    private PathRel findCycleEndpoint(String source, String target) {
        if (target.equalsIgnoreCase(this.source) && !source.equals(this.target)) {
            return this;
        } else if (getParent() != null) {
            return getParent().findCycleEndpoint(source, target);
        } else {
            return null;
        }
    }

    boolean isEnd() {
        return isEnd;
    }

    PathRel getNext() {
        return next;
    }

    private PathRel getCycle() {
        return cycle;
    }

    private void incrementDepth() {
        getRoot().depth += 1;
    }

    PathRel createNew(String source, String target) {
        return new PathRel(this.target, target).set(getUserId(), getSpId(), getClientCatalog());
    }

    PathRel setNext(String target) {
        if (backward) return this;
        if (next != null) {
            return next;
        }
        next = createNew(this.target, target);
        incrementDepth();
        next.setRoot(getRoot());
        next.setParent(this);
        this.isEnd = false;
        return next;
    }

    PathRel setCycle(String target) {
        if (cycle != null) {
            return cycle;
        }
        cycle = createNew(this.target, target);
        incrementDepth();
        cycle.setRoot(getRoot());
        cycle.setParent(this);
        cycle.next = cycleEndpoint;
        this.next = cycle;
        this.isEnd = true;
        return cycle;
    }

    public int getUv() {
        return getRoot().uv;
    }

    public String getUserId() {
        return getRoot().userId;
    }

    public String getSpId() {
        return getRoot().spId;
    }

    public String getClientCatalog() {
        return getRoot().clientCatalog;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    /**
     * @return 回到路径根部
     * 2种结构:
     * 1. 不涉及回环:  spId:s->t1-t2
     * 2. 涉及回环: spId:s->t1-t2@
     */
    public PathRel getRoot() {
        return root;
    }

    /**
     * 当前连线source向target方向的点击数
     */
    public int getForwardCount() {
        return forwardCount;
    }

    /**
     * 当前连线target返回source方向的点击数
     */
    public int getBackwardCount() {
        return backwardCount;
    }

    /**
     * 相同路径点击数合并
     */
    public void merge(PathRel p2) {
        if (this.equals(getRoot()) && !p2.getUserId().equals(getUserId())) {
            this.uv += p2.uv; // 合并uv
            set(p2.getUserId(), p2.getSpId(), p2.getClientCatalog());
        }
        this.forwardCount += p2.forwardCount;
        this.backwardCount += p2.backwardCount;
        if (!isEnd()) {
            getNext().merge(p2.getNext());
        } else if (cycle != null && p2.cycle != null) {  // 检查尾部是否存在回环
            getCycle().merge(p2.getCycle());
        } else if (p2.cycle != null) {
            this.cycle = p2.cycle;
        }
    }

    /**
     * 统计路径上的所有点击数（in方向+out方向）
     */
    public int total() {
        return forwardCount + backwardCount + (isEnd() ? (cycle != null ? cycle.total() : 0) : next.total());
    }

    @Override
    public String toString() {
        return getUv() + "&" + desc(true, true, true);
    }

    /**
     * @return 路径涉及的节点集
     */
    public List<String> getNodes() {
        List<String> nodes = new ArrayList<>();
        outForwardCountsByPath = new ArrayList<Integer>();
        outBackwardCountsByPath = new ArrayList<>();
        PathRel node = this;
        nodes.add(node.getSource());
        while (!node.isEnd()) {
            nodes.add(node.getTarget());
            outForwardCountsByPath.add(node.getForwardCount());
            outBackwardCountsByPath.add(node.getBackwardCount());
            node = node.getNext();
        }
        return nodes;
    }

    /**
     * @return 路径描述，以字符串形式
     * 存在2种结构：
     * 1）存在回环：spId:s->t1->t2@
     * 1）不存在回环：spId:s->t1->t2
     */
    public String getPath() {
        return getSpId() + ":" + desc(true, false, false);
    }

    private String desc(boolean root, boolean hasCount, boolean hasCycle) {
        String str = getSourceDesc(root) + getCntDesc(hasCount) + getTargetDesc();
        if (isEnd()) {
            if (hasCycle && cycle != null) str += cycle.desc(false, hasCount, false) + "@";
        } else {
            str += next.desc(false, hasCount, hasCycle);
        }
        return str;
    }

    protected String getSourceDesc(boolean root) {
        return root ? source : "";
    }

    protected String getTargetDesc() {
        return target == null ? "" : "->" + target;
    }

    protected String getCntDesc(boolean hasCount) {
        return hasCount ? "-" + forwardCount + ":" + backwardCount : "";
    }

    /**
     * 从source流向target的点击数
     */
    public List<Integer> getBackwardCounts() {
        return outBackwardCountsByPath;
    }

    /**
     * 从target流向source的点击数
     */
    public List<Integer> getForwardCounts() {
        return outForwardCountsByPath;
    }
}
