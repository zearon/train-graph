package org.paradise.etrc.data;

import static org.paradise.etrc.ETRC.__;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.annotation.TGPElement;
import org.paradise.etrc.data.util.Tuple;

public class RailNetworkChart 
extends TrainGraphPart<RailroadLineChart> {
	
	protected Vector<RailroadLineChart> railLineCharts = new Vector<RailroadLineChart> ();
	
	RailNetworkChart() {
	}

	@TGPElement(name="All Line Charts", isList=true)
	public Vector<RailroadLineChart> getRailLineCharts() {
		return railLineCharts;
	}
	
	public RailroadLineChart findRailLineChart(RailroadLine line) {
		return railLineCharts.stream().filter(chart -> chart.railroadLine.equals(line))
				.findFirst().orElse(null);
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
	@Override String createTGPNameById(int id) { 
		return String.format(__("Timetable v%d"), id);
	}
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return RailNetworkChart::new;
	}
	@Override
	public void registerSubclasses() {
		new RailroadLineChart().registerClasses();
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
