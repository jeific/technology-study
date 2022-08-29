package com.snowlake.java.test;

import sun.misc.Signal;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class NohupTest implements sun.misc.SignalHandler {
    public static void main(String[] args) throws InterruptedException {
        //Signal.handle(null);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("====hook1====")));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("====hook2_1====");
                TimeUnit.SECONDS.sleep(2);
                System.out.println("====hook2_2====");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        System.out.println("ARGS:" + Arrays.toString(args));
        int i = 0;
        while (i < 60 * 10) {
            i++;
            System.out.println(">>>" + i);
            System.err.println("====" + i);
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Override
    public void handle(Signal signal) {
        System.out.println("Signal:" + signal.getName() + "\t" + signal.getNumber());
    }
}
