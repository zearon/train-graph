package com.zearon.util.ui.controller.action;

import com.zearon.util.ui.controller.ActionManager;

import static com.zearon.util.debug.DebugUtil.DEBUG_ACTION;

import static org.paradise.etrc.ETRC.__;

public abstract class UIAction {
	protected static int idCounter = 0;
	
	protected int id;
	protected boolean done = false;
	
	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return this.id;
	}
	
	public boolean isActionDone() {
		return done;
	}
	
	public void setActionDone(boolean done) {
		this.done = done;
	}
	
	public UIAction addToManagerAndDoIt() {
		ActionManager.getInstance().addActionAndDoIt(this);
		return this;
	}
	
	public boolean doAction() {
		try {
			_doAction();
			done = true;
			log("Do action: %s", repr());
			return true;
		} catch (Exception e) {
			log("Error occurs when doing action: %s", repr());
			e.printStackTrace();
			return false;
		}
	}
	
	public void undoAction() {
		_undoAction();
		done = false;
		
		log("Undo action: %s", repr());
	}
	
	public void redoAction() {
		_doAction();
		done = true;
		
		log("Undo action: %s", repr());
	}
	
	public boolean shouldSkip() {
		return _shouldSkip();
		
//		log("Undo action: %s", repr());
	}
	
	public String getReprWithID() {
		return String.format(__("action %d@%s"), id, repr());
	}
	
	protected abstract void _doAction();
	
	protected abstract void _undoAction();
	
	protected abstract boolean _shouldSkip();
	
	public boolean canMerge(UIAction action2) { return false; }
	public void merge(UIAction action2) {}
	
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
