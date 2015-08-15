package com.zearon.util.ui.databinding;
import java.awt.Component;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;

import org.paradise.etrc.controller.action.UIAction;

import com.zearon.util.data.Tuple2;
import com.zearon.util.ui.widget.JColorChooserLabel;


public class UIBindingManager {
	

	static HashMap<Tuple2<String, String>, UIBinding<? extends Object, ? extends Object>> bindingDict = 
			new HashMap<Tuple2<String,String>, UIBinding<? extends Object, ? extends Object>> ();
	
	static boolean cacheEnabled = true;
	
	public static void setCacheEnabledForSameProperty(boolean enabled) {
		cacheEnabled = enabled;
	}

// {{
//	static void registerKnownConverters() {
//		UIBinding.registerModelValueTypeConverter(
//				new IValueTypeConverter<Boolean, String>() {
//					@Override public Class<Boolean> getAValueType() { return Boolean.class; }
//					@Override public Class<String> getBValueType() { return String.class; }
//					@Override public String convertAValueToBValue(Boolean modelValue) {
//						return modelValue ? "Yes" : "No";
//					}
//		});
//		UIBinding.registerUIValueTypeConverter(
//				new IValueTypeConverter<String, Boolean>() {
//					@Override public Class<String> getAValueType() { return String.class; }
//					@Override public Class<Boolean> getBValueType() { return Boolean.class; }
//					@Override public Boolean convertAValueToBValue(String uiValue) {
//						return "yes".equalsIgnoreCase(uiValue) ? true : false;
//					}
//				});
//	}
//
//	static {
//		registerKnownConverters();
//	} 

	public static UIBinding<Object, Object> getUIBinding(Object model, String propertyName, String propertyDesc,
			Consumer<String> callback) {
		return getUIBinding(model, propertyName, propertyDesc, callback, true, true);
	}
	
	public static UIBinding<Object, Object> getUIBinding(Object model, String propertyName, String propertyDesc, 
			Consumer<String> callback, boolean isField) {
		return getUIBinding(model, propertyName, propertyDesc, callback, false, isField);
	}
	
	private static UIBinding<Object, Object> getUIBinding(Object model, String propertyName, String propertyDesc, 
			Consumer<String> callback, boolean autoFind, boolean isField) {
		
		// A Test class makes none sense. It is used to test already implemented functions
		// in its abstract base class. 
		UIBinding<Object, Object> binding = new UIBinding<Object, Object>(model, propertyName, propertyDesc, callback) {
			@Override public Object getOldUIValue() { return ""; }
			@Override public Object getNewUIValue() { return ""; }
			@Override public void setUIValue(Object uiValue) {}
			@Override public void addEventListenersOnUI() {}
			@Override public void removeEventListenersOnUI() {}
		};
		getBinding(model, propertyName, binding, autoFind, isField);
		return binding;
	}

//	}}
	
	
	private static HashMap<Component, UIBindingManager> instanceMap = new HashMap<> ();
	
	public static UIBindingManager getInstance(Component containerObj) {
		if (instanceMap.containsKey(containerObj))
			return instanceMap.get(containerObj);
		else {
			UIBindingManager instance = new UIBindingManager();
			instanceMap.put(containerObj, instance);
			return instance;
		}
	}
	
	private Vector<UIBinding<?, ?>> allBindings = new Vector<> ();
	
	private Stack<UIAction> actionHistoryStack = new Stack<>();
	
	public void clearDataBinding() {
		for (UIBinding<?, ?> binding : allBindings) {
			binding.removeEventListenersOnUI();
		}
		allBindings.clear();
	}
	
	/**
	 * Read data binding string from <b>name</b> property of UI component, and create a corresponding
	 * data binding object and set it up. <br/>
	 * As an example, <b>trainType.lineStyle:LineStyle</b> specifies that the data source model object is 
	 * identified by trainType, the lineStyle property of the data source object is to be bound 
	 * to the ui component, and a value type converter identified as LineStyle is used to convert
	 * value between model value type and UI value type. <br/>
	 * Note: A value type converter should implements the interface
	 * org.paradise.etrc.util.ui.databinding.converter.IModelValueConverter. Before being referenced 
	 * in a data binding string, it should be registered with class
	 * org.paradise.etrc.util.ui.databinding.converter.ValueConverterManager in advance.
	 * @param component 	The target UI component to be bound with a model object. Cannot be null.
	 * @param modelMapper 	A function that accepts a string object id and returns the model object. 
	 * 						Cannot be null.
	 * @param propertyDescMapper A function that accepts a string property name and returns corresponding
	 * 						property description. Can be null. If it is null, then use property name as its description.
	 * @param callback 		A function that will be called after the model value is set. Same with modelMapper,
	 * 						only objectID matched bindings will take effect. Can be null.
	 * 						If it is null, no extra actions are taken after the model value is set.
	 */
	public void addDataBinding(Component component, Function<String, Object> modelMapper,
			Function<String, String> propertyDescMapper, Consumer<String> callback) {
		
		if (component == null || modelMapper == null)
			throw new NullPointerException("Component and modelMapper cannot be null.");
		
		addDataBinding(component, component.getName(), modelMapper, propertyDescMapper, null, callback);
	}

