package com.broadtech.mysql;

import java.sql.*;

public class MysqlTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.95.65", "root", "starcor");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("show databases")) {
            while (rs.next()) {
                System.out.println(rs.getObject(1));
            }
        }
    }
}
