package org.paradise.etrc.view.alltrains;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.util.Vector;

import javax.swing.RowFilter;

import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.view.alltrains.TrainListView.TrainsTableModel;

public class TrainFilter extends RowFilter<TrainsTableModel, Integer> {
	RailroadLineChart lineChart;
	private static Vector<TrainFilter> allRaillineFilters = new Vector<> ();
	
	public TrainFilter(RailroadLineChart lineChart) {
		this.lineChart = lineChart;
	}
	
	public static Vector<TrainFilter> createFiltersForAllRaillines(
			TrainGraph trainGraph) {
		
		allRaillineFilters.clear();
		allRaillineFilters.add(new TrainFilter(null));
		
		if (trainGraph != null)
			for (RailroadLineChart lineChart : trainGraph.currentNetworkChart.allRailLineCharts()) {
				allRaillineFilters.add(new TrainFilter(lineChart));
			}
		
		return allRaillineFilters;
	}

	@Override
	public boolean include(
			javax.swing.RowFilter.Entry<? extends TrainsTableModel, ? extends Integer> entry) {
		
		if (lineChart == null)
			return true;
		
		int rowIndex = entry.getIdentifier();
		Train train = entry.getModel().getTrain(rowIndex);
		return lineChart.containTrain(train);
	}

	@Override
	public String toString() {
		return lineChart == null ? __("All railroad lines") :
			lineChart.railroadLine.getName();
	}
}
