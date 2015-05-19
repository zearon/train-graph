package org.paradise.etrc.data.v1;

import static org.paradise.etrc.ETRC.__;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javafx.scene.chart.LineChart;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.util.data.Tuple2;

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
		trainDict.put(element.getName(), element);
		
		trains.add(element);
	}

	public void addTrain(int index, Train element) {
		trainDict.put(element.getName(), element);
		
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
		trainDict.remove(obj.getName());
		
		trains.removeElementAt(index);
	}

	public boolean removeTrain(Train obj) {
		trainDict.remove(obj.getName());
		
		return trains.removeElement(obj);
	}
	
	public void clearTrains() {
		trains.clear();
		trainDict.clear();
	}

	public Stream<Train> trainStream() {
		return trains.stream();
	}

	public Stream<Train> parallelStream() {
		return trains.parallelStream();
	}

	public Train findTrain(String trainName) {
		if (trainDict != null ) {
			return trainDict.get(trainName);
		} else {
			return null;
		}
	}
	
	public void updateTrainDict() {
		trainDict.clear();
		trains.forEach(train -> trainDict.put(train.getName(), train));
	}

	public void updateTrain(Train newTrain) {
		for (int i = 0; i < trains.size(); i++) {
			if (newTrain.equals(trains.get(i))) {
				if(newTrain.color == null)
					newTrain.color = trains.get(i).color;
				trains.set(i, newTrain);
			}
		}
	}

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
				.map(trainRef->findTrain(trainRef.name))
				.forEachOrdered(lineChart.trains::add);
		});
	};
	
	
	@Override
	public String toString() {
		return this.name;
	}
	
	
	
	
	
	
	
	
	
	


}
