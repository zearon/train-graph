package org.paradise.etrc.data;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.paradise.etrc.data.util.Tuple;
import org.paradise.etrc.data.annotation.*;
import org.paradise.etrc.util.ui.databinding.ValueTypeConverter;

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
	public static int TO_STRING_ELEMENT_DEPTH = 0;
	public static HashMap<String, Integer> TO_STRING_SPECIFIC_TYPE_DEPTH = new HashMap<String, Integer> ();
//	public static HashMap<String, Integer> TO_DEBUG_STRING_SPECIFIC_TYPE_DEPTH = new HashMap<String, Integer> ();
	public static boolean TO_STRING_SHOW_TYPE = false;
	public static boolean TO_STRING_SHOW_PROPERTIES = false;
	
	static {
		setSimpleToString();
	}
	
	public static final String IDENT_STR	= "  ";
	public Boolean IN_ONE_LINE	= null;
	
	/* DO NOT CHANGE "{", "}", "\r\n" to other strings! 
	 * Classes that have only simple properties can be written in one line, i.e. 
	 * START_SECTION_ of these types could not end with "\r\n".
	 * Classes that have object properties or element array can not be written
	 * in one line, i.e. START_SECTION_ of these types must end with "\r\n"
	 */
	public static final String START_SECTION_TRAIN_GRAPH	 	= "TrainGraph {\r\n";
	public static final String END_SECTION_TRAIN_GRAPH		 	= "} TrainGraph\r\n";	
	
	public static final String START_SECTION_GLOBAL_SETTINGS 	= "GlobalSettings {\r\n";
	public static final String END_SECTION_GLOBAL_SETTINGS	 	= "} GlobalSettings\r\n";
	
	public static final String START_SECTION_RAILROAD_NETWORK 	= "RailNetwork {\r\n";
	public static final String END_SECTION_RAILROAD_NETWORK 	= "} RailNetwork\r\n";
	public static final String START_SECTION_RAILROAD_LINE 		= "RailLine {\r\n";
	public static final String END_SECTION_RAILROAD_LINE 		= "} RailLine\r\n";
	public static final String START_SECTION_STATION 			= "Station {";
	public static final String END_SECTION_STATION 				= "}\r\n";
	
	public static final String START_SECTION_ALL_TRAIN_TYPES	= "All TrainTypes {\r\n";
	public static final String END_SECTION_ALL_TRAIN_TYPES		= "} All TrainTypes\r\n";
	public static final String START_SECTION_TRAIN_TYPE			= "TrainType {";
	public static final String END_SECTION_TRAIN_TYPE			= "}\r\n";
	
	public static final String START_SECTION_ALL_TRAIN			= "All Trains {\r\n";
	public static final String END_SECTION_ALL_TRAIN			= "} All Trains\r\n";
	public static final String START_SECTION_TRAIN				= "Train {\r\n";
	public static final String END_SECTION_TRAIN				= "} Train\r\n";
	public static final String START_SECTION_STOP				= "Stop {";
	public static final String END_SECTION_STOP 				= "}\r\n";
	
	public static final String START_SECTION_RAILNETWORK_CHART	= "RailNetwork Chart {\r\n";
	public static final String END_SECTION_RAILNETWORK_CHART	= "} RailNetwork Chart\r\n";
	public static final String START_SECTION_RAILINE_CHART		= "RailLine Chart {\r\n";
	public static final String END_SECTION_RAILINE_CHART		= "} RailLine Chart\r\n";
	public static final String START_SECTION_TRAIN_REF			= "Train Ref {";
	public static final String END_SECTION_TRAIN_REF			= "}\r\n";
	
	public static final String START_SECTION_RAILNETWORK_MAP	= "RailNetwork Map {\r\n";
	public static final String END_SECTION_RAILNETWORK_MAP		= "} RailNetwork Map\r\n";
	
	/* Field used by reflection code to determine the class of element.
	 * No need to set value. */
