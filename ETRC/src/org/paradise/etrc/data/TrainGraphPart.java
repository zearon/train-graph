package org.paradise.etrc.data;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.paradise.etrc.ETRCUtil;
import org.paradise.etrc.data.v1.*;
import org.paradise.etrc.data.annotation.*;
import org.paradise.etrc.util.data.Tuple2;
import org.paradise.etrc.util.data.Tuple3;
import org.paradise.etrc.util.data.ValueTypeConverter;
import org.paradise.etrc.util.function.MultiConsumer;
import org.paradise.etrc.util.function.TriConsumer;

import com.sun.org.apache.xpath.internal.operations.Bool;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;


/**
 * 所有组成运行图的部件的基类
 * T是继承该类的类型本身,
 * ET是该类的元素的类型
 * @author Jeff Gong
 *
 */
@SuppressWarnings("rawtypes")
public abstract class TrainGraphPart<ET extends TrainGraphPart> {
	
	
	static boolean SHOW_DEBUG_MESSAGE = "true".equalsIgnoreCase(System.getenv("TGP-DEBUG"));

	static void DEBUG_MSG(String msgFormat, Object... msgArgs) {
		if (SHOW_DEBUG_MESSAGE)
			ETRCUtil.DEBUG_MSG(msgFormat, msgArgs);
	}
	
	// {{ Static fields
	
	public static int TO_STRING_ELEMENT_DEPTH = 0;
	public static HashMap<String, Integer> TO_STRING_SPECIFIC_TYPE_DEPTH = new HashMap<String, Integer> ();
//	public static HashMap<String, Integer> TO_DEBUG_STRING_SPECIFIC_TYPE_DEPTH = new HashMap<String, Integer> ();
	public static boolean TO_STRING_SHOW_TYPE = false;
	public static boolean TO_STRING_SHOW_PROPERTIES = false;
	
	public static final String IDENT_STR	= "  ";
	public static final String NEW_LINE_STR	= "\r\n";
		
	protected static LinkedHashMap<Tuple2<String, String>, Function<TrainGraphPart, String>> simplePropertyGetterMap =
			new LinkedHashMap<> ();
	protected static LinkedHashMap<Tuple2<String, String>, BiConsumer<TrainGraphPart, String>> simplePropertySetterMap =
			new LinkedHashMap<> ();
	protected static HashMap<Tuple2<String, String>, String> PropertyTypeMap =
			new HashMap<> ();
	/* a list of tuples in form of ((className, propName), (porpIndex, firstLine) ) */
	protected static Vector<Tuple2<Tuple2<String, String>, Tuple2<Integer,Boolean>>> simplePropertyIndexList =
			new Vector<> ();
	/* a map whose key is className, and the value is a list of tuples 
	   in form of ((className, propName), (porpIndex, firstLine) ) */
	protected static Map<String, List<Tuple2<Tuple2<String, String>, Tuple2<Integer,Boolean> >>> simplePropertyIndexMap;
	
	protected static LinkedHashMap<Tuple2<String, String>, 
		Tuple3<TGElementAttr, Class<?>, Function<TrainGraphPart, Object>> > elementGetterMap = 
			new LinkedHashMap<> ();
	protected static LinkedHashMap<Tuple2<String, String>, 
	Tuple3<TGElementAttr, Class<?>, BiConsumer<TrainGraphPart, Object>> > elementSetterMap = 
		new LinkedHashMap<> ();
	protected static LinkedHashMap<Tuple2<String, String>, 
	Tuple2<Class<?>, Supplier<? extends Object>> > elementCreatorMap = 
		new LinkedHashMap<> ();
	Vector<Tuple3<Class<?>, String, TGElementType>> registeredTypeList = new Vector<> ();

	// (className) -> <ClassName, PropName) key in elementGetterMap
	protected static Map<String, List<Tuple2<String, String>> > elementGetterKeyDict;
	

	protected static HashMap<Class<? extends TrainGraphPart>, Supplier<? extends TrainGraphPart>> registeredTypeCreatorDict = 
			new HashMap<> ();
	
	static Map<Class<?>, String> typeToNameDict;
	static Map<Class<?>, TGElementType> typeToAttrDict;
	static Map<String, Class<?>> nameToTypeDict;
	
	
	
	public static String ELEMENT_REPR_PREFIX;
	public static String ELEMENT_REPR_SUFFIX;
	public static Class<? extends TrainGraphPart> ELEMENT_TYPE;
	public static Vector<Field> ELEMENT_SIMPLE_PROPERTY_FIELD_LIST;

	static HashMap<String, Integer> _objectIdMap = new HashMap<String, Integer> ();

	
	static {
		setSimpleToString();
	}
	
	// }}
	
	// {{ 实例属性及抽象方法
	protected int _id;
	
	public String name;
	@TGProperty(firstline=true)
	public String getName() { return name; }
	@TGProperty
	public void setName(String name) { this.name = name; }
	
	/**************************************************************************
	 * Methods need to be implemented.
	 * Element data accessers which will be used in save/load template methods
	 **************************************************************************/
	
	public String getElementName() {
		return typeToNameDict.get(getClass());
	}
	
	String createTGPNameById(int id) { return String.format("%s %d", getElementName(), id); }
	public void initElements() {}
	public void setToDefault() {}
	
	/* Binary coding */
	protected boolean isBase64Encoded() { return false; }
	protected String encodeToBase64() { return ""; }
	protected void decodeFromBase64Start() {};
	protected void decodeFromBase64NewLine(String base64Line) {};
	protected void decodeFromBase64End() {};
	
	/* Do complete work after all data loaded from file */
	protected void loadComplete() {}
	
	/**************************End of Abstract Methods*********************/
	
	// }}
	
	// {{ 构造函数及静态newInstance方法
	
