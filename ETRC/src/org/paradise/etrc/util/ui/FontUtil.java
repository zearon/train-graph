package org.paradise.etrc.util.ui;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.GraphicsEnvironment;

public class FontUtil {
	public static String[] getFontFamilyNames() {
		String[] fontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();
		return fontFamilyNames;
	}
}
