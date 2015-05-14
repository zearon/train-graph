package org.paradise.etrc.data;

import static org.paradise.etrc.ETRC.__;

import java.math.BigInteger;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.annotation.TGPProperty;
import org.paradise.etrc.data.util.Tuple;

public class ChartSettings extends TrainGraphPart<NullPart> {
	@TGPProperty
	public float distScale;
	@TGPProperty
	public int displayLevel;
	@TGPProperty
	public int boldLevel;
	@TGPProperty
	public int startHour;
	@TGPProperty
	public float minuteScale;
	public int timeInterval;
	@TGPProperty
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
	public void registerSubclasses() {}
	@Override
	void setToDefault() {
		setProperties(3.0f, 4, 2, 18, 2.0f, 10, "km");
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