package org.paradise.etrc.view.lineedit;

import static org.paradise.etrc.ETRC.__;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;

import org.paradise.etrc.controller.ActionManager;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;

public class RailroadLineTableModel extends DefaultJEditTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5316084320996309203L;
	private JTable table;
	public Vector<RailroadLine> raillines;

	public RailroadLineTableModel(JTable table, Vector<RailroadLine> existingRailLines) {
		this.table = table;
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

	protected UIAction getAction(Object aValue, int rowIndex, int columnIndex) {
		return ActionFactory.createTableEditAction(__("railroad line table"), 
				table, this, rowIndex, columnIndex, aValue);
	}
	
	public void _setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 1:
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