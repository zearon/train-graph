package org.paradise.etrc.controller.action;

import javax.swing.JTable;

import org.paradise.etrc.view.lineedit.StationTableModel;

import static org.paradise.etrc.ETRC.__;

public class RevertRaillineAction extends UIAction {

	StationTableModel tableModel;
	JTable table;

	RevertRaillineAction(JTable table, StationTableModel tableModel) {
		this.table = table;
		this.tableModel = tableModel;
	}

	@Override
	protected void _doAction() {
		tableModel.railroadLine.revertStations();

		tableModel.fireTableDataChanged();

		int rowSelection = table.getSelectedRow();
		if (rowSelection >= 0) {
			rowSelection = table.getRowCount() - 1 - rowSelection;
			table.setRowSelectionInterval(rowSelection, rowSelection);
		}
	}

	@Override
	protected void _undoAction() {
		_doAction();
	}

	@Override
	protected boolean _shouldSkip() {
		return false;
	}

	@Override
	public String repr() {
		return String.format(__("Revert stations in %s."),
				tableModel.railroadLine.getName());
	}

}
