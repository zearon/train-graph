package org.paradise.etrc.data;

import static org.paradise.etrc.ETRC.__;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

public class RailNetworkChart 
extends TrainGraphPart<RailNetworkChart, RailroadLineChart> {

	public String name;
	
	public transient int id;
	protected static int idCount = 0;
	
	protected Vector<RailroadLineChart> railLineCharts = new Vector<RailroadLineChart> ();
	
	public RailNetworkChart() {
		id = ++ idCount;
		name = String.format(__("Train Graph %d"), id);
	}
	
	public Vector<RailroadLineChart> getRailLineCharts() {
		return railLineCharts;
	}
	
	
	@Override
	public String toString() {
		return this.name;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_RAILNETWORK_CHART; }
	@Override
	protected String getEndSectionString() { return END_SECTION_RAILNETWORK_CHART; }

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;
	@Override
	protected Tuple<String, Class<?>>[] getSimpleTGPProperties() {
		if (propTuples == null) {
			propTuples = new Tuple[1];
			
			propTuples[0] = Tuple.of("name", String.class);
		}
		
		return propTuples;
	}

	@Override
	protected void setTGPProperty(String propName, String valueInStr) {
		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
		
		if (propTuples[0].A.equals(propName)) {
			name = valueInStr;
		}
	}

	@Override
	protected String getTGPPropertyReprStr(int index) {
		String value = "";
		
		if (index == 0) {
			value = name;	
		}
		
		return value;
	}

	/* Element array */
	@Override
	protected Vector<RailroadLineChart> getTGPElements() {
		return railLineCharts;
	}

	@Override
	protected void addTGPElement(RailroadLineChart element) {
		railLineCharts.add(element);
	}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {
		return part != null && part instanceof RailroadLineChart;
	}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {
		
	};

}
