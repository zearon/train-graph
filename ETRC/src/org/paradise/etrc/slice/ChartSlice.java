package org.paradise.etrc.slice;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.paradise.etrc.data.RailroadLineChart;
import org.paradise.etrc.data.Station;
import org.paradise.etrc.data.Train;
import org.paradise.etrc.dialog.SliceDialog;

public class ChartSlice {
	public Hashtable<Train, TrainSlice> trainSlices = new Hashtable<Train, TrainSlice>();
	RailroadLineChart chart;
	
	public ChartSlice(RailroadLineChart _chart) {
		chart = _chart;
		buildUp();
	}

	//做车次切片
	public void makeTrainSlice(Train train) {
		//获取事件
		Vector<ChartEvent> events = new Vector<ChartEvent>();
		events.addAll(getStationEventsOfTrain(train));
		events.addAll(getTrainEventsOfTrainSameDir(train));
		events.addAll(getTrainEventsOfTrainDiffDir(train));
				
		//展现
		System.out.println("\n" + train.getTrainName() + "次列车 " + train.getStartStation() + "至" + train.getTerminalStation() + 
				           " 在" + chart.railroadLine.name + "上(" + train.getTrainName(chart.railroadLine)+ ")的运行图切片");
		System.out.println(events);
		new SliceDialog("\n" + train.getTrainName() + "次列车 " + train.getStartStation() + "至" + train.getTerminalStation() + 
		           " 在" + chart.railroadLine.name + "上(" + train.getTrainName(chart.railroadLine)+ ")的运行图切片", 
		           events.toString()).showMessage();
	}

	//做车站切片
	public void makeStationSlice(Station station) {
		//获取事件
		Vector<ChartEvent> events = getStationEventsOfStation(station);
		
		//展现
		System.out.println(chart.railroadLine.name + " " + station.name + "站 的运行图切片");
		System.out.println(events);
		new SliceDialog(chart.railroadLine.name + " " + station.name + "站 的运行图切片", events.toString()).showMessage();
	}
	
	//初始化
	private void buildUp() {
		for(int i=0; i<chart.getTrainNum(); i++) {
			trainSlices.put(chart.getTrain(i), new TrainSlice(chart.getTrain(i), chart.railroadLine));
		}
	}
	
	//获取车次切片的同向事件：让、越
	public Vector<ChartEvent> getTrainEventsOfTrainSameDir(Train train) {
		Vector<ChartEvent> events = new Vector<ChartEvent>();
		TrainSlice trainSlice = (TrainSlice) trainSlices.get(train);
		
		Enumeration<Train> en = trainSlices.keys();
		while(en.hasMoreElements()) {
			TrainSlice anotherTrain = (TrainSlice) trainSlices.get(en.nextElement());
			events.addAll(trainSlice.getTrainEventsOfSameDir(anotherTrain));
		}
		
		if(train.isDownTrain(chart.railroadLine) == Train.DOWN_TRAIN)
			return ChartEvent.sortByDistAsc(events);
		else
			return ChartEvent.sortByDistDesc(events);
	}
	
	//获取车次切片的反向事件：会
	public Vector<ChartEvent> getTrainEventsOfTrainDiffDir(Train train) {
		Vector<ChartEvent> events = new Vector<ChartEvent>();
		TrainSlice trainSlice = (TrainSlice) trainSlices.get(train);
		
		Enumeration<Train> en = trainSlices.keys();
		while(en.hasMoreElements()) {
			TrainSlice anotherTrain = (TrainSlice) trainSlices.get(en.nextElement());
			events.addAll(trainSlice.getTrainEventsOfDiffDir(anotherTrain));
		}
		
		if(train.isDownTrain(chart.railroadLine) == Train.DOWN_TRAIN)
			return ChartEvent.sortByDistAsc(events);
		else
			return ChartEvent.sortByDistDesc(events);
	}
	
	//获取车次切片的车站事件：通、到、发
	public Vector<ChartEvent> getStationEventsOfTrain(Train train) {
		TrainSlice trainSlice = (TrainSlice) trainSlices.get(train);
		Vector<ChartEvent> events = (trainSlice == null) ? new Vector<ChartEvent>() : trainSlice.getStationEvents();
		
		if(train.isDownTrain(chart.railroadLine) == Train.DOWN_TRAIN)
			return ChartEvent.sortByDistAsc(events);
		else
			return ChartEvent.sortByDistDesc(events);
	}
	
	//获取车站切片事件
	private Vector<ChartEvent> getStationEventsOfStation(Station station) {
		Vector<ChartEvent> events = new Vector<ChartEvent>();
		
		Enumeration<Train> en = trainSlices.keys();
		while(en.hasMoreElements()) {
			TrainSlice trainSlice = (TrainSlice) trainSlices.get(en.nextElement());
			events.addAll(trainSlice.getStationEventsAt(station));
		}
		
		return ChartEvent.sortByTime(events);
	}
}
