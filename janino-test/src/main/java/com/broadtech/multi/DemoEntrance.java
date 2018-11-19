package com.broadtech.multi;

import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.Scanner;

import java.io.StringReader;

/**
 * create by 2018/2/12 16:21<br>
 *
 * @author Yuanjun Chen
 * <p>
 * 测试实现多接口的字节码翻译
 */
public class DemoEntrance {

    public static void main(String[] args) throws Exception {
        String classBody =
                "public String trans(String arg){return arg;} " +
                        "public void print(String arg){System.out.println(arg);}";
        MultiMethodInterface in = (MultiMethodInterface) ClassBodyEvaluator.createFastClassBodyEvaluator(
                new Scanner(null, new StringReader(classBody)),
                MultiMethodInterface.class,                  // Base type to extend/implement
                (ClassLoader) null          // Use current thread's context class loader
        );

        in.print(in.trans("This is first class body evaluator!"));

        System.out.println(in.getClass().getAnnotatedInterfaces()[0].getType().getTypeName());
        System.out.println(in.getClass().getMethods()[0].toGenericString());
    }
}
