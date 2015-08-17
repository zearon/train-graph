package com.zearon.util.ui.controller.action;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JTable;

import static org.paradise.etrc.ETRC.__;

public class AddTableElementAction<T> extends UIAction implements TableAction {

	JTable table;
	String tableName;
	boolean vertical;
	int index;
	T element;
	BiConsumer<Integer, T> adder;
	Consumer<Integer> remover;
	Runnable callback;

	public AddTableElementAction(String tableName, JTable table, boolean vertical,
			int index, T element, BiConsumer<Integer, T> adder,
			Consumer<Integer> remover, Runnable callback) {
		this.table = table;
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

		if (callback != null)
			callback.run();
	}

	@Override
	protected void _undoAction() {
		stopCellEditing(false);
		remover.accept(index);
		selectElement(index, vertical);
		stopCellEditing(false);

		if (callback != null)
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

	@Override
	public JTable getTable() {
		return table;
	}

}
