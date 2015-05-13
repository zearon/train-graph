package org.paradise.etrc.data;

import java.util.Iterator;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.scene.shape.Line;

import org.paradise.etrc.data.util.Tuple;

import static org.paradise.etrc.ETRC.__;

public class TrainGraph extends TrainGraphPart<TrainGraph, RailNetworkChart> {

	public ChartSettings settings;
	public RailNetwork railNetwork;
	public AllTrainTypes allTrainTypes;
	public AllTrains allTrains;
	public RailNetworkMap map;
	protected Vector<RailNetworkChart> charts;
	
	TrainGraph() {
	}
	
	@Override
	void initTGP() {
		settings = TrainGraphFactory.createInstance(ChartSettings.class);
		railNetwork = TrainGraphFactory.createInstance(RailNetwork.class);
		allTrainTypes = TrainGraphFactory.createInstance(AllTrainTypes.class);
		allTrains = TrainGraphFactory.createInstance(AllTrains.class);
		map = TrainGraphFactory.createInstance(RailNetworkMap.class);
		charts = new Vector<RailNetworkChart> ();
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

					if (networkChart.getRailLineCharts().stream()
							.noneMatch(lineChart -> lineChart.railroadLine.equals(line)) ) {
						networkChart.getRailLineCharts()
						.add(TrainGraphFactory.createInstance(RailroadLineChart.class).setProperties(line));
					}
			});
		
		});
	}

	public Vector<RailNetworkChart> getCharts () {
		return charts;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_TRAIN_GRAPH; }
	@Override
	protected String getEndSectionString() { return END_SECTION_TRAIN_GRAPH; }
	@Override String createTGPNameById(int id) { 
		return String.format(__("Train Graph %d"), id);
	}
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return TrainGraph::new;
	}
	@Override
	public void _prepareForFirstLoading() {
		new ChartSettings().prepareForFirstLoading();
		new RailNetwork().prepareForFirstLoading();
		new AllTrainTypes().prepareForFirstLoading();
		new AllTrains().prepareForFirstLoading();
		new RailNetworkChart().prepareForFirstLoading();
		new RailNetworkMap().prepareForFirstLoading();
	}

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
	
	/* Object Properties */
	@Override
	protected void getObjectTGPProperties() {
		objectProperties.clear();
		objectProperties.add(Tuple.of("settings", settings));
		objectProperties.add(Tuple.of("allTrainTypes", allTrainTypes));
		objectProperties.add(Tuple.of("railNetwork", railNetwork));
		objectProperties.add(Tuple.of("allTrains", allTrains));
		objectProperties.add(Tuple.of("map", map));
	}
	
	@Override
	protected void setObjectTGPProperties(TrainGraphPart part) {
		if (part instanceof ChartSettings) {
			settings = (ChartSettings) part;
		} else if (part instanceof AllTrainTypes) {
			allTrainTypes.trainTypes.clear();
			allTrainTypes.trainTypes.clear();
			allTrainTypes = (AllTrainTypes) part;
		} else if (part instanceof RailNetwork) {
			railNetwork.getAllRailroadLines().clear();
			railNetwork = (RailNetwork) part;
		} else if (part instanceof AllTrains) {
			allTrains.trains.clear();
			allTrains.trainDict.clear();
			allTrains = (AllTrains) part;
		} else if (part instanceof RailNetworkMap) {
			map = (RailNetworkMap) part;
		}
	};

	/* Element array */
	@Override
	protected Vector<RailNetworkChart> getTGPElements() {
		return charts;
	}

	@Override
	protected void addTGPElement(RailNetworkChart element) {
		charts.add(element);
	}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {
		return part != null && part instanceof RailNetworkChart;
	}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {
		charts.forEach(chart-> {
			chart.railLineCharts.forEach(railineChart-> {
				// Set railroadLine according to railroadLineName
				railineChart.railroadLine = railNetwork.getAllRailroadLines().stream()
						.filter(line->line.name.equals(railineChart.name))
						.findFirst().orElse(null);				

				// Set trains in terms of trainRefs
				railineChart.trains.clear();
				railineChart.trainRefs.stream()
					.map(trainRef->allTrains.findTrain(trainRef.name))
					.forEachOrdered(railineChart.trains::add);
			});
		});

	};	
}
