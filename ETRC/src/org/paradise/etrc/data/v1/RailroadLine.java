package org.paradise.etrc.data.v1;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.annotation.TGProperty;
import org.paradise.etrc.data.util.BOMStripperInputStream;
import org.paradise.etrc.util.data.Tuple2;

import static org.paradise.etrc.ETRC.__;

/**
 * 运行图的区间，可以是一整条线路，也可以是某条干线的一段
 * 一个railroadline内的上下行应当一致（南京西－南京理解为下行） 
 * 距离一律以下行为递增方向，如沪宁线以南京（西）站为0坐标，沪杭线以上海站为0坐标
 * @author lguo@sina.com
 * @version 1.0
 * 
 */

@TGElementType(name="RailLine")
public class RailroadLine extends TrainGraphPart {
	private static int idCounter = 0;
//	public static int MAX_STATION_NUM = 512;
	
	@TGProperty
	public int length = 0;
	@TGProperty
	public int multiplicity = 2;
	@TGProperty
	public int zindex = 0;
	@TGProperty
	public float dispScale = 1.0f;
	@TGProperty
	public boolean visible = true;
	@TGProperty
	public boolean isProjection = false;
	@TGProperty
	public Color lineColor = Color.BLACK;
	
	public transient String dinfo = "";
	private transient int id;

	@TGElement(name="All Stations", isList=true, type=Station.class)
	private Vector<Station> stations = new Vector<Station> (10);
	
	public int calIndex = 0;
	public boolean dispReverted = false;   // 在铁路网络中位于其他线路保持一致,颠倒了上下行顺序.
	private int crossoverCount = 0;
	private List<Station> crossoverStations = new Vector<Station> (4);
	
	private List<Consumer<RailroadLine>> railroadLineChangedListeners = new Vector<Consumer<RailroadLine>>();

	RailroadLine() {
	}
	
	RailroadLine(String name) {
		this();
		setName(name);
	}
	
	@Override
	public void setToDefault() {
		this.length = 30;
		this.multiplicity = 2;
		this.zindex = 1;
		this.dispScale = 1.0f;
		lineColor = Color.BLACK;
		
		appendStation(TrainGraphFactory.createInstance(Station.class)
				.setProperties(0, 1, false));
		appendStation(TrainGraphFactory.createInstance(Station.class)
				.setProperties(30, 1, false));
	}

	public RailroadLine copy() {
		RailroadLine cir = new RailroadLine();

		cir.setName(this.getName());
		cir.length = this.length;
		cir.multiplicity = this.multiplicity;
		cir.zindex = this.zindex;
		cir.id = this.id;

		for (int i = 0; i < getStationNum(); i++)
			cir.stations.add(stations.get(i).copy());
		

		return cir;
	}
	
//	
//	@Override
//	public int hashCode() {
//		return id;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		return (obj != null && obj instanceof RailroadLine 
//				&& ((RailroadLine) obj).name == name) ? true : false;
//	}

	public int getID() {
		return id;
	}
	
	public void setName() {
		
	}
	
	public Vector<Station> getAllStations() {
		return stations;
	}
	
	public Station getStation(int index) {
//		return _stations[index];
		return stations.get(index);
	}

	public int getStationNum() {
//		return stationNum;
		return stations.size();
	}
	
	public void updateStations() {
		crossoverStations = stations.stream().filter(station->station.isCrossover).collect(Collectors.toList());
		crossoverCount = crossoverStations.size();
	}
	
	public boolean hasCrossover() {
		return crossoverCount > 0;
	}
	
	public int getCrossoverCount() {
		return crossoverCount;
	}
	
	public List<Station> getCrossoverStations() {
		return crossoverStations;
	}
	
	public Station getCrossoverStation(int index) {
		return crossoverStations.get(index);
	}
	
	/**
	 * Get a tuple of crossover station and its index in the circuit
	 * @param index
	 * @return
	 */
	public Tuple2<Station, Integer> getCrossoverStationTuple(int index) {
		Station station = crossoverStations.get(index);
		int stationIndex = stations.indexOf(station);
		return Tuple2.ofKey(station, stationIndex);
	}

	/**
	 * 在index前插入新的车站
	 * @param index int
	 * @param station Station
	 */
	public void insertStation(int index, Station station) {
		if ((index < 0) /* || (index >= MAX_STATION_NUM) */ )
			return;

		stations.insertElementAt(station, index);
		
		if (getStationNum() != 0)
			this.length = stations.get(getStationNum() - 1).dist;
		else
			this.length = 0;

		fireStationChangedEvent();
	}

