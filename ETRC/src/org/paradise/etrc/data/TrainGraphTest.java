package org.paradise.etrc.data;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.Vector;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.paradise.etrc.data.v1.RailNetworkChart;
import org.paradise.etrc.data.v1.RailNetworkMap;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;

public class TrainGraphTest {
	
	TrainGraph trainGraph;
	OutputStreamWriter writer;

	public TrainGraphTest() {
	}

	@Before
	public void setUp() throws Exception {
//		trainGraph = TrainGraphFactory.loadTrainGraphFromFile("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/Test3-2.trc");
//		writer = new OutputStreamWriter(System.out, "utf-8");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	public void load() throws IOException {
		trainGraph = TrainGraphFactory.loadTrainGraphFromFile("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/Test3-2.trc");
	}
	
	public void makeDefault(boolean loadMap) {
		trainGraph = TrainGraphFactory.createDefaultTrainGraph();
		
		int stationCount = 6;
		RailroadLine line0 = trainGraph.railNetwork.getAllRailroadLines().get(0);
		for (int i = 3; i <= stationCount; ++ i) {
			Station s = TrainGraphFactory.createInstance(Station.class);
			s.name = "Station " + i;
			s.dist = 30 * (i -1);
			if (i % 2 == 0)
				s.hide = true;
			if (i % 3 == 0)
				s.level = 1;
			else 
				s.level = 3;
			line0.appendStation(s);
		}

		int trainCount = 2;
		Random rand = new Random();
		RailNetworkChart networkChart0 = trainGraph.allCharts().get(0);
		RailroadLineChart lineChart0 = networkChart0.allRailLineCharts().get(0);
		for (int i = 1; i <= trainCount; ++ i) {
			Train t = TrainGraphFactory.createInstance(Train.class);
			networkChart0.addTrain(t);
			
			int station1 = rand.nextInt(stationCount);
			int station2 = station1;
			do {
				station2 = rand.nextInt(stationCount);
			} while (Math.abs(station1 - station2) <= 2);
			
			int step = station1 <= station2 ? 1 : -1;
			if (step > 0)
				t.trainNameDown = t.name;
			else
				t.trainNameUp = t.name;
			t.color = Color.RED;
			
			for (int j = station1; j != station2; j += step) {
				Station s = line0.getStation(j);
				Stop stop = TrainGraphFactory.createInstance(Stop.class);
				stop.name = s.name;
				stop.isPassenger  = true;
				stop.arrive = (j % 24) + ": 10";
				stop.leave = (j % 24) + ": 15";
				
				t.appendStop(stop);
			}
			
			lineChart0.addTrain(t);
		}
		
		if (loadMap) {
			RailNetworkMap map = trainGraph.map;
			try {
				map.loadFromFile(new File(this.getClass().getResource("/pic/gmap.jpg").toURI()));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

//	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
//	@Test
	public void testSimpleToString() {
		System.out.println("-------      Simple toString      ---------");
		TrainGraphPart.setSimpleToString();
		System.out.println(trainGraph.toString());
	}
	
//	@Test
	public void testDebugToString() {
		System.out.println("-------      Debug toString      ---------");
		TrainGraphPart.setDebugToString();
		System.out.println(trainGraph.toString());
	}
	
//	@Test
	public void testFullToString() {
		System.out.println("-------      Full toString      ---------");
		TrainGraphPart.setFullToString();
		
		System.out.println(trainGraph.toString());
	}
	
//	@Test
	public void testSyncLineChartsWithLines() {
		System.out.println("-------      SyncLineChartsWithLines      ---------");
		TrainGraphPart.setDebugToString();
		trainGraph.syncLineChartsWithRailNetworks();
		
		System.out.println(String.format("----------- Original -----------\r\n%s\r\n\r\n", trainGraph));
		
		for (int i = 0; i< 2; ++ i) {
			RailNetworkChart chart = TrainGraphFactory.createInstance(RailNetworkChart.class);
			
			trainGraph.allCharts().add(chart);
		}
		
		IntStream.range(1, 3).forEach(i -> {
			trainGraph.syncLineChartsWithRailNetworks();
			System.out.println(String.format("----------- Iteration %d -----------\r\n%s\r\n\r\n", 
					i, trainGraph));
		});
	}
	
//	@Test
	public void testLinePattern1() {
		System.out.println("-------      test Line Pattern 1     ---------");
		String line1 = "RailNetwork Chart {";
		
		String[] groups = TrainGraphPart.matchObjectLine(line1);
		assertNotNull(groups);
		assertNull(groups[1]);
		assertEquals("RailNetwork Chart", groups[2]);
		assertNull(groups[3]);
	}
	
//	@Test
	public void testLinePattern2() {
		System.out.println("-------      test Line Pattern 2     ---------");
		String line2 = "allTrains = All Trains {";
		
		String[] groups = TrainGraphPart.matchObjectLine(line2);
		assertNotNull(groups);
		assertEquals("allTrains", groups[1]);
		assertEquals("All Trains", groups[2]);
		assertNull(groups[3]);
	}
	
//	@Test
	public void testLinePattern3() {
		System.out.println("-------      test Line Pattern 3     ---------");
		String line3 = "All Line Charts = 1[";
		
		String[] groups = TrainGraphPart.matchObjectLine(line3);
		assertNotNull(groups);
		assertEquals("All Line Charts", groups[1]);
		assertNull(groups[2]);
		assertEquals("1", groups[3]);
	}
	
//	@Test
//	public void testBase64Region() throws IOException {
//		System.out.println("-------      test Base 64 region     ---------");
//		RailNetworkMap map = TrainGraphFactory.createInstance(RailNetworkMap.class);
//		map.loadFromFile(new File("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/map.jpg"));
//		String base64Orig = map.encodeToBase64(null);
//		
//		FileWriter writer = new FileWriter("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/map-base64-orig.txt");
//		writer.append(base64Orig);
//		writer.flush();
//		writer.close();
//		
//		load();
//		String base64Load = trainGraph.map.encodeToBase64(null);
//		
//		FileWriter writer2 = new FileWriter("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/map-base64-load.txt");
//		writer2.append(base64Load);
//		writer2.flush();
//		writer2.close();
//		
//		assertEquals(base64Orig, base64Load);
//	}
	
	@Test
	public void testSimplePropertyAnnotationSave() {
		System.out.println("-------      test SimpleProperty Annotation Save      ---------");
		TrainGraphPart.setFullToString();
		makeDefault(false);
		try {
			trainGraph.saveToStream(System.out, 0);
			
			trainGraph.saveToFile("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/Test3-2-save.trc");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSimplePropertyAnnotationLoad() {
		System.out.println("-------      test SimpleProperty Annotation Load      ---------");
		TrainGraphPart.setSimpleToString();
		
		PrintStream fileout;
		try {
			load();
			
			System.out.println("********* trainGraph");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			trainGraph.saveToStream(baos, 0);
			System.out.print(baos);
			fileout = new PrintStream(new File("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/Test3-2-load.trc"));
			fileout.print(baos);
			fileout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testArrayClass() {
		System.out.println("-------      test Array Class      ---------");
		Object[] list = {new int[0], new Integer[0], new long[0], new Long[0], 
				new boolean[0], new Boolean[0], new byte[0], new Byte[0], 
				new float[0], new Float[0], new double[0], new Double[0], 
				new String[0], new Color[0], new Object[0]};
		for (Object array : list) {
			Class<?> clazz = array.getClass();
			System.out.println(clazz.getName());
		}
	}

}
