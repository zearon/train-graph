package org.paradise.etrc.util.ui;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JComponent;

import org.paradise.etrc.controller.action.ActionFactory;

import sun.reflect.generics.scope.MethodScope;
import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * UIBinding is used to bing a UI control and a data model.
 * @author Jeff Gong
 *
 * @param <VT> Type of UI value. For example, VT should be text for JTextField
 */
public abstract class UIBinding<VT> {
	protected Object model;
	protected String propertyName;
	protected String propertyDesc;
	
	protected Function<Object, Object> getter = null;
	protected BiConsumer<Object, Object> setter = null;
//	protected Class<? extends Object> propertyClass = null;
	protected String propertyClassName = null;
	
	protected Runnable callback;
	
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
	UIBinding(Object model, String propertyName, String propertyDesc, Runnable callback) {
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

	public void updateModel() {
		VT oldUIValue = getOldUIValue();
		VT newUIValue = getNewUIValue();
		
		Object oldModelValue, newModelValue;
		try {
			oldModelValue = uiValueToModelValue(oldUIValue);
		} catch (Exception e) {
			Exception e2 = new IllegalArgumentException(String.format(__("Invalid input: %s. Only a value of %s type is accepted."), 
					newUIValue, getReadableTypeStr(propertyClassName)), e);
			handleException(e2);
			setUIValue(oldUIValue);
			return;
		}
		try {
			newModelValue = uiValueToModelValue(newUIValue);
		} catch (Exception e) {
			Exception e2 = new IllegalArgumentException(String.format(__("Invalid input: %s. Only a value of %s type is accepted."), 
					newUIValue, getReadableTypeStr(propertyClassName)), e);
			handleException(e2);
			setUIValue(oldUIValue);
			return;
		}
		
		ActionFactory.createSetValueActionAndDoIt(propertyDesc, oldModelValue, newModelValue, 
				this::setModelAndUIValue, this::callbackAfterModelUpdated);
	}
	
	private void handleException(Exception e) {
		if (exceptionHandler != null) {
			exceptionHandler.accept(e);
		} else {
			e.printStackTrace();
		}
	}

	public void updateUI() {
		Object modelValue = getModelValue();
		VT uiValue = modelValueToUIValue(modelValue);
		_setUIValue(uiValue);
	}
	
	public void callbackAfterModelUpdated() {
		if (callback != null) {
			callback.run();
		}
	}
	
	public Object getModelValue() {
		return getter.apply(model);
	}
	
	public void setModelValue(Object value) {
		setter.accept(model, value);
	}
	
	public void setModelAndUIValue(Object value) {
		setModelValue(value);
		updateUI();
	}
	
	public void _setUIValue(VT uiValue) {
		if (uiValue == null)
			return;
		
		// If uiValue is not changed, then do nothing.
		if (uiValue.equals(getNewUIValue()))
			return;
		
		setUIValue(uiValue);
	}
	
	public abstract VT getOldUIValue();
	
	public abstract VT  getNewUIValue();
	
	public abstract void setUIValue(VT uiValue);
	
	public abstract VT modelValueToUIValue(Object modelValue);
	
	public Object uiValueToModelValue(VT uiValue) {
		String strValue = uiValue.toString();
		Object modelValue = null;
		
		if("byte".equals(propertyClassName) || "java.lang.Byte".equals(propertyClassName) ) {
			modelValue = Byte.parseByte(strValue);
		} else if("int".equals(propertyClassName) || "java.lang.Integer".equals(propertyClassName) ) {
			modelValue = Integer.parseInt(strValue);
		} else if("long".equals(propertyClassName) || "java.lang.Long".equals(propertyClassName) ) {
			modelValue = Long.parseLong(strValue);
		} else if ("float".equals(propertyClassName) || "java.lang.Float".equals(propertyClassName) ) {
			modelValue = Float.parseFloat(strValue);
		} else if ("double".equals(propertyClassName) || "java.lang.Double".equals(propertyClassName) ) {
			modelValue = Double.parseDouble(strValue);
		} else if ("boolean".equals(propertyClassName) || "java.lang.Boolean".equals(propertyClassName) ) {
			modelValue = Boolean.parseBoolean(strValue);
		} else if ("java.lang.String".equals(propertyClassName)) {
			modelValue = strValue;
		} else {
			throw new IllegalArgumentException(String.format("I Don't know how to convert the ui value into a %s value", propertyClassName));
		}
		
		return modelValue;
	}
	
	private String getReadableTypeStr(String propertyClassName) {
		String modelValue ="";
		
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
	
	/**
	 * Find getter/setter for model property.
	 * @param model Model object
	 * @param property Name Name of model property as the data source of UI control.
	 * @param autoFind If True try to find field first and then try to find getter/setter methods.
	 * If False, then only find field or getter/setter methods as isField parameter instructs.
	 * @param isField If true, find field; otherwise find getter/setter methods.
	 * if autoFind parameter is set to true, then this parameter is meaningless.
	 * @return
	 */
	public String initSetterAndGetter(boolean autoFind, boolean isField) {
		if (model == null || propertyName == null) {
			throw new IllegalArgumentException("Both model and propertyName cannot be empty");
		}
		
		String status = "";
		if (autoFind) {
			// Find field first
			status = findField(model, propertyName);
			if ("".equals(status))
				return status;
			
			// then find property method
			status = findMethods(model, propertyName);
			return status;
		} else {
			if (isField) {
				return findField(model, propertyName);
			} else {
				return findMethods(model, propertyName);
			}
		}
	}
	
	public void copySetterAndGetter(UIBinding<? extends Object> binding2) {
		this.getter = binding2.getter;
		this.setter = binding2.setter;
		this.propertyClassName = binding2.propertyClassName;
	}
	
	protected String findField(Object model, String fieldName) {
		Field field;
		
		try {
			Class<? extends Object> clazz = model.getClass();
			field = clazz.getField(fieldName);
			propertyClassName = field.getType().getName();

			try {
				field.get(model);
				
				getter = obj -> { 
					try { 
						return field.get(obj); 
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
				System.err.println(String.format("Cannot get property value from model field."));
				e.printStackTrace();
			}
		} catch (Exception e) {
		}
		
		return "Cannot find field.";
	}
	
	protected String findMethods(Object model, String propertyName) {
		
		try {
			Class<? extends Object> clazz = model.getClass();
			propertyClassName = null;
			String getterName = "get" + propertyName.substring(0, 1).toUpperCase() +
					propertyName.substring(1);
			String setterName = "set" + propertyName.substring(0, 1).toUpperCase() +
					propertyName.substring(1);
			
			getter = null;
			setter = null;
			
			for (Method method : clazz.getMethods()) {
				// Getter
				if (method.getName().equals(getterName) && method.getParameterCount() == 0) {
					propertyClassName = method.getReturnType().getName();
					try {
						method.invoke(model);

						getter = obj -> { 
							try { 
								return method.invoke(obj); 
							} catch (Exception e) { 
								e.printStackTrace();
								return null;
							} 
						};
						break;
					} catch (Exception e) {
						e.printStackTrace();
						return "Cannot invoke property getter method.";
					}
				}
			}
			
			if (getter == null) {
				return "Cannot find property getter";
			}
			
			for (Method method : clazz.getMethods()) {
				// Setter
				if (method.getName().equals(setterName) && method.getParameterCount() == 1 
						&& method.getParameters()[0].getType().getName().equals(propertyClassName)) {

					setter = (obj, value) -> { 
						try { 
							method.invoke(obj, value);
						} catch (Exception e) {} 
					};
				}
			}
			
			if (setter == null) {
				return "Cannot find property setter";
			} else {
				return "";
			}
		} catch (Exception e) {
		}
		
		return "Cannot find getter/setter methods";
	}
}
