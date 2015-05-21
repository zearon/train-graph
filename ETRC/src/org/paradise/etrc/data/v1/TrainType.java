package org.paradise.etrc.data.v1;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.annotation.TGProperty;
import org.paradise.etrc.util.data.Tuple2;

@TGElementType(name="Train Type", printInOneLine=true)
public class TrainType extends TrainGraphPart {
	public static final int LINE_STYLE_SOLID = 0;
	public static final int LINE_STYLE_DASH = 1;
	public static final int LINE_STYLE_DOT = 2;
	public static final int LINE_STYLE_DOT_DASH = 3;
	public static final int LINE_STYLE_CUSTOM = 4;
	
	public static TrainType defaultTrainType = new TrainType(__("Default"));
	static {
		defaultTrainType.setProperties(__("Default"), ".*", Color.decode("#000000"), 
				TrainType.LINE_STYLE_SOLID, 1.0f, new float[] {5, 2, 2, 2}, 
				__("Lucida Grande"), Font.PLAIN, 12, Color.decode("#000000")).loadComplete();
	}
	
	@TGProperty
	public String abbriveation;
	@TGProperty
	protected String pattern;
	@TGProperty
	protected Color color;
	@TGProperty
	protected float lineWidth;
	@TGProperty
	protected int lineStyle;
	// This property only takes effect when lineStyle is set to custom.
	@TGProperty(isArray=true)
	protected float[] dashStroke = new float[] {5,2,1,2};
	@TGProperty
	protected String fontFamily;
	@TGProperty
	protected int fontStyle;
	@TGProperty
	protected int fontSize;
	@TGProperty
	protected Color fontColor;

	public boolean visible = true;
	public int displayOrder;
	
	private boolean patternUpdated = true;
	protected Pattern namePattern;
	private boolean lineAttrUpdated = true;
	private BasicStroke lineStroke;
	private boolean fontAttrUpdated = true;
	private Font font;
	
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
	public TrainType setProperties(String abbriveation, String pattern, Color color, int lineStype, 
			float lineWidth, float[] dashStroke, String fontFamily, int fontStyle, int fontSize, Color fontColor) {
		this.abbriveation = abbriveation;
		this.pattern = pattern;
		this.color = color;
		this.lineStyle = lineStype;
		this.lineWidth = lineWidth;
		this.dashStroke = dashStroke;
		this.fontFamily = fontFamily;
		this.fontStyle = fontStyle;
		this.fontSize = fontSize;
		this.fontColor = fontColor;
		
		return this;
	}
	
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */	
	@Override
	public void setToDefault() {
		setProperties("", "", Color.decode("#ff0000"), LINE_STYLE_SOLID, 1.0f, 
				new float[] {6, 2, 2, 2}, __("Lucida Grande"), 
				Font.PLAIN, 12, Color.decode("#700000"));
	}	
	
	@Override
	public void loadComplete() {
		namePattern = Pattern.compile(pattern);
		patternUpdated = false;
	}

	public Pattern getNamePattern() {
		if (!patternUpdated && namePattern != null)
			return namePattern;
		
		patternUpdated = false;
		namePattern = Pattern.compile(pattern);
		return namePattern;
	}
	
	public Stroke getLineStroke() {
		if (!lineAttrUpdated && lineStroke != null)
			return lineStroke;

		lineAttrUpdated = false;
		
		switch (lineStyle) {
		case TrainType.LINE_STYLE_DASH:
			lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
					10.0f, new float[] {10, 5}, 0f);
			break;
		case TrainType.LINE_STYLE_DOT:
			lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
					10.0f, new float[] {3, 4}, 0f);
			break;
		case TrainType.LINE_STYLE_DOT_DASH:
			lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
					10.0f, new float[] {10, 4, 3, 4}, 0f);
			break;
		case TrainType.LINE_STYLE_CUSTOM:
			lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
					10.0f, dashStroke, 0f);
			break;
		default:
			lineStroke = new BasicStroke(lineWidth);
			break;
		}

		return lineStroke;
	}
	
	public Font getFont() {
		if (!fontAttrUpdated && font != null)
			return font;

		fontAttrUpdated = false;
		font = new Font(fontFamily, fontStyle, fontSize);
		return font;
	}
	
	public String getDashStrokeStr() {
		if (dashStroke == null)
			return "";
		
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < dashStroke.length; ++ i) {
			if (i > 0)
				str.append(",");
			str.append(dashStroke[i]);
		}
		return str.toString();
	}

	public void setDashStrokeStr(String dashStrokeStr) {
		dashStrokeStr = dashStrokeStr.trim();
		if ("".equalsIgnoreCase(dashStrokeStr)) {
			dashStroke = new float[0];
			return;
		}
		
		String[] parts = dashStrokeStr.split(",");
		try {
			float[] newValue = new float[parts.length];
			for (int i = 0; i < parts.length; ++ i) {
				newValue[i] = Float.parseFloat(parts[i]);
			}
			dashStroke = newValue;
			lineAttrUpdated = true;
		} catch (Exception e) {
			throw new IllegalArgumentException(__("Invalid dash stroke. It should be comma seperated decimals."));
		}
	}

	public String getAbbriveation() {
		return abbriveation;
	}

	public void setAbbriveation(String abbriveation) {
		this.abbriveation = abbriveation;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
		patternUpdated = true;
	}

	public Color getLineColor() {
		return color;
	}

	public void setLineColor(Color color) {
		this.color = color;
	}

	public int getLineStyle() {
		return lineStyle;
	}

	public void setLineStyle(int lineStype) {
		this.lineStyle = lineStype;
		lineAttrUpdated = true;
	}

	public float getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
		lineAttrUpdated = true;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
		fontAttrUpdated = true;
	}

	public int getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
		fontAttrUpdated = true;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
		fontAttrUpdated = true;
	}

	public Color getFontColor() {
		return fontColor;
	}

	public void setFontColor(Color fontColor) {
		this.fontColor = fontColor;
	}



	
}
