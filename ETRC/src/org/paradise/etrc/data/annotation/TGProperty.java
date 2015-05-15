package org.paradise.etrc.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 支持序列化及反序列化的标记简单属性., 用于标记继承自TrainGraphPart的TrainGraph元素的需要被序列化的.<br/>
 * 该标签应该标注在Field或者对应的Getter和Setter方法之上.
 * 所谓简单属性, 指的是类型不在TrainGraphPart继承体系中的属性, 默认支持的类型包含 boolean, byte, 
 * int, long, float, double 类型及其wrapper类型, 以及 java.lang.String 和 java.awt.Color.
 * 
 * java.awt.Color等类型的..<br/><br/>
 * 注: 对每一个继承TrainGraphPart的类来说,标注于public, protected, (package), private类型
 * 上的Field和Method都可以标签处理程序发现, 但是继承自基类的带标记Field和Method中只有public类型
 * 的可以被标签处理程序发现并处理.其属性包含: <br/><br/>
 * <code>name</code> 	序列化时的元素名称.每一个TrainGraph元素的对象属性及列表属性在该元素的范围内
 * 						必须有唯一的名称.<br/>
 * 
 * @see org.paradise.etrc.util.data.ValueTypeConverter
 * @author Jeff Gong
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface TGProperty {
	/**
	 * 序列化时的元素名称.每一个TrainGraph元素的对象属性及列表属性在该元素的范围内
	 * 必须有唯一的名称. 
	 */
	String name() default "";
	
	/**
	 * 用于决定序列化顺序的序号. 序列化一个TrainGraph元素时, 分两组进行. 第一组首先序列化其简单属性, 
	 * 然后第二组序列化对象属性和数组属性. index值越大,序列化时顺序越靠后. 如果不指定该属性, 则按照
	 * 属性在类中的定义顺序进行. 继承自基类的属性在本类属性之后进行. 通过指定index的值,可以改变属性的
	 * 序列化先后次序.<br/><br/>
	 * 默认值为Integer.MAX_VALUE.
	 */
	int index() default Integer.MAX_VALUE;
	boolean firstline() default false;
}
