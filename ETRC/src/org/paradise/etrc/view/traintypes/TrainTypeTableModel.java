package org.paradise.etrc.view.traintypes;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainType;
import org.paradise.etrc.util.ui.widget.table.DefaultJEditTableModel;
import org.paradise.etrc.util.ui.widget.table.JEditTable;

import static org.paradise.etrc.ETRC.__;

public class TrainTypeTableModel extends DefaultJEditTableModel {
	public TrainGraph trainGraph;
	JEditTable table;
	int emptyRowIndex;
	int defaultRowIndex;
	
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
			return __("Train Name Pattern");
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
		int typeCount = trainGraph.trainTypeCount();
		emptyRowIndex = typeCount;
		defaultRowIndex = emptyRowIndex + 1;
		return typeCount + 2;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		TrainType trainType = getTrainTypeAtRow(rowIndex);
		if (trainType == null)
			return "";
		
		return getObjectAttribute(trainType, rowIndex, columnIndex);
	}
	
	public TrainType getTrainTypeAtRow(int rowIndex) {
		if (rowIndex == emptyRowIndex)
			return null;
		
		if (rowIndex == defaultRowIndex)
			return trainGraph.getDefaultTrainType();
		else
			return trainGraph.getTrainType(rowIndex);
	}
	
	private Object getObjectAttribute(TrainType trainType, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			// index
			return rowIndex < emptyRowIndex ? rowIndex + 1 : "0";
		case 1:
			return trainType.getName();
		case 2:
			return trainType.getPattern();
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
		TrainType trainType = trainGraph.getDefaultTrainType();
		if (rowIndex >=0 && rowIndex < trainGraph.trainTypeCount())
			trainType = trainGraph.getTrainType(rowIndex);
		
		switch (columnIndex) {
		case 1:
			trainType.setName((String) aValue);
			break;
		case 2:
			trainType.setPattern((String) aValue);
			break;
		case 3:
			// Use font family, font color and font size to draw this text.
			trainType.abbriveation = (String) aValue;
			break;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (rowIndex < emptyRowIndex && columnIndex > 0 && columnIndex < 4) ||
				(rowIndex == defaultRowIndex && (columnIndex == 1 || columnIndex == 3));
	}


	@Override
	public boolean nextCellIsBelow(int row, int column, int increment) {
		return false;
	}

	@Override
	protected UIAction getActionAndDoIt(Object aValue, int rowIndex,
			int columnIndex) {
		return ActionFactory.createTableCellEditAction(__("train types table"), 
				table, this, rowIndex, columnIndex, aValue).addToManagerAndDoIt();
	}

	@Override
	public boolean columnIsTimeString(int column) {
		return false;
	}

}
