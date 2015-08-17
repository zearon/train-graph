package com.zearon.util.interface_;


import java.lang.reflect.Method;
import java.util.HashMap;

import com.zearon.util.data.Tuple2;
import com.zearon.util.interface_.test.IA;

/**
 * This interface holds data for interface instance just like FIELDs of INTERFACE do, and hence
 * enables the multi-inheritance programming paradigm. This interface behaves just like java.lang.Object 
 * in multi-inheritance class hierarchy.<br/><br/>
 * 
 * How to create a sub interface? The Following code framework should be enforced. <pre><code>
 * // IA and IB are both sub-interface of IMultiInheritance
 *public interface <b>IAB</b> extends IA, IB {
 *	// PLEASE CONFIRM that the parameter type is the same with the interface type.
 *	// Otherwise, this interface will NOT be initialized.
 * 	default void IMI_init(<b>IAB</b> thisObject) {
 * 		// Invoke this method for <b>EACH</b> parent interface.
 * 		IMI_initSuper(IA.class, thisObject);
 * 		IMI_initSuper(IB.class, thisObject);
 * 
 *		// Fields initializations. The First parameter is the <b>CLASS of this interface</b>.
 *		IMI_setProperty(<b>IAB.class</b>, "anInteger", 3);
 *		...
 *
 *		// Other stuff
 *		...
 *	}
 *
 *	// Other interface members
 *	...
 *}
 * </code></pre><br/>
 * 
 * Note: <br/>
 * 1. DO NOT override any method of this interface in any sub-interface/sub-class.<br/>
 * 2. IMI_getThisObject can ONLY be implemented by the final class implements this interface.
 * 		Any sub-interface CAN NOT implement this method by providing default method.<br/>
 * 3. If you want to provide a sub-interface of this interface, please DO PROVIDE a default method
 * 		which returns void and accepts a parameter of sub-interface type. This method will serves
 * 		as an "interface initializer" in which super interfaces are initialized and instance data,
 * 		just like fields of this object, are set.
 * 
 * @author Jeff Gong
 *
 */
public interface IMultiInheritance {

	public static HashMap<Object, HashMap<Tuple2<String, String>, Object>> allInstancePropertiesMap = new HashMap<>();
	
	/**
	 * PLEASE IMPLEMENT THIS METHOD AS THE FOLLOWING TO MAKE IT CORRECTLY WORKING!!! 
	 * <br/><pre><code>
	 * {
	 * 	IMI_initInterface();
	 * }
	 * 
	 * public IMultiInheritance IMI_getThisObject() {
	 * 	return this;
	 * }
	 * </code></pre><br/>
	 * DO NOT FORGET THE INITIALIZER!!!
	 * @return
	 */
	public IMultiInheritance IMI_getThisObject();
	
	/**
	 * Interface initializer.
	 * @param thisObject
	 */
	default void IMI_init(IMultiInheritance thisObject) {
	}

	/**
	 * DO NOT OVERRIDE THIS METHOD IN ANY SUB-INTERFACE/SUB-CLASS!!!
	 * @param thisObject
	 * @return
	 */
	default Object IMI_getProperty(Class<?> interfaceClass, String propertyName) {
		Object instance = IMI_getThisObject();
		HashMap<Tuple2<String, String>, Object> instanceProperties = allInstancePropertiesMap.get(instance);
		if (instanceProperties == null) {
			return null;
		}

		Tuple2<String, String> key = Tuple2.of(interfaceClass.getName(), propertyName);
		return instanceProperties.get(key);
	}

	/**
	 * DO NOT OVERRIDE THIS METHOD IN ANY SUB-INTERFACE/SUB-CLASS!!!
	 * @param thisObject
	 * @return
	 */
	default void IMI_setProperty(Class<?> interfaceClass, String propertyName, Object propertyValue) {
		Object instance = IMI_getThisObject();
		HashMap<Tuple2<String, String>, Object> instanceProperties = allInstancePropertiesMap.get(instance);
		if (instanceProperties == null) {
			instanceProperties = new HashMap<>();
			allInstancePropertiesMap.put(instance, instanceProperties);
		}
		
		Tuple2<String, String> key = Tuple2.of(interfaceClass.getName(), propertyName);
		instanceProperties.put(key, propertyValue);
	}
	
	/**
	 * DO NOT OVERRIDE THIS METHOD IN ANY SUB-INTERFACE/SUB-CLASS!!!
	 * @param thisObject
	 * @return
	 */
	default void IMI_initInterface() {
		IMultiInheritance thisObject = IMI_getThisObject();
		Class<?>[] interfaceClasses = thisObject.getClass().getInterfaces();
		for (int i = 0; i < interfaceClasses.length; ++ i) {
			if (IMultiInheritance.class.isAssignableFrom(interfaceClasses[i])) {
				IMI_initSuper(interfaceClasses[i], thisObject);
			}
		}
	}

	
	/**
	 * DO NOT OVERRIDE THIS METHOD IN ANY SUB-INTERFACE/SUB-CLASS!!!
	 * @param thisObject
	 * @return
	 */
	default void IMI_initSuper(Class<?> superInterfaceClass, IMultiInheritance thisObject) {
		final String initedKeyName = "IMI_Interface_Inited";
		try {
			Method method = superInterfaceClass.getDeclaredMethod("IMI_init", superInterfaceClass);
			Object inited = IMI_getProperty(superInterfaceClass, initedKeyName);
			if (inited == null) {
				method.invoke(thisObject, thisObject);
				IMI_setProperty(superInterfaceClass, initedKeyName, true);
			}
		} catch (Exception e) {
			System.err.println("Failed to initialize Interface %s" + superInterfaceClass.getName());
			e.printStackTrace();
		}
	}
}
