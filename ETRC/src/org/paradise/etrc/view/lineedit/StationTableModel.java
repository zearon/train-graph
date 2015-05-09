package org.paradise.etrc.view.lineedit;

import static org.paradise.etrc.ETRC.__;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;

import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;

public class StationTableModel extends DefaultJEditTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6136704973824924463L;

	private JTable table;
	public RailroadLine railroadLine;

	public StationTableModel(JTable table, RailroadLine line) {
		this.table = table;
		setRailroadLine(line);
	}
	
	public void setRailroadLine(RailroadLine line) {
		if (line != null)
			railroadLine = line.copy();
		else
			railroadLine = TrainGraphFactory.createInstance(RailroadLine.class);
	}

	/**
	 * getColumnCount
	 *
	 * @return int
	 */
	public int getColumnCount() {
		return 5;
	}

	/**
	 * getRowCount
	 *
	 * @return int
	 */
	public int getRowCount() {
		return railroadLine.getStationNum();
	}

	/**
	 * isCellEditable
	 *
	 * @param rowIndex
	 *            int
	 * @param columnIndex
	 *            int
	 * @return boolean
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex == 0) || (columnIndex == 1)
				|| (columnIndex == 3) || (columnIndex == 4);
	}

	/**
	 * getColumnClass
	 *
	 * @param columnIndex
	 *            int
	 * @return Class
	 */
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
		case 2:
		case 3:
			return Integer.class;
		case 4:
			return Boolean.class;
		default:
			return null;
		}

	}

	/**
	 * getValueAt
	 *
	 * @param rowIndex
	 *            int
	 * @param columnIndex
	 *            int
	 * @return Object
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return railroadLine.getStation(rowIndex).name;
		case 1:
			return new Integer(railroadLine.getStation(rowIndex).dist);
		case 2:
			return new Integer(Math.round(railroadLine.dispScale
					* railroadLine.getStation(rowIndex).dist));
		case 3:
			return new Integer(railroadLine.getStation(rowIndex).level);
		case 4:
			return new Boolean(railroadLine.getStation(rowIndex).hide);
		default:
			return null;
		}
	}

	protected UIAction getAction(Object aValue, int rowIndex, int columnIndex) {
		return ActionFactory.createTableEditAction(__("station table"), 
				table, this, rowIndex, columnIndex, aValue);
	}

	/**
	 * setValueAt
	 *
	 * @param aValue
	 *            Object
	 * @param rowIndex
	 *            int
	 * @param columnIndex
	 *            int
	 */
	public void _setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			railroadLine.getStation(rowIndex).name = (String) aValue;
			break;
		case 1:
			int offset = ((Integer) aValue).intValue()
					- railroadLine.getStation(rowIndex).dist;
			railroadLine.getStation(rowIndex).dist = ((Integer) aValue)
					.intValue();

			if (rowIndex == railroadLine.getStationNum() - 1)
				railroadLine.length += offset;
			break;
		case 2:
			break;
		case 3:
			railroadLine.getStation(rowIndex).level = ((Integer) aValue)
					.intValue();
			break;
		case 4:
			railroadLine.getStation(rowIndex).hide = ((Boolean) aValue)
					.booleanValue();
			break;
		default:
		}

		fireTableCellUpdated(rowIndex, columnIndex);
	}

	/**
	 * getColumnName
	 *
	 * @param columnIndex
	 *            int
	 * @return String
	 */
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return __("Station");
		case 1:
			return __("Distance");
		case 2:
			return __("Display Dist.");
		case 3:
			return __("Level");
		case 4:
			return __("Hidden");
		default:
			return null;
		}
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