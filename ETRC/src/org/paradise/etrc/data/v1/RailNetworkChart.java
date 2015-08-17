package org.paradise.etrc.data.v1;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;

@TGElementType(name="RailNetwork Chart")
public class RailNetworkChart extends TrainGraphPart {

	@TGElement(name="All Line Charts", isList=true, type=RailroadLineChart.class)
	protected Vector<RailroadLineChart> railLineCharts = new Vector<RailroadLineChart> ();

	@TGElement(name="All Trains", isList=true, type=Train.class)
	public Vector<Train> trains = new Vector<Train>();
	protected HashMap<String, Train> trainDict = new HashMap<String, Train> ();
	
	RailNetworkChart() {}
	
	// {{ Train Operations
	/*****************************************************
	 * Train Operations
	 *****************************************************/
	
	public int trainCount() {
		return trains.size();
	}

	public void addTrain(Train element) {
		addToTrainDict(element);
		
		trains.add(element);
	}

	public void addTrain(int index, Train element) {
		addToTrainDict(element);
		
		trains.add(index, element);
	}

	public boolean addAllTrains(Collection<? extends Train> c) {
		boolean result =  trains.addAll(c);
		updateTrainDict();
		
		return result;
	}

	public Train getTrain(int index) {
		return trains.get(index);
	}
	
	public boolean containsTrain(Train o) {
		return trains.contains(o);
	}

	public void forEachTrain(Consumer<? super Train> action) {
		trains.forEach(action);
	}

	public void removeTrainAt(int index) {
		Train obj = getTrain(index);
		removeFromTrainDict(obj);
		removeTrainInLineCharts(obj);
		
		trains.removeElementAt(index);
	}

	public boolean removeTrain(Train obj) {
		removeFromTrainDict(obj);
		removeTrainInLineCharts(obj);
		
		return trains.removeElement(obj);
	}
	
	public void clearTrains() {
		trains.clear();
		trainDict.clear();
	}

	public Stream<Train> trainStream() {
		return trains.stream();
	}

	public Stream<Train> parallelTrainStream() {
		return trains.parallelStream();
	}

	public Train findTrain(String trainName) {
		if (trainDict != null ) {
			return trainDict.get(trainName);
		} else {
			return null;
		}
	}
	
	private void addToTrainDict(Train train) {
		String[] trainNames = train.getName() != null ? 
				train.getName().split("/") : new String[0];
				
		for (String trainName : trainNames)
			trainDict.put(trainName, train);
		
		if (train.getName() != null)
			trainDict.put(train.getName(), train);
	}
	
	private void removeFromTrainDict(Train train) {
		String[] trainNames = train.getName() != null ? 
				train.getName().split("/") : new String[0];
				
		for (String trainName : trainNames)
			trainDict.remove(trainName);
		
		if (train.getName() != null)
			trainDict.remove(train.getName(), train);
	}
	
	public void updateTrainDict() {
		trainDict.clear();
		trains.forEach(this::addToTrainDict);
	}

	public void updateTrain(Train newTrain) {
		for (int i = 0; i < trains.size(); i++) {
			if (newTrain.equals(trains.get(i))) {
//				if(newTrain.color == null)
//					newTrain.color = trains.get(i).color;
				trains.set(i, newTrain);
			}
		}
	}
	
	private void removeTrainInLineCharts(Train train) {
		for (RailroadLineChart lineChart : allRailLineCharts()) {
			for (Iterator<Train> iter = lineChart.trains.iterator(); iter.hasNext(); ) {
				Train train0 = iter.next();
				
				if (train0.equals(train))
					iter.remove();
			}
		}
	}
	
//	public void syncTrains() {
//		// Set trains in terms of trainRefs
//		railLineCharts.forEach(this::syncTrains);
//	}
//	
//	public void syncTrains(RailroadLineChart lineChart) {
//		for (Iterator<TrainRef> iter = lineChart.trainRefs.iterator(); iter.hasNext(); ) {
//			TrainRef ref = iter.next();
//			
//			
//		}
//	}

	/*****************************************************
	 * End of Train Operations
	 *****************************************************/
	// }}

	public Vector<RailroadLineChart> allRailLineCharts() {
		return railLineCharts;
	}
	
	public RailroadLineChart findRailLineChart(RailroadLine line) {
		return railLineCharts.stream().filter(chart -> chart.railroadLine.equals(line))
				.findFirst().orElse(null);
	}	
	

	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {
		updateTrainDict();
		
		// Set trains in terms of trainRefs
		railLineCharts.forEach(lineChart -> {

			lineChart.trains.clear();
			lineChart.trainRefs.stream()
				.map(trainRef->findTrain(trainRef.getName()))
				.filter(train -> train != null)
				.forEachOrdered(lineChart.trains::add);

//			syncTrains(lineChart);
		});
	};
	
	/**
	 * Should only be used for imported trains
	 */
	public void createTrainRouteSectionsForTrainsInAllLines() {
		railLineCharts.forEach(lineChart -> lineChart.createTrainRouteSectionsForAllTrains());
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
	
	
	
	
	
	
	
	
	


}
