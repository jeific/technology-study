package com.broadtech.qp.other;

import org.apache.solr.client.solrj.SolrClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by jeifi on 2017/8/9.
 */
public class Algorithm {
    public static void main(String[] args) throws Exception {
        SolrClient solrClient;

        timeValid();
    }

    private static void timeValid() {
        long time = System.currentTimeMillis();
        int baseSeconds = (int) (time / 1000);
        Map<Integer, Set<Integer>> rs = new HashMap<>();
        int span = 72 * 60; // 72小时
        for (int i = 0; i < span * 10; i++) {
            int currTime = (int) ((time + i * 6 * 1000) / 1000);
            int key = currTime / (span * 60);
            if (!rs.containsKey(key)) {
                rs.put(key, new TreeSet<>());
            }
            rs.get(key).add(i);
        }
        rs.forEach((k, v) -> System.out.println(k + ":" + v));
    }

    private static int getTime(DateFormat format, String time) throws ParseException {
        return (int) (format.parse(time).getTime() / 1000);
    }
}
