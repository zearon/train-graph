package org.paradise.etrc.util.ui.databinding.converter;
import static org.paradise.etrc.ETRC.__;

public class FontStyleConverter implements IModelValueConverter<Integer, String> {

	/**
	 * Font style is a byte of bit flags defined in java.awt.Font as:
	 * java.awt.Font.PLAIN 	= 0;
	 * java.awt.Font.BOLD 	= 1;
	 * java.awt.Font.ITALIC	= 2;
	 */
	public static final String[] FONT_STYLES = {__("Plain"), __("Bold"), __("Italic"), __("Bold and Italic")};
			
	@Override
	public String getID() {
		return "FontStyle";
	}

	@Override
	public String modelValueToUI(Integer modelValue) {
		if (modelValue < 0 || modelValue > 3)
			modelValue = 0;
		
		return FONT_STYLES[modelValue];
	}

	@Override
	public Integer UIvalueToModel(String uiValue) {
		for (int i = 0; i < FONT_STYLES.length; ++ i) {
			if (FONT_STYLES[i].equalsIgnoreCase(uiValue))
				return i;
		}
		
		return 0;
	}

}
