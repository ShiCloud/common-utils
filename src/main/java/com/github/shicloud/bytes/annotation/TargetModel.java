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
public @interface TargetModel {
	/**
	 * value
	 */
	String value() default "";

}
