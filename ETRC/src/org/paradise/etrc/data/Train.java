package org.paradise.etrc.data;

import static org.paradise.etrc.ETRC.__;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.BOMStripperInputStream;
import org.paradise.etrc.data.util.Tuple;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class Train extends TrainGraphPart<Train, Stop> {
//	public static int MAX_STOP_NUM = 100;

	public String trainNameDown = "";

	public String trainNameUp = "";
	
	/**
	 * LGuo 20070114 added
	 * 用于部分解决三车次以上的问题，暂时还不能存储
	 * 
	 * LGuo 20070119
	 * 解决存储问题，trf文件新版本
	 */
	public String trainNameFull = null;

//	改成getStartStation()方法，直接取stop[0]的站名
//	再次添加，以便加入不全的点单，但采用set方法，不直接存取，get方法优先取本值
	private String startStation = "";
//	改成getTerminalStation()方法，直接取stop[stopNum-1]的站名
//	再次添加，以便加入不全的点单，但采用set方法，不直接存取，get方法优先取本值
	private String terminalStation = "";

//	private int stopNum = 0;

//	private Stop[] _stops = new Stop[MAX_STOP_NUM];
	private Vector<Stop> stops = new Vector<Stop>(15);

	public Color color = null;
	public String getColorStr() {
		return color == null ? "" : "#" + Integer.toHexString(color.getRGB() & 0x11ffffff);
	}
	public void setColorByStr(String colorStr) {
		try {
			color = Color.decode(colorStr);
		} catch (Exception e) {
			System.err.println("Invalid color string:" + colorStr);
		}
	}
	
	private List<Consumer<Train>> trainChangedListeners = new Vector<Consumer<Train>> ();

	public Train() {
	}

	public Stop getStop(int index) {
		return stops.get(index);
	}
	
	public void setStop(int index, Stop stop) {
		//
	}
	
//	private Stop[] getStops() {
//		return _stops;
//	}

//	public void setStops(Stop[] stops) {
//		this._stops = stops;
//	}

	public int getStopNum() {
		return stops.size();
	}

	public Train copy() {
		Train tr = new Train();
		tr.color = color;
//		tr.startStation = startStation;
//		tr.terminalStation = terminalStation;
		tr.trainNameDown = trainNameDown;
		tr.trainNameUp = trainNameUp;
		tr.trainNameFull = trainNameFull;
//		tr.setStopNum(stopNum);
		for (int i = 0; i < getStopNum(); i++) {
//			tr.getStops()[i] = getStops()[i].copy();
			tr.stops.add(stops.get(i).copy());
		}
		return tr;
	}
	
	public void setStartStation(String sta) {
		startStation = sta;
	}
	
	public void setTerminalStation(String sta) {
		terminalStation = sta;
	}
	
	public String getStartStation() {
		if(!startStation.equalsIgnoreCase(""))
			return startStation;
		else if(getStopNum() > 0)
			return stops.get(0).stationName;
		else
			return "";
	}
	
	public String getTerminalStation() {
		if(!terminalStation.equalsIgnoreCase(""))
			return terminalStation;
		else if(getStopNum() > 0)
			return stops.get(getStopNum() - 1).stationName;
		else
			return "";
	}

	/**
	 * 在index前插入新的停站stop
	 * @param stop Stop
	 * @param index int
	 */
	public void insertStop(Stop stop, int index) {
		if ((index < 0) /* || (index >= MAX_STOP_NUM) */ )
			return;

//		Stop[] newStops = new Stop[MAX_STOP_NUM];
//
//		int j = 0;
//		for (int i = 0; i < index; i++) {
//			newStops[j++] = getStops()[i];
//		}
//
//		newStops[j++] = stop;
//
//		for (int i = index; i < getStopNum(); i++) {
//			newStops[j++] = getStops()[i];
//		}
//
//		setStops(newStops);
//		setStopNum(getStopNum() + 1);
		
		stops.insertElementAt(stop, index);
		
		fireTrainChangedEvent();
	}

	/**
	 * 在最后添加新的停站stop
	 * @param stop Stop
	 */
	public void appendStop(Stop stop) {
//		Stop[] newStops = new Stop[MAX_STOP_NUM];
//
//		int j = 0;
//		for (int i = 0; i < getStopNum(); i++) {
//			newStops[j++] = getStops()[i];
//		}
//
//		newStops[j++] = stop;
//
//		setStops(newStops);
//		setStopNum(getStopNum() + 1);
		stops.add(stop);
		
		fireTrainChangedEvent();
	}

	public void delStop(int index) {
		if ((index < 0) || index >= getStopNum() /*(index >= MAX_STOP_NUM) */ )
			return;

//		Stop[] newStops = new Stop[MAX_STOP_NUM];
//
//		int j = 0;
//		for (int i = 0; i < index; i++) {
//			newStops[j++] = getStops()[i];
//		}
//
//		for (int i = index + 1; i < getStopNum(); i++) {
//			newStops[j++] = getStops()[i];
//		}
//
//		setStops(newStops);
//		setStopNum(getStopNum() - 1);
		
		stops.remove(index);
		
		fireTrainChangedEvent();
	}

	public void delStop(String name) {
//		Stop[] newStops = new Stop[MAX_STOP_NUM];

//		int j = 0;
		for (int i = 0; i < getStopNum(); i++) {
			if (!stops.get(i).stationName.equalsIgnoreCase(name)) {
//				newStops[j++] = getStops()[i];
				stops.remove(i);
			}
		}
//
//		setStops(newStops);
//		setStopNum(getStopNum() - 1);
		
		fireTrainChangedEvent();
	}

	public void loadFromFile2(String file) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new BOMStripperInputStream(new FileInputStream(file)),"UTF-8"));

		String line;
		//车次
		if ((line = in.readLine()) != null) {
			paserTrainNameLine(line);
		} else {
			in.close();
			throw new IOException(__("Error reading train number."));
		}

		//始发站
		if ((line = in.readLine()) != null) {
			this.setStartStation(line);
		} else {
			in.close();
			throw new IOException(__("Error reading departure station."));
		}

		//终到站
		if ((line = in.readLine()) != null) {
			this.setTerminalStation(line);
		} else {
			in.close();
			throw new IOException(__("Error reading terminal station."));
		}

		//停站
		while ((line = in.readLine()) != null) {
			parseStopLine(line);
		}
		
		in.close();

		if (getStopNum() < 2)
			throw new IOException(__("Data incomplete in:" + file ));
		
		fireTrainChangedEvent();
	}
	
	public void writeTo(String fileName) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		
		this.writeTo(out);
		
		out.close();
	}

	public void writeTo(BufferedWriter out) throws IOException {
//		//旧版
//		//车次，需要判断上下行车次是否有空
//		if (trainNameDown.equalsIgnoreCase("")) {
//			out.write(trainNameUp);
//		} else if (trainNameUp.equalsIgnoreCase("")) {
//			out.write(trainNameDown);
//		} else {
//			out.write(trainNameDown + "," + trainNameUp);
//		}
		//新版本
		out.write("trf2," + getTrainName() + "," +
				  trainNameDown + "," +
				  trainNameUp);
		out.newLine();
		//始发站
		out.write(getStartStation());
		out.newLine();
		//终到站
		out.write(getTerminalStation());
		out.newLine();
		//停站
		for (int i = 0; i < getStopNum(); i++) {
			out.write(stops.get(i).stationName + ","
					+ stops.get(i).arrive + ","
					+ stops.get(i).leave + ","
					+ stops.get(i).isPassenger); //20070224新增，是否图定
			out.newLine();
		}
	}

	public void parseLine(String line, int lineNum) throws IOException {
		switch (lineNum) {
		case 0:
			paserTrainNameLine(line);
			break;
		case 1:
			this.setStartStation(line);
			break;
		case 2:
			this.setTerminalStation(line);
			break;
		default:
			parseStopLine(line);
		}
	}

	private void parseStopLine(String line) throws IOException {
//		SimpleDateFormat df = new SimpleDateFormat("H:mm");

		String stStop[] = line.split(",");
		if (stStop.length < 3)
			throw new IOException(String.format(__("Station %d data error in line %s"), (getStopNum() + 1), line));
		
		//20070224增加是否图定
		boolean isSchedular = true;
		if (stStop.length >= 4) {
			isSchedular = Boolean.valueOf(stStop[3]).booleanValue();
		}

		//站名
		String stName = stStop[0];

		//到点
		String stArrive = stStop[1];
//		Date arrive = new Date(0);
//		try {
//			arrive = df.parse(stArrive);
//		} catch (ParseException e) {
//			System.err.print("E");
//			throw new IOException(stName + "站到点读取错");
//		}

		//发点
		String stLeave = stStop[2];
//		Date leave = new Date(0);
//		try {
//			leave = df.parse(stLeave);
//		} catch (ParseException e) {
//			throw new IOException(stName + "站发点读取错");
//		}

		Stop stop = new Stop(stName, stArrive, stLeave, isSchedular);
		stops.add(stop);
//		getStops()[getStopNum()] = stop;
//		setStopNum(getStopNum() + 1);
	}

	private void paserTrainNameLine(String line) throws IOException {
		String trainName[] = line.split(",");
		
		//新版trf文件
		if (line.startsWith("trf2")) {
			trainNameFull = trainName[1];
			
			if(trainName.length == 4) {
				if(isDownName(trainName[2]))
					trainNameDown = trainName[2];
				else if(isUpName(trainName[2]))
					trainNameUp = trainName[2];

				if(isDownName(trainName[3]))
					trainNameDown = trainName[3];
				else if(isUpName(trainName[3]))
					trainNameUp = trainName[3];
			}
			else if(trainName.length == 3) {
				trainNameDown = isDownName(trainName[2]) ? trainName[2] : "";
				trainNameUp = isDownName(trainName[2]) ? "" : trainName[2];
			}
		}
		// 旧版trf文件
		else {
			if (trainName.length == 1) {
				trainNameDown = isDownName(trainName[0]) ? trainName[0] : "";
				trainNameUp = isDownName(trainName[0]) ? "" : trainName[0];
			} else if (trainName.length == 2) {
				trainNameDown = isDownName(trainName[0]) ? trainName[0]
						: trainName[1];
				trainNameUp = isDownName(trainName[0]) ? trainName[1]
						: trainName[0];
			} else
				throw new IOException("车次读取错");
		}
	}

	public static boolean isDownName(String trainName) {
		if (trainName.endsWith("1") || trainName.endsWith("3")
				|| trainName.endsWith("5") || trainName.endsWith("7")
				|| trainName.endsWith("9"))
			return true;

		return false;
	}

	public static boolean isUpName(String trainName) {
		if (trainName.endsWith("2") || trainName.endsWith("4")
				|| trainName.endsWith("6") || trainName.endsWith("8")
				|| trainName.endsWith("0"))
			return true;

		return false;
	}

	public String getTrainName() {
		//LGuo 20070114 added 如果有全称则返回全称（主要用于三车次以上以及AB车的情形）
		if (trainNameFull != null)
			return trainNameFull;
		else if (trainNameDown.trim().equalsIgnoreCase(""))
			return trainNameUp;
		else if (trainNameUp.trim().equalsIgnoreCase(""))
			return trainNameDown;
		else
			return trainNameDown.compareToIgnoreCase(trainNameUp) < 0 ? 
					trainNameDown + "/" + trainNameUp : 
					trainNameUp + "/" + trainNameDown;
	}

	public String getTrainName(RailroadLine c) {
		switch (isDownTrain(c)) {
		case DOWN_TRAIN:
			return trainNameDown == "" ? trainNameUp : trainNameDown;
		case UP_TRAIN:
			return trainNameUp == ""? trainNameDown : trainNameUp;
		default:
			return getTrainName();
		}
	}
	
	public void insertStopAfter(Stop afterStop, String newStopName,	String arrive, String leave, boolean isSchedular) {
		Stop newStop = new Stop(newStopName, arrive, leave, isSchedular);
		
		insertStopAfter(afterStop, newStop);
	}

	public void insertStopAfter(Stop afterStop, Stop newStop) {
		if(afterStop == null)
			insertStopAtFirst(newStop);
		
//		Stop newStops[] = new Stop[MAX_STOP_NUM];
//		int newStopNum = 0;
		for (int i = 0; i < getStopNum(); i++) {
//			newStops[newStopNum] = getStops()[i];
//			newStopNum++;

			if (stops.get(i).equals(afterStop)) {
//				newStops[newStopNum] = newStop;
//				newStopNum++;
				stops.insertElementAt(newStop, i);
			}
		}

//		setStops(newStops);
//		setStopNum(newStopNum);
		
		fireTrainChangedEvent();
	}

	private void insertStopAtFirst(Stop newStop) {
		insertStop(newStop, 0);
	}

	public void replaceStop(String oldName, String newName) {
		for (int i = 0; i < getStopNum(); i++) {
			if (stops.get(i).stationName.equalsIgnoreCase(oldName))
				stops.get(i).stationName = newName;
		}
		
		fireTrainChangedEvent();
	}

	public void setArrive(String name, String _arrive) {
		for (int i = 0; i < getStopNum(); i++) {
			if (stops.get(i).stationName.equalsIgnoreCase(name))
				stops.get(i).arrive = _arrive;
		}
		
		fireTrainChangedEvent();
	}

	public void setLeave(String name, String _leave) {
		for (int i = 0; i < getStopNum(); i++) {
			if (stops.get(i).stationName.equalsIgnoreCase(name))
				stops.get(i).leave = _leave;
		}
		
		fireTrainChangedEvent();
	}

	public static final int UNKNOWN = 0;

	public static final int DOWN_TRAIN = 1;

	public static final int UP_TRAIN = 2;

	public int isDownTrain(RailroadLine c) {
		return isDownTrain(c, true);
	}
	public int isDownTrain(RailroadLine c, boolean isGuessByTrainName) {
		int lastDist = -1;
		for (int i = 0; i < getStopNum(); i++) {
			int thisDist = c.getStationDist(stops.get(i).stationName);
			//当上站距离不为-1时，即经过本线路第二站时可以判断上下行
			if ((lastDist != -1) && (thisDist != -1)) {
				//本站距离大于上站距离，下行
				if (thisDist > lastDist)
					return DOWN_TRAIN;
				else
					return UP_TRAIN;
			}
			lastDist = thisDist;
		}
		//遍历完仍然未能确定
		return isGuessByTrainName?isDownTrainByTrainName(c):UNKNOWN;
	}

	private int isDownTrainByTrainName(RailroadLine c) {
		String name = getTrainName();
		if((name.endsWith("1")) ||
		   (name.endsWith("3")) ||
		   (name.endsWith("5")) ||
		   (name.endsWith("7")) ||
		   (name.endsWith("9")))
			return Train.DOWN_TRAIN;
		else
			return Train.UP_TRAIN;
	}

	/**
	 * 取时间字符串
	 * @param time Date
	 * @return String
	 */
