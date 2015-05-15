package org.paradise.etrc.util.ui.databinding;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.paradise.etrc.util.data.Tuple2;

public class UIBindingFactory {
	

	static HashMap<Tuple2<String, String>, UIBinding<? extends Object, ? extends Object>> bindingDict = 
			new HashMap<Tuple2<String,String>, UIBinding<? extends Object, ? extends Object>> ();
	
	static boolean cacheEnabled = true;
	
	public static void setCacheEnabledForSameProperty(boolean enabled) {
		cacheEnabled = enabled;
	}
	
	static void registerKnownConverters() {
		UIBinding.registerModelValueTypeConverter(
				new IValueTypeConverter<Boolean, String>() {
					@Override public Class<Boolean> getAValueType() { return Boolean.class; }
					@Override public Class<String> getBValueType() { return String.class; }
					@Override public String convertAValueToBValue(Boolean modelValue) {
						return modelValue ? "Yes" : "No";
					}
		});
		UIBinding.registerUIValueTypeConverter(
				new IValueTypeConverter<String, Boolean>() {
					@Override public Class<String> getAValueType() { return String.class; }
					@Override public Class<Boolean> getBValueType() { return Boolean.class; }
					@Override public Boolean convertAValueToBValue(String uiValue) {
						return "yes".equalsIgnoreCase(uiValue) ? true : false;
					}
				});
	}

	static {
		registerKnownConverters();
	}

	public static UIBinding<Object, Object> getUIBinding(Object model, String propertyName, String propertyDesc,
			Runnable callback) {
		return getUIBinding(model, propertyName, propertyDesc, callback, true, true);
	}
	
	public static UIBinding<Object, Object> getUIBinding(Object model, String propertyName, String propertyDesc, 
			Runnable callback, boolean isField) {
		return getUIBinding(model, propertyName, propertyDesc, callback, false, isField);
	}
	
	private static UIBinding<Object, Object> getUIBinding(Object model, String propertyName, String propertyDesc, 
			Runnable callback, boolean autoFind, boolean isField) {
		
		// A Test class makes none sense. It is used to test already implemented functions
		// in its abstract base class. 
		UIBinding<Object, Object> binding = new UIBinding<Object, Object>(model, propertyName, propertyDesc, callback) {
			@Override public Object getOldUIValue() { return ""; }
			@Override public Object getNewUIValue() { return ""; }
			@Override public void setUIValue(Object uiValue) {}
		};
		getBinding(model, propertyName, binding, autoFind, isField);
		return binding;
	}
	
	public static JTextFieldBinding<Object> getJTextFieldBinding(JTextField component, Object model, 
			String propertyName, String propertyDesc, Runnable callback) {
		
		JTextFieldBinding<Object> binding = new JTextFieldBinding<Object>(component, model, propertyName, propertyDesc, callback);
		getBinding(model, propertyName, binding, true, true);
		return binding;
	}
	
	public static <T> JComboBoxBinding<Object, T> getJComboBoxBindingBinding(JComboBox<T> component, Object model, 
			String propertyName, String propertyDesc, Runnable callback) {
		
		JComboBoxBinding<Object, T> binding = 
				new JComboBoxBinding<Object, T> (component, model, 
						propertyName, propertyDesc, callback);
		getBinding(model, propertyName, binding, true, true);
		return binding;
	}
	
	
	
	
	private static <M, U> void getBinding(Object model, String propertyName, UIBinding<M, U> binding, 
			boolean autoFind, boolean isField) {
		
		if (model == null || propertyName == null) {
			throw new IllegalArgumentException("Both model and propertyName cannot be empty");
		}
		
		Tuple2<String, String> property = Tuple2.oF(model.getClass().getName(), propertyName);
		UIBinding<? extends Object, ? extends Object> oldbinding = 
				bindingDict.get(property);
		boolean notExists = oldbinding == null;
		if (!cacheEnabled || notExists) {
			String status = binding.init(autoFind, isField);
			if (!"".equals(status)) {
				throw new IllegalArgumentException(status);
			}
			bindingDict.putIfAbsent(property, binding);
		} else {
			binding.copySetterAndGetter((UIBinding<M, ? extends Object>) oldbinding);
		}
	}
}
