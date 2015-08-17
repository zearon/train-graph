package org.paradise.etrc.view.runningchart.sheet;

import javax.swing.AbstractListModel;

import org.paradise.etrc.data.v1.RailroadLineChart;

public class RowHeaderModel extends AbstractListModel<Object> {
	private static final long serialVersionUID = 547009998890792058L;
	
	RailroadLineChart chart;
	
	public RowHeaderModel(RailroadLineChart _chart) {
		chart = _chart;
	}
	
    public int getSize() { 
    	return chart.railroadLine.getStationNum() * 2;
    }
    
    public Object getElementAt(int index) { 
//		String sta = chart.railroadLine.getStation(index / 2).getName();
		return index % 2 == 0 ? chart.railroadLine.getStation(index / 2).getName() + "站 到" : "发";
//		return sta;
    }
}
