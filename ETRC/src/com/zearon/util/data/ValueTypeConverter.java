package com.zearon.util.data;

import java.awt.Color;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import org.paradise.etrc.data.TrainGraphPart;

import static org.paradise.etrc.ETRC.__;

public class ValueTypeConverter {
	public static final String NULL_STR = "$NULL$";
	
	static final Class<?>[] PRIMITIVE_TYPES_ARRAY = {
		boolean.class,
		byte.class,
		int.class,
		long.class,
		float.class,
		double.class};
	static final List<Class<?>> PRIMITIVE_TYPES = Arrays.asList(PRIMITIVE_TYPES_ARRAY);
	static final Class<?>[] PRIMITIVE_ARRAY_TEYPS_ARRAY = {
		new boolean[0].getClass(), new Boolean[0].getClass(), 
		new byte[0].getClass(), new Byte[0].getClass(), 
		new int[0].getClass(), new Integer[0].getClass(), 
		new long[0].getClass(), new Long[0].getClass(), 
		new float[0].getClass(), new Float[0].getClass(), 
		new double[0].getClass(), new Double[0].getClass(),
		new Color[0].getClass()};
	static final List<Class<?>> PRIMITIVE_ARRAY_TYPES = Arrays.asList(PRIMITIVE_ARRAY_TEYPS_ARRAY);
	
	static HashMap<Tuple2<Class<?>, Class<?>>, Function<? extends Object, Object>> typeConverterDict = 
			new LinkedHashMap<>();
	
	static {
		registerPrimitiveTypes();
		registerArrayTypes();
	}
	
	public static void registerTypeConverter(Tuple2<Class<?>, Class<?>> typeTuple, 
			Function<? extends Object, Object> typeConverter) {
		
		typeConverterDict.put(typeTuple, typeConverter);
	}
	
	public static void registerPrimitiveTypes() {
		PRIMITIVE_TYPES.forEach(type -> {
			Function<? extends Object, Object> converter = null;
			if("byte".equals(type.getName()) ) {
				converter = (String str) -> Byte.valueOf(str);
			} else if("int".equals(type.getName()) ) {
				converter = (String str) -> Integer.valueOf(str);
			} else if("long".equals(type.getName()) ) {
				converter = (String str) -> Long.valueOf(str);
			} else if ("float".equals(type.getName()) ) {
				converter = (String str) -> Float.valueOf(str);
			} else if ("double".equals(type.getName()) ) {
				converter = (String str) -> Double.valueOf(str);
			} else if ("boolean".equals(type.getName()) ) {
				converter = (String str) -> Boolean.valueOf(str);
			}
			
			if (type.isPrimitive()) {
				registerTypeConverter(Tuple2.of(String.class, type), converter);
				Class<?> clazz = convertPrimitiveTypeToClass(type);
				registerTypeConverter(Tuple2.of(String.class, clazz), converter);
			}
		});

		registerTypeConverter(Tuple2.of(Color.class, String.class), 
				ValueTypeConverter::colorToString);
		registerTypeConverter(Tuple2.of(String.class, Color.class), 
				ValueTypeConverter::stringToColor);
	}
	
	@SuppressWarnings("unchecked")
	public static void registerArrayTypes() {
		PRIMITIVE_ARRAY_TYPES.forEach(arrayType -> {
			if (!arrayType.isArray()) {
				return;
			}
			
			Function<? extends Object, Object> arrayToStringConverter = null;
			Function<? extends Object, Object> stringToArrayConverter = null;

			Class<?> elementType = arrayType.getComponentType();
			Function<Object, Object> elementToStringConverter = (Function<Object, Object>) 
					typeConverterDict.get(Tuple2.of(elementType, String.class));
			if (elementToStringConverter == null)
				elementToStringConverter = object -> object.toString();
			
			Function<Object, Object> elementToStringConverter0 = elementToStringConverter;
			arrayToStringConverter = array -> arrayToString(array, arrayType, 
					elementToStringConverter0);
			registerTypeConverter(Tuple2.of(arrayType, String.class), arrayToStringConverter);
			
			Function<Object, Object> stringToElementConverter = (Function<Object, Object>) 
					typeConverterDict.get(Tuple2.of(String.class, elementType));
			if (stringToElementConverter != null) {
				stringToArrayConverter = (String arrayRepr) -> stringToArray(arrayRepr, 
						elementType, stringToElementConverter);
				registerTypeConverter(Tuple2.of(String.class, arrayType), stringToArrayConverter);
			}
		});
	}
	
