package org.paradise.etrc.data.annotation;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

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
public @interface TGElementType {
	/**
	 * 序列化时的元素名称.每一个TrainGraph元素的对象属性及列表属性在该元素的范围内
	 * 必须有唯一的名称. 
	 */
	String name();
	
	/**
	 * 序列化时是否显示在一行内. 默认值为false.
	 */
	boolean printInOneLine() default false;
	
	/**
	 * 是否是树形对象模型TrainGraph的根元素. <br/><br/>默认值为false.
	 */
	boolean root() default false;
}
