package com.snowlake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class HiveModule {
    private static final Logger logger = LoggerFactory.getLogger(HiveModule.class);
    private static final String TABLE = "hive_perform_test_20180512";
    private static final String TABLE_2 = "hive_perform_test_20180512_2";

    public static void main(String[] args) throws ClassNotFoundException {
        String url = "jdbc:hive2://192.168.95.237:10000/default";
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        try (Connection conn = DriverManager.getConnection(url, "root", "");
             Statement stmt = conn.createStatement();) {
            stmt.execute(createTable(TABLE));
            stmt.execute(createTable(TABLE_2));

            CountDownLatch cdl = new CountDownLatch(2);
            exeOverride(10, cdl, conn.createStatement()); // 新建连接，否则遭遇SASL authentication not complete
            exeQuery(10, cdl, conn.createStatement());
            cdl.await();

            stmt.execute(dropTable(TABLE));
            stmt.execute(dropTable(TABLE_2));
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }

    private static void exeOverride(int num, CountDownLatch cdl, Statement stmt) {
        String overrideSQL = "INSERT OVERWRITE TABLE " + TABLE_2 + " select * from " + TABLE;
        //String overrideSQL = "set hive.support.concurrency=false;INSERT OVERWRITE TABLE " + TABLE_2 + " select * from " + TABLE;
        exe(statement1 -> {
            try {
                long time = System.currentTimeMillis();
                boolean override = statement1.execute(overrideSQL);
                logger.info("insert override: {} cost:{} SQL: {}", override, (System.currentTimeMillis() - time), overrideSQL);
            } catch (SQLException e) {
                logger.error(e.toString(), e);
            }
        }, num, cdl, stmt);
    }

    private static void exeQuery(int num, CountDownLatch cdl, Statement stmt) {
        String querySQL = "select count(1) from " + TABLE_2;
        exe(statement1 -> {
            long time = System.currentTimeMillis();
            try (ResultSet rs = statement1.executeQuery(querySQL)) {
                while (rs.next()) {
                    logger.info("query: {} cost:{} SQL: {}", rs.getInt(1), (System.currentTimeMillis() - time), querySQL);
                }
            } catch (SQLException e) {
                logger.error(e.toString(), e);
            }
        }, num, cdl, stmt);
    }

    private static void exe(Consumer<Statement> consumer, int num, CountDownLatch cdl, Statement stmt) {
        new Thread(() -> {
            try {
                for (int i = 0; i < num; i++) {
                    consumer.accept(stmt);
                }
            } finally {
                cdl.countDown();
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error(e.toString(), e);
                }
            }
        }).start();
    }

    private static void generateData() {
        try (OutputStream out = new FileOutputStream("data.csv")) {
            for (int i = 0; i < 110; i++) {
                out.write(("name_" + i).getBytes());
                out.write(("" + i).getBytes());
                out.write(("like_" + i).getBytes());
                out.write("\n".getBytes());
            }
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    private static String createTable(String table) {
        return "create external TABLE if not exists " + table + " (`name` string,`age` int, `like` string)" +
                " ROW FORMAT DELIMITED FIELDS TERMINATED BY ','" +
                " STORED AS TextFile";
    }

    private static String dropTable(String table) {
        return "drop TABLE  if exists " + table;
    }
}
