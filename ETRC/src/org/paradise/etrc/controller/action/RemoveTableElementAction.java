package org.paradise.etrc.controller.action;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.util.ui.table.DefaultJEditTableModel;
import org.paradise.etrc.util.ui.table.JEditTable;
import org.paradise.etrc.view.lineedit.RailroadLineTableModel;
import org.paradise.etrc.view.lineedit.StationTableModel;

import apple.laf.JRSUIConstants.Size;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

public class RemoveTableElementAction<T> extends TableAction {

	String tableName;
	boolean vertical;
	int[] indeces;
	Object[] elements;
	IntFunction<T> getter;
	BiConsumer<Integer, T> adder;
	Consumer<Integer> remover;
	Runnable callback;

	RemoveTableElementAction(String tableName, JTable table, boolean vertical,
			int[] indeces, IntFunction<T> getter, BiConsumer<Integer, T> adder,
			Consumer<Integer> remover, Runnable callback) {
		
		super(table);
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

}