	protected TrainGraphPart() {
		// Set ID
		String className = getClass().getName();
		int id = _objectIdMap.getOrDefault(className, -1);
		if (id < 0) {
			id = 0;
			_objectIdMap.put(className, id + 1);
		}
		
		this._id = ++ id;
		
		_objectIdMap.put(className, id);
	}

	public static <T extends TrainGraphPart> T newInstance(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException(__("can not load default instance for class NULL."));
		}
		
		// Instantiate the object
		Constructor<T> constructor = findDefaultConstructor(clazz);
		
		return newInstance(constructor);
	}
	
	public static <T extends TrainGraphPart> Constructor<T> findDefaultConstructor(Class<T> clazz) {

		if (clazz == null) {
			throw new IllegalArgumentException(__("can not find constructor for class NULL."));
		}

		try {
			// Instantiate the object
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			
			return constructor;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(String.format(
					__("Class %s does not have a default non-argument constructor."),
					clazz.getName()), e);
		}

	}
	
	public static <T extends TrainGraphPart> T newInstance(Constructor<T> constructor) {
		if (constructor == null) {
			throw new IllegalArgumentException(__("Can not instantiate class with NULL constructor."));
		}

		try {
			constructor.setAccessible(true);
			
			T obj = constructor.newInstance();
			
			return obj;
		} catch (SecurityException e) {
			throw new IllegalArgumentException(String.format(
					__("Constructor of class %s is not accessible."),
					""), e);
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException | IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format(
					__("Can not instantiate class %s with default constructor."),
					""), e);
		}
	}
	
	// }}

	// {{ 静态 reprJoining 方法
	
	public static String reprJoining(Object[] array, String delimeter, boolean debug) {
		return reprJoining(Arrays.stream(array), delimeter, debug);
	}
	
	public static String reprJoining(Iterable<Object> iterable, String delimeter, boolean debug) {
		return reprJoining(StreamSupport.stream(iterable.spliterator(), false), delimeter, debug);
	}
	
	public static String reprJoining(Stream<Object> stream, String delimeter, boolean debug) {
		return stream.map(obj -> {
					if (obj instanceof TrainGraphPart) {
						if (debug)
							return ((TrainGraphPart) obj).toDebugString();
						else
							return ((TrainGraphPart) obj).toString();
					} else {
						return obj.toString();
					} })
			.collect(Collectors.joining(delimeter, "[", "]"));
	}
	
	// }}
	
	// {{ TrainGraphPart及其子类的Annotation处理 (@TGElementTYpe, @TGProperty, @TGElement)
	
	public void prepareForFirstLoading() {
		processAnnotations();
	}
	
	protected void processAnnotations() {		
		simplePropertyGetterMap.clear();
		simplePropertySetterMap.clear();
		simplePropertyIndexList.clear();
		elementGetterMap.clear();
		elementSetterMap.clear();
		elementCreatorMap.clear();
		
		LinkedList<Class<? extends TrainGraphPart>> classList = new LinkedList<> ();
		HashSet<Class<? extends TrainGraphPart>> processedClassSet = new HashSet<> ();
		Class<? extends TrainGraphPart> clazz = getClass(); 
		classList.addLast(clazz);
		processedClassSet.add(clazz);
		
		while (!classList.isEmpty()) {
			clazz = classList.pollFirst();
			processedClassSet.add(clazz);
			processClassAnnotations(clazz, classList, processedClassSet);
		}

		// Create maps for efficiency fetching.
		typeToNameDict = registeredTypeList.stream().collect(Collectors.toMap(
				(Tuple3<Class<?>, String, TGElementType> tuple) -> tuple.A   , 
				(Tuple3<Class<?>, String, TGElementType> tuple) -> tuple.B));
		typeToAttrDict = registeredTypeList.stream().collect(Collectors.toMap(
				(Tuple3<Class<?>, String, TGElementType> tuple) -> tuple.A   , 
				(Tuple3<Class<?>, String, TGElementType> tuple) -> tuple.C));
		nameToTypeDict = registeredTypeList.stream().collect(Collectors.toMap(
				(Tuple3<Class<?>, String, TGElementType> tuple) -> tuple.B   , 
				(Tuple3<Class<?>, String, TGElementType> tuple) -> tuple.A));
		
		elementGetterKeyDict = elementGetterMap.keySet().stream().collect(Collectors.groupingBy(
				(Tuple2<String, String> tuple) -> tuple.A));
		
		
		// Check missing setters and getters for simple properties in all classes
		Vector<String> errorMsgs = new Vector<>();
		Set<Tuple2<String, String>> getterKeysSet = simplePropertyGetterMap.keySet();
		Set<Tuple2<String, String>> setterKeysSet = simplePropertySetterMap.keySet();
		getterKeysSet.stream().filter(getterTuple -> !setterKeysSet.contains(getterTuple))
			.map(tuple -> String.format("Getter for %s.%s has no corresponding setter.", tuple.A, tuple.B))
			.forEach(errorMsgs::add);
		setterKeysSet.stream().filter(setterTuple -> !getterKeysSet.contains(setterTuple))
		.map(tuple -> String.format("Setter for %s.%s has no corresponding getter.", tuple.A, tuple.B))
		.forEach(errorMsgs::add);
		
		if (errorMsgs.size() > 0) {
			String errMsg = errorMsgs.stream().collect(Collectors.joining("\r\n", "\r\n", ""));
			throw new RuntimeException(errMsg);
		}
		
		/* 
		 * Converting a list of tuples in form of ((className, propName), (porpIndex, firstLine) )
		 * into a map of className -> List< ((className, propName), (porpIndex, firstLine) ) >
		 */
		simplePropertyIndexMap = simplePropertyIndexList.stream()
				.collect(Collectors.groupingBy(tuple -> tuple.A.A));
	}
	
	public void processClassAnnotations(Class<? extends TrainGraphPart> clazz, 
			LinkedList<Class<? extends TrainGraphPart>> classList, 
			HashSet<Class<? extends TrainGraphPart>> processedClassSet) {
		
		// If the class is annotated with @TGElement.
		TGElementType tgeType = (TGElementType) clazz.getAnnotation(TGElementType.class);
		if (tgeType != null) {
			Constructor<? extends TrainGraphPart> defaultConstructor = findDefaultConstructor(clazz);
			Supplier<TrainGraphPart> creator = () -> TrainGraphPart.newInstance(defaultConstructor);
			
			DEBUG_MSG("Register class '%s' as %s", tgeType.name(), clazz.getName());
			registeredTypeList.add(Tuple3.oF(clazz, tgeType.name(), tgeType));
			elementCreatorMap.put(Tuple2.oF("", tgeType.name()), 
					Tuple2.oF(clazz, (Supplier<? extends Object>) creator));
			registeredTypeCreatorDict.put(clazz, creator);
		} else {
			throw new RuntimeException(String.format("Class %s must be annotabed with @TGElementType annotation.", 
					clazz.getName()));
		}
		
		int[] propIndex = {0};

		// Find declared fields and inherited public fields as simple properties.
		Vector<Field> fields = new Vector<>();
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		fields.addAll(Arrays.asList(clazz.getFields()));
		fields.stream().distinct().forEach(field-> {
			TGProperty tp = field.getAnnotation(TGProperty.class);
			TGElement te = field.getAnnotation(TGElement.class);
			if (tp != null)
				processFieldAsSimpleProperty(clazz, tp, field, ++ propIndex[0]);
			else if (te != null)
				processFieldAsElement(clazz, te, field, classList, processedClassSet, 
						++ propIndex[0]);
		});
		
		// Find public methods as simple property getters and setters
		Vector<Method> methods = new Vector<>();
		methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		methods.addAll(Arrays.asList(clazz.getMethods()));
		methods.stream().distinct().forEach(method -> {
			TGProperty tp = method.getAnnotation(TGProperty.class);
			TGElement te = method.getAnnotation(TGElement.class);
			if (tp != null)
				processMethodAsSimpleProperty(clazz, tp, method, ++ propIndex[0]);
			else if (te != null)
				processMethodAsElement(clazz, te, method, classList, processedClassSet, 
						++ propIndex[0]);
		});
	}
	
	private void processFieldAsSimpleProperty(Class clazz, TGProperty tp, 
			Field field, int propIndex) {	
		
		String className = clazz.getName();	
		if (tp == null)
			return;
		
		String fieldName = "".equals(tp.name()) ? field.getName() : tp.name();
		Class<?> fieldClass = field.getType();
		Tuple2<String, String> propTuple = Tuple2.oF(className, fieldName);
		field.setAccessible(true);
		
		// field is a simple property
		simplePropertyGetterMap.put(propTuple, tgp -> {
			Object value;
			try {
				value = field.get(tgp);
				return value == null ? "" : (String) ValueTypeConverter.convertType(value, 
						fieldClass, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		});
		simplePropertySetterMap.put(propTuple, (tgp, strValue) -> {
			Object value = ValueTypeConverter.convertType(strValue, 
					String.class, fieldClass);
			try {
				field.set(tgp, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		DEBUG_MSG("Register Simple property field %s.'%s' in %s type", className, 
				fieldName, fieldClass.getName());
		
		int newPropIndex = tp.index() == Integer.MAX_VALUE ? propIndex : tp.index();
		Tuple2 attrTuple = Tuple2.oF(newPropIndex, tp.firstline());
		simplePropertyIndexList.add(Tuple2.oF(propTuple, attrTuple));
	}
	
	private void processFieldAsElement(Class clazz, TGElement te, 
			Field field, LinkedList<Class<? extends TrainGraphPart>> classList, 
			HashSet<Class<? extends TrainGraphPart>> processedClassSet, 
			int propIndex) {	
		
		String className = clazz.getName();	
		if (te == null)
			return;
		
		String fieldName = field.getName();
		String propName = "".equals(te.name()) ? fieldName : te.name();
		Class fieldClass = field.getType();
		Class elementClass = fieldClass;
		Tuple2<String, String> propTuple = Tuple2.oF(className, propName);
		String elementType;
		field.setAccessible(true);
		
		TGElementAttr tea = TGElementAttr.fromAnnotation(te);
		tea.setName(propName);
		tea.setIndex(te.index() == Integer.MAX_VALUE ? propIndex : te.index());
		
		Function<TrainGraphPart, Object> getter = tgp -> {
			Object value;
			try {
				value = field.get(tgp);
				return value;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		};
		BiConsumer<TrainGraphPart, Object> setter = (tgp, value) -> {
			try {
				field.set(tgp, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		};
		
		
		if (te.isList()) {
			// List type
			
			elementType = "List property";
			elementClass = tea.type();
			checkListElementType(fieldClass, elementClass, field.getName(), className,
					propTuple, true, false, classList, processedClassSet);
		} else {
			// Object property type

			elementType = "Object property";
			checkObjectElementType(elementClass, field.getName(), className, 
					true, false, classList, processedClassSet);
		}
		
		DEBUG_MSG("Register %s field %s.'%s' as %s", elementType, className, 
				propName, fieldClass.getName());
		elementGetterMap.put(propTuple, Tuple3.oF(tea, elementClass, getter));
		elementSetterMap.put(propTuple, Tuple3.oF(tea, elementClass, setter));
		
	}
	
	private void processMethodAsSimpleProperty(Class<? extends TrainGraphPart> clazz,
			TGProperty tp, Method method, int propIndex) {

		String className = clazz.getName();
		if (tp != null) {
			String methodName = method.getName();
			String propName;
			if ("".equals(tp.name())) {
				propName = methodName.replace("get", "").replace("set", "");
				if (propName.length() > 1)
					propName = propName.substring(0, 1).toLowerCase() + propName.substring(1);				
			} else {
				propName = tp.name();
			}
			
			Tuple2<String, String> propTuple = Tuple2.oF(className, propName);
			method.setAccessible(true);
			
			// Setters
			if (methodName.toLowerCase().startsWith("set")) {
				if (method.getParameterCount() != 1) {
					throw new RuntimeException(String.format("The %s method in %s class with "
							+ "@TGProperty should take and only take one paramter.",
							methodName, className));
				}
				Class<?> propClass = method.getParameterTypes()[0];
				String setterPropClass = propClass.getName();
				String getterPropClass = PropertyTypeMap.get(propTuple);
				if (getterPropClass == null) {
					PropertyTypeMap.put(propTuple, setterPropClass);
				} else if (!setterPropClass.equals(getterPropClass)) {
					throw new RuntimeException(String.format("The type of first argument of method %s in class %s with "
							+ "@TGProperty should be %s, which is the return type of corresponding getter method.",
							methodName, className, getterPropClass));
				}
				
				simplePropertySetterMap.put(propTuple, (tgp, strValue) -> {
					Object value = ValueTypeConverter.convertType(strValue, 
							String.class, propClass);
					try {
						method.invoke(tgp, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				
				
			}
			
			// Getters
			else if (methodName.toLowerCase().startsWith("get")) {
				if (method.getParameterCount() != 0) {
					throw new RuntimeException(String.format("The %s method in %s class with "
							+ "@TGProperty should take no paramter.",
							methodName, className));
				}
				Class<? extends Object> propClass = method.getReturnType();
				String getterPropClass = propClass.getName();
				String setterPropClass = PropertyTypeMap.get(propTuple);
				if (setterPropClass == null) {
					PropertyTypeMap.put(propTuple, getterPropClass);
				} else if (!setterPropClass.equals(getterPropClass)) {
					throw new RuntimeException(String.format("The return type of method %s in class %s with "
							+ "@TGProperty should be %s, which is the type of the first argument of corresponding setter method.",
							methodName, className, setterPropClass));
				}
				
				simplePropertyGetterMap.put(propTuple, tgp -> {
					Object value;
					try {
						value = method.invoke(tgp);
						return value == null ? "" : (String) ValueTypeConverter.convertType(value, 
								propClass, String.class);
					} catch (Exception e) {
						e.printStackTrace();
						return "";
					}
				});

				int newPropIndex = Math.min(propIndex, tp.index());
				Tuple2 attrTuple = Tuple2.oF(newPropIndex, tp.firstline());
				simplePropertyIndexList.add(Tuple2.oF(propTuple, attrTuple));
				
				
			}
			
		}
	}
	
	private void processMethodAsElement(Class<? extends TrainGraphPart> clazz,
			TGElement te, Method method, LinkedList<Class<? extends TrainGraphPart>> classList, 
			HashSet<Class<? extends TrainGraphPart>> processedClassSet, 
			int propIndex) {

		
		String className = clazz.getName();	
		if (te == null)
			return;

		String methodName = method.getName();
		boolean isSetter = methodName.startsWith("set");
		boolean isGetter = methodName.startsWith("get");
		if (!isSetter && !isGetter)
			throw new RuntimeException(String.format("Method %s.%s annotated with @TGElement has to "
					+ "be a setter or a getter, i.e. its name must starts with 'get' or 'set'.",
					className, methodName));
		
		String propName = te.name();
		if ("".equals(te.name())) {
			propName = methodName.replace("get", "").replace("set", "");
			if (propName.length() > 1)
				propName = propName.substring(0, 1).toLowerCase() + propName.substring(1);				
		}
		Tuple2<String, String> propTuple = Tuple2.oF(className, propName);
		TGElementAttr tea = TGElementAttr.fromAnnotation(te);
		tea.setName(propName);
		tea.setIndex(te.index() == Integer.MAX_VALUE ? propIndex : te.index());
		method.setAccessible(true);
		
		Function<TrainGraphPart, Object> getter = null;
		BiConsumer<TrainGraphPart, Object> setter = null;
		Supplier<? extends Object> creator = null;
		Class<?> propClass = null;
		String elementType;
		
		if (isSetter) {
			if (method.getParameterCount() != 1) {
				throw new RuntimeException(String.format("The %s method in %s class with "
						+ "@TGElement should take and only take one paramter.",
						methodName, className));
			}
			propClass = method.getParameterTypes()[0];
			String setterPropClass = propClass.getName();
			String getterPropClass = PropertyTypeMap.get(propTuple);
			if (getterPropClass == null) {
				PropertyTypeMap.put(propTuple, setterPropClass);
			} else if (!setterPropClass.equals(getterPropClass)) {
				throw new RuntimeException(String.format("The type of first argument of method %s in class %s with "
						+ "@TGElement should be %s, which is the return type of corresponding getter method.",
						methodName, className, getterPropClass));
			}
			
			setter = (tgp, value) -> {
				try {
					method.invoke(tgp, value);
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			};
		} else if (isGetter) {
			if (method.getParameterCount() != 0) {
				throw new RuntimeException(String.format("The %s method in %s class with "
						+ "@TGElement should take no paramter.",
						methodName, className));
			}
			propClass = method.getReturnType();
			String getterPropClass = propClass.getName();
			String setterPropClass = PropertyTypeMap.get(propTuple);
			if (setterPropClass == null) {
				PropertyTypeMap.put(propTuple, getterPropClass);
			} else if (!setterPropClass.equals(getterPropClass)) {
				throw new RuntimeException(String.format("The return type of method %s in class %s with "
						+ "@TGElement should be %s, which is the type of the first argument of corresponding setter method.",
						methodName, className, setterPropClass));
			}
			getter = tgp -> {
				Object value;
				try {
					value = method.invoke(tgp);
					return value;
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
					return null;
				}
			};
		}
		Class<?> elementClass = propClass;
		
		if (te.isList()) {
			// List type
			
			elementType = "List property";
			elementClass = tea.type();
			checkListElementType(propClass, elementClass, methodName, className,
					propTuple, false, isSetter, classList, processedClassSet);
		} else {
			// Object property type

			elementType = "Object property";
			checkObjectElementType(elementClass, methodName, className, 
					false, isSetter, classList, processedClassSet);
		}
		
		String methodType = isGetter ? "getter" : "setter";
		DEBUG_MSG("Register %s %s %s.'%s' as %s", elementType, methodType, className, 
				propName, propClass.getName());
		if (isGetter)
			elementGetterMap.put(propTuple, Tuple3.oF(tea, elementClass, getter));
		else
			elementSetterMap.put(propTuple, Tuple3.oF(tea, elementClass, setter));
	}
	
	@SuppressWarnings("unchecked")
	private void checkListElementType(Class listClass, Class elementClass, 
			String memberName, String className, Tuple2<String, String> propTuple, 
			boolean memberIsField, boolean memberIsSetter, 
			LinkedList<Class<? extends TrainGraphPart>> classList, 
			HashSet<Class<? extends TrainGraphPart>> processedClassSet) {
		
		boolean isListType = java.util.List.class.isAssignableFrom(listClass);
		
		if (!isListType) 
			if (memberIsField)
				throw new RuntimeException(String.format("The type of field %s.%s anotated with "
					+ "@TGElement should be a subclass of java.util.List<? extends %s>.",
					className, memberName, TrainGraphPart.class.getName()));
			else {
				String typePosition = memberIsSetter ? "parameter" : "return";
				throw new RuntimeException(String.format("The %s type of method %s.%s anotated with "
					+ "@TGElement should be a subclass of java.util.List<? extends %s>.",
					typePosition, className, memberName, TrainGraphPart.class.getName()));
			}
				
		
		// Default value is used
		if (elementClass.equals(TrainGraphPart.class)) 
			if (memberIsField)
				throw new RuntimeException(String.format("Field %s.%s anotated with "
					+ "@TGElement should has annotation attribute type set to the type parameter in "
					+ "java.util.List<? extends %s>.",
					className, memberName, TrainGraphPart.class.getName()));
			else
				throw new RuntimeException(String.format("Method %s.%s anotated with "
					+ "@TGElement should has annotation attribute type set to the type parameter in "
					+ "java.util.List<? extends %s>.",
					className, memberName, TrainGraphPart.class.getName()));
		
		if (!processedClassSet.contains(elementClass)) {
			processedClassSet.add(elementClass);
			classList.addLast(elementClass);
		}
		
		// List types will not have corresponding creator item in creator map, since
		// List type is not a subclass of TrainGraphPart, and thus will not be processed
		// in processClassANnotaitons method.
		// As a result, an item should be created for the list property/
		if (memberIsSetter) {
			Supplier<Object> creator = () -> {
				try {
					return listClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(String.format("Cannot create list property %s for class %s",
							propTuple.B, className), e);
				}
			};
			elementCreatorMap.put(propTuple, Tuple2.oF(listClass, creator));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void checkObjectElementType(Class elementClass, String memberName, String className, 
			boolean memberIsField, boolean memberIsSetter, 
			LinkedList<Class<? extends TrainGraphPart>> classList, 
			HashSet<Class<? extends TrainGraphPart>> processedClassSet) {
		
		
		boolean typeMatch = TrainGraphPart.class.isAssignableFrom(elementClass);
		
		if (!typeMatch) 
			if (memberIsField)
				throw new RuntimeException(String.format("The type of field %s.%s anotated with "
					+ "@TGElement should be a subclass of %s.",
					className, memberName, TrainGraphPart.class.getName()));
			else {
				String typePosition = memberIsSetter ? "paramter" : "return";
				throw new RuntimeException(String.format("The %s type of method %s.%s anotated with "
					+ "@TGElement should be a subclass of %s.",
					typePosition, className, memberName, TrainGraphPart.class.getName()));
			}

		if (!processedClassSet.contains(elementClass)) {
			processedClassSet.add(elementClass);
			classList.addLast(elementClass);
		}
	}
	
	// }}

	
	// {{ Save file

	
	public void saveToFile(String fileName) throws IOException {
		FileOutputStream fs = new FileOutputStream(fileName);
		try {
			saveToStream(fs);
		} catch (IOException e) {
			throw e;
		} finally {
			if (fs != null) {
				try { fs.close(); } catch (Exception e) {}
			}
		}	
		
		System.gc();
	}
	
	public void saveToStream(OutputStream out) throws IOException {
		Writer writer = new OutputStreamWriter(out, "utf-8");
		saveToWriter(writer, 0, true);
		writer.flush();
	}
	
	public void saveToStream(OutputStream out, int identLevel) throws IOException {
		Writer writer = new OutputStreamWriter(out, "utf-8");
		saveToWriter(writer, identLevel, true);
		writer.flush();
	}
	
	protected void saveToWriter(Writer writer, int identLevel, boolean printIdentOnFirstLine) 
			throws IOException {
		
		Vector<ET> elements = null;
		boolean elementPrinted = false;
		// Write section begin string
		if (printIdentOnFirstLine)
			_printIdent(writer, identLevel, false);
		_print(writer, "%s {%s", getElementName(), isInOneLine() ? "" : NEW_LINE_STR);
		
		if (isBase64Encoded()) {
			// Write base64 codes for binary contents
			
			writer.append( encodeToBase64() );
			_println(writer);
		} else {			
			// Write simple properties
			saveSimplePropertiesToWriter(writer, identLevel, isInOneLine());
			
			// Write element properties recursively
			elementPrinted = saveElementPropertiesToWriter(writer, identLevel, 
					NEW_LINE_STR, NEW_LINE_STR, "", true, 
					(element, te, eIdentLevel, printIdentOnFirstLine0) -> {
				try {
					element.saveToWriter(writer, eIdentLevel, printIdentOnFirstLine0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		
		// Write section end string
		if (!isInOneLine() && !elementPrinted) {
			_println(writer);
		}
		_printIdent(writer, identLevel, isInOneLine());
		_print(writer, "}");
	}
	
	private void saveSimplePropertiesToWriter(Writer writer, int identLevel, boolean inOneLine) {
		String className = getClass().getName();
		List<Tuple2<Tuple2<String, String>, Tuple2<Integer, Boolean> >> propKeyList= simplePropertyIndexMap.get(className);
		if (propKeyList == null) {
			DEBUG_MSG("There is no fileds/accessor registered as simple property "
					+ "in %s class with @SimpleProperty", className);
			return;
		}

//		_printIdent(writer, identLevel + 1, inOneLine);
//		_println(writer);
		_printIdent(writer, identLevel + 1, inOneLine);
		
		// A flag set up in sorting order and used in forEach
		boolean[] boolFlags = {false, false}; // {hasFirstLineProperties, lineBreakPrinted}
		int[] intFlags = {0, propKeyList.size() - 1}; // [iterationIndex, iterationMaxCount]
		propKeyList.stream().sorted((tuple1, tuple2) -> {
			// tuple.A is (className, propName), tuple.B is (propIndex, isFirstLine)
			Tuple2<Integer, Boolean> prop1Attr = tuple1.B;
			Tuple2<Integer, Boolean> prop2Attr = tuple2.B;
			
			if (prop1Attr.B || prop2Attr.B)
				// Set has isFirstLine=true flag to true
				boolFlags[0] = true;
			
			/*
			 * Boolean.compareTo makes false smaller than true, so 
			 * compare in the reverse order to make true comes first.
			 */
			int firstLineSort = prop2Attr.B.compareTo(prop1Attr.B);
			int indexSort = prop1Attr.A - prop2Attr.A;
			
			// Sort by isFirstLine first, and then property index.
			return firstLineSort != 0 ? firstLineSort : indexSort;
			
		}).forEach(tuple-> {
			// tuple.A is (className, propName), tuple.B is (propIndex, isFirstLine)
			Tuple2<String, String> propTuple = tuple.A;
			Tuple2<Integer, Boolean> propAttr = tuple.B;
			
			if (!inOneLine) {
				// If there are firstline=true properties
				if (boolFlags[0]) {
					// Encounter a firstline=false properties and line break is not printed yet.
					if (!propAttr.B && !boolFlags[1]) {
						boolFlags[1] = true;
						_println(writer);
						_printIdent(writer, identLevel + 1, inOneLine);
					}
				}
				
			}
			
			String propValue = getSimpleProperty(propTuple);
			propValue = _encode(propValue);
			
			_print(writer, "%s=%s", propTuple.B, propValue);

			// ++ index and print , between properties.
			if (intFlags[0] ++ != intFlags[1])
				_print(writer, ", ");
			
			// ++ index
//			++ intFlags[0];
		});
	}
	
	private String getSimpleProperty(Tuple2<String, String> propTuple) {
		Function<TrainGraphPart, String> getter = simplePropertyGetterMap.get(propTuple);
		
		if (getter != null) {
			return getter.apply(this);
		} else {
			return "";
		}
	}

	private boolean saveElementPropertiesToWriter(Writer writer, int identLevel,
			String prefix, String delimiter, String suffix, boolean printLnAfterElement,
			MultiConsumer<TrainGraphPart, TGElementAttr, Integer, Boolean> elementPrintAction) {
		
		boolean elementPrinted = false;
		String newLineStrAfterElement = printLnAfterElement ? NEW_LINE_STR : "";
		
		List<Tuple2<String, String>> propKeyList = 
			elementGetterKeyDict.get(getClass().getName());
		if (propKeyList != null && propKeyList.size() > 0) {
			elementPrinted = true;
			_print(writer, prefix);
			
			int[] flags = {0, propKeyList.size() - 1};
			propKeyList.stream().map(
					propKey -> elementGetterMap.get(propKey))
				.sorted((propTuple1, propTuple2) -> propTuple1.A.index() - propTuple2.A.index())
				.forEach(propTuple -> {
					TGElementAttr tea = propTuple.A;
					Function<TrainGraphPart, Object> getter = propTuple.C;
					if (tea.isList()) {
						// List property
						@SuppressWarnings("unchecked")
						List<Object> elementList = (List<Object>) getter.apply(this);
						if (printLnAfterElement)
							_printIdent(writer, identLevel + 1, false);
						_print(writer, "%s = %d[%s", tea.name(), elementList.size(), 
								newLineStrAfterElement);
						
						for (int listItemIndex = 0; listItemIndex < elementList.size(); ++listItemIndex) {
							TrainGraphPart element = (TrainGraphPart) elementList.get(listItemIndex);
							elementPrintAction.accept(element, tea, identLevel + 2, true);
							
							if (printLnAfterElement)
								_println(writer);
						}

						if (printLnAfterElement)
							_printIdent(writer, identLevel + 1, false);
						_print(writer, "]%s", printLnAfterElement ? NEW_LINE_STR : "");
					} else {
						// object property
						
						if (printLnAfterElement)
							_printIdent(writer, identLevel + 1, false);
						_print(writer, "%s = ", tea.name());
						TrainGraphPart element = (TrainGraphPart) getter.apply(this);
						elementPrintAction.accept(element, tea, identLevel + 1, false);
					}
					
					 // index != size -1
					if (flags[0] ++ != flags[1]) {
						_print(writer, delimiter);
					}
				});
			
			_print(writer, suffix);
		}
		
		return elementPrinted;
	}
	
	// }}
	
	// {{ Load from file
	
	/**
	 * Load a train graph part from a reader.
	 * @param reader0
	 * @param clazz The top level train graph part to be loaded.
	 * @param loadingNode The top level train graph part instance to be loaded into.
	 * Users should initialize or reset the instance by themsleves on need.
	 * @return The loaded instance. 
	 * @throws IOException
	 */
	static TrainGraphPart loadFromReader(Reader reader0,
			Class<? extends TrainGraphPart> clazz, TrainGraphPart loadingNode)
			throws IOException {
		
		BufferedReader reader = new BufferedReader(reader0);
		
		TrainGraphPart root = null;
		TrainGraphPart classObjRoot = null;
		String line = null;
		int lineNum = 0;
		// A stack objects being parsed. Each element is (object, name of object)
		Stack<Tuple2<TrainGraphPart, Tuple2<String, String>>> parsingNodeStack = new Stack<>();
		Vector<String> errMsgs = new Vector<> ();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (classObjRoot == null) {
				root = parseLine(line, ++lineNum, parsingNodeStack, errMsgs, loadingNode);
			} else {
				root = parseLine(line, ++lineNum, parsingNodeStack, errMsgs, null);
			}
			
			if (classObjRoot == null && clazz.isInstance(root)) {
				classObjRoot = root;
			}
		}
		
		reader.close();
		
		if (clazz != null)
			return classObjRoot;
		else
			return root;
	}
	
	protected static TrainGraphPart parseLine(String line, int lineNum, 
			Stack<Tuple2<TrainGraphPart, Tuple2<String, String>>> parsingNodeStack, Vector<String> errMsgs, 
			TrainGraphPart loadingNode) {
		
		TrainGraphPart parentPart = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek().A;
		
		// Step 1. try to interpret this line as a start of an object.
		TrainGraphPart thisObj = createObject(line, lineNum, parsingNodeStack, 
				errMsgs, loadingNode);
		
		if (thisObj instanceof UnknownPart)
			return thisObj;
		
		if (parentPart != null) {
			if (parentPart instanceof UnknownPart) {
				// skip current line.
			} if (parentPart.isBase64Encoded()) {
				parentPart.decodeFromBase64NewLine(line);
			} else {
				
				// Step 2. try to find "name=value," patterns for properties
				TrainGraphPart objToBeAssigned = parsingNodeStack.peek().A;
				if (objToBeAssigned != null) {
					String propLine = line.replaceFirst("^[^\\{]*\\{", "").replaceFirst("\\}.*", "");
					String[] assignments = propLine.split(",");
					for (String assignment : assignments) {
						String[] strParts = assignment.split("=");
						if (strParts.length >= 2) {
							objToBeAssigned.setSimpleProperty(objToBeAssigned, 
									strParts[0], _decode(strParts[1]));
						} else {
							objToBeAssigned.setSimpleProperty(objToBeAssigned, 
									strParts[0], "");
						}
					}
				}
				
				// Step 3. try to read as an object property or an element.
//				if (thisObj != null && parentPart != null) {
//					setElementProperty(parentPart, thisObj);
					
//					if (parentPart.isOfElementType(thisObj)) {
//						// Add thisObj as an element of parantPart
//						parentPart.addTGPElement(thisObj);
//					} else {
//						// Set thisObj as an object property of parantPart
//						parentPart.setObjectTGPProperties(thisObj);
//					}
//				}
			}
		}
		
		finishObject(line, lineNum, parsingNodeStack, errMsgs);
		
		return parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek().A;
	}
	
	private void setSimpleProperty(TrainGraphPart obj, String propName, String valueInStr) {
		Tuple2 propTuple = Tuple2.oF(obj.getClass().getName(), propName);
		BiConsumer<TrainGraphPart, String> setter = simplePropertySetterMap.get(propTuple);
		
		if (setter != null) {
			setter.accept(obj, valueInStr);
		}
	}
	
	private static TrainGraphPart createObject(String line, int lineNum, 
			Stack<Tuple2<TrainGraphPart, Tuple2<String, String>>> parsingNodeStack, 
			Vector<String> errMsgs, 
			TrainGraphPart loadingNode) {
		
		// Part object starts with a "{". If no brace found, then no object is created.
		if (!line.contains("{"))
			return null;

		TrainGraphPart obj = null;
		TrainGraphPart stackTop = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek().A;
		String parentName = stackTop == null ? "" : stackTop.getClass().getName();
		String propName = line.replaceFirst("\\{.*", "").trim();
		
		if (stackTop != null && stackTop instanceof UnknownPart) {
			// If the current scope is an unknown part, then treat current part
			// as an unknown part too, because there is no need to parse the content
			// of an unknown part.
			obj = new UnknownPart();
		} 
		else {
			Tuple2<Class<?>, Supplier<? extends Object>> creatorTuple = 
					elementCreatorMap.get(Tuple2.oF(parentName, propName));
			
			if (creatorTuple != null) {
				/* If loadingNode is not null and it is an instance of corresponding
				 * class to current line being parsed, then use it as the current object
				 * instead of creating new one.
				 */
				if (loadingNode != null && creatorTuple.A.isInstance(loadingNode)) {
					if (loadingNode.isBase64Encoded()) {
						loadingNode.decodeFromBase64Start();
					}
					obj = loadingNode;
				} else {
					Class<?> clazz = creatorTuple.A;
					String className = clazz.getName();
					obj = (TrainGraphPart) creatorTuple.B.get();
					
					// Failed to create an instance.
					if (obj == null) {
						obj = createUnknownPartint(line, lineNum, parsingNodeStack, errMsgs);
						((UnknownPart) obj).message = 
								String.format(__("Cannot create an instance of %s class. "
								+ "This object and all its content is skipped."), className);
					} else {
	//					obj.initElements();
						if (obj.isBase64Encoded()) {
							obj.decodeFromBase64Start();
						}
					}
				}
			} else {
				// A top level unknown part encountered during parsing.
				obj = createUnknownPartint(line, lineNum, parsingNodeStack, errMsgs);
			}
		}	
		
		if (obj != null) {
			parsingNodeStack.push(Tuple2.oF(obj, Tuple2.oF(parentName, propName)));
		}
		
		return obj;
	}
	
	private static void finishObject(String line, int lineNum, 
			Stack<Tuple2<TrainGraphPart, Tuple2<String, String>>> parsingNodeStack, 
			Vector<String> errMsgs) {
		
		if (!"}".equals(line))
			return;
		
//		if (parsingNodeStack.isEmpty())
//			throw new RuntimeException()
		TrainGraphPart obj = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek().A;
		if (obj != null && obj.isBase64Encoded()) {
			obj.decodeFromBase64End();
		}
		
//		TrainGraphPart part = parsingNodeStack.pop();
//		part.loadComplete();
		
		// Object loading complete.
		
	}
	
	private static UnknownPart createUnknownPartint(String line, int lineNum, 
			Stack<Tuple2<TrainGraphPart, Tuple2<String, String>>> parsingNodeStack, Vector<String> errMsgs) {

		UnknownPart obj = new UnknownPart();
		obj.startLineIndex = lineNum;
		obj.startLine = line;
		obj.topLevel = true;
		
		return obj;
		
	}
	
	// }}
	
	// {{ 辅助方法, encode, decode, print...
	
	private static String _encode(String str) {
		return str == null ? null : str.replace(',', '`').replace('=', '|');
	}
	
	private static String _decode(String str) {
		return str == null ? null : str.replace('`', ',').replace('|', '=');		
	}
	
	private void _print(Writer writer, String msg) {
		try {
			writer.append(msg);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void _print(Writer writer, String formatStr, Object... params)  {
		try {
			writer.append(String.format(formatStr, params));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void _println(Writer writer) {
		try {
			writer.append(NEW_LINE_STR);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}	
	
	private boolean isInOneLine() {		
		return typeToAttrDict.get(getClass()).printInOneLine();
	}
	
	private void _printIdent(Writer writer, int identLevel, boolean notNeedIdent) {
		try {
			if (notNeedIdent)
				return;
			
			while (identLevel -- > 0) {
				writer.append(IDENT_STR);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	// }}
	
	// {{ toString 方法, repr方法及其相关方法
	
	public static void setSimpleToString() {
		TO_STRING_ELEMENT_DEPTH = -1;
		
		// Init for toDebugString
		TO_STRING_SHOW_TYPE = true;
		TO_STRING_SHOW_PROPERTIES = false;
		TO_STRING_SPECIFIC_TYPE_DEPTH.put(AllTrains.class.getName(), 0);
		TO_STRING_SPECIFIC_TYPE_DEPTH.put(RailroadLineChart.class.getName(), 0);
		TO_STRING_SPECIFIC_TYPE_DEPTH.put(RailroadLine.class.getName(), 0);
	}
	
	public static void setDebugToString() {
		TO_STRING_ELEMENT_DEPTH = 2;
		TO_STRING_SHOW_TYPE = false;
		TO_STRING_SHOW_PROPERTIES = false;
	}
	
	public static void setFullToString() {
		TO_STRING_ELEMENT_DEPTH = 100;
		TO_STRING_SHOW_TYPE = true;
		TO_STRING_SHOW_PROPERTIES = true;
	}

	
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof TrainGraphPart && ((TrainGraphPart) o)._id == _id &&
				getClass().equals(o.getClass());
				
	}
	
	@Override
	public int hashCode() {
		return _id;
	}
	
	@Override 
	public String toString() {
		return repr(TO_STRING_ELEMENT_DEPTH, TO_STRING_SHOW_TYPE, TO_STRING_SHOW_PROPERTIES);
	}
	
	public String toDebugString() {
		return repr(TO_STRING_SPECIFIC_TYPE_DEPTH.getOrDefault(
				getElementName(), 3), false, false);
	}
	
	public String repr(int elementDepth, boolean showType, boolean showProperties) {

		StringWriter writer = new StringWriter();
		
		repr0(writer, elementDepth, showType, showProperties);
		
		return writer.toString();
	}
	
	private void repr0(Writer writer, int elementDepth, 
			boolean showType, boolean showProperties) {
		
		if (elementDepth < 0) {
			_print(writer, getName());
		} else {
			if (showType) {
				_print(writer, getElementName().replace("{\r\n", "{"));
			} else {
				_print(writer, "{");
			}
			
			// Print simple properties
			if (showProperties) {
				_print(writer, "id=" + _id + ",");
				saveSimplePropertiesToWriter(writer, 0, true);
			} else {
				if (showType) {
					_print(writer, String.format("id=%d,name=%s", _id, getName()));
				} else {
					_print(writer, getName() + "@" + _id);
				}
			}
			
			String propertiesRepr=null, elementsRepr=null;
			
			// Print element properties recursively
			boolean elementPrinted = saveElementPropertiesToWriter(
					writer, 0, ": ", ", ", "", false, 
					(element, te, eIdentLevel, printIdentOnFirstLine0) -> {
					
				_print(writer, "%s=", te.name());
				element.repr0(writer, TO_STRING_SPECIFIC_TYPE_DEPTH.getOrDefault(
					element.getClass().getName(), elementDepth - 1),
				showType, showProperties);
			});
			
			_print(writer, "}");
		}
	}
	
	// }}

}
