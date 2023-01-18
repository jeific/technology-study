package com.snowlake.java.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class Test {
    public interface Past {
        void exe();
    }

    @Deprecated
    public void slot() throws Exception {
        //获取默认类池
        ClassPool c = ClassPool.getDefault();
        //引入需要的依赖包,可以引入多个(需要全路径)
        c.importPackage("org.apache.commons.lang3.StringUtils");
//        c.importPackage("...");
        //创建一个类ClassDemo
        CtClass ctClass = c.makeClass("cn.sdata.entity.test");
        //让该类实现序列化接口
        ctClass.addInterface(c.get(Past.class.getName()));
        //添加需要执行的方法
        CtMethod m = CtNewMethod.make(
                "public void exe(){         System.out.println(\"Hello World!\" + (13 + 2 * 5 / 3));\n" +
                        "        for (int i = 0; i < 10; i++) {\n" +
                        "            System.out.println(i);\n" +
                        "        } }", ctClass);
        //给一个类添加新的方法及该方法的具体实现
        ctClass.addMethod(m);
        //制作一个空的类初始化程序(静态构造函数)
        ctClass.makeClassInitializer();
        //将此类转换为java.lang.Class对象
        java.lang.Class<?> t = ctClass.toClass();
        Past object = (Past) t.newInstance();
        //执行接口中的方法
        object.exe();
    }

    public static void main(String[] args) throws Exception {
        new Test().slot();
    }
}