package com.zearon.util.config;

public class ConfigItem {

	public Class<?> valueType;
	public String propertyKeyName;
	public String propertyName;
	public String defaultValue;
	
	public ConfigItem(Class<?> valueType, String propertyKeyName, String propertyName,
			String defaultValue) {
		this.valueType = valueType;
		this.propertyKeyName = propertyKeyName;
		this.propertyName = propertyName;
		this.defaultValue = defaultValue;
	}

	public Class<?> getValueType() {
		return valueType;
	}

	public String getPropertyKeyName() {
		return propertyKeyName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isStringType() {
		return valueType.equals(String.class);
	}
	
	public boolean isBooleanType() {
		return valueType.equals(Boolean.class) || valueType.equals(Boolean.class);
	}
	
	public boolean isNumberType() {
		return valueType.equals(Byte.class) || valueType.equals(byte.class) ||
				valueType.equals(Short.class) || valueType.equals(short.class) ||
				valueType.equals(Integer.class) || valueType.equals(int.class) ||
				valueType.equals(Long.class) || valueType.equals(long.class) ||
				valueType.equals(Float.class) || valueType.equals(float.class) ||
				valueType.equals(Double.class) || valueType.equals(double.class);
	}
	
	public String getParseNumberFuncName() {
		if (valueType.equals(Byte.class) || valueType.equals(byte.class)) {
			return "Byte.parseByte";
		} else if (valueType.equals(Short.class) || valueType.equals(short.class)) {
			return "Short.parseShort";
		} else if (valueType.equals(Integer.class) || valueType.equals(int.class)) {
			return "Integer.parseInt";
		} else if (valueType.equals(Long.class) || valueType.equals(long.class)) {
			return "Long.parseLong";
		} else if (valueType.equals(Float.class) || valueType.equals(float.class)) {
			return "Float.parseFloat";
		} else if (valueType.equals(Double.class) || valueType.equals(double.class)) {
			return "Double.parseDouble";
		} else {
			return "";
		}
	}
}