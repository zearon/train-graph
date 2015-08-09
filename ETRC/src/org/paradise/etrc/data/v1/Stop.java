package org.paradise.etrc.data.v1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.data.annotation.TGProperty;

import static org.junit.Assert.assertArrayEquals;

import static org.paradise.etrc.ETRC.__;

@TGElementType(name="Stop", printInOneLine=true)
public class Stop extends TrainGraphPart {
	
	public static final int				NOT_GO_THROUGH				= 0;
	public static final int				PASS									= 1;
	public static final int				STOP_START_STATION		= 2;
	public static final int				STOP_TERMINAL_STATION	= 3;
	public static final int				STOP_PASSENGER				= 4;
	public static final int				STOP_NON_PASSENGER		= 5;
	public static final String[]	STOP_STATUS_DESC			= {
		__("Not go through"), __("Pass"), 
		__("Stop at start station"),
		__("Stop at terminal station"),
		__("Stop for business"),
		__("Stop without business"),
		};
	
	public static int DEFAULT_STOP_TIME = 3;
	
	public int getDEFAULT_STOP_TIME() {
		return DEFAULT_STOP_TIME;
	}
	public void setDEFAULT_STOP_TIME(int defaultStopTime) {
		DEFAULT_STOP_TIME = defaultStopTime;
	}

	//	@TGProperty
	private String trainName;
	public String getTrainName() { return trainName; }
	public void setTrainName(String trainName) { this.trainName = trainName; }
	
	@TGProperty
	public Integer stopStatus = NOT_GO_THROUGH;
	//20070224新增，是否图定
	public boolean isPassenger() { 
		return stopStatus == STOP_PASSENGER || stopStatus == STOP_START_STATION 
				|| stopStatus == STOP_TERMINAL_STATION; 
	}
	public void setPassenger(boolean isPassenger) {
		stopStatus = isPassenger ? STOP_PASSENGER : STOP_NON_PASSENGER;
	}
	
	private int stopMinutes;
	public Integer getStopTime() { 
		return leaveTime >= 0 && arriveTime >= 0 ? 
				(stopMinutes = getDuration(arriveTime, leaveTime)) : 0; 
	}
	public void setStopTIme(Integer stopMinutes) {
		this.stopMinutes = stopMinutes;
//		leaveTime = addTime(arriveTime, stopMinutes);
		leaveTimeChanged = true;
	}

	private int arriveTime = -1;
	public Integer getArriveTime() { return arriveTime;}
	public void setArriveTime(Integer arriveTime) {
		this.arriveTime = arriveTime;
//		stopMinutes = getDuration(arriveTime, leaveTime);
		arriveTimeChanged = true;
	}

	private String arrive = null;
	private boolean arriveTimeChanged = true;
	@TGProperty
	public String getArrive() {
		if	(arriveTimeChanged) {
			arrive = makeTimeStr(arriveTime);
			arriveTimeChanged = false;
		}
			
		return arrive;
	}
	@TGProperty
	public void setArrive(String arrive) {
		arriveTime = makeTimeFromStr(arrive, true);
//		stopMinutes = getDuration(arriveTime, leaveTime);
		this.arrive = arrive;
	}
	public String getArriveTimeStr() {
		switch (stopStatus) {
		case NOT_GO_THROUGH:
			return "";
		case PASS:
			if (arriveTime < 0)
				return "↓";
			else
				return makeTimeStr(arriveTime);
		default:
			if (arriveTime < 0)
				return "◯";
			else
			return makeTimeStr(arriveTime);
		}
	}
	
	private int leaveTime = -1;
	private boolean leaveTimeChanged = true;
	public Integer getLeaveTime() { return leaveTime;}
	public void setLeaveTime(Integer leaveTime) {
		this.leaveTime = leaveTime;
//		stopMinutes = getDuration(arriveTime, leaveTime);
		leaveTimeChanged = true;
	}
	public String getLeaveTimeStr() {
		switch (stopStatus) {
		case NOT_GO_THROUGH:
			return "";
		case PASS:
			if (leaveTime < 0)
				return "↓︎";
			else
				return makeTimeStr(leaveTime);
		default:
			if (leaveTime < 0)
				return "◯";
			else
				return makeTimeStr(leaveTime);
		}
	}
	
