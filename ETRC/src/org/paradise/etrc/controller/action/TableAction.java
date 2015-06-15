package org.paradise.etrc.controller.action;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.paradise.etrc.util.ui.widget.table.JEditTable;

public abstract class TableAction extends UIAction {
	protected JTable table;
	
	TableAction(JTable table) {
		this.table = table;
	}
	
	protected void stopCellEditing(boolean save) {
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
	
	protected void selectRow(int index) {
		table.setRowSelectionInterval(index, index);
	}
	
	protected void selectColumn(int index) {
		table.setColumnSelectionInterval(index, index);
	}
	
	protected void selectElement(int index, boolean vertical) {
		if (vertical) {
			int rowCount = table.getRowCount();
			if (index < 0)
				index = 0;
			if (index >= rowCount)
				index = rowCount - 1;
				
			selectRow(index);
		} else {
			int columnCount = table.getColumnCount();
			if (index < 0)
				index = 0;
			if (index >= columnCount)
				index = columnCount - 1;
			
			selectColumn(index);
		}
	}
	
	protected void fireTableChanged() {
		AbstractTableModel model = (AbstractTableModel) table.getModel();
		model.fireTableDataChanged();
	}
	
	protected void fireTableChanged(boolean vertical, int index1, int index2) {
		int startIndex, endIndex;
		if (index1 <= index2) {
			startIndex = index1;
			endIndex = index2;
		} else {
			startIndex = index2;
			endIndex = index1;
		}
		
		if (vertical) {
			AbstractTableModel model = (AbstractTableModel) table.getModel();
			model.fireTableRowsUpdated(startIndex, endIndex);
		} else {
			AbstractTableModel model = (AbstractTableModel) table.getModel();
			model.fireTableDataChanged();
		}
	}
}
