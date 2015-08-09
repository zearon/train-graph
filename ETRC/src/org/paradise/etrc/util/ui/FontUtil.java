package org.paradise.etrc.util.ui;
import java.awt.GraphicsEnvironment;

public class FontUtil {
	public static String[] getFontFamilyNames() {
		String[] fontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();
		return fontFamilyNames;
	}
}
