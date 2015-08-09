package org.paradise.etrc.util.ui.databinding;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

import javax.swing.text.JTextComponent;


/**
 * A data binding model to binding a JTextField to a data source model.<br/>
 * NOTE: the value type should be the same as the value type of UI component. <br/>
 * If two types are not the same, you should extend this class or its sub-class
 * and override two methods: <br/> <code>
 * public T modelValueToUIValue(Object modelValue) </code> and <br/><code>
 * public Object uiValueToModelValue(T uiValue) </code>
 * 
 * @param <M> Type of model value. M can only be java.lang.String or classes
 * corresponding to primitive types. Otherwise, you should extends this class
 * and override two methods: <br/> <code>
 * public String modelValueToUIValue(M modelValue) </code> and <br/><code>
 * public M uiValueToModelValue(String uiValue) </code>
 * 
 * @author Jeff Gong
 * 
 */
public class JTextComponentBinding extends UIBinding<String, String> implements FocusListener, KeyListener {
	JTextComponent textComponent;
	String oldValue;

	/**
	 * Create a TextFieldFocusListner with action and model support.
	 * @param model The data binding model object
	 * @param propertyName The property name of the model as the data source
	 * @param isField True if the property is accessed directly as a field; 
	 * False if the property is accessed through a getter and a setter.
	 */
	JTextComponentBinding(JTextComponent textComponent, Object model, String propertyName, 
			String propertyDesc, Consumer<String> callback) {
		super(model, propertyName, propertyDesc, callback);
		this.textComponent = textComponent;
	}

	@Override
	public void focusGained(FocusEvent e) {
		oldValue = textComponent.getText();
//		DEBUG_MSG("Focus gained. old value is %s", oldValue);
	}

	@Override
	public void focusLost(FocusEvent e) {
		updateModel();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (oldValue != null && !oldValue.equals(getNewUIValue())) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER)
				updateModel();
			
			oldValue = getNewUIValue();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	
	
	@Override
	public String getOldUIValue() { 
		return oldValue; 
	}
	
	@Override
	public String getNewUIValue() { 
		return textComponent.getText(); 
	}

	@Override
	public void setUIValue(String uiValue) {
		textComponent.setText(uiValue);
	}

	@Override
	public void addEventListenersOnUI() {
		textComponent.addFocusListener(this);
//		if (textComponent instanceof JTextField)
//			textComponent.addKeyListener(this);
	}

	@Override
	public void removeEventListenersOnUI() {
		textComponent.removeFocusListener(this);
	}


}
