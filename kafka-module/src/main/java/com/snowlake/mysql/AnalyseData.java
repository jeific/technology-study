package com.snowlake.mysql;

import java.io.*;
import java.util.*;

/**
 * 证明分组交叉
 */
public class AnalyseData {

    public static void main(String[] args) throws IOException {
        String data = "D:\\测试\\贵州\\data1.txt\\data1.txt";
        Map<String, Integer> schema = schemaWrap(schema());
        Map<String, Model> statistic = new HashMap<>();
        Map<String, HashSet<String>> groupStatistic = new HashMap<>();
        String[] groupByField = "svr_id,play_type,service_type,protocol_type,dtype,user_domain".split(",");
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(data))) {
            String row;
            String group;
            while ((row = reader.readLine()) != null) {
                count++;
                String[] fieldValues = row.split("\t");
                String session = fieldValues[0];
                group = groupBy(schema, groupByField, fieldValues);
                statistic.computeIfAbsent(session, k -> new Model(session)).add(row, group);
            }
        }
        String resultPath = "D:\\测试\\贵州\\data1.txt\\analyse_result_summary.txt";
        String resultPath2 = "D:\\测试\\贵州\\data1.txt\\analyse_result.txt";
        try (OutputStream out = new FileOutputStream(resultPath);
             OutputStream out2 = new FileOutputStream(resultPath2)) {
            out.write(("session:" + statistic.size() + "\n").getBytes());
            out.write(("total count record:" + count + "\n").getBytes());
            out2.write(("session:" + statistic.size() + "\n").getBytes());
            out2.write(("total count record:" + count + "\n").getBytes());
            Collection<Model> values = statistic.values();
            List<Model> models = new ArrayList<>(values);
            Collections.sort(models);
            for (Model model : models) {
                out.write(model.getSummary().getBytes());
                out2.write(model.toString().getBytes());
            }
        }
    }

    private static class Model implements Comparable<Model> {
        String session;
        int count = 0;
        Map<String, String> groupBy = new HashMap<>();

        Model(String session) {
            this.session = session;
        }

        void add(String row, String group) {
            count++;
            if (groupBy.size() < 5) {
                groupBy.put(group, row);
            }
        }

        @Override
        public int compareTo(Model o) {
            return -Integer.compare(count, o.count); // 从大到小
        }

        String getSummary() {
            return session + ":" + count + "\t" + groupBy.size() + "\n";
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(session + ":" + count);
            for (Map.Entry<String, String> entry : groupBy.entrySet()) {
                builder.append("\n\t\t").append(entry.getKey()).append(" : ").append(entry.getValue());
            }
            builder.append("\n");
            return builder.toString();
        }
    }

    private static String groupBy(Map<String, Integer> schema, String[] groupByField, String[] row) {
        StringBuilder builder = new StringBuilder();
        for (String field : groupByField) {
            String filed = row[schema.get(field)];
            builder.append(filed).append("|");
        }
        builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }

    private static Map<String, Integer> schemaWrap(String[] schema) {
        Map<String, Integer> wrapSchema = new HashMap<>();
        for (int i = 0; i < schema.length; i++) {
            wrapSchema.put(schema[i], i);
        }
        return wrapSchema;
    }

    private static String[] schema() {
        String columns = "session\n" +
                "request_session\n" +
                "server_time\n" +
                "platform_id\n" +
                "cmd\n" +
                "svr_id\n" +
                "svr_ip\n" +
                "uip\n" +
                "uid\n" +
                "uagent\n" +
                "did\n" +
                "dtype\n" +
                "dversion\n" +
                "id\n" +
                "cp\n" +
                "sp\n" +
                "user_arg\n" +
                "protocol_type\n" +
                "service_type\n" +
                "play_type\n" +
                "user_domain\n" +
                "service_domain\n" +
                "play_time_len\n" +
                "begin_time\n" +
                "response_time\n" +
                "send_time_len\n" +
                "send_byte\n" +
                "send_speed\n" +
                "read_wait_time\n" +
                "send_wait_time\n" +
                "url\n" +
                "code\n" +
                "reason\n" +
                "first_play_time\n" +
                "first_response_time\n" +
                "first_request_session\n" +
                "data_type\n" +
                "finish_time\n" +
                "first_finish_time\n" +
                "first_ts_play_time\n" +
                "first_ts_response_time\n" +
                "first_ts_finish_time\n" +
                "first_ts_request_session\n" +
                "slice_id\n" +
                "slice_time_len\n" +
                "slice_filesize\n" +
                "slice_bandwidth\n" +
                "pre_m3u8_time\n" +
                "pre_slice_ts_time\n" +
                "pre_slice_ts_time_len\n" +
                "pre_slice_bandwidth\n" +
                "describe_begin_time\n" +
                "describe_response_time\n" +
                "setup_begin_time\n" +
                "setup_response_time\n" +
                "qam_code\n" +
                "qam_frequence\n" +
                "qam_ip\n" +
                "send_detail_count\n" +
                "send_detail\n" +
                "total_content_time_len\n" +
                "cur_content_time_len\n" +
                "playbill_start_time\n" +
                "playbill_length\n" +
                "day\n" +
                "minute";
        return columns.split("\n");
    }
}
