package com.snowlake.java.path;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * 该模型在完成数据转化后，获取以删除模式执行，获取一旦获取一次则会消失
 */
public class VideoPayData implements Serializable {
    private Map<String, LongAdder> payOkMap = new HashMap<>();
    private Map<String, LongAdder> payCancelMap = new HashMap<>();

    /**
     * 添加影片的支付记录
     *
     * @param spId      sp id
     * @param userId    用户id
     * @param videoId   影片id
     * @param payStatus 支付状态
     */
    public void addPayRecord(String spId, String userId, String videoId, String payStatus) {
        if ("pay_ok".equals(payStatus)) {
            payOkMap.computeIfAbsent(getKey(spId, userId, videoId), k -> new LongAdder()).increment();
        } else {
            payCancelMap.computeIfAbsent(getKey(spId, userId, videoId), k -> new LongAdder()).increment();
        }
    }

    private String getKey(String spId, String userId, String videoId) {
        return spId + userId + videoId;
    }

    long getPayOkCount(String spId, String userId, String videoId) {
        LongAdder isPayOk = payOkMap.remove(getKey(spId, userId, videoId));
        return isPayOk == null ? 0 : isPayOk.sum();
    }

    long getPayCancelCount(String spId, String userId, String videoId) {
        LongAdder longAdder = payCancelMap.remove(getKey(spId, userId, videoId));
        return longAdder == null ? 0 : longAdder.sum();
    }
}
