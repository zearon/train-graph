package org.paradise.etrc.util.ui.databinding.converter;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.util.HashMap;

import org.paradise.etrc.view.traintypes.LineStyleConverter;

public class ValueConverterManager {
	public static HashMap<String, IModelValueConverter<?,?>> converterMap = new HashMap<>();
	
	static {
		registerConvert(new BooleanToYesNoConverter());
		registerConvert(new FontStyleConverter());
	}
	
	public static void registerConvert(IModelValueConverter<?, ?> converter) {
		String id = converter.getID();
		converterMap.putIfAbsent(id, converter);
	}
	
	public static IModelValueConverter<?, ?> getConverter(String id) {
		return converterMap.get(id);
	}
}