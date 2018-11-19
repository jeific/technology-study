package com.snowlake.java;

import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.airlift.stats.cardinality.HyperLogLog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class HyperLLTestFun {

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        String jdbc = "jdbc:mysql://192.168.90.44:3306/ad?user=root&password=starcor";
        try (Connection con = DriverManager.getConnection(jdbc);
             Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("select exposure_uv from ad.exposure_click_bidding_10minute " +
                    "where day=20180815 and minute>='1720' and minute<'1730'");
            int count = 1;
            while (rs.next()) {
                count++;
                byte[] b = rs.getBytes(1);
                if (b == null) continue;
                try {
                    System.out.println(count + "\t" + Arrays.toString(b));
                    Slice value = Slices.wrappedBuffer(b);
                    HyperLogLog.newInstance(value);
                } catch (Exception e) {
                    System.out.println(count + " input is too big");
                }
            }
            rs.close();
            stmt.close();
        }
    }
}
