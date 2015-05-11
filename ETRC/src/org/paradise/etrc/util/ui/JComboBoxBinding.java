package org.paradise.etrc.util.ui;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A data binding model to binding a JComboBox to a data source model.
 * 
 * @author Jeff Gong
 *
 * @param <T> Element type of JComboBox. <b>DO NOT support primitive types. Use corresponding 
 * wrapper class instead</b>. For example, change to following JComboBox declaration from: <br/><br/>
 * <code>
 * JComboBox comboBox = new JComboBox (); <br/>
 * comboBox.setModel(new DefaultComboBoxModel(new int[] {60, 30, 20, 15, 10, 5})); <br/><br/>
 * </code>
 * to <br/><br/>
 * <code>
 * JComboBox<Integer> comboBox = new JComboBox<Integer> (); <br/>
 * comboBox.setModel(new DefaultComboBoxModel(new Integer[] {60, 30, 20, 15, 10, 5})); <br/><br/>
 * </code>
 */
public class JComboBoxBinding<T> extends UIBinding<T> implements ItemListener {
	JComboBox<T> comboBox;
	T oldValue;
	
	boolean uiChangedByCode = false;
	
	JComboBoxBinding(JComboBox<T> comboBox, Object model, String propertyName, 
			String propertyDesc, Runnable callback) {
		super(model, propertyName, propertyDesc, callback);
		this.comboBox = comboBox;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (uiChangedByCode)
			return;
		
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			oldValue = (T) e.getItem();
		} else if (e.getStateChange() == ItemEvent.SELECTED) {
			updateModel();
		}
	}

	@Override
	public T getOldUIValue() {
		return oldValue;
	}

	@Override
	public T getNewUIValue() {
		return (T) comboBox.getSelectedItem();
	}

	@Override
	public void setUIValue(T uiValue) {
		uiChangedByCode = true;
		
		comboBox.setSelectedItem(uiValue);
		
		uiChangedByCode = false;
	}

	@Override
	public T modelValueToUIValue(Object modelValue) {
		return (T) modelValue;
	}

	@Override
	public Object uiValueToModelValue(T uiValue) {
		return uiValue;
	}

}
