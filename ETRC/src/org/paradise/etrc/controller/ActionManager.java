package org.paradise.etrc.controller;

import static org.paradise.etrc.ETRC.__;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.paradise.etrc.controller.action.*;

public class ActionManager {
	public static final int MAX_ACTION_COUNT = 100;

	static ActionManager instance;

	public static synchronized ActionManager getInstance() {
		if (instance == null) {
			instance = new ActionManager();
		}

		return instance;
	}

	private LinkedList<UIAction> actionList = new LinkedList<UIAction>();
	private LinkedList<UIAction> undoneActionList = new LinkedList<UIAction>();
	private int actionCount = 0;

	private LinkedList<JMenuItem> undoneActionItemList = new LinkedList<JMenuItem>();

	private JMenuItem undoMenuItem;
	private JMenuItem redoMenuItem;
	private JMenuItem actionHistoryMenuItem;
	private JButton undoButton;
	private JButton redoButton;

	private ActionManager() {
	}

	public void setMenuItem(JMenuItem undoMenuItem, JMenuItem redoMenuItem,
			JMenu actionHistoryMenuItem) {
		this.undoMenuItem = undoMenuItem;
		this.redoMenuItem = redoMenuItem;
		this.actionHistoryMenuItem = actionHistoryMenuItem;

		updateUI();
	}

	public void setToolbarButton(JButton undoButton, JButton redoButton) {
		this.undoButton = undoButton;
		this.redoButton = redoButton;

		updateUI();
	}

	public synchronized void addActionAndDoIt(UIAction action) {
		if (action.shouldSkip())
			return;

		if (actionCount == MAX_ACTION_COUNT) {
			actionList.removeFirst();
			actionList.addLast(action);

			// actionHistoryMenuItem.remove(actionCount - 1);
			// actionHistoryMenuItem.add(createMenuItem(action), 0);
		} else {
			actionList.addLast(action);
			++actionCount;

			// actionHistoryMenuItem.add(createMenuItem(action), 0);
		}

		action.doAction();

		undoneActionList.clear();

		// undoneActionItemList.clear();

		updateUI();
	}

	public synchronized void undo() {
		if (!canUndo()) {
			return;
		}

		UIAction action = actionList.pollLast();
		// JMenuItem menuItem = (JMenuItem)
		// actionHistoryMenuItem.getComponent(0);

		action.undoAction();

		undoneActionList.addFirst(action);
		// undoneActionItemList.addFirst(menuItem);

		updateUI();
	}

	public synchronized void redo() {
		if (!canRedo()) {
			return;
		}

		UIAction action = undoneActionList.pollFirst();
		// JMenuItem menuItem = undoneActionItemList.pollFirst();

		action.redoAction();

		actionList.addLast(action);
		// actionHistoryMenuItem.add(menuItem, 0);

		updateUI();
	}

	public synchronized boolean canUndo() {
		return !actionList.isEmpty();
	}

	public synchronized boolean canRedo() {
		return !undoneActionList.isEmpty();
	}

	private JMenuItem createMenuItem(UIAction action) {
		JMenuItem menuItem = new JMenuItem(action.repr());
		menuItem.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

		// menuItem.addActionListener(this);

		return menuItem;
	}

	private void updateUI() {
		boolean canUndo = canUndo();
		boolean canRedo = canRedo();

		if (undoMenuItem != null)
			undoMenuItem.setEnabled(canUndo);
		if (redoMenuItem != null)
			redoMenuItem.setEnabled(canRedo);

		if (undoButton != null) {
			undoButton.setEnabled(canUndo);
			
			UIAction nextUndoAction = actionList.peekLast();
			String undoTooltip = nextUndoAction == null ? __("Undo") : String
					.format(__("Undo %s"), nextUndoAction.repr());
			undoButton.setToolTipText(undoTooltip);
		}
		if (redoButton != null) {
			redoButton.setEnabled(canRedo);
			
			UIAction nextRedoAction = undoneActionList.peekFirst();
			String redoTooltip = nextRedoAction == null ? __("Redo") : String
					.format(__("Redo %s"), nextRedoAction.repr());
			redoButton.setToolTipText(redoTooltip);
		}
	}

}
