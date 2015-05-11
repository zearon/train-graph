package org.paradise.etrc.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.paradise.etrc.data.util.Tuple;


/**
 * 所有组成运行图的部件的基类
 * T是继承该类的类型本身,
 * ET是该类的元素的类型
 * @author Jeff Gong
 *
 */
@SuppressWarnings("rawtypes")
public abstract class TrainGraphPart<T, ET extends TrainGraphPart> {
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
	
	/* Field used by reflection code to determine the class of element.
	 * No need to set value. */
//	public ET _elementInstance = null;
	
	protected Vector<Tuple<String, TrainGraphPart>> objectProperties = new Vector<Tuple<String, TrainGraphPart>>();
	/* Store a (Class, Class constructor) tuple for each sub-class */
	protected static HashMap<String, 
		Tuple<Class<? extends TrainGraphPart>, Supplier<? extends TrainGraphPart>>> _partClassMap = 
			new HashMap<String, Tuple<Class<? extends TrainGraphPart>,Supplier<? extends TrainGraphPart>>> ();

	static HashMap<String, Integer> _objectIdMap = new HashMap<String, Integer> ();
	
	protected int _id;
	public String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
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
	
	/**************************************************************************
	 * Methods need to be implemented.
	 * Element data accessers which will be used in save/load template methods
	 **************************************************************************/
	

	
	protected abstract String getStartSectionString();
	protected abstract String getEndSectionString();
	String createTGPNameById(int id) { return null; }
	protected abstract Supplier<? extends TrainGraphPart> getConstructionFunc();	
	public abstract void _prepareForFirstLoading();
	void initTGP() {}
	void setToDefault() {}
	
	/* Properties */
	protected abstract Tuple<String, Class<?>>[] getSimpleTGPProperties();
	protected abstract void setTGPProperty(String name, String valueInStr);
	protected abstract String getTGPPropertyReprStr(int index);
	
	/* Object Properties 
	 * Must be overridden by sub-classes that has object properties */
	protected void getObjectTGPProperties() {};
	protected void setObjectTGPProperties(TrainGraphPart part) {};

	/* Element array */
	protected abstract Vector<ET> getTGPElements();
	protected abstract void addTGPElement(ET element);
	protected abstract boolean isOfElementType(TrainGraphPart part);
	
	
	/* Do complete work after all data loaded from file */
	protected abstract void loadComplete();
	
	/**************************End of Abstract Methods*********************/

	public void prepareForFirstLoading() {		
		_partClassMap.putIfAbsent(getStartSectionString().trim(), 
				Tuple.of(getClass(), getConstructionFunc()) );
		_partClassMap.putIfAbsent(getClass().getName(), 
				Tuple.of(getClass(), getConstructionFunc()) );
		
		_objectIdMap.putIfAbsent(getClass().getName(), 0);
		
		_prepareForFirstLoading();
	}
	
	public void saveToFile(String fileName) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(fileName);
			saveToWriter(writer, 0);
		} catch (IOException e) {
			throw e;
		} finally {
			if (writer != null) {
				try { writer.close(); } catch (Exception e) {}
			}
		}		
	}
	
	public void print(OutputStream out) throws IOException {
		Writer writer = new OutputStreamWriter(out);
		saveToWriter(writer, 0);
		writer.flush();
	}
	
	public void saveToWriter(Writer writer, int identLevel) throws IOException {
		// Write section begin string
		_printIdent(writer, identLevel, false);
		_print(writer, _getStartSectionString());
		
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
		Vector<ET> elements = getTGPElements();
		if (elements != null) {
			_println(writer);
			for (ET element : getTGPElements()) {
				element.saveToWriter(writer, identLevel + 1);
			}
		}
		
		// Write section end string
		_printIdent(writer, identLevel, isInOneLine());
		_print(writer, getEndSectionString());
	}
	
	private void saveSimplePropertiesToWriter(Writer writer, int identLevel, boolean inOneLine)
			throws IOException {
		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
		int propIndex = 0;
		int propCount = propTuples.length;
		Tuple<String, Class<?>> currentProperty = null;

		_printIdent(writer, identLevel + 1, inOneLine);
		for (; propIndex < propCount; ++ propIndex) {
			if (propIndex == 1 && !inOneLine) {
				_println(writer);
				_printIdent(writer, identLevel + 1, inOneLine);
			}
			currentProperty = propTuples[propIndex];
			String propRepr = _encode(getTGPPropertyReprStr(propIndex));
			_print(writer, "%s=%s,", currentProperty.A, propRepr);
		}
	}
	
	private String _getStartSectionString() {
		String string = getStartSectionString();

		IN_ONE_LINE = string == null || !string.endsWith("\r\n");
		
		return string;
	}
	
