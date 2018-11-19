package com.broadtech.janino;

import java.util.ArrayList;
import java.util.List;

/**
 * create by 2017/12/29 15:29<br>
 *
 * @author Yuanjun Chen
 */
public class Demo {

    public static void main(String[] args) throws Exception {
        String code = "String[] items = input.split(\" \");\n" +
                "List<String> list = new ArrayList<String>();\n" +
                "list.add(\"1\");\n" +
                "list.add(\"3\");\n";
        // code += "list.forEach(k -> System.out.println(k));";
        code += "return items[0];";

        JaninoCompiler compiler = new JaninoCompiler();
        IPlugin plugin = compiler.compile(code, new String[]{"input"},
                new String[]{"java.util.ArrayList", "java.util.List"});

        // 执行
        String output = plugin.exe("Hello world!");
        if (output.equals("Hello")) {
            System.out.println("执行成功 > " + output);
        } else {
            System.out.println("执行失败 > " + output);
        }
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("3");
        list.forEach(k -> System.out.println(k));
    }
}
