package org.paradise.etrc.controller.action;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.swing.JTable;

import org.paradise.etrc.data.v1.TrainRouteSection;
import org.paradise.etrc.view.timetableedit.TimetableEditSheetModel.PasteParameters;

public class PasteTrainRouteSectionAction extends CompoundAction {
	private Runnable callback;
	private ChangeTableSelectionAction selectionAction;
	private int pastedItemCount;
	
	PasteTrainRouteSectionAction(String actionDesc, JTable table,
			boolean isRow, int index, TrainRouteSection[] trains,
			BiConsumer<Integer, TrainRouteSection> adder,
			Consumer<Integer> remover, PasteParameters oldValue,
			PasteParameters newValue, Consumer<PasteParameters> valueSetter,
			Runnable callback) {
		
		super(null, actionDesc, new Vector<UIAction>(2));
		
		// Copy train route sections
		pastedItemCount = trains.length;
		TrainRouteSection[] copiedTrains = new TrainRouteSection[pastedItemCount];
		for (int i = 0; i < pastedItemCount; ++ i) {
			copiedTrains[i] = trains[i].prepareCloneForPaste(newValue.times, newValue.offset);
		}
		
		SetValueAction<PasteParameters> action1 = new SetValueAction<>("",
				oldValue, newValue, valueSetter, null);

		AddTableElementsAction<TrainRouteSection> action2 = new AddTableElementsAction<>(
				"", table, isRow, index, copiedTrains, adder, remover,
				null);
		
		actions.add(action1);
		actions.add(action2);
		
		int[] indexes = IntStream.range(index, index + pastedItemCount).toArray();
		selectionAction = new ChangeTableSelectionAction(table, isRow, indexes, true);
		
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
		return pastedItemCount < 1;
	}

	@Override
	public String repr() {
		return super.repr();
	}

}
