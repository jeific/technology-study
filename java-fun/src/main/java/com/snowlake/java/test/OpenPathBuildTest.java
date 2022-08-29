package com.snowlake.java.test;

import com.snowlake.java.path.OpenPathBuild;
import com.snowlake.java.path.PathRel;
import com.snowlake.java.path.Record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class OpenPathBuildTest {

    public static void main(String[] args) throws IOException {
        String path = "C:\\Users\\jeifi\\Desktop\\kbdh.out";
        Map<String, PathRel> pathRelMap = new HashMap<>();
        OpenPathBuild discover = new OpenPathBuild();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
            String sessionId = "";
            List<Record> group = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> record = Arrays.asList(line.split("\\s+"));
                String sid = record.get(0);
                if (!sessionId.equals(sid) && !group.isEmpty()) {
                    sessionId = sid;
                    discover.buildPath(group, pathRelMap);
                    group.clear();
                }
                group.add(Record.of(sessionId, record.get(4), record.get(1), record.get(2), ""));
            }
            if (!group.isEmpty())
                discover.buildPath(group, pathRelMap);
        }
        List<PathRel> list = new ArrayList<>(pathRelMap.values());
        list.sort(Comparator.comparingInt(PathRel::total));
        System.out.println("pv\tpath");
        list.forEach(v -> {
            List<String> nodes = v.getNodes();
            Set<String> set = new HashSet<>(nodes);
            if (set.size() < nodes.size()) {
                System.out.println(v.toString() + "\t\t" + v.getPath());
            }
        });
//        list.forEach(v -> {
//            v.forEach(n -> System.out.println(n.getSource() + "-" + n.getForwardCount() + ":" + n.getBackwardCount() + "->" + n.getTarget()));
//            System.out.println(v.total() + "\t" + v.toString());
//            System.out.println(v.getNodes());
//            System.out.println(v.getForwardCounts());
//            System.out.println(v.getBackwardCounts());
//        });
//        System.out.println("path count:" + pathRelMap.size());
//        System.out.println("total count:" + pathRelMap.values().stream().flatMapToInt(v -> IntStream.of(v.total())).sum());
//        System.out.println("PATH:" + list.get(0).getPath());
    }
}
