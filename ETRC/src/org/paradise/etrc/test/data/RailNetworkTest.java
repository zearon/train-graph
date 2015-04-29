package org.paradise.etrc.test.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.paradise.etrc.data.RailNetwork;

public class RailNetworkTest {
	
	private static RailNetwork railNetwork;

	public RailNetworkTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		railNetwork = new RailNetwork();
		railNetwork.loadFromFile("/Users/zhiyuangong/Hobby/Railroad/列车运行图/Test.crs");
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
		railNetwork.getVirtualCircuit();
		//fail("Not yet implemented");
	}

//	@Test
	public void getProjectionCircuit() {
		railNetwork.getProjectionCircuit();
		//fail("Not yet implemented");
	}


	@Test
	public void scaleUpCircuits() {
		System.out.println("\n------------scaleUpCircuits-----------");
		List<String> errMsgs = railNetwork.scaleUpCircuits();
		
		railNetwork.print(true);
		errMsgs.stream().map(msg->msg==null?"OK":msg).forEach(System.out::println);

		List<String> expectedCrossoverStations = Arrays.asList(new String[] {"永宁镇", "武昌", "信阳", "长安集", "六安", "汉口", "武昌南", "武昌东", "何刘"});
		String[] actualCrossoverStations = railNetwork.getCrossoverStations().stream().map(station->station.name).toArray(String[]::new);
		Stream.of(actualCrossoverStations).forEach(station->System.out.print("\"" + station + "\", "));
		
		assertEquals(expectedCrossoverStations.size(), actualCrossoverStations.length);
		assertTrue(Stream.of(actualCrossoverStations).anyMatch(expectedCrossoverStations::contains));
	}

}
