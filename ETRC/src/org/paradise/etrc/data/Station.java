package org.paradise.etrc.data;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

import static org.paradise.etrc.ETRC.__;

public class Station extends TrainGraphPart<Station, NullPart> {
	public boolean hide = false;
	public int level = 0;
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
		Station newStation = new Station(this.name);
		newStation.setProperties(this.dist, this.level, this.hide);
		newStation.isCrossover = isCrossover;
		newStation.scaledDist = scaledDist;
		newStation.isLoopStation = isLoopStation;
		return newStation;
	}

	public String getOneName() {
		if (name.startsWith("北京"))
			return "京";
		else if (name.startsWith("上海"))
			return "沪";
		else if (name.startsWith("南京"))
			return "宁";
		else if (name.startsWith("天津"))
			return "津";
		else if (name.startsWith("重庆"))
			return "渝";
		else if (name.startsWith("宁波"))
			return "甬";
		else if (name.startsWith("无锡"))
			return "锡";
		else if (name.length() <= 0)
			return "";
		else
			return name.substring(0, 1);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return name != null && obj != null && name.equals(((Station) obj).name);
	}

	public String toString() {
		return name + ":" + level + ":" + dist + ":" + hide;
	}
	
	
	
	
	
	
	
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_STATION; }
	@Override
	protected String getEndSectionString() { return END_SECTION_STATION; }
	@Override 
	String createTGPNameById(int id) { 
		return String.format(__("Station %d"), id);
	}
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return Station::new;
	}
	@Override
	public void _prepareForFirstLoading() {}

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;
	@Override
	protected Tuple<String, Class<?>>[] getSimpleTGPProperties() {
		if (propTuples == null) {
			propTuples = new Tuple[4];
			
			propTuples[0] = Tuple.of("name", String.class);
			propTuples[1] = Tuple.of("dist", int.class);
			propTuples[2] = Tuple.of("level", int.class);
			propTuples[3] = Tuple.of("hide", boolean.class);
		}
		
		return propTuples;
	}

	@Override
	protected void setTGPProperty(String porpName, String valueInStr) {
		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
		
		if (propTuples[0].A.equals(porpName)) {
			name = valueInStr;
		} else if (propTuples[1].A.equals(porpName)) {
			dist = Integer.parseInt(valueInStr);
		} else if (propTuples[2].A.equals(porpName)) {
			level = Integer.parseInt(valueInStr);
		} else if (propTuples[3].A.equals(porpName)) {
			hide = Boolean.parseBoolean(valueInStr);
		}
	}

	@Override
	protected String getTGPPropertyReprStr(int index) {
		String value = "";
		
		if (index == 0) {
			value = name;	
		} else if (index == 1) {
			value = dist + "";
		} else if (index == 2) {
			value = level + "";
		} else if (index == 3) {
			value = hide + "";
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
