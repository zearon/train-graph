package org.paradise.etrc.view.traintypes;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import org.paradise.etrc.data.v1.TrainType;
import org.paradise.etrc.util.ui.databinding.converter.IModelValueConverter;

import sun.net.ftp.FtpClient.TransferType;

public class LineStyleConverter implements IModelValueConverter<String, String> {

	/**
	 * LINE_STYLES is several String constants defined in org.paradise.etrc.data.v1.TrainType:
	 */
	public static final String[] LINE_STYLES = {TrainType.LINE_STYLE_SOLID, TrainType.LINE_STYLE_DASH, 
		TrainType.LINE_STYLE_DOT, TrainType.LINE_STYLE_DOT_DASH, TrainType.LINE_STYLE_CUSTOM};
	public static final String[] LINE_STYLE_DESCS = {__("Solid"), __("Dash"), __("Dot"), __("Dot and dash"), __("Custom")};
			
	@Override
	public String getID() {
		return "LineStyle";
	}

	@Override
	public String modelValueToUI(String modelValue) {
		for (int i = 0; i < LINE_STYLES.length; ++ i) {
			if (LINE_STYLES[i].equalsIgnoreCase(modelValue))
				return LINE_STYLE_DESCS[i];
		}
		
		return LINE_STYLE_DESCS[0];
	}

	@Override
	public String UIvalueToModel(String uiValue) {
		for (int i = 0; i < LINE_STYLE_DESCS.length; ++ i) {
			if (LINE_STYLE_DESCS[i].equalsIgnoreCase(uiValue))
				return LINE_STYLES[i];
		}
		
		return LINE_STYLES[0];
	}

}
