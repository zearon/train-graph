package org.paradise.etrc.data;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.Color;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.annotation.TGPProperty;
import org.paradise.etrc.data.util.Tuple;

public class TrainType extends TrainGraphPart<NullPart> {
	public static final String LINE_STYLE_SOLID = "SOLID";
	public static final String LINE_STYLE_DASH = "DASH";
	public static final String LINE_STYLE_DOT_DASH = "DOT_DASH";
	public static final String LINE_STYLE_DOT = "DOT";
	
	@TGPProperty
	public String abbriveation;
	@TGPProperty
	public String pattern;
	@TGPProperty
	public Color color;
	@TGPProperty
	public String lineStype;
	@TGPProperty
	public float lineWidth;
	@TGPProperty
	public String fontFamily;
	@TGPProperty
	public int fontSize;
	@TGPProperty
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
	
	
	
	

	
	
	
	
	
	
	
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_TRAIN_TYPE; }
	@Override
	protected String getEndSectionString() { return END_SECTION_TRAIN_TYPE; }
	@Override 
	String createTGPNameById(int id) { 
		return String.format(__("Train type %d"), id);
	}
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return TrainType::new;
	}
	@Override
	public void registerSubclasses() {}

	/* Element array */
	@Override
	protected Vector<NullPart> getTGPElements() {return null;}

	@Override
	protected void addTGPElement(NullPart element) {}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {
		return part != null && part instanceof NullPart;
	}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {};
	
	
}
