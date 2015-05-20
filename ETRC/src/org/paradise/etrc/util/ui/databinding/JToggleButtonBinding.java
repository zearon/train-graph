package org.paradise.etrc.util.ui.databinding;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

import javax.swing.JToggleButton;

public class JToggleButtonBinding extends UIBinding<Boolean, Boolean> 
implements MouseListener {
	
	private JToggleButton button;
	private Boolean oldValue;
	
	boolean uiChangedByCode = false;
	
	
	JToggleButtonBinding(JToggleButton button, Object model, String propertyName,
			String propertyDesc, Consumer<String> callback) {
		
		super(model, propertyName, propertyDesc, callback);
		this.button = button;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
//		DEBUG_MSG("Clicked: %s", button.isSelected());
	}

	@Override
	public void mousePressed(MouseEvent e) {
//		DEBUG_MSG("Pressed: %s", button.isSelected());
		oldValue = button.isSelected();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
//		DEBUG_MSG("Released: %s", button.isSelected());
		updateModel();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

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
		button.addMouseListener(this);
	}

}
