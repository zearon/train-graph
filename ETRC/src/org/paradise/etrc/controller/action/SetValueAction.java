package org.paradise.etrc.controller.action;

import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.util.ui.table.DefaultJEditTableModel;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

public class SetValueAction extends UIAction {
	String valueDesc;
	Object oldValue;
	Object newValue;
	Consumer<Object> valueSetter;
	Runnable callback;

	SetValueAction(String valueDesc, Object oldValue, Object newValue,
			Consumer<Object> valueSetter, Runnable callback) {
		this.valueDesc = valueDesc;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.valueSetter = valueSetter;
		this.callback = callback;
	}

	@Override
	protected void _doAction() {
		valueSetter.accept(newValue);
		
		if (callback != null)
			callback.run();
	}

	@Override
	protected void _undoAction() {
		valueSetter.accept(oldValue);
		
		if (callback != null)
			callback.run();
	}

	@Override
	protected boolean _shouldSkip() {
		return oldValue.equals(newValue);
	}

	@Override
	public String repr() {
		return String.format(__("Set %s from %s to %s."), valueDesc, oldValue,
				newValue);
	}

}