	private String leave;
	@TGProperty
	public String getLeave() {
		if	(leaveTimeChanged) {
			leave = makeTimeStr(leaveTime);
			leaveTimeChanged = false;
		}

		return leave;
	}
	@TGProperty
	public void setLeave(String leave) {
		leaveTime = makeTimeFromStr(leave, true);
//		stopMinutes = getDuration(arriveTime, leaveTime);
		this.leave = leave;
	}
	
	public RailroadLine linkedRailLine = null;
	
	// Make constructor public to enable Unit Test
	public Stop() {}

	Stop(String _name) {
		this();
		setName(_name);
	}
	
	public Stop setProperties(String trainName, String arrive, 
			String leave, boolean schedular) {
		setTrainName(trainName);
		this.setArrive(arrive);
		this.setLeave(leave);
		this.stopStatus = STOP_PASSENGER;
		this.setPassenger(schedular);
		
		return this;
	}	
	
	public Stop setProperties(String trainName, int stopStatus, int arriveTime, int leaveTime) {
		setTrainName(trainName);
		setArriveTime(arriveTime);
		setLeaveTime(leaveTime);
		this.stopStatus = stopStatus;
		
		arriveTimeChanged 	= true;
		leaveTimeChanged 	= true;
		
		return this;
	}

	public Stop copy() {
		Stop st = TrainGraphFactory.createInstance(Stop.class, this.getName())
			.setProperties(this.trainName, stopStatus, arriveTime, leaveTime);
		st.parent = parent;
		st.root = root;
		
		return st;
	}

	public void copyTo(Stop stop2) {
		stop2.setProperties(trainName, stopStatus, arriveTime, leaveTime);
	}
	
	public static Stop createStopFromStation(Station station, String trainName) {
		Stop st = TrainGraphFactory.createInstance(Stop.class, station.getName())
			.setProperties(trainName, NOT_GO_THROUGH, -1, -1);
		
		return st;
	}
	
	public void applyStopTimeOffset(int offset) {
		arriveTime = addTime(arriveTime, offset);
		leaveTime = addTime(leaveTime, offset);
		leaveTimeChanged = true;
	}

