package org.paradise.etrc.test.data;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.paradise.etrc.data.RailNetworkChart;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.TrainGraphPart;

public class TrainGraphTest {
	
	TrainGraph trainGraph;
	OutputStreamWriter writer;

	public TrainGraphTest() {
	}

	@Before
	public void setUp() throws Exception {
		trainGraph = TrainGraphFactory.loadTrainGraphFromFile("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/Test3-2.trc");
//		writer = new OutputStreamWriter(System.out, "utf-8");
	}

	@After
	public void tearDown() throws Exception {
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
			
			trainGraph.getCharts().add(chart);
		}
		
		IntStream.range(1, 3).forEach(i -> {
			trainGraph.syncLineChartsWithRailNetworks();
			System.out.println(String.format("----------- Iteration %d -----------\r\n%s\r\n\r\n", 
					i, trainGraph));
		});
	}
	
//	@Test
	public void testSimplePropertyAnnotationLoad() {
		System.out.println("-------      test SimpleProperty Annotation Load      ---------");
		TrainGraphPart.setFullToString();
		try {
			System.out.println(trainGraph.settings);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSimplePropertyAnnotationSave() {
		System.out.println("-------      test SimpleProperty Annotation Save      ---------");
		TrainGraphPart.setFullToString();
		try {
			System.out.println("********* trainGraph.railNetwork");
			trainGraph.railNetwork.saveToStream(System.out, 0);
			
			System.out.println("********* trainGraph.settings");
			trainGraph.settings.saveToStream(System.out, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
