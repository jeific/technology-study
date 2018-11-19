package com.snowlake.java.io;

public class HLL {

    public static void main(String[] args) {
        double maxStandardError = 0.023;
        System.out.println(standardErrorToBuckets(maxStandardError));
    }

    static int standardErrorToBuckets(double maxStandardError) {
        return log2Ceiling((int) Math.ceil(1.0816 / (maxStandardError * maxStandardError)));
    }

    private static int log2Ceiling(int value) {
        return Integer.highestOneBit(value - 1) << 1;
    }
}
