package org.paradise.etrc.view.timetables;

import static org.paradise.etrc.ETRC.__;

import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.RailNetworkChart;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.util.ui.table.DefaultJEditTableModel;
import org.paradise.etrc.util.ui.table.JEditTable;

public class TimetableListTableModel extends DefaultJEditTableModel {
	public TrainGraph trainGraph;
	JEditTable table;
	
	public TimetableListTableModel(JEditTable table) {
		this.table = table;
		table.setModel(this);
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return __("Name");
		default:
			break;
		}
		return "";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		default:
			break;
		}
		return null;
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
