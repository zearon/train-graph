package org.paradise.etrc.data.v1;

import static org.paradise.etrc.ETRC.__;

import java.math.BigInteger;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.annotation.TGProperty;
import org.paradise.etrc.util.data.Tuple2;

@TGElementType(name="Chart Settings")
public class ChartSettings extends TrainGraphPart {
	@TGProperty
	public float distScale;
	@TGProperty
	public int displayLevel;
	@TGProperty
	public int boldLevel;
	@TGProperty
	public int startHour;
	@TGProperty
	public float minuteScale;
	@TGProperty
	public int timeInterval;
	@TGProperty
	public String distUnit;
	@TGProperty
	public boolean useAntiAliasing;

	ChartSettings() {}

	public void setProperties(float distScale, int displayLevel, int boldLevel, 
			int startHour, float minuteScale, int timeInterval, String distUnit, boolean useAntiAliasing) {
		this.setName("");
		this.distScale = distScale;
		this.displayLevel = displayLevel;
		this.boldLevel = boldLevel;
		this.startHour = startHour;
		this.minuteScale = minuteScale;
		this.timeInterval = timeInterval;
		this.distUnit = distUnit;
		this.useAntiAliasing = useAntiAliasing;
	}
	
	@Override
	public void setToDefault() {
		setProperties(3.0f, 4, 2, 18, 2.0f, 10, "km", false);
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
}