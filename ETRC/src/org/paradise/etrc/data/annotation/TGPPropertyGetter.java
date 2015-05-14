package org.paradise.etrc.data.annotation;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TGPPropertyGetter {
	int index() default Integer.MAX_VALUE;
	boolean firstline() default false;
	boolean element() default false;
}