	/**
	 * Read data binding string from <b>name</b> property of UI component, and create a corresponding
	 * data binding object and set it up. <br/>
	 * As an example, <b>trainType.lineStyle:LineStyle</b> specifies that the data source model object is 
	 * identified by trainType, the lineStyle property of the data source object is to be bound 
	 * to the ui component, and a value type converter identified as LineStyle is used to convert
	 * value between model value type and UI value type. <br/>
	 * Note: A value type converter should implements the interface
	 * org.paradise.etrc.util.ui.databinding.converter.IModelValueConverter. Before being referenced 
	 * in a data binding string, it should be registered with class
	 * org.paradise.etrc.util.ui.databinding.converter.ValueConverterManager in advance.
	 * @param component 	The target UI component to be bound with a model object. Cannot be null.
	 * @param modelMapper 	A function that accepts a string object id and returns the model object. 
	 * 						Cannot be null.
	 * @param propertyDescMapper A function that accepts a string property name and returns corresponding
	 * 						property description. Can be null. If it is null, then use property name as its description.
	 * @param modelValueValidator A validator function whose type is Function<?, Boolean> that accepts 
	 * 						the new model value generated by user interaction and returns true if the value 
	 * 						is valid, or false if the value is invalid. It can be null.
	 * @param callback 		A function that will be called after the model value is set. Same with modelMapper,
	 * 						only objectID matched bindings will take effect. Can be null.
	 * 						If it is null, no extra actions are taken after the model value is set.
	 */
	public void addDataBinding(Component component, Function<String, Object> modelMapper,
			Function<String, String> propertyDescMapper, Function<?, Boolean> modelValueValidator, Consumer<String> callback) {
		
		if (component == null || modelMapper == null)
			throw new NullPointerException("Component and modelMapper cannot be null.");
		
		addDataBinding(component, component.getName(), modelMapper, propertyDescMapper, modelValueValidator, callback);
	}
	
	/**
	 * Create a data binding for a UI component with a data binding string. <br/>
	 * As an example, data binding string <b>trainType.lineStyle:LineStyle</b> specifies that the data source model object is 
	 * identified by trainType, the lineStyle property of the data source object is to be bound 
	 * to the ui component, and a value type converter identified as LineStyle is used to convert
	 * value between model value type and UI value type. <br/>
	 * Note: A value type converter should implements the interface
	 * org.paradise.etrc.util.ui.databinding.converter.IModelValueConverter. Before being referenced 
	 * in a data binding string, it should be registered with class
	 * org.paradise.etrc.util.ui.databinding.converter.ValueConverterManager in advance.
	 * @param component 	The target UI component to be bound with a model object. Cannot be null.
	 * @param dataBindingStr The data binding string.
	 * @param modelMapper 	A function that accepts a string object id and returns the model object. 
	 * 						Cannot be null.
	 * @param propertyDescMapper A function that accepts a string property name and returns corresponding
	 * 						property description. Can be null. If it is null, then use property name as its description.
	 * @param modelValueValidator A validator function whose type is Function<?, Boolean> that accepts 
	 * 						the new model value generated by user interaction and returns true if the value 
	 * 						is valid, or false if the value is invalid. It can be null.
	 * @param callback 		A function that will be called after the model value is set. Same with modelMapper,
	 * 						only objectID matched bindings will take effect. Can be null.
	 * 						If it is null, no extra actions are taken after the model value is set.
	 */
	public void addDataBinding(Object component, String dataBindingStr, Function<String, Object> modelMapper, 
			Function<String, String> propertyDescMapper, Function<?, Boolean> modelValueValidator, Consumer<String> callback) {
		
		if (component == null || modelMapper == null)
			throw new NullPointerException("Component and modelMapper cannot be null.");
		
		if (dataBindingStr == null)
			throw new NullPointerException("Cannot read data binding string from name property.");
		
		String[] attrs = getUIBindingAttr(dataBindingStr);
		String modelObjID = attrs[0];
		String propertyName = attrs[1];
		String propertyConverterID = attrs[2];
		if (propertyName == null || "".equals(propertyName))
			throw new NullPointerException("The property name in data binding string '" + 
					dataBindingStr + "' cannot be null or empty.");
		
		Object model = modelMapper.apply(modelObjID);
		if (model == null)
			throw new NullPointerException("The model object obtained from the modelMapper is null.");
		
		String propertyDesc;
		if (propertyDescMapper != null)
			propertyDesc = propertyDescMapper.apply(propertyName);
		else
			propertyDesc = propertyName;
		
		// Create binding object and set it up.
		UIBinding<?, ?> binding = createUIBindingByComponentType(component, model, 
				propertyName, propertyDesc, callback);
		binding.modelObjID = modelObjID;
		getBinding(model, propertyName, binding, true, true);
		binding.setConverter(propertyConverterID);
		binding.addEventListenersOnUI();
		binding._setValueValidator(modelValueValidator);
		binding.setUIBindingManager(this);
		
		allBindings.add(binding);
	}
	
