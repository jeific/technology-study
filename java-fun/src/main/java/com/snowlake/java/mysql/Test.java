package com.snowlake.java.mysql;

import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.airlift.stats.cardinality.HyperLogLog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        String jdbc = "jdbc:mysql://192.168.90.96:3306/bluewhale_smart?user=root&password=Bluewhale@1234&useSSL=false";
        try (Connection con = DriverManager.getConnection(jdbc);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select * from bluewhale_smart.enterprise")) {
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }
}
