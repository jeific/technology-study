package com.snowlake.java.hdfs;

public class AM {

    public static void main(String[] args) {
        A a1 = new A1();
        String s = a1.abs(String.class);
        int n = a1.abs(Integer.TYPE);
        System.out.println(n);
        System.out.println(s);
    }
}
