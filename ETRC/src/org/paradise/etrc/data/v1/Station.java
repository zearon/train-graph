package org.paradise.etrc.data.v1;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.annotation.TGProperty;
import org.paradise.etrc.util.data.Tuple2;

import static org.paradise.etrc.ETRC.__;

@TGElementType(name="Station", printInOneLine=true)
public class Station extends TrainGraphPart {
	@TGProperty
	public boolean hide = false;
	@TGProperty
	public int level = 0;
	@TGProperty
	public int dist = 0;

	public boolean isCrossover = false;
	public int scaledDist = 0;
	public boolean isLoopStation = false;
	
	Station() {}

	protected Station(String _name) {
		this();
		setName(_name);
	}
	
	/**
	 * Set properties of the station object
	 * @param dist
	 * @param level
	 * @param hide
	 * @return current object, for convenience of link-style invocation
	 */
	public Station setProperties(int dist, int level, boolean hide) {
		this.level = level;
		this.dist = dist;
		this.hide = hide;
		
		return this;
	}

	public Station copy() {
		Station newStation = new Station(this.getName());
		newStation.setProperties(this.dist, this.level, this.hide);
		newStation.isCrossover = isCrossover;
		newStation.scaledDist = scaledDist;
		newStation.isLoopStation = isLoopStation;
		return newStation;
	}

	public String getOneName() {
		if (getName().startsWith("北京"))
			return "京";
		else if (getName().startsWith("上海"))
			return "沪";
		else if (getName().startsWith("南京"))
			return "宁";
		else if (getName().startsWith("天津"))
			return "津";
		else if (getName().startsWith("重庆"))
			return "渝";
		else if (getName().startsWith("宁波"))
			return "甬";
		else if (getName().startsWith("无锡"))
			return "锡";
		else if (getName().length() <= 0)
			return "";
		else
			return getName().substring(0, 1);
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return getName() != null && obj != null && getName().equals(((Station) obj).getName());
	}

	public String toString() {
		return getName() + ":" + level + ":" + dist + ":" + hide;
	}
}
