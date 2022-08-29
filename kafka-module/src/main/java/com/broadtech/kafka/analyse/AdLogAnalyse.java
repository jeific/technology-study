package com.broadtech.kafka.analyse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdLogAnalyse {

    public static void main(String[] args) throws IOException, ParseException {
        String path = "G:\\data01\\data\\ad\\adrequest_xjcbc\\adrequest_2.csv";
        Pattern p = Pattern.compile("\"timestamp\"\\s*:\\s*(\\d{13})");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm");
        long baseLower = dateFormat.parse("20190618 0012").getTime();
        long baseUppwer = dateFormat.parse("20190618 0015").getTime();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    long time = Long.parseLong(m.group(1));
                    if (time >= baseLower && time < baseUppwer) {
                        System.out.println(line);
                    }
                    System.out.println(new Date(time));
                }
            }
        }
    }
}
