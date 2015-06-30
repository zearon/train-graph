package org.paradise.etrc.controller.action;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import javax.swing.JTable;

import static org.paradise.etrc.ETRC.__;

public class RemoveTableElementsAction<T> extends UIAction implements TableAction {
	JTable table;
	String tableName;
	boolean vertical;
	int[] indeces;
	Object[] elements;
	IntFunction<T> getter;
	BiConsumer<Integer, T> adder;
	Consumer<Integer> remover;
	Runnable callback;

	RemoveTableElementsAction(String tableName, JTable table, boolean vertical,
			int[] indeces, IntFunction<T> getter, BiConsumer<Integer, T> adder,
			Consumer<Integer> remover, Runnable callback) {
		
		this.table = table;
		this.tableName = tableName;
		this.vertical = vertical;
		this.indeces = Arrays.stream(indeces).sorted().toArray();
		elements = Arrays.stream(this.indeces).mapToObj(index->getter.apply(index))
				.toArray();
		this.adder = adder;
		this.remover = remover;
		this.callback = callback;
	}

	@Override
	protected void _doAction() {
		stopCellEditing(false);
		for (int i = indeces.length - 1; i >= 0; -- i) {
			int index = indeces[i];
			remover.accept(index);
		}
//		fireTableChanged();
		stopCellEditing(false);

		callback.run();
	}

	@Override
	protected void _undoAction() {
		stopCellEditing(false);
		for (int i = 0; i < indeces.length; ++ i) {
			int index = indeces[i];
			@SuppressWarnings("unchecked")
			T e = (T) elements[i];
			adder.accept(index, e);
		}
//		fireTableChanged();
		stopCellEditing(false);

		callback.run();
	}

	@Override
	protected boolean _shouldSkip() {
		return false;
	}

	@Override
	public String repr() {
		return String.format(__("Remove element at position [%d-%d] from %s."),
				indeces[0], indeces[indeces.length - 1], tableName);
	}

	@Override
	public JTable getTable() {
		return table;
	}

}
