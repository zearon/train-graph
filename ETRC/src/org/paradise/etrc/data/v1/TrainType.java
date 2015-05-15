package org.paradise.etrc.data.v1;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.Color;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.annotation.TGProperty;
import org.paradise.etrc.util.data.Tuple2;

@TGElementType(name="Train Type", printInOneLine=true)
public class TrainType extends TrainGraphPart {
	public static final String LINE_STYLE_SOLID = "SOLID";
	public static final String LINE_STYLE_DASH = "DASH";
	public static final String LINE_STYLE_DOT_DASH = "DOT_DASH";
	public static final String LINE_STYLE_DOT = "DOT";
	
	@TGProperty
	public String abbriveation;
	@TGProperty
	public String pattern;
	@TGProperty
	public Color color;
	@TGProperty
	public String lineStype;
	@TGProperty
	public float lineWidth;
	@TGProperty
	public String fontFamily;
	@TGProperty
	public int fontSize;
	@TGProperty
	public Color fontColor;
	
	TrainType() {}	
	
	TrainType(String name) {
		this();
		setName(name);
	}
	
	/**
	 * Set properties of the train type object
	 * @param dist
	 * @param level
	 * @param hide
	 * @return current object, for convenience of link-style invocation
	 */
	public TrainType setProperties(String abbriveation, String pattern, Color color, String lineStype, 
			float lineWidth, String fontFamily, int fontSize, Color fontColor) {
		this.abbriveation = abbriveation;
		this.pattern = pattern;
		this.color = color;
		this.lineStype = lineStype;
		this.lineWidth = lineWidth;
		this.fontFamily = fontFamily;
		this.fontSize = fontSize;
		this.fontColor = fontColor;
		
		return this;
	}
	
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */	
	@Override
	public void setToDefault() {
		setProperties("", "", Color.decode("#ff0000"), LINE_STYLE_SOLID, 1.0f, __("Lucida Grande"), 12, Color.decode("#700000"));
	}	
	

//
//	@TGPProperty(name="color")
//	public String getColorStr() {
//		return color == null ? "" : "#" + Integer.toHexString(color.getRGB() & 0x11ffffff);
//	}
//
//	@TGPProperty(name="color")
//	public void setColorStr(String colorStr) {
//		try {
//			color = Color.decode(colorStr);
//		} catch (Exception e) {
//			System.err.println("Invalid color string:" + colorStr);
//		}
//	}
//
//	@TGPProperty(name="fontColor")
//	public String getFontColorStr() {
//		return fontColor == null ? "" : "#" + Integer.toHexString(fontColor.getRGB() & 0x11ffffff);
//	}
//
//	@TGPProperty(name="fontColor")
//	public void setFontColorStr(String colorStr) {
//		try {
//			fontColor = Color.decode(colorStr);
//		} catch (Exception e) {
//			System.err.println("Invalid color string:" + colorStr);
//		}
//	}
//	
	
	
	
	

	
	
	
	
	
	

	
}
