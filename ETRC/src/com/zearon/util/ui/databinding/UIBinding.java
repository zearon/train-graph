package com.zearon.util.ui.databinding;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;

import com.zearon.util.data.Tuple2;
import com.zearon.util.ui.databinding.converter.IModelValueConverter;
import com.zearon.util.ui.databinding.converter.IValueTypeConverter;
import com.zearon.util.ui.databinding.converter.ValueConverterManager;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.DEBUG_MSG;

/**
 * UIBinding is used to bing a UI control and a data model.
 * @author Jeff Gong
 *
 * @param <M> Type of model value. For example.
 * @param <U> Type of UI value. For example, UVT should be text for JTextField
 */
public abstract class UIBinding<M, U> {
	//
	static HashMap<Tuple2<String, String>, IValueTypeConverter<? extends Object, ? extends Object>> modelValueConverterMap = 
			new HashMap<>();
	static HashMap<Tuple2<String, String>, IValueTypeConverter<? extends Object, ? extends Object>> uiValueConverterMap = 
			new HashMap<>();
	
	/**
	 * Register model value to UI value type converter. <br/>
	 * if UI value type U is String and model value type M is String, and
	 * modelValue.toString() is what you want, then youdon't need to register 
	 * a ValueTypeConverter<M, String>, since this function is built-in.
	 * @param converter A ValueTypeConverter type converter.
	 */
	public static <M, U> void registerModelValueTypeConverter(
			IValueTypeConverter<M, U> converter) {
		
		modelValueConverterMap.put(Tuple2.of(converter.getAValueType().getName(), 
				converter.getBValueType().getName()), converter);
	}
	
	/**
	 * Register ui value to movel value type converter. <br/>
	 * if ui value type U is String and model value type M is String too or classes
	 * corresponding to primitive types, then you don't need to register a 
	 * ValueTypeConverter<String, M>, since this function is built-in.
	 * @param converter A ValueTypeConverter type converter.
	 */
	public static <U, M> void registerUIValueTypeConverter(
			IValueTypeConverter<U, M> converter) {
		
		uiValueConverterMap.put(Tuple2.of(converter.getAValueType().getName(), 
				converter.getBValueType().getName()), converter);
	}
	
	String modelObjID;
	protected Object model;
	protected String propertyName;
	protected String propertyDesc;
	protected IModelValueConverter<M, U> converter;
	
	protected Function<Object, M> getter = null;
	protected BiConsumer<Object, M> setter = null;
//	protected Class<? extends Object> propertyClass = null;
	protected String propertyClassName = null;
	protected String uiValueClassName = null;
	
	protected Consumer<String> callback;
	protected Function<M, Boolean> valueValidator;
	
	protected UIBindingManager bindingManager;
	
	protected static Consumer<Exception> exceptionHandler;	
	public static void setExceptionHandler(Consumer<Exception> exceptionHandler) {
		UIBinding.exceptionHandler = exceptionHandler;
	}
	
	/**
	 * 
	 * @param model
	 * @param propertyName
	 * @param propertyDesc
	 * @param callback Actions to be taken after model are updated by UI component. Can be null.
	 */
	UIBinding(Object model, String propertyName, String propertyDesc, Consumer<String> callback) {
		this.model = model;
		this.propertyName = propertyName;
		this.propertyDesc = propertyDesc;
		this.callback = callback;
	}

	public Object getModel() {
		return model;
	}

	public void setModel(Object model) {
		this.model = model;
		
		if (model != null) {
			updateUI();
		}
	}

	public void setConverter(IModelValueConverter<M, U> converter) {
		this.converter = converter;
	}

	@SuppressWarnings("unchecked")
	public void setConverter(String convertID) {
		this.converter = (IModelValueConverter<M, U>) ValueConverterManager.getConverter(convertID);
	}
	
