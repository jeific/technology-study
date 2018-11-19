package com.broadtech.kafka.test;

public class ParseMain {

    public static void main(String[] args) {
        String value = "{\"play_sid\":\"\",\"client_type\":\"stb\",\"asset_id\":\"\",\"mac\":\"\",\"page_id\":\"\",\"episode_id\":\"\",\"category_id\":\"\",\"system_version\":\"guizhou_tongzhou1.0\",\"media_id\":\"\",\"playbill_length\":\"\",\"apk_version\":\"guizhou_tongzhou1.0\",\"event_status\":\"\",\"playbill_start_time\":\"\",\"page_sid\":\"ea14d7d5-d7c7-4898-963b-3d41def2c929\",\"device_id\":\"0701130015050909668\",\"system_name\":\"Android\",\"video_type\":\"live\",\"sp_id\":\"gzgd\",\"user_id\":\"0701130015050909668\",\"network_type\":\"cable\",\"heartbeat_type\":\"live\",\"event_time\":1529092612716,\"region_code\":\"\"}{\"ip\":\"10.195.223.45\",\"server_time\":1529092612716}";
        int index = value.indexOf("server_time") + "server_time".length() + 2;
        long serverTime = Long.parseLong(value.substring(index, index + 13));
        System.out.println(serverTime);
    }
}
