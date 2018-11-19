package com.snowlake.java;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

    public static void main(String[] args) {
        String name = "ad_total.201806281810.zip";
        Pattern p = Pattern.compile("\\w+\\.(\\d{12})\\.zip", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(name);
        if (m.find()) {
            System.out.println(m.group() + "\t" + m.group(1));
        } else {
            System.out.println("error");
        }
    }
}