	// return UIBinding for convenience of chain invocation
	public UIBinding<M, U> setValueValidator(Function<M, Boolean> modelValueValidator) {
		this.valueValidator = modelValueValidator;
		return this;
	}
	@SuppressWarnings("unchecked")
	UIBinding<M, U> _setValueValidator(Function<?, Boolean> modelValueValidator) {
		return setValueValidator((Function<M, Boolean>) modelValueValidator);
	}
	
	void setUIBindingManager(UIBindingManager bindingManager) {
		this.bindingManager = bindingManager;
	}

	public void updateModel() {
		U oldUIValue = getOldUIValue();
		U newUIValue = getNewUIValue();
		
		M oldModelValue, newModelValue;
		try {
			oldModelValue = convertUiValueToModelValue(oldUIValue);
		} catch (Exception e) {
			Exception e2 = new IllegalArgumentException(String.format(__("Invalid input: %s. Only a value of %s type is accepted."), 
					newUIValue, getReadableTypeStr(propertyClassName)), e);
			handleException(e2);
			setUIValue(oldUIValue);
			return;
		}
		try {
			newModelValue = convertUiValueToModelValue(newUIValue);
		} catch (Exception e) {
			Exception e2 = new IllegalArgumentException(String.format(__("Invalid input: %s. Only a value of %s type is accepted."), 
					newUIValue, getReadableTypeStr(propertyClassName)), e);
			handleException(e2);
			setUIValue(oldUIValue);
			return;
		}
		
		if (!validateUiValue(newModelValue)) {
			DEBUG_MSG("Invalid new ui value.");
			setUIValue(oldUIValue);
			return;
		}
		
		UIAction action = ActionFactory.createSetValueAction(
				propertyDesc, oldModelValue, newModelValue,
				this::setModelAndUIValue, this::callbackAfterModelUpdated).addToManagerAndDoIt();
		
		if (bindingManager != null)
			bindingManager.addUIAction(action);
	}
	
	protected boolean validateUiValue(M modelValue) {
		if (valueValidator == null)
			return true;
		else
			return valueValidator.apply(modelValue);
	}
	
	private void handleException(Exception e) {
		if (exceptionHandler != null) {
			exceptionHandler.accept(e);
		} else {
			e.printStackTrace();
		}
	}

	public void updateUI() {
		M modelValue = getModelValue();
		U uiValue = convertModelValueToUIValue(modelValue);
		_setUIValue(uiValue);
	}
	
	public void callbackAfterModelUpdated() {
		if (callback != null) {
			callback.accept(modelObjID);
		}
	}
	
	public M getModelValue() {
		return getter.apply(model);
	}
	
	public void setModelValue(M value) {
		setter.accept(model, value);
	}
	
	private void setModelAndUIValue(M value) {
		setModelValue(value);
		updateUI();
	}
	
	public void _setUIValue(U uiValue) {
		if (uiValue == null)
			return;
		
		// If uiValue is not changed, then do nothing.
		if (uiValue.equals(getNewUIValue()))
			return;
		
		setUIValue(uiValue);
	}
	
	public abstract U getOldUIValue();
	
	public abstract U  getNewUIValue();
	
	public abstract void setUIValue(U uiValue);
	
	public abstract void addEventListenersOnUI();
	
	public abstract void removeEventListenersOnUI();
	
