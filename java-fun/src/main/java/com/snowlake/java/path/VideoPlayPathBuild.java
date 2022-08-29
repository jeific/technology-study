package com.snowlake.java.path;

import java.util.List;
import java.util.Map;

/**
 * 影片播放路径分析
 * 算法：
 * 热门影片：
 * select sp_id, asset_id,video_id
 * from (
 * select sp_id, asset_id, video_id,cnt,row_number() over(partition by sp_id,asset_id order by cnt  desc) num from (
 * select sp_id, asset_id, video_id, count(distinct play_sid)  cnt
 * from starcor.play_log
 * where day=20190301
 * group by sp_id,asset_id,video_id
 * having max(server_time)-min(server_time) >= 60000) a ) b  where num <= 20
 * group by sp_id,asset_id,video_id
 * <p>
 * 支付数据：
 * select sp_id,user_id,event_value["pay_status"] pay_status,event_value["video_id"] video_id from cqyx.user_log
 * where day=20190529 and event_name='pay' and event_value["pay_status"] <> 'pay_create' and event_value["video_id"] <> ''
 * and user_id <> '' order by sp_id,user_id,server_time
 * <p>
 * 播放记录（播放时间不小于1min）
 * select sp_id,user_id,max(video_id) video_id,max(apk_version) apk_version, min(server_time) min_server_time
 * from starcor.play_log
 * where day=20190301
 * group by sp_id,user_id,play_sid having max(server_time)-min(server_time) >= 60000
 * order by sp_id,user_id,min_server_time
 */
public class VideoPlayPathBuild {
    /**
     * 构建算法：连续的路径构建，一旦不能连续，则是新的路径开始
     *
     * @param hotVideo       热门影片
     * @param payData        支付数据
     * @param userVideoPlays 同一个用户按时间连续的记录
     * @param pathRelMap     以构建的路径组合集合
     */
    public void buildPath(HotVideo hotVideo, VideoPayData payData
            , List<VideoInfo> userVideoPlays, Map<String, NodeLink> pathRelMap, int pathLevelLimit) {
        NodeLink nodeLink = null;
        for (VideoInfo videoInfo : userVideoPlays) {
            if (nodeLink != null && nodeLink.getRoot().getLevel() >= pathLevelLimit) {
                store(pathRelMap, nodeLink);
                nodeLink = null;
            }
            // 首节点必须是热门影片
            if (nodeLink == null && hotVideo.isHot(videoInfo.getSpId(), videoInfo.getVideoId())) {
                nodeLink = getNewPath(videoInfo, payData);
                continue;
            }
            if (nodeLink == null) continue;
            if (nodeLink.addNode(videoInfo.getVideoId(), videoInfo.getVideoName(), hotVideo.isHot(videoInfo.getSpId(), videoInfo.getVideoId())
                    , payData.getPayOkCount(videoInfo.getSpId(), videoInfo.getUserId(), videoInfo.getVideoId())
                    , payData.getPayCancelCount(videoInfo.getSpId(), videoInfo.getUserId(), videoInfo.getVideoId()))) {
                nodeLink = nodeLink.getCursor();
            } else {
                store(pathRelMap, nodeLink);
                nodeLink = hotVideo.isHot(videoInfo.getSpId(), videoInfo.getVideoId()) ? getNewPath(videoInfo, payData) : null;
            }
        }
        if (nodeLink != null) store(pathRelMap, nodeLink);
    }

    private NodeLink getNewPath(VideoInfo videoInfo, VideoPayData payData) {
        NodeLink nodeLink = new NodeLink(videoInfo.getVideoId(), videoInfo.getVideoName(), true
                , payData.getPayOkCount(videoInfo.getSpId(), videoInfo.getUserId(), videoInfo.getVideoId())
                , payData.getPayCancelCount(videoInfo.getSpId(), videoInfo.getUserId(), videoInfo.getVideoId()));
        nodeLink.set(videoInfo.getUserId(), videoInfo.getSpId(), videoInfo.getClientCatalog());
        return nodeLink;
    }

    private void store(Map<String, NodeLink> pathRelMap, NodeLink node) {
        NodeLink root = node.getRoot();
        String key = root.getPath();
        NodeLink cached = pathRelMap.get(key);
        if (cached == null) {
            pathRelMap.put(key, root);
        } else {
            cached.merge(root);
        }
    }
}
