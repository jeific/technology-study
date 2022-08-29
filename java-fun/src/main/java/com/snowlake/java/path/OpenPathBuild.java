package com.snowlake.java.path;

import java.util.List;
import java.util.Map;

public class OpenPathBuild {
    /**
     * 构建算法：连续的路径构建，一旦不能连续，则是新的路径开始
     *
     * @param sessionOpenPathRecords 同一个session按时间连续的记录
     * @param pathRelMap             以构建的路径组合集合
     */
    public void buildPath(List<Record> sessionOpenPathRecords, Map<String, PathRel> pathRelMap) {
        PathRel pathRel = null;
        for (Record record : sessionOpenPathRecords) {
            String source = record.getSource();
            String target = record.getTarget();
            if (pathRel == null) {
                pathRel = getNewPath(record, source, target);
                continue;
            }
            if (pathRel.isBackward(source, target)) {
                pathRel = pathRel.getBackPathRel().incrementBackwardCount();
            } else if (pathRel.isCycle(source, target)) {
                pathRel = pathRel.setCycle(target).incrementCount();
            } else if (pathRel.isForward(source, target)) {
                pathRel = pathRel.setNext(target).incrementCount();
            } else {
                store(pathRelMap, pathRel);
                pathRel = getNewPath(record, source, target); // 斩断路径，重新计算
            }
        }
        if (pathRel != null) store(pathRelMap, pathRel);
    }

    private PathRel getNewPath(Record record, String source, String target) {
        return new PathRel(source, target)
                .set(record.getUserId(), record.getSpId(), record.getClientCatalog())
                .incrementCount();
    }

    private void store(Map<String, PathRel> pathRelMap, PathRel pathRel) {
        PathRel root = pathRel.getRoot();
        String key = root.getPath();
        PathRel cached = pathRelMap.get(key);
        if (cached == null) {
            pathRelMap.put(key, root);
        } else {
            cached.merge(root);
        }
    }
}
