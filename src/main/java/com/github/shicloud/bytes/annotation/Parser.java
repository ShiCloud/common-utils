package com.github.shicloud.bytes.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * @author sfilo
 *
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Parser {
    /**
     * 解析字节的顺序 
     */
    int index() default 0;

    /**
     * 根据第几个index的值 截取字节的长度
     */
    int dependsOn() default 0;
    
    /**
     * 截取字节的长度
     */
    int lenght() default 0;
    /**
     * 解析字节的偏移量
     */
    int offset() default 0;
    /**
     * 解析字节的偏移量
     */
    int divide() default 1;
}
