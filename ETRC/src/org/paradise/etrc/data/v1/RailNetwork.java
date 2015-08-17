package org.paradise.etrc.data.v1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.event.RailroadLineChangeType;
import org.paradise.etrc.data.util.BOMStripperInputStream;

import com.zearon.util.data.Tuple2;

import static com.zearon.util.debug.DebugUtil.DEBUG;
import static com.zearon.util.debug.DebugUtil.IS_DEBUG;

import static org.paradise.etrc.ETRC.__;

/**
 * 由多条铁路线路组成的铁路网。
 * 
 * @author Jeff Gong
 *
 */
@TGElementType(name="RailNetwork")
public class RailNetwork extends TrainGraphPart {

	@TGElement(name="All Raillines", isList=true, type=RailroadLine.class)
	protected Vector<RailroadLine> railroadLines;

	protected Vector<Station> virtualRailroadLine;
	protected Vector<Station> crossoverStations;

	Vector<Station> tempStations;
		
	private List<BiConsumer<RailroadLine, RailroadLineChangeType>> circuitChangedListeners = 
			new Vector<BiConsumer<RailroadLine, RailroadLineChangeType>>();

	RailNetwork() {
		railroadLines = new Vector<>(8);
		virtualRailroadLine = new Vector<>(100);
		crossoverStations = new Vector<>(20);
		tempStations = new Vector<>(100);
	}

	RailNetwork(String name) {
		this();
		setName(name);
	}

	@Override 
	public void loadComplete() {
		findCrossoverStations();
	}
	
	public Vector<RailroadLine> getAllRailroadLines() {
		return railroadLines;
	}
	
	public void addRailroadLine(RailroadLine line) {
		railroadLines.add(line);
		fireRailroadLineChangedEvent(line, RailroadLineChangeType.ADD);
		line.addStationChangedListener(this::onRailroadLineChange);
	}
	
	public void insertRailroadLine(RailroadLine line, int index) {
		railroadLines.insertElementAt(line, index);
		fireRailroadLineChangedEvent(line, RailroadLineChangeType.ADD);
		line.addStationChangedListener(this::onRailroadLineChange);
	}
	
	public void removeRailroadLine(RailroadLine line) {
		railroadLines.remove(line);
		fireRailroadLineChangedEvent(line, RailroadLineChangeType.REMOVE);
		line.removeStationChangedListener(this::onRailroadLineChange);
	}
	
	public void removeRailroadLine(int index) {
		RailroadLine circuit = railroadLines.remove(index);
		fireRailroadLineChangedEvent(circuit, RailroadLineChangeType.REMOVE);
		circuit.removeStationChangedListener(this::onRailroadLineChange);
	}
	
	public void clearRailroadLines() {
		railroadLines.clear();
		fireRailroadLineChangedEvent(null, RailroadLineChangeType.UPDATE);
	}
	
	public void replaceAllRailroadLines(Collection<RailroadLine> railines) {
		railroadLines.clear();
		railroadLines.addAll(railines);
		fireRailroadLineChangedEvent(null, RailroadLineChangeType.UPDATE);
	}
	
	public RailroadLine getRailroadLine(int index) {
		return railroadLines.get(index);
	}

	public RailroadLine setRailroadLine(int index, RailroadLine element) {
		return railroadLines.set(index, element);
	}
	
	public int getRailrodeLineCount() {
		return railroadLines.size();
	}

	public String repr() {
		StringBuffer sb = new StringBuffer(this.getName());
		
		sb.append(String.format(__("There are %d railroad lines in total.\n"),
				getAllRailroadLines().size()));

		for (RailroadLine line : getAllRailroadLines())
			sb.append(line.toString());

		return sb.toString();
	}

	public Vector<Station> getCrossoverStations() {
		return crossoverStations;
	}
	
	public void addRailroadLineChangedListener(BiConsumer<RailroadLine, RailroadLineChangeType> eventHandler) {
		circuitChangedListeners.add(eventHandler);
	}
	
	public void removeRailroadLineChangedListener(BiConsumer<RailroadLine, RailroadLineChangeType> eventHandler) {
		circuitChangedListeners.remove(eventHandler);
	}
	
	public void fireRailroadLineChangedEvent(RailroadLine line, RailroadLineChangeType changeType) {
		circuitChangedListeners.stream().parallel()
			.forEach(action->action.accept(line, changeType));
	}
	
	protected void onRailroadLineChange(RailroadLine line) {
		fireRailroadLineChangedEvent(line, RailroadLineChangeType.UPDATE);
	}

	public void addProjectionNode() {
		throw new RuntimeException("NotImplementedException");
	}

	public void removeProjectionNode() {
		throw new RuntimeException("NotImplementedException");
	}

	public RailroadLine getProjectionCircuit() {
		throw new RuntimeException("NotImplementedException");
	}

	public RailroadLine getVirtualRailroadLine() {
		throw new RuntimeException("NotImplementedException");
	}

