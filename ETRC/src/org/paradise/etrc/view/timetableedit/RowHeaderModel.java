package org.paradise.etrc.view.timetableedit;

import java.util.Comparator;
import java.util.Vector;

import javax.swing.AbstractListModel;

import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.util.data.Tuple2;

import sun.security.krb5.Config;

import static org.paradise.etrc.ETRC.__;

public class RowHeaderModel extends AbstractListModel<Object> {
	private static final long serialVersionUID = 547009998890792058L;
	public static final String ARRIVE_STR = __("%s站 到");
	public static final String DEPARTURE_STR = __(" 发");
	
	TrainGraph trainGraph;
	RailroadLineChart chart;
	Vector<Station> stations;
	
	public RowHeaderModel(TrainGraph trainGraph) {
		setTrainGraph(trainGraph);
	}
	
	public void setTrainGraph(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		switchChart(true);
	}
	
	@SuppressWarnings("unchecked")
	public void switchChart(boolean downGoing) {
		chart = trainGraph.currentLineChart;
		trainGraph.railNetwork.findCrossoverStations();
		stations = (Vector<Station>) chart.railroadLine.getAllStations().clone();
		
		Comparator<Station> comparator = (station1, station2) -> 
			downGoing ? station1.dist - station2.dist : station2.dist - station1.dist;
		stations.sort(comparator);
	}
	
    public int getSize() { 
    	return stations.size() * 2 + 1;
    }
    
    public Object getElementAt(int index) { 
    	if (index == getSize() - 1)
    		return Tuple2.of(__("Remarks"), false);
    	
    	Station station = stations.get(index / 2);
		String sta = station.getName();
		String text = index % 2 == 0 ? String.format(ARRIVE_STR, sta) : station.dist + trainGraph.settings.distUnit + DEPARTURE_STR;
		return Tuple2.of(text, station.isCrossover);
    }
}
