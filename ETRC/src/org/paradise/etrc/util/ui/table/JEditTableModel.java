package org.paradise.etrc.view.widget;

import javax.swing.table.TableModel;

interface IncrementalCellTableModel {
	/**
	 * Change the value of a cell by an increment.
	 * @param row 				row index of the cell
	 * @param column			column index of the cell
	 * @param increment		increment of change.
	 * @param isFirstInSeries	If this change is the first one in a series of changes.
	 * @return true if cell value changed.
	 */
	public boolean increaseCell(int row, int column, int increment, boolean isFirstInSeries);
	
	/**
	 * Get a copy of the value of a cell and the <b>Type</b> of the value
	 * is mutable. For example, String and Integer are both immutable, so
	 * this method should just return the object itself.
	 * @param obj
	 * @return
	 */
	public <T> T getCopyOfMutableCellValue(T obj);
	
	/**
	 * Get the increment of newObj comparing with baseObj
	 * @param baseObj
	 * @param newObj
	 * @return the increment
	 */
	public <T> int getIncrement(T baseObj, T newObj);
	
	/**
	 * Determine the going direction of changing cells.
	 * @param row 				row index of the cell
	 * @param column			column index of the cell
	 * @param increment		increment of change.
	 * @return true if next cell to be changed is below or to the right of the current cell .
	 */
	
	public boolean nextCellIsBelow(int row, int column, int increment);
}

public interface JEditTableModel extends TableModel, IncrementalCellTableModel {

}
