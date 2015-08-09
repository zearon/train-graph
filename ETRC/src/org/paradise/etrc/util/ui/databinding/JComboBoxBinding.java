package org.paradise.etrc.util.ui.databinding;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Consumer;

import javax.swing.JComboBox;

/**
 * A data binding model to binding a JComboBox to a data source model. <br/>
 * NOTE: the value type should be the same as the element type in JComboBox as 
 * type parameter E in JComboBox<E> denotes. <br/>
 * If types of the data source property and element of JComboBox are not the same,
 * you should extend this class and override two methods: <br/> <code>
 * public T modelValueToUIValue(Object modelValue) </code> and <br/><code>
 * public Object uiValueToModelValue(T uiValue) </code>
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
public class JComboBoxBinding<M, U> extends UIBinding<M, U> implements ItemListener {
	JComboBox<U> comboBox;
	U oldValue;
	
	boolean uiChangedByCode = false;
	
	JComboBoxBinding(JComboBox<U> comboBox, Object model, String propertyName, 
			String propertyDesc, Consumer<String> callback) {
		super(model, propertyName, propertyDesc, callback);
		this.comboBox = comboBox;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (uiChangedByCode)
			return;
		
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			oldValue = (U) e.getItem();
		} else if (e.getStateChange() == ItemEvent.SELECTED) {
			updateModel();
		}
	}

	@Override
	public U getOldUIValue() {
		return oldValue;
	}

	@Override
	public U getNewUIValue() {
		return (U) comboBox.getSelectedItem();
	}

	@Override
	public void setUIValue(U uiValue) {
		uiChangedByCode = true;
		
		comboBox.setSelectedItem(uiValue);
		
		uiChangedByCode = false;
	}

	@Override
	public void addEventListenersOnUI() {
		comboBox.addItemListener(this);
	}

	@Override
	public void removeEventListenersOnUI() {
		comboBox.removeItemListener(this);
	}

}
