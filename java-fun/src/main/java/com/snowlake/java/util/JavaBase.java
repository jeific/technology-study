package com.snowlake.java.util;

public class JavaBase {
    protected long total = -1;

    public static void main(String[] args) {
        /**
         * 位移运算符： 左目运算符
         */
        System.out.println(8 << 1);   // 左移一位 === * 2
        System.out.println(8 >> 1);   // 右移一位 === /2
        System.out.println((byte) 255 >> 1);
        System.out.println((byte) 255 >>> 1); // 无符号右移，忽略符号位，空位都以0补齐; 无符号右移一位 === /2
        new JavaBase().m();
    }

    public void m() {
        for (int i = 1; i <= 12; i++) {
            int quarter = (i - 1) / 3 * 3 + 1;
            System.out.print(quarter + " "); // 1 4 7 10
        }
    }
}
