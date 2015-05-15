package org.paradise.etrc.data.v1;

import static org.paradise.etrc.ETRC.__;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.util.data.Tuple2;

@TGElementType(name="RailNetwork Chart")
public class RailNetworkChart extends TrainGraphPart {

	@TGElement(name="All Line Charts", isList=true, type=RailroadLineChart.class)
	protected Vector<RailroadLineChart> railLineCharts = new Vector<RailroadLineChart> ();
	
	RailNetworkChart() {
	}

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
	
	
	
	
	
	
	
	
	
	
	
	
	

	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {
		
	};

}
