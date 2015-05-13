package org.paradise.etrc.data;

import static org.paradise.etrc.ETRC.__;

import java.math.BigInteger;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

public class ChartSettings extends TrainGraphPart<ChartSettings, NullPart> {
	public float distScale;
	public int displayLevel;
	public int boldLevel;
	public int startHour;
	public float minuteScale;
	public int timeInterval;
	public String distUnit;
	
	ChartSettings() {}

	public void setProperties(float distScale, int displayLevel,
			int boldLevel, int startHour, float minuteScale, int timeInterval, String distUnit) {
		this.name = "";
		this.distScale = distScale;
		this.displayLevel = displayLevel;
		this.boldLevel = boldLevel;
		this.startHour = startHour;
		this.minuteScale = minuteScale;
		this.timeInterval = timeInterval;
		this.distUnit = distUnit;
	}
	
	public int getDisplayLevel() {
		return displayLevel;
	}

	public void setDisplayLevel(int displayLevel) {
		this.displayLevel = displayLevel;
	}

	public float getMinuteScale() {
		return minuteScale;
	}

	public void setMinuteScale(float minuteScale) {
		this.minuteScale = minuteScale;
	}

	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_GLOBAL_SETTINGS; }
	@Override
	protected String getEndSectionString() { return END_SECTION_GLOBAL_SETTINGS; }
	@Override 
	String createTGPNameById(int id) { 
		return String.format(__("Global Setting"), id);
	}
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return ChartSettings::new;
	}
	@Override
	public void _prepareForFirstLoading() {}
	@Override
	void setToDefault() {
		setProperties(3.0f, 4, 2, 18, 2.0f, 10, "km");
	}

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;
	@Override
	protected Tuple<String, Class<?>>[] getSimpleTGPProperties() {
		if (propTuples == null) {
			propTuples = new Tuple[8];
			
			propTuples[0] = Tuple.of("name", String.class);
			propTuples[1] = Tuple.of("distScale", float.class);
			propTuples[2] = Tuple.of("minuteScale", float.class);
			propTuples[3] = Tuple.of("startHour", int.class);
			propTuples[4] = Tuple.of("timeInterval", int.class);
			propTuples[5] = Tuple.of("displayLevel", int.class);
			propTuples[6] = Tuple.of("boldLevel", int.class);
			propTuples[7] = Tuple.of("distUnit", String.class);
		}
		
		return propTuples;
	}

	@Override
	protected void setTGPProperty(String porpName, String valueInStr) {
		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
		
		if (propTuples[0].A.equals(porpName)) {
			name = valueInStr;
		} else if (propTuples[1].A.equals(porpName)) {
			distScale = Float.parseFloat(valueInStr);
		} else if (propTuples[2].A.equals(porpName)) {
			minuteScale = Float.parseFloat(valueInStr);
		} else if (propTuples[3].A.equals(porpName)) {
			startHour = Integer.parseInt(valueInStr);
		} else if (propTuples[4].A.equals(porpName)) {
			timeInterval = Integer.parseInt(valueInStr);
		} else if (propTuples[5].A.equals(porpName)) {
			displayLevel = Integer.parseInt(valueInStr);
		} else if (propTuples[6].A.equals(porpName)) {
			boldLevel = Integer.parseInt(valueInStr);
		} else if (propTuples[7].A.equals(porpName)) {
			distUnit = valueInStr;
		}
	}

	@Override
	protected String getTGPPropertyReprStr(int index) {
		String value = "";
		
		if (index == 0) {
			value = name;	
		} else if (index == 1) {
			value = distScale + "";
		} else if (index == 2) {
			value = minuteScale + "";
		} else if (index == 3) {
			value = startHour + "";
		} else if (index == 4) {
			value = timeInterval + "";
		} else if (index == 5) {
			value = displayLevel + "";
		} else if (index == 6) {
			value = boldLevel + "";
		} else if (index == 7) {
			value = distUnit == null ? "" : distUnit;
		}
		
		return value;
	}

	/* Element array */
	@Override
	protected Vector<NullPart> getTGPElements() {return null;}

	@Override
	protected void addTGPElement(NullPart element) {}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) { return false; }
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {};
}