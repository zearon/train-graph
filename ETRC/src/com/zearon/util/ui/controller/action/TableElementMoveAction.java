package com.zearon.util.ui.controller.action;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import static org.paradise.etrc.ETRC.__;

public class TableElementMoveAction extends UIAction implements TableAction {
	JTable table;
	String tableName;
	Vector<Object> list;
	int oldIndex;
	int newIndex;
	boolean vertical;
	Runnable callback;

	public TableElementMoveAction(String tableName, JTable table, Vector<Object> list,
			int oldIndex, int newIndex, boolean vertical, Runnable callback) {
		
		this.table = table;
		this.tableName = tableName;
		this.list = list;
		this.oldIndex = oldIndex;
		this.newIndex = newIndex;
		this.vertical = vertical;
		this.callback = callback;
	}

	@Override
	protected void _doAction() {
		TableCellEditor editor = null;
		if (vertical) {
			int column = table.getSelectedColumn();
			editor = column >= 0 ? table.getCellEditor(oldIndex, column) : null;
		} else {
			int row = table.getSelectedColumn();
			editor = row >= 0 ? table.getCellEditor(row, oldIndex) : null;
		}

		if (editor != null)
			editor.stopCellEditing();
		
		Object element = list.remove(oldIndex);
		list.insertElementAt(element, newIndex);
		fireTableChanged(vertical, oldIndex, newIndex);
		
		editor = null;
		if (vertical) {
			table.setRowSelectionInterval(newIndex, newIndex);
//			table.setColumnSelectionInterval(0, table.getColumnCount() - 1);
			int column = table.getSelectedColumn();
			editor = column >= 0 ? table.getCellEditor(newIndex, column) : null;
		} else {
			table.setColumnSelectionInterval(newIndex, newIndex);
//			table.setRowSelectionInterval(0, table.getRowCount() - 1);
			int row = table.getSelectedColumn();
			editor = row >= 0 ? table.getCellEditor(row, newIndex) : null;
		}

		if (editor != null)
			editor.stopCellEditing();
		
		if (callback != null)
			callback.run();
	}

	@Override
	protected void _undoAction() {
		TableCellEditor editor = null;
		if (vertical) {
			int column = table.getSelectedColumn();
			editor = column >= 0 ? table.getCellEditor(newIndex, column) : null;
		} else {
			int row = table.getSelectedColumn();
			editor = row >= 0 ? table.getCellEditor(row, newIndex) : null;
		}

		if (editor != null)
			editor.stopCellEditing();
		
		Object element = list.remove(newIndex);
		list.insertElementAt(element, oldIndex);
		fireTableChanged(vertical, oldIndex, newIndex);
		
		editor = null;
		if (vertical) {
			table.setRowSelectionInterval(oldIndex, oldIndex);
//			table.setColumnSelectionInterval(0, table.getColumnCount() - 1);
			int column = table.getSelectedColumn();
			editor = column >= 0 ? table.getCellEditor(oldIndex, column) : null;
		} else {
			table.setColumnSelectionInterval(oldIndex, oldIndex);
//			table.setRowSelectionInterval(0, table.getRowCount() - 1);
			int row = table.getSelectedColumn();
			editor = row >= 0 ? table.getCellEditor(row, oldIndex) : null;
		}
		
		if (editor != null)
			editor.stopCellEditing();
		
		if (callback != null)
			callback.run();
	}

	@Override
	protected boolean _shouldSkip() {
		return (oldIndex == newIndex);
	}

	@Override
	public String repr() {
		return String.format(__("Move the element at [%d] to [%d] in %s "),
				oldIndex, newIndex, tableName);
	}

	@Override
	public JTable getTable() {
		return table;
	}

}
