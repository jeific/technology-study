package com.broadtech.fun;

import com.broadtech.bdp.common.util.ThreadAssistant;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;


public class CaffeineTest {

    @Test
    public void test1() {
        Cache<String, String> cache = Caffeine.newBuilder()
                //.expireAfterWrite(10, TimeUnit.MINUTES)
                .executor(Runnable::run)
                .ticker(Ticker.systemTicker())
                .expireAfterAccess(5, TimeUnit.SECONDS)
                .maximumSize(5)
                .removalListener((String key, String value, RemovalCause cause) -> {
                    System.out.println(key + ":" + value + "\t" + cause);
                })
                .recordStats()
                .build();
        cache.put("1", "1");
        cache.put("2", "2");
        cache.put("3", "3");
        cache.put("21", "21");
        cache.put("31", "31");
        cache.getIfPresent("3"); // 3将被保留，因最近使用过
        cache.getIfPresent("1");
        System.out.println(cache.asMap().toString() + "\n" + cache.stats().toString()
                + "\nhitRate:" + cache.stats().hitRate());
        cache.put("4", "4");
        cache.put("4", "4");
        cache.getIfPresent("5");
        System.out.println(cache.asMap().toString() + "\n" + cache.stats().toString()
                + "\nhitRate:" + cache.stats().hitRate() + "\t" + cache.stats().requestCount());
        cache.invalidate("4");

        ThreadAssistant.sleep(10, TimeUnit.SECONDS);

        System.out.println("===========================================");
        System.out.println("invalidateAll()前: " + cache.estimatedSize());
        cache.invalidateAll();
        System.out.println("invalidateAll()后: " + cache.estimatedSize());

        try {
            File file = new File("test_1235.txt");
            OutputStream outs = new FileOutputStream(file);
            System.out.println("1. 初始" + file.getAbsolutePath() + "\t" + file.length());
            outs.write("System.out.println(file.getAbsolutePath() + \"\\t\" + file.length());".getBytes());
            System.out.println("2. 不重新打开File实例" + file.getAbsolutePath() + "\t" + file.length());
            file = new File("test_1235.txt");
            System.out.println("3. 重新打开File实例" + file.getAbsolutePath() + "\t" + file.length());
            file.delete();
            outs.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
