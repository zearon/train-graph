package org.paradise.etrc.view.chart.traindrawing;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.paradise.etrc.data.v1.ChartSettings;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.view.chart.ChartView;

/**
 * A model representing all the train data should be painted on a ChartView,
 *  mainly the scheduling information about every train in a train circuit.
 * @author Jeff Gong
 *
 */
public class DrawingModel {
	
	public boolean empty = true;
	protected RailroadLine currentCircuit;

	protected Vector<TrainDrawing> allTrainDrawings = new Vector<TrainDrawing>();
	protected Vector<TrainDrawing> upTrainDrawings = new Vector<TrainDrawing>();
	protected Vector<TrainDrawing> downTrainDrawings = new Vector<TrainDrawing> ();
	protected Vector<TrainDrawing> unknownTrainDrawings = new Vector<TrainDrawing>();
	
	protected HashMap<String, Integer> trainDrawingIndex = new HashMap<String, Integer>();
	protected Vector<String> upTrainNames = new Vector<String>();
	protected Vector<String> downTrainNames = new Vector<String>();

	protected TrainDrawing activeTrainDrawing = null;
	protected boolean isLastActiveTrainDrawingInUnderMode = false;
	protected Vector<TrainDrawing> normalDrawings = new Vector<TrainDrawing>();
	protected Vector<TrainDrawing> underDrawings  = new Vector<TrainDrawing>();

	private ChartSettings settings;

	public TrainDrawing getActiveTrainDrawing() {
		return activeTrainDrawing;
	}


	public Vector<TrainDrawing> getNormalDrawings() {
		return normalDrawings;
	}


	public Vector<TrainDrawing> getUnderDrawings() {
		return underDrawings;
	}

	public DrawingModel() {
	}
	

	public void buildTrainDrawings(ChartSettings settings, RailroadLineChart chart, 
			ChartView chartView) {
		this.settings = settings;
		isLastActiveTrainDrawingInUnderMode = chartView.showUpDownState == ChartView.SHOW_NONE;
		updateCurrentCircuit(chart, currentCircuit, settings, chartView);
		updateUpDownTrainOption(3);
	}
	
	/**
	 * 根据视图的当前线路更新上下行train列表
	 * @param chart
	 * @param currentCircuit
	 */
	public void updateCurrentCircuit(RailroadLineChart chart, RailroadLine currentCircuit, 
			ChartSettings settings, ChartView chartView) {
		// 更新当前线路以及Train列表
		this.currentCircuit = currentCircuit;
		
		// 根据新线路区分上下行train
		allTrainDrawings.clear();
		upTrainDrawings.clear();
		downTrainDrawings.clear();
		unknownTrainDrawings.clear();
		trainDrawingIndex.clear();
		upTrainNames.clear();
		downTrainNames.clear();
		
		for (int i = 0; i < chart.getTrainNum(); i++) {
			Train train = chart.getTrain(i);
			
			TrainDrawing trainDrawing = new TrainDrawing(chartView, settings, train, false, true); // active=false, under=true
			allTrainDrawings.add(trainDrawing);
			if (!"".equals(train.trainNameUp))
				trainDrawingIndex.put(trainDrawing.train.trainNameUp, i);
			if (!"".equals(train.trainNameDown))
				trainDrawingIndex.put(trainDrawing.train.trainNameDown, i);			
			
			int isDown = train.isDownTrain(currentCircuit);
			switch (isDown) {
			case Train.UNKNOWN:
				unknownTrainDrawings.add(trainDrawing);
				break;
			case Train.UP_TRAIN:
				upTrainDrawings.add(trainDrawing);
				upTrainNames.add(train.getName());
				upTrainNames.add(train.trainNameUp);
				break;
			case Train.DOWN_TRAIN:
				downTrainDrawings.add(trainDrawing);
				downTrainNames.add(train.getName());
				downTrainNames.add(train.trainNameDown);
				break;
			default:
				break;
			}
		}
	}
	
	private int compareTrainDrawingByVisibility(TrainDrawing t1, TrainDrawing t2) {
		int visibility = Boolean.compare(t1.train.trainType.visible, t2.train.trainType.visible);
		int displayOrder = t1.train.trainType.displayOrder - t2.train.trainType.displayOrder;
		return visibility != 0 ? visibility : displayOrder;
	}
	
