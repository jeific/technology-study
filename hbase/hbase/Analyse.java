package com.broadtech.hbase;

import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.TokenUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Hash;

import java.util.List;

/**
 * create by 2018/1/3 16:56<br>
 *
 * @author Yuanjun Chen
 */
public class Analyse {
    private static final Logger logger = Logger.getLogger(Analyse.class);

    public static void main(String[] args) throws DecoderException {
        System.out.println(1514910200757L / 1000 / 5 / 60);
        Analyse analyse = new Analyse();
        String rowkeyByHex = "0000004d0d64323138333932333331d3035297b0";
        String value = "03cd4e000000045636b046001714273735377b572e77";
        analyse.indexRowKeyAnalyse(rowkeyByHex, value);
        String entityValue = "0003ff3903ff3135313439313032303037323803ff3135313439313032303037353703ff3003ff34363030313632393236313539373603ff3335333235323037343834393239303703ff313836323632393338333003ff31302e3130332e32372e363603ff313439353403ff31302e3130302e33362e363803ff3336313836363937323903ff323037383603ff03ff32313833393233333103ff34363003ff303103ff31333003ff313330313303ff34363003ff31333003ff313330313303ff34363003ff31303103ff03ff3603ff33676e65742e6d6e633030312e6d63633436302e6770727303ff333003ff3003ff3103ff3103ff343003ff38373403ff31302e3133312e3131302e31373603ff353034313003ff3132332e3230372e3230392e363003ff323532363903ff313903ff3738373103ff03ff03ff03ff3003ff39393903ff3003ff3003ff3003ff3003ff3003ff3003ff3003ff3003ff03ff03ff3003ff3003ff3003ff3003ff3003ff3003ff3003ff3003ff03ff03ff3003ff3003ff3103ff333003ff3003ff3003ff3003ff3003ff3003ff3003ff3003ff3003ff3003ff3003ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff03ff";

        logger.info("IndexRowKey: " + rowkeyByHex + " EntityRowKey: " + value);

        byte[] data = Hex.decodeHex(entityValue.toCharArray());
        byte[] fieldSeq = Hex.decodeHex("03ff".toCharArray());
        List<byte[]> fieldValues = TokenUtils.tokensFromLine(data, fieldSeq, 300);
        for (int i = 0; i < fieldValues.size(); i++) {
            logger.info((i + 1) + "\t" + new String(fieldValues.get(i)));
        }
    }

    public void indexRowKeyAnalyse(String rowkeyByHex, String value) throws DecoderException {
        logger.info(rowkeyByHex.length() + "\t" + value.length());
        byte[] rowKey = Hex.decodeHex(rowkeyByHex.toCharArray());
        byte[] valueData = Hex.decodeHex(value.toCharArray());
        logger.info(indexRowKeyAnalyse(rowKey, valueData));
    }

    public String indexRowKeyAnalyse(byte[] rowKey, byte[] value) {
        logger.info("rowKey len: " + rowKey.length + " value data len:" + value.length + " Num(/22): " + value.length / 22);
        short hash = Bytes.toShort(Bytes.copy(rowKey, 0, 2));
        int timeTimeGranularity = Bytes.toInt(Bytes.copy(rowKey, 2, 4));
        String ci = Bytes.toString(Bytes.copy(rowKey, 6, rowKey.length - 11));
        return "hash: " + hash + " TimeGranularity: " + timeTimeGranularity + " CI: " + ci;
    }

    public String indexRowKeyAnalyse(byte[] rowKey, byte[] value, String expect) {
        short hash = Bytes.toShort(Bytes.copy(rowKey, 0, 2));
        int timeTimeGranularity = Bytes.toInt(Bytes.copy(rowKey, 2, 4));
        String ci = Bytes.toString(Bytes.copy(rowKey, 6, rowKey.length - 11));
        if (expect == null || ci.equals(expect))
            return "hash: " + hash + " TimeGranularity: " + timeTimeGranularity + " CI: " + ci;
        else return null;
    }

    /**
     * 按数据时间计算时间分区
     *
     * @param timeSeconds 数据时间 单位（秒）
     * @param timeTimeMin 时间粒度（分钟）
     * @return 4位的byte[]
     */
    public static byte[] getTimeTimeGranularity(int timeSeconds, int timeTimeMin) {
        int timeTimeGranularitySeconds = timeTimeMin * 60;
        int timeTimeGranularity = timeSeconds / timeTimeGranularitySeconds;
        return Bytes.toBytes(timeTimeGranularity);
    }

    /**
     * 生成hash头,2个字节
     */
    public static byte[] buildRowKeyHash(byte[] value) {
        short hashValue = (short) (Math.abs(Hash.getInstance(2).hash(value)) % 1000);
        return Bytes.toBytes(hashValue);
    }
}
