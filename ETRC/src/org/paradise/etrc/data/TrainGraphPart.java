package org.paradise.etrc.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

/**
 * 所有组成运行图的部件的基类
 * T是继承该类的类型本身,
 * ET是该类的元素的类型
 * @author Jeff Gong
 *
 */
public abstract class TrainGraphPart<T, ET extends TrainGraphPart> {
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
	
	public static final String START_SECTION_RAILROAD_NETWORK 	= "RailNetwork {\r\n";
	public static final String END_SECTION_RAILROAD_NETWORK 	= "} RailNetwork\r\n";
	public static final String START_SECTION_RAILROAD_LINE 		= "RailLine {\r\n";
	public static final String END_SECTION_RAILROAD_LINE 		= "} RailLine\r\n";
	public static final String START_SECTION_STATION 			= "Station {";
	public static final String END_SECTION_STATION 				= "}\r\n";
	
	public static final String START_SECTION_ALL_TRAIN			= "All Trains {\r\n";
	public static final String END_SECTION_ALL_TRAIN			= "} All Trains\r\n";
	public static final String START_SECTION_TRAIN				= "Train {\r\n";
	public static final String END_SECTION_TRAIN				= "} Train\r\n";
	public static final String START_SECTION_STOP				= "Stop {";
	public static final String END_SECTION_STOP 				= "}\r\n";
	
	public static final String START_SECTION_RAILNETWORK_CHART	= "RailNetwork Chart {\r\n";
	public static final String END_SECTION_RAILNETWORK_CHART	= "} RailNetwork Chart\r\n";
	public static final String START_SECTION_RAILINE_CHART				= "RailLine Chart {\r\n";
	public static final String END_SECTION_RAILINE_CHART				= "} RailLine Chart\r\n";
	public static final String START_SECTION_TRAIN_REF			= "Train Ref {";
	public static final String END_SECTION_TRAIN_REF			= "}\r\n";
	
	/* Field used by reflection code to determine the class of element.
	 * No need to set value. */
//	public ET _elementInstance = null;
	
	protected Vector<TrainGraphPart> objectProperties = new Vector<TrainGraphPart>();
	/* Store a (Class, Class constructor) tuple for each sub-class */
	protected static HashMap<String, 
		Tuple<Class<? extends TrainGraphPart>, Supplier<? extends TrainGraphPart>>> _partClassMap = 
			new HashMap<String, Tuple<Class<? extends TrainGraphPart>,Supplier<? extends TrainGraphPart>>> ();

	static HashMap<String, Integer> _objectIdMap = new HashMap<String, Integer> ();
	
	protected int _id;
	public String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
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
		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
		int propIndex = 0;
		int propCount = propTuples.length;
		Tuple<String, Class<?>> currentProperty = null;

		_printIdent(writer, identLevel + 1, isInOneLine());
		for (; propIndex < propCount; ++ propIndex) {
			if (propIndex == 1 && !isInOneLine()) {
				_println(writer);
				_printIdent(writer, identLevel + 1, isInOneLine());
			}
			currentProperty = propTuples[propIndex];
			String propRepr = _encode(getTGPPropertyReprStr(propIndex));
			_print(writer, "%s=%s,", currentProperty.A, propRepr);
		}
		
		// Write object properties
		getObjectTGPProperties();
		if (objectProperties.size() > 0) {
			_println(writer);
			for (TrainGraphPart element : objectProperties) {
				element.saveToWriter(writer, identLevel + 1);
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
				return partClassTuple.B.get();
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
	
	private static String _encode(String str) {
		return str.replace(',', '`').replace('=', '|');
	}
	
	private static String _decode(String str) {
		return str.replace('`', ',').replace('|', '=');		
	}

}
