package org.paradise.etrc.util.ui.databinding;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.JToggleButton;

/**
 * Works for JToggleButton and JCheckbox, which is a sub-class of JToggleButton
 * @author Jeff Gong
 */
public class JToggleButtonBinding extends UIBinding<Boolean, Boolean> 
implements ItemListener/*, MouseListener*/ {
	
	private JToggleButton button;
	private Boolean oldValue;
	
	boolean uiChangedByCode = false;
	
	
	JToggleButtonBinding(JToggleButton button, Object model, String propertyName,
			String propertyDesc, Consumer<String> callback) {
		
		super(model, propertyName, propertyDesc, callback);
		this.button = button;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (uiChangedByCode)
			return;
		
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			oldValue = true;
		} else if (e.getStateChange() == ItemEvent.SELECTED) {
			oldValue = false;
		}
		
		updateModel();
	}
	
/*
	@Override public void mouseClicked(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) {
		oldValue = button.isSelected();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		updateModel();
	}

	@Override public void mouseEntered(MouseEvent e) {}

	@Override public void mouseExited(MouseEvent e) {}
*/
	@Override
	public Boolean getOldUIValue() {
		return oldValue;
	}

	@Override
	public Boolean getNewUIValue() {
		return button.isSelected();
	}

	@Override
	public void setUIValue(Boolean uiValue) {
		uiChangedByCode = true;
		
		button.setSelected(uiValue);
		
		uiChangedByCode = false;
	}

	@Override
	public void addEventListenersOnUI() {
		button.addItemListener(this);
//		button.addMouseListener(this);
	}

	@Override
	public void removeEventListenersOnUI() {
		button.removeItemListener(this);
//		button.removeMouseListener(this);
	}

}
