package com.broadtech.kafka.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class H {

    public static void main(String[] args) throws IOException {
        String json = "{\"aid\":\"54f02ea6be8a4a7aae5487630e86673c\",\"app\":{\"id\":\"69712365a1e645a592ac6974d1a7c2ce\",\"cat\":[\"10000013\"],\"keywords\":\"\",\"ver\":\"6.0.4.android_Koznak_release\",\"bundle\":\"cn.wlantv.kznk\",\"publisher\":{\"domain\":\"\",\"name\":\"\"},\"content\":{\"id\":\"597ec6ec6a4ac151de22a4bcb04399d0\",\"context\":7,\"series\":\"\",\"season\":\"\",\"episode\":10,\"title\":\"پىنو كىئو\",\"keywords\":\"\",\"liveStream\":0,\"len\":0,\"ext\":{\"category\":\"10020024\",\"columnid\":\"1000\",\"ispay\":false,\"ispreview\":0}}},\"device\":{\"ua\":\"Dalvik/2.1.0 (Linux; U; Android 8.1.0; M1822 Build/OPM1.171019.026)\",\"ip\":\"36.108.42.23\",\"geo\":{\"type\":3,\"lat\":0,\"lon\":0,\"country\":\"\",\"prov\":\"\",\"city\":\"\",\"region\":\"100110300011\"},\"did\":\"86806703078232\",\"mac\":\"获取MAC地址失败！\",\"carrier\":0,\"devicetype\":4,\"make\":\"Meizu\",\"model\":\"M1822\",\"os\":5,\"osv\":\"8.1.0\",\"w\":1080,\"h\":2064,\"connectiontype\":6},\"errorcode\":104002,\"errordesc\":\"[104002]flow [9ab245f1d97c8866f4cce46a34e26d5a] is giveup\",\"ip\":\"36.108.42.23\",\"mediaid\":\"69712365a1e645a592ac6974d1a7c2ce\",\"server_time\":1560228801.969,\"sessionid\":\"a888b15d038440a284479c45338b6c08\",\"timestamp\":1560228801969,\"user\":{\"id\":\"2aa038fb56a4cbf7\",\"yob\":\"\",\"gender\":\"\",\"keywords\":\"\",\"ext\":{}}}";
        Properties pro = new Properties();
        try (InputStream in = Class.class.getResourceAsStream("/conf.properties")) {
            pro.load(in);
        }
        String regexp = pro.getProperty("time.regexp");
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(json);
        System.out.println(m.find());
        System.out.println(regexp + "\t" + m.group(1));

        long[][] ranges = {new long[]{1560765600000L, 1560768000000L}};
        System.out.println(getMaxTime(ranges));
    }


    private static long getMaxTime(long[][] timeRanges) {
        long maxTime = 0;
        for (long[] t : timeRanges) {
            maxTime = Math.max(t[1], maxTime);
        }
        StringBuilder builder = new StringBuilder();
        Arrays.stream(timeRanges).forEach(i -> builder.append(Arrays.toString(i)));
        System.out.println(String.format("MaxTime:%d, Ranges:%s", maxTime, builder.toString()));
        return maxTime;
    }
}
