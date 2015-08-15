package com.zearon.util.ui.databinding.converter;
import java.util.HashMap;

import org.paradise.etrc.view.timetableedit.StopPassingStatusConverter;

public class ValueConverterManager {
	public static HashMap<String, IModelValueConverter<?,?>> converterMap = new HashMap<>();
	
	static {
		registerConverter(new BooleanToYesNoConverter());
		registerConverter(new FontStyleConverter());
		registerConverter(new StopPassingStatusConverter());
	}
	
	public static void registerConverter(IModelValueConverter<?, ?> converter) {
		String id = converter.getID();
		converterMap.putIfAbsent(id, converter);
	}
	
	public static IModelValueConverter<?, ?> getConverter(String id) {
		return converterMap.get(id);
	}
}
