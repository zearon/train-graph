package org.paradise.etrc.data;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

public class TrainGraph extends TrainGraphPart<TrainGraph, RailNetworkChart> {

	public String name = "";
	public RailNetwork railNetwork;
	public AllTrains allTrains;
	protected Vector<RailNetworkChart> charts;
	
	public TrainGraph() {
		railNetwork = new RailNetwork();
		allTrains = new AllTrains();
		charts = new Vector<RailNetworkChart> ();
		charts.add(new RailNetworkChart());
	}
	
	public static TrainGraph getDefaultInstance() {
		TrainGraph tg = new TrainGraph ();
		
		// Add a default railroad line rail network
		RailroadLine line = new RailroadLine();
		tg.railNetwork.addRailroadLine(line);
		
		// Add a default time table
		RailroadLineChart lineChart = new RailroadLineChart(line);
		RailNetworkChart networkChart = new RailNetworkChart();
		tg.charts.add(networkChart);
		
		return tg;
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
					.map(trainRef->allTrains.findTrain(trainRef.trainNameFull))
					.forEachOrdered(railineChart.trains::add);
			});
		});

	};	
}
