package org.paradise.etrc.data;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

import static org.paradise.etrc.ETRC.__;

public class TrainGraphFactory {
	
	static {
		init();
	}
	
	/**
	 * 初始化ID列表, 并调用顶层对象的prepare方法, 初始化所有涉及到的TrainGraphPart
	 */
	private static void init() {
		new TrainGraph().prepareForFirstLoading();
	}
	
	/**
	 * Load a train graph element from file
	 * @param clazz
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static <T extends TrainGraphPart> T loadPartFromFile(Class<T> clazz, String fileName) 
			throws IOException {
		
		FileReader reader0 = null;
		try {
			reader0 = new FileReader(fileName);
			return (T) TrainGraphPart.loadFromReader(reader0, clazz, null);
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("运行图文件格式错误!", e);
		} finally {
			if (reader0 != null) {
				try { reader0.close(); } catch (Exception e) {}
			}
		}
	}

	/**
	 * Load a train graph from file
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static TrainGraph loadTrainGraphFromFile(String fileName) throws IOException {
		return loadPartFromFile(TrainGraph.class, fileName);
	}
	
	public static TrainGraph createDefaultTrainGraph() {
		TrainGraph tg = createTrainGraph();
		
		// Add a default railroad line rail network
		RailroadLine line = createInstance(RailroadLine.class);
		tg.railNetwork.addRailroadLine(line);
		
		// Add a default time table
		RailroadLineChart lineChart = TrainGraphFactory
				.createInstance(RailroadLineChart.class).setProperties(line);
		RailNetworkChart networkChart = TrainGraphFactory
				.createInstance(RailNetworkChart.class);
		networkChart.getRailLineCharts().add(lineChart);
		tg.charts.add(networkChart);
		
		tg.syncLineChartsWithRailNetworks();
		
		return tg;
	}
	
	public static TrainGraph createTrainGraph() {
		return createInstance(TrainGraph.class);
	}
	
	public static <T extends TrainGraphPart> T createInstance(Class<T> clazz) {
		return createInstance(clazz, null);
	}
	
	/**
	 * 
	 * @param clazz
	 * @param setNameByID
	 * @return
	 */
	public static <T extends TrainGraphPart> T createInstance(
			Class<T> clazz, String name) {
		if (clazz == null) {
			throw new IllegalArgumentException(__("can not load default instance for class NULL."));
		}
		
		String className = clazz.getName();
		Tuple<Class<? extends TrainGraphPart>, 
			Supplier<? extends TrainGraphPart>> tuple = 
				TrainGraphPart._partClassMap.get(className);
		if (tuple == null) {
			throw new IllegalArgumentException(String.format(
					__("Class %s is not registered through method prepareForFirstLoading"),
					className));
		}
		
		// Instantiate the object
		T obj = (T) tuple.B.get();
		
		// Initialize the object
		setID(clazz, obj);
		obj.initTGP();
		obj.setToDefault();
		
		if (name == null) {
			name = obj.createTGPNameById(obj._id);
		}
		if (name != null) {
			obj.setName(name);
		}
		
		return obj;
	}
	
	public static synchronized void setID(Class<? extends TrainGraphPart> clazz, 
			TrainGraphPart instance) {
		
		String className = clazz.getName();
		int id = TrainGraph._objectIdMap.get(className);
		
		instance._id = ++ id;
		
		TrainGraph._objectIdMap.put(className, id);
	}
	
	/**
	 * Reset id counters for all train graph part sub-classes.
	 */
	public static void resetIDCounters() {
		resetIDCounterForClass(null);
	}
	
	/**
	 * Reset id counter for a train graph part class.
	 * @param clazz The class whose id counter is to be reset. 
	 * If clazz is null, then reset id counters for all TrainGraphPart sub-classes.
	 */
	public static void resetIDCounterForClass(Class<? extends TrainGraphPart> clazz) {
		if (clazz == null) {
			TrainGraphPart._objectIdMap.entrySet().forEach(entry->entry.setValue(0));
		} else {
			TrainGraphPart._objectIdMap.put(clazz.getName(), 0);
		}
	}
}
