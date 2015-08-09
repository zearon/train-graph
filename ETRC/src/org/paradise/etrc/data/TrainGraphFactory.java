package org.paradise.etrc.data;

import java.awt.Color;
import java.awt.Font;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.paradise.etrc.data.v1.RailNetworkChart;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainType;

import static org.paradise.etrc.ETRC.__;

public class TrainGraphFactory {
	
	static {
		init();
	}
	
	/**
	 * 初始化ID列表, 并调用顶层对象的prepare方法, 初始化所有涉及到的TrainGraphPart
	 */
	private static void init() {
		TrainGraph trainGraph;
		try {
			trainGraph = TrainGraphPart.newInstance(TrainGraph.class);
			trainGraph.prepareForFirstLoading();
			
			resetIDCounters();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		networkChart.allRailLineCharts().add(lineChart);
		tg.allCharts().add(networkChart);
		tg.currentNetworkChart = networkChart;
		tg.currentLineChart = lineChart;
		
		// Add default train types
		TrainType[] types = new TrainType[9];
		types[0] = createInstance(TrainType.class, "Regular");
		types[1] = createInstance(TrainType.class, "Temp L");
		types[2] = createInstance(TrainType.class, "Express K");
		types[3] = createInstance(TrainType.class, "Ultra Express T");
		types[4] = createInstance(TrainType.class, "None-stop Express Z");
		types[5] = createInstance(TrainType.class, "International/Transregional Q");
		types[6] = createInstance(TrainType.class, "CRH D");
		types[7] = createInstance(TrainType.class, "Inter-city C");
		types[8] = createInstance(TrainType.class, "High Speed G");
		
		types[0].setProperties("Reg.", "\\d+", Color.decode("#008000"), TrainType.LINE_STYLE_SOLID, 
				1.0f, new float[0], __("Lucida Grande"), Font.PLAIN, 12, Color.decode("#008000")).loadComplete();
		types[1].setProperties("Temp", "L\\d+", Color.decode("#804000"), TrainType.LINE_STYLE_SOLID, 
				1.0f, new float[0], __("Lucida Grande"), Font.PLAIN, 12, Color.decode("#804000")).loadComplete();
		types[2].setProperties("K", "K\\d+", Color.decode("#ff0000"), TrainType.LINE_STYLE_SOLID, 
				1.0f, new float[0], __("Lucida Grande"), Font.PLAIN, 12, Color.decode("#ff0000")).loadComplete();
		types[3].setProperties("T", "T\\d+", Color.decode("#0000ff"), TrainType.LINE_STYLE_SOLID, 
				1.0f, new float[0], __("Lucida Grande"), Font.PLAIN, 12, Color.decode("#0000ff")).loadComplete();
		types[4].setProperties("Z", "Z\\d+", Color.decode("#0099ff"), TrainType.LINE_STYLE_SOLID, 
				1.0f, new float[0], __("Lucida Grande"), Font.PLAIN, 12, Color.decode("#0099ff")).loadComplete();
		types[5].setProperties("Q", "Q\\d+", Color.decode("#00f524"), TrainType.LINE_STYLE_SOLID, 
				1.0f, new float[0], __("Lucida Grande"), Font.PLAIN, 12, Color.decode("#00f524")).loadComplete();
		types[6].setProperties("D", "D\\d+", Color.decode("#800080"), TrainType.LINE_STYLE_SOLID, 
				1.0f, new float[0], __("Lucida Grande"), Font.PLAIN, 12, Color.decode("#008000")).loadComplete();
		types[7].setProperties("C", "C\\d+", Color.decode("#800080"), TrainType.LINE_STYLE_SOLID, 
				1.0f, new float[0], __("Lucida Grande"), Font.PLAIN, 12, Color.decode("#008000")).loadComplete();
		types[8].setProperties("G", "G\\d+", Color.decode("#ff00ff"), TrainType.LINE_STYLE_SOLID, 
				1.0f, new float[0], __("Lucida Grande"), Font.PLAIN, 12, Color.decode("#ff00ff")).loadComplete();
		tg.addAllTrainTypes(Arrays.asList(types));
		
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
		T obj = TrainGraphPart.newInstance(clazz);

		// Initialize the object
		obj.initElements();
		obj.setToDefault();
		obj.loadComplete();
		
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
