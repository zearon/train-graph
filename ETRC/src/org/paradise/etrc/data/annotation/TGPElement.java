package org.paradise.etrc.data.annotation;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TGPElement {
	String name();
	boolean inOneLine() default false;
	boolean isList() default false;
	int index() default Integer.MAX_VALUE;
}
