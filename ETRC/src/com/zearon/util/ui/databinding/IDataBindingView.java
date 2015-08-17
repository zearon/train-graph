package com.zearon.util.ui.databinding;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

import com.zearon.util.interface_.IMultiInheritance;

/**
 * A View with data binding support for its UI components. <br/>
 * A typical use of this interface is done with 3 steps: <br/>
 * 1. Implement three methods: <b>UIDB_getModelObject, UIDB_getPropertyDesc and UIDB_updateUIforModel</b>.<br/>
 * 2. Call <b>UIDB_addDataBinding</b> for each UI data-binding component in constructor.<br/>
 * 3. Call <b>UIDB_setupUIDataBinding</b> at the end of the constructor.
 * 
 * @author Jeff Gong
 * @see UIBinding
 */
public interface IDataBindingView extends IMultiInheritance {
	
	// PLEASE CONFIRM that the parameter type is the same with the interface type.
	// Otherwise, this interface will NOT be initialized.
 	default void IMI_init(IDataBindingView thisObject) {
 		// Invoke this method for EACH parent interface.
 		IMI_initSuper(IMultiInheritance.class, thisObject);
 
		// Fields initializations. The First parameter is the CLASS of this interface.
		IMI_setProperty(IDataBindingView.class, "uiBindingManager", 
				UIBindingManager.getInstance(IMI_getThisObject()));
		IMI_setProperty(IDataBindingView.class, "uiComponents", new Vector<Object>());
		IMI_setProperty(IDataBindingView.class, "uiComponentsAttr", new HashMap<Object, ComponentBindingAttr>());
	}
	
 	/**
 	 * Get the UI binding manager instance.<br/>
 	 * You should not override this method.
 	 * @return The UI binding manager
 	 */
	public default UIBindingManager UIDB_getUIBindingManager() {
		return (UIBindingManager) IMI_getProperty(IDataBindingView.class, "uiBindingManager");
	}
	
	/**
	 * Get all UI components with data binding support.<br/>
	 * You should not override this method.
	 * @return All the UI components with data binding support.
	 */
	public default List<Object> UIDB_getAllBindedComponents() {
		@SuppressWarnings("unchecked")
		List<Object> result = (List<Object>) IMI_getProperty(IDataBindingView.class, "uiComponents");
		return result;
	}
	
	/**
	 * Setup data binding support for components added through UIDB_addDataBinding method before.<br/>
	 * Please invoke this method after UI components are all set up and before the UI is shown.
	 * A typical location of invocation for a UI class is at the end of its constructor. <br/>
	 * You should not override this method.
	 */
	public default void UIDB_setupUIDataBinding() {
		UIBindingManager uiBindingManager = UIDB_getUIBindingManager();
		List<Object> uiComponents = UIDB_getAllBindedComponents();
		@SuppressWarnings("unchecked")
		HashMap<Object, ComponentBindingAttr> uiComponentsAttr = (HashMap<Object, ComponentBindingAttr>) 
				IMI_getProperty(IDataBindingView.class, "uiComponentsAttr");
		
		for (Object component : uiComponents) {
			ComponentBindingAttr bindingAttr = uiComponentsAttr.get(component);
			uiBindingManager.addDataBinding(component, bindingAttr.bindingStr, this::UIDB_getModelObject, 
					this::UIDB_getPropertyDesc, bindingAttr.modelValueValidator, this::UIDB_updateUIforModel);
		}
		
		uiBindingManager.updateUI(null);
	}
	
	/**
	 * Update UI for all relating data bindings.
	 * @param objID 	Only the UI components whose model object ID equals the objID are updated.
	 * 					If objID is null, all UI components are updated. 
	 */
	public default void UIDB_updateUI(String objID) {
		UIDB_getUIBindingManager().updateUI(objID);
	}
	