//	public ET _elementInstance = null;
	
	protected Vector<Tuple<String, TrainGraphPart>> objectProperties = new Vector<Tuple<String, TrainGraphPart>>();
	/* Store a (Class, Class constructor) tuple for each sub-class */
	protected static HashMap<String, 
		Tuple<Class<? extends TrainGraphPart>, Supplier<? extends TrainGraphPart>>> _partClassMap = 
			new HashMap<String, Tuple<Class<? extends TrainGraphPart>,Supplier<? extends TrainGraphPart>>> ();
	
	protected static LinkedHashMap<Tuple<String, String>, Function<TrainGraphPart, String>> simplePropertyGetterMap =
			new LinkedHashMap<> ();
	protected static LinkedHashMap<Tuple<String, String>, BiConsumer<TrainGraphPart, String>> simplePropertySetterMap =
			new LinkedHashMap<> ();
	protected static HashMap<Tuple<String, String>, String> simplePropertyTypeMap =
			new HashMap<> ();
	/* a list of tuples in form of ((className, propName), (porpIndex, firstLine) ) */
	protected static Vector<Tuple<Tuple<String, String>, Tuple<Integer,Boolean>>> simplePropertyIndexList =
			new Vector<> ();
	/* a map whose key is className, and the value is a list of tuples 
	   in form of ((className, propName), (porpIndex, firstLine) ) */
	protected static Map<String, List<Tuple<Tuple<String, String>, Tuple<Integer,Boolean> >>> simplePropertyIndexMap;
	
	protected static LinkedHashMap<Tuple<String, String>, BiConsumer<TrainGraphPart, String>> elementPropertyGetterMap = 
			new LinkedHashMap<> ();
	
	public static String ELEMENT_REPR_PREFIX;
	public static String ELEMENT_REPR_SUFFIX;
	public static Class<? extends TrainGraphPart> ELEMENT_TYPE;
	public static Vector<Field> ELEMENT_SIMPLE_PROPERTY_FIELD_LIST;
	
	protected Supplier<String> _elementReprPreffix;
	
