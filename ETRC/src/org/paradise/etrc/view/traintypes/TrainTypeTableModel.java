package org.paradise.etrc.view.traintypes;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.data.TrainType;
import org.paradise.etrc.data.util.Tuple;
import org.paradise.etrc.util.ui.table.DefaultJEditTableModel;
import org.paradise.etrc.util.ui.table.JEditTable;

public class TrainTypeTableModel extends DefaultJEditTableModel {
	public TrainGraph trainGraph;
	JEditTable table;
	
	public TrainTypeTableModel(JEditTable table) {
		this.table = table;
		table.setModel(this);
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			// index
			return __("");
		case 1:
			return __("Name");
		case 2:
			return __("Train Name Regex Pattern");
		case 3:
			// Use font family, font color and font size to draw this text.
			return __("Abbr.");
		case 4:
			// Use line color and line style to draw a sample line.
			return __("Line Style");
		default:
			return "";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			// index
			return int.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		case 3:
			// Use font family, font color and font size to draw this text.
			return String.class;
		case 4:
			// Use line color and line style to draw a sample line.
			return TrainType.class;
		default:
			return null;
		}
	}
	
	@Override
	public int getRowCount() {
		return trainGraph.allTrainTypes.size();
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		TrainType trainType = trainGraph.allTrainTypes.get(rowIndex);
		switch (columnIndex) {
		case 0:
			// index
			return rowIndex;
		case 1:
			return trainType.getName();
		case 2:
			return trainType.pattern;
		case 3:
			// Use font family, font color and font size to draw this text.
			return trainType.abbriveation;
		case 4:
			// Use line color and line style to draw a sample line.
			return trainType;
		}
		return "";
	}

	@Override
	public void _setValueAt(Object aValue, int rowIndex, int columnIndex) {
		TrainType trainType = trainGraph.allTrainTypes.get(rowIndex);
		switch (columnIndex) {
		case 1:
			trainType.setName((String) aValue);
			break;
		case 2:
			trainType.pattern = (String) aValue;
			break;
		case 3:
			// Use font family, font color and font size to draw this text.
			trainType.abbriveation = (String) aValue;
			break;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex > 0 && columnIndex < 4;
	}


	@Override
	public boolean nextCellIsBelow(int row, int column, int increment) {
		return false;
	}

	@Override
	protected UIAction getActionAndDoIt(Object aValue, int rowIndex,
			int columnIndex) {
		return ActionFactory.createTableCellEditActionAndDoIt(__("train types table"), 
				table, this, rowIndex, columnIndex, aValue);
	}

	@Override
	public boolean columnIsTimeString(int column) {
		return false;
	}

}
