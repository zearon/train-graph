package org.paradise.etrc.data;

import static org.paradise.etrc.ETRC.__;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.BOMStripperInputStream;
import org.paradise.etrc.data.util.Tuple;

public class RailroadLineChart extends TrainGraphPart<RailroadLineChart, TrainRef> {
	//Y轴（距离）显示参数
	public float distScale = 3; //每公里像素数
	public static final float MAX_DIST_SCALE = 10f;
	public int displayLevel = 4; //最低可显示车站等级
	public int boldLevel = 2; //最低粗线显示车站等级（特等为0）

	//X轴（时间）显示参数
	public int startHour = 18; //0坐标时刻（0-23）
	public int minuteScale = 2; //每分钟像素数
	public static final int  MAX_MINUTE_SCALE = 10;
	public int timeInterval = 10; //时间轴间隔（必须是60的约数，即可以是5，6，10等，但不能是7分钟）

	//本运行图的线路
	public RailroadLine railroadLine;
	// 用于序列化时引用RailNetwork中的线路
	String railroadLineName = "";
	public Vector<RailroadLine> allCircuits = new Vector<RailroadLine>(6);

	//本运行图所包含的车次，最多600趟
//	public static final int MAX_TRAIN_NUM = 6000;
//	private Train _trains[] = new Train[MAX_TRAIN_NUM];
	public Vector<Train> trains = new Vector<Train> (100);
	
	//由于车次跨多条线路,因此序列化线路运行图时只保留途径该线路的车次的引用
	Vector<TrainRef> trainRefs = new Vector<TrainRef> ();

	//本运行图的车次总数
//	private int trainNum = 0;
	//下行车次数目
	public int dNum = 0;
	//上行车次数目
	public int uNum = 0;
	

	private List<Consumer<RailroadLineChart>> chartChangedListeners = new Vector<Consumer<RailroadLineChart>> ();

	public RailroadLineChart() {}
	
	public RailroadLineChart(File f) throws IOException {
		loadFromFile2(f);
	}
	
	public RailroadLineChart(RailroadLine cir) {
		railroadLine = cir;
	}
	
	public Train getTrain(int index) {
		return trains.get(index);
	}

	public int getTrainNum() {
		return trains.size();
	}

	public void addTrain(Train loadingTrain) {
//		if (getTrainNum() >= MAX_TRAIN_NUM)
//			return;

		if (isLoaded(loadingTrain)) {
			updateTrain(loadingTrain);
			return;
		}

		if (loadingTrain.color == null)
			loadingTrain.color = loadingTrain.getDefaultColor();
//			loadingTrain.color = getNextTrainColor();

//		this._trains[getTrainNum()] = loadingTrain;
		trains.add(loadingTrain);

		switch (loadingTrain.isDownTrain(railroadLine)) {
		case Train.DOWN_TRAIN:
			dNum++;
			break;
		case Train.UP_TRAIN:
			uNum++;
			break;
		}
//		setTrainNum(getTrainNum() + 1);

//	    String direction="";
//	    switch(loadingTrain.isDownTrain(circuit)){
//	      case Train.DOWN_TRAIN:
//	        direction = "下行";
//	        break;
//	      case Train.UP_TRAIN:
//	        direction = "上行";
//	        break;
//	      default:
//	        direction = "未知";
//	    }
//	    System.out.println(loadingTrain.getTrainName(circuit) + "次" + direction);
		
		loadingTrain.addTrainChangedListener(this::onTrainChanged);
		fireChartChangedEvent();
	}
	
	public Train findTrain(String trainName) {
		for(int i=0; i<getTrainNum(); i++) {
			if(trains.get(i).getTrainName(railroadLine).equals(trainName))
				return trains.get(i);
		}
		
		return null;
	}
	
	public boolean containTrain(Train train) {
//		for(int i=0; i<getTrainNum(); i++) {
//			if(_trains[i].equals(train))
//				return true;
//		}
//		
//		return false;
		
		return trains.contains(train);
	}

