package org.paradise.etrc.util.ui;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.paradise.etrc.data.util.Tuple;

public class UIBindingFactory {

	static HashMap<Tuple<String, String>, UIBinding<? extends Object>> bindingDict = 
			new HashMap<Tuple<String,String>, UIBinding<? extends Object>> ();
	
	static boolean cacheEnabled = true;
	
	public static void setCacheEnabledForSameProperty(boolean enabled) {
		cacheEnabled = enabled;
	}

	public static UIBinding<Object> getUIBinding(Object model, String propertyName, String propertyDesc,
			Runnable callback) {
		return getUIBinding(model, propertyName, propertyDesc, callback, true, true);
	}
	
	public static UIBinding<Object> getUIBinding(Object model, String propertyName, String propertyDesc, 
			Runnable callback, boolean isField) {
		return getUIBinding(model, propertyName, propertyDesc, callback, false, isField);
	}
	
	private static UIBinding<Object> getUIBinding(Object model, String propertyName, String propertyDesc, 
			Runnable callback, boolean autoFind, boolean isField) {
		
		// A Test class makes none sense. It is used to test already implemented functions
		// in its abstract base class. 
		UIBinding<Object> binding = new UIBinding<Object>(model, propertyName, propertyDesc, callback) {
			@Override public Object getOldUIValue() { return ""; }
			@Override public Object getNewUIValue() { return ""; }
			@Override public void setUIValue(Object uiValue) {}
			@Override public Object modelValueToUIValue(Object modelValue) { return ""; }
		};
		getBinding(model, propertyName, binding, autoFind, isField);
		return binding;
	}
	
	
	public static JTextFieldBinding getJTextFieldBinding(JTextField component, Object model, 
			String propertyName, String propertyDesc, Runnable callback) {
		return getJTextFieldBinding(component, model, propertyName, propertyDesc, callback, true, true);
	}
	
	public static JTextFieldBinding getJTextFieldBinding(JTextField component, Object model, 
			String propertyName, String propertyDesc, Runnable callback, boolean isField) {
		return getJTextFieldBinding(component, model, propertyName, propertyDesc, callback, false, isField);
	}
	
	private static JTextFieldBinding getJTextFieldBinding(JTextField component, Object model, 
			String propertyName, String propertyDesc, Runnable callback, boolean autoFind, boolean isField) {
		
		JTextFieldBinding binding = new JTextFieldBinding(component, model, propertyName, propertyDesc, callback);
		getBinding(model, propertyName, binding, autoFind, isField);
		return binding;
	}
	
	
	public static <T> JComboBoxBinding<T> getJComboBoxBindingBinding(JComboBox<T> component, Object model, 
			String propertyName, String propertyDesc, Runnable callback) {
		return getJComboBoxBindingBinding(component, model, propertyName, propertyDesc, callback, true, true);
	}
	
	public static <T> JComboBoxBinding<T> getJComboBoxBindingBinding(JComboBox<T> component, Object model, 
			String propertyName, String propertyDesc, Runnable callback, boolean isField) {
		return getJComboBoxBindingBinding(component, model, propertyName, propertyDesc, callback, false, isField);
	}
	
	private static <T> JComboBoxBinding<T> getJComboBoxBindingBinding(JComboBox<T> component, Object model, 
			String propertyName, String propertyDesc, Runnable callback, boolean autoFind, boolean isField) {
		
		JComboBoxBinding<T> binding = new JComboBoxBinding<T>(component, model, propertyName, propertyDesc, callback);
		getBinding(model, propertyName, binding, autoFind, isField);
		return binding;
	}
	
	
	
	
	
	
	private static <VT> void getBinding(Object model, String propertyName, UIBinding<VT> binding, 
			boolean autoFind, boolean isField) {
		
		if (model == null || propertyName == null) {
			throw new IllegalArgumentException("Both model and propertyName cannot be empty");
		}
		
		Tuple<String, String> property = Tuple.oF(model.getClass().getName(), propertyName);
		UIBinding<? extends Object> oldbinding = bindingDict.get(property);
		boolean notExists = oldbinding == null;
		if (!cacheEnabled || notExists) {
			String status = binding.initSetterAndGetter(autoFind, isField);
			if (!"".equals(status)) {
				throw new IllegalArgumentException(status);
			}
			bindingDict.putIfAbsent(property, binding);
		} else {
			binding.copySetterAndGetter(oldbinding);
		}
	}
}
