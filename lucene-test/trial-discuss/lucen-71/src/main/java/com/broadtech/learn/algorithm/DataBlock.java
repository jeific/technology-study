package com.broadtech.learn.algorithm;

/**
 * 描述一个缓存实体，并提供数据访问器
 */
public class DataBlock {
    public DataBlock forward;
    public DataBlock backward;
    public long startPoint;
    public long endPoint;
    private byte[] data;
    private int length;

    public DataBlock(byte[] data, long startPoint, int length) {
        this.data = data;
        this.startPoint = startPoint;
        this.endPoint = startPoint + length;
        this.length = length;
    }

    public byte read(long pos) {
        return data[(int) (pos - startPoint)];
    }

    public int length() {
        return length;
    }

    public boolean contains(long pos) {
        return startPoint <= pos && pos < endPoint;
    }

    public void copy(int srcPos, byte[] dest, int destPos, int length) {
        System.arraycopy(data, srcPos, dest, destPos, length);
    }
}
