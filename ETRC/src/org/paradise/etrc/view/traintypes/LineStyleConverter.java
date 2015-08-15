package org.paradise.etrc.view.traintypes;
import com.zearon.util.ui.databinding.converter.IModelValueConverter;

import static org.paradise.etrc.ETRC.__;

public class LineStyleConverter implements IModelValueConverter<Integer, String> {

	/**
	 * LINE_STYLES is several String constants defined in org.paradise.etrc.data.v1.TrainType:
	 */
	public static final String[] LINE_STYLE_DESCS = {__("Solid"), __("Dash"), __("Dot"), __("Dot and dash"), __("Custom")};
			
	@Override
	public String getID() {
		return "LineStyle";
	}

	@Override
	public String modelValueToUI(Integer modelValue) {
		if (modelValue < 0 || modelValue >= LINE_STYLE_DESCS.length)
			return LINE_STYLE_DESCS[0];
		
		return LINE_STYLE_DESCS[modelValue];
	}

	@Override
	public Integer UIvalueToModel(String uiValue) {
		for (int i = 0; i < LINE_STYLE_DESCS.length; ++ i) {
			if (LINE_STYLE_DESCS[i].equalsIgnoreCase(uiValue))
				return i;
		}
		
		return 0;
	}

}
