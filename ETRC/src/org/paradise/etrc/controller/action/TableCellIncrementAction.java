package org.paradise.etrc.controller.action;

import com.zearon.util.ui.controller.action.UIAction;
import com.zearon.util.ui.widget.table.JEditTable;

import static org.paradise.etrc.ETRC.__;

public class TableCellIncrementAction extends UIAction {
	String tableName;
	JEditTable table;
	int startRow;
	int startColumn;
	int increment;
	boolean skipTheFirst;
	boolean isContinuousEditMode;
	boolean mouseAction;

	TableCellIncrementAction(String tableName, JEditTable table, int startRow,
			int startColumn, int increment, boolean skipTheFirst,
			boolean isContinuousEditMode, boolean mouseAction) {
		this.tableName = tableName;
		this.table = table;
		this.startRow = startRow;
		this.startColumn = startColumn;
		this.increment = increment;
		this.skipTheFirst = skipTheFirst;
		this.isContinuousEditMode = isContinuousEditMode;
		this.mouseAction = mouseAction;
	}

	@Override
	protected void _doAction() {
		table._doContinuousIncreaseCell(startRow, startColumn, increment,
				skipTheFirst, isContinuousEditMode);
	}

	@Override
	protected void _undoAction() {
		table._doContinuousIncreaseCell(startRow, startColumn, -1 * increment,
				skipTheFirst, isContinuousEditMode);
	}

	@Override
	protected boolean _shouldSkip() {
		return increment == 0;
	}
	
	@Override
	public boolean canMerge(UIAction action2) {
		if (action2 == null)
			return false;
		
		if (!(action2 instanceof TableCellIncrementAction))
			return false;
		
		TableCellIncrementAction a2 = (TableCellIncrementAction) action2;
		if (!mouseAction || !a2.mouseAction)
			return false;
		
		if (table != a2.table)
			return false;
		
		if (isContinuousEditMode != a2.isContinuousEditMode)
			return false;
		
		if (startRow != a2.startRow || startColumn != a2.startColumn)
			return false;
		
		return true;
	}
	
	@Override
	public void merge(UIAction action2) {
		increment += ((TableCellIncrementAction) action2).increment;
		log("Merge action {%s} into {%s}", action2.repr(), repr());
	}

	@Override
	public String repr() {
		if (isContinuousEditMode)
			return String.format(__("Continuous increase cell by %d from [%d,%d] in %s"),
					increment, startRow, startColumn, tableName);
		else
			return String.format(__("Increase cell by %d at [%d,%d] in %s"),
					increment, startRow, startColumn, tableName);
	}

}