	public static Object arrayToString(Object array, Class<?> arrayType,
			Function<Object, Object> elementToStringConverter) {
		
		if (array == null)
			return NULL_STR;
		
		if (!arrayType.isInstance(array))
			throw new IllegalArgumentException(String.format("Array to be converted is not an instance of %s[]", 
					arrayType.getComponentType().getName()));
		
		StringBuilder sb = new StringBuilder();
		int arrayLen = Array.getLength(array);
		sb.append(arrayLen);
		sb.append("<");
		for (int i = 0; i < arrayLen; ++ i) {
			Object element = Array.get(array, i);
			String str = (String) elementToStringConverter.apply(element);
			str = str == null ? NULL_STR : TrainGraphPart._encode(str, false);
			if (i != 0)
				sb.append(";");
			sb.append(str);
		}
		sb.append(">");
		
		return sb.toString();
	}
	
	public static Object stringToArray(String arrayRepr, Class<?> componentType,
			Function<Object, Object> stringToElementConverter) {
		
		if (!(arrayRepr instanceof String))
			return null;
		
		if (NULL_STR.equals(arrayRepr))
			return null;
			
		// strip out < and >
		boolean isEmpty = "0<>".equals(arrayRepr);
		String elementReprs = arrayRepr.replaceFirst("^.*?<", "").replaceFirst(">$", "");
		String[] elements = ((String) elementReprs).split(";");
		int arrayLen = isEmpty ? 0 : elements.length;
		Object array = Array.newInstance(componentType, arrayLen);
		for (int i = 0; i < arrayLen; ++ i) {
			String repr = elements[i];
			Object element = null;
			if (!NULL_STR.equals(repr)) {
				element = stringToElementConverter.apply(repr);
			}
			Array.set(array, i, element);
		}
		
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public static Object convertType(Object value, Class<? extends Object> srcType, Class<? extends Object> destType) {
		String srcTypeName = srcType.getName();
		String destTypeName = destType.getName();
		
		if (isTheSameType(srcType, destType)) {
			return value;
		}
		
		Function<Object, Object> converter = (Function<Object, Object>) typeConverterDict.get(
				Tuple2.of(srcType, destType));
		if (converter != null) {
			try {
				return converter.apply(value);
			} catch (Exception e) {
				throw new RuntimeException(String.format("Error occurs when converting %s from %s to %s", 
						value, srcTypeName, destTypeName), e);
			}
		}
		
		if (String.class.getName().equals(destTypeName) ) {
			if (value == null)
				return NULL_STR;
			
			String repr = value.toString();
			
			// : is used as separator character between elements in an array
			if (srcType.isArray())
				return repr;
			else
				return repr.replace(":", "$COLOM$");
		}
		
		throw new IllegalArgumentException(String.format("Cannot convert value from type %s to type %s", 
				srcTypeName, destTypeName));
	}
	
	protected static boolean isTheSameType(Class<?> type1, Class<?> type2) {
		if (type1.equals(type2)) {
			return true;
		}
		
		type1 = convertPrimitiveTypeToClass(type1);
		type2 = convertPrimitiveTypeToClass(type2);
		
		return type1.equals(type2);
	}
	
	public static Class<?> convertPrimitiveTypeToClass(Class<?> type) {
		if(byte.class.equals(type)) {
			return Byte.class;
		} else if(int.class.equals(type)) {
			return Integer.class;
		} else if(long.class.equals(type)) {
			return Long.class;
		} else if (float.class.equals(type)) {
			return Float.class;
		} else if (double.class.equals(type)) {
			return Double.class;
		} else if (boolean.class.equals(type)) {
			return Boolean.class;
		}
		
		return type;
//		throw new RuntimeException(type.getName() + " is not a primitive type, and thus can not be converted to corresponding class.");
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