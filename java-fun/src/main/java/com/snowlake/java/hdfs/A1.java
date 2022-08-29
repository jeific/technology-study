package com.snowlake.java.hdfs;

public class A1 implements A {

    @Override
    public <T> T abs(Class clz) {
        if (clz.getTypeName().equals(String.class.getTypeName())) {
            return (T) "string";
        } else if (clz.getTypeName().equals(Integer.TYPE.getTypeName())) {
            return (T) new Integer(123);
        } else {
            return null;
        }
    }
}
