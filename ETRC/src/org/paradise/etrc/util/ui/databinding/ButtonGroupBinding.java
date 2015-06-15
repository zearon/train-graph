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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

public class ButtonGroupBinding extends UIBinding<Integer, Integer> 
implements ItemListener {
	
	private ButtonGroup buttonGroup;
	private Integer oldValue;
	private int indexInButtonGroup;
	
	boolean uiChangedByCode = false;
	
	
	ButtonGroupBinding(ButtonGroup buttonGroup, Object model, String propertyName,
			String propertyDesc, Consumer<String> callback) {
		
		super(model, propertyName, propertyDesc, callback);
		
		this.buttonGroup = buttonGroup;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (uiChangedByCode)
			return;
		
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			Enumeration<AbstractButton> buttons = buttonGroup.getElements();
			for (int index = 0; buttons.hasMoreElements(); ++ index) {
				AbstractButton button = buttons.nextElement();
				if (button == e.getSource()) {
					oldValue = index;
					return;
				}
			}
			
			oldValue = -1;
		} else if (e.getStateChange() == ItemEvent.SELECTED) {
			updateModel();
		}
	}

	@Override
	public Integer getOldUIValue() {
		return oldValue;
	}

	@Override
	public Integer getNewUIValue() {
		Enumeration<AbstractButton> buttons = buttonGroup.getElements();
		for (int index = 0; buttons.hasMoreElements(); ++ index) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected())
				return index;
		}
		
		return -1;
	}

	@Override
	public void setUIValue(Integer uiValue) {
		uiChangedByCode = true;

		Enumeration<AbstractButton> buttons = buttonGroup.getElements();
		for (int index = 0; buttons.hasMoreElements(); ++ index) {
			AbstractButton button = buttons.nextElement();
			if (index == uiValue) {
				button.setSelected(true);
				break;
			}
		}
		
		uiChangedByCode = false;
	}

	@Override
	public void addEventListenersOnUI() {
		Enumeration<AbstractButton> buttons = buttonGroup.getElements();
		while (buttons.hasMoreElements()) {
			AbstractButton button = buttons.nextElement();
			button.addItemListener(this);
		}
	}

	@Override
	public void removeEventListenersOnUI() {
		Enumeration<AbstractButton> buttons = buttonGroup.getElements();
		while (buttons.hasMoreElements()) {
			AbstractButton button = buttons.nextElement();
			button.removeItemListener(this);
		}
	}

}
