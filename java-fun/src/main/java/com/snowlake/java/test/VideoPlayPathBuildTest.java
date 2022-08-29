package com.snowlake.java.test;

import com.snowlake.java.path.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class VideoPlayPathBuildTest {

    public static void main(String[] args) throws IOException {
        String hotVoidPath = "F:\\项目\\bluewhale\\路径 影片\\play_log_hot_video.out";
        String videoPayPath = "F:\\项目\\bluewhale\\路径 影片\\pay_video.out";
        String playLogPath = "F:\\项目\\bluewhale\\路径 影片\\play_trak.out";
        HotVideo hotVideo = getHot(hotVoidPath);
        VideoPayData videoPayData = getVideoPayData(videoPayPath);
        Map<String, NodeLink> pathRelMap = new HashMap<>();
        build(hotVideo, videoPayData, pathRelMap, playLogPath, 3);

        List<NodeLink> list = new ArrayList<>(pathRelMap.values());
        list.sort(Comparator.comparingInt(NodeLink::total));
        System.out.println("pv\tuv\tlevel\tpath");
        list.forEach(v -> System.out.println(v.total() + "\t" + v.getRoot().getUv() + "\t" + v.getRoot().getLevel() + "\t" + v.toString()));
        System.out.println("path count:" + pathRelMap.size());
        System.out.println("total count:" + pathRelMap.values().stream().flatMapToInt(v -> IntStream.of(v.total())).sum());
    }

    private static void build(HotVideo hotVideo, VideoPayData videoPayData, Map<String, NodeLink> pathRelMap, String playLogPath, int pathLevelLimit) throws IOException {
        VideoPlayPathBuild build = new VideoPlayPathBuild();
        List<VideoInfo> userGroup = new ArrayList<>();
        read(playLogPath, fields -> {
            String userId = fields[1];
            if (!userGroup.isEmpty() && !userGroup.get(0).getUserId().equals(userId)) { // 切换用户
                build.buildPath(hotVideo, videoPayData, userGroup, pathRelMap, pathLevelLimit);
                userGroup.clear();
            }
            userGroup.add(new VideoInfo(fields[0], userId, fields[5], "", fields[6]));
        });
        if (!userGroup.isEmpty())
            build.buildPath(hotVideo, videoPayData, userGroup, pathRelMap, pathLevelLimit);
    }

    private static HotVideo getHot(String path) throws IOException {
        HotVideo hotVideo = new HotVideo();
        read(path, fields -> {
            hotVideo.addHotVideo(fields[0], fields[1]);
        });
        return hotVideo;
    }

    private static VideoPayData getVideoPayData(String path) throws IOException {
        VideoPayData videoPayData = new VideoPayData();
        read(path, fields -> {
            videoPayData.addPayRecord(fields[0], fields[1], fields[4], fields[3]);
        });
        return videoPayData;
    }

    private static void read(String path, Consumer<String[]> consumer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                consumer.accept(line.split("\\s+"));
            }
        }
    }
}
