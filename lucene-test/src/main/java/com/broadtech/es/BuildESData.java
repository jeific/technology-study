package com.broadtech.es;

import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 关于性能：
 * 1. 关闭不必要的全文分词 即String字段类型
 * 2. 关闭_all功能
 * 3. routing合理使用, 利用它实现数据分类
 * 4. shards合理定位, 尽可能保证一次查询在一个shard中完成
 * 5. mget,bulk进行批量的读写
 * 6.查询设置timeout限制查询耗时，在不需要返回全部民众且不需要score场景有用
 */
public class BuildESData {

    /**
     * index.mapping.total_fields.limit=1000 (default)
     * 测试结论： Field计算公式: rootField + rootNestedField x nestedFieldNum<br>
     */
    @Test
    public void testIndexMappingExplosion() {
        String value = "[{\n" +
                "\t\t\"job_id\":\"423456\",\n" +
                "\t\t\"value\":\"10\",\n" +
                "\t\t\"weight\":0.5,\n" +
                "\t\t\"last_time\":1538073269899\n" +
                "\t},{\n" +
                "\t\t\"job_id\":\"523456\",\n" +
                "\t\t\"value\":\"其它\",\n" +
                "\t\t\"weight\":1.0,\n" +
                "\t\t\"last_time\":1538073269899\n" +
                "\t}],\n";
        StringBuilder builder = new StringBuilder("{\n");
        for (int i = 0; i < 50; i++) {
            builder.append("   \"tag_").append(i).append("\":").append(value);
        }
        builder.setLength(builder.length() - 2);
        builder.append("\n}");
        System.out.println(builder.toString());
    }

    /**
     * 默认下，nested Field不得>50个<br>
     * index.mapping.nested_fields.limit=50 (default)<br>
     * 可选方案: 优点，查询总体耗时有保证 （提取的数据较少）<br>
     * 缺点：默认仅支持50个tag字段， 当Nested字段和小于20时
     */
    @Test
    public void testNestedFieldNum() {
        String value = "[{\n" +
                "\t\t\"job_id\":\"423456\",\n" +
                "\t\t\"value\":\"10\",\n" +
                "\t\t\"weight\":0.5,\n" +
                "\t\t\"high\":1.0,\n" +
                "\t\t\"last_time\":1538073269899\n" +
                "\t}],\n";
        StringBuilder builder = new StringBuilder("{\n");
        for (int i = 0; i < 51; i++) {
            builder.append("   \"tag_").append(i).append("\":").append(value);
        }
        builder.setLength(builder.length() - 2);
        builder.append("\n}");
        System.out.println(builder.toString());
    }

    /**
     * 测试表明： 单个Field嵌套的对象至少不低于1W，初步估计是无限的
     */
    @Test
    public void testNestedObjectNum() {
        String value = "{\n" +
                "\t\t\"job_id\":\"423456\",\n" +
                "\t\t\"value\":\"10\",\n" +
                "\t\t\"weight\":0.5,\n" +
                "\t\t\"high\":1.0,\n" +
                "\t\t\"last_time\":1538073269899\n" +
                "\t},\n";
        StringBuilder builder = new StringBuilder("{\n   \"tag_51\":[");
        for (int i = 0; i < 10000; i++) {
            builder.append(value).append("\t");
        }
        builder.setLength(builder.length() - 3);
        builder.append("]\n}");
        System.out.println(builder.toString());
    }

    /**
     * 方案2：<br>
     * 优点： 1. 不受tag上限先知，数据集中<br>
     * 缺点: 每次都需要提取所有Nested数据，即全部tag<br>
     * 查询耗时和{@link #testNestedFieldNum()} ()}相同
     */
    @Test
    public void testSuggestSolution_2() {
        StringBuilder builder = new StringBuilder("{\n   \"tag_value\":[");
        for (int i = 0; i < 2000; i++) {
            builder.append(getNested("001_005_" + i)).append("\t");
        }
        builder.setLength(builder.length() - 3);
        builder.append("]\n}");
        System.out.println(builder.toString());
    }

    private String getNested(String tagId) {
        return "{\n" +
                "\t\t\"_tag_id\":\"" + tagId + "\",\n" +
                "\t\t\"job_id\":\"423456\",\n" +
                "\t\t\"value\":\"10\",\n" +
                "\t\t\"weight\":0.5,\n" +
                "\t\t\"last_time\":1538073269899\n" +
                "\t},{\n" +
                "\t\t\"_tag_id\":\"" + tagId + "\",\n" +
                "\t\t\"job_id\":\"523456\",\n" +
                "\t\t\"value\":\"其它\",\n" +
                "\t\t\"weight\":1.0,\n" +
                "\t\t\"last_time\":1538073269899\n" +
                "\t},\n";
    }

    @Test
    public void testSet() {
        Set<Integer> set = new TreeSet<>();
        // set.add(null); 不支持null
        set.add(1);
        set.add(20);
        set.add(15);
        System.out.println(set);

        Map<Integer, Integer> map = new TreeMap<>();
        // map.put(null, 2); key不支持null
        map.put(40, 2);
        map.put(20, 3);
        map.put(10, 4);
        System.out.println(map);
    }
}