	/**
	 * Add a data binding component record. 
	 * The binding string should be accessible from getName() of the component. <br/>
	 * Note: The actual binding action are done with UIDB_setupUIDataBinding method. <br/><br/>
	 * As an example of a binding string for a combobox, "<b>trainType.lineStyle:LineStyle</b>" 
	 * specifies that the data source model object is 
	 * identified by trainType, the lineStyle property of the data source object is to be bound 
	 * to the ui component, i.e. the combo box, and a value type converter identified as LineStyle is used to convert
	 * value between model value type and UI value type. <br/>
	 * Note: A value type converter should implements the interface
	 * org.paradise.etrc.util.ui.databinding.converter.IModelValueConverter. Before being referenced 
	 * in a data binding string, it should be registered with class
	 * org.paradise.etrc.util.ui.databinding.converter.ValueConverterManager in advance.
	 * @param com The component to be added with data binding support.
	 * @param modelValueValidator A value validator which will be called on value of component changed.
	 * 				If true is returned, the new value will be set into model object; otherwise, the new value
	 * 				will be ignored. It could be null.
	 */
	public default void UIDB_addDataBinding(Component com, Function<?, Boolean> modelValueValidator) {
		List<Object> uiComponents = UIDB_getAllBindedComponents();
		@SuppressWarnings("unchecked")
		HashMap<Object, ComponentBindingAttr> uiComponentsAttr = (HashMap<Object, ComponentBindingAttr>) 
				IMI_getProperty(IDataBindingView.class, "uiComponentsAttr");
		
		uiComponents.add(com);
		uiComponentsAttr.put(com, new ComponentBindingAttr(com.getName(), modelValueValidator));
	}
	
	/**
	 * Add a data binding component record. 
	 * The binding string should be accessible from getName() of the component. <br/>
	 * Note: The actual binding action are done with UIDB_setupUIDataBinding method. <br/><br/>
	 * As an example of a binding string for a combobox, "<b>trainType.lineStyle:LineStyle</b>" 
	 * specifies that the data source model object is 
	 * identified by trainType, the lineStyle property of the data source object is to be bound 
	 * to the ui component, i.e. the combo box, and a value type converter identified as LineStyle is used to convert
	 * value between model value type and UI value type. <br/>
	 * Note: A value type converter should implements the interface
	 * org.paradise.etrc.util.ui.databinding.converter.IModelValueConverter. Before being referenced 
	 * in a data binding string, it should be registered with class
	 * org.paradise.etrc.util.ui.databinding.converter.ValueConverterManager in advance.
	 * @param com The component to be added with data binding support.
	 * @param bindingStr The binding string.
	 * @param modelValueValidator A value validator which will be called on value of component changed.
	 * 				If true is returned, the new value will be set into model object; otherwise, the new value
	 * 				will be ignored. It could be null.
	 */
	public default void UIDB_addDataBinding(Object com, String bindingStr, Function<?, Boolean> modelValueValidator) {
		List<Object> uiComponents = UIDB_getAllBindedComponents();
		@SuppressWarnings("unchecked")
		HashMap<Object, ComponentBindingAttr> uiComponentsAttr = (HashMap<Object, ComponentBindingAttr>) 
				IMI_getProperty(IDataBindingView.class, "uiComponentsAttr");
		
		uiComponents.add(com);
		uiComponentsAttr.put(com, new ComponentBindingAttr(bindingStr, modelValueValidator));
	}
	
	/**
	 * This method accepts an object id string and returns the model object. 
	 * For example, a binding string "<b>trainType.lineStyle:LineStyle</b>" for a combo box suggests that
	 * A specific trainType object should be returned when the objectID parameter is "trainType";
	 * @param objectID The object ID String.
	 * @return The object itself.
	 */
	public Object UIDB_getModelObject(String objectID); 
	
	/**
	 * This method accepts a string property name and returns corresponding property description. 
	 * The property description will be used in redo/undo action system to identify an action. <br/>
	 * For example, a binding string "<b>trainType.lineStyle:LineStyle</b>" for a combo box suggests that
	 * A description string like "The line style of the train type" should be returned when the objectID parameter is "lineStyle";
	 * @param propertyName The name of a property of an object.
	 * @return The description of the property.
	 */
	public String UIDB_getPropertyDesc(String propertyName);
	
	/**
	 * This method should contain codes that updates views (within or out of this view) related to the 
	 * model objects bound with UI components when the model objects are changed by user interaction.
	 * @param propertyGroup
	 */
	public void UIDB_updateUIforModel(String objectID);
}





class ComponentBindingAttr {
	public String bindingStr;
	public Function<?, Boolean> modelValueValidator;
	
	public ComponentBindingAttr(String bindingStr,
			Function<?, Boolean> modelValueValidator) {
		
		this.bindingStr = bindingStr;
		this.modelValueValidator = modelValueValidator;
	}
	
}