	public void updateTrain(Train newTrain) {
		for (int i = 0; i < trains.size(); i++) {
			if (newTrain.equals(trains.get(i))) {
				if(newTrain.color == null)
					newTrain.color = trains.get(i).color;
				trains.set(i, newTrain);
			}
		}
	}
	
	public void delTrain(int index) {
		if ((index < 0) || index >= getTrainNum() /* (index >= MAX_TRAIN_NUM) */ )
			return;
		
		trains.remove(index).removeTrainChangedListener(this::onTrainChanged);

		fireChartChangedEvent();
	}
	
	public void delTrain(Train theTrain) {
		if(!isLoaded(theTrain))
			return;
		
		trains.remove(theTrain);
		
		theTrain.removeTrainChangedListener(this::onTrainChanged);
		fireChartChangedEvent();
	}

	//画运行线的颜色
//	private static int trainColorIndex = 0;
//
//	public static Color trainColors[] = { Color.red, Color.blue, Color.cyan,
//			Color.green, Color.magenta, Color.orange, new Color(172, 0, 172) /*Color.gray, Color.pink*/
//	};
//
//	private Color getNextTrainColor() {
//		trainColorIndex++;
//		if (trainColorIndex >= trainColors.length) {
//			trainColorIndex = 0;
//		}
//		return trainColors[trainColorIndex];
//	}
//
	/**
	 * 判断指定的车次是否已经在本运行图中
	 * @param _t
	 * @return
	 */
	public boolean isLoaded(Train _t) {
//		for (int i = 0; i < trainNum; i++) {
//			if (_t.getTrainName(circuit).equalsIgnoreCase(
//					trains[i].getTrainName(circuit)))
//				return true;
//		}
		for (int i = 0; i < getTrainNum(); i++) {
			if (_t.getTrainName().equalsIgnoreCase(trains.get(i).getTrainName()))
				return true;
		}

		return false;
	}

	/*
	 * 文件操作
	 */
	public static final String circuitPattern = "***Circuit***";
	public static final String trainPattern = "===Train===";
	public static final String colorPattern = "---Color---";
	public static final String setupPattern = "...Setup...";

	public void saveToFile(File f) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));

		//线路
		for (int i = 0; i < allCircuits.size(); ++ i) {
			out.write(circuitPattern);
			out.newLine();
			allCircuits.get(i).writeTo(out);
		}
		
		//车次
		for (int i = 0; i < getTrainNum(); i++) {
			out.write(trainPattern);
			out.newLine();
			trains.get(i).writeTo(out);
		}
		//颜色
		out.write(colorPattern);
		out.newLine();
		for (int i = 0; i < getTrainNum(); i++) {
			out.write(trains.get(i).getTrainName());
			out.write("," + trains.get(i).color.getRed());
			out.write("," + trains.get(i).color.getGreen());
			out.write("," + trains.get(i).color.getBlue());
			out.newLine();
		}
		//设置
		out.write(setupPattern);
		out.newLine();
		out.write(distScale + "," +
				  displayLevel + "," +
				  boldLevel + "," +
				  startHour + "," +
				  minuteScale + "," +
				  timeInterval);
		out.newLine();

		out.flush();
		out.close();
	}

	public void loadFromFile2(File f) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new BOMStripperInputStream(new FileInputStream(f)),"UTF-8"));

		//读取文件状态
		final int READING_CIRCUIT = 1;
		final int READING_TRAIN = 2;
		final int READING_COLOR = 3;
		final int READING_SETUP = 4;

		int reading_state = 0;
		int lineNum = 0;

		String line = in.readLine();
		if (line == null) {
			in.close();
			throw new IOException("运行图文件格式错");
		}

//		Circuit readingCircuit = new Circuit();
//		Train readingTrains[] = new Train[MAX_TRAIN_NUM];
		Vector<RailroadLine> readingCircuits = new Vector<RailroadLine>(7);
		Vector<Train> readingTrains = new Vector<Train>(20);