//	static {
//		registerSubClasses();
//		processAnnotations();
//	}
//	
//	static void registerSubClasses() {
//		_partClassMap.put(START_SECTION_TRAIN_GRAPH.trim(), 
//				Tuple.of(TrainGraph.class, TrainGraph::new));
//		_partClassMap.put(START_SECTION_RAILROAD_NETWORK.trim(), 
//				Tuple.of(RailNetwork.class, RailNetwork::new));
//		_partClassMap.put(START_SECTION_RAILROAD_LINE.trim(), 
//				Tuple.of(RailroadLine.class, RailroadLine::new));
//		_partClassMap.put(START_SECTION_STATION.trim(), 
//				Tuple.of(Station.class, Station::new));
//		
//		_partClassMap.put(START_SECTION_ALL_TRAIN.trim(), 
//				Tuple.of(AllTrains.class, AllTrains::new));
//		_partClassMap.put(START_SECTION_TRAIN.trim(), 
//				Tuple.of(Train.class, Train::new));
//		_partClassMap.put(START_SECTION_STOP.trim(), 
//				Tuple.of(Stop.class, Stop::new));
//		
//		_partClassMap.put(START_SECTION_RAILNETWORK_CHART.trim(), 
//				Tuple.of(RailNetworkChart.class, RailNetworkChart::new));
//		_partClassMap.put(START_SECTION_RAILINE_CHART.trim(), 
//				Tuple.of(RailroadLineChart.class, RailroadLineChart::new));
//		_partClassMap.put(START_SECTION_TRAIN_REF.trim(), 
//				Tuple.of(TrainRef.class, TrainRef::new));
//		
//	}


	static HashMap<String, Integer> _objectIdMap = new HashMap<String, Integer> ();
	
	protected int _id;
	
	public String name;
	@TGPProperty(firstline=true)
	public String getName() { return name; }
	@TGPProperty
	public void setName(String name) { this.name = name; }
	
	TrainGraphPart() {
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
	
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof TrainGraphPart && ((TrainGraphPart) o)._id == _id &&
				((TrainGraphPart) o).getStartSectionString().equals(getStartSectionString());
				
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
				getStartSectionString(), 3), false, false);
	}
	
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
	
	public void prepareForFirstLoading() {
		registerClasses();

		processPropertyAnnotations();
	}
	
	public void processPropertyAnnotations() {
		simplePropertyGetterMap.clear();
		simplePropertySetterMap.clear();
		simplePropertyIndexList.clear();
		
		_partClassMap.values().stream().distinct().map(tuple->tuple.A).forEach(clazz -> {
			try {
				int[] propIndex = {0};

				// Find declared fields and inherited public fields as simple properties.
				Vector<Field> fields = new Vector<>();
				fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
				fields.addAll(Arrays.asList(clazz.getFields()));
				fields.stream().distinct().forEach(field-> {
					TGPProperty tp = field.getAnnotation(TGPProperty.class);
					TGPElement te = field.getAnnotation(TGPElement.class);
					if (tp != null)
						processFieldAsSimpleProperty(clazz, tp, field, ++ propIndex[0]);
					else if (te != null)
						processFieldAsElement(clazz, te, field, ++ propIndex[0]);
				});
				
				// Find public methods as simple property getters and setters
				Vector<Method> methods = new Vector<>();
				methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
				methods.addAll(Arrays.asList(clazz.getMethods()));
				methods.stream().distinct().forEach(method -> {
					TGPProperty tp = method.getAnnotation(TGPProperty.class);
					TGPElement te = method.getAnnotation(TGPElement.class);
					if (tp != null)
						processMethodAsSimpleProperty(clazz, tp, method, ++ propIndex[0]);
					else if (te != null)
						processMethodAsElement(clazz, te, method, ++ propIndex[0]);
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		// Check missing setters and getters for all classes
		Vector<String> errorMsgs = new Vector<>();
		Set<Tuple<String, String>> getterKeysSet = simplePropertyGetterMap.keySet();
		Set<Tuple<String, String>> setterKeysSet = simplePropertySetterMap.keySet();
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
	
	private void processFieldAsSimpleProperty(Class clazz, TGPProperty tp, 
			Field field, int propIndex) {	
		
		String className = clazz.getName();	
		if (tp == null)
			return;
		
		String fieldName = "".equals(tp.name()) ? field.getName() : tp.name();
		Class fieldClass = field.getType();
		Tuple propTuple = Tuple.oF(className, fieldName);
		
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
		int newPropIndex = Math.min(propIndex, tp.index());
		Tuple attrTuple = Tuple.oF(newPropIndex, tp.firstline());
		simplePropertyIndexList.add(Tuple.oF(propTuple, attrTuple));
	}
	
	private void processFieldAsElement(Class clazz, TGPElement te, 
			Field field, int propIndex) {	
		
	}
	
	private void processMethodAsSimpleProperty(Class<? extends TrainGraphPart> clazz,
			TGPProperty tp, Method method, int propIndex) {

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
			
			Tuple propTuple = Tuple.oF(className, propName);
			
			// Setters
			if (methodName.toLowerCase().startsWith("set")) {
				if (method.getParameterCount() != 1) {
					throw new RuntimeException(String.format("The %s method in %s class with "
							+ "@SimplePropertySetter should take and only take one paramter.",
							methodName, className));
				}
				Class propClass = method.getParameterTypes()[0];
				String setterPropClass = propClass.getName();
				String getterPropClass = simplePropertyTypeMap.get(propTuple);
				if (getterPropClass == null) {
					simplePropertyTypeMap.put(propTuple, setterPropClass);
				} else if (!setterPropClass.equals(getterPropClass)) {
					throw new RuntimeException(String.format("The type of first argument of method %s in class %s with "
							+ "@SimplePropertySetter should be %s, which is the return type of corresponding getter method.",
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
							+ "@SimplePropertySetter should take no paramter.",
							methodName, className));
				}
				Class propClass = method.getReturnType();
				String getterPropClass = propClass.getName();
				String setterPropClass = simplePropertyTypeMap.get(propTuple);
				if (setterPropClass == null) {
					simplePropertyTypeMap.put(propTuple, getterPropClass);
				} else if (!setterPropClass.equals(getterPropClass)) {
					throw new RuntimeException(String.format("The return type of method %s in class %s with "
							+ "@SimplePropertySetter should be %s, which is the type of the first argument of corresponding setter method.",
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
				Tuple attrTuple = Tuple.oF(newPropIndex, tp.firstline());
				simplePropertyIndexList.add(Tuple.oF(propTuple, attrTuple));
				
				
			}
			
		}
	}
	
	private void processMethodAsElement(Class<? extends TrainGraphPart> clazz,
			TGPElement te, Method method, int propIndex) {
		
	}
	
	protected String getTGPProperty(Tuple<String, String> propTuple) {
		Function<TrainGraphPart, String> getter = simplePropertyGetterMap.get(propTuple);
		
		if (getter != null) {
			return getter.apply(this);
		} else {
			return "";
		}
	}

	protected void setTGPProperty(TrainGraphPart obj, String propName, String valueInStr) {
		Tuple propTuple = Tuple.oF(obj.getClass().getName(), propName);
		BiConsumer<TrainGraphPart, String> setter = simplePropertySetterMap.get(propTuple);
		
		if (setter != null) {
			setter.accept(obj, valueInStr);
		}
	}
	
	/**************************************************************************
	 * Methods need to be implemented.
	 * Element data accessers which will be used in save/load template methods
	 **************************************************************************/
	

	
	protected abstract String getStartSectionString();
	protected abstract String getEndSectionString();
	String createTGPNameById(int id) { return null; }
	protected abstract Supplier<? extends TrainGraphPart> getConstructionFunc();	
	public abstract void registerSubclasses();
	void initElements() {}
	void setToDefault() {}
	
	/* Binary coding */
	protected boolean isBase64Encoded() { return false; }
	protected String encodeToBase64() { return ""; }
	protected void decodeFromBase64Start() {};
	protected void decodeFromBase64NewLine(String base64Line) {};
	protected void decodeFromBase64End() {};
	
	/* Object Properties 
	 * Must be overridden by sub-classes that has object properties */
	protected void getObjectTGPProperties() {};
	protected void setObjectTGPProperties(TrainGraphPart part) {};

	/* Element array */
	protected Vector<ET> getTGPElements() { return null; }
	protected void addTGPElement(ET element) {}
	protected boolean isOfElementType(TrainGraphPart part) {return false;}
	
	
	/* Do complete work after all data loaded from file */
	protected abstract void loadComplete();
	
	/**************************End of Abstract Methods*********************/

	public void registerClasses() {		
		_partClassMap.putIfAbsent(getStartSectionString().trim(), 
				Tuple.of(getClass(), getConstructionFunc()) );
		_partClassMap.putIfAbsent(getClass().getName(), 
				Tuple.of(getClass(), getConstructionFunc()) );
		
		_objectIdMap.putIfAbsent(getClass().getName(), 0);
		
		registerSubclasses();
	}
	
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
		saveToWriter(writer, 0);
		writer.flush();
	}
	
	public void saveToStream(OutputStream out, int identLevel) throws IOException {
		Writer writer = new OutputStreamWriter(out, "utf-8");
		saveToWriter(writer, identLevel);
		writer.flush();
	}
	
	protected void saveToWriter(Writer writer, int identLevel) throws IOException {
		Vector<ET> elements = null;
		// Write section begin string
		_printIdent(writer, identLevel, false);
		_print(writer, _getStartSectionString());
		
		if (isBase64Encoded()) {
			// Write base64 codes for binary contents
			
			writer.append( encodeToBase64() );
			_println(writer);
		} else {			
			// Write simple properties
			saveSimplePropertiesToWriter(writer, identLevel, isInOneLine());
			
			// Write object properties
			getObjectTGPProperties();
			if (objectProperties.size() > 0) {
				_println(writer);
				for (Tuple<String, TrainGraphPart> elementTuple : objectProperties) {
					elementTuple.B.saveToWriter(writer, identLevel + 1);
				}
			}
			
			// Write element arrays
			elements = getTGPElements();
			if (elements != null) {
				_println(writer);
				for (ET element : getTGPElements()) {
					element.saveToWriter(writer, identLevel + 1);
				}
			}
		}
		
		// Write section end string
		if (!isInOneLine() && objectProperties.size() == 0 && 
				(elements == null || elements.size() == 0)) {
			_println(writer);
		}
		_printIdent(writer, identLevel, isInOneLine());
		_print(writer, getEndSectionString());
	}
	
	private void saveSimplePropertiesToWriter(Writer writer, int identLevel, boolean inOneLine) {
		String className = getClass().getName();
		List<Tuple<Tuple<String, String>, Tuple<Integer, Boolean> >> propKeyList= simplePropertyIndexMap.get(className);
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
		int[] intFlags = {0}; // {iterationIndex}
		propKeyList.stream().sorted((tuple1, tuple2) -> {
			// tuple.A is (className, propName), tuple.B is (propIndex, isFirstLine)
			Tuple<Integer, Boolean> prop1Attr = tuple1.B;
			Tuple<Integer, Boolean> prop2Attr = tuple2.B;
			
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
			Tuple<String, String> propTuple = tuple.A;
			Tuple<Integer, Boolean> propAttr = tuple.B;
			
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
			
			String propValue = getTGPProperty(propTuple);
			propValue = _encode(propValue);
			

			_print(writer, "%s=%s,", propTuple.B, propValue);
			
			// ++ index
			++ intFlags[0];
		});
	}
	
	private String _getStartSectionString() {
		String string = getStartSectionString();

		IN_ONE_LINE = string == null || !string.endsWith("\r\n");
		
		return string;
	}
	
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
		Stack<TrainGraphPart> parsingNodeStack = new Stack<TrainGraphPart>();
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
			Stack<TrainGraphPart> parsingNodeStack, Vector<String> errMsgs, 
			TrainGraphPart loadingNode) {
		
		TrainGraphPart parentPart = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
		
		// Step 1. try to interpret this line as a start of an object.
		TrainGraphPart thisObj = createObject(line, lineNum, parsingNodeStack, errMsgs, loadingNode);
		if (thisObj != null)
			parsingNodeStack.push(thisObj);
		
		if (thisObj instanceof UnknownPart)
			return thisObj;
		
		if (parentPart != null) {
			if (parentPart instanceof UnknownPart) {
				// skip current line.
			} if (parentPart.isBase64Encoded()) {
				parentPart.decodeFromBase64NewLine(line);
			} else {
				
				// Step 2. try to find "name=value," patterns for properties
				TrainGraphPart objToBeAssigned = parsingNodeStack.peek();
				if (objToBeAssigned != null) {
					String propLine = line.replaceFirst("^[^\\{]*\\{", "").replaceFirst("\\}.*", "");
					String[] assignments = propLine.split(",");
					for (String assignment : assignments) {
						String[] strParts = assignment.split("=");
						if (strParts.length >= 2) {
							objToBeAssigned.setTGPProperty(objToBeAssigned, 
									strParts[0], _decode(strParts[1]));
						} else {
							objToBeAssigned.setTGPProperty(objToBeAssigned, 
									strParts[0], "");
						}
					}
				}
				
				// Step 3. try to read as an object property or an element.
				if (thisObj != null && parentPart != null) {
					if (parentPart.isOfElementType(thisObj)) {
						// Add thisObj as an element of parantPart
						parentPart.addTGPElement(thisObj);
					} else {
						// Set thisObj as an object property of parantPart
						parentPart.setObjectTGPProperties(thisObj);
					}
				}
			}
		}
		
		if (line.contains("}")) {
			if (parentPart != null && parentPart.isBase64Encoded()) {
				parentPart.decodeFromBase64End();
			}
			
			TrainGraphPart part = parsingNodeStack.pop();
			part.loadComplete();
		}
		
		return parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
	}
	
	protected static TrainGraphPart createObject(String line, int lineNum, 
			Stack<TrainGraphPart> parsingNodeStack, Vector<String> errMsgs, 
			TrainGraphPart loadingNode) {
		
		// Part object starts with a "{". If no brace found, then no object is created.
		if (!line.contains("{"))
			return null;
		
		TrainGraphPart stackTop = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
		if (stackTop != null && stackTop instanceof UnknownPart) {
			// If the current scope is an unknown part, then treat current part
			// as an unknown part too, because there is no need to parse the content
			// of an unknown part.
			return new UnknownPart();
		}
		
		String startSectionString = line.replaceFirst("\\{.*", "{");
		Tuple<Class<? extends TrainGraphPart>, Supplier<? extends TrainGraphPart>> partClassTuple = 
				_partClassMap.get(startSectionString);
		
		if (partClassTuple != null) {
			/* If loadingNode is not null and it is an instance of corresponding
			 * class to current line being parsed, then use it as the current object
			 * instead of creating new one.
			 */
			if (loadingNode != null && partClassTuple.A.isInstance(loadingNode)) {
				if (loadingNode.isBase64Encoded()) {
					loadingNode.decodeFromBase64Start();
				}
				return loadingNode;
			} else {
				Class<? extends TrainGraphPart> clazz = partClassTuple.A;
				String className = clazz.getName();
				TrainGraphPart obj = null;
				
				try {
					obj = partClassTuple.A.newInstance(); // partClassTuple.B.get();
					obj.initElements();
					
					if (obj.isBase64Encoded()) {
						obj.decodeFromBase64Start();
					}
				} catch (InstantiationException | IllegalAccessException e) {
					obj = createUnknownPartint(line, lineNum, parsingNodeStack, errMsgs);
					((UnknownPart) obj).message = 
							String.format(__("Cannot call the default constructor with no arguments on %s class. "
							+ "This object and all its content is skipped."), className);
					e.printStackTrace();
				} 
				
				return obj;
			}
		} else {
			// A top level unknown part encountered during parsing.
			UnknownPart obj = createUnknownPartint(line, lineNum, parsingNodeStack, errMsgs);
			
			return obj;
		}
	}
	
	private static UnknownPart createUnknownPartint(String line, int lineNum, 
			Stack<TrainGraphPart> parsingNodeStack, Vector<String> errMsgs) {

		UnknownPart obj = new UnknownPart();
		obj.startLineIndex = lineNum;
		obj.startLine = line;
		obj.topLevel = true;
		
		return obj;
		
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
			writer.append("\r\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}	
	
	private boolean isInOneLine() {		
		return IN_ONE_LINE;
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
	
	public static void setSimpleToString() {
		TO_STRING_ELEMENT_DEPTH = -1;
		
		// Init for toDebugString
		TO_STRING_SHOW_TYPE = true;
		TO_STRING_SHOW_PROPERTIES = false;
		TO_STRING_SPECIFIC_TYPE_DEPTH.put(START_SECTION_ALL_TRAIN, 0);
		TO_STRING_SPECIFIC_TYPE_DEPTH.put(START_SECTION_RAILINE_CHART, 0);
		TO_STRING_SPECIFIC_TYPE_DEPTH.put(START_SECTION_RAILROAD_LINE, 0);
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
	
	public String repr(int elementDepth, 
			boolean showType, boolean showProperties) {
		
		if (elementDepth < 0) {
			return getName();
		} else {
			StringWriter writer = new StringWriter();
			
			if (showType) {
				writer.append(getStartSectionString().replace("{\r\n", "{"));
			} else {
				writer.append("{");
			}
			
			// Print simple properties
			if (showProperties) {
				writer.append("id=" + _id + ",");
				saveSimplePropertiesToWriter(writer, 0, true);
			} else {
				if (showType) {
					writer.append(String.format("id=%d,name=%s", _id, getName()));
				} else {
					writer.append(getName() + "@" + _id);
				}
			}
			
			String propertiesRepr=null, elementsRepr=null;
			
			// Print object properties
			getObjectTGPProperties();
			propertiesRepr = objectProperties.stream()
				.map(propertyTuple -> {
					String aString = propertyTuple.A + "=";
					aString += propertyTuple.B.repr(
						TO_STRING_SPECIFIC_TYPE_DEPTH.getOrDefault(
								propertyTuple.B.getStartSectionString(), elementDepth - 1),
						showType, showProperties);
					return aString;})
				.collect(Collectors.joining(","));
			
			// Print elements
			if (elementDepth > 0) {
				if (getTGPElements() != null) {
					elementsRepr = getTGPElements().stream().map(element -> 
										element.repr(TO_STRING_SPECIFIC_TYPE_DEPTH.getOrDefault(
												element.getStartSectionString(), elementDepth - 1),
											showType, showProperties))
									.collect(Collectors.joining(",", "[", "]"));
				}
			}
			
			if (!"".equals(propertiesRepr) || elementsRepr != null) {
				writer.append(": ");
				if (propertiesRepr != null) {
					writer.append(propertiesRepr);
				}
				if (elementsRepr != null) {
					if (!"".equals(propertiesRepr)) {
						writer.append(", ");
					}

					writer.append("elements=" + getTGPElements().size());
					writer.append(elementsRepr);
				}
			}
			
			writer.append("}");
			
			
			return writer.toString();
		}
	}
	
	private static String _encode(String str) {
		return str == null ? null : str.replace(',', '`').replace('=', '|');
	}
	
	private static String _decode(String str) {
		return str == null ? null : str.replace('`', ',').replace('|', '=');		
	}

}
