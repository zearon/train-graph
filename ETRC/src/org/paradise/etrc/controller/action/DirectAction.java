package org.paradise.etrc.controller.action;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Action with no undo/redo support.
 * 
 * @author Jeff Gong
 *
 */
public class DirectAction extends UIAction {
	@FunctionalInterface
	public static interface TriConsumer<T1, T2, T3> {
		void accept(T1 a1, T2 a2, T3 a3);
	}
	
	@FunctionalInterface
	public static interface MultiConsumer<T1, T2, T3, T4> {
		void accept(T1 a1, T2 a2, T3 a3, T4... a4);
	}
	
	private Runnable action;
	
	
	DirectAction(Runnable action) {
		this.action = action;
	}
	<T1> DirectAction(Consumer<T1> action, T1 a) {
		this.action = () -> action.accept(a);
	}
	<T1, T2> DirectAction(BiConsumer<T1, T2> action, T1 a1, T2 a2) {
		this.action = () -> action.accept(a1, a2);
	}
	<T1, T2, T3> DirectAction(TriConsumer<T1, T2, T3> action, T1 a1, T2 a2, T3 a3) {
		this.action = () -> action.accept(a1, a2, a3);
	}
	<T1, T2, T3, T4> DirectAction(MultiConsumer<T1, T2, T3, T4> action, T1 a1, T2 a2, T3 a3, T4... args) {
		this.action = () -> action.accept(a1, a2, a3, args);
	}

	@Override
	protected void _doAction() {
		if (action != null)
			action.run();
	}

	@Override
	protected void _undoAction() {
		// empty undo
	}
	

	@Override
	public void redoAction() {
		// Skip calling _doAction, because there is no really UN-do, 
		// there is no need to really RE-do.
	}

	@Override
	protected boolean _shouldSkip() {
		return false;
	}

	@Override
	public String repr() {
		return "Direct action with no undo/redo support.";
	}

}