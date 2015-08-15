package com.zearon.util.ui.databinding.converter;

public class BooleanToYesNoConverter implements IModelValueConverter<Boolean, String> {

	@Override
	public String getID() {
		return "YesNo";
	}

	@Override
	public String modelValueToUI(Boolean modelValue) {
		return modelValue ? "Yes" : "No";
	}

	@Override
	public Boolean UIvalueToModel(String uiValue) {
		return "yes".equalsIgnoreCase(uiValue) ? true : false;
	}

}