	/**
	 * 在最后添加新的停站station
	 * @param station Station
	 */
	public void appendStation(Station station) {
		stations.add(station);
		
		if (getStationNum() != 0)
			this.length = stations.get(getStationNum() - 1).dist;
		else
			this.length = 0;
		
		fireStationChangedEvent();
	}

	public void delStation(int index) {
		if ((index < 0) || index >= getStationNum() /*(index >= MAX_STATION_NUM)*/ )
			return;
		
		stations.remove(index);
		
		if (getStationNum() != 0)
			this.length = stations.get(getStationNum() - 1).dist;
		else
			this.length = 0;

		fireStationChangedEvent();
	}

	public void delStation(String name) {
		int index = haveTheStation(name);
		if(index >= 0)
			return;
		
		stations.remove(index);
		
		if (getStationNum() != 0)
			this.length = stations.get(getStationNum() - 1).dist;
		else
			this.length = 0;
		
		fireStationChangedEvent();
	}
	
	public int haveTheStation(String theName) {
		for (int i = 0; i < getStationNum(); i++) {
			if (theName.equalsIgnoreCase(stations.get(i).getName()))
				return i;
		}

		return -1;
	}
	
	public Station getStationBetweenTheDists(int dist1, int dist2) {
		if(dist1 == -1 || dist2 == -1)
			return null;
		
		for (int i = 0; i < getStationNum(); i++) {
			if ((dist1 < stations.get(i).dist && stations.get(i).dist <= dist2) ||
				(dist2 <= stations.get(i).dist && stations.get(i).dist < dist1))
				
				return stations.get(i);
		}

		return null;
	}

	//查找在时刻time的时候列车train是否停靠在某站
	//如果停靠于某站则返回站名，否则返回空字符串
	public String getStationNameAtTheTime(Train train, int time) {
		for(int i=0; i<train.getStopNum(); i++) {
			int t1 = Train.trainTimeToInt(train.getStop(i).getArrive());
			int t2 = Train.trainTimeToInt(train.getStop(i).getLeave());

			//跨越0点情况的处理
			int myTime = time;
			if(t2 < t1) {
				if (myTime < t2)
					myTime += 24 * 60;
				t2 += 24 * 60;
			}

			//t1 == t2时通过或者始发、终到，不作为停靠处理
			if(t1<= myTime && t2 >= myTime && t1 != t2) {
				int d = getStationDist(train.getStop(i).getName());
//				System.out.println(train.getTrainName() + "^" + time + "~" + train.stops[i].stationName + 
//						"~" + t1 + "~" + train.stops[i].arrive + 
//						"~" + t2 + "~" + train.stops[i].leave +
//						"~" + d);
				if(d >= 0)
					return train.getStop(i).getName();
			}
		}
		
		return "";
	}
	
//	private boolean isBetween(int myTime, int t1, int t2) {
//		if(t1 < t2)
//			return (t1<= myTime && t2 >= myTime);
//		else
//			return (t1<=myTime && myTime<=24*60) || 
//			       (0<myTime && myTime<=t2);
//	}
	
	//查找train在时刻time的时候本线位置的距离值，不在本线上则返回-1
	public int getDistOfTrain(Train train, int time) {
		//停站的情况
		for(int i=0; i<train.getStopNum(); i++) {
			int t1 = Train.trainTimeToInt(train.getStop(i).getArrive());
			int t2 = Train.trainTimeToInt(train.getStop(i).getLeave());
			
			//跨越0点情况的处理
			int myTime = time;
			if(t2 < t1) {
				if (myTime < t2)
					myTime += 24 * 60;
				t2 += 24 * 60;
			}

			if(t1<= myTime && t2 >= myTime ) {
//				System.out.println(train.getTrainName() + "^" + time + "~" + train.stops[i].stationName + 
//						"~" + t1 + "~" + train.stops[i].arrive + 
//						"~" + t2 + "~" + train.stops[i].leave);
				int d = getStationDist(train.getStop(i).getName());
				
				if(d >= 0)
					return d;
			}
		}
		
		//运行中的情况
		for(int i=0; i<train.getStopNum()-1; i++) {
			Stop s1 = train.getStop(i);
			Stop s2 = train.getStop(i+1);
			
			int d1 = getStationDist(s1.getName());
			int d2 = getStationDist(s2.getName());
			
			int t1 = Train.trainTimeToInt(s1.getLeave());
			int t2 = Train.trainTimeToInt(s2.getArrive());
			
			//不在本线路上-继续找（可能下一天会在本线路上的）
			if(d1<0 || d2<0)
				continue;
			
			//跨越0点情况的处理
			int myTime = time;
			if(t2 < t1) {
				if (myTime < t2)
					myTime += 24 * 60;
				t2 += 24 * 60;
			}
			
			if(t1 <= myTime && t2 >= myTime) {
				int dist = (d2-d1)*(myTime-t1)/(t2-t1) + d1;
//				System.out.println(train.getTrainName() + "^" + myTime + "~" + train.stops[i].stationName + 
//						"~" + t1 + "~" + train.stops[i].arrive + 
//						"~" + t2 + "~" + train.stops[i].leave + "*****" 
//						+ d1 + "~" + d2 + "~" + dist);
				return dist;
			}
		}
		
		return -1;
	}

