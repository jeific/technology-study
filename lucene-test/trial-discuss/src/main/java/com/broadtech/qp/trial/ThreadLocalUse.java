package com.broadtech.qp.trial;

/**
 * Created by jeifi on 2017/7/31.
 */
public class ThreadLocalUse {

    public static void main(String[] args) {
        final ThreadLocal<String> threadLocal = new ThreadLocal<>();
        final ThreadLocal<String> threadLocal2 = new ThreadLocal<>();
        Thread t1 = new Thread() {
            @Override
            public void run() {
                threadLocal.set("kkk");
                System.out.println(threadLocal.get());
                System.out.println(threadLocal2.get());
            }
        };
        t1.start();
    }
}
