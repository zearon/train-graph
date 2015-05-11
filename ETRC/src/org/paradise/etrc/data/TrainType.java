package org.paradise.etrc.data;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.Color;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

public class TrainType extends TrainGraphPart<TrainType, NullPart> {
	public static final String LINE_STYLE_SOLID = "SOLID";
	public static final String LINE_STYLE_DASH = "DASH";
	public static final String LINE_STYLE_DOT_DASH = "DOT_DASH";
	public static final String LINE_STYLE_DOT = "DOT";
	
	public String abbriveation;
	public String pattern;
	public Color color;
	public String lineStype;
	public float lineWidth;
	public String fontFamily;
	public int fontSize;
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
		setProperties("", "", Color.BLACK, LINE_STYLE_SOLID, 1.0f, __("Lucida Grande"), 12, Color.BLACK);
	}

	public String getColorStr() {
		return color == null ? "" : "#" + Integer.toHexString(color.getRGB() & 0x11ffffff);
	}

	public void setColorStr(String colorStr) {
		try {
			color = Color.decode(colorStr);
		} catch (Exception e) {
			System.err.println("Invalid color string:" + colorStr);
		}
	}

	public String getFontColorStr() {
		return fontColor == null ? "" : "#" + Integer.toHexString(fontColor.getRGB() & 0x11ffffff);
	}

	public void setFontColorStr(String colorStr) {
		try {
			fontColor = Color.decode(colorStr);
		} catch (Exception e) {
			System.err.println("Invalid color string:" + colorStr);
		}
	}
	
	
	
	
	

	
	
	
	
	
	
	
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
	public void _prepareForFirstLoading() {}

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;
	@Override
	protected Tuple<String, Class<?>>[] getSimpleTGPProperties() {
		if (propTuples == null) {
			propTuples = new Tuple[9];
			
			propTuples[0] = Tuple.of("name", String.class);
			propTuples[1] = Tuple.of("abbriveation", String.class);
			propTuples[2] = Tuple.of("pattern", String.class);
			propTuples[3] = Tuple.of("color", String.class);
			propTuples[4] = Tuple.of("lineStype", String.class);
			propTuples[5] = Tuple.of("lineWidth", String.class);
			propTuples[6] = Tuple.of("fontFamily", String.class);
			propTuples[7] = Tuple.of("fontSize", String.class);
			propTuples[8] = Tuple.of("fontColor", String.class);
		}
		
		return propTuples;
	}

	@Override
	protected void setTGPProperty(String porpName, String valueInStr) {
		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
		
		if (propTuples[0].A.equals(porpName)) {
			name = valueInStr;
		} else if (propTuples[1].A.equals(porpName)) {
			abbriveation = valueInStr;
		} else if (propTuples[2].A.equals(porpName)) {
			pattern = valueInStr;
		} else if (propTuples[3].A.equals(porpName)) {
			setColorStr(valueInStr);
		} else if (propTuples[4].A.equals(porpName)) {
			lineStype = valueInStr;
		} else if (propTuples[5].A.equals(porpName)) {
			lineWidth = Float.parseFloat(valueInStr);
		} else if (propTuples[6].A.equals(porpName)) {
			fontFamily = valueInStr;
		} else if (propTuples[7].A.equals(porpName)) {
			fontSize = Integer.parseInt(valueInStr);
		} else if (propTuples[8].A.equals(porpName)) {
			setFontColorStr(valueInStr);
		}
	}

	@Override
	protected String getTGPPropertyReprStr(int index) {
		String value = "";
		
		if (index == 0) {
			value = name;	
		} else if (index == 1) {
			value = abbriveation + "";
		} else if (index == 2) {
			value = pattern + "";
		} else if (index == 3) {
			value = getColorStr() + "";
		} else if (index == 4) {
			value = lineStype + "";
		} else if (index == 5) {
			value = lineWidth + "";	
		} else if (index == 6) {
			value = fontFamily + "";
		} else if (index == 7) {
			value = fontSize + "";
		} else if (index == 8) {
			value = getFontColorStr() + "";
		}
		
		return value;
	}

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
