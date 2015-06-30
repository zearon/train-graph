package org.paradise.etrc.controller.action;

import java.util.Arrays;
import java.util.List;

public class CompoundAction extends UIAction {
	protected List<UIAction> actions;
	Runnable callback;
	String actionDesc;

	CompoundAction(Runnable callback, String actionDesc, UIAction... actionArray) {
		this.actions = Arrays.asList(actionArray);
		this.actionDesc = actionDesc;
		this.callback = callback;
	}

	CompoundAction(Runnable callback, String actionDesc, List<UIAction> actionList) {
		this.actions = actionList;
		this.actionDesc = actionDesc;
		this.callback = callback;
	}

	@Override
	protected void _doAction() {
		if (actions == null)
			return;
		
		for (int i = 0; i < actions.size(); ++ i) {
			UIAction action = actions.get(i);
			if (!action._shouldSkip())
				action._doAction();
		}
		
		if (callback != null)
			callback.run();
	}

	@Override
	protected void _undoAction() {
		if (actions == null)
			return;
		
		for (int i = actions.size() - 1; i >= 0; -- i) {
			UIAction action = actions.get(i);
			if (!action._shouldSkip())
				action._undoAction();
		}
		
		if (callback != null)
			callback.run();
	}

	@Override
	protected boolean _shouldSkip() {
		return actions == null || actions.size() == 0 ||
				!actions.stream().anyMatch(action -> !action._shouldSkip());
	}

	@Override
	public String repr() {
		if (actionDesc == null)
			actionDesc = "";
		
		return actionDesc;
	}

}
