package org.paradise.etrc.data.skb;

import static org.paradise.etrc.ETRCUtil.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.paradise.etrc.data.util.BOMStripperInputStream;
import org.paradise.etrc.data.v1.RailNetworkChart;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.TrainGraphFactory;

public class ETRCSKB {
	private String path;

	/**
	 * 车次列表
	 */
	private Vector<String> cc;
	
	/**
	 * 车站列表
	 */
	private Vector<String> zm;
	
	/**
	 * 停靠信息列表
	 */
	private Vector<String []> tk;

	public ETRCSKB(String _path) throws IOException {
		path = _path;
		
		cc = new Vector<String>();
		zm = new Vector<String>();
		tk = new Vector<String []>();
		
		loadcc();
		loadzm();
		loadtk();
	}
	
	private void loadtk() throws IOException {
		File f = new File(path + "etrc.eda");
		
		DataInputStream in = new DataInputStream(new FileInputStream(f));
		
		// Skip the BOM of UTF-8 text file
		boolean firstTime = true;
		byte buf = 0;
		while(in.available() > 0) {
			buf = in.readByte();
			if (buf > 0)
				break;
		}
		
		while(in.available() > 0) {
			byte[] buffer = new byte[8];
			if (firstTime)	{
				buffer[0] = buf;
				for (int i = 0; i < 7; ++i)
				{
					buffer[i + 1] = in.readByte();
				}
				firstTime = false;
			}
			else
			{
				in.read(buffer);
			}
			
			tk.add(decodeTK(new String(buffer)));
		}
		
		in.close();
	}

