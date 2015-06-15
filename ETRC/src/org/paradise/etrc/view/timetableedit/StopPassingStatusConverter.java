package org.paradise.etrc.view.timetableedit;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.util.ui.databinding.converter.IModelValueConverter;

public class StopPassingStatusConverter implements IModelValueConverter<Integer, String> {

	@Override
	public String getID() {
		return "StopPassingStatus";
	}

	@Override
	public String modelValueToUI(Integer modelValue) {
		if (modelValue < 0 || modelValue >= Stop.STOP_STATUS_DESC.length)
			modelValue = 0;
		
		return Stop.STOP_STATUS_DESC[modelValue];
	}

	@Override
	public Integer UIvalueToModel(String uiValue) {
		for (int i = 0; i < Stop.STOP_STATUS_DESC.length; ++ i) {
			if (Stop.STOP_STATUS_DESC[i].equalsIgnoreCase(uiValue))
				return i;
		}
		
		return 0;
	}

}
