package com.broadtech.ydb;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jeifi on 2017/8/14.
 */
public class FunTest {

    public static void main(String[] args) {
        String[] var1 = new String[]{"赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "楮", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏", "陶", "姜", "戚", "谢", "邹", "喻", "柏", "水", "窦", "章", "云", "苏", "潘", "葛", "奚", "范", "彭", "郎", "鲁", "韦", "昌", "马", "苗", "凤", "花", "方", "俞", "任", "袁", "柳", "酆", "鲍", "史", "唐", "费", "廉", "岑", "薛", "雷", "贺", "倪", "汤", "滕", "殷", "罗", "毕", "郝", "邬", "安", "常", "乐", "于", "时", "傅", "皮", "卞", "齐", "康", "伍", "余", "元", "卜", "顾", "孟", "平", "黄", "和", "穆", "萧", "尹", "姚", "邵", "湛", "汪", "祁", "毛"};
        String[] var2 = makeRage(10, var1);
        System.out.println(Arrays.toString(var1) + "\n" + Arrays.toString(var2));

    }

    public static String[] makeRage(int var0, String[] var1) {
        ArrayList var2 = new ArrayList();
        String[] var3 = var1;
        int var4 = var1.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String var6 = var3[var5];
            String[] var7 = var6.split(",");
            int var8 = (int) (Math.random() * (double) var0);

            for (int var9 = 0; var9 < var8; ++var9) {
                var2.add(var7[0].replace(',', ' '));
            }
        }

        return (String[]) var2.toArray(new String[var2.size()]);
    }
}
