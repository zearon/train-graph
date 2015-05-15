package org.paradise.etrc.data.v1;

import java.util.Iterator;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.scene.shape.Line;

import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.annotation.TGProperty;
import org.paradise.etrc.data.annotation.TrainGraphElement;
import org.paradise.etrc.util.data.Tuple2;

import static org.paradise.etrc.ETRC.__;

@TGElementType(name="Train Graph", root=true)
public class TrainGraph extends TrainGraphPart<RailNetworkChart> {

	@TGElement
	public ChartSettings settings;
	@TGElement
	public RailNetwork railNetwork;
//	@TGElement
	public AllTrainTypes allTrainTypes;
	@TGElement
	public AllTrains allTrains;
	@TGElement(index=999)
	public RailNetworkMap map;
	public Vector<RailNetworkChart> charts;
	
	TrainGraph() {
	}
	
	@TGElement
	public AllTrainTypes getAllTrainTypes() {
		return allTrainTypes;
	}

	@TGElement
	public void setAllTrainTypes(AllTrainTypes allTrainTypes) {
		this.allTrainTypes = allTrainTypes;
	}

	@TGElement(isList=true, type=RailNetworkChart.class)
	public Vector<RailNetworkChart> getCharts () {
		return charts;
	}


	@Override
	public void initElements() {
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
	
	
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	
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
