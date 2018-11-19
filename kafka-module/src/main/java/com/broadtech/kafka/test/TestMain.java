package com.broadtech.kafka.test;

import com.broadtech.kafka.DataRangeRewrite;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMain {

    public static void main(String[] args) throws IOException {
        String json = "{\"firstname\":\"John\",\"lastname\":\"Doe\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> obj = objectMapper.readValue(json, Map.class);
        System.out.println(obj);


        Properties conf = new Properties();
        try (InputStream ins = DataRangeRewrite.class.getResourceAsStream("/conf.properties")) {
            conf.load(ins);
        }
        json = "{\"duration\":\"0\",\"end_play_pos\":\"0\",\"event_status\":\"\",\"event_time\":\"1530254236020\",\"event_type\":\"finish\",\"ext_info\":{\"special_name\":\"\",\"p_id\":\"\",\"current_position\":0,\"source_page\":\"page_Home\",\"special_id\":\"\",\"p_name\":\"\",\"recommend_code\":\"\"},\"report_time\":\"1530254236020\",\"start_play_pos\":\"0\",\"asset_id\":\"kznkVIP\",\"category_id\":\"1000003\",\"episode_id\":\"5b2b2fb1c65953b97a74f24ece0a0b16\",\"media_id\":\"5b2b30b22e6eb2a26cf62344a0270573\",\"page_id\":\"page_VideoPlay\",\"page_sid\":\"8412e73923e549ed9fdf349e33af050b\",\"play_sid\":\"8362286bb8e74f4fbe7a1e2d4460011a\",\"playbill_length\":\"0\",\"playbill_name\":\"\",\"playbill_start_time\":\"0\",\"video_id\":\"5a811b78247643e246bb8d61faaee0b9\",\"video_name\":\"ناروتو\",\"video_type\":\"vod\",\"apk_version\":\"4.3.1.android_Koznak_release\",\"client_type\":\"phone\",\"device_id\":\"a04b0675b024b6ed\",\"mac\":\"a04b0675b024b6ed\",\"network_type\":\"3g\",\"platform_id\":\"xjcbc\",\"region_code\":\"0\",\"sdk_version\":\"v2.5.2\",\"sp_id\":\"koznak\",\"system_name\":\"Android\",\"system_version\":\"vivo/PD1522A/PD1522A:5.1.1/LMY47V/compiler04021808:user/release-keys\",\"user_id\":\"052987da7869860a0f36e0e36f49aa65\"}\u0001{\"server_time\":1530254236803,\"ip\":\"43.242.153.138\"}";
        // "\"server_time\"\\s*:\\s*(\\d{13})"
        Pattern pattern = Pattern.compile(conf.getProperty("time.regexp"), Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(json);
        System.out.println(m.find() + "\t" + m.group(1));
    }
}
