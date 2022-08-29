package com.snowlake.java.util;

import com.broadtech.common.util.LoggerUtil;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.FileInputStream;
import java.io.InputStream;

public class ExceptionTest {

    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            test();
        }
    }

    private static void test() {
        try (InputStream ins = new FileInputStream("/kkkk.test")) {
        } catch (Exception e) {
            LoggerUtil.getLogger(ExceptionTest.class)
                    .error("测试错误堆栈格式化\n" + ExceptionUtils.getFullStackTrace(e));
        }
    }
}
