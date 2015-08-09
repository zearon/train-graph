package org.paradise.etrc.data.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.paradise.etrc.data.TrainGraphPart;

/**
 * 支持序列化及反序列化的标记, 用于标记继承自TrainGraphPart的TrainGraph元素.<br/>
 * 该标记用于需要被序列化的元素上, 这些元素可以是模型的顶层元素,如TrainGraph; 还可以是TrainGraph元素的对象属性, 如
 * TrainGraph.RailNetwork, 和数组属性, 如TrainGraph.charts. 但是不论是对象属性, 还是数组属性,
 * 其类型必须为TrainGraphPart的子类.<br/><br/>
 * 注: 对每一个继承TrainGraphPart的类来说,标注于public, protected, (package), private类型
 * 上的Field和Method都可以标签处理程序发现, 但是继承自基类的带标记Field和Method中只有public类型
 * 的可以被标签处理程序发现并处理.其属性包含: <br/><br/>
 * 
 * @author Jeff Gong
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface TGElement {
	/**
	 * 序列化时的元素名称.每一个TrainGraph元素的对象属性及列表属性在该元素的范围内
	 * 必须有唯一的名称. 
	 */
	String name() default "";
	
	/**
	 * 该标签标注的属性是否是一个数组属性. 对于TrainGraph元素的对象属性,该值必须为false;
	 * 对于TrainGraph元素的数组属性,该值必须为true. <br/><br/>默认值为false.
	 */
	boolean isList() default false;
	
	Class<? extends TrainGraphPart> type() default TrainGraphPart.class;
	
	/**
	 * 用于决定序列化顺序的序号. 序列化一个TrainGraph元素时, 分两组进行. 第一组首先序列化其简单属性, 
	 * 然后第二组序列化对象属性和数组属性. index值越大,序列化时顺序越靠后. 如果不指定该属性, 则按照
	 * 属性在类中的定义顺序进行. 继承自基类的属性在本类属性之后进行. 通过指定index的值,可以改变属性的
	 * 序列化先后次序.<br/><br/>
	 * 默认值为Integer.MAX_VALUE.
	 */
	int index() default Integer.MAX_VALUE;
}