//	public static String toTrainFormat(Date time) {
//		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
//		return df.format(time);
//	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof Train))
			return false;

		return ((Train) obj).getTrainName().equalsIgnoreCase(
				this.getTrainName());
	}
	
	@Override
	public int hashCode() {
		return getTrainName().hashCode();
	}

	public String toString() {
		String strRt = getTrainName() + "次从" + getStartStation() + "到"
				+ getTerminalStation() + "，共经停" + getStopNum() + "个车站\r\n";

		for (int i = 0; i < getStopNum(); i++)
			strRt += stops.get(i).stationName + "站 "
					+ stops.get(i).arrive + " 到 "
					+ stops.get(i).leave + " 发\r\n";

		return strRt;
	}

	//测试用
	public static void main(String argv[]) {
		Train t = new Train();
		try {
			t.loadFromFile2("c:\\N518_519_w.trf");

			System.out.println(t.getTrainName() + "次从" + t.getStartStation() + "到"
					+ t.getTerminalStation() + "，共经停" + t.getStopNum() + "个车站");
			for (int i = 0; i < t.getStopNum(); i++)
				System.out.println(t.stops.get(i).stationName + "站 "
						+ t.stops.get(i).arrive + " 到 "
						+ t.stops.get(i).leave + " 发");

			File f = new File("c:\\test_w.trf");
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			t.writeTo(out);
			out.flush();
			out.close();
		} catch (IOException ex) {
			System.out.println("Error:" + ex.getMessage());
		}
	}

	/**
	 * getNextStopName
	 *
	 * @param string String
	 * @return int
	 */
	public String getNextStopName(String stopName) {
		int i = 0;
		for (i = 0; i < getStopNum(); i++) {
			if (stops.get(i).stationName.equalsIgnoreCase(stopName))
				break;
		}
		if (i < getStopNum() - 1)
			return stops.get(i + 1).stationName;
		else
			return null;
	}

	public String getPrevStopName(String stopName) {
		int i = 0;
		for (i = 0; i < getStopNum(); i++) {
			if (stops.get(i).stationName.equalsIgnoreCase(stopName))
				break;
		}
		if (i > 1)
			return stops.get(i - 1).stationName;
		else
			return null;
	}
	
	public static Color getTrainColorByName(String trainName) {
		char type = trainName.toUpperCase().charAt(0);
		switch(type) {
		case 'G':
			return new Color(255, 0, 190);
		case 'Z':
		case 'D':
		case 'C':
			return new Color(128, 0, 128);
		case 'T':
			return Color.BLUE;
		case 'K':
		case 'N':
			return Color.RED;
		case 'L':
		case 'A':
			return new Color(128, 64, 0);
		default:
			return new Color(0, 128, 0);
		}
	}
	
	public Color getDefaultColor() {
		return getTrainColorByName(getTrainName());
	}

	public static String makeFullName(String[] names) {
		String name1 = "ZZZ";
		String name2 = "ZZZ";
		String name3 = "ZZZ";
		String name4 = "ZZZ";

		for(int i=0; i<names.length; i++) {
			String theName = (String) names[i];
			if(theName.compareTo(name1) < 0) {
				name4 = name3;
				name3 = name2;
				name2 = name1;
				name1 = theName;
			}
			else if(theName.compareTo(name2) < 0) {
				name4 = name3;
				name3 = name2;
				name2 = theName;
			}
			else if(theName.compareTo(name3) < 0) {
				name4 = name3;
				name3 = theName;
			}
			else if(theName.compareTo(name4) < 0)
				name4 = theName;
		}
		
		String name = name1;
		if(!name2.equals("ZZZ"))
			name += "/" + name2;
		if(!name3.equals("ZZZ"))
			name += "/" + name3;
		if(!name4.equals("ZZZ"))
			name += "/" + name4;
		
		return name;
	}
	
	public static String makeFullName(Vector<String> names) {
		return makeFullName((String[]) names.toArray());
	}

	//格式化输入的时间，如果格式有误则用原来的时间
	//时分间隔可以用空格（任意多个），全角或者半角的分号、句号
	//当输入3位或者4位纯数字时解析为后两位分钟，前一、两位小时
	public static String formatTime(String oldTime, String input) {
		input = input.trim();
		
		//允许为空
		if(input.equals(""))
			return input;

		input = input.replaceAll(" ", "");
		if(input.length() == 3 
		   && input.charAt(0) >= '0' && input.charAt(0) <= '9'
		   && input.charAt(1) >= '0' && input.charAt(1) <= '9'
		   && input.charAt(2) >= '0' && input.charAt(2) <= '9') {
			
			input = "0" + input.charAt(0) + ":" 
			       + input.charAt(1) + input.charAt(2);
		}
		else if(input.length() == 4 
				   && input.charAt(0) >= '0' && input.charAt(0) <= '9'
				   && input.charAt(1) >= '0' && input.charAt(1) <= '9'
				   && input.charAt(2) >= '0' && input.charAt(2) <= '9'
				   && input.charAt(3) >= '0' && input.charAt(3) <= '9') {
					
			input = "" + input.charAt(0) + input.charAt(1) + ":" 
				      + input.charAt(2) + input.charAt(3);
		}
		else {
			input = input.replace('：', ':');
			input = input.replace('；', ':');
			input = input.replace('，', ':');
			input = input.replace('。', ':');
			input = input.replace(';', ':');
			input = input.replace(',', ':');
			input = input.replace('.', ':');
		}

		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		
		Date date = null;
		try {
			date = df.parse(input);
		} catch (ParseException e) {
		}
		
		//解析不成功则返回原来的时间，解析成功则返回标准格式的时间
		if(date == null)
			return oldTime;
		else
			return df.format(date);
	}
	
	public static String[] formatName(String input) {
		input = input.trim();

		input = input.replace('\\', '/');

		input = input.replace('、', '/');
		
		input = input.replace('，', '/');
		input = input.replace(',', '/');
		
		input = input.replace('。', '/');			
		input = input.replace('.', '/');

		String[] names = input.split("/");
		if(names.length > 4)
			return null;
		
		String[] myNames = new String[names.length];
		myNames[0] = names[0].toUpperCase();
		for(int i=1; i<names.length; i++) {
			if(names[i].length() <=2 && myNames[0].length() > names[i].length()) {
				myNames[i] = (myNames[0].substring(0, myNames[0].length() - names[i].length()) + names[i]).toUpperCase();
			}
			else {
				myNames[i] = names[i].toUpperCase();
			}
		}
		
		return myNames;
	}

	//工具方法
	public static int trainTimeToInt(String strTime) {
		String strH = strTime.split(":")[0];
		String strM = strTime.split(":")[1];
		
		int h = Integer.parseInt(strH);
		int m = Integer.parseInt(strM);
		
		return h*60 + m;
	}

	public static String intToTrainTime(int minutes) {
		int hours = minutes < 0 ? -1 : minutes / 60;

		int clockMinute = minutes - hours * 60;
		if (clockMinute < 0)
			clockMinute += 60;

		String strMinute = clockMinute < 10 ? "0" + clockMinute : "" + clockMinute;

		int clockHour = hours;
		if (clockHour < 0)
			clockHour += 24;
		if (clockHour >= 24)
			clockHour -= 24;

		String strHour = clockHour < 10 ? "0" + clockHour : "" + clockHour;

		return strHour + ":" + strMinute;
	}

	public boolean hasStop(String staName) {
		for (int i = 0; i < getStopNum(); i++) {
			if (stops.get(i).stationName.equalsIgnoreCase(staName))
				return true;
		}
		
		return false;
	}
	
	public int findStopIndex(String staName) {
		for (int i = 0; i < getStopNum(); i++) {
			if (stops.get(i).stationName.equalsIgnoreCase(staName))
				return i;
		}
		
		return -1;
	}

	public Stop findStop(String staName) {
		for (int i = 0; i < getStopNum(); i++) {
			if (stops.get(i).stationName.equalsIgnoreCase(staName))
				return stops.get(i);
		}
		
		return null;
	}

	public void setTrainNames(String[] myNames) {
		trainNameFull = makeFullName(myNames);
		for(int i=0; i<myNames.length; i++) {
			if(isDownName(myNames[i]))
				trainNameDown = myNames[i];
			else if(isUpName(myNames[i]))
				trainNameUp = myNames[i];
		}
	}

	public static int getDefaultVelocityByName(String trainName) {
		char type = trainName.toUpperCase().charAt(0);
		switch(type) {
		case 'G':
			return 250;
		case 'D':
		case 'Z':
		case 'C':
			return 120;
		case 'T':
			return 100;
		case 'K':
		case 'N':
			return 85;
		case 'L':
		case 'A':
			return 60;
		default:
			return 70;
		}
	}

	
	public void addTrainChangedListener(Consumer<Train> eventHandler) {
		trainChangedListeners.add(eventHandler);
	}
	
	public void removeTrainChangedListener(Consumer<Train> eventHandler) {
		trainChangedListeners.remove(eventHandler);
	}
	
	public void fireTrainChangedEvent() {
		trainChangedListeners.stream().parallel()
			.forEach(action->action.accept(this));
	}
	
	
	
	
	
	
	
	
	
	
	
	

	
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_TRAIN; }
	@Override
	protected String getEndSectionString() { return END_SECTION_TRAIN; }

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;
	@Override
	protected Tuple<String, Class<?>>[] getSimpleTGPProperties() {
		if (propTuples == null) {
			propTuples = new Tuple[6];
			
			propTuples[0] = Tuple.of("trainNameFull", String.class);
			propTuples[1] = Tuple.of("trainNameDown", String.class);
			propTuples[2] = Tuple.of("trainNameUp", String.class);
			propTuples[3] = Tuple.of("startStation", String.class);
			propTuples[4] = Tuple.of("terminalStation", String.class);
			propTuples[5] = Tuple.of("color", Color.class);
		}
		
		return propTuples;
	}

	@Override
	protected void setTGPProperty(String propName, String valueInStr) {
		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
		
		if (propTuples[0].A.equals(propName)) {
			trainNameFull = valueInStr;
		} else if (propTuples[1].A.equals(propName)) {
			trainNameDown = valueInStr;
		} else if (propTuples[2].A.equals(propName)) {
			trainNameUp = valueInStr;
		} else if (propTuples[3].A.equals(propName)) {
			setStartStation(valueInStr);
		} else if (propTuples[4].A.equals(propName)) {
			setTerminalStation(valueInStr);
		} else if (propTuples[5].A.equals(propName)) {
			setColorByStr(valueInStr);
		}
	
	}

	@Override
	protected String getTGPPropertyReprStr(int index) {
		String value = "";
		
		if (index == 0) {
			value = trainNameFull + "";	
		} else if (index == 1) {
			value = trainNameDown + "";
		} else if (index == 2) {
			value = trainNameUp + "";
		} else if (index == 3) {
			value = getStartStation() + "";
		} else if (index == 4) {
			value = getTerminalStation() + "";
		} else if (index == 5) {
			value = getColorStr() + "";
		}
		
		return value;
	}

	/* Element array */
	@Override
	protected Vector<Stop> getTGPElements() {
		return stops;
	}

	@Override
	protected void addTGPElement(Stop element) {
		stops.add(element);
	}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {
		return part != null && part instanceof Stop;
	}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {
		
	};
}
