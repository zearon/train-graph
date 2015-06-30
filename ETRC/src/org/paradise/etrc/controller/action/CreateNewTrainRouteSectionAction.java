package org.paradise.etrc.controller.action;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JTable;

public class CreateNewTrainRouteSectionAction extends CompoundAction {
	private Runnable callback;
	ChangeTableSelectionAction selectionAction;
	
	<T, P> CreateNewTrainRouteSectionAction(String actionDesc, JTable table, boolean isRow,
			int index, T element, BiConsumer<Integer, T> adder,
			Consumer<Integer> remover, P oldValue, P newValue,
			Consumer<P> valueSetter, Runnable callback) {
		
		super(null, actionDesc, new Vector<UIAction>(2));
		
		AddTableElementAction<T> action1 = new AddTableElementAction<T>(
				"", table, isRow, index, element, adder, remover,
				null);
		actions.add(action1);
		
		SetValueAction<P> action2 = new SetValueAction<P>("", oldValue, newValue, valueSetter, null);
		actions.add(action2);
		
		selectionAction = new ChangeTableSelectionAction(table, isRow, new int[]{index}, true);
		
		this.callback = callback;
	}

	@Override
	protected void _doAction() {
		super._doAction();
		
		if (callback != null)
			callback.run();
		
		selectionAction._doAction();
	}

	@Override
	protected void _undoAction() {
		super._undoAction();
		
		if (callback != null)
			callback.run();

		selectionAction._undoAction();
	}

	@Override
	protected boolean _shouldSkip() {
		return false;
	}

	@Override
	public String repr() {
		return super.repr();
	}

}
