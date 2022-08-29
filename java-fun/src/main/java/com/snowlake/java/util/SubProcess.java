package com.snowlake.java.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubProcess {
    private static final Logger logger = LoggerFactory.getLogger(SubProcess.class);

    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            logger.info("{} segmentId:f257481b-134a-6fff-8c7e-b8d8fc684929 segmentName:20190813000000_20190813010000 HDFS名字: kylin-b964bf2a-737f-f48c-928d-6d8ea04c660b,在Hbase中的表名: KYLIN_H5H4V5FXU2; 过滤截至时间:20191013", i);
        }
        logger.info("============= END ==========");
    }
}
