package com.broadtech.hbase;

import com.broadtech.bdp.common.util.Logger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * create by 2018/1/3 15:57<br>
 * idx_MLTE_S1ULOG_20180103 => CI: 218392331 endTime:1514910200757  0000004d0d64323138333932333331d3035297b0
 *
 * @author Yuanjun Chen
 */
public class HbaseShell {
    private static final Logger logger = Logger.getLogger(HbaseShell.class);

    public static void main(String[] args) {
        String entityTableName = "XDATA_78_2018-01-03"; // XDATA_77_2018-01-03
        String idxTableName = "idx_MLTE_S1ULOG_20180103"; // idx_MLTE_S1MMELOG_20180103

        try {
            fun(entityTableName, idxTableName);
            //translate("218392331", 1514910200757L);
        } catch (Throwable e) {
            logger.error(e.toString(), e);
        }

        logger.info("Game Over!");
    }

    private static void translate(String ci, long time) throws DecoderException {
        byte[] tg = Analyse.getTimeTimeGranularity((int) (time / 1000), 5);
        byte[] field = Bytes.toBytes(ci);
        byte[] head = Analyse.buildRowKeyHash(Bytes.add(tg, field));
        byte[] startRow = Bytes.add(head, tg, field);
        byte[] endRow = Bytes.add(head, tg, Bytes.toBytes(String.valueOf(Long.valueOf(ci) + 1)));

        byte[] b1 = Hex.decodeHex("0000004d0d64323138333932333331d3035297b0".toCharArray());

        System.out.println("0000004d0d64323138333932333331d3035297b0"
                + "\n" + Hex.encodeHexString(startRow)
                + "\n" + Hex.encodeHexString(endRow)
                + "\nb1 compare endRow is " + Bytes.compareTo(b1, endRow));
    }

    private static void fun(String entityTableName, String idxTableName) {
        logger.info("Hbase index 查询启动");
        Configuration hbaseConfig = HBaseConfiguration.create();
        hbaseConfig.set("hbase.client.max.total.tasks", "500");
        hbaseConfig.set("hbase.client.max.perserver.tasks", "6");
        try (HConnection connection = HConnectionManager.createConnection(hbaseConfig)) {
            logger.info("connection 已建立");
            HTableInterface indexHTable = connection.getTable(idxTableName);
            logger.info("获取表: " + idxTableName);
            HTableInterface mlteS1u = connection.getTable(entityTableName);

            //sample(indexHTable, mlteS1u)
            //count(indexHTable, mlteS1u)
            //query(indexHTable, mlteS1u, "219234572", 1514901600127L);
            //query(indexHTable, mlteS1u, "218392331", 1514910200757L);
            query(indexHTable, mlteS1u, "218290443", 1514910214853L);

            indexHTable.close();
            mlteS1u.close();
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    private static void query(HTableInterface indexHTable, HTableInterface mlteS1u, String ci, long time) throws IOException {
        byte[] tg = Analyse.getTimeTimeGranularity((int) (time / 1000), 5);
        byte[] field = Bytes.toBytes(ci);
        byte[] head = Analyse.buildRowKeyHash(Bytes.add(tg, field));
        byte[] startRow = Bytes.add(head, tg, field);
        //byte[] endRow = Bytes.add(startRow, Bytes.toBytes("99999"));
        byte[] endRow = Bytes.add(head, tg, Bytes.toBytes(String.valueOf(Long.valueOf(ci) + 1)));

        Analyse analyse = new Analyse();
        Scan scan = new Scan();
        scan.setStartRow(startRow);
        scan.setStopRow(endRow);
        ResultScanner scanResult = indexHTable.getScanner(scan);
        Result result;
        int count = 1;
        while ((result = scanResult.next()) != null) {
            Cell cell = result.rawCells()[0];
            byte[] key = CellUtil.cloneRow(cell);
            byte[] value = CellUtil.cloneValue(cell);
            String str = analyse.indexRowKeyAnalyse(key, value, null);
            if (str != null) logger.info(count++ + "\t" + str);
        }
        scanResult.close();
    }

    private static void count(HTableInterface indexHTable, HTableInterface mlteS1u) throws IOException {
        long indexRowCount = 0;
        long indexValueCount = 0;
        logger.info("开始计算索引表的数据量： " + indexHTable.getName().getNameAsString());

        Result result;
        int internal = 1000000;

        Scan scan = new Scan();
        ResultScanner scanResult = indexHTable.getScanner(scan);
        while ((result = scanResult.next()) != null) {
            indexRowCount++;
            Cell cell = result.rawCells()[0];
            byte[] value = CellUtil.cloneValue(cell);
            indexValueCount += value.length / 22;
            if (indexRowCount % internal == 0) {
                printCount(indexRowCount, indexValueCount, 0);
            }
        }
        scanResult.close();

        // mlteS1u
        logger.info("开始打开实体表的Scanner： " + mlteS1u.getName().getNameAsString());
        long entityCount = 0;
        scan = new Scan();
        scanResult = indexHTable.getScanner(scan);
        logger.info("开始计算实体表的数据量： " + mlteS1u.getName().getNameAsString());
        while ((result = scanResult.next()) != null) {
            entityCount++;
            if (entityCount % internal == 0) {
                printCount(indexRowCount, indexValueCount, entityCount);
            }
        }
        scanResult.close();

        printCount(indexRowCount, indexValueCount, entityCount);
        logger.info("OK");
    }

    private static void printCount(long indexRowCount, long indexValueCount, long entityCount) {
        logger.info("IndexRowCount:" + indexRowCount + " IndexValueCount:" + indexValueCount + " entryCount:" + entityCount);
    }

    private static void sample(HTableInterface hTable, HTableInterface mlteS1u) throws IOException {
        Scan scan = new Scan();
//            scan.setStartRow(Bytes.toBytes((short) 0));
//            scan.setStartRow(Bytes.toBytes((short) 100));
        scan.setMaxResultSize(30);
        ResultScanner scanResult = hTable.getScanner(scan);
        logger.info("获取scanResult");
        Analyse analyse = new Analyse();
        for (int i = 0; i < 30; i++) {
            Result result = scanResult.next();
            if (result == null) break;
            Cell cell = result.rawCells()[0];
            byte[] entityRowKey = CellUtil.cloneValue(cell);
            String line = Hex.encodeHexString(CellUtil.cloneRow(cell)) + " => " + Hex.encodeHexString(entityRowKey);
            String analyseRs = analyse.indexRowKeyAnalyse(CellUtil.cloneRow(cell), entityRowKey);
            if (entityRowKey.length == 22) {
                Get get = new Get(entityRowKey);
                Result getRs = mlteS1u.get(get);
                line += " EntityValue: " + Hex.encodeHexString(CellUtil.cloneValue(getRs.rawCells()[0]));
            }
            logger.info(line + " >> " + analyseRs);
        }
        scanResult.close();
    }
}