//		int readTrainNum = 0;

		while (line != null) {
			if (line.equalsIgnoreCase(circuitPattern)) {
				reading_state = READING_CIRCUIT;
				readingCircuits.add(new RailroadLine());
				lineNum = 0;
			} else if (line.equalsIgnoreCase(trainPattern)) {
				reading_state = READING_TRAIN;
//				readingTrains[readTrainNum] = new Train();
				readingTrains.add(new Train());
				lineNum = 0;
//				readTrainNum++;
			} else if (line.equalsIgnoreCase(colorPattern)) {
				reading_state = READING_COLOR;
				lineNum = 0;
			} else if (line.equalsIgnoreCase(setupPattern)) {
				reading_state = READING_SETUP;
				lineNum = 0;
			} else {
				switch (reading_state) {
				case READING_CIRCUIT:
					readingCircuits.get(readingCircuits.size() - 1).parseLine(line, lineNum);
					lineNum++;
					break;
				case READING_TRAIN:
//					readingTrains[readTrainNum - 1].parseLine(line, lineNum);
					readingTrains.get(readingTrains.size() - 1).parseLine(line, lineNum);
					lineNum++;
					break;
				case READING_COLOR:
//					parseColor(line, readingTrains, readTrainNum);
					parseColor(line, readingTrains, readingTrains.size());
					lineNum++;
					break;
				case READING_SETUP:
					parseSetup(line);
					lineNum++;
					break;
				default:
					lineNum++;
				}
			}
			line = in.readLine();
		}

		this.railroadLine=readingCircuits.get(0);
		this.allCircuits = readingCircuits;

		trains.clear();
		dNum = 0;
		uNum = 0;
		for (int i = 0; i < readingTrains.size() /*readTrainNum*/; i++) {
//			addTrain(readingTrains[i]);
			addTrain(readingTrains.get(i));
		}

		//System.out.println("下行："+dNum+"，上行："+uNum);
		in.close();
		
		fireChartChangedEvent();
	}

	private void parseSetup(String line) throws IOException {
		String setup[] = line.split(",");
		try {
			distScale = Float.parseFloat(setup[0]);
			displayLevel = Integer.parseInt(setup[1]);
			boldLevel = Integer.parseInt(setup[2]);
			startHour = Integer.parseInt(setup[3]);
			minuteScale = Integer.parseInt(setup[4]);
			timeInterval = Integer.parseInt(setup[5]);

		} catch (Exception e) {
			throw new IOException(__("Unable to read chart settings."));
		}
	}
	
	/**
	 * parseColor
	 *
	 * @param readingTrains Train[]
	 */
	private void parseColor(String line, Vector<Train> /*Train[]*/ readingTrains, int readTrainNum)
			throws IOException {
		String colorLine[] = line.split(",");

		if (colorLine.length < 4)
			throw new IOException(__("Error reading color."));

		int r = 255;
		int g = 255;
		int b = 255;
		try {
			r = Integer.parseInt(colorLine[1]);
			g = Integer.parseInt(colorLine[2]);
			b = Integer.parseInt(colorLine[3]);
		} catch (Exception e) {
			throw new IOException(String.format(__("Error reading color settings for the train %s."), colorLine[0]));
		}

		for (int i = 0; i < readTrainNum; i++) {
			if (readingTrains.get(i).getTrainName().equalsIgnoreCase(colorLine[0])) {
				readingTrains.get(i).color = new Color(r, g, b);
				//System.out.println(readingTrains[i].getTrainName()+":"+r+","+g+","+b);
			}
		}
	}
	
	  //测试用
	  public static void main(String argv[]) {
	    RailroadLineChart chart = null;
	    try {
	      chart = new RailroadLineChart(new File("d:\\huning2.trc"));
	    }
	    catch (IOException ex) {
	      System.out.println("Error:" + ex.getMessage());
	    }
	    
	    System.out.print(chart.railroadLine.toString());
	    for (int i = 0; i < chart.getTrainNum(); i++) {
			System.out.print("==== " + i + " ==== (");
			System.out.println(chart.trains.get(i).color.getRed() + "," + 
					           chart.trains.get(i).color.getGreen() + "," + 
					           chart.trains.get(i).color.getBlue() + ") ====");
			System.out.print(chart.trains.get(i).toString());
		}
	    System.out.println("\nSettings: " + 
	    		  chart.distScale + "," +
	    		  chart.displayLevel + "," +
	    		  chart.boldLevel + "," +
	    		  chart.startHour + "," +
	    		  chart.minuteScale + "," +
	    		  chart.timeInterval);
	    
	    chart.distScale ++;
	    
	    try {
			chart.saveToFile(new File("d:\\huning3.trc"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	  }

	public void clearTrains() {
		dNum = 0;
		uNum = 0;
		trains.forEach(train->train.removeTrainChangedListener(this::onTrainChanged));
		trains.clear();
		
		fireChartChangedEvent();
	}

	public void insertNewStopToTrain(Train theTrain, Stop stop) {
		if (theTrain.isDownTrain(railroadLine) == Train.DOWN_TRAIN) {
			insertNewStopToTrainDown(theTrain, stop);
		} else
			insertNewStopToTrainUp(theTrain, stop);
		

		theTrain.addTrainChangedListener(this::onTrainChanged);
		fireChartChangedEvent();
	}

	private void insertNewStopToTrainUp(Train theTrain, Stop stop) {
		int newDist = this.railroadLine.getStationDist(stop.stationName);
		
		//不在本线 返回 null
		if(newDist < 0)
			return;
		
		//新站在theTrain在本线的第一个停靠站之前 插在第一个站之前
		Station firstStop = this.railroadLine.getFirstStopOnMe(theTrain);
		if (firstStop == null) {
			theTrain.insertStop(stop, 0);
		}
		else {
			int firstDist = this.railroadLine.getStationDist(firstStop.name);
			if(newDist > firstDist)
				theTrain.insertStop(stop, theTrain.findStopIndex(firstStop.name));
		}
		//新站在theTrain在本线的最后一个停靠站之后 append在最后一个站之后
		Station lastStop = this.railroadLine.getLastStopOnMe(theTrain);
		if (lastStop == null) {
			theTrain.appendStop(stop);
		}
		else {
			int lastDist = this.railroadLine.getStationDist(lastStop.name);
			if(newDist < lastDist)
				theTrain.appendStop(stop);
		}
		//新站在theTrain的第一个停靠站和最后一个停靠站之间
		//遍历theTrain的所有停站
		for(int i=0; i<theTrain.getStopNum()-1; i++) {
			int dist1 = railroadLine.getStationDist(theTrain.getStop(i).stationName);
			int dist2 = railroadLine.getStationDist(theTrain.getStop(i+1).stationName);
			
			if(dist1 >= 0 && dist2 >=0)
				//如果新站距离在两个站之间，则应当插在第一个站之后（返回第一个站）
				if(dist1 > newDist  && newDist > dist2)
					theTrain.insertStopAfter(theTrain.getStop(i), stop);
		}
	}

	private void insertNewStopToTrainDown(Train theTrain, Stop stop) {
		int newDist = this.railroadLine.getStationDist(stop.stationName);
		
		//不在本线 返回 null
		if(newDist < 0)
			return;
		
		//新站在theTrain在本线的第一个停靠站之前 插在第一个站之前
		Station firstStop = this.railroadLine.getFirstStopOnMe(theTrain);
		if (firstStop == null) {
			theTrain.insertStop(stop, 0);
		}
		else
		{
			int firstDist = this.railroadLine.getStationDist(firstStop.name);
			if(newDist < firstDist)
				theTrain.insertStop(stop, theTrain.findStopIndex(firstStop.name));
		}
		//新站在theTrain在本线的最后一个停靠站之后 append在最后一个站之后
		Station lastStop = this.railroadLine.getLastStopOnMe(theTrain);
		if (lastStop == null) {
			theTrain.appendStop(stop);
		}
		else
		{
			int lastDist = this.railroadLine.getStationDist(lastStop.name);
			if(newDist > lastDist)
				theTrain.appendStop(stop);
		}
		//新站在theTrain的第一个停靠站和最后一个停靠站之间
		//遍历theTrain的所有停站
		for(int i=0; i<theTrain.getStopNum()-1; i++) {
			int dist1 = railroadLine.getStationDist(theTrain.getStop(i).stationName);
			int dist2 = railroadLine.getStationDist(theTrain.getStop(i+1).stationName);
			
			if(dist1 >= 0 && dist2 >=0)
				//如果新站距离在两个站之间，则应当插在第一个站之后（返回第一个站）
				if(dist1 < newDist  && newDist < dist2)
					theTrain.insertStopAfter(theTrain.getStop(i), stop);
		}
	}
	

	
	public void addChartChangedListener(Consumer<RailroadLineChart> eventHandler) {
		chartChangedListeners.add(eventHandler);
	}
	
	public void removeChartChangedListener(Consumer<RailroadLineChart> eventHandler) {
		chartChangedListeners.remove(eventHandler);
	}
	
	public void fireChartChangedEvent() {
		chartChangedListeners.stream().parallel()
			.forEach(action->action.accept(this));
	}
	
	protected void onTrainChanged(Train train) {
		fireChartChangedEvent();
	}
	
	protected void onCircuitChanged() {
		fireChartChangedEvent();
	}
	
	
	
	
	
	
	
	
	
	
	

	
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_RAILINE_CHART; }
	@Override
	protected String getEndSectionString() { return END_SECTION_RAILINE_CHART; }

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;
	@Override
	protected Tuple<String, Class<?>>[] getSimpleTGPProperties() {
		if (propTuples == null) {
			propTuples = new Tuple[7];
			
			propTuples[0] = Tuple.of("railroadLineName", String.class);
			propTuples[1] = Tuple.of("distScale", float.class);
			propTuples[2] = Tuple.of("displayLevel", int.class);
			propTuples[3] = Tuple.of("boldLevel", int.class);
			propTuples[4] = Tuple.of("startHour", int.class);
			propTuples[5] = Tuple.of("minuteScale", int.class);
			propTuples[6] = Tuple.of("timeInterval", int.class);
		}
		
		return propTuples;
	}

	@Override
	protected void setTGPProperty(String propName, String valueInStr) {
		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
		
		if (propTuples[0].A.equals(propName)) {
			railroadLineName = valueInStr;
		} else if (propTuples[1].A.equals(propName)) {
			distScale = Float.parseFloat(valueInStr);
		} else if (propTuples[2].A.equals(propName)) {
			displayLevel = Integer.parseInt(valueInStr);
		} else if (propTuples[3].A.equals(propName)) {
			boldLevel = Integer.parseInt(valueInStr);
		} else if (propTuples[4].A.equals(propName)) {
			startHour = Integer.parseInt(valueInStr);
		} else if (propTuples[5].A.equals(propName)) {
			minuteScale = Integer.parseInt(valueInStr);
		} else if (propTuples[6].A.equals(propName)) {
			timeInterval = Integer.parseInt(valueInStr);
		}
	}

	@Override
	protected String getTGPPropertyReprStr(int index) {
		String value = "";
		
		if (index == 0) {
			railroadLineName = railroadLine.name;
			value = railroadLineName;	
		} else if (index == 1) {
			value = distScale + "";
		} else if (index == 2) {
			value = displayLevel + "";
		} else if (index == 3) {
			value = boldLevel + "";
		} else if (index == 4) {
			value = startHour + "";
		} else if (index == 5) {
			value = minuteScale + "";
		} else if (index == 6) {
			value = timeInterval + "";
		}
		
		return value;
	}

	/* Element array */
	@Override
	protected Vector<TrainRef> getTGPElements() {
		trainRefs.clear();
		trains.stream().map(train->new TrainRef(train.trainNameFull))
			.forEachOrdered(trainRefs::add);
		return trainRefs;
	}

	@Override
	protected void addTGPElement(TrainRef element) {
		trainRefs.add(element);
	}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {
		return part != null && part instanceof TrainRef;
	}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {
	};
}