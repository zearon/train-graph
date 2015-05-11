package org.paradise.etrc.util.ui;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import java.util.function.Consumer;

import javax.swing.JTextField;


/**
 * A data binding model to binding a JTextField to a data source model.
 * 
 * @author Jeff Gong
 * 
 */
public class JTextFieldBinding extends UIBinding<String> implements FocusListener {
	JTextField textField;
	String oldValue;

	/**
	 * Create a TextFieldFocusListner with action and model support.
	 * @param model The data binding model object
	 * @param propertyName The property name of the model as the data source
	 * @param isField True if the property is accessed directly as a field; 
	 * False if the property is accessed through a getter and a setter.
	 */
	JTextFieldBinding(JTextField textField, Object model, String propertyName, 
			String propertyDesc, Runnable callback) {
		super(model, propertyName, propertyDesc, callback);
		this.textField = textField;
	}

	@Override
	public void focusGained(FocusEvent e) {
		oldValue = textField.getText();
	}

	@Override
	public void focusLost(FocusEvent e) {
		updateModel();
	}
	
	@Override
	public String getOldUIValue() { 
		return oldValue; 
	}
	
	@Override
	public String getNewUIValue() { 
		return textField.getText(); 
	}

	@Override
	public void setUIValue(String uiValue) {
		textField.setText(uiValue);
	}

	@Override
	public String modelValueToUIValue(Object modelValue) {
		return modelValue == null ? "" : modelValue.toString();
	}

}