	public int hashCode() {
		int stationCode = name == null ? 0 : name.hashCode();
		int trainCode = trainName == null ? 0 : trainName.hashCode();
		
		return stationCode << 16 + trainCode;
	}
	
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof Stop))
			return false;
		
		Stop s2 = (Stop) obj;
		boolean stationEqual = ( name == null && s2.name == null) || 
				( name != null && name.equals(s2.name));
		boolean trainEqual = ( trainName == null && s2.trainName == null) || 
				( trainName != null && trainName.equals(s2.trainName));

		return stationEqual && trainEqual;
	}
	
	public static Stop makeStop(String stopName, String trainName, 
			String strArrive, String strLeave, boolean isSchedular) {
		
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		Date theArrive = null;
		Date theLeave = null;
		String myArrive = "";
		String myLeave = "";
		
		try {
			theArrive = df.parse(strArrive);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		try {
			theLeave = df.parse(strLeave);
		} catch (ParseException e) {
			//e.printStackTrace();
		}

		//如果到点解析不成功就把到点设为发点
		if(theArrive == null)
			myArrive = df.format(theLeave);
		else
			myArrive = df.format(theArrive);
		
		//如果发点解析不成功就把发点设为到点
		if(theLeave == null)
			myLeave = df.format(theArrive);
		else
			myLeave = df.format(theLeave);
		
		return new Stop(stopName).setProperties(trainName, myArrive, myLeave, isSchedular);
	}
	
	// {{ Time utility functions
	
//	public String get
	
	public static String makeTimeStr(int time) {
		int minute = time % 100;
		int hour = (time - minute) / 100;
		if (minute < 10)
			return "" + hour + ":0" + minute;
		else
			return "" + hour + ":" + minute;
	}
	
	private static Pattern timePattern = Pattern.compile("(\\d{1,2})\\s*:?\\s*(\\d{2})");
	/**
	 * Make a time integer from a time string. For example, make 22:30 to 2230,
	 * 0:21 to 21, etc.
	 * @param timeStr
	 * @param throwException If true, then an IllegualArgumentException is thrown
	 * when time string is invalid. Otherwise, -1 is returned.
	 * @return The parsed time integer or -1 if error occurred and throwException
	 * is set to false.
	 */
	public static int makeTimeFromStr(String timeStr, boolean throwException) {
		if ("0:0-1".equals(timeStr))
			return -1;
		
		String hourStr = null, minuteStr = null;
		int hour, minute, time = -1;
		boolean error = false, strParsed = false;
		Matcher matcher = timePattern.matcher(timeStr);
		if (matcher.matches()) {
			hourStr = matcher.group(1);
			minuteStr = matcher.group(2);
			
			if (hourStr == null || minuteStr == null)
				error = true;
			else
				strParsed = true;
		} else {
			error = true;
		}
		
		if (strParsed) {
			try {
				hour = Integer.parseInt(hourStr);
				minute = Integer.parseInt(minuteStr);
				if (hour < 0 || hour > 23 || minute < 0 || minute > 60)
					error = true;
				else
					time = hour * 100 + minute;
			} catch (Exception e) { error = true; }
		}
		
		if (error && throwException)
			throw new IllegalArgumentException(String.format(__("Invalide time string %s. It should a string between "
					+ "000 to 2359 or 0:00 to 23:59"), timeStr));
		else
			return time;
	}
	
	public static int addTime(int time, int deltaInMinutes) {
		if (time < 0)
			return time;

//		DEBUG_MSG("-------test addTime(%d, %d)", time, deltaInMinutes);
//		DEBUG_MSG("hour:minute is %d:%d", hour, minute);
//		DEBUG_MSG("Step 1 changed hour:minute is %d:%d", hour, minute);
		int minute = time % 100;
		int hour = (time - minute) / 100;
		
		minute += deltaInMinutes;
		
		if (minute >= 60) {
			hour += minute / 60;
			minute %= 60;
		} else if (minute < 0) {
			hour += minute / 60;
			minute %= 60;
			if (minute != 0) {
				-- hour;
				minute += 60;
			}
		}
		
		hour %= 24;
		if (hour < 0)
			hour += 24;
		
		return hour * 100 + minute;
	}
	
	/**
	 * Get time duration of (time2 - time1) in minutes
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getDuration(int time1, int time2) {
		int minute1 = time1 % 100;
		int hour1 = (time1 - minute1) / 100;
		int minute2 = time2 % 100;
		int hour2 = (time2 - minute2) / 100;
		
		int time1InMinutes = hour1 * 24 + minute1;
		int time2InMinutes = hour2 * 24 + minute2;
		return time2InMinutes - time1InMinutes;
	}
	
	// }}
	
	// {{ Unit Test code
	@Test
	public void testMakeStimeFromStr() {
		String[] testCases = {"000", "0:00", "605", "6:05", "752", "7:52",
				"1131", "11:31", "2315", "23:15",
				"adf", "-135", "2407", "399", "sdfdsf"};
		
		int[] expecteds	= {0, 0, 605, 605, 752, 752,
				1131, 1131, 2315, 2315,
				-1, -1, -1, -1, -1};
		int[] actuals 	= Arrays.stream(testCases)
				.mapToInt(testCase -> makeTimeFromStr(testCase, false))
				.toArray();
		
		assertArrayEquals("msg", expecteds, actuals);
	}
	
	@Test
	public void testAddTime() {
		int[][] testCases = {{1,2,3}, {1202,3,1205}, {1252,10,1302}, 
				{2202,120,2}, {2202,150,32}, {2202,60*48+30,2232}, 
				{002,-3,2359}, {002,-120,2202}, {002,-150,2132}, 
				{002,-60*24,2}, {002,-60*24-20,2342}};
		
		int[] expecteds	= Arrays.stream(testCases)
				.mapToInt(testCase -> testCase[2])
				.toArray();
		int[] actuals 	= Arrays.stream(testCases)
				.mapToInt(testCase -> addTime(testCase[0], testCase[1]))
				.toArray();
		
		assertArrayEquals("msg", expecteds, actuals);
	}
	
	// }}
}
