package com.broadtech.qp;

import com.broadtech.bdp.common.util.UnitHelper;
import com.broadtech.qp.index.ResourcesUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 2017/7/11.
 */
public class CommonTest {

    public static void main(String[] args) throws Exception {
        Path resourceDir = Paths.get("");
//        while (true) {
//            Stream ds = Files.list(resourceDir);
//            ds.close();  // stream不关闭将导致内存泄漏 [打开太多的file]
//        }
        long count = ResourcesUtil.getPathSize(resourceDir);
        System.out.println(resourceDir.toAbsolutePath().toString() + " => " + count + " " + UnitHelper.getHumanSize(count));
    }


    @Test
    public void funTest() {
        byte[] hexStr = ResourcesUtil.transform("0x0a0d");
        System.out.println(Arrays.toString(hexStr) + new String(hexStr));
        hexStr = ResourcesUtil.transform("0x7c");
        System.out.println(Arrays.toString(hexStr) + new String(hexStr));
        hexStr = ResourcesUtil.transform("0x2c");
        System.out.println(Arrays.toString(hexStr) + new String(hexStr));

        Path p = Paths.get("123.txt");
        System.out.println(p.getFileName().toString());
        short s = 1;
        int i = (int) s;
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(9);
        }};
        list.forEach(System.out::println);
        System.out.println("--------------------------");
        list.forEach((k) -> {
            k += 4;
            System.out.println(k);
        });
        System.out.println("--------------------------");

        // java.lang.NumberFormatException: For input string: ""
        //  System.out.println("new byte[0] parse to int => " + Integer.parseInt(new String(new byte[0])));

        long[] arrayOfLong = new long[200];
        Arrays.parallelSetAll(arrayOfLong, index -> ThreadLocalRandom.current().nextInt(1000000));
        Arrays.stream(arrayOfLong).limit(10).forEach(e -> System.out.print(e + " "));
        Arrays.parallelSort(arrayOfLong);
        System.out.println();
        Arrays.stream(arrayOfLong).limit(10).forEach(e -> System.out.print(e + " "));
        Arrays.stream(arrayOfLong).skip(arrayOfLong.length - 5).forEach(System.out::println);
    }

    @Test
    public void testCommon() {
        int base = 0x80000000;
        int value = 23;
        int v2 = value ^ base; // 异或
        Assert.assertEquals("10000000", Integer.toBinaryString(0x80));
        Assert.assertEquals("10000000000000000000000000000000", Integer.toBinaryString(base));
        Assert.assertEquals("10111", Integer.toBinaryString(value));
        Assert.assertEquals("10000000000000000000000000010111", Integer.toBinaryString(v2));

        int i = 0x80;
        Assert.assertEquals(128, i);
        Assert.assertEquals(256, i << 1); // 放大
        Assert.assertEquals(64, i >> 1);  // 缩小
        // int => byte[] 大段含义： byte[0]存高位
    }
}
