package org.paradise.etrc.data.v1;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.util.Comparator;
import java.util.Vector;

import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.annotation.TGProperty;

/**
 * 用于表示一个车次在某一条线路上的停靠路线投影视图, 即包含且只包含该线路上的每一个站点, 
 * 及其通过状态.
 * @author Jeff Gong
 *
 */
@TGElementType(name="Train Route Section")
public class TrainRouteSection extends TrainGraphPart {
	
	// Use inherited name attributes as train name
	public String getTrainName() { return getName(); }
	public void setTrainName(String name) { setName(name); }
	
	@TGProperty
	public boolean downGoing = true;
	@TGProperty
	public String remarks = "";
	
	public RailroadLineChart getRailLineChart() {return (RailroadLineChart) getParent();}
	public void setRailLineChart(RailroadLineChart raillineChart) {
		setParent(raillineChart);
	}
	public RailroadLine getRailLine() {
		RailroadLineChart lineChart = (RailroadLineChart) getParent();
		if (lineChart == null)
			return null;
		return lineChart.railroadLine;
	}
	
	@TGElement(isList=true, type=Stop.class)
	private Vector<Stop> stops = new Vector<> ();
	public Vector<Stop> allStops() { return stops; }
	
	public Train getTrain() {
		RailroadLineChart lineChart = (RailroadLineChart) getParent();
		if (lineChart == null)
			throw new RuntimeException("The train route section is not set a railroad line chart parent.");
		
		RailNetworkChart networkChart = (RailNetworkChart) lineChart.getParent();
		if (networkChart == null)
			throw new RuntimeException("The train route section's parent railroad line chart is not set a railroad network chart parent.");
		
		return networkChart.findTrain(getName());
	}
	
	public void setTrainNameForAllStops() {
		stops.forEach(stop -> stop.setTrainName(getTrainName()));
	}
	
	public void createEmptySection() {
		RailroadLine railLine = getRailLine();
		if (railLine == null)
			return;

		stops.clear();
		
		Comparator<Station> comparator = (station1, station2) -> 
			downGoing ? station1.dist - station2.dist : station2.dist - station1.dist;
		
		railLine.getAllStations().stream().sorted(comparator).map(station -> {
			Stop stop = TrainGraphFactory.createInstance(Stop.class, station.getName());
			// set-up the stop
			return stop;
		}).forEach(stops::add);
	}
	
	/**
	 * Should only be used in parsing imported trains.
	 * @param train
	 */
	public static TrainRouteSection createTrainRouteSectionForTrain(RailroadLineChart railLineChart, Train train) {
		if (railLineChart.railroadLine == null)
			return null;
		
		boolean downGoing = train.isDownTrain(railLineChart.railroadLine) == Train.DOWN_TRAIN;
		TrainRouteSection section = TrainGraphFactory.createInstance(TrainRouteSection.class,
				train.getName());
		section.setRailLineChart(railLineChart);
		section.downGoing = downGoing;
		
		section.createEmptySection();
		
		int stopIndex = 0, firstStopIndex = -1, lastStopIndex = -1, stopCount = 0;
		for (Stop stop : section.stops) {
			stop.setTrainName(train.getName());
			Stop stopInTrain = train.findStop(stop.getName());
			if (stopInTrain != null) {
				stopInTrain.copyTo(stop);
				firstStopIndex = firstStopIndex < 0 ? stopIndex : firstStopIndex;
				lastStopIndex = stopIndex;
				++ stopCount;
			}
			
			++ stopIndex;
		}
		
		for (stopIndex = firstStopIndex; stopIndex <= lastStopIndex && 
				stopIndex < section.stops.size(); ++ stopIndex) {
			
			Stop stop = section.stops.get(stopIndex);
			if (stop.stopStatus == Stop.NOT_GO_THROUGH)
				stop.stopStatus = Stop.PASS;
		}
		
		// 只有一站在本线路内的车次不加入时刻表
		if (stopCount > 1) {
			if (downGoing) {
				railLineChart.addDownwardTrainSection(section);
			} else {
				railLineChart.addUpwardTrainSection(section);
			}
			
			return section;
		} else {
			return null;
		}
		
		
	}
}
