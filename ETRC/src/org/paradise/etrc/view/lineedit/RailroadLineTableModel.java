package org.paradise.etrc.view.lineedit;

import java.awt.Color;
import java.util.Vector;

import javax.swing.JTable;

import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.v1.RailNetwork;
import org.paradise.etrc.data.v1.RailroadLine;

import com.zearon.util.ui.widget.table.DefaultJEditTableModel;

import static org.paradise.etrc.ETRC.__;

public class RailroadLineTableModel extends DefaultJEditTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5316084320996309203L;
	private JTable table;
	public Vector<RailroadLine> raillines;

	public RailroadLineTableModel(JTable table, RailNetwork railNetwork) {
		this.table = table;
		setRailNetwork(railNetwork);
	}
	
	public void setRailNetwork(RailNetwork railNetwork) {
		if (railNetwork != null)
			this.raillines = railNetwork.getAllRailroadLines();
		fireTableDataChanged();
	}

	/**
	 * getColumnCount
	 *
	 * @return int
	 */
	public int getColumnCount() {
		return 4;
	}

	/**
	 * getRowCount
	 *
	 * @return int
	 */
	public int getRowCount() {
		if (raillines == null)
			return 0;
		else
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
		case 3:
			return Color.class;
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
			return raillines.get(rowIndex).getName();
		case 2:
			return new Integer(raillines.get(rowIndex).zindex);
		case 3:
			return raillines.get(rowIndex).lineColor;
		case 0:
			return new Boolean(raillines.get(rowIndex).visible);
		default:
			return null;
		}
	}

	protected UIAction getActionAndDoIt(Object aValue, int rowIndex, int columnIndex) {
		return ActionFactory.createTableCellEditAction(__("railroad line table"), 
				table, this, rowIndex, columnIndex, aValue).addToManagerAndDoIt();
	}
	
	public void _setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 1:
			break;
		case 2:
			raillines.get(rowIndex).zindex = ((Number) aValue).intValue();
			break;
		case 3:
			raillines.get(rowIndex).lineColor = (Color) aValue;
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
		case 3:
			return __("Color");
		case 0:
			return __("Visible");
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