package org.paradise.etrc.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TGPProperty {
	int index() default Integer.MAX_VALUE;
	boolean firstline() default false;
	boolean element() default false;
}