	public List<String> findCrossoverStations() {
		crossoverStations.clear();
		tempStations.clear();
		
		List<String>[] duplicateStationsInLine = new List [railroadLines.size()];
		
		for (int ci = 0; ci < railroadLines.size(); ++ci) {
			RailroadLine circuit = railroadLines.get(ci);
			circuit.calIndex = ci;

			Vector<Station> stations = circuit.getAllStations();
			duplicateStationsInLine[ci]= circuit.getDuplicatedStationNames();
			

			// Find crossover stations
			for (int si = 0; si < stations.size(); ++si) {
				Station station = stations.get(si);

				if (tempStations.contains(station)) {
					station.isCrossover = true;
					if (!crossoverStations.contains(station))
						crossoverStations.add(station);
				} else {
					station.isCrossover = false;
					tempStations.add(station);
				}
			}
		}
		
		railroadLines.forEach(line -> line.getAllStations().forEach(station -> {
			if (crossoverStations.contains(station))
				station.isCrossover = true;
			else
				station.isCrossover = false;
		}));
		
		List<String> errMsg = Stream.of(duplicateStationsInLine)
				.map((List<String> list)->list.size()<1?null:String.format(__("There are duplicate stations on railraod Line: %s. "
						+ "Duplicate stations can only be the first and the last in a railroad line to represent a circle route."), 
						list.stream().reduce("", (a, b)->a.length() > 0 ? a+", "+b : b)))
				.collect(Collectors.toList());
		
		return errMsg;
	}

	/**
	 * 
	 * @return Error messages
	 */
	public List<String> scaleUpRailroadLines() {
		crossoverStations.clear();
		tempStations.clear();

		List<String> errMsgs = findCrossoverStations();
		if (errMsgs.stream().anyMatch(msg->msg!=null)) {
			errMsgs.stream().filter(msg->msg!=null).forEach(msg->DEBUG("ERROR occured: %d", msg));
			return errMsgs;
		}

		// Update crossover station status for each circuit
		railroadLines.stream().flatMap(circuit -> circuit.getAllStations().stream())
				.filter(crossoverStations::contains)
				.forEach(station -> station.isCrossover = true);
		railroadLines.stream().parallel()
				.forEach(circuit -> circuit.updateStations());

		/*
		 * Sort circuits and prepare for calculate according to the following
		 * rules 1. The first circuit is treated as the trunk circuit, so keep
		 * it at list head. 2. Circuits with crossover stations are in front of
		 * those without. 3. Respect the original circuit order as possible.
		 */
		List<RailroadLine> sortedCircuits = railroadLines.stream()
				.sorted(this::compareRailroadLine).collect(Collectors.toList());

		// Calculate
		virtualRailroadLine.clear();
		errMsgs = new ArrayList<String> (railroadLines.size());
		for (int ci = 0; ci < sortedCircuits.size(); ++ci) {
			RailroadLine circuit = railroadLines.get(ci);
			circuit.calIndex = ci;

			if (!isScaleNeeded(circuit)) {
				// For the trunk circuit or those circuits without any crossover stations.
				virtualRailroadLine.addAll(circuit.getAllStations());
				circuit.getAllStations().stream()
						.forEach(station -> station.scaledDist = station.dist);
				
				if (IS_DEBUG())
					circuit.dinfo = "IF-clause";
				
				errMsgs.add(null);
			} else {
				Vector<Station> stations = circuit.getAllStations();

				int crossoverCount = circuit.getCrossoverCount();

				switch (crossoverCount) {
				case 0:
					// Do nothing for circuits without any crossover stations
					// because this case is already handled in the if-clause
					break;
				case 1: {
					// Apply an offset of the crossover station on each station on the circuit
					Station station = circuit.getCrossoverStation(0);
					Optional<Station> stationOnVCircuit = findStationOnVirtualRailLine(station);
					if (!stationOnVCircuit.isPresent())
						virtualRailroadLine.add(station);
					int offset = stationOnVCircuit.orElse(station).dist
							- station.dist;
					
					stations.stream().filter(sta->! station.equals(sta))
						.forEach(sta -> {
							sta.scaledDist = sta.dist + offset;
							virtualRailroadLine.add(sta);
						});
					
					if (IS_DEBUG())
						circuit.dinfo = "Case 1";

					errMsgs.add(null);
					
					break;
				}
				default: {
					// For circuits with more than 1 crossover stations
					stations.stream().forEach(sta->sta.scaledDist=sta.dist);
					
					boolean skip = false;
					boolean alignedAtFirstCrossover = false;
					for (int csindex = 0; csindex < crossoverCount - 1; ++ csindex) {
						Tuple2<Station, Integer> cstation = circuit.getCrossoverStationTuple(csindex);
						Tuple2<Station, Integer> cstation2 = circuit.getCrossoverStationTuple(csindex + 1);
						Optional<Station> cstationOnVCircuit = findStationOnVirtualRailLine(cstation.A);
						Optional<Station> cstation2OnVCircuit = findStationOnVirtualRailLine(cstation2.A);

						// The crossover station is met at the first time. No need to process. Skip it.
						if (!cstationOnVCircuit.isPresent())
							continue;
						
						if (cstationOnVCircuit.isPresent() && cstation2OnVCircuit.isPresent() && cstation.B == 0 && cstation2.B == 1) {
							// Both the first and the second station are crossover station and already in the virtual cirtuit.
							// Then, the first
						}

						// Step 2. Apply an offset of the 1st crossover station that is already in the virtual circuit.
						if (!alignedAtFirstCrossover) {
							int offset = cstationOnVCircuit.get().dist- cstation.A.dist;
							stations.stream().forEach(sta -> {
								sta.scaledDist = sta.dist + offset;
							});
							alignedAtFirstCrossover = true;
							
							if (IS_DEBUG())
								circuit.dinfo = "Case 2-Step 2";
						}

						// Step 3. 
						if (cstationOnVCircuit.isPresent() && cstation2OnVCircuit.isPresent()) {
							if (cstation.B == 0 && cstation2.B == 1) {
								// The first crossover station is 
							}
							
							// The station is already in the virtual circuit
							if (cstation2.A.dist != cstation2OnVCircuit.get().dist) {
								int diff = cstation2.A.scaledDist - cstation.A.scaledDist;
								int diffOnVCirtuit = cstation2OnVCircuit.get().dist - cstation.A.scaledDist;
							}
						}
						
						cstation = cstation2;
					}
					
					// Step 3. Add all stations on the circuit into the virtual circuit.
//					stations.stream().forEach(sta->virtualCircuit.add(sta));

					errMsgs.add(null);
					
					break;
				}
				} // end of switch-case

			} // end of if-else
		} // end of for each circuit loop

		return errMsgs;
	}

