package com.snowlake.java.type;

import org.junit.Assert;

import java.nio.ByteBuffer;
import java.nio.file.Paths;

public class BaseTypeTrans {

    private BaseTypeTrans() {
    }

    public static void main(String[] args) {
        long l = 1538040903L;
        byte[] lbs = longToBytes(l);
        Assert.assertArrayEquals(new byte[]{0, 0, 0, 0, 91, -84, -92, 71}, lbs);
        Assert.assertEquals(l, bytesToLong(lbs));
        // byte:  -128(11111111) -- 127(11111111)
        Assert.assertEquals(-1, Integer.valueOf("11111111", 2).byteValue());
        Assert.assertEquals(127, Integer.valueOf("1111111", 2).byteValue());
        Assert.assertEquals("/bluewhale/bdp-web/job/leaderElect", // Paths构造连接路径表达式
                Paths.get("/bluewhale", "/bdp-web/job/leaderElect").toString().replace('\\', '/'));
    }

    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    //byte 数组与 long 的相互转换
    private static ByteBuffer buffer = ByteBuffer.allocate(8);

    public static byte[] longToBytes(long x) {
        buffer.clear();
        buffer.putLong(0, x).flip();
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        buffer.clear();
        buffer.put(bytes, 0, bytes.length).flip();//need flip
        return buffer.getLong();
    }

    /**
     * 将字节数组转为long<br>
     * 如果input为null,或offset指定的剩余数组长度不足8字节则抛出异常
     *
     * @param offset       起始偏移量
     * @param littleEndian 输入数组是否小端模式
     */
    public static long longFrom8Bytes(byte[] input, int offset, boolean littleEndian) {
        long value = 0;
        // 循环读取每个字节通过移位运算完成long的8个字节拼装
        for (int count = 0; count < 8; ++count) {
            int shift = (littleEndian ? count : (7 - count)) << 3;
            value |= ((long) 0xff << shift) & ((long) input[offset + count] << shift);
        }
        return value;
    }
}
