package org.paradise.etrc.controller.action;

import static org.paradise.etrc.ETRCUtil.*;

public abstract class UIAction {
	protected static int idCounter = 0;
	
	protected int id;
	
	public UIAction() {
		id = ++ id;
	}
	
	public void doAction() {
		_doAction();
		
		log("Do action: %s", repr());
	}
	
	public void undoAction() {
		_undoAction();
		
		log("Undo action: %s", repr());
	}
	
	public void redoAction() {
		_doAction();
		
		log("Undo action: %s", repr());
	}
	
	public boolean shouldSkip() {
		return _shouldSkip();
		
//		log("Undo action: %s", repr());
	}
	
	protected abstract void _doAction();
	
	protected abstract void _undoAction();
	
	protected abstract boolean _shouldSkip();
	
	public abstract String repr();
	
	protected void log(String str, Object... params) {
		DEBUG_ACTION( () -> System.err.println(String.format(str, params)) );
	}
	
	@Override
	public String toString() { return repr(); }
	
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof UIAction && ((UIAction) o).id == id;
	}
	
	@Override
	public int hashCode() {
		return id;
	}

}