	public void loadFromFile2(String file) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new BOMStripperInputStream(new FileInputStream(file)),"UTF-8"));

		String line;

		// 线路名
		if ((line = in.readLine()) != null) {
			this.setName(line);
		} else {
			in.close();
			throw new IOException(__("Error reading circuit name."));
		}

		// 线路总长,单线/复线/四线，zindex, dispScale
		if ((line = in.readLine()) != null) {
			String[] parts = line.split(",");
			this.length = Integer.parseInt(parts[0]);
			String stLength = parts[0];
			try {
				this.length = Integer.parseInt(stLength);
			} catch (NumberFormatException e) {
				in.close();
				throw new IOException(__("Error in circuit length format."));
			}
			try {
				if (parts.length > 1)
					this.multiplicity = Integer.parseInt(parts[1]);
			} catch (NumberFormatException e) {
				in.close();
				throw new IOException(__("Error in circuit multiplicity format."));
			}
			try {
				if (parts.length > 2)
					this.zindex = Integer.parseInt(parts[2]);
			} catch (NumberFormatException e) {
				in.close();
				throw new IOException(__("Error in circuit zindex format."));
			}
			try {
				if (parts.length > 3)
					this.dispScale = Float.parseFloat(parts[3]);
			} catch (NumberFormatException e) {
				in.close();
				throw new IOException(__("Error in circuit display scale format."));
			}
			
		} else {
			in.close();
			throw new IOException(__("Error reading circuit length."));
		}

		// 站点
		while ((line = in.readLine()) != null) {
			parseStationLine(line);
		}
		
		in.close();		

		fireStationChangedEvent();
	}

	/**
	 * 根据站名获取该站与线路起始站之间的距离 若该站不在本线路上则返回-1
	 * 
	 * @param stationName
	 *            String
	 * @return int
	 */
	public int getStationDist(String stationName) {
		for (int i = 0; i < getStationNum(); i++)
			if (stations.get(i).getName().equalsIgnoreCase(stationName))
				return stations.get(i).dist;
		return -1;
	}

	public Station getStation(String stationName) {
		for (int i = 0; i < getStationNum(); i++)
			if (stations.get(i).getName().equalsIgnoreCase(stationName))
				return stations.get(i);
		return null;
	}

	/*
	 * public int[] getStationIndexs(ChartPanel chart) { int indexs[] = new
	 * int[stationNum]; for(int i = 0; i < stationNum; i++ } }
	 */

	public int getStationIndex(String stationName) {
		for (int i = 0; i < getStationNum(); i++) {
			// 被隐藏的站不返回
			if (stations.get(i).hide)
				continue;

			if (stations.get(i).getName().equalsIgnoreCase(stationName))
				return i;
		}
		return -1;
	}

	// 查找下一个可停站，下行递增，上行递减
	public int getNextStationIndex(Train train, int index) {
		if (train.isDownTrain(this) == Train.DOWN_TRAIN) {
			for (int i = index + 1; i < getStationNum(); i++) {
				// 被隐藏的站不返回跳过
				if (stations.get(i).hide)
					continue;
				else
					return i;
			}
		} else {
			for (int i = index - 1; i > 0; i--) {
				// 被隐藏的站不返回跳过
				if (stations.get(i).hide)
					continue;
				else
					return i;
			}
		}
		return -1;
	}

	// 查找上一个可停站，上行递增，下行递减
	public int getPrevStationIndex(Train train, int index) {
		if (train.isDownTrain(this) == Train.UP_TRAIN) {
			for (int i = index + 1; i < getStationNum(); i++) {
				// 被隐藏的站不返回跳过
				if (stations.get(i).hide)
					continue;
				else
					return i;
			}
		} else {
			for (int i = index - 1; i > 0; i--) {
				// 被隐藏的站不返回跳过
				if (stations.get(i).hide)
					continue;
				else
					return i;
			}
		}
		return -1;
	}

	//查找距离dist最近的station的Index
	public int getStationIndex(int dist) {
		int gap[] = new int[getStationNum()];
		// 计算给定距离值与各站距离值之间的差
		for (int i = 0; i < getStationNum(); i++) {
			gap[i] = Math.abs(dist - stations.get(i).dist);
		}
		// 找出距离差最小的那一站
		int minIndex = 0;
		int minGap = gap[0];
		for (int i = 1; i < getStationNum(); i++) {
			// 被隐藏的站不参加比较
			if (stations.get(i).hide)
				continue;
			if (gap[i] < minGap) {
				minIndex = i;
				minGap = gap[i];
			}
		}

		return minIndex;
	}

	//查找距离dist最近的station
	public String getStationName(int dist, boolean addSuffix) {
		int gap[] = new int[getStationNum()];
		// 计算给定距离值与各站距离值之间的差
		for (int i = 0; i < getStationNum(); i++) {
			gap[i] = Math.abs(dist - stations.get(i).dist);
		}
		// 找出距离差最小的那一站
		int minIndex = 0;
		int minGap = gap[0];
		for (int i = 1; i < getStationNum(); i++) {
			// 被隐藏的站不参加比较
			if (stations.get(i).hide)
				continue;
			if (gap[i] < minGap) {
				minIndex = i;
				minGap = gap[i];
			}
		}

		if (addSuffix) {
			if (minGap > 1)
				return String.format(__("Near %s Station"), stations.get(minIndex).getName());
			else
				return String.format(__("%s Station"), stations.get(minIndex).getName());
		} else
			return stations.get(minIndex).getName();
	}

	public String getStationName(int dist) {
		return getStationName(dist, false);
	}

	public void writeTo(BufferedWriter out) throws IOException {
		out.write(getName());
		out.newLine();
		out.write(length + "," + multiplicity + "," + zindex + "," + dispScale);
		out.newLine();
		for (int i = 0; i < getStationNum(); i++) {
			out.write(stations.get(i).getName() + "," + stations.get(i).dist + ","
					+ stations.get(i).level + "," + stations.get(i).hide);
			out.newLine();
		}
	}

	public void parseLine(String line, int lineNum) throws IOException {
		switch (lineNum) {
		case 0:
			this.setName(line);
			break;
		case 1:
			try {
				String[] parts = line.split(",");
				this.length = Integer.parseInt(parts[0]);
				if (parts.length > 1)
					this.multiplicity = Integer.parseInt(parts[1]);
				if (parts.length > 2)
					this.zindex = Integer.parseInt(parts[2]);
				if (parts.length > 3)
					this.dispScale = Float.parseFloat(parts[3]);
			} catch (NumberFormatException e) {
				throw new IOException(__("Error in circuit lenght[,multiplicity,zindex,display scale] format."));
			}
			break;
		default:
			parseStationLine(line);
		}
	}

	private void parseStationLine(String line) throws IOException {
		if ("".equals(line.trim()))
			return;
		
		String stStation[] = line.split(",");
		if (stStation.length < 2)
			throw new IOException(String.format(__("Invalid data for station %d"), getStationNum() + 1));

		// 站名
		String stName = stStation[0];

		// 公里数
		int dist = 0;
		String stDist = stStation[1];
		try {
			dist = Integer.parseInt(stDist);
		} catch (NumberFormatException e) {
			throw new IOException(String.format(__("Invalid distance data for station %s"), stName));
		}

		// 等级，特等站为0；读不到的则认为是特等站
		int level = 0;
		if (stStation.length > 2) {
			String stLevel = stStation[2];
			try {
				level = Integer.parseInt(stLevel);
			} catch (NumberFormatException e) {
				throw new IOException(String.format(__("Invalid level data for station %s"), stName));
			}
		}

		// 是否隐藏，为1、t、true是隐藏，为0、f、false不隐藏；读不到则认为不隐藏
		boolean hide = false;
		if (stStation.length > 3) {
			String stHide = stStation[3];
			if (stHide.equals("1") || stHide.equalsIgnoreCase("t")
					|| stHide.equalsIgnoreCase("true"))
				hide = true;
			else if (stHide.equals("0") || stHide.equalsIgnoreCase("f")
					|| stHide.equalsIgnoreCase("false"))
				hide = false;
			else
				throw new IOException(String.format(__("Invalid hidden type data for station %s"), stName));
		}

		Station st = TrainGraphFactory.createInstance(Station.class, stName).setProperties(dist, level, hide); //new Station(stName, dist, level, hide);
		stations.add(st);
//		_stations[stationNum] = st;
//		stationNum++;
	}

	public String repr() {
		StringBuffer sb = new StringBuffer(this.getName());

		switch (multiplicity) {
		case 1:
			sb.append("单线");
			break;
		case 2:
			sb.append("复线");
			break;
		case 4:
			sb.append("四线");
			break;
		default:
			sb.append(multiplicity).append("线");
			break;
		}
		sb.append("共").append(this.getStationNum()).append("个车站，总长：").append(
				this.length).append("公里\n");

		for (int i = 0; i < this.getStationNum(); i++)
			sb.append(this.stations.get(i).getName()).append("站 距离：").append(
					this.stations.get(i).dist).append(" 等级:").append(
					this.stations.get(i).level).append(" 隐藏：").append(
					this.stations.get(i).hide).append("\n");

		return sb.toString();
	}
	
	public void revertStations() {
		int stationCount = getStationNum();

		for (int i = 0, j = stationCount - 1; i < j; ++i, --j) {
			Station station1 = getStation(i);
			Station station2 = getStation(j);
			int tempDist = station2.dist;
			station2.dist = station1.dist;
			station1.dist = tempDist;

			delStation(j);
			delStation(i);
			insertStation(i, station2);
			insertStation(j, station1);
		}
	}

