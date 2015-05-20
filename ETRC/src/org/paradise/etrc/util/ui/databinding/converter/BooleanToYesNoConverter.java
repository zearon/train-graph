package org.paradise.etrc.util.ui.databinding.converter;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

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
