package com.snowlake.java.path;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 热门影片
 */
public class HotVideo implements Serializable {
    private Map<String, Set<String>> hotVideo = new HashMap<>();

    /**
     * 添加热门影片集合
     *
     * @param spId  sp id
     * @param video 影片id
     */
    public void addHotVideo(String spId, String video) {
        hotVideo.computeIfAbsent(spId, k -> new HashSet<>()).add(video);
    }

    boolean isHot(String spId, String video) {
        return hotVideo.computeIfAbsent(spId, k -> new HashSet<>()).contains(video);
    }

    public int count(String spId) {
        return hotVideo.getOrDefault(spId, new HashSet<>()).size();
    }
}