	/**
	 * Update UI for all relating data bindings.
	 * @param objID 	Only the UI components whose model object ID equals the objID are updated.
	 * 					If objID is null, all UI components are updated. 
	 */
	public void updateUI(String objID) {
		if (objID == null) {
			allBindings.forEach(binding -> binding.updateUI());
		} else {
			allBindings.parallelStream()
				.filter(binding -> objID.equals(binding.modelObjID))
				.forEach(binding -> binding.updateUI());
		}
	}
	
	/**
	 * Set a model to the data binding.
	 * @param modelMapper 	A function that accepts a string object id and returns the model object. 
	 * 						Cannot be null.
	 * @param objID Only 	the UI components whose model object ID equals the objID are set with a
	 * 						new model value. If objID is null, all UI components are updated. 
	 */
	public void setModel(Function<String, Object> modelMapper, String objID) {
		allBindings.parallelStream()
			.filter(binding -> objID == null || objID.equals(binding.modelObjID))
			.forEach(binding -> {
				Object model = modelMapper.apply(binding.modelObjID);
				binding.setModel(model);
			});
	}
	
//	private boolean ObjectIDMatched
	
	private String[] getUIBindingAttr(String bindingStr) {
		String modelObj = null, propertyName = null, propertyConverterID = null;
		
		String[] parts = bindingStr.split(":");
		if (parts.length > 1) {
			propertyConverterID = parts[1].trim();
			bindingStr = parts[0];
		}
			
		parts = bindingStr.split("\\.");
		if (parts.length > 1) {
			modelObj = parts[0].trim();
			propertyName = parts[1].trim();
		} else {
			modelObj = "";
			propertyName = parts[0].trim();
		}
		
		return new String[] {modelObj, propertyName, propertyConverterID};
	}
	
	private UIBinding<?,?> createUIBindingByComponentType(Object component, Object model, 
			String propertyName, String propertyDesc, Consumer<String> callback) {
		
		if (component instanceof JTextComponent)
			return new JTextComponentBinding((JTextComponent) component, model, 
					propertyName, propertyDesc, callback);
		else if (component instanceof JComboBox)
			return getJComboBoxBindingBinding((JComboBox) component, model, 
					propertyName, propertyDesc, callback);
//		else if (component instanceof JList)
//			return getJListBindingBinding((JList) component, model, 
//					propertyName, propertyDesc, callback);
		else if (component instanceof JColorChooserLabel)
			return new JColorChooserLabelBinding((JColorChooserLabel) component, model, 
					propertyName, propertyDesc, callback);
		else if (component instanceof JToggleButton)
			return new JToggleButtonBinding((JToggleButton) component, model, 
					propertyName, propertyDesc, callback);
		else if (component instanceof ButtonGroup)
			return new ButtonGroupBinding((ButtonGroup) component, model, 
					propertyName, propertyDesc, callback);
		
		throw new RuntimeException("There is no ui binding supporting " + 
				component.getClass().getName() + " yet.");
	}
	
	private <T> JComboBoxBinding<Object, T> getJComboBoxBindingBinding(JComboBox<T> component, Object model, 
			String propertyName, String propertyDesc, Consumer<String> callback) {
		
		JComboBoxBinding<Object, T> binding = 
				new JComboBoxBinding<Object, T> (component, model, 
						propertyName, propertyDesc, callback);
		return binding;
	}
	
//	public <T> JListBinding<Object, T> getJListBindingBinding(JList<T> component, Object model, 
//			String propertyName, String propertyDesc, Consumer<String> callback) {
//		
//		JListBinding<Object, T> binding = 
//				new JListBinding<Object, T> (component, model, 
//						propertyName, propertyDesc, callback);
//		return binding;
//	}


	private static <M, U> void getBinding(Object model, String propertyName, UIBinding<M, U> binding, 
			boolean autoFind, boolean isField) {
		
		if (model == null || propertyName == null) {
			throw new IllegalArgumentException("Both model and propertyName cannot be empty");
		}
		
		Tuple2<String, String> property = Tuple2.of(model.getClass().getName(), propertyName);
		UIBinding<? extends Object, ? extends Object> oldbinding = 
				bindingDict.get(property);
		boolean notExists = oldbinding == null;
		if (!cacheEnabled || notExists) {
			String status = binding.init(true, autoFind, isField);
			if (!"".equals(status)) {
				throw new IllegalArgumentException(status);
			}
			bindingDict.putIfAbsent(property, binding);
		} else {
			binding.init(false, autoFind, isField);
			binding.copySetterAndGetter((UIBinding<M, ? extends Object>) oldbinding);
		}
	}
	
	void addUIAction(UIAction uiAction) {
		actionHistoryStack.push(uiAction);
	}
	
	public void cancelAllEditing() {
		while(!actionHistoryStack.isEmpty()) {
			UIAction action = actionHistoryStack.pop();
			action.redoAction();
		}
	}
}
