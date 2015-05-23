package org.paradise.etrc.test.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.v1.RailNetwork;
import org.paradise.etrc.data.v1.RailNetworkChart;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.TrainGraph;

public class RailNetworkTest {
	
	private static TrainGraph trainGraph;
	private static RailNetwork railNetwork;

	public RailNetworkTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		railNetwork = TrainGraphFactory.createInstance(RailNetwork.class);
		railNetwork.loadFromFile2("/Users/zhiyuangong/Hobby/Railroad/列车运行图/Test.crs");
		railNetwork.setName("Test Railroad Network");
		
		File chartFile = new File("/Users/zhiyuangong/Hobby/Railroad/sample.trc");
		RailroadLineChart chart = TrainGraphFactory.createInstance(RailroadLineChart.class);
		chart.loadFromFile2(chartFile);	

		
		trainGraph = TrainGraphFactory.createInstance(TrainGraph.class);
		trainGraph.setName("Test Train Graph");
		trainGraph.railNetwork = railNetwork;
//		trainGraph.allTrains.trains.addAll(chart.trains);
		RailNetworkChart railNetworkChart = TrainGraphFactory.createInstance(RailNetworkChart.class);;
		railNetworkChart.allRailLineCharts().add(chart);
		trainGraph.allCharts().add(railNetworkChart);
		

		TrainGraphPart.setDebugToString();
		
//		railNetwork.print(false);
	}

	@After
	public void tearDown() throws Exception {
	}

	//@Test
	public void testPrint() {
		//railNetwork.print(System.out);
		//fail("Not yet implemented");
	}

//	@Test
	public void getVirtualCircuit() {
		railNetwork.getVirtualRailroadLine();
		//fail("Not yet implemented");
	}

//	@Test
	public void getProjectionCircuit() {
		railNetwork.getProjectionCircuit();
		//fail("Not yet implemented");
	}


//	@Test
	public void scaleUpCircuits() {
		System.out.println("\n------------scaleUpCircuits-----------");
		List<String> errMsgs = railNetwork.scaleUpRailroadLines();
		
		railNetwork.print(true);
		errMsgs.stream().map(msg->msg==null?"OK":msg).forEach(System.out::println);

		List<String> expectedCrossoverStations = Arrays.asList(new String[] {"永宁镇", "武昌", "信阳", "长安集", "六安", "汉口", "武昌南", "武昌东", "何刘"});
		String[] actualCrossoverStations = railNetwork.getCrossoverStations().stream().map(station->station.getName()).toArray(String[]::new);
		Stream.of(actualCrossoverStations).forEach(station->System.out.print("\"" + station + "\", "));
		
		assertEquals(expectedCrossoverStations.size(), actualCrossoverStations.length);
		assertTrue(Stream.of(actualCrossoverStations).anyMatch(expectedCrossoverStations::contains));
	}
	
	@Test
	public void testSaveTrainGraph() throws IOException {
		System.out.println("\n------------testSaveRailNetwork-----------");
		trainGraph.saveToFile("/Users/zhiyuangong/Hobby/Railroad/列车运行图/Test.test1");
		
//		trainGraph.print(System.out);
	}
	
	@Test
	public void testLoadTrainGraph() throws IOException {
		System.out.println("\n------------testLoadTrainGraph-----------");
		TrainGraph graph2 = TrainGraphFactory.loadTrainGraphFromFile(
				"/Users/zhiyuangong/Hobby/Railroad/列车运行图/Test.test1");
		
		graph2.saveToStream(System.out);
	}
	
//	@Test
	public void testColor() throws IOException {
		System.out.println("\n------------testColor-----------");
		
		Color color1 = Color.GREEN;
		String colorStr1 = "#" + Integer.toHexString(color1.getRGB() & 0x11ffffff);
		Color color2 = Color.decode(colorStr1);
		System.out.println(String.format("Color1=%s, rgb=%d, colorStr1=%s, color2=%s", 
				color1, color1.getRGB(), colorStr1, color2));
	}

}
