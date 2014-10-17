package org.paradise.etrc.data;

import java.io.*;
import java.util.Vector;

import static org.paradise.etrc.ETRC._;

/**
 * @author lguo@sina.com
 * @version 1.0
 * 
 * 运行图的区间，可以是一整条线路，也可以是某条干线的一段
 * 一个circuit内的上下行应当一致（南京西－南京理解为下行） 
 * 距离一律以下行为递增方向，如沪宁线以南京（西）站为0坐标，沪杭线以上海站为0坐标
 */

public class Circuit {
//	public static int MAX_STATION_NUM = 512;

	public String name = "";

	public int length = 0;

//	private int stationNum = 0;

//	private Station[] _stations = new Station[MAX_STATION_NUM];
	private Vector<Station> stations = new Vector<Station> (10);

	public Circuit() {
	}

	public Circuit copy() {
		Circuit cir = new Circuit();

		cir.name = this.name;
		cir.length = this.length;
//		cir.stationNum = this.stationNum;

		for (int i = 0; i < getStationNum(); i++)
//			cir._stations[i] = this._stations[i].copy();
			cir.stations.add(stations.get(i).copy());
		

		return cir;
	}
	
	public Station getStation(int index) {
//		return _stations[index];
		return stations.get(index);
	}

	public int getStationNum() {
//		return stationNum;
		return stations.size();
	}

	/**
	 * 在index前插入新的车站
	 * @param station Station
	 * @param index int
	 */
	public void insertStation(Station station, int index) {
		if ((index < 0) /* || (index >= MAX_STATION_NUM) */ )
			return;

//		Station[] newStations = new Station[MAX_STATION_NUM];
//
//		int j = 0;
//		for (int i = 0; i < index; i++) {
//			newStations[j++] = _stations[i];
//		}
//
//		newStations[j++] = station;
//
//		for (int i = index; i < stationNum; i++) {
//			newStations[j++] = _stations[i];
//		}
//
//		_stations = newStations;
		stations.insertElementAt(station, index);
//		stationNum++;
		
		if (getStationNum() != 0)
			this.length = stations.get(getStationNum() - 1).dist;
		else
			this.length = 0;
	}

	/**
	 * 在最后添加新的停站station
	 * @param station Station
	 */
	public void appendStation(Station station) {
//		Station[] newStations = new Station[MAX_STATION_NUM];
//
//		int j = 0;
//		for (int i = 0; i < stationNum; i++) {
//			newStations[j++] = _stations[i];
//		}
//
//		newStations[j++] = station;
//
//		_stations = newStations;
//		stationNum++;
		
		stations.add(station);
		
		if (getStationNum() != 0)
//			this.length = _stations[stationNum - 1].dist;
			this.length = stations.get(getStationNum() - 1).dist;
		else
			this.length = 0;
	}

	public void delStation(int index) {
		if ((index < 0) || index >= getStationNum() /*(index >= MAX_STATION_NUM)*/ )
			return;

//		Station[] newStations = new Station[MAX_STATION_NUM];
//
//		int j = 0;
//		for (int i = 0; i < index; i++) {
//			newStations[j++] = _stations[i];
//		}
//
//		for (int i = index + 1; i < stationNum; i++) {
//			newStations[j++] = _stations[i];
//		}
//
//		_stations = newStations;
		stations.remove(index);
//		stationNum--;
		
		if (getStationNum() != 0)
			this.length = stations.get(getStationNum() - 1).dist;
		else
			this.length = 0;
	}

	public void delStation(String name) {
		int index = haveTheStation(name);
		if(index >= 0)
			return;
		
//		Station[] newStations = new Station[MAX_STATION_NUM];
//
//		int j = 0;
//		for (int i = 0; i < stationNum; i++) {
//			if (!_stations[i].name.equalsIgnoreCase(name)) {
//				newStations[j++] = _stations[i];
//			}
//		}
//
//		_stations = newStations;
		
		stations.remove(index);
//		stationNum--;
		
		if (getStationNum() != 0)
			this.length = stations.get(getStationNum() - 1).dist;
		else
			this.length = 0;
	}
	
	public int haveTheStation(String theName) {
		for (int i = 0; i < getStationNum(); i++) {
			if (theName.equalsIgnoreCase(stations.get(i).name))
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
			int t1 = Train.trainTimeToInt(train.getStop(i).arrive);
			int t2 = Train.trainTimeToInt(train.getStop(i).leave);

			//跨越0点情况的处理
			int myTime = time;
			if(t2 < t1) {
				if (myTime < t2)
					myTime += 24 * 60;
				t2 += 24 * 60;
			}

			//t1 == t2时通过或者始发、终到，不作为停靠处理
			if(t1<= myTime && t2 >= myTime && t1 != t2) {
				int d = getStationDist(train.getStop(i).stationName);
//				System.out.println(train.getTrainName() + "^" + time + "~" + train.stops[i].stationName + 
//						"~" + t1 + "~" + train.stops[i].arrive + 
//						"~" + t2 + "~" + train.stops[i].leave +
//						"~" + d);
				if(d >= 0)
					return train.getStop(i).stationName;
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
			int t1 = Train.trainTimeToInt(train.getStop(i).arrive);
			int t2 = Train.trainTimeToInt(train.getStop(i).leave);
			
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
				int d = getStationDist(train.getStop(i).stationName);
				
				if(d >= 0)
					return d;
			}
		}
		
		//运行中的情况
		for(int i=0; i<train.getStopNum()-1; i++) {
			Stop s1 = train.getStop(i);
			Stop s2 = train.getStop(i+1);
			
			int d1 = getStationDist(s1.stationName);
			int d2 = getStationDist(s2.stationName);
			
			int t1 = Train.trainTimeToInt(s1.leave);
			int t2 = Train.trainTimeToInt(s2.arrive);
			
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

	public void loadFromFile(String file) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new BOMStripperInputStream(new FileInputStream(file)),"UTF-8"));

