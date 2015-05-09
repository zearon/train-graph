package org.paradise.etrc.data;

import java.util.Iterator;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.scene.shape.Line;

import org.paradise.etrc.data.util.Tuple;

import static org.paradise.etrc.ETRC.__;

public class TrainGraph extends TrainGraphPart<TrainGraph, RailNetworkChart> {

	public RailNetwork railNetwork;
	public AllTrains allTrains;
	protected Vector<RailNetworkChart> charts;
	
	TrainGraph() {
	}
	
	@Override
	void initTGP() {
		railNetwork = TrainGraphFactory.createInstance(RailNetwork.class);
		allTrains = TrainGraphFactory.createInstance(AllTrains.class);
		charts = new Vector<RailNetworkChart> ();
	}
	
	public void syncLineChartsWithRailNetworks() {
		
		// Remove line charts in every network chart, which does not match 
		// any railroad line in the rail network.
		charts.forEach(networkChart -> {
			for(Iterator<RailroadLineChart> iter = networkChart.railLineCharts.iterator(); 
					iter.hasNext(); ) {
				
				RailroadLineChart lineChart = iter.next();
				if (railNetwork.getAllRailroadLines().stream()
						.noneMatch(line->line.equals(lineChart.railroadLine))) {
					iter.remove();
				}
			}
		});

		// Create empty line charts in every network chart for railroad lines
		// that have no corresponding line charts.
		railNetwork.getAllRailroadLines().stream()
			.filter(line -> charts.get(0).getRailLineCharts().stream()
					.noneMatch(lineChart->lineChart.railroadLine.equals(line)))
			.forEachOrdered(line -> {
				
				charts.forEach(networkChart ->
					networkChart.getRailLineCharts()
					.add(TrainGraphFactory.createInstance(RailroadLineChart.class).setProperties(line)) );
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
		new RailNetwork().prepareForFirstLoading();
		new AllTrains().prepareForFirstLoading();
		new RailNetworkChart().prepareForFirstLoading();
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
		objectProperties.add(railNetwork);
		objectProperties.add(allTrains);
	}
	
	@Override
	protected void setObjectTGPProperties(TrainGraphPart part) {
		if (part instanceof RailNetwork) {
			railNetwork.getAllRailroadLines().clear();
			railNetwork = (RailNetwork) part;
		} else if (part instanceof AllTrains) {
			allTrains.trains.clear();
			allTrains.trainDict.clear();
			allTrains = (AllTrains) part;
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
						.filter(line->line.name.equals(railineChart.railroadLineName))
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
