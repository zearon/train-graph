package org.paradise.etrc.controller.action;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;

public class ActionFactory {

	public static UIAction createRailroadLineTableEditAction(JTable table,
			DefaultJEditTableModel tableModel, int row, int column,
			Object newValue) {

		return new RailroadLineTableEditAction(table, tableModel, row, column,
				newValue);
	}

	public static UIAction createStationTableEditAction(JTable table,
			DefaultJEditTableModel tableModel, int row, int column,
			Object newValue) {

		return new StationTableEditAction(table, tableModel, row, column,
				newValue);
	}
}