		String line;

		// 线路名
		if ((line = in.readLine()) != null) {
			this.name = line;
		} else {
			in.close();
			throw new IOException(_("Error reading circuit name."));
		}

		// 线路总长
		if ((line = in.readLine()) != null) {
			String stLength = line;
			try {
				this.length = Integer.parseInt(stLength);
			} catch (NumberFormatException e) {
				in.close();
				throw new IOException(_("Error in circuit length format."));
			}
		} else {
			in.close();
			throw new IOException(_("Error reading circuit length."));
		}

		// 站点
		while ((line = in.readLine()) != null) {
			parseStationLine(line);
		}
		
		in.close();
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
			if (stations.get(i).name.equalsIgnoreCase(stationName))
				return stations.get(i).dist;
		return -1;
	}

	public Station getStation(String stationName) {
		for (int i = 0; i < getStationNum(); i++)
			if (stations.get(i).name.equalsIgnoreCase(stationName))
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

			if (stations.get(i).name.equalsIgnoreCase(stationName))
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
				return String.format(_("Near %s Station"), stations.get(minIndex).name);
			else
				return String.format(_("%s Station"), stations.get(minIndex).name);
		} else
			return stations.get(minIndex).name;
	}

	public String getStationName(int dist) {
		return getStationName(dist, false);
	}

	public void writeTo(BufferedWriter out) throws IOException {
		out.write(name);
		out.newLine();
		out.write(length + "");
		out.newLine();
		for (int i = 0; i < getStationNum(); i++) {
			out.write(stations.get(i).name + "," + stations.get(i).dist + ","
					+ stations.get(i).level + "," + stations.get(i).hide);
			out.newLine();
		}
	}

	public void parseLine(String line, int lineNum) throws IOException {
		switch (lineNum) {
		case 0:
			this.name = line;
			break;
		case 1:
			try {
				this.length = Integer.parseInt(line);
			} catch (NumberFormatException e) {
				throw new IOException(_("Error in circuit lenght format."));
			}
			break;
		default:
			parseStationLine(line);
		}
	}

	private void parseStationLine(String line) throws IOException {
		String stStation[] = line.split(",");
		if (stStation.length < 2)
			throw new IOException(String.format(_("Invalid data for station %d"), getStationNum() + 1));

		// 站名
		String stName = stStation[0];

		// 公里数
		int dist = 0;
		String stDist = stStation[1];
		try {
			dist = Integer.parseInt(stDist);
		} catch (NumberFormatException e) {
			throw new IOException(String.format(_("Invalid distance data for station %s"), stName));
		}

		// 等级，特等站为0；读不到的则认为是特等站
		int level = 0;
		if (stStation.length > 2) {
			String stLevel = stStation[2];
			try {
				level = Integer.parseInt(stLevel);
			} catch (NumberFormatException e) {
				throw new IOException(String.format(_("Invalid level data for station %s"), stName));
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
				throw new IOException(String.format(_("Invalid hidden type data for station %s"), stName));
		}

		Station st = new Station(stName, dist, level, hide);
		stations.add(st);
//		_stations[stationNum] = st;
//		stationNum++;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(this.name);

		sb.append("共").append(this.getStationNum()).append("个车站，总长：").append(
				this.length).append("公里\n");

		for (int i = 0; i < this.getStationNum(); i++)
			sb.append(this.stations.get(i).name).append("站 距离：").append(
					this.stations.get(i).dist).append(" 等级:").append(
					this.stations.get(i).level).append(" 隐藏：").append(
					this.stations.get(i).hide).append("\n");

		return sb.toString();
	}

	public static void main(String argv[]) {
		Circuit c = new Circuit();
		try {
			c.loadFromFile("c:\\沪宁线.cir");
			System.out.println(c.name + "共" + c.getStationNum() + "个车站，总长："
					+ c.length);
			for (int i = 0; i < c.getStationNum(); i++)
				System.out.println(c.stations.get(i).name + "站" + " 距离："
						+ c.stations.get(i).dist + " 等级:" + c.stations.get(i).level
						+ " 隐藏：" + c.stations.get(i).hide);
		} catch (IOException ex) {
			System.out.println("Error:" + ex.getMessage());
		}
	}
	
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
			Station sta = getStation(train.getStop(i).stationName);
			if(sta != null)
				return sta;
		}
		
		return null;
	}
	
	public Station getLastStopOnMe(Train train) {
		for(int i=train.getStopNum() - 1; i>=0; i--) {
			Station sta = getStation(train.getStop(i).stationName);
			if(sta != null)
				return sta;
		}
		
		return null;
	}
}
