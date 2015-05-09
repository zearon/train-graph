package org.paradise.etrc.controller.action;

import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.paradise.etrc.controller.ActionManager;
import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;
import org.paradise.etrc.view.widget.JEditTable;

public class ActionFactory {

	public static UIAction createTableEditActionAndDoIt(String tableName,
			JTable table, DefaultJEditTableModel tableModel, int row,
			int column, Object newValue) {

		UIAction action = new TableCellEditAction(tableName, table, tableModel,
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

	public static UIAction createTableCellIncrementActionAndDoIt(
			String tableName, JEditTable table, int startRow, int startColumn,
			int increment, boolean skipTheFirst, boolean isContinuousEditMode,
			boolean mouseAction) {

		UIAction action = new TableCellIncrementAction(tableName, table,
				startRow, startColumn, increment, skipTheFirst,
				isContinuousEditMode, mouseAction);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}
	
	public static UIAction createSetValueActionAndDoIt(String valueDesc, Object oldValue, Object newValue,
			Consumer<Object> valueSetter) {

		UIAction action = new SetValueAction(valueDesc, oldValue, newValue, valueSetter);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
		
	}
}
