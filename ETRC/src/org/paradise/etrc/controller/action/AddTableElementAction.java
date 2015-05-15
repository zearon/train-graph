package org.paradise.etrc.controller.action;

import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.util.ui.table.DefaultJEditTableModel;
import org.paradise.etrc.util.ui.table.JEditTable;
import org.paradise.etrc.view.lineedit.RailroadLineTableModel;
import org.paradise.etrc.view.lineedit.StationTableModel;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

public class AddTableElementAction<T> extends TableAction {

	String tableName;
	boolean vertical;
	int index;
	T element;
	BiConsumer<Integer, T> adder;
	Consumer<Integer> remover;
	Runnable callback;

	AddTableElementAction(String tableName, JTable table, boolean vertical,
			int index, T element, BiConsumer<Integer, T> adder,
			Consumer<Integer> remover, Runnable callback) {
		super(table);
		this.tableName = tableName;
		this.vertical = vertical;
		this.index = index;
		this.element = element;
		this.adder = adder;
		this.remover = remover;
		this.callback = callback;
	}

	@Override
	protected void _doAction() {
		stopCellEditing(false);
		adder.accept(index, element);
		selectElement(index, vertical);
		stopCellEditing(false);

		callback.run();
	}

	@Override
	protected void _undoAction() {
		stopCellEditing(false);
		remover.accept(index);
		selectElement(index, vertical);
		stopCellEditing(false);

		callback.run();
	}

	@Override
	protected boolean _shouldSkip() {
		return false;
	}

	@Override
	public String repr() {
		return String.format(__("Add element {%s} into %s at position %d."),
				element, tableName, index);
	}

}
