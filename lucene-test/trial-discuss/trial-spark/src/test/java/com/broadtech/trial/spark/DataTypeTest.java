package com.broadtech.trial.spark;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.junit.Assert;
import org.junit.Test;

/**
 * create by 2018/1/9 11:22<br>
 *
 * @author Yuanjun Chen
 */
public class DataTypeTest {

    @Test
    public void dataType() {
        DataType t1 = DataTypes.BinaryType;
        String json = t1.json();
        DataType c1 = DataType.fromJson(json);
        Assert.assertEquals(json, c1.json());
    }
}
