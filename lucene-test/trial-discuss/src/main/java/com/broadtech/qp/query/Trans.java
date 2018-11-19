package com.broadtech.qp.query;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

/**
 * Created by Chen Yuanjun on 2017/8/3.
 */
public class Trans {

    public static void main(String[] args) throws DecoderException {
        String[] arr = arr();
        byte[] b = new byte[arr.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = Byte.parseByte(arr[i].trim());
        }
        System.out.println(new String(b) + "\n" + Arrays.toString(b));

        String hex = "d0";
        byte[] hex_bytes = Hex.decodeHex(hex.toCharArray());
        String str = Arrays.toString(hex_bytes);

        hex = Hex.encodeHexString(new byte[]{(byte) 128});

        System.out.println(str + "\n" + new String(hex_bytes) + "\n" + hex);

//        byte d = (byte) 127;
//        System.out.println(Integer.toBinaryString(d) + "\n" + Integer.toBinaryString(d >>> 1) + "\n" + Integer.toBinaryString(d >> 1));
//        System.out.println((d) + "\n" + (d >>> 1) + "\n" + (d >> 1));  // 没有 <<<运算符
//        System.out.println((d) + "\n" + "\n" + (d << 1));  // << 倍率： 2^i次方

    }

    public static String[] arr() {
        return ("4\n" +
                "98\n" +
                "111\n" +
                "111\n" +
                "107\n" +
                "5\n" +
                "98\n" +
                "111\n" +
                "111\n" +
                "107\n" +
                " 115\n" +
                " 1\n" +
                " 105\n" +
                " 4\n" +
                " 108\n" +
                " 105\n" +
                " 107\n" +
                " 101\n" +
                " 5\n" +
                " 109\n" +
                " 117\n" +
                " 115\n" +
                " 105\n" +
                " 99\n" +
                " 3\n" +
                " 110\n" +
                " 111\n" +
                " 110\n" +
                " 4\n" +
                " 112\n" +
                " 108\n" +
                " 97\n" +
                " 121\n" +
                " 8\n" +
                " 112\n" +
                " 108\n" +
                " 101\n" +
                " 97\n" +
                " 115\n" +
                " 97\n" +
                " 110\n" +
                " 116\n" +
                " 4\n" +
                " 114\n" +
                " 101\n" +
                " 97\n" +
                " 100").split("\n",-1);
    }
}
