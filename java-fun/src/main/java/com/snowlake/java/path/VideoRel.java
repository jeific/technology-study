package com.snowlake.java.path;

/**
 * 影片关系
 * 描述：浏览（播放）自己后下一个浏览（播放）影片
 * 记录路径的VV（播放次数），Uv（浏览次数）
 */
public class VideoRel extends PathRel {
    private int payOkCnt = 0; // 0 表示没有支付成功记录
    private int payCancelCnt = 0;  // 0表示没有支付取消
    private int sourceCnt;
    private int targetCnt;

    private int endPointVideoPayOkCnt = 0; // 0 表示没有支付成功记录
    private int endPointVideoPayCancelCnt = 0;  // 0表示没有支付取消

    VideoRel(String source, String target) {
        super(source, target);
    }

    VideoRel pay(VideoPayData payData) {
        payOkCnt += payData.getPayOkCount(getSpId(), getUserId(), getSource());
        payCancelCnt += payData.getPayCancelCount(getSpId(), getUserId(), getSource());
        if (getTarget() != null) {
            endPointVideoPayOkCnt += payData.getPayOkCount(getSpId(), getUserId(), getTarget());
            endPointVideoPayCancelCnt += payData.getPayCancelCount(getSpId(), getUserId(), getTarget());
        }
        return this;
    }

    VideoRel incrementSourceCnt() {
        sourceCnt++;
        return this;
    }

    void incrementTargetCnt() {
        targetCnt++;
    }

    @Override
    protected String getSourceDesc(boolean root) {
        return super.getSourceDesc(root) + (root ? "(" + getCount() + ")" : "");
    }

    @Override
    protected String getTargetDesc() {
        return super.getTargetDesc() + (getTarget() != null && !isEnd()
                && ((VideoRel) getNext()).getCount() > 0 ? "(" + ((VideoRel) getNext()).getCount() + ")" : "");
    }

    public int getCount() {
        if (getParent() == null) {
            return sourceCnt;
        } else {
            return ((VideoRel) getParent()).targetCnt;
        }
    }

    @Override
    PathRel createNew(String source, String target) {
        return new VideoRel(source, target).set(getUserId(), getSpId(), getClientCatalog());
    }

    public VideoRel repairTarget(String target) {
        setTarget(target);
        return this;
    }

    public int getPayOkCnt() {
        return payOkCnt;
    }

    public int getPayCancelCnt() {
        return payCancelCnt;
    }

    /**
     * @return 端点（end）支持成功次数
     */
    public int getEndPointPayOkCnt() {
        if (isEnd()) {
            return endPointVideoPayOkCnt;
        } else {
            return ((VideoRel) getParent()).endPointVideoPayOkCnt;
        }
    }

    /**
     * @return 端点（end）支持取消次数
     */
    public int getEndPointPayCancelCnt() {
        if (isEnd()) {
            return endPointVideoPayCancelCnt;
        } else {
            return ((VideoRel) getParent()).endPointVideoPayCancelCnt;
        }
    }

}
