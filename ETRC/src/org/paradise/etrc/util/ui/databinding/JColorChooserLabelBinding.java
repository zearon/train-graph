package org.paradise.etrc.util.ui.databinding;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

import org.paradise.etrc.util.ui.JColorChooserLabel;

public class JColorChooserLabelBinding extends UIBinding<Color, Color> implements PropertyChangeListener {
	private JColorChooserLabel label;
	private Color oldValue;
	
	boolean uiChangedByCode = false;
	
	JColorChooserLabelBinding(JColorChooserLabel label, Object model, String propertyName,
			String propertyDesc, Consumer<String> callback) {
		
		super(model, propertyName, propertyDesc, callback);
		this.label = label;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (uiChangedByCode)
			return;
		
		oldValue = (Color) evt.getOldValue();
		updateModel();
	}
	
	@Override
	public Color getOldUIValue() {
		return oldValue;
	}

	@Override
	public Color getNewUIValue() {
		return label.getColor();
	}

	@Override
	public void setUIValue(Color uiValue) {
		uiChangedByCode = true;
		
		label.setColor(uiValue);
		
		uiChangedByCode = false;
	}

	@Override
	public void addEventListenersOnUI() {
		label.addPropertyChangeListener("color", this);
	}

	@Override
	public void removeEventListenersOnUI() {
		label.removePropertyChangeListener("color", this);
	}

}