	private void loadzm() throws IOException {
		File f = new File(path + "ezm.eda");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new BOMStripperInputStream(new FileInputStream(f)),"UTF-8"));
		
		String line = in.readLine();
		while(line != null) {
			zm.add(line);
			
			line = in.readLine();
		}
		
		in.close();
	}
	
	private void loadcc() throws IOException {
		File f = new File(path + "ecc.eda");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new BOMStripperInputStream(new FileInputStream(f)),"UTF-8"));
		
		String line = in.readLine();
		while(line != null) {
			cc.add(line);
			
			line = in.readLine();
		}
		
		in.close();
	}
	
	private String[] decodeTK(String tk) {
		if(tk.length() != 8)
			return new String[] {"0000", "错误", "00:00", "00:00"};
		
		int trainIndex = ETRCData.decode(tk.charAt(0)) * ETRCData.codeTable.length
		               + ETRCData.decode(tk.charAt(1));
		
		int stationIndex = ETRCData.decode(tk.charAt(2)) * ETRCData.codeTable.length
		                 + ETRCData.decode(tk.charAt(3));
		
		int h_arrive = ETRCData.decode(tk.charAt(4));
		int m_arrive = ETRCData.decode(tk.charAt(5));

		int h_leave = ETRCData.decode(tk.charAt(6));
		int m_leave = ETRCData.decode(tk.charAt(7));
		
		return new String[] {
			(String)cc.get(trainIndex),
			(String)zm.get(stationIndex),
			h_arrive + ":" + m_arrive,
			h_leave + ":" + m_leave
		};
	}
	
	public List<Train> findTrains(Collection<RailroadLine> circuits) {
		Instant instant1 = null, instant2 = null, instant3 = null;
		if (IS_DEBUG())
			instant1= Instant.now();
		HashSet<String> allStationsOnCircuits = 
				circuits.stream().flatMap(cir->cir.getAllStations().stream()).distinct()
				.map(station->station.getName().toLowerCase())
				.collect(Collectors.toCollection(HashSet::new));

		if (IS_DEBUG())
			instant2= Instant.now();
		List<Train> trains = 
				tk.stream().parallel()
				.filter(tkinfo->allStationsOnCircuits.contains(tkinfo[1].toLowerCase()))				// filter stops only matching the station lists.
				.map(tkinfo->getTrainByFullName(tkinfo[0]))
				.distinct()
				.collect(Collectors.toList());
		
		if (IS_DEBUG())
			instant3= Instant.now();
		
		DEBUG("Benchmark: [find for circuit]: GetName:%d, GetTrain:%d", instant2.toEpochMilli() - instant1.toEpochMilli(), instant3.toEpochMilli() - instant2.toEpochMilli());
		
		return trains;
	}	
	
	
	
	/**
	 * 查找经过某个circuit的车次
	 */
	public Vector<Train> findTrains(RailroadLine cir) {
		Vector<Train> trains = new Vector<Train>();
		
		for(int i=0; i<cir.getStationNum(); i++) {
			Vector<Train> newTrains = findTrains(cir.getStation(i).getName());
			
			Enumeration<Train> en = newTrains.elements();
			while(en.hasMoreElements()) {
				Train train = en.nextElement();
				if(!trains.contains(train)) {
					trains.add(train);
				}
			}
		}
		
		return trains;
	}	
	
	public void findTrains(RailNetworkChart networkChart) {
		networkChart.clearTrains();

		Vector<String> addedTrainNames = new Vector<> ();
		tk.stream().forEach(tkInfo -> {
			String trainName = tkInfo[0];
			String tkName = tkInfo[1];
			
			networkChart.allRailLineCharts().stream().forEach(lineChart -> {
				
				boolean stopAtLine = lineChart.railroadLine.getAllStations().stream()
					.anyMatch(station -> tkName.equalsIgnoreCase(station.getName()));
				if (stopAtLine) {
					Train aTrain;
					if (addedTrainNames.contains(trainName)) {
						aTrain = networkChart.findTrain(trainName);
					} else {
						aTrain = getTrainByFullName(trainName);
						addedTrainNames.add(trainName);
						networkChart.addTrain(aTrain);
					}
					
					lineChart.addTrain(aTrain);
				}
			});
		});
	}
	
	public void findTrains(RailroadLineChart lineChart) {
		Vector<Train> trains = new Vector<Train>();
		
		for(int i=0; i<lineChart.railroadLine.getStationNum(); i++) {
			Vector<Train> newTrains = findTrains(lineChart.railroadLine.getStation(i).getName());
			
			Enumeration<Train> en = newTrains.elements();
			while(en.hasMoreElements()) {
				Train train = en.nextElement();
				if(!trains.contains(train)) {
					trains.add(train);
					lineChart.addTrain(train);
				}
			}
		}
	}
	
	/**
	 * 查找经过某个车站的车次
	 * @param stationName
	 * @return
	 */
	public Vector<Train> findTrains(String stationName) {
		Vector<Train> trains = new Vector<Train>();
		
		Enumeration<String []> en = tk.elements();
		while(en.hasMoreElements()) {
			String tkInfo[] = (String[]) en.nextElement();
			
			String trainName = tkInfo[0];
			String tkName = tkInfo[1];

			if(tkName.equalsIgnoreCase(stationName)) {
				Train aTrain = getTrainByFullName(trainName);
				if(!trains.contains(aTrain))
					trains.add(aTrain);
			}
		}
		
		return trains;
	}
	
	/**
	 * 获取指定车次（根据单车次查找，可能重复，包括A B）
	 * @param trainName
	 * @return
	 */
	public Vector<Train> getTrains(String trainName) {
		Vector<Train> trains = new Vector<Train>();
		
		Enumeration<String> en = cc.elements();
		while(en.hasMoreElements()) {
			String myFullName = (String)en.nextElement();
			String myNames[] = myFullName.split("/");
			for(int i=0; i<myNames.length; i++) {
				String theName = myNames[i];
				if(theName.endsWith("A") || theName.endsWith("B"))
					theName = theName.substring(0, theName.length()-1);
				
				if(theName.equalsIgnoreCase(trainName)) {
					trains.add(getTrainByFullName(myFullName));
				}
			}
		}
		
		return trains;
	}
	
	/**
	 * 根据全称取车次（不会重复）
	 * @param trainName
	 * @return
	 */
	private Train getTrainByFullName(String trainName) {
		Train train = TrainGraphFactory.createInstance(Train.class);
		
		Enumeration<String []> en = tk.elements();
		while(en.hasMoreElements()) {
			String tkInfo[] = (String[]) en.nextElement();
			
			String myName = tkInfo[0];

			if(myName.equalsIgnoreCase(trainName)) {
				String tkName = tkInfo[1];
				String str_arrive = tkInfo[2];
				String str_leave = tkInfo[3];
				
				train.appendStop(Stop.makeStop(tkName, trainName, str_arrive, str_leave, true));
	
//				SimpleDateFormat df = new SimpleDateFormat("H:mm");
//				Date arrive = null;
//				Date leave = null;
//				
//				try {
//					arrive = df.parse(str_arrive);
//				} catch (ParseException e) {
//					//e.printStackTrace();
//				}
//				
//				try {
//					leave = df.parse(str_leave);
//				} catch (ParseException e) {
//					//e.printStackTrace();
//				}
//				
//				if(arrive == null)
//					arrive = leave;
//				
//				if(leave == null)
//					leave = arrive;
//				
//				train.appendStop(new Stop(tkName, arrive, leave));
			}
		}
		
//		train.startStation = train.stops[0].stationName;
//		train.terminalStation = train.stops[train.stopNum - 1].stationName;
		train.setName(trainName);
		
		String names[] = train.getName().split("/");
		for(int i=0; i<names.length; i++) {
			if(names[i].endsWith("A") || names[i].endsWith("B"))
				names[i] = names[i].substring(0, names[i].length()-1);

			if(Train.isDownName(names[i]))
				train.trainNameDown = names[i];
			else
				train.trainNameUp = names[i];
		}
		
		return train;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ETRCSKB data = new ETRCSKB("C:\\trains\\");
			System.out.println(data.cc.size());
			System.out.println(data.zm.size());
			System.out.println(data.tk.size());
			
			Train train = (Train) (data.getTrains("N552").get(0));
			System.out.println(train);
			
//			System.out.println(data.findTrains("南通"));
			
			RailroadLine cir = TrainGraphFactory.createInstance(RailroadLine.class); //new RailroadLine();
			cir.loadFromFile2("C:\\trains\\沪杭线.cir");
			Vector<Train> trains = data.findTrains(cir);
			System.out.println("沪杭线：" + trains.size() + "\r\n");
//			System.in.read();
//			System.out.println(trains);
			
			System.out.println(train.isDownTrain(cir));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
