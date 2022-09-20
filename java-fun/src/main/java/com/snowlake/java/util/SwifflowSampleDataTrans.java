package com.snowlake.java.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class SwifflowSampleDataTrans {

    public static void main(String[] args) throws DecoderException {
        String sampleData = "32303139303830367c63627378313334303336313240697074767c3230313930383036313932323136";
        byte[] data = Hex.decodeHex(sampleData.toCharArray());
        String str = new String(data);
        System.out.println(str);
        byte b = '|';
        String hex = Hex.encodeHexString(new byte[]{b});
        System.out.println(String.format("%c --> %s", b, hex));
    }
}
