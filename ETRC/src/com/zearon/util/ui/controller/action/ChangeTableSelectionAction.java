package com.zearon.util.ui.controller.action;
import java.util.stream.IntStream;

import javax.swing.JTable;

import static org.paradise.etrc.ETRC.__;

public class ChangeTableSelectionAction extends UIAction implements TableAction {
	JTable table;
	boolean selectionIsRow;
	int[] beforeIndexes1;
	int[] afterIndexes1;
	int[] Indexes2;

	public ChangeTableSelectionAction(JTable table, boolean selectionIsRow, int[] selectedIndexes, boolean expandSelection) {
		this.table = table;
		this.selectionIsRow = selectionIsRow;
		this.beforeIndexes1 = selectionIsRow ? table.getSelectedRows() : table.getSelectedColumns();
		this.afterIndexes1 = selectedIndexes;
		this.Indexes2 = expandSelection ? 
				(selectionIsRow ? IntStream.range(0, table.getColumnCount()).toArray() : IntStream.range(0, table.getRowCount()).toArray()) :
				(selectionIsRow ? table.getSelectedColumns() : table.getSelectedRows());
	}

	@Override
	public void _doAction() {
		selectElements(selectionIsRow, afterIndexes1);
		selectElements(!selectionIsRow, Indexes2);
	}

	@Override
	public void _undoAction() {
		selectElements(selectionIsRow, beforeIndexes1);
		selectElements(!selectionIsRow, Indexes2);
	}

	@Override
	protected boolean _shouldSkip() {
		return false;
	}

	@Override
	public String repr() {
		return __("Move table selections");
	}

	@Override
	public JTable getTable() {
		return table;
	}

}
