package org.paradise.etrc.controller.action;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;

public class TrainTableEditAction extends TableEditAction {

	Vector<RailroadLine> raillines;

	TrainTableEditAction(JTable table,
			DefaultJEditTableModel tableModel, int row, int column,
			Object newValue) {

		super("train", table, tableModel, row, column, newValue);
	}
}