	@SuppressWarnings("unchecked")
	protected U convertModelValueToUIValue(M modelValue) {
		if (converter != null)
			return converter.modelValueToUI(modelValue);
		
		// Directly return if the model type is the same with the ui value type.
		if (isModelTypeAndUiTypeTheSame()) {
			return (U) modelValue;
		}
		
		// If ui value is String and no M->String converter is registered,
		// then use modelValue.toString() as the default converter.
		if (String.class.getName().equals(uiValueClassName) &&
				!modelValueConverterMap.containsKey(Tuple2.of(propertyClassName, 
						String.class.getName() )) ) {
			
			if (modelValue == null)
				return (U) "";
			else
				return (U) modelValue.toString();
		} 
		
		// Find a M->U converter to do the converting job.
		else {
			try {
				IValueTypeConverter<M, U> converter = (IValueTypeConverter<M, U>) 
					modelValueConverterMap.get(Tuple2.of(propertyClassName, uiValueClassName));
				if (converter == null)
					throw new NullPointerException();
				else
					return converter.convertAValueToBValue(modelValue);
			} catch (Exception e) {
				throw new IllegalArgumentException(String.format(
						__("I Don't know how to convert the model value in (%s) type into a"
								+ " ui value in (%s) type."),
								propertyClassName, uiValueClassName ));
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected M convertUiValueToModelValue(U uiValue) {
		if (converter != null)
			return converter.UIvalueToModel(uiValue);
		
		// Directly return if the model type is the same with the ui value type.
		if (isModelTypeAndUiTypeTheSame()) {
			return (M) uiValue;
		}

		// If ui value is String and no String->M converter is registered,
		// then use the default converter.
		if (String.class.getName().equals(uiValueClassName) &&
				!uiValueConverterMap.containsKey(Tuple2.of(String.class.getName(), 
						propertyClassName )) ) {
			
			if (uiValue == null)
				return null;
			else
				return (M) stringValueToKnownTypesValue((String) uiValue, 
						propertyClassName);
		} 
		
		// Find a U->M converter to do the converting job.
		else {
			try {
				IValueTypeConverter<U, M> converter = (IValueTypeConverter<U, M>) 
					uiValueConverterMap.get(Tuple2.of(uiValueClassName, propertyClassName));
				if (converter == null)
					throw new NullPointerException();
				else
					return converter.convertAValueToBValue(uiValue);
			} catch (Exception e) {
				throw new IllegalArgumentException(String.format(
						__("I Don't know how to convert the ui value in (%s) type into a"
								+ " model value in (%s) type.")
								, uiValueClassName, propertyClassName));
			}
		}
		
	}
	
	protected boolean isModelTypeAndUiTypeTheSame() {
		if (propertyClassName.equals(uiValueClassName)) {
			return true;
		}
		
		propertyClassName = convertPrimitiveTypeNameToObjectName(propertyClassName);
		
		return propertyClassName.equals(uiValueClassName);
	}
	
	public String convertPrimitiveTypeNameToObjectName(String name) {
		
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
	
	public static Object stringValueToKnownTypesValue(String strValue, String propertyClassName) {
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
	
	private String getReadableTypeStr(String propertyClassName) {
		String modelValue = propertyClassName;
		
		if("byte".equals(propertyClassName) || "java.lang.Byte".equals(propertyClassName) ) {
			modelValue = __("byte");
		} else if("int".equals(propertyClassName) || "java.lang.Integer".equals(propertyClassName) ) {
			modelValue = __("integer");
		} else if("long".equals(propertyClassName) || "java.lang.Long".equals(propertyClassName) ) {
			modelValue = __("long type interger");
		} else if ("float".equals(propertyClassName) || "java.lang.Float".equals(propertyClassName) ) {
			modelValue = __("decimal");
		} else if ("double".equals(propertyClassName) || "java.lang.Double".equals(propertyClassName) ) {
			modelValue = __("decimal");
		} else if ("boolean".equals(propertyClassName) || "java.lang.Boolean".equals(propertyClassName) ) {
			modelValue = __("boolean");
		} else if ("java.lang.String".equals(propertyClassName)) {
			modelValue = __("string");;
		}
		
		return modelValue;
	}
	
	public String init(boolean findAccessor, boolean autoFind, boolean isField) {
		uiValueClassName = getNewUIValue().getClass().getName();
		
		if (findAccessor)
			return initGetterAndSetter(true, true);
		else
			return "";
	}
	
	/**
	 * Find getter/setter for model property. This methods is called by the factory once
	 * instance is created.
	 * @param model Model object
	 * @param property Name Name of model property as the data source of UI control.
	 * @param autoFind If True try to find field first and then try to find getter/setter methods.
	 * If False, then only find field or getter/setter methods as isField parameter instructs.
	 * @param isField If true, find field; otherwise find getter/setter methods.
	 * if autoFind parameter is set to true, then this parameter is meaningless.
	 * @return
	 */
	public String initGetterAndSetter(boolean autoFind, boolean isField) {
		if (model == null || propertyName == null) {
			throw new IllegalArgumentException("Both model and propertyName cannot be empty");
		}
		
		String status = "";
		if (autoFind) {
			// Find property accessor first
			status = findMethods(model, propertyName);
			if ("".equals(status))
				return status;
			
			// then find field method
			status = findField(model, propertyName);
			return status;
		} else {
			if (isField) {
				return findField(model, propertyName);
			} else {
				return findMethods(model, propertyName);
			}
		}
	}

	public void copySetterAndGetter(UIBinding<M, ? extends Object> binding2) {
		this.getter = binding2.getter;
		this.setter = binding2.setter;
		this.propertyClassName = binding2.propertyClassName;
	}
	
	@SuppressWarnings("unchecked")
	protected String findField(Object model, String fieldName) {
		Field field;
		
		try {
			Class<? extends Object> clazz = model.getClass();
			field = clazz.getField(fieldName);
			field.setAccessible(true);
			propertyClassName = field.getType().getName();

			try {
				field.get(model);
				
				getter = obj -> { 
					try { 
						return (M) field.get(obj); 
					} catch (Exception e) { 
						e.printStackTrace();
						return null;
					} 
				};
				setter = (obj, value) -> { 
					try { 
						field.set(obj, value);
					} catch (Exception e) {} 
				};
				
				return "";
			} catch (Exception e) {
				System.err.println(String.format("Cannot get property value from model field %s in class.", 
						fieldName, model.getClass().getName()));
				e.printStackTrace();
			}
		} catch (Exception e) {
		}
		
		return "Cannot find field " + fieldName + " in class " + model.getClass().getName();
	}
	
	@SuppressWarnings("unchecked")
	protected String findMethods(Object model, String propertyName) {
		
		try {
			Class<? extends Object> clazz = model.getClass();
			propertyClassName = null;
			propertyName = propertyName.substring(0, 1).toUpperCase() +
					propertyName.substring(1);
			String getterName1 = "get" + propertyName;
			String getterName2 = "is" + propertyName;
			String setterName = "set" + propertyName;
			
			getter = null;
			setter = null;
			
			for (Method method : clazz.getMethods()) {
				// Getter
				if ((method.getName().equals(getterName1) || method.getName().equals(getterName2))
						&& method.getParameterCount() == 0) {
					
					propertyClassName = method.getReturnType().getName();
					method.setAccessible(true);
					
					try {
						method.invoke(model);

						getter = obj -> { 
							try { 
								return (M) method.invoke(obj); 
							} catch (Exception e) { 
								e.printStackTrace();
								return null;
							} 
						};
						break;
					} catch (Exception e) {
						e.printStackTrace();
						return "Cannot invoke property getter method " + method.getName() + 
								" in class " + model.getClass().getName();
					}
				}
			}
			
			if (getter == null) {
				return "Cannot find getter for property " + propertyName + " in class " + model.getClass().getName();
			}
			
			for (Method method : clazz.getMethods()) {
				// Setter
				if (method.getName().equals(setterName) && method.getParameterCount() == 1 
						&& method.getParameters()[0].getType().getName().equals(propertyClassName)) {
					
					method.setAccessible(true);

					setter = (obj, value) -> { 
						try { 
							method.invoke(obj, value);
						} catch (Exception e) {} 
					};
				}
			}
			
			if (setter == null) {
				return "Cannot find setter for property " + propertyName + " in class " + model.getClass().getName();
			} else {
				return "";
			}
		} catch (Exception e) {
		}
		
		return "Cannot find getter/setter methods for property " + propertyName + " in class " + model.getClass().getName();
	}
}