	public boolean isScaleNeeded(RailroadLine circuit) {
		// The trunk circuit is considered as the standard for scale.
		if (circuit.calIndex == 0)
			return false;

		return circuit.hasCrossover();
	}

	int compareRailroadLine(RailroadLine c1, RailroadLine c2) {
		if (c1.calIndex == 0)
			return -1;

		if (c1.hasCrossover() && !c2.hasCrossover())
			return -1;
		else if (!c1.hasCrossover() && c2.hasCrossover())
			return 1;
		else {
			int indexDiff = c1.calIndex - c2.calIndex;
			return indexDiff != 0 ? indexDiff : c1.getName().compareToIgnoreCase(c2.getName());
		}
	}

	Optional<Station> findStationOnVirtualRailLine(Station station) {
		int index = virtualRailroadLine.indexOf(station);
		if (index >= 0)
			return Optional.of(virtualRailroadLine.get(index));
		else {
			return Optional.empty();
		}
	}

	public void print(boolean sorted) {
		print(System.out, sorted);
	}

	public void print(PrintStream out, boolean sorted) {
		// for (Circuit circuit : circuits) {
		// printCircuit(out, circuit);
		// }
		Stream<RailroadLine> stream = railroadLines.stream();
		if (sorted)
			stream = stream.sorted(this::compareRailroadLine);

		stream.forEachOrdered(circuit -> printRailroadLine(out, circuit));
	}

	public void printRailroadLine(PrintStream out, RailroadLine circuit) {
		String multiplicity = (circuit.multiplicity + "线").replace("1", "单").replace("2", "复").replace("4", "四");
		out.printf(
				"% 2d. %s %s, (%dkm), zIndex: % 2d. %s Phase:****%s****. It has the following stations: %n",
				circuit.calIndex, circuit.getName(), multiplicity, circuit.length, 
				circuit.zindex, circuit.dispReverted?"(颠倒上下行)":"", circuit.dinfo);

		for (Station station : circuit.getAllStations()) {
			printStation(out, station);
		}
	}

	public void printStation(PrintStream out, Station station) {
		out.printf("\t%s, \t%s \t% 5d/%5dkm, \tLevel:%d \t%s%n", station.getName(),
				station.isCrossover ? "Crossover" : "\t\t", station.dist,
				station.scaledDist, station.level, station.hide ? "Hidden" : "");
	}

	public void loadFromFile2(String file) throws IOException {
		BufferedReader in = null;
		Vector<RailroadLine> loadedCircuits = new Vector<RailroadLine>(8); // in most
																	// cases,
																	// there are
																	// no more
																	// than 8
																	// circuits.
		RailroadLine railline = null;
		int lineNum = 0;
		try {
			in = new BufferedReader(new InputStreamReader(
					new BOMStripperInputStream(new FileInputStream(file)),
					"UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.equalsIgnoreCase(RailroadLineChart.circuitPattern)) {
					railline = new RailroadLine();
					loadedCircuits.add(railline);
					lineNum = 0;
				} else {
					railline.parseLine(line, lineNum++);
				}
			}

			if (loadedCircuits.size() < 1) {
				throw new IOException(__("Loaded circuits are empty."));
			}

			railroadLines.clear();
			railroadLines.addAll(loadedCircuits);
			loadedCircuits.clear();

		} catch (IOException ex) {
			throw ex;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}

	}
	

}
