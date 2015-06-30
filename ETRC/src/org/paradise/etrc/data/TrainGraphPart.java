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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.paradise.etrc.ETRCUtil;
import org.paradise.etrc.data.annotation.AnnotationException;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementAttr;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.annotation.TGProperty;
import org.paradise.etrc.data.v1.NullPart;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.util.data.Tuple2;
import org.paradise.etrc.util.data.Tuple3;
import org.paradise.etrc.util.data.ValueTypeConverter;
import org.paradise.etrc.util.function.MultiConsumer;

import static org.paradise.etrc.ETRC.__;


/**
 * 所有组成运行图的部件的基类
 * ET是该类的元素的类型
 * @author Jeff Gong
 *
 */
@SuppressWarnings("rawtypes")
public abstract class TrainGraphPart {
	
	
	static boolean SHOW_DEBUG_MESSAGE_ANO = "true".equalsIgnoreCase(System.getenv("TGP-ANO-DEBUG"));
	static boolean SHOW_DEBUG_MESSAGE_SAVE = "true".equalsIgnoreCase(System.getenv("TGP-SAVE-DEBUG"));
	static boolean SHOW_DEBUG_MESSAGE_LOAD = "true".equalsIgnoreCase(System.getenv("TGP-LOAD-DEBUG"));

	static void DEBUG_MSG_ANO(String msgFormat, Object... msgArgs) {
		if (SHOW_DEBUG_MESSAGE_ANO)
			ETRCUtil.DEBUG_MSG(msgFormat, msgArgs);
	}
	
	static void DEBUG_MSG_SAVE(String msgFormat, Object... msgArgs) {
		if (SHOW_DEBUG_MESSAGE_SAVE)
			ETRCUtil.DEBUG_MSG(msgFormat, msgArgs);
	}
	
	static void DEBUG_MSG_LOAD(String msgFormat, Object... msgArgs) {
		if (SHOW_DEBUG_MESSAGE_LOAD)
			ETRCUtil.DEBUG_MSG(msgFormat, msgArgs);
	}
	
	// {{ Static fields
	
	public static int TO_STRING_ELEMENT_DEPTH = 0;
	public static HashMap<String, Integer> TO_STRING_SPECIFIC_TYPE_DEPTH = new HashMap<String, Integer> ();
//	public static HashMap<String, Integer> TO_DEBUG_STRING_SPECIFIC_TYPE_DEPTH = new HashMap<String, Integer> ();
	public static boolean TO_STRING_SHOW_TYPE = false;
	public static boolean TO_STRING_SHOW_PROPERTIES = false;
	
	public static final String IDENT_STR	= "  ";
	public static final String NEW_LINE_STR	= "\n";
		
	// Value is a 2-tuple (getter, propIsArray)
	protected static LinkedHashMap<Tuple2<String, String>, Tuple2<Function<TrainGraphPart, String>, Boolean> > 
		simplePropertyGetterMap =new LinkedHashMap<> ();
	// Value is a 2-tuple (setter, propIsArray)
	protected static LinkedHashMap<Tuple2<String, String>, Tuple2<BiConsumer<TrainGraphPart, String>, Boolean> > 
		simplePropertySetterMap = new LinkedHashMap<> ();
	protected static HashMap<Tuple2<String, String>, String> PropertyTypeMap =
			new HashMap<> ();
	/* a list of tuples in form of ((className, propName), (porpIndex, firstLine) ) */
	protected static Vector<Tuple2<Tuple2<String, String>, Tuple2<Integer,Boolean>>> 
		simplePropertyIndexList = new Vector<> ();
	/* a map whose key is className, and the value is a list of tuples 
	   in form of ((className, propName), (porpIndex, firstLine, isArray) ) */
	protected static Map<String, List<Tuple2<Tuple2<String, String>, Tuple2<Integer,Boolean> >>> simplePropertyIndexMap;
	
	protected static LinkedHashMap<Tuple2<String, String>, 
		Tuple3<TGElementAttr, Class<?>, Function<TrainGraphPart, Object>> > elementGetterMap = 
			new LinkedHashMap<> ();
	protected static LinkedHashMap<Tuple2<String, String>, 
	Tuple3<TGElementAttr, Class<?>, BiConsumer<TrainGraphPart, Object>> > elementSetterMap = 
		new LinkedHashMap<> ();
	protected static LinkedHashMap<String, Supplier<?> > elementCreatorMap = 
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
	
	public static String getElementName(Class clazz) {
		return typeToNameDict.get(clazz);
	}
	// }}
	
	// {{ 实例属性及抽象方法
	protected int _id;
	
	protected String name;
	protected TrainGraphPart root;
	protected TrainGraphPart parent;
	
	public int getID() { return _id; }
	@TGProperty(firstline=true)
	public String getName() { return name; }
	@TGProperty
	public void setName(String name) { this.name = name; }
	public TrainGraphPart getParent() { return parent; }
	@SuppressWarnings("unchecked")
	public <T> T getParent(Class<T> clazz) { return (T) parent; }
	public void setParent(TrainGraphPart parent) { this.parent = parent; }
	public TrainGraphPart getRoot() { return root; }
	@SuppressWarnings("unchecked")
	public <T> T getRoot(Class<T> clazz) { return (T) parent; }
	public void setRoot(TrainGraphPart root) { this.root = root; }
	
	/**************************************************************************
	 * Methods need to be implemented.
	 * Element data accessers which will be used in save/load template methods
	 **************************************************************************/
	
	public String getElementName() {
		return getElementName(getClass());
	}
	
	String createTGPNameById(int id) { return String.format("%s %d", getElementName(), id); }
	public void initElements() {}
	public void setToDefault() {}
	
	/* Binary coding */
	protected boolean isBase64Encoded() { return false; }
	protected void encodeToBase64(OutputStream out) throws IOException {}
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
			throw new IllegalArgumentException(__("Can not instantiate a class with NULL constructor."));
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
		Vector<AnnotationException> exceptions = new Vector<> ();
		
		processAnnotations(exceptions);
		
