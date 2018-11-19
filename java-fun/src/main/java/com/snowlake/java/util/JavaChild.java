package com.snowlake.java.util;

public class JavaChild extends JavaBase {

    public static void main(String[] args) {
        JavaChild javaChild = new JavaChild();
        javaChild.m1();
        javaChild.m2();

        JavaBase javaBase = new JavaChild();
        javaBase.m();
    }

    public void m1(){
        this.total = 10;
    }

    public void m2(){
        System.out.println(this.total);
    }

    @Override
    public void m() {
        m1();
        m2();
    }
}
