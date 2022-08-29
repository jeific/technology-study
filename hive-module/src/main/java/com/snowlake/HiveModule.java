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
        // jdbc:hive2://192.168.95.235:10000/default
        String url = "jdbc:hive2://master01:2181,master02:2181,slave01:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2_zk";
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        Class.forName("com.mysql.jdbc.Driver");
        url = "jdbc:mysql://slave01:3306";
        try (Connection conn = DriverManager.getConnection(url, "root", "starcor");
             Statement stmt = conn.createStatement();) {
//            stmt.execute(createTable(TABLE));
//            stmt.execute(createTable(TABLE_2));
//            stmt.execute("select * from ad.adflow_log where day=20200503 limit 1");

//            CountDownLatch cdl = new CountDownLatch(2);
//            exeOverride(10, cdl, conn.createStatement()); // 新建连接，否则遭遇SASL authentication not complete
//            exeQuery(10, cdl, conn.createStatement());
//            cdl.await();

//            stmt.execute(dropTable(TABLE));
//            stmt.execute(dropTable(TABLE_2));

            ResultSet res = conn.getMetaData().getColumns("ad", "%", "evs_movie", "%");
           // TABLE_CAT	TABLE_SCHEM	TABLE_NAME	COLUMN_NAME	DATA_TYPE	TYPE_NAME	COLUMN_SIZE	BUFFER_LENGTH	DECIMAL_DIGITS	NUM_PREC_RADIX
            // NULLABLE	REMARKS	COLUMN_DEF	SQL_DATA_TYPE	SQL_DATETIME_SUB	CHAR_OCTET_LENGTH	ORDINAL_POSITION
            // IS_NULLABLE	SCOPE_CATALOG	SCOPE_SCHEMA	SCOPE_TABLE	SOURCE_DATA_TYPE	IS_AUTOINCREMENT	IS_GENERATEDCOLUMN
            for(int i = 1;i<=res.getMetaData().getColumnCount();i++){
                System.out.print(res.getMetaData().getColumnName(i)+"\t");
            }
            System.out.println();
            while (res.next()) {
                for (int i = 1; i <= res.getMetaData().getColumnCount(); i++) {
                    System.out.print(res.getObject(i) + "\t");
                }
                System.out.println();
            }
            res.close();
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
