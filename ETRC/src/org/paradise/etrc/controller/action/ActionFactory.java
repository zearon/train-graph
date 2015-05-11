package org.paradise.etrc.controller.action;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.paradise.etrc.controller.ActionManager;
import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.util.ui.table.DefaultJEditTableModel;
import org.paradise.etrc.util.ui.table.JEditTable;
import org.paradise.etrc.view.lineedit.StationTableModel;

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
				oldIndex, newIndex, vertical, null);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	public static UIAction createTableElementMoveActionAndDoIt(
			String tableName, JTable table, Vector list, int oldIndex,
			int newIndex, boolean vertical, Runnable callback) {

		UIAction action = new TableElementMoveAction(tableName, table, list,
				oldIndex, newIndex, vertical, callback);

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

	public static UIAction createSetValueActionAndDoIt(String valueDesc,
			Object oldValue, Object newValue, Consumer<Object> valueSetter, Runnable callback) {

		UIAction action = new SetValueAction(valueDesc, oldValue, newValue,
				valueSetter, callback);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	public static UIAction createRevertRaillineActionAndDoIt(JTable table,
			StationTableModel tableModel) {

		UIAction action = new RevertRaillineAction(table, tableModel);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	public static <T> UIAction createAddTableElementActionAndDoIt(
			String tableName, JTable table, boolean vertical, int index,
			T element, BiConsumer<Integer, T> adder, Consumer<Integer> remover,
			Runnable callback) {

		UIAction action = new AddTableElementAction<T>(tableName, table,
				vertical, index, element, adder, remover, callback);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	public static <T> UIAction createRemoveTableElementActionAndDoIt(String tableName,
			JTable table, boolean vertical, int[] indeces,
			IntFunction<T> getter, BiConsumer<Integer, T> adder,
			Consumer<Integer> remover, Runnable callback) {

		UIAction action = new RemoveTableElementAction<>(tableName, table,
				vertical, indeces, getter, adder, remover, callback);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	public static UIAction createTableRowColumnIncrementActionAndDoIt(
			String repr, String tableName, JEditTable table, int increment,
			int start, int end, int coord, boolean vertical) {

		UIAction action = new TableRowColumnIncrementAction(repr, tableName,
				table, increment, start, end, coord, vertical);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;

	}
}
