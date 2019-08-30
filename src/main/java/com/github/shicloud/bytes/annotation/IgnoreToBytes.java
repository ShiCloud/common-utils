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
public @interface IgnoreToBytes {
    /**
     * 解析不解析到字节数组 
     */
    boolean value() default true;
}
