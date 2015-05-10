package org.paradise.etrc.controller.action;

import javax.swing.JTable;

import org.paradise.etrc.view.widget.JEditTable;

import static org.paradise.etrc.ETRC.__;

public class TableRowColumnIncrementAction extends TableAction {

	String repr;
	String tableName;
	JEditTable table;
	int increment;
	int start;
	int end;
	int coord;
	boolean vertical;

	TableRowColumnIncrementAction(String repr, String tableName,
			JEditTable table, int increment, int start, int end, int coord,
			boolean vertical) {
		
		super(table);
		this.repr = repr;
		this.tableName = tableName;
		this.table = table;
		this.increment = increment;
		this.start = Math.min(start, end);
		this.end = Math.max(start, end);
		this.coord = coord;
		this.vertical = vertical;
	}

	@Override
	protected void _doAction() {
		stopCellEditing(false);
		table.batchChangeValue(increment, start, end, coord, vertical);
		fireTableChanged();
	}

	@Override
	protected void _undoAction() {
		stopCellEditing(false);
		table.batchChangeValue(-1*increment, start, end, coord, vertical);
		fireTableChanged();
	}

	@Override
	protected boolean _shouldSkip() {
		return increment == 0;
	}

	@Override
	public String repr() {
		String position = vertical ? String.format("[%d-%d,%d]", start, end, coord) :
			String.format("[%d,%d-%d]", coord, start, end);
		return repr != null ? repr : String.format(__("Batch change value at %s by %d in %s"), 
				position, increment, tableName);
	}

}
