package com.broadtech.util;

import com.broadtech.common.util.CommonUtil;
import com.broadtech.common.util.ThreadAssistant;

import java.util.concurrent.TimeUnit;

public class GranularityMin {

    public static void main(String[] args) {
        int granularity = 5;
        for (int j = 0; j < granularity; j++) {
            String date = CommonUtil.convertTimeGranularity(System.currentTimeMillis(), granularity);
            System.out.println(date);
            ThreadAssistant.sleep(5, TimeUnit.SECONDS);
        }

        double maxStandardError = 0.023;
        int buckets = log2Ceiling((int) Math.ceil(1.0816 / (maxStandardError * maxStandardError)));
        System.out.println(buckets); // 2048
    }

    private static int log2Ceiling(int value) {
        return Integer.highestOneBit(value - 1) << 1;
    }
}