	public void setActiveTrain(Train activeTrain, int showUpDownState) {
		if (activeTrainDrawing != null) {
			activeTrainDrawing.setActive(false);
			switch (showUpDownState) {
			case ChartView.SHOW_NONE:
				activeTrainDrawing.setUnderDrawing(true);
				break;
			case ChartView.SHOW_UP:
				activeTrainDrawing.setUnderDrawing(!upTrainNames.contains(activeTrainDrawing.train.getName()));
				break;
			case ChartView.SHOW_DOWN:
				activeTrainDrawing.setUnderDrawing(!downTrainNames.contains(activeTrainDrawing.train.getName()));
				break;
			case ChartView.SHOW_ALL:
				activeTrainDrawing.setUnderDrawing(false);
				break;
			}
		}
		
		if (activeTrain == null) {
			activeTrainDrawing = null;
		} else {
			activeTrainDrawing = findTrainDrawing(activeTrain);
			activeTrainDrawing.setActive(true);
			activeTrainDrawing.setUnderDrawing(false);
		}
	}

	/**
	 * 
	 * @param showUpDownState 显示 上行/下行/全部/无 列车
	 * 	SHOW_NONE = 0; //0000
	 * SHOW_DOWN = 1; //0001
	 * SHOW_UP = 2; 		//0010
	 * SHOW_ALL = 3; 		//0011 = SHOW_UP | SHOW_DOWN
	 */
	public void updateUpDownTrainOption(int showUpDownState) {

		//清空正常、水印显示车次列表
		normalDrawings.clear();
		underDrawings.clear();
		
		// 根据显示选项把上行/下行/未知列车分入底层和正常层drawing列表
		switch(showUpDownState) {
		case ChartView.SHOW_NONE:
			underDrawings.addAll(allTrainDrawings);
			break;
		case ChartView.SHOW_DOWN:
			normalDrawings.addAll(downTrainDrawings);
			underDrawings.addAll(unknownTrainDrawings);
			underDrawings.addAll(upTrainDrawings);
			break;
		case ChartView.SHOW_UP:
			normalDrawings.addAll(upTrainDrawings);
			underDrawings.addAll(unknownTrainDrawings);
			underDrawings.addAll(downTrainDrawings);
			break;
		case ChartView.SHOW_ALL:
			normalDrawings.addAll(allTrainDrawings);
			break;
		}
		
		// 更新train drawing对象的underDrawing属性
		for (TrainDrawing trainDrawing : normalDrawings) {
			trainDrawing.setUnderDrawing(false);
		}
		for (TrainDrawing trainDrawing : underDrawings) {
			trainDrawing.setUnderDrawing(true);
		}
		
		updateTrainTypeDisplayOrder();
	}
	
	public void updateTrainTypeDisplayOrder() {
		// let visible trains drawing are above invisible ones.
		normalDrawings.sort(this::compareTrainDrawingByVisibility);
		underDrawings.sort(this::compareTrainDrawingByVisibility);
	}
	
	public void updateScale() {
		for (TrainDrawing trainDrawing : allTrainDrawings) {
			trainDrawing.rebuild();
		}
	}


	/**
	 * 根据Train寻找对应的Train Drawing. 如果找不到,返回空.
	 * @param train
	 * @return
	 */
	public TrainDrawing findTrainDrawing(Train train) {
		if (train == null)
			return null;
		
		for (TrainDrawing trainDrawing : allTrainDrawings) {
			if (trainDrawing.train.equals(train))
				return trainDrawing;
		}
		
		return null;
	}
	
	/**
	 * 根据Train名寻找对应的Train Drawing. 如果找不到,返回空.
	 * @param trainName
	 * @return
	 */
	public TrainDrawing findTrainDrawingByName(String trainName) {
		if (trainName == null)
			return null;
		
		Integer index = trainDrawingIndex.get(trainName);
		return (index != null && index >= 0 && index < allTrainDrawings.size()) ? 
				allTrainDrawings.get(index) : null;
		
//		for (TrainDrawing trainDrawing : allTrainDrawings.values()) {
//			if (trainDrawing.train.trainNameDown.equalsIgnoreCase(trainName) ||
//					trainDrawing.train.trainNameUp.equalsIgnoreCase(trainName) ) 
//				return trainDrawing;
//		}
//		
//		return null;
	}
	
}
