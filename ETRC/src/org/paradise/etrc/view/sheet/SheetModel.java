package org.paradise.etrc.view.sheet;

import org.paradise.etrc.data.RailroadLineChart;
import org.paradise.etrc.data.Stop;
import org.paradise.etrc.data.Train;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;

public class SheetModel extends DefaultJEditTableModel {
	private static final long serialVersionUID = 6767541225039467460L;

	public RailroadLineChart chart;
	public SheetModel(RailroadLineChart _chart) {
		chart = _chart;
	}
	
	public int getColumnCount() {
		return chart.getTrainNum();
	}

	public int getRowCount() {
		return chart.railroadLine.getStationNum() * 2;
	}

	public Object getValueAt(int rowIndex, int colIndex) {
		Train theTrain = chart.getTrain(colIndex);
		String staName = chart.railroadLine.getStation(rowIndex / 2).name;
		Stop stop = findStop(theTrain, staName);
		
		return stop;		
//		if(stop == null)
//			return null;
//		else if(rowIndex % 2 == 0)
//			return stop.arrive;
//		else
//			return stop.leave;
	}

	private Stop findStop(Train theTrain, String staName) {
		for(int i=0; i<theTrain.getStopNum(); i++) {
			if(theTrain.getStop(i).stationName.equalsIgnoreCase(staName)) {
				return theTrain.getStop(i);
			}
		}
		return null;
	}

	public String getColumnName(int conIndex) {
		return chart.getTrain(conIndex).getTrainName(chart.railroadLine);
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		return Stop.class;
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	//修改到发点时间不需要特殊处理，在CellEditor里面就处理好了
	//此处需要处理添加、删除停站的操作
	public void setValueAt(Object aValue, int rowIndex, int colIndex)  {
		Train theTrain = chart.getTrain(colIndex);
		String staName = chart.railroadLine.getStation(rowIndex / 2).name;
		Stop stop = (Stop) aValue;
		
		if(stop != null) {
//			System.out.println(theTrain.trainNameFull + " VV " + stop.stationName + "|" + stop.arrive + "|" + stop.leave);
			//stop的站名非空，表示原来就有这条数据
			if(stop.stationName != null) {
				//到发点只要有一个为"DEL"就认为要删除这个停站
				if(stop.arrive.equals("DEL") || stop.leave.equals("DEL"))
					theTrain.delStop(stop.stationName);
			}
			//stop站名为空，表示原来没有这条数据，是CellEditor接收到输入后new出来的新的停站数据
			else {
				stop.stationName = staName;
//				Stop prevStop = chart.findPrevStop(theTrain, stop.stationName);
//				theTrain.insertStopAfter(prevStop, stop);
				
				chart.insertNewStopToTrain(theTrain, stop);
			}
		}
		else {
//			System.out.println("NN");
		}
	}

	@Override
	public boolean columnIsTimeString(int column) {
		return false;
	}

	@Override
	public boolean nextCellIsBelow(int row, int column, int increment) {
		Train theTrain = chart.getTrain(column);
		
		if (theTrain.isDownTrain(chart.railroadLine) == Train.DOWN_TRAIN)
			return true;
		else
			return false;
	}

}
