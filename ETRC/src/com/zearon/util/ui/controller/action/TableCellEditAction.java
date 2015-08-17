package com.zearon.util.ui.controller.action;

import javax.swing.JTable;

import com.zearon.util.ui.widget.table.DefaultJEditTableModel;

import static com.zearon.util.debug.DebugUtil.IS_DEBUG;

import static org.paradise.etrc.ETRC.__;

public class TableCellEditAction extends UIAction implements TableAction {
	JTable table;
	String tableName;
	DefaultJEditTableModel tableModel;
	int row;
	int column;
	Object oldValue;
	Object newValue;

	public TableCellEditAction(String tableName, JTable table,
			DefaultJEditTableModel tableModel, int row, int column,
			Object newValue) {

		this.table = table;
		this.tableName = tableName;
		this.tableModel = tableModel;
		this.row = row;
		this.column = column;
		this.newValue = newValue;
		this.oldValue = tableModel.getValueAt(row, column);
	}

	@Override
	public void undoAction() {
		stopCellEditing(true);

		_undoAction();

		log("Undo action: %s", repr());
	}

	@Override
	public void redoAction() {
		stopCellEditing(true);

		_doAction();

		log("Undo action: %s", repr());
	}

	@Override
	protected void _doAction() {
		tableModel._setValueAt(newValue, row, column);
	}

	@Override
	protected void _undoAction() {
		tableModel._setValueAt(oldValue, row, column);
	}

	@Override
	protected boolean _shouldSkip() {
		return (oldValue == null && newValue == null)
				|| (newValue != null && newValue.equals(oldValue));
	}

	@Override
	public String repr() {
		if (IS_DEBUG()) {
			return String.format(
					__("Set %s cell value [%d,%d]=%s. Old value is %s"),
					tableName, row, column, newValue, oldValue);
		} else {
			return String.format(__("Set %s cell value [%d,%d]=%s"), tableName,
					row, column, newValue);
		}
	}

	@Override
	public JTable getTable() {
		return table;
	}

}
