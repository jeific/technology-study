package com.snowlake.java.sqlserver;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        System.out.println(ZoneId.systemDefault().getId());
        System.out.println(ZoneId.of("GMT+8"));
        System.out.println(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.of("GMT+8")));
        try (Connection connection = conn()) {
//            insert(connection);
            query(connection);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Connection conn() throws SQLException, ClassNotFoundException {
        String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";//SQL数据库引擎
        String dbURL = "jdbc:sqlserver://localhost:1533";//DatabaseName=TutorialDB";//数据源  ！！！注意若出现加载或者连接数据库失败一般是这里出现问题
        String Name = "test"; // "root";
        String Pwd = "654321"; //"Bluewhale@1234";
        try {
            Class.forName(driverName);
            Connection connection = DriverManager.getConnection(dbURL, Name, Pwd);
            System.out.println("连接数据库成功");
            return connection;
        } catch (Exception e) {
            System.out.println("连接失败");
            throw e;
        }
    }

    private static void insert(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("insert into dbo.Customers values(1,'佩奇','chengdu','jie@163.com')");
            System.out.println("写入成功");
        }
    }

    private static void query(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery("select * from sys.databases")) {
            System.out.println("查询成功");
            List<String> databases = new ArrayList<>();
            extracted(res, databases, "name");
            System.out.println(databases.size() + "\t" + databases);

            System.out.println("=====sys.schemas");
            try (ResultSet res2 = stmt.executeQuery("select * from sys.schemas")) {
                List<String> listNames = new ArrayList<>();
                extracted(res2, listNames, "name");
                System.out.println(listNames.size() + "\t" + listNames);
            }
            System.out.println("=====sys.objects");
            try (ResultSet res2 = stmt.executeQuery("select * from sys.objects")) {
                List<String> objectNames = new ArrayList<>();
                extracted(res2, objectNames, "name");
                System.out.println(objectNames.size() + "\t" + objectNames);
            }
            System.out.println("=====sys.tables");
            try (ResultSet res2 = stmt.executeQuery("select * from sys.tables")) {
                List<String> listNames = new ArrayList<>();
                extracted(res2, listNames, "name");
                System.out.println(listNames.size() + "\t" + listNames);
            }
            System.out.println("=====sys.columns");
            try (ResultSet res2 = stmt.executeQuery("select * from sys.columns")) {
                List<String> listNames = new ArrayList<>();
                extracted(res2, listNames, "name");
                System.out.println(listNames.size() + "\t");
            }
            System.out.println("=====information_schema.tables");
            try (ResultSet res2 = stmt.executeQuery("select * from information_schema.tables")) {
                List<String> listNames = new ArrayList<>();
                extracted(res2, listNames, "table_name");
                System.out.println(listNames.size() + "\t" + listNames);
            }
            System.out.println("=====information_schema.columns");
            try (ResultSet res2 = stmt.executeQuery("select * from information_schema.columns")) {
                List<String> listNames = new ArrayList<>();
                extracted(res2, listNames, "COLUMN_NAME");
                System.out.println(listNames.size() + "\t");
            }
            try (ResultSet res2 = stmt.executeQuery("select count(1) from dbo.actionsetting")) {
                if (res2.next())
                    System.out.println("ecology.actionsetting count " + res2.getLong(1));
            }
            System.out.println("=====ecology.dbo.actionsetting"); // `ecology`.`dbo`.`actionsetting`这种格式在SQLServer中不支持
            try (ResultSet res2 = stmt.executeQuery("select * from ecology.dbo.formtable_main_192_dt4")) {
                List<String> listNames = new ArrayList<>();
                extracted(res2, listNames, "id", true);
                System.out.println(listNames.size() + "\t" + listNames);
            }

//            System.out.println("=====zwscmx"); // `ecology`.`dbo`.`actionsetting`这种格式在SQLServer中不支持
//            try (ResultSet res2 = stmt.executeQuery("select * from zwscmx")) {
//                List<String> listNames = new ArrayList<>();
//                extracted(res2, listNames, "id", false);
//                System.out.println(listNames.size() + "\t" + listNames);
//            }
        }
    }

    private static void extracted(ResultSet res, List<String> list, String nameColumn, boolean... allPrint) throws SQLException {
        int c = 0;
        List<String> tablList = Arrays.asList("zwscmx", "zgaunyifahuo", "zguanyituihuo");
        ResultSetMetaData meta = res.getMetaData();
        while (res.next()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                builder.append(meta.getColumnName(i))
                        .append(":").append(res.getObject(i))
                        .append("\t");
            }
            if (c++ == 0 || allPrint.length > 0 && allPrint[0] || tablList.contains(res.getString(nameColumn)))
                System.out.println(builder);
            list.add(res.getString(nameColumn));
        }
    }

    public static void update(Connection connection) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //增删改操作
        String sql_1 = "insert into student values('990403','佩奇','男','12','1','2101')";
        int count_1 = 0;
        try {
            count_1 = stmt.executeUpdate(sql_1);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }//返回值表示增删改几条数据
        //处理结果
        if (count_1 > 0) {
            System.out.println("更新成功!");
        }
        //查询操作
        String sql_2 = "select * from student";

        //关闭
        try {
            stmt.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}