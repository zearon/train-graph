package org.paradise.etrc.controller.action;

import java.util.function.Consumer;

import static org.paradise.etrc.ETRC.__;

public class SetValueAction<T> extends UIAction {
	String valueDesc;
	T oldValue;
	T newValue;
	Consumer<T> valueSetter;
	Runnable callback;

	SetValueAction(String valueDesc, T oldValue, T newValue,
			Consumer<T> valueSetter, Runnable callback) {
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
