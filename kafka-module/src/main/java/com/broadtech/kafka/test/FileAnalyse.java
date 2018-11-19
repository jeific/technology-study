package com.broadtech.kafka.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;

public class FileAnalyse {

    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String file = "C:\\Users\\jeifi\\Desktop\\777.out";
        int count = 0;
        int nocount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(file)))
        ) {
            long start = df.parse("201806151625").getTime();
            long end = df.parse("201806160220").getTime();
            String line = null;
            while ((line = reader.readLine()) != null) {
                long time = parseTime(line);
                if (time >= start && time <= end) {
                    System.out.println(df.format(time));
                    count++;
                } else {
                    nocount++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(count + "\t" + nocount);
        }
    }

    private static long parseTime(String value) {
        int index = value.indexOf("server_time") + "server_time".length() + 2;
        return Long.parseLong(value.substring(index, index + 13));
    }
}
