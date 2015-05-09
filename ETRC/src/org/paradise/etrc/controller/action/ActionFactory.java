package org.paradise.etrc.controller.action;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.paradise.etrc.controller.ActionManager;
import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;

public class ActionFactory {

	public static UIAction createTableEditActionAndDoIt(String tableName,
			JTable table, DefaultJEditTableModel tableModel, int row,
			int column, Object newValue) {

		UIAction action = new TableEditAction(tableName, table, tableModel,
				row, column, newValue);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	public static UIAction createTableElementMoveActionAndDoIt(
			String tableName, JTable table, Vector list, int oldIndex,
			int newIndex, boolean vertical) {

		UIAction action = new TableElementMoveAction(tableName, table, list,
				oldIndex, newIndex, vertical);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}
}
