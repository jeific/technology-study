package com.snowlake.java.io;

import com.broadtech.common.jdbc.DatabaseProxy;
import com.broadtech.common.jdbc.QueryAdapter;
import com.broadtech.common.util.BlueWhaleEnv;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class URLTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        databaseProxy();
    }

    private static void databaseProxy() throws SQLException {
        BlueWhaleEnv.DatabaseCatalog catalog = BlueWhaleEnv.DatabaseCatalog.HIVE;
        String url = BlueWhaleEnv.getDatabaseConnectURL(catalog);
        System.out.println(url);
        DatabaseProxy proxy = DatabaseProxy.getProxy(BlueWhaleEnv.getDriverClass(catalog)
                , url
                , BlueWhaleEnv.getDatabaseConnectUser(catalog)
                , BlueWhaleEnv.getDatabaseConnectPassword(catalog));
        proxy.query(new QueryAdapter<String>() {
            @Override
            public String getSQL() {
                return "show databases";
            }

            @Override
            public List<String> query(ResultSet resultSet) {
                List<String> rs = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        System.out.println(resultSet.getString(1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return rs;
            }
        });
    }

    private static void prototypeVersion() throws ClassNotFoundException, SQLException {
        BlueWhaleEnv.DatabaseCatalog catalog = BlueWhaleEnv.DatabaseCatalog.HIVE;
        String url = BlueWhaleEnv.getDatabaseConnectURLAndUserPassword(catalog);
        String driver = BlueWhaleEnv.getDriverClass(catalog);
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url);
        // presto调用该接口，url不能带有user，password参数
//        Connection conn = DriverManager.getConnection(url, "hive", null);
        System.out.println("connected " + url);
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery("show databases");
        while (res.next()) {
            System.out.println(res.getString(1));
        }
        System.out.println(stmt.execute("desc ad.impression_log"));

        res = stmt.executeQuery("show create table ad.impression_log");
        while (res.next()) {
            System.out.println(res.getString(1));
        }
    }
}
