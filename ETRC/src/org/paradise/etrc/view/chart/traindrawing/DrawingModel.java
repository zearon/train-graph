package org.paradise.etrc.view.chart.traindrawing;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.paradise.etrc.data.Chart;
import org.paradise.etrc.data.Circuit;
import org.paradise.etrc.data.Train;
import org.paradise.etrc.view.chart.ChartView;

/**
 * A model representing all the train data should be painted on a ChartView,
 *  mainly the scheduling information about every train in a train circuit.
 * @author Jeff Gong
 *
 */
public class DrawingModel {
	
	protected Circuit currentCircuit;

	protected Vector<TrainDrawing> allTrainDrawings = new Vector<TrainDrawing>();
	protected Vector<TrainDrawing> upTrainDrawings = new Vector<TrainDrawing>();
	protected Vector<TrainDrawing> downTrainDrawings = new Vector<TrainDrawing> ();
	protected Vector<TrainDrawing> unknownTrainDrawings = new Vector<TrainDrawing>();
	
	protected HashMap<String, Integer> trainDrawingIndex = new HashMap<String, Integer>();

	protected TrainDrawing activeTrainDrawing = null;
	protected boolean isLastActiveTrainDrawingInUnderMode = false;
	protected Vector<TrainDrawing> normalDrawings = new Vector<TrainDrawing>();
	protected Vector<TrainDrawing> underDrawings  = new Vector<TrainDrawing>();

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
	

	public void buildTrainDrawings(Chart chart, ChartView chartView) {
		isLastActiveTrainDrawingInUnderMode = chartView.showUpDownState == ChartView.SHOW_NONE;
		updateCurrentCircuit(chart, currentCircuit, chartView);
		updateUpDownTrainOption(3);
	}
	
	/**
	 * 根据视图的当前线路更新上下行train列表
	 * @param chart
	 * @param currentCircuit
	 */
	public void updateCurrentCircuit(Chart chart, Circuit currentCircuit, ChartView chartView) {
		// 更新当前线路以及Train列表
		this.currentCircuit = currentCircuit;
		
		// 根据新线路区分上下行train
		allTrainDrawings.clear();
		upTrainDrawings.clear();
		downTrainDrawings.clear();
		unknownTrainDrawings.clear();
		trainDrawingIndex.clear();
		
		for (int i = 0; i < chart.getTrainNum(); i++) {
			Train train = chart.getTrain(i);
			
			TrainDrawing trainDrawing = new TrainDrawing(chartView, train, false, true); // active=false, under=true
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
				break;
			case Train.DOWN_TRAIN:
				downTrainDrawings.add(trainDrawing);
				break;
			default:
				break;
			}
		}
	}
	
	public void setActiveTrain(Train activeTrain) {
		if (activeTrainDrawing != null) {
			activeTrainDrawing.setActive(false);
			activeTrainDrawing.setUnderDrawing(isLastActiveTrainDrawingInUnderMode);
		}
		
		if (activeTrain == null) {
			activeTrainDrawing = null;
		} else {
			activeTrainDrawing = findTrainDrawing(activeTrain);

			isLastActiveTrainDrawingInUnderMode = activeTrainDrawing.isUnderDrawing();
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
