package org.paradise.etrc.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.paradise.etrc.data.NullPart;
import org.paradise.etrc.data.TrainGraphPart;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TrainGraphElement {
	String prefix();
	String suffix();
	Class<? extends TrainGraphPart> elementType() default NullPart.class;
}