//	public static void main(String argv[]) {
//		RailroadLine c = new RailroadLine();
//		try {
//			c.loadFromFile2("c:\\沪宁线.cir");
//			System.out.println(c.name + "共" + c.getStationNum() + "个车站，总长："
//					+ c.length);
//			for (int i = 0; i < c.getStationNum(); i++)
//				System.out.println(c.stations.get(i).name + "站" + " 距离："
//						+ c.stations.get(i).dist + " 等级:" + c.stations.get(i).level
//						+ " 隐藏：" + c.stations.get(i).hide);
//		} catch (IOException ex) {
//			System.out.println("Error:" + ex.getMessage());
//		}
//	}
	
	public boolean isStartInsideMe(Train train) {
		if(train == null)
			return false;
		
		return null != getStation(train.getStartStation()); 
	}
	
	public boolean isEndInsideMe(Train train) {
		if(train == null)
			return false;
		
		return null != getStation(train.getTerminalStation());
	}

	public Station getFirstStopOnMe(Train train) {
		for(int i=0; i<train.getStopNum(); i++) {
			Station sta = getStation(train.getStop(i).getName());
			if(sta != null)
				return sta;
		}
		
		return null;
	}
	
	public Station getLastStopOnMe(Train train) {
		for(int i=train.getStopNum() - 1; i>=0; i--) {
			Station sta = getStation(train.getStop(i).getName());
			if(sta != null)
				return sta;
		}
		
		return null;
	}
	
	/**
	 * Duplicated station names can only occur in the first or the last position of a circuit.
	 * In this case, the circuit represents a loop circuit. Otherwise, the duplicates are illegal.
	 * @return the illegal duplicated station names.
	 */
	public List<String> getDuplicatedStationNames() {
		ArrayList<String> names = new ArrayList<>();
		ArrayList<String> duplicatedNames = new ArrayList<>();
		
		int stationCount = stations.size();
		String firstName = null;
		for (int i = 0; i < stationCount; ++ i) {
			Station station = stations.get(i);
			if (i == 0)
				firstName = station.getName();
			
			if (names.contains(station.getName())) {
				if (i == stationCount - 1)
					station.isLoopStation = true;
				else if (i < stationCount - 1 || (firstName != null && !firstName.equals(station.getName())))
					duplicatedNames.add(station.getName());
			} else {
				names.add(station.getName());
			}
		}
		
		return duplicatedNames;
	}
	
	public void addStationChangedListener(Consumer<RailroadLine> eventHandler) {
		railroadLineChangedListeners.add(eventHandler);
	}
	
	public void removeStationChangedListener(Consumer<RailroadLine> eventHandler) {
		railroadLineChangedListeners.remove(eventHandler);
	}
	
	protected void fireStationChangedEvent() {
		railroadLineChangedListeners.stream().parallel()
			.forEach(action->action.accept(this));
	}
	

}
