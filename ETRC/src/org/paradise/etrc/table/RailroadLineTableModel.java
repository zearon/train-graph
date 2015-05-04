package org.paradise.etrc.table;

import static org.paradise.etrc.ETRC.__;

import java.util.Vector;

import javax.swing.event.TableModelListener;

import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;

public class RailroadLineTableModel extends DefaultJEditTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5316084320996309203L;
	public Vector<RailroadLine> raillines;

	public RailroadLineTableModel(Vector<RailroadLine> existingRailLines) {
		setRailLines(existingRailLines);
	}
	
	public void setRailLines(Vector<RailroadLine> existingRailLines) {
		if (existingRailLines == null) {
			raillines = new Vector<RailroadLine>(8);
		} else {
			raillines = new Vector<RailroadLine>(existingRailLines.size() + 8);
			for (RailroadLine line : existingRailLines) {
				raillines.add(line.copy());
			}
		}
	}

	/**
	 * getColumnCount
	 *
	 * @return int
	 */
	public int getColumnCount() {
		return 3;
	}

	/**
	 * getRowCount
	 *
	 * @return int
	 */
	public int getRowCount() {
		return raillines.size();
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
		return (columnIndex != 1);
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
		case 1:
			return String.class;
		case 2:
			return Integer.class;
		case 0:
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
		case 1:
			return raillines.get(rowIndex).name;
		case 2:
			return new Integer(raillines.get(rowIndex).zindex);
		case 0:
			return new Boolean(raillines.get(rowIndex).visible);
		default:
			return null;
		}
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
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 1:
			// circuit.getStation(rowIndex).name = (String) aValue;
			break;
		case 2:
			raillines.get(rowIndex).zindex = ((Number) aValue).intValue();
			break;
		case 0:
			raillines.get(rowIndex).visible = ((Boolean) aValue).booleanValue();
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
		case 1:
			return __("Circuit");
		case 2:
			return __("zIndex");
		case 0:
			return __("Visible");
		default:
			return null;
		}
	}

	/**
	 * addTableModelListener
	 *
	 * @param l
	 *            TableModelListener
	 */
	public void addTableModelListener(TableModelListener l) {
	}

	/**
	 * removeTableModelListener
	 *
	 * @param l
	 *            TableModelListener
	 */
	public void removeTableModelListener(TableModelListener l) {
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