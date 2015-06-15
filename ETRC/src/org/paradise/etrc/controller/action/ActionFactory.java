package org.paradise.etrc.controller.action;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.paradise.etrc.controller.ActionManager;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.util.function.MultiConsumer;
import org.paradise.etrc.util.function.TriConsumer;
import org.paradise.etrc.util.ui.widget.table.DefaultJEditTableModel;
import org.paradise.etrc.util.ui.widget.table.JEditTable;
import org.paradise.etrc.view.lineedit.StationTableModel;

public class ActionFactory {
	
	/**
	 * In order to keep track of all actions that may modify the underlying
	 * data model, even actions that should not or do not yet support undo/redo
	 * are in control of action manager. For these actions, DirectAction is used.
	 * @param actionRepr A string that describes the action.
	 * @param action0 An action that takes no parameter.
	 * @return The DirectAction created and executed
	 */
	public static UIAction createDirectActionAndDoIt(String actionDesc, Runnable action0) {
		UIAction action = new DirectAction(actionDesc, action0);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}
	/**
	 * In order to keep track of all actions that may modify the underlying
	 * data model, even actions that should not or do not yet support undo/redo
	 * are in control of action manager. For these actions, DirectAction is used.
	 * @param actionRepr A string that describes the action.
	 * @param action1 An action that takes one parameter.
	 * @param a Parameter 1.
	 * @return The DirectAction created and executed
	 */
	public static <T1> UIAction createDirectActionAndDoIt(String actionDesc, 
			Consumer<T1> action1, T1 a) {
		
		UIAction action = new DirectAction(actionDesc, action1, a);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}
	/**
	 * In order to keep track of all actions that may modify the underlying
	 * data model, even actions that should not or do not yet support undo/redo
	 * are in control of action manager. For these actions, DirectAction is used.
	 * @param actionRepr A string that describes the action.
	 * @param action1 An action that takes two parameters.
	 * @param a1 Parameter 1.
	 * @param a2 Parameter 2
	 * @return The DirectAction created and executed
	 */
	public static <T1, T2> UIAction createDirectActionAndDoIt(String actionDesc, 
			BiConsumer<T1, T2> action2, T1 a1, T2 a2) {
		
		UIAction action = new DirectAction(actionDesc, action2, a1, a2);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}
	/**
	 * In order to keep track of all actions that may modify the underlying
	 * data model, even actions that should not or do not yet support undo/redo
	 * are in control of action manager. For these actions, DirectAction is used.
	 * @param actionRepr A string that describes the action.
	 * @param action1 An action that takes three parameters.
	 * @param a1 Parameter 1.
	 * @param a2 Parameter 2
	 * @param a3 Parameter 3
	 * @return The DirectAction created and executed
	 */
	public static <T1, T2, T3> UIAction createDirectActionAndDoIt(String actionDesc, 
			TriConsumer<T1, T2, T3> action3, T1 a1, T2 a2, T3 a3) {
		
		UIAction action = new DirectAction(actionDesc, action3, a1, a2, a3);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}
	/**
	 * In order to keep track of all actions that may modify the underlying
	 * data model, even actions that should not or do not yet support undo/redo
	 * are in control of action manager. For these actions, DirectAction is used.
	 * @param actionRepr A string that describes the action.
	 * @param action1 An action that takes at least three parameters.
	 * @param a1 Parameter 1.
	 * @param a2 Parameter 2
	 * @param a3 Parameter 3
	 * @param a4 Parameter 4
	 * @return The DirectAction created and executed
	 */
	public static <T1, T2, T3, T4> UIAction createDirectActionAndDoIt(String actionDesc, 
			MultiConsumer<T1, T2, T3, T4> action4, T1 a1, T2 a2, T3 a3, T4 a4) {
		
		UIAction action = new DirectAction(actionDesc, action4, a1, a2, a3, a4);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	/**
	 * Create a TableEditAction and execute it.
	 * A TableEditAction represents an action that modifies a cell in a JTable.
	 * @param tableName A name used to describe the table being modified.
	 * @param table The JTable object being modified.
	 * @param tableModel A model in DefaultJEditTableModel type of the table being modified.
	 * @param row The row index of the cell being modified in the table.
	 * @param column The column index of the cell being modified in the table.
	 * @param newValue The new value of the cell being modified in the table.
	 * @return The created and executed TableCellEditAction object.
	 */
	public static UIAction createTableCellEditActionAndDoIt(String tableName,
			JTable table, DefaultJEditTableModel tableModel, int row,
			int column, Object newValue) {

		UIAction action = new TableCellEditAction(tableName, table, tableModel,
				row, column, newValue);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	/**
	 * Create a TableElementMoveAction and execute it.
	 * A TableElementMoveAction represents an action that moves a row up or down,
	 * or a column left or right in a JTable.
	 * @param tableName A name used to describe the table being modified.
	 * @param table The JTable object being modified.
	 * @param list A vector of the underlying data model, which represents the 
	 * row/column elements of the table.
	 * @param oldIndex Old index of the element.
	 * @param newIndex New index of the element.
	 * @param vertical True if move a row up and down; False if move column left and right.
	 * Note: this should be consistent with the list parameter in data model. If the 
	 * elements are rendered as rows, then they can only be moved up and down; if they are
	 * rendered as columns, then only horizontal moving is allowed.
	 * @return The created and executed TableElementMoveAction object.
	 */
	public static UIAction createTableElementMoveActionAndDoIt(
			String tableName, JTable table, Vector list, int oldIndex,
			int newIndex, boolean vertical) {

		UIAction action = new TableElementMoveAction(tableName, table, list,
				oldIndex, newIndex, vertical, null);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}


	/**
	 * Create a TableElementMoveAction and execute it. After the table element is moved,
	 * do an extra call back action.
	 * A TableElementMoveAction represents an action that moves a row up or down,
	 * or a column left or right in a JTable.
	 * @param tableName A name used to describe the table being modified.
	 * @param table The JTable object being modified.
	 * @param list A vector of the underlying data model, which represents the 
	 * row/column elements of the table.
	 * @param oldIndex Old index of the element.
	 * @param newIndex New index of the element.
	 * @param vertical True if move a row up and down; False if move column left and right.
	 * Note: this should be consistent with the list parameter in data model. If the 
	 * elements are rendered as rows, then they can only be moved up and down; if they are
	 * rendered as columns, then only horizontal moving is allowed.
	 * @param callback The action to be done after the table element is moved. It can be null.
	 * @return The created and executed TableElementMoveAction object.
	 */
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

	/**
	 * Create a SetValueAction and execute it. After the value is set,
	 * do an extra call back action.
	 * A SetValueAction represents an action that change a specific value of the 
	 * underlying data model.
	 * @param valueDesc A string represents the value to be changed.
	 * @param oldValue The old value in type <T>.
	 * @param newValue The new value in type <T>.
	 * @param valueSetter A Consumer<T> type function to set the value.
	 * @param callback The action to be done after the value is set. It can be null.
	 * @return The created and executed TableElementMoveAction object.
	 */
	public static <T> UIAction createSetValueActionAndDoIt(String valueDesc,
			T oldValue, T newValue, Consumer<T> valueSetter, Runnable callback) {

		UIAction action = new SetValueAction<T>(valueDesc, oldValue, newValue,
				valueSetter, callback);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	/**
	 * Create a RevertRaillineAction and execute it. 
	 * A RevertRaillineAction represents an action that revert all stations
	 * in a railroad line, i.e. make the first station the last one and the
	 * last station first one; all other stations between are swapped in the
	 * same way.
	 * @param table The JTable represents the station table of a railroad way.
	 * @param tableModel The table of mode in type StationTableModel of of the table.
	 * @return The created and executed RevertRaillineAction object.
	 */
	public static UIAction createRevertRaillineActionAndDoIt(JTable table,
			StationTableModel tableModel) {

		UIAction action = new RevertRaillineAction(table, tableModel);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	/**
	 * Create a AddTableElementAction and execute it. After a new element is added,
	 * do an extra call back action.
	 * A AddTableElementAction represents an action that adds a new element into a JTable.
	 * @param tableName A name used to describe the table being modified.
	 * @param table The JTable object being modified.
	 * @param vertical True if add a row ; False if add a column.
	 * @param index The new row/column index of the element to be inserted.
	 * @param element The element in type of T to be inserted.
	 * @param adder A add function in type of BiConsumer<Integer, T> that add a new element of
	 * type T into the underlying data model in a specific position as the first argument indicates.
	 * <br/>If the underlying data model is a Vector, then <b>add</b> method of the vector
	 * should be used.
	 * @param remover A remove function in type of Consumer<Integer> that remove an element of
	 * type T from the underlying data model at a specific position as the first argument indicates.
	 * This function is used to support undo of the add action.
	 * <br/>If the underlying data model is a Vector, then <b>removeElementAt</b> method of the vector
	 * should be used.
	 * @param callback The action to be done after the value is set. It can be null.
	 * @return The created and executed AddTableElementAction object.
	 */
	public static <T> UIAction createAddTableElementActionAndDoIt(
			String tableName, JTable table, boolean vertical, int index,
			T element, BiConsumer<Integer, T> adder, Consumer<Integer> remover,
			Runnable callback) {

		UIAction action = new AddTableElementAction<T>(tableName, table,
				vertical, index, element, adder, remover, callback);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}


	/**
	 * Create a RemoveTableElementAction and execute it. After the elements are removed,
	 * do an extra call back action.
	 * A RemoveTableElementAction represents an action that removes some elements from a JTable.
	 * @param tableName A name used to describe the table being modified.
	 * @param table The JTable object being modified.
	 * @param vertical True if remove a row ; False if remove a column.
	 * @param indexes The row/column indexes of the elements to be removed.
	 * @param getter A get function in type of IntFunction<T> which accept an index and return
	 * the value at that position from the list of the underlying data model.
	 * @param adder A add function in type of BiConsumer<Integer, T> that add a new element of
	 * type T into the underlying data model in a specific position as the first argument indicates.
	 * This function is used to support redo of the remove action.
	 * <br/>If the underlying data model is a Vector, then <b>add</b> method of the vector
	 * should be used.
	 * @param remover A remove function in type of Consumer<Integer> that remove an element of
	 * type T from the underlying data model at a specific position as the first argument indicates.
	 * <br/>If the underlying data model is a Vector, then <b>removeElementAt</b> method of the vector
	 * should be used.
	 * @param callback The action to be done after the value is set. It can be null.
	 * @return The created and executed RemoveTableElementAction object.
	 */
	public static <T> UIAction createRemoveTableElementActionAndDoIt(String tableName,
			JTable table, boolean vertical, int[] indexes,
			IntFunction<T> getter, BiConsumer<Integer, T> adder,
			Consumer<Integer> remover, Runnable callback) {

		UIAction action = new RemoveTableElementAction<>(tableName, table,
				vertical, indexes, getter, adder, remover, callback);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;
	}

	/**
	 * Create a TableRowColumnIncrementAction and execute it
	 * A TableRowColumnIncrementAction represents an action that change a specific 
	 * range of columns of all rows, or change a specific range of rows of all columns.
	 * @param repr A string representation of the action. If repr is null, a description
	 * of which cells are modified is used as the representation.
	 * @param tableName A name used to describe the table being modified.
	 * @param table The JTable object being modified.
	 * @param increment How much the value of every affected cell is changed.
	 * @param start The start index of the affected range.
	 * @param end The end index of the affected range.
	 * @param coord The other 2D coordinate. if start-end are row indexes, then coord is
	 * a column index; otherwise, it is a row index.
	 * @param vertical True if change a specific range of columns of all rows; 
	 * False if change a specific range of rows of all columns.
	 * @return The created and executed TableRowColumnIncrementAction object.
	 */
	public static UIAction createTableRowColumnIncrementActionAndDoIt(
			String repr, String tableName, JEditTable table, int increment,
			int start, int end, int coord, boolean vertical) {

		UIAction action = new TableRowColumnIncrementAction(repr, tableName,
				table, increment, start, end, coord, vertical);

		ActionManager.getInstance().addActionAndDoIt(action);

		return action;

	}
}
