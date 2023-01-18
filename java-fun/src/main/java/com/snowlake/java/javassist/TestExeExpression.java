package com.snowlake.java.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import java.util.UUID;

/**
 * 字符串表达式计算
 * 方案一：通过字符串转换代码动态执行表达式计算
 * 方案二：通过mysql执行select语句
 */
public class TestExeExpression {
    public static void main(String[] args) throws Exception {
        System.out.println(exeExpression("1 + 2 * 6"));
        System.out.println(exeExpression("100 + 2 * 6/2.3"));
    }

    public interface ExpressionOpt {
        double exe();
    }

    private static String getClassName() {
        String id = UUID.randomUUID().toString();
        return "C" + id.replace("-", "");
    }

    private static double exeExpression(String expression) throws Exception {
        //获取默认类池
        ClassPool c = ClassPool.getDefault();
        //创建一个类ClassDemo
        CtClass ctClass = c.makeClass("com.snowlake.java.javassist." + getClassName());
        //让该类实现序列化接口
        ctClass.addInterface(c.get(ExpressionOpt.class.getName()));
        //添加需要执行的方法
        CtMethod m = CtNewMethod.make(
                "public double exe(){\n" +
                        "        return (double)(" + expression + ");\n" +
                        "        }", ctClass);
        //给一个类添加新的方法及该方法的具体实现
        ctClass.addMethod(m);
        //制作一个空的类初始化程序(静态构造函数)
        ctClass.makeClassInitializer();
        //将此类转换为java.lang.Class对象
        java.lang.Class<?> t = ctClass.toClass();
        ExpressionOpt object = (ExpressionOpt) t.newInstance();
        //执行接口中的方法
        return object.exe();
    }
}
