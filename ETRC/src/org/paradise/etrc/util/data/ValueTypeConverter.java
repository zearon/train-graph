package org.paradise.etrc.util.data;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;

import com.sun.xml.internal.bind.v2.runtime.Name;

import static org.paradise.etrc.ETRC.__;

public class ValueTypeConverter {
	static final String[] PRIMITIVE_TYPES_ARRAY = {
		boolean.class.getName(), Boolean.class.getName(), 
		byte.class.getName(), Byte.class.getName(),
		int.class.getName(), Integer.class.getName(), 
		long.class.getName(), Long.class.getName(), 
		float.class.getName(), Float.class.getName(), 
		double.class.getName(), Double.class.getName()};
	static final List<String> PRIMITIVE_TYPES = Arrays.asList(PRIMITIVE_TYPES_ARRAY);
	
	static HashMap<Tuple2<String, String>, Function<? extends Object, Object>> typeConverterDict = 
			new LinkedHashMap<>();
	
	static {
		PRIMITIVE_TYPES.forEach(typeName -> {
			boolean isPrimitive = false;
			Function<? extends Object, Object> converter = null;
			if("byte".equals(typeName) ) {
				isPrimitive = true;
				converter = (String str) -> Byte.valueOf(str);
			} else if("int".equals(typeName) ) {
				isPrimitive = true;
				converter = (String str) -> Integer.valueOf(str);
			} else if("long".equals(typeName) ) {
				isPrimitive = true;
				converter = (String str) -> Long.valueOf(str);
			} else if ("float".equals(typeName) ) {
				isPrimitive = true;
				converter = (String str) -> Float.valueOf(str);
			} else if ("double".equals(typeName) ) {
				isPrimitive = true;
				converter = (String str) -> Double.valueOf(str);
			} else if ("boolean".equals(typeName) ) {
				isPrimitive = true;
				converter = (String str) -> Boolean.valueOf(str);
			}
			
			if (isPrimitive) {
				registerTypeConverter(Tuple2.oF(String.class.getName(), typeName), converter);
				String className = convertPrimitiveTypeNameToCName(typeName);
				registerTypeConverter(Tuple2.oF(String.class.getName(), className), converter);
			}
		});

		registerTypeConverter(Tuple2.oF(Color.class.getName(), String.class.getName()), 
				ValueTypeConverter::colorToString);
		registerTypeConverter(Tuple2.oF(String.class.getName(), Color.class.getName()), 
				ValueTypeConverter::stringToColor);
	}
	
	public static void registerTypeConverter(Tuple2<String, String> typeNamesTyple, 
			Function<? extends Object, Object> typeConverter) {
		
		typeConverterDict.put(typeNamesTyple, typeConverter);
	}
	
	public static Object convertType(Object value, Class<? extends Object> srcType, Class<? extends Object> destType) {
		String srcTypeName = srcType.getName();
		String destTypeName = destType.getName();
		
		if (isTheSameType(srcTypeName, destTypeName)) {
			return value;
		}
		
		Function<Object, Object> converter = (Function<Object, Object>) typeConverterDict.get(
				Tuple2.oF(srcTypeName, destTypeName));
		if (converter != null) {
			return converter.apply(value);
		}
		
		if (String.class.getName().equals(destTypeName) ) {
			return value == null ? null : value.toString();
		}
		
		throw new IllegalArgumentException(String.format("Cannot convert value from type %s to type %s", 
				srcTypeName, destTypeName));
	}
	
	protected static boolean isTheSameType(String typeName1, String typeName2) {
		if (typeName1.equals(typeName2)) {
			return true;
		}
		
		typeName1 = convertPrimitiveTypeNameToCName(typeName1);
		typeName2 = convertPrimitiveTypeNameToCName(typeName2);
		
		return typeName1.equals(typeName2);
	}
	
	public static boolean isPrimitiveType(String typeName) {
		typeName = convertPrimitiveTypeNameToCName(typeName);
		return PRIMITIVE_TYPES.contains(typeName);
	}
	
	public static String convertPrimitiveTypeNameToCName(String name) {
		
		if("byte".equals(name) ) {
			return "java.lang.Byte";
		} else if("int".equals(name) ) {
			return "java.lang.Integer";
		} else if("long".equals(name) ) {
			return "java.lang.Long";
		} else if ("float".equals(name) ) {
			return "java.lang.Float";
		} else if ("double".equals(name) ) {
			return "java.lang.Double";
		} else if ("boolean".equals(name) ) {
			return "java.lang.Boolean";
		}
		
		return name;
	}
	
	public static Object stringToPrimitive(String strValue, String propertyClassName) {
		if (strValue == null)
			return null;
		
		Object modelValue = null;
		
		if("byte".equals(propertyClassName) || "java.lang.Byte".equals(propertyClassName) ) {
			modelValue = Byte.valueOf(strValue);
		} else if("int".equals(propertyClassName) || "java.lang.Integer".equals(propertyClassName) ) {
			modelValue = Integer.valueOf(strValue);
		} else if("long".equals(propertyClassName) || "java.lang.Long".equals(propertyClassName) ) {
			modelValue = Long.valueOf(strValue);
		} else if ("float".equals(propertyClassName) || "java.lang.Float".equals(propertyClassName) ) {
			modelValue = Float.valueOf(strValue);
		} else if ("double".equals(propertyClassName) || "java.lang.Double".equals(propertyClassName) ) {
			modelValue = Double.valueOf(strValue);
		} else if ("boolean".equals(propertyClassName) || "java.lang.Boolean".equals(propertyClassName) ) {
			modelValue = Boolean.valueOf(strValue);
		} else if ("java.lang.String".equals(propertyClassName)) {
			modelValue = strValue;
		} else {
			throw new IllegalArgumentException(String.format(
					__("I Don't know how to convert the ui value in string type into a"
							+ " model value in (%s) type.")
							, propertyClassName));
		}
		
		return modelValue;
	}
	
	public static boolean isColorType(String typeName) {
		return java.awt.Color.class.getName().equals(typeName);
	}
	

	public static Object colorToString(Object color) {
		return color == null ? "" : "#" + Integer.toHexString(((Color)color).getRGB() & 0x11ffffff);
	}
	public static Object stringToColor(Object colorStr) {
		try {
			return Color.decode((String) colorStr);
		} catch (Exception e) {
			System.err.println("Invalid color string:" + colorStr);
			return Color.RED;
		}
	}
}