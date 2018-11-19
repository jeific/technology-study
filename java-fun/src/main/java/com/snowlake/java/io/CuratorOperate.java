package com.snowlake.java.io;

import org.junit.Assert;

public class CuratorOperate {

    public static void main(String[] args) {
        ZookeeperClientHelper helper = new ZookeeperClientHelper("master01:2181");
        String root = "/snow_lake";
        helper.createEphemeral(root + "/test_1", new byte[0]);
        helper.createEphemeral(root + "/test_2", null);
        helper.createEphemeral(root + "/test_3/one", null);
        helper.setData(root + "/test_1", null);
        Assert.assertTrue(helper.exists(root));
        helper.recursiveDelete(root);
        Assert.assertFalse(helper.exists(root));
    }
}
