package org.paradise.etrc.controller.action;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JTable;

import static org.paradise.etrc.ETRC.__;

public class AddTableElementsAction<T> extends UIAction implements TableAction {

	JTable table;
	String tableName;
	boolean vertical;
	int index;
	T[] elements;
	BiConsumer<Integer, T> adder;
	Consumer<Integer> remover;
	Runnable callback;

	AddTableElementsAction(String tableName, JTable table, boolean vertical,
			int index, T[] elements, BiConsumer<Integer, T> adder,
			Consumer<Integer> remover, Runnable callback) {
		this.table = table;
		this.tableName = tableName;
		this.vertical = vertical;
		this.index = index;
		this.elements = elements;
		this.adder = adder;
		this.remover = remover;
		this.callback = callback;
	}

	@Override
	protected void _doAction() {
		stopCellEditing(false);
		for (int i = elements.length - 1; i >= 0; -- i) {
			adder.accept(index, elements[i]);
		}
		selectElements(index, index + elements.length, vertical);
		stopCellEditing(false);

		if (callback != null)
			callback.run();
	}

	@Override
	protected void _undoAction() {
		stopCellEditing(false);
		for (int i = 0; i < elements.length; ++ i) {
			remover.accept(index);
		}
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
		int elementsCount = elements.length;
		return String.format(__("Add %d %s into %s at position %d."), elements.length, 
				elementsCount > 1 ? __("elements") : __("element"), tableName, index);
	}

	@Override
	public JTable getTable() {
		return table;
	}

}
