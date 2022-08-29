package com.snowlake.java.path;

/**
 * 影片信息
 */
public class VideoInfo {
    private final String spId;
    private final String userId;
    private final String videoId;
    private final String clientCatalog;
    private final String videoName;

    public VideoInfo(String spId,
                     String userId,
                     String videoId,
                     String videoName,
                     String clientCatalog) {
        this.spId = spId;
        this.userId = userId;
        this.videoId = videoId;
        this.videoName = videoName;
        this.clientCatalog = clientCatalog;
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

    public String getClientCatalog() {
        return clientCatalog;
    }

    public String getVideoName() {
        return videoName;
    }
}
