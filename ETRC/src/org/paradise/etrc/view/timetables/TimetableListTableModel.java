package org.paradise.etrc.view.timetables;

import static org.paradise.etrc.ETRC.__;

import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.RailNetworkChart;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;
import org.paradise.etrc.view.widget.JEditTable;

public class TimetableListTableModel extends DefaultJEditTableModel {
	public TrainGraph trainGraph;
	JEditTable table;
	
	public TimetableListTableModel(JEditTable table) {
		this.table = table;
		table.setModel(this);
	}

	@Override
	public int getRowCount() {
		return trainGraph.getCharts().size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		RailNetworkChart chart = trainGraph.getCharts().get(rowIndex);
		switch(columnIndex) {
		case 0:
			return chart.name;
		}
		
		return "";
	}

	@Override
	public void _setValueAt(Object aValue, int rowIndex, int columnIndex) {
		RailNetworkChart chart = trainGraph.getCharts().get(rowIndex);
		switch(columnIndex) {
		case 0:
			chart.name = (String) aValue;
			break;
		}
	}

	@Override
	protected UIAction getActionAndDoIt(Object aValue, int rowIndex,
			int columnIndex) {
		return ActionFactory.createTableEditActionAndDoIt(__("station table"), 
				table, this, rowIndex, columnIndex, aValue);
	}

	@Override
	public boolean nextCellIsBelow(int row, int column, int increment) {
		return true;
	}

	@Override
	public boolean columnIsTimeString(int column) {
		return false;
	}

}