		if (exceptions.size() > 0) {
			int errorCount = exceptions.stream().mapToInt(e -> e.errorCount).sum();
			String msg = exceptions.stream().map(e -> e.getMessage())
					.collect(Collectors.joining(NEW_LINE_STR, 
							String.format("There are %d error%s found when parsing annotations:%s",
									errorCount, (errorCount > 1 ? "s" : ""), NEW_LINE_STR ), ""));
			System.err.println(msg);
		}
	}
	
	protected void processAnnotations(Vector<AnnotationException> exceptions) {		
		simplePropertyGetterMap.clear();
		simplePropertySetterMap.clear();
		simplePropertyIndexList.clear();
		elementGetterMap.clear();
		elementSetterMap.clear();
		elementCreatorMap.clear();
		
		LinkedList<Class<? extends TrainGraphPart>> classList = new LinkedList<> ();
		HashSet<Class<? extends TrainGraphPart>> processedClassSet = new HashSet<> ();
		Class<? extends TrainGraphPart> clazz = getClass(); 
		classList.addLast(NullPart.class);
		processedClassSet.add(NullPart.class);
		classList.addLast(clazz);
		processedClassSet.add(clazz);
		
		while (!classList.isEmpty()) {
			clazz = classList.pollFirst();
			processedClassSet.add(clazz);
			try {
				processClassAnnotations(clazz, classList, processedClassSet, exceptions);
			} catch (AnnotationException e) {
				exceptions.add(e);
			}
		}
		
		/* 
		 * Converting a list of tuples in form of ((className, propName), (porpIndex, firstLine) )
		 * into a map of className -> List< ((className, propName), (porpIndex, firstLine) ) >
		 */
		simplePropertyIndexMap = simplePropertyIndexList.stream()
				.collect(Collectors.groupingBy(tuple -> tuple.A.A));

		// Check missing setters and getters for simple properties in all classes
		try {
			checkMissingAccessor(simplePropertyGetterMap.keySet(), 
					simplePropertySetterMap.keySet());
		} catch (AnnotationException e) {
			exceptions.add(e);
		}

		// Check missing setters and getters for element properties in all classes
		try {
			checkMissingAccessor(elementGetterMap.keySet(), elementSetterMap.keySet());
		} catch (AnnotationException e) {
			exceptions.add(e);
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
	}
	
	public void processClassAnnotations(Class<? extends TrainGraphPart> clazz, 
			LinkedList<Class<? extends TrainGraphPart>> classList, 
			HashSet<Class<? extends TrainGraphPart>> processedClassSet,
			Vector<AnnotationException> exceptions) {
		
		// If the class is annotated with @TGElement.
		TGElementType tgeType = (TGElementType) clazz.getAnnotation(TGElementType.class);
		if (tgeType != null) {
			String typeName = tgeType.name().trim();
			Constructor<? extends TrainGraphPart> defaultConstructor = findDefaultConstructor(clazz);
			Supplier<TrainGraphPart> creator = () -> TrainGraphPart.newInstance(defaultConstructor);
			
			DEBUG_MSG_ANO("%sRegister class '%s' as %s", NEW_LINE_STR, typeName, clazz.getName());
			registeredTypeList.add(Tuple3.of(clazz, typeName, tgeType));
			elementCreatorMap.put(typeName, (Supplier<?>) creator);
			registeredTypeCreatorDict.put(clazz, creator);
		} else {
			throw new ParsingException(String.format("Class %s must be annotabed with @TGElementType annotation.", 
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
			try {
				if (tp != null)
					processFieldAsSimpleProperty(clazz, tp, field, ++ propIndex[0]);
				else if (te != null)
					processFieldAsElement(clazz, te, field, classList, processedClassSet, 
							++ propIndex[0]);
			} catch (AnnotationException e) {
				exceptions.add(e);
			}
		});
		
		// Find public methods as simple property getters and setters
		Vector<Method> methods = new Vector<>();
		methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		methods.addAll(Arrays.asList(clazz.getMethods()));
		methods.stream().distinct().forEach(method -> {
			TGProperty tp = method.getAnnotation(TGProperty.class);
			TGElement te = method.getAnnotation(TGElement.class);
			try {
			if (tp != null)
				processMethodAsSimpleProperty(clazz, tp, method, ++ propIndex[0]);
			else if (te != null)
				processMethodAsElement(clazz, te, method, classList, processedClassSet, 
						++ propIndex[0]);
			} catch (AnnotationException e) {
				exceptions.add(e);
			}
		});
	}
	
	private void processFieldAsSimpleProperty(Class clazz, TGProperty tp, 
			Field field, int propIndex) {	
		
		String className = clazz.getName();	
		if (tp == null)
			return;
		
		String tpName = tp.name().trim();
		String fieldName = "".equals(tpName) ? field.getName() : tpName;
		Class<?> fieldClass = field.getType();
		Tuple2<String, String> propTuple = Tuple2.of(className, fieldName);
		field.setAccessible(true);
		
		// field is a simple property
		Function<TrainGraphPart, String> getter = tgp -> {
			if (tgp == null) {
				throw new RuntimeException(String.format(__("Cannot get value of '%s' field from NULL element"),
						fieldName));
			}
			
			Object value;
			try {
				value = field.get(tgp);
			} catch (Exception e) {
				throw new RuntimeException(String.format(__("Cannot get value of %s field in %s class due to %s(%s)"), 
						fieldName, tgp.getClass().getName(),
						e.getClass().getName(), e.getMessage()));
			}
			
			return value == null ? "" : (String) ValueTypeConverter.convertType(value, 
					fieldClass, String.class);
		};
		BiConsumer<TrainGraphPart, String> setter = (tgp, strValue) -> {
			if (tgp == null) {
				throw new RuntimeException(String.format(__("Cannot set value of '%s' field for NULL element"),
						fieldName));
			}
			
			Object value = ValueTypeConverter.convertType(strValue, 
					String.class, fieldClass);
			try {
				field.set(tgp, value);
			} catch (Exception e) {
				throw new RuntimeException(String.format(__("Cannot set value of %s field in %s class due to %s(%s)"), 
						fieldName, tgp.getClass().getName(), 
						e.getClass().getName(), e.getMessage()));
			}
		};
		simplePropertyGetterMap.put(propTuple, Tuple2.of(getter, tp.isArray()));
		simplePropertySetterMap.put(propTuple, Tuple2.of(setter, tp.isArray()));
		
		DEBUG_MSG_ANO("Register Simple property field %s.'%s' in %s type", className, 
				fieldName, fieldClass.getName());
		
		int newPropIndex = tp.index() == Integer.MAX_VALUE ? propIndex : tp.index();
		Tuple2 attrTuple = Tuple2.of(newPropIndex, tp.firstline());
		simplePropertyIndexList.add(Tuple2.of(propTuple, attrTuple));
	}
	
	private void processFieldAsElement(Class clazz, TGElement te, 
			Field field, LinkedList<Class<? extends TrainGraphPart>> classList, 
			HashSet<Class<? extends TrainGraphPart>> processedClassSet, 
			int propIndex) {	
		
		String className = clazz.getName();	
		if (te == null)
			return;
		
		String fieldName = field.getName();
		String teName = te.name();
		String propName = "".equals(teName) ? fieldName : teName;
		Class fieldClass = field.getType();
		Tuple2<String, String> propTuple = Tuple2.of(className, propName);
		String elementType;
		field.setAccessible(true);
		
		TGElementAttr tea = TGElementAttr.fromAnnotation(te);
		tea.setName(propName);
		tea.setIndex(te.index() == Integer.MAX_VALUE ? propIndex : te.index());
		
		Function<TrainGraphPart, Object> getter = tgp -> {
			if (tgp == null) {
				throw new RuntimeException(String.format(__("Cannot get value of '%s' field from NULL element"),
						fieldName));
			}
			
			Object value;
			try {
				value = field.get(tgp);
				return value;
			} catch (Exception e) {
				throw new RuntimeException(String.format(__("Cannot get value of %s field in %s class due to %s(%s)"), 
						fieldName, tgp.getClass().getName(),
						e.getClass().getName(), e.getMessage()));
			}
		};
		BiConsumer<TrainGraphPart, Object> setter = (tgp, value) -> {
			if (tgp == null) {
				throw new RuntimeException(String.format(__("Cannot set value of '%s' field for NULL element"),
						fieldName));
			}
			
			try {
				field.set(tgp, value);
			} catch (NullPointerException ne) {
				System.err.println();
			} catch (Exception e) {
				throw new RuntimeException(String.format(__("Cannot set value of %s field in %s class due to %s(%s)"), 
						fieldName, tgp.getClass().getName(), 
						e.getClass().getName(), e.getMessage()));
			}
		};
		
		
		if (te.isList()) {
			// List type
			
			elementType = "List property";
			Class elementClass = tea.type();
			checkListElementType(fieldClass, elementClass, field.getName(), className,
					propTuple, true, false, classList, processedClassSet);
		} else {
			// Object property type

			elementType = "Object property";
			checkObjectElementType(fieldClass, field.getName(), className, 
					true, false, classList, processedClassSet);
		}
		
		DEBUG_MSG_ANO("Register %s field %s.'%s' as %s", elementType, className, 
				propName, fieldClass.getName());
		elementGetterMap.put(propTuple, Tuple3.of(tea, fieldClass, getter));
		elementSetterMap.put(propTuple, Tuple3.of(tea, fieldClass, setter));
		
	}
	
	private void processMethodAsSimpleProperty(Class<? extends TrainGraphPart> clazz,
			TGProperty tp, Method method, int propIndex) {
		
		String className = clazz.getName();	
		if (tp == null)
			return;

		String methodName = method.getName();
		boolean isSetter = methodName.startsWith("set");
		boolean isGetter = methodName.startsWith("get");
		if (!isSetter && !isGetter)
			throw new AnnotationException(String.format("Method %s.%s annotated with @TGElement has to "
					+ "be a setter or a getter, i.e. its name must starts with 'get' or 'set'.",
					className, methodName));
		
		Class<?> propClass = null;
		String propName = tp.name().trim();
		if ("".equals(tp.name())) {
			propName = methodName.replace("get", "").replace("set", "");
			if (propName.length() > 1)
				propName = propName.substring(0, 1).toLowerCase() + propName.substring(1);				
		}
		
		Tuple2<String, String> propTuple = Tuple2.of(className, propName);
		method.setAccessible(true);
		
		// Setters
		if (isSetter) {
			if (method.getParameterCount() != 1) {
				throw new AnnotationException(String.format("The %s method in %s class with "
						+ "@TGProperty should take and only take one paramter.",
						methodName, className));
			}
			propClass = method.getParameterTypes()[0];
			String setterPropClass = propClass.getName();
			String getterPropClass = PropertyTypeMap.get(propTuple);
			if (getterPropClass == null) {
				PropertyTypeMap.put(propTuple, setterPropClass);
			} else if (!setterPropClass.equals(getterPropClass)) {
				throw new AnnotationException(String.format("The type of first argument of method %s in class %s with "
						+ "@TGProperty should be %s, which is the return type of corresponding getter method.",
						methodName, className, getterPropClass));
			}
			
			Class<?> propClass0 = propClass;
			BiConsumer<TrainGraphPart, String> setter = (tgp, strValue) -> {
				if (tgp == null) {
					throw new RuntimeException(String.format(__("Cannot set value with '%s' method for NULL element"),
							methodName));
				}
				
				Object value = ValueTypeConverter.convertType(strValue, 
						String.class, propClass0);
				try {
					method.invoke(tgp, value);
				} catch (InvocationTargetException ie) {
					throw new RuntimeException(String.format(__("Cannot set value with %s method in %s class due to %s(%s)"), 
							methodName, tgp.getClass().getName(), 
							ie.getTargetException().getClass().getName(), ie.getTargetException().getMessage()));
				} catch (Exception e) {
					throw new RuntimeException(String.format(__("Cannot set value with %s method in %s class due to %s(%s)"), 
							methodName, tgp.getClass().getName(), 
							e.getClass().getName(), e.getMessage()));
				}
			};
			simplePropertySetterMap.put(propTuple, Tuple2.of(setter, tp.isArray()));
		}
		
		// Getters
		else if (isGetter) {
			if (method.getParameterCount() != 0) {
				throw new AnnotationException(String.format("The %s method in %s class with "
						+ "@TGProperty should take no paramter.",
						methodName, className));
			}
			propClass = method.getReturnType();
			String getterPropClass = propClass.getName();
			String setterPropClass = PropertyTypeMap.get(propTuple);
			if (setterPropClass == null) {
				PropertyTypeMap.put(propTuple, getterPropClass);
			} else if (!setterPropClass.equals(getterPropClass)) {
				throw new AnnotationException(String.format("The return type of method %s in class %s with "
						+ "@TGProperty should be %s, which is the type of the first argument of corresponding setter method.",
						methodName, className, setterPropClass));
			}

			Class<?> propClass0 = propClass;
			Function<TrainGraphPart, String> getter = tgp -> {
				if (tgp == null) {
					throw new RuntimeException(String.format(__("Cannot get value through '%s' method from NULL element"),
							methodName));
				}
				
				Object value;
				try {
					value = method.invoke(tgp);
				} catch (InvocationTargetException ie) {
					throw new RuntimeException(String.format(__("Cannot get value through %s method in %s class due to %s(%s)"), 
							methodName, tgp.getClass().getName(), 
							ie.getTargetException().getClass().getName(), ie.getTargetException().getMessage()));
				} catch (Exception e) {
					throw new RuntimeException(String.format(__("Cannot get value through %s method in %s class due to %s(%s)"), 
							methodName, tgp.getClass().getName(),
							e.getClass().getName(), e.getMessage()));
				}
				
				return value == null ? "" : (String) ValueTypeConverter.convertType(value, 
						propClass0, String.class);
			};
			simplePropertyGetterMap.put(propTuple, Tuple2.of(getter, tp.isArray()));

			int newPropIndex = Math.min(propIndex, tp.index());
			Tuple2 attrTuple = Tuple2.of(newPropIndex, tp.firstline());
			simplePropertyIndexList.add(Tuple2.of(propTuple, attrTuple));
			
			
		}
		
		String methodDesc = isGetter ? "getter" : "setter";
		DEBUG_MSG_ANO("Register Simple property %s %s.'%s' in %s type", methodDesc,
				className, propName, propClass.getName());

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
			throw new AnnotationException(String.format("Method %s.%s annotated with @TGElement has to "
					+ "be a setter or a getter, i.e. its name must starts with 'get' or 'set'.",
					className, methodName));
		
		String propName = te.name().trim();
		if ("".equals(te.name())) {
			propName = methodName.replace("get", "").replace("set", "");
			if (propName.length() > 1)
				propName = propName.substring(0, 1).toLowerCase() + propName.substring(1);				
		}
		Tuple2<String, String> propTuple = Tuple2.of(className, propName);
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
				throw new AnnotationException(String.format("The %s method in %s class with "
						+ "@TGElement should take and only take one paramter.",
						methodName, className));
			}
			propClass = method.getParameterTypes()[0];
			String setterPropClass = propClass.getName();
			String getterPropClass = PropertyTypeMap.get(propTuple);
			if (getterPropClass == null) {
				PropertyTypeMap.put(propTuple, setterPropClass);
			} else if (!setterPropClass.equals(getterPropClass)) {
				throw new AnnotationException(String.format("The type of first argument of method %s in class %s with "
						+ "@TGElement should be %s, which is the return type of corresponding getter method.",
						methodName, className, getterPropClass));
			}
			
			setter = (tgp, value) -> {
				try {
					if (tgp == null) {
						throw new RuntimeException(String.format(__("Cannot set value with '%s' method for NULL element"),
								methodName));
					}
					
					method.invoke(tgp, value);
				} catch (InvocationTargetException ie) {
					throw new RuntimeException(String.format(__("Cannot set value with %s method in %s class due to %s(%s)"), 
							methodName, tgp.getClass().getName(), 
							ie.getTargetException().getClass().getName(), ie.getTargetException().getMessage()));
				} catch (Exception e) {
					throw new RuntimeException(String.format(__("Cannot set value with %s method in %s class due to %s(%s)"), 
							methodName, tgp.getClass().getName(), 
							e.getClass().getName(), e.getMessage()));
				}
			};
		} else if (isGetter) {
			if (method.getParameterCount() != 0) {
				throw new AnnotationException(String.format("The %s method in %s class with "
						+ "@TGElement should take no paramter.",
						methodName, className));
			}
			propClass = method.getReturnType();
			String getterPropClass = propClass.getName();
			String setterPropClass = PropertyTypeMap.get(propTuple);
			if (setterPropClass == null) {
				PropertyTypeMap.put(propTuple, getterPropClass);
			} else if (!setterPropClass.equals(getterPropClass)) {
				throw new AnnotationException(String.format("The return type of method %s in class %s with "
						+ "@TGElement should be %s, which is the type of the first argument of corresponding setter method.",
						methodName, className, setterPropClass));
			}
			getter = tgp -> {
				Object value;
				try {
					if (tgp == null) {
						throw new RuntimeException(String.format(__("Cannot get value through '%s' method from NULL element"),
								methodName));
					}
					
					value = method.invoke(tgp);
					return value;
				} catch (InvocationTargetException ie) {
					throw new RuntimeException(String.format(__("Cannot get value through %s method in %s class due to %s(%s)"), 
							methodName, tgp.getClass().getName(), 
							ie.getTargetException().getClass().getName(), ie.getTargetException().getMessage()));
				} catch (Exception e) {
					throw new RuntimeException(String.format(__("Cannot get value through %s method in %s class due to %s(%s)"), 
							methodName, tgp.getClass().getName(),
							e.getClass().getName(), e.getMessage()));
				}
			};
		}
		
		if (te.isList()) {
			// List type
			
			elementType = "List property";
			Class<?> elementClass = tea.type();
			checkListElementType(propClass, elementClass, methodName, className,
					propTuple, false, isSetter, classList, processedClassSet);
		} else {
			// Object property type

			elementType = "Object property";
			checkObjectElementType(propClass, methodName, className, 
					false, isSetter, classList, processedClassSet);
		}
		
		String methodType = isGetter ? "getter" : "setter";
		DEBUG_MSG_ANO("Register %s %s %s.'%s' as %s", elementType, methodType, className, 
				propName, propClass.getName());
		if (isGetter)
			elementGetterMap.put(propTuple, Tuple3.of(tea, propClass, getter));
		else
			elementSetterMap.put(propTuple, Tuple3.of(tea, propClass, setter));
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
				throw new AnnotationException(String.format("The type of field %s.%s anotated with "
					+ "@TGElement should be a subclass of java.util.List<? extends %s>.",
					className, memberName, TrainGraphPart.class.getName()));
			else {
				String typePosition = memberIsSetter ? "parameter" : "return";
				throw new AnnotationException(String.format("The %s type of method %s.%s anotated with "
					+ "@TGElement should be a subclass of java.util.List<? extends %s>.",
					typePosition, className, memberName, TrainGraphPart.class.getName()));
			}
				
		
		// Default value is used
		if (elementClass.equals(TrainGraphPart.class)) 
			if (memberIsField)
				throw new AnnotationException(String.format("Field %s.%s anotated with "
					+ "@TGElement should has annotation attribute type set to the type parameter in "
					+ "java.util.List<? extends %s>.",
					className, memberName, TrainGraphPart.class.getName()));
			else
				throw new AnnotationException(String.format("Method %s.%s anotated with "
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
//		if (memberIsSetter) {
//			Supplier<Object> creator = () -> {
//				try {
//					return listClass.newInstance();
//				} catch (InstantiationException | IllegalAccessException e) {
//					throw new RuntimeException(String.format("Cannot instantiate list property %s as a %s for class %s",
//							propTuple.B, listClass.getName(), className), e);
//				}
//			};
//			elementCreatorMap.put(propTuple, Tuple2.oF(listClass, creator));
//		}
	}
	
	@SuppressWarnings("unchecked")
	private void checkObjectElementType(Class elementClass, String memberName, String className, 
			boolean memberIsField, boolean memberIsSetter, 
			LinkedList<Class<? extends TrainGraphPart>> classList, 
			HashSet<Class<? extends TrainGraphPart>> processedClassSet) {
		
		
		boolean typeMatch = TrainGraphPart.class.isAssignableFrom(elementClass);
		
		if (!typeMatch) 
			if (memberIsField)
				throw new AnnotationException(String.format("The type of field %s.%s anotated with "
					+ "@TGElement should be a subclass of %s.",
					className, memberName, TrainGraphPart.class.getName()));
			else {
				String typePosition = memberIsSetter ? "paramter" : "return";
				throw new AnnotationException(String.format("The %s type of method %s.%s anotated with "
					+ "@TGElement should be a subclass of %s.",
					typePosition, className, memberName, TrainGraphPart.class.getName()));
			}

		if (!processedClassSet.contains(elementClass)) {
			processedClassSet.add(elementClass);
			classList.addLast(elementClass);
		}
	}
	
	private void checkMissingAccessor(Set<Tuple2<String, String>> getterKeySet, 
			Set<Tuple2<String, String>> setterKeySet) {
		// Check missing setters and getters for simple properties in all classes
		Vector<String> errorMsgs = new Vector<>();
		getterKeySet.stream().filter(getterTuple -> !setterKeySet.contains(getterTuple))
			.map(tuple -> String.format("Getter for %s.%s has no corresponding setter.", tuple.A, tuple.B))
			.forEach(errorMsgs::add);
		setterKeySet.stream().filter(setterTuple -> !getterKeySet.contains(setterTuple))
		.map(tuple -> String.format("Setter for %s.%s has no corresponding getter.", tuple.A, tuple.B))
		.forEach(errorMsgs::add);
		
		if (errorMsgs.size() > 0) {
			String errMsg = errorMsgs.stream().collect(Collectors.joining(NEW_LINE_STR, NEW_LINE_STR, ""));
			throw new AnnotationException(errorMsgs.size(), errMsg);
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
		saveToWriter(writer, out, 0, true);
		writer.flush();
	}
	
	public void saveToStream(OutputStream out, int identLevel) throws IOException {
		Writer writer = new OutputStreamWriter(out, "utf-8");
		saveToWriter(writer, out, identLevel, true);
		writer.flush();
	}
	
	protected void saveToWriter(Writer writer, OutputStream out, int identLevel, 
			boolean printIdentOnFirstLine) throws IOException {
		
		boolean elementPrinted = false;
		// Write section begin string
		if (printIdentOnFirstLine)
			_printIdent(writer, identLevel, false);
		_print(writer, "%s {%s", getElementName(), isInOneLine() ? "" : NEW_LINE_STR);
		
		if (isBase64Encoded()) {
			// Write base64 codes for binary contents
			
			writer.flush();
			encodeToBase64(out);
//			writer.append( encodeToBase64() );
//			_println(writer);
			
			elementPrinted = true;
		} else {			
			// Write simple properties
			elementPrinted |= saveSimplePropertiesToWriter(writer, identLevel, isInOneLine());
			
			// Write element properties recursively
			elementPrinted |= saveElementPropertiesToWriter(writer, identLevel, 
					NEW_LINE_STR, ", " + NEW_LINE_STR, "", true, 
					(element, te, eIdentLevel, printIdentOnFirstLine0) -> {
				try {
					element.saveToWriter(writer, out, eIdentLevel, printIdentOnFirstLine0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		
		// Write section end string
		if (!isInOneLine() && elementPrinted) {
			_println(writer);
			_printIdent(writer, identLevel, isInOneLine());
		}
		_print(writer, "}");
	}
	
	/**
	 * Print all simple properties in an TG element.
	 * @param writer
	 * @param identLevel
	 * @param inOneLine
	 * @return If any single property is printed.
	 */
	private boolean saveSimplePropertiesToWriter(Writer writer, int identLevel, boolean inOneLine) {
		String className = getClass().getName();
		List<Tuple2<Tuple2<String, String>, Tuple2<Integer, Boolean> >> propKeyList= simplePropertyIndexMap.get(className);
		if (propKeyList == null) {
			DEBUG_MSG_ANO("There is no fileds/accessor registered as simple property "
					+ "in %s class with @SimpleProperty", className);
			return false;
		}

//		_printIdent(writer, identLevel + 1, inOneLine);
//		_println(writer);
		_printIdent(writer, identLevel + 1, inOneLine);
		
		// A flag set up in sorting order and used in forEach
		boolean[] boolFlags = {false, false}; // {hasFirstLineProperties, lineBreakPrinted}
		int[] intFlags = {0, propKeyList.size() - 1}; // [iterationIndex, iterationMaxCount]
		propKeyList.stream().sorted((tuple1, tuple2) -> {
			// tuple.A is (className, propName), tuple.B is (propIndex, isFirstLine, isArray)
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
			// tuple.A is (className, propName), tuple.B is (propIndex, isFirstLine, isArray)
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
			
			_print(writer, "%s=%s", propTuple.B, propValue);

			// ++ index and print , between properties.
			if (intFlags[0] ++ != intFlags[1])
				_print(writer, ", ");
			
			// ++ index
//			++ intFlags[0];
		});
		
		return intFlags[1] > 0;
	}
	
	private String getSimpleProperty(Tuple2<String, String> propTuple) {
		Tuple2<Function<TrainGraphPart, String>, Boolean> getterTuple =
				simplePropertyGetterMap.get(propTuple);
		Function<TrainGraphPart, String> getter = getterTuple.A;
		boolean isArray = getterTuple.B;
		
		if (getter != null) {
			return _encode(getter.apply(this), isArray);
		} else {
			return ValueTypeConverter.NULL_STR;
		}
	}

	/**
	 * Save all element properties in a TG element, including object properties
	 * and list properties.
	 * @param writer
	 * @param identLevel
	 * @param prefix
	 * @param delimiter
	 * @param suffix
	 * @param printLnAfterElement
	 * @param elementPrintAction
	 * @return If any element property is printed.
	 */
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
					boolean isList = tea.isList();
					Object propValue = getter.apply(this);
					if (propValue == null) {
						NullPart nullPart = TrainGraphFactory.createInstance(NullPart.class);
						propValue = nullPart;
						isList = false;
					}
					
					if (isList) {
						// List property
						@SuppressWarnings("unchecked")
						List<Object> elementList = (List<Object>) propValue;
						int listItemCount = elementList.size();
						if (printLnAfterElement)
							_printIdent(writer, identLevel + 1, false);
						_print(writer, "%s = %d[%s", tea.name(), elementList.size(), 
								newLineStrAfterElement);
						
						for (int listItemIndex = 0; listItemIndex < listItemCount; ++listItemIndex) {
							TrainGraphPart element = (TrainGraphPart) elementList.get(listItemIndex);
							elementPrintAction.accept(element, tea, identLevel + 2, true);
							
							if (listItemIndex != listItemCount - 1)
								_print(writer, ",");
							if (printLnAfterElement)
								_println(writer);
						}

						if (printLnAfterElement)
							_printIdent(writer, identLevel + 1, false);
						_print(writer, "]");
					} else {
						// object property
						
						if (printLnAfterElement)
							_printIdent(writer, identLevel + 1, false);
						_print(writer, "%s = ", tea.name());
						TrainGraphPart element = (TrainGraphPart) propValue;
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
	
	private static TrainGraphPart parsingRoot;
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
		parsingRoot = null;
		String line = null;
		int lineNum = 0;
		Vector<ParsingException> exceptions = new Vector<> ();
		// A stack objects being parsed. Each element is (object, name of object)
		Stack<TrainGraphPart> parsingNodeStack = new Stack<>();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			
			TrainGraphPart loadingNodeArg = parsingRoot == null ? loadingNode : null;
			
			try {
				root = parseLine(line, ++lineNum, parsingNodeStack, exceptions, loadingNodeArg);
				
				if (parsingRoot == null && clazz.isInstance(root)) {
					parsingRoot = root;
				}
			} catch (ParsingException e) {
				exceptions.add(e);
			}
		}
		
		reader.close();
		
		if (!parsingNodeStack.isEmpty()) {
			exceptions.add(ParsingException.create(lineNum, line, 
					__("Unexpected end of file. '}' or ']' is missing.")));
		}
		
		if (exceptions.size() > 0) {
			int errorCount = exceptions.stream().mapToInt(e -> e.errorCount).sum();
			String msg = exceptions.stream().map(e -> e.getMessage())
					.collect(Collectors.joining(NEW_LINE_STR, 
							String.format("There are %d error%s found when parsing annotations:",
									errorCount, (errorCount > 1 ? "s" : ""), NEW_LINE_STR ), ""));
			System.err.println(msg);
			throw new ParsingException(__("Train graph file is corrupted."));
		}
		
		if (clazz != null)
			return parsingRoot;
		else
			return root;
	}
	
//	private static FileWriter fw;
//	static {
//		try {
//			fw = new FileWriter("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/map-base64-load-debug.txt");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	
	protected static TrainGraphPart parseLine(String line, int lineNum, 
			Stack<TrainGraphPart> parsingNodeStack, 
			Vector<ParsingException> exceptions, TrainGraphPart loadingNode) {
		
		boolean modelModified = false;
		
		// Step 0. strip out comments
		String fullLine = line;
		line = stripComment(line);
		boolean emptyLine = "".equals(line);
		
		TrainGraphPart parentObj = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
		if (parentObj != null && parentObj.isBase64Encoded()) {
			parentObj.decodeFromBase64NewLine(fullLine);
			finishObject(line, fullLine, lineNum, parsingNodeStack, exceptions);
//			try {
//				fw.append(fullLine + "\r");
//				fw.flush();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			modelModified |= true;
		} else {
			
			if (!emptyLine) {
				
				// Step 1. try to interpret this line as a start of an object.
				Tuple2<TrainGraphPart, String> objTuple = createObjectForLine(line, 
						fullLine, lineNum, parsingNodeStack, exceptions, loadingNode);
				TrainGraphPart thisObj = objTuple.A;
				String remainingline = objTuple.B;
				modelModified |= thisObj != null;
				
				/*TrainGraphPart*/ parentObj = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
				
				if (parentObj != null) {
					if (parentObj instanceof UnknownPart) {
						// skip current line.
						UnknownPart unknownElement = ((UnknownPart) parentObj);
						if (! unknownElement.alerted) {
							unknownElement.alerted = true;
							throw ParsingException.create(lineNum, fullLine, ((UnknownPart) parentObj).message);
						} else {
							
						}
					} else {
						
						// Step 2. try to find simple property assignments, i.e.
						// key-value pair matching "name=value" patterns.
						Tuple2<Boolean, String> aspTuple = assignSimpleProperties(remainingline, fullLine, lineNum,
								parsingNodeStack, exceptions);
						modelModified |= aspTuple.A;
						remainingline = aspTuple.B;
					}
				}
				
				// Step 3. try to complete the object, which including assigning element properties
				// and do load_complete job on the object.
				modelModified |= finishObject(remainingline, fullLine, lineNum, parsingNodeStack, exceptions);
			}
		}
		
		if (!emptyLine && !modelModified)
			throw ParsingException.create(lineNum, line,
					__("Potential error exists in a meaningless line."));
		
		return parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
	}
	
	private static String stripComment(String line) {
		return line.replaceFirst("//.*", "").trim();
	}

	private static Tuple2<Boolean, String> assignSimpleProperties(String line, String fullLine, int lineNum,
			Stack<TrainGraphPart> parsingNodeStack, Vector<ParsingException> exceptions) {
		
		boolean modelModified = false;
		TrainGraphPart objToBeAssigned = parsingNodeStack.peek();
		String remainingLine = line;
		
		if (objToBeAssigned != null) {
			String propLine = line.replaceFirst("\\}.*", "").replaceFirst("\\].*", "").trim();
			String[] assignments = propLine.split(",");
			for (String assignment : assignments) {
				String[] strParts = assignment.split("=");
				String propName = strParts[0].trim();
				if ("".equals(propName))
					continue;
				
				String propValue = strParts.length >= 2 ? strParts[1] : "";
				try {
					objToBeAssigned.setSimpleProperty(objToBeAssigned, 
							propName, propValue, lineNum, fullLine);
					
					modelModified = true;
				} catch (ParsingException e) {
					exceptions.add(e);
				}
			}
			
			remainingLine = line.substring(propLine.length());
		}
		
		return Tuple2.of(modelModified, remainingLine);
	}
	
	private void setSimpleProperty(TrainGraphPart obj, String propName, 
			String valueInStr, int lineNum, String fullLine) {
		
		Tuple2 propTuple = Tuple2.of(obj.getClass().getName(), propName);
		Tuple2<BiConsumer<TrainGraphPart, String>, Boolean> setterTuple =
				 simplePropertySetterMap.get(propTuple);
		if (setterTuple == null) {
			DEBUG_MSG_LOAD(__("Unknown simple property '%s' ignored in class %s"),
					propName, propTuple.A);
			return;
		}
		
		BiConsumer<TrainGraphPart, String> setter = setterTuple.A;
		boolean isArray = setterTuple.B;
		
		if (ValueTypeConverter.NULL_STR.equals(valueInStr))
			valueInStr = null;
		
		if (setter != null) {
			setter.accept(obj, _decode(valueInStr, isArray));
		} else {
			throw ParsingException.create(lineNum, fullLine,
					String.format(__("Undefined simple property '%s' encounted in '%s' element."), 
							propName, getElementName(obj.getClass())));
		}
	}
	
	private static Tuple2<TrainGraphPart, String> createObjectForLine(
			String line, String fullLine, int lineNum, 
			Stack<TrainGraphPart> parsingNodeStack, Vector<ParsingException> exceptions, 
			TrainGraphPart loadingNode) {
		
		// Part object starts with either a "{" for an object property or element of list property,
		// or a "[" for a list property . If neither is found, then no object is created.
		boolean isObject = line.contains("{");
		boolean isList = line.contains("[");
		if (!isObject && !isList)
			return Tuple2.of(null, line);

		// {remainingLine, propNameToBeAssigned, ElementNameOfObjectProperty, LengthOfListProperty}
		String[] lineParts = matchObjectLine(line); 
		if (lineParts == null)
			throw ParsingException.create(lineNum, line, "'{' or '[' exists but this line does not comply the syntax of element property.");
		
		String remainingLine = lineParts[0];
		String propName = lineParts[1];
		String objName = lineParts[2];
		String listLenStr = lineParts[3];
		
		TrainGraphPart obj = null;
		TrainGraphPart stackTop = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
		String parentName = stackTop == null ? "" : stackTop.getClass().getName();
//		String propName = line.replaceFirst("\\{.*", "").trim();
		

		
		if (stackTop != null && stackTop instanceof UnknownPart) {
			// If the current scope is an unknown part, then treat current part
			// as an unknown element too no matter what kind of element it is, 
			// because there is no need to parse the content of an unknown element.
			obj = new UnknownPart();
			((UnknownPart) obj).alerted = true; // not necessary to alert .
		} 
		else {
			if (listLenStr != null) {
				// It is a list property
				
				Tuple3<TGElementAttr, Class<?>, BiConsumer<TrainGraphPart, Object>> setterTuple = 
						findSetterForProperty(fullLine, lineNum, stackTop, propName);
				Function<TrainGraphPart, Object> getter = findGetterForProperty(fullLine, lineNum, 
						stackTop, propName);
				Supplier<? extends Object> creator = () -> {
					try {
						return setterTuple.B.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				};
				
				ListElementAssignment leAssignment = new ListElementAssignment(lineNum, line, 
						stackTop, setterTuple.B);
				leAssignment.creator = creator;
				leAssignment.setter = setterTuple.C;
				leAssignment.getter = getter;
				leAssignment.createList();
				
				obj = leAssignment;
			} else {
				if (propName != null) {
					// It is a object property
					
					Tuple3<TGElementAttr, Class<?>, BiConsumer<TrainGraphPart, Object>> setterTuple = 
							findSetterForProperty(line, lineNum, stackTop, propName);
					ObjectPropertyAssignment opAssignment = new ObjectPropertyAssignment(lineNum, line, 
							stackTop, setterTuple.B);
					opAssignment.setter = setterTuple.C;
					
					parsingNodeStack.push(opAssignment);
					
					obj = createObject(fullLine, lineNum, parsingNodeStack, exceptions,
							loadingNode, propName, objName, parentName);
				} else {
					// It is an element of a list property or it is the root object
					
					obj = createObject(fullLine, lineNum, parsingNodeStack, exceptions,
							loadingNode, propName, objName, parentName);
				}
			}
			
		}	
		
		if (obj != null) {
			parsingNodeStack.push(obj);
		}

		return Tuple2.of(obj, remainingLine);
	}
	
	private static Tuple3<TGElementAttr, Class<?>, BiConsumer<TrainGraphPart, Object>> 
		findSetterForProperty(String fullLine, int lineNum, TrainGraphPart stackTop, 
				String propName) {
		
		String parentClassName = stackTop == null ? "" : stackTop.getClass().getName();
		Tuple2<String, String> propTuple = Tuple2.of(parentClassName, propName);
		
		Tuple3<TGElementAttr, Class<?>, BiConsumer<TrainGraphPart, Object>> setter = 
				elementSetterMap.get(propTuple);
		
		if (setter == null)
			throw ParsingException.create(lineNum, fullLine, __("property %s does not exist in class %s"), 
					propName, parentClassName);
		
		return setter;
	}
	
//	private static Supplier<? extends Object> findCreatorForProperty(String fullLine, int lineNum, 
//			TrainGraphPart stackTop,  String propName) {
//		
//		String parentClassName = stackTop == null ? "" : stackTop.getClass().getName();
//		Tuple2<String, String> propTuple = Tuple2.oF(parentClassName, propName);
//		
//		Supplier<?> creator = elementCreatorMap.get(propTuple);
//		
//		if (creator == null)
//			throw ParsingException.create(lineNum, fullLine, __("There is no creator for property %s in class %s"), 
//					propName, parentClassName);
//		
//		return creator;
//	}	
	
	private static Function<TrainGraphPart, Object> findGetterForProperty(String fullLine, int lineNum, 
			TrainGraphPart stackTop,  String propName) {
		
		String parentClassName = stackTop == null ? "" : stackTop.getClass().getName();
		Tuple2<String, String> propTuple = Tuple2.of(parentClassName, propName);
		
		Tuple3<TGElementAttr, Class<?>, Function<TrainGraphPart, Object>> getter = 
				elementGetterMap.get(propTuple);
		
		return getter == null ? null : getter.C;
	}

	private static TrainGraphPart createObject(String fullLine, int lineNum,
			Stack<TrainGraphPart> parsingNodeStack, Vector<ParsingException> exceptions, 
			TrainGraphPart loadingNode, String propName, String elementTypeName, String parentName) {
		
		TrainGraphPart obj = null;
		
		Supplier<? extends Object> creator = elementCreatorMap.get(elementTypeName);
		
		if (creator != null) {
			/* If loadingNode is not null and it is an instance of corresponding
			 * class to current line being parsed, then use it as the current object
			 * instead of creating new one.
			 */
			if (loadingNode != null) {
				Class<?> nodeClass = nameToTypeDict.get(elementTypeName);
				if (nodeClass != null && nodeClass.isInstance(loadingNode)) {
					if (loadingNode.isBase64Encoded()) {
						loadingNode.decodeFromBase64Start();
					}
					obj = loadingNode;
				}
			} else {
				obj = (TrainGraphPart) creator.get();
				
				// Failed to create an instance.
				if (obj == null) {
					obj = createUnknownPartint(fullLine, lineNum, elementTypeName, 
							parsingNodeStack, exceptions);
					((UnknownPart) obj).message = 
						String.format(__("Cannot create an instance for %s. "
							+ "This object and all its content is skipped."), elementTypeName);
							
				} else {
					obj.setRoot(parsingRoot);
					if (obj.isBase64Encoded()) {
						obj.decodeFromBase64Start();
					}
//					obj.initElements();
				}
			}
		} else {
			// A top level unknown part encountered during parsing.
			
			obj = createUnknownPartint(fullLine, lineNum, elementTypeName, 
					parsingNodeStack, exceptions);
		}
		return obj;
	}
	
	private static boolean finishObject(String line, String fullLine, int lineNum, 
			Stack<TrainGraphPart> parsingNodeStack, Vector<ParsingException> exceptions) {
		
		boolean closingListElement = line.contains("]");
		boolean closingObject = line.contains("}");
		if (!closingListElement && !closingObject)
			return false;
		
		if (parsingNodeStack.isEmpty())
			throw ParsingException.create(lineNum, fullLine, 
					__("Cannot finish an object because the parsing stack becomes empty unexptectedly."));
		
		TrainGraphPart obj = parsingNodeStack.isEmpty() ? null : parsingNodeStack.pop();
		if (obj != null && obj.isBase64Encoded()) {
			obj.decodeFromBase64End();
		}
		
		if (obj instanceof ListElementAssignment || obj instanceof ObjectPropertyAssignment) {
		} else {
			// Assign object to parent object.
			TrainGraphPart stackTop = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
			if (stackTop instanceof ListElementAssignment) {
				((ListElementAssignment) stackTop).addElement(lineNum, fullLine, obj);
			} else if (stackTop instanceof ObjectPropertyAssignment) {
				((ObjectPropertyAssignment) stackTop).assign(obj);
				
				// Pop out object assignment node
				parsingNodeStack.pop();
			}
		}
		
		obj.loadComplete();
		
		return true;
	}
	
	private static UnknownPart createUnknownPartint(String fullLine, int lineNum, String elementTypeName,
			Stack<TrainGraphPart> parsingNodeStack, Vector<ParsingException> exceptions) {

		UnknownPart obj = new UnknownPart();
		obj.startLineIndex = lineNum;
		obj.startLine = fullLine;
		obj.topLevel = true;
		
		TrainGraphPart stackTop = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
		String parentNodeClassName = "Root";
		if (stackTop != null) {
			if (stackTop instanceof ObjectPropertyAssignment)
				parentNodeClassName = getElementName(((ObjectPropertyAssignment) stackTop).getParentClass());
			else if (stackTop instanceof ListElementAssignment)
				parentNodeClassName = getElementName(((ListElementAssignment) stackTop).getParentClass());
			else
				parentNodeClassName = getElementName(stackTop.getClass());
		}
			
		obj.message = String.format(String.format(__("Encounted element '%s' is not an element of class '%s'."), 
				elementTypeName, parentNodeClassName));
		
		return obj;
		
	}
	
	/**
	 * A Regular expression pattern for object line referenced in matchObjectLine method.
	 * It should cover the following three cases: <br/><ol>
	 * <li>An object property. e.g.<br/>
	 *     allTrains = All Trains {</li>
	 * <li>Object element of an list property. e.g.<br/>
	 *     RailNetwork Chart {</li>
	 * <li>A list property. e.g.<br/>
	 *     All Line Charts = 1[</li>
	 * </ol>
	 */
	private static Pattern objectPattern = Pattern.compile("(([^=,\\[\\]\\{\\}]+?)\\s*=\\s*)?((([^=,\\[\\]\\{\\}]+?)\\s*\\{)|((\\d+)\\[))");

	/**
	 * Match an object line. It should be in any one of the following three cases: <br/><ol>
	 * <li>An object property. e.g.<br/>
	 *     allTrains = All Trains {</li>
	 * <li>Object element of an list property. e.g.<br/>
	 *     RailNetwork Chart {</li>
	 * <li>A list property. e.g.<br/>
	 *     All Line Charts = 1[</li>
	 * </ol>
	 * @param line 
	 * @return A String array. 
	 * <br/>If the line is not an object line, then <b>Null</b> 
	 * is returned.<br/>
	 * Otherwise, a String array is returned whose content is <b>
	 * {remainingLine, propNameToBeAssigned, ElementNameOfObjectProperty, LengthOfListProperty}</b><br/>
	 * If case 1 and 2, i.e. the object is an object property or an element of list property, 
	 * LengthOfListProperty is null. In case 3, i.e. the object is a list property, 
	 * ElementNameOfObjectProperty is null. Beside, in case 2, i.e. no property assignment 
	 * involved, propNameToBeAssigned is null.
	 */
	public static String[] matchObjectLine(String line) {
		Matcher m = objectPattern.matcher(line);
		
		if (m.find()) {
			int groupCount = m.groupCount();
			String[] groups = new String[groupCount + 1];
//			System.out.println(m.groupCount());
			for (int i = 0; i <= m.groupCount(); ++ i) {
				String group = m.group(i);
				groups[i] = group;
//				System.out.println(String.format("Group[%d] = %s", i, group));
			}
			
			String remainingLine = line.substring(m.end()).trim();
//			System.out.println(String.format("Remaining line: %s", remainingLine));
			
			return new String[] {remainingLine, groups[2], groups[5], groups[7]};
		} else {
			return null;
		}
	}

	// }}
	
	// {{ 辅助方法, encode, decode, print...
	
	private static Tuple2[] ESCAPE_CHARS = {
		// < and > should be at the beging, since _encode uses its index
		Tuple2.of("<", "$LT$"), Tuple2.of(">", "$GT$"),
		
		Tuple2.of("[", "$BRACKET_L$"), Tuple2.of("]", "$BRACKET_R$"),
		Tuple2.of(",", "$COMMA$"), Tuple2.of("=", "$EQUAL$"), 
		Tuple2.of("//", "$DOUBLE_SLASH$"), Tuple2.of(";", "$SEMICOLOM$"), 
		Tuple2.of("{", "$BRACE_L$"), Tuple2.of("}", "$BRACE_R$"),  
		Tuple2.of("\r\n", "$NEWLINE_RN$"), Tuple2.of("\n\r", "$NEWLINE_NR$"),  
		Tuple2.of("\r", "$NEWLINE_R$"), Tuple2.of("\n", "$NEWLINE_N$"),  
	};
	
	@SuppressWarnings("unchecked")
	public static String _encode(String str, boolean isArray) {
		if (str == null)
			return null;
		
		else if (isArray)
			return str;
		
		for (int i = 0; i < ESCAPE_CHARS.length; ++ i) {
			Tuple2<String, String> escapeChar = ESCAPE_CHARS[i];
			str = str.replace(escapeChar.A, escapeChar.B);
		}
		
		return str;
	}
	
	@SuppressWarnings("unchecked")
	public static String _decode(String str, boolean isArray) {
		if (str == null)
			return null;
		
		for (Tuple2<String, String> escapeChar : ESCAPE_CHARS) {
			str = str.replace(escapeChar.B, escapeChar.A);
		}
		
		return str;
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
		Class clazz = getClass();
		TGElementType tgeType = null;
		do {
			tgeType = typeToAttrDict.get(clazz);
			clazz = clazz.getSuperclass();
		} while (tgeType != null && !clazz.equals(TrainGraphPart.class));
		
		return tgeType != null ? tgeType.printInOneLine() : false;
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
				_print(writer, getElementName().replace("{" + NEW_LINE_STR, "{"));
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
