package com.broadtech.qp.arithmetic;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeifi on 2017/7/27.
 * 自定义的finite state automation<br>
 * 有穷自动机（finite automation，FA）,有时也叫有穷状态机（finite state machine）:<br>
 * <li>确定性有穷自动机（DFA），其特点是从每一个状态只能发出一条具有某个符号的边。
 * 也就是说不能出现同一个符号出现在同一状态发出的两条边上</li>
 * <li>非确定性有穷自动机（NFA),它允许从一个状态发出多条具有相同符号的边，
 * 甚至允许发出标有ε（表示空）符号的边，也就是说，NFA可以不输入任何字符就自动沿ε边转换到下一个状态</li>
 */
public class CustomFSA {

    public static void main(String[] args) {
        System.out.println("1>" + Integer.toString(17, 36));
        System.out.println("2>" + Integer.toString(17, Character.MIN_RADIX));
        System.out.println("3>" + Integer.toString(17, 8));
        System.out.println("4>" + Integer.toString(17, 10));
        System.out.println("5>" + Integer.toString(17, 16));
    }

    private static void testJosn() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 23);
        map.put("name", "jeific");
        map.put("price", 29.63);
        map.put("price2", 29.63f);
        map.put("enjoy", true);
        System.out.println(toJSON(map));
    }

    public static String toJSON(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder("{");
        map.forEach((k, v) -> {
            builder.append("\"").append(k).append("\":");
            switch (v.getClass().getSimpleName()) {
                case "Integer":
                case "Byte":
                case "Short":
                case "Long":
                case "Float":
                case "Double":
                case "Boolean":
                    builder.append(v);
                    break;
                case "Date":
                case "Timestamp":
                    builder.append(((Date) v).getTime());
                    break;
                case "BigDecimal":
                    builder.append(((BigDecimal) v).toBigInteger());
                    break;
                default:
                    builder.append("\"").append(v).append("\"");
                    break;
            }
            builder.append(",");
        });
        builder.replace(builder.length() - 1, builder.length(), "}");
        return builder.toString();
    }
}
