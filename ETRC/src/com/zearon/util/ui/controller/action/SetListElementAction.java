package com.zearon.util.ui.controller.action;

import java.util.ArrayList;
import java.util.List;

public class SetListElementAction<T> extends UIAction {
	String valueDesc;
	List<T> list;
	List<T> oldListElements;
	List<T> listElements;
	Runnable callback;

	public SetListElementAction(String valueDesc, List<T> list, List<T> listElements,
			Runnable callback) {
		this.valueDesc = valueDesc;
		this.list = list;
		this.listElements = listElements;
		this.callback = callback;
		
		if (list.size() > 0) {
			this.oldListElements = new ArrayList<>();
			this.oldListElements.addAll(list);
		} else {
			this.oldListElements = null;
		}
	}

	@Override
	protected void _doAction() {
		list.clear();
		if (listElements != null)
			list.addAll(listElements);
		
		if (callback != null)
			callback.run();
	}

	@Override
	protected void _undoAction() {
		list.clear();
		if (listElements != null)
			list.addAll(oldListElements);
		
		if (callback != null)
			callback.run();
	}

	@Override
	protected boolean _shouldSkip() {
		return list == null;
	}

	@Override
	public String repr() {
		return valueDesc;
	}

}
