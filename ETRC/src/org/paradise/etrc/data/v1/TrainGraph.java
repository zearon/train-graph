package org.paradise.etrc.data.v1;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;

@TGElementType(name="Train Graph", root=true)
public class TrainGraph extends TrainGraphPart {

	@TGElement
	public ChartSettings settings;
	
	@TGElement
	public RailNetwork railNetwork;
	
//	@TGElement
//	public AllTrainTypes allTrainTypes;
//	@TGElement
//	public AllTrains allTrains;
	
	@TGElement(name="Train Types", isList=true, type=TrainType.class)
	protected Vector<TrainType> trainTypes = new Vector<TrainType>();
	protected HashMap<String, TrainType> trainTypeDict = new HashMap<String, TrainType> ();
	@TGElement
	protected TrainType defaultTrainType = TrainType.defaultTrainType;
	
	@TGElement(isList=true, type=RailNetworkChart.class)
	protected Vector<RailNetworkChart> charts;
	
	@TGElement(index=999)
	public RailNetworkMap map;
	
	public RailNetworkChart currentNetworkChart;
	public RailroadLineChart currentLineChart;
	
	TrainGraph() {
	}

	public Vector<RailNetworkChart> allCharts () {
		return charts;
	}
	
	public TrainType getDefaultTrainType() {
		return defaultTrainType;
	}
	
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {
		updateTrainTypeDict();
		updateTrainTypeDisplayOrder();
		setTrainTypeByNameForAllTrains();
		
		if (charts != null) {
			charts.forEach(chart-> {
				chart.railLineCharts.forEach(railineChart-> {
					// Set railroadLine according to railroadLineName
					railineChart.railroadLine = railNetwork.getAllRailroadLines().stream()
							.filter(line->line.getName().equals(railineChart.getOrigName()))
							.findFirst().orElse(null);
				});
			});
		
			if (charts.size() > 0) {
				currentNetworkChart = charts.get(0);
				if (currentNetworkChart != null && currentNetworkChart.allRailLineCharts().size() > 0) {
					currentLineChart = currentNetworkChart.allRailLineCharts().get(0);
				}
			}
		}

	};
	
	// {{ Train type Operations
	/*****************************************************
	 * Train type Operations
	 *****************************************************/
	public Vector<TrainType> allTrainTypes() {
		return trainTypes;
	}
	
	public int trainTypeCount() {
		return trainTypes.size();
	}

	public void removeTrainTypeAt(int index) {
		TrainType obj = getTrainType(index);
		trainTypeDict.remove(obj.getName());
		
		trainTypes.removeElementAt(index);
	}

	public boolean removeTrainType(TrainType obj) {
		trainTypeDict.remove(obj.getName());
		
		return trainTypes.removeElement(obj);
	}

	public Stream<TrainType> trainTypeStream() {
		return trainTypes.stream();
	}

	public TrainType getTrainType(int index) {
		return trainTypes.get(index);
	}

	public void addTrainType(int index, TrainType element) {
		trainTypeDict.put(element.getName(), element);
		
		trainTypes.add(index, element);
	}

	public boolean addAllTrainTypes(Collection<? extends TrainType> c) {
		return trainTypes.addAll(c);
	}

	public void forEachTrainType(Consumer<? super TrainType> action) {
		trainTypes.forEach(action);
	}
	
	public TrainType findTrainType(String trainTypeName) {
		if (trainTypeDict != null) {
			return trainTypeDict.get(trainTypeName);
		} else {
			return null;
		}
	}

	public void updateTrainTypeDict() {
		trainTypeDict.clear();
		trainTypes.forEach(trainType -> trainTypeDict.put(trainType.getName(), trainType));
	}
	
	public void updateTrainTypeDisplayOrder() {
		for (int i = 0; i < trainTypes.size(); ++ i) {
			trainTypes.get(i).displayOrder = i;
		}
	}
	
	public TrainType guessTrainTypeByName(String name) {
		if (name == null)
			return defaultTrainType;

		String trainName = name.split("/")[0];
		TrainType type = trainTypes.stream()
			.filter(trainType -> trainType.getNamePattern().matcher(trainName).matches())
			.findFirst().orElse(defaultTrainType);
		
		return type;
	}
	
	public void setTrainTypeByName(Train train) {
		if (train.getName() == null)
			return;
		
		train.trainType = guessTrainTypeByName(train.getName());
	}
	
	public void setTrainTypeByNameForAllTrains() {
		if (charts != null)
			charts.forEach(networkChart -> {
				networkChart.trains.parallelStream().forEach(this::setTrainTypeByName);
			});
	}

	/*****************************************************
	 * End of Train type Operations
	 *****************************************************/
	// }}

	@Override
	public void initElements() {
		settings = TrainGraphFactory.createInstance(ChartSettings.class);
		railNetwork = TrainGraphFactory.createInstance(RailNetwork.class);
		map = TrainGraphFactory.createInstance(RailNetworkMap.class);
		charts = new Vector<RailNetworkChart> ();
//		charts2;//		allTrainTypes = TrainGraphFactory.createInstance(AllTrainTypes.class);
//		allTrains = TrainGraphFactory.createInstance(AllTrains.class);
	}
	
	public void syncLineChartsWithRailNetworks() {
		
		// Remove line charts in every network chart, which does not match 
		// any railroad line in the rail network.
		charts.forEach(networkChart -> {
			for(Iterator<RailroadLineChart> iter = networkChart.railLineCharts.iterator(); 
					iter.hasNext(); ) {
				
				RailroadLineChart lineChart = iter.next();
				if (lineChart.railroadLine == null) {
					iter.remove();
				}
				if (railNetwork.getAllRailroadLines().stream()
						.noneMatch(line->line.equals(lineChart.railroadLine))) {
					iter.remove();
				}
			}
		});

		// Create empty line charts in every network chart for railroad lines
		// that have no corresponding line charts.
		railNetwork.getAllRailroadLines().forEach(line -> {
				
				charts.forEach(networkChart -> {

					if (networkChart.allRailLineCharts().stream()
							.noneMatch(lineChart -> lineChart.railroadLine.equals(line)) ) {
						
						RailroadLineChart lineChart = TrainGraphFactory.createInstance(
								RailroadLineChart.class).setProperties(line);
						lineChart.setParent(networkChart);
						networkChart.allRailLineCharts().add(lineChart);
					}
					
					networkChart.setParent(this);
			});
		
		});
	}
	
	
	
	
	
	
	
	
	
	

}
