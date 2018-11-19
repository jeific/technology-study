package com.starcor.biz.demo.spi;

import java.lang.annotation.*;

/**
 * Created by ideal on 17-1-4.
 */
//加上这行只能在方法上注解
//@Target({ElementType.FIELD,ElementType.METHOD})//定义注解的作用目标**作用范围字段、枚举的常量/方法
@Target({ElementType.TYPE})   //只允许在class上注解
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Documented//说明该注解将被包含在javadoc中
public @interface Application {
    /**
     * 接口action
     * 如果有多个 则用逗号拼接起来
     */
    String action();

    String version() default "last";

}
