package org.paradise.etrc.controller.action;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

public interface TableAction {
	public JTable getTable();
	
	default void stopCellEditing(boolean save) {
		JTable table = getTable();
		int row = table.getSelectedRow();
		int column = table.getSelectedColumn();

		TableCellEditor editor = row >= 0 && column >= 0 ?
				table.getCellEditor(row, column) : null;
		if (editor != null)
			if (save)
				editor.stopCellEditing();
			else
				editor.cancelCellEditing();
	}
	
	default void selectRow(int index1, int index2) {
		JTable table = getTable();
		table.setRowSelectionInterval(index1, index2);
	}
	
	default void selectColumn(int index1, int index2) {
		JTable table = getTable();
		table.setColumnSelectionInterval(index1, index2);
	}
	
	default void selectElement(int index, boolean isRow) {
		selectElements(index, index, isRow);
	}
	
	default void selectElements(int index1, int index2, boolean isRow) {
		index1 = validateIndex(index1, isRow);
		index2 = validateIndex(index1, isRow);
		
		if (isRow) {
			selectRow(index1, index2);
		} else {
			selectColumn(index1, index2);
		}
	}
	
	default void selectElements(boolean isRow, int[] selectedIndexes) {
		JTable table = getTable();
		if (isRow) {
			table.getSelectionModel().clearSelection();
			for (int selectedIndex : selectedIndexes) {
				table.addRowSelectionInterval(selectedIndex, selectedIndex);
			}
		} else {
			table.getColumnModel().getSelectionModel().clearSelection();
			for (int selectedIndex : selectedIndexes) {
				table.addColumnSelectionInterval(selectedIndex, selectedIndex);
			}
		}
	}
	
	default int validateIndex(int index, boolean isRow) {
		JTable table = getTable();
		int count = isRow ? table.getRowCount() : table.getColumnCount();
		if (index < 0)
			index = 0;
		if (index >= count)
			index = count - 1;
		
		return index;
	}
	
	default void fireTableChanged() {
		JTable table = getTable();
		AbstractTableModel model = (AbstractTableModel) table.getModel();
		model.fireTableDataChanged();
	}
	
	default void fireTableChanged(boolean isRow, int index1, int index2) {
		JTable table = getTable();
		int startIndex, endIndex;
		if (index1 <= index2) {
			startIndex = index1;
			endIndex = index2;
		} else {
			startIndex = index2;
			endIndex = index1;
		}
		
		if (isRow) {
			AbstractTableModel model = (AbstractTableModel) table.getModel();
			model.fireTableRowsUpdated(startIndex, endIndex);
		} else {
			AbstractTableModel model = (AbstractTableModel) table.getModel();
			model.fireTableDataChanged();
		}
	}
}
