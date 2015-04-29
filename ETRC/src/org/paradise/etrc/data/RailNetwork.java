package org.paradise.etrc.data;

import static org.paradise.etrc.ETRC.__;
import static org.paradise.etrc.ETRCUtil.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * 由多条铁路线路组成的铁路网。
 * 
 * @author Jeff Gong
 *
 */
public class RailNetwork {
	protected  String name;
	protected Vector<Circuit> circuits;

	protected Vector<Station> virtualCircuit;
	protected Vector<Station> crossoverStations;

	Vector<Station> tempStations;
	
	public enum CircuitChangeType {
		ADD, REMOVE, UPDATE
	}
	private List<BiConsumer<Optional<Circuit>, CircuitChangeType>> circuitChangedListeners = 
			new Vector<BiConsumer<Optional<Circuit>, CircuitChangeType>>();

	public RailNetwork() {
		circuits = new Vector<>(8);
		virtualCircuit = new Vector<>(100);
		crossoverStations = new Vector<>(20);
		tempStations = new Vector<>(100);
	}

	public RailNetwork(String name) {
		this();
		this.name = name;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector<Circuit> getCircuits() {
		return circuits;
	}
	
	public void addCircuit(Circuit circuit) {
		circuits.add(circuit);
		fireCircuitChangedEvent(circuit, CircuitChangeType.ADD);
		circuit.addCircuitChangedListener(this::onCircuitChange);
	}
	
	public void insertCircuit(Circuit circuit, int index) {
		circuits.insertElementAt(circuit, index);
		fireCircuitChangedEvent(circuit, CircuitChangeType.ADD);
		circuit.addCircuitChangedListener(this::onCircuitChange);
	}
	
	public void removeCircuit(Circuit circuit) {
		circuits.remove(circuit);
		fireCircuitChangedEvent(circuit, CircuitChangeType.REMOVE);
		circuit.removeCircuitChangedListener(this::onCircuitChange);
	}
	
	public void removeCircuit(int index) {
		Circuit circuit = circuits.remove(index);
		fireCircuitChangedEvent(circuit, CircuitChangeType.REMOVE);
		circuit.removeCircuitChangedListener(this::onCircuitChange);
	}

	public Vector<Station> getCrossoverStations() {
		return crossoverStations;
	}
	
	public void addCircuitChangedListener(BiConsumer<Optional<Circuit>, CircuitChangeType> eventHandler) {
		circuitChangedListeners.add(eventHandler);
	}
	
	public void removeCircuitChangedListener(BiConsumer<Optional<Circuit>, CircuitChangeType> eventHandler) {
		circuitChangedListeners.remove(eventHandler);
	}
	
	public void fireCircuitChangedEvent(Circuit circuit, CircuitChangeType changeType) {
		circuitChangedListeners.stream().parallel()
			.forEach(action->action.accept(Optional.ofNullable(circuit), changeType));
	}
	
	protected void onCircuitChange(Circuit circuit) {
		fireCircuitChangedEvent(circuit, CircuitChangeType.UPDATE);
	}

	public void addProjectionNode() {
		throw new NotImplementedException();
	}

	public void removeProjectionNode() {
		throw new NotImplementedException();
	}

	public Circuit getProjectionCircuit() {
		throw new NotImplementedException();
	}

	public Circuit getVirtualCircuit() {
		throw new NotImplementedException();
	}

	public List<String> findCrossoverStations() {
		
		List<String>[] duplicateStationsInCircuit = new List [circuits.size()];
		
		for (int ci = 0; ci < circuits.size(); ++ci) {
			Circuit circuit = circuits.get(ci);
			circuit.calIndex = ci;

			Vector<Station> stations = circuit.getAllStations();
			duplicateStationsInCircuit[ci]= circuit.getDuplicatedStationNames();
			

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
		
		List<String> errMsg = Stream.of(duplicateStationsInCircuit)
				.map((List<String> list)->list.size()<1?null:String.format(__("There are duplicate stations on circuit: %s"), 
						list.stream().reduce("", (a, b)->a.length() > 0 ? a+", "+b : b)))
				.collect(Collectors.toList());
		
		return errMsg;
	}

	/**
	 * 
	 * @return Error messages
	 */
	public List<String> scaleUpCircuits() {
		crossoverStations.clear();
		tempStations.clear();

		List<String> errMsgs = findCrossoverStations();
		if (errMsgs.stream().anyMatch(msg->msg!=null)) {
			errMsgs.stream().filter(msg->msg!=null).forEach(msg->DEBUG("ERROR occured: %d", msg));
			return errMsgs;
		}

		// Update crossover station status for each circuit
		circuits.stream().flatMap(circuit -> circuit.getAllStations().stream())
				.filter(crossoverStations::contains)
				.forEach(station -> station.isCrossover = true);
		circuits.stream().parallel()
				.forEach(circuit -> circuit.updateStations());

		/*
		 * Sort circuits and prepare for calculate according to the following
		 * rules 1. The first circuit is treated as the trunk circuit, so keep
		 * it at list head. 2. Circuits with crossover stations are in front of
		 * those without. 3. Respect the original circuit order as possible.
		 */
		List<Circuit> sortedCircuits = circuits.stream()
				.sorted(this::compareCircuit).collect(Collectors.toList());

		// Calculate
		virtualCircuit.clear();
		errMsgs = new ArrayList<String> (circuits.size());
		for (int ci = 0; ci < sortedCircuits.size(); ++ci) {
			Circuit circuit = circuits.get(ci);
			circuit.calIndex = ci;

			if (!isScaleNeeded(circuit)) {
				// For the trunk circuit or those circuits without any crossover stations.
				virtualCircuit.addAll(circuit.getAllStations());
				circuit.getAllStations().stream()
						.forEach(station -> station.scaledDist = station.dist);
				
				if (DEBUG())
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
					Optional<Station> stationOnVCircuit = findStationOnVirtualCircuit(station);
					if (!stationOnVCircuit.isPresent())
						virtualCircuit.add(station);
					int offset = stationOnVCircuit.orElse(station).dist
							- station.dist;
					
					stations.stream().filter(sta->! station.equals(sta))
						.forEach(sta -> {
							sta.scaledDist = sta.dist + offset;
							virtualCircuit.add(sta);
						});
					
					if (DEBUG())
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
						Tuple<Station, Integer> cstation = circuit.getCrossoverStationTuple(csindex);
						Tuple<Station, Integer> cstation2 = circuit.getCrossoverStationTuple(csindex + 1);
						Optional<Station> cstationOnVCircuit = findStationOnVirtualCircuit(cstation.A);
						Optional<Station> cstation2OnVCircuit = findStationOnVirtualCircuit(cstation2.A);

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
							
							if (DEBUG())
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

	public boolean isScaleNeeded(Circuit circuit) {
		// The trunk circuit is considered as the standard for scale.
		if (circuit.calIndex == 0)
			return false;

		return circuit.hasCrossover();
	}

	int compareCircuit(Circuit c1, Circuit c2) {
		if (c1.calIndex == 0)
			return -1;

		if (c1.hasCrossover() && !c2.hasCrossover())
			return -1;
		else if (!c1.hasCrossover() && c2.hasCrossover())
			return 1;
		else {
			int indexDiff = c1.calIndex - c2.calIndex;
			return indexDiff != 0 ? indexDiff : c1.name.compareToIgnoreCase(c2.name);
		}
	}

	Optional<Station> findStationOnVirtualCircuit(Station station) {
		int index = virtualCircuit.indexOf(station);
		if (index >= 0)
			return Optional.of(virtualCircuit.get(index));
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
		Stream<Circuit> stream = circuits.stream();
		if (sorted)
			stream = stream.sorted(this::compareCircuit);

		stream.forEachOrdered(circuit -> printCircuit(out, circuit));
	}

	public void printCircuit(PrintStream out, Circuit circuit) {
		String multiplicity = (circuit.multiplicity + "线").replace("1", "单").replace("2", "复").replace("4", "四");
		out.printf(
				"% 2d. %s %s, (%dkm), zIndex: % 2d. %s Phase:****%s****. It has the following stations: %n",
				circuit.calIndex, circuit.name, multiplicity, circuit.length, 
				circuit.zindex, circuit.dispReverted?"(颠倒上下行)":"", circuit.dinfo);

		for (Station station : circuit.getAllStations()) {
			printStation(out, station);
		}
	}

	public void printStation(PrintStream out, Station station) {
		out.printf("\t%s, \t%s \t% 5d/%5dkm, \tLevel:%d \t%s%n", station.name,
				station.isCrossover ? "Crossover" : "\t\t", station.dist,
				station.scaledDist, station.level, station.hide ? "Hidden" : "");
	}

	public void loadFromFile(String file) throws IOException {
		BufferedReader in = null;
		Vector<Circuit> loadedCircuits = new Vector<Circuit>(8); // in most
																	// cases,
																	// there are
																	// no more
																	// than 8
																	// circuits.
		Circuit circuit = null;
		int lineNum = 0;
		try {
			in = new BufferedReader(new InputStreamReader(
					new BOMStripperInputStream(new FileInputStream(file)),
					"UTF-8"));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.equalsIgnoreCase(Chart.circuitPattern)) {
					circuit = new Circuit();
					loadedCircuits.add(circuit);
					lineNum = 0;
				} else {
					circuit.parseLine(line, lineNum++);
				}
			}

			if (loadedCircuits.size() < 1) {
				throw new IOException(__("Loaded circuits are empty."));
			}

			circuits.clear();
			circuits.addAll(loadedCircuits);
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
