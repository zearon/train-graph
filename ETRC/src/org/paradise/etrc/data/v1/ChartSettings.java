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
	@TGProperty
	public int timetableEditRowHeaderWidth;
	@TGProperty
	public int timetableEditCellWidth;
	@TGProperty
	public int timetableEditVehicleNameRowHeight;
	@TGProperty
	public int timetableEditRemarksRowHeight;
	@TGProperty
	public int timetableEditTrainNumberIncrement;
	@TGProperty
	public boolean timetableEditUseTrainTypeFontColor;

	ChartSettings() {
		setToDefault();
	}

	public void setProperties(float distScale, int displayLevel, int boldLevel, 
			int startHour, float minuteScale, int timeInterval, String distUnit, boolean useAntiAliasing,
			int timetableEditRowHeaderWidth, int timetableEditCellWidth,
			int timetableEditVehicleNameRowHeight, int timetableEditRemarksRowHeight,
			int timetableEditTrainNumberIncrement, boolean timetableEditUseTrainTypeFontColor) {
		this.setName("");
		this.distScale = distScale;
		this.displayLevel = displayLevel;
		this.boldLevel = boldLevel;
		this.startHour = startHour;
		this.minuteScale = minuteScale;
		this.timeInterval = timeInterval;
		this.distUnit = distUnit;
		this.useAntiAliasing = useAntiAliasing;
		this.timetableEditRowHeaderWidth = timetableEditRowHeaderWidth;
		this.timetableEditCellWidth = timetableEditCellWidth;
		this.timetableEditVehicleNameRowHeight = timetableEditVehicleNameRowHeight;
		this.timetableEditRemarksRowHeight = timetableEditRemarksRowHeight;
		this.timetableEditTrainNumberIncrement = timetableEditTrainNumberIncrement;
		this.timetableEditUseTrainTypeFontColor = timetableEditUseTrainTypeFontColor;
	}
	
	@Override
	public void setToDefault() {
		setProperties(3.0f, 4, 2, 18, 2.0f, 10, "km", false, 80, 40, 60, 250, 2, false);
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