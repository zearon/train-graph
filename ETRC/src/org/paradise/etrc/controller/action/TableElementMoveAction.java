package org.paradise.etrc.controller.action;

import java.util.Vector;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;

import static org.paradise.etrc.ETRC.__;
import static org.paradise.etrc.ETRCUtil.*;

public class TableElementMoveAction extends UIAction {
	String tableName;
	JTable table;
	Vector list;
	int oldIndex;
	int newIndex;
	boolean vertical;

	TableElementMoveAction(String tableName, JTable table, Vector list,
			int oldIndex, int newIndex, boolean vertical) {
		
		this.tableName = tableName;
		this.table = table;
		this.list = list;
		this.oldIndex = oldIndex;
		this.newIndex = newIndex;
		this.vertical = vertical;
	}

	@Override
	public void undoAction() {
		_undoAction();

		log("Undo action: %s", repr());
	}

	@Override
	public void redoAction() {
		_doAction();

		log("Undo action: %s", repr());
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

}