//	/**
//	 * 第一次调用本函数前,应先调用prepareForFirstLoading.
//	 * @param fileName
//	 * @return
//	 * @throws IOException
//	 */
//	protected T _loadFromFile(String fileName) throws IOException {
//		FileReader reader0 = null;
//		try {
//			reader0 = new FileReader(fileName);
//			return (T) TrainGraphPart.loadFromReader(reader0, this.getClass(), this);
//		} catch (IOException ioe) {
//			throw ioe;
//		} catch (Exception e) {
//			throw new IOException("运行图文件格式错误!", e);
//		} finally {
//			if (reader0 != null) {
//				try { reader0.close(); } catch (Exception e) {}
//			}
//		}
//	}
	
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
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (classObjRoot == null) {
				root = parseLine(line, loadingNode);
			} else {
				root = parseLine(line, null);
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
	protected static Stack<TrainGraphPart> parsingNodeStack = new Stack<TrainGraphPart>();
	
	protected static TrainGraphPart parseLine(String line, TrainGraphPart loadingNode) {
		TrainGraphPart parentPart = parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
		
		// Step 1. try to interpret this line as a start of an object.
		TrainGraphPart thisObj = createObject(line, loadingNode);
		if (thisObj != null)
			parsingNodeStack.push(thisObj);
		
		// Step 2. try to find "name=value," patterns for properties
		TrainGraphPart objToBeAssigned = parsingNodeStack.peek();
		if (objToBeAssigned != null) {
			String propLine = line.replaceFirst("^[^\\{]*\\{", "").replaceFirst("\\}.*", "");
			String[] assignments = propLine.split(",");
			for (String assignment : assignments) {
				String[] strParts = assignment.split("=");
				if (strParts.length >= 2) {
					objToBeAssigned.setTGPProperty(strParts[0], _decode(strParts[1]));
				}
			}
		}
		
		if (thisObj != null && parentPart != null) {
			if (parentPart.isOfElementType(thisObj)) {
				// Add thisObj as an element of parantPart
				parentPart.addTGPElement(thisObj);
			} else {
				// Set thisObj as an object property of parantPart
				parentPart.setObjectTGPProperties(thisObj);
			}
		}
		
		if (line.contains("}")) {
			TrainGraphPart part = parsingNodeStack.pop();
			part.loadComplete();
		}
		
		return parsingNodeStack.isEmpty() ? null : parsingNodeStack.peek();
	}
	
	protected static TrainGraphPart createObject(String line, TrainGraphPart loadingNode) {
		String startSectionString = line.replaceFirst("\\{.*", "{");
		Tuple<Class<? extends TrainGraphPart>, Supplier<? extends TrainGraphPart>> partClassTuple = 
				_partClassMap.get(startSectionString);
		
		if (partClassTuple != null) {
			/* If loadingNode is not null and it is an instance of corresponding
			 * class to current line being parsed, then use it as the current object
			 * instead of creating new one.
			 */
			if (loadingNode != null && partClassTuple.A.isInstance(loadingNode)) {
				return loadingNode;
			} else {
				TrainGraphPart obj = partClassTuple.B.get();
				TrainGraphFactory.setID(partClassTuple.A, obj);
				obj.initTGP();
				return obj;
			}
		} else {
			return null;
		}
	}
	
	
	
	
	private void _print(Writer writer, String formatStr, Object... params) 
			throws IOException {
		
		writer.append(String.format(formatStr, params));
	}
	
	private void _println(Writer writer) throws IOException {
		writer.append("\r\n");
	}	
	
	private boolean isInOneLine() {		
		return IN_ONE_LINE;
	}
	
	private void _printIdent(Writer writer, int identLevel, boolean notNeedIdent) throws IOException {
		if (notNeedIdent)
			return;
		
		while (identLevel -- > 0) {
			writer.append(IDENT_STR);
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
				try {
					saveSimplePropertiesToWriter(writer, 0, true);
				} catch (IOException e) {}
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
		return str.replace(',', '`').replace('=', '|');
	}
	
	private static String _decode(String str) {
		return str.replace('`', ',').replace('|', '=');		
	}

}
