package org.paradise.etrc.view.sheet;

import javax.swing.AbstractListModel;

import org.paradise.etrc.data.RailroadLineChart;

public class RowHeaderModel extends AbstractListModel<Object> {
	private static final long serialVersionUID = 547009998890792058L;
	
	private RailroadLineChart chart;
	
	public RowHeaderModel(RailroadLineChart _chart) {
		chart = _chart;
	}
	
    public int getSize() { 
    	return chart.railroadLine.getStationNum() * 2;
    }
    
    public Object getElementAt(int index) { 
		String sta = chart.railroadLine.getStation(index / 2).name;
		sta += index % 2 == 0 ? "站 到" : "站 发";
		return sta;
    }
}
