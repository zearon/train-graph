package org.paradise.etrc.controller;

import static org.paradise.etrc.ETRC.__;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.paradise.etrc.controller.action.*;

/**
 * Keep in track of all actions that may modify the underlying model,
 * and provide do-undo-redo support for all actions.
 * @author Jeff Gong
 *
 */
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
	private int distinctActionID = 0;
	private int actionCount = 0;
	private int actionCountAtMark = 0;
	private boolean newActionCarriedOn = false;

	private LinkedList<JMenuItem> actionItemList = new LinkedList<JMenuItem> ();
	private LinkedList<JMenuItem> undoneActionItemList = new LinkedList<JMenuItem>();
	private HashMap<Integer, Boolean> actionItemIsUndoMap = new HashMap<Integer, Boolean>();

	private JMenuItem undoMenuItem;
	private JMenuItem redoMenuItem;
	private JMenu undoMenu;
	private JMenu redoMenu;
	private JButton undoButton;
	private JButton redoButton;
	
	Consumer<ActionManager> updateUIHook;

	private ActionManager() {
	}

	/**
	 * Set menu items relating to undo/redo actions.
	 * @param undoMenuItem
	 * @param redoMenuItem
	 * @param undoMenu
	 * @param redoMenu
	 */
	public void setMenuItem(JMenuItem undoMenuItem, JMenuItem redoMenuItem,
			JMenu undoMenu, JMenu redoMenu) {
		this.undoMenuItem = undoMenuItem;
		this.redoMenuItem = redoMenuItem;
		this.undoMenu = undoMenu;
		this.redoMenu = redoMenu;

		updateUI();
	}

	/**
	 * Set toolbar buttons relating to undo/redo actions;
	 * @param undoButton
	 * @param redoButton
	 */
	public void setToolbarButton(JButton undoButton, JButton redoButton) {
		this.undoButton = undoButton;
		this.redoButton = redoButton;

		updateUI();
	}
	
	/**
	 * Set a update UI hook. Every time a do/undo/redo action is done, the
	 * update UI hook is called beside updating the status of menu items
	 * and toolbar buttons.
	 * @param updateUIHook
	 */
	public void setUpdateUIHook(Consumer<ActionManager> updateUIHook) {
		this.updateUIHook = updateUIHook;
	}

	public synchronized void addActionAndDoIt(UIAction action) {
		if (action.shouldSkip())
			return;

		action.setID(++ distinctActionID);
		action.doAction();
		actionItemIsUndoMap.put(action.getID(), true);
		if ((actionCount == MAX_ACTION_COUNT && actionCount == actionCountAtMark) ||
				actionCount < actionCountAtMark)
			newActionCarriedOn = true;
		
		UIAction lastAction = actionList.peekLast();
		if (lastAction != null && lastAction.canMerge(action)) {
			lastAction.merge(action);
		} else {
			JMenuItem item = createMenuItem(action);
			if (actionCount == MAX_ACTION_COUNT) {
				actionList.removeFirst();
				actionList.addLast(action);
	
				actionItemList.removeFirst();
				actionItemList.addLast(item);
				undoMenu.remove(actionCount - 1);
				undoMenu.add(createMenuItem(action), 0);
			} else {
				actionList.addLast(action);
				++actionCount;
	
				actionItemList.addLast(item);
				undoMenu.add(item, 0);
			}
	
			undoneActionList.clear();
	
			undoneActionItemList.clear();
		}
		
		updateUI();
	}
	
	public void undo() {
		undo(true);
	}

	private synchronized void undo(boolean updateUI) {
		if (!canUndo()) {
			return;
		}

		UIAction action = actionList.pollLast();
		JMenuItem menuItem = actionItemList.pollLast();
		undoMenu.remove(menuItem);

		action.undoAction();
		actionItemIsUndoMap.put(action.getID(), false);
		actionCount --;
		
		undoneActionList.addFirst(action);
		undoneActionItemList.addFirst(menuItem);
		redoMenu.add(menuItem, 0);

		if (updateUI)
			updateUI();
	}
	
	public synchronized void redo() {
		redo(true);
	}

	private synchronized void redo(boolean updateUI) {
		if (!canRedo()) {
			return;
		}

		UIAction action = undoneActionList.pollFirst();
		JMenuItem menuItem = undoneActionItemList.pollFirst();
		redoMenu.remove(menuItem);

		action.redoAction();
		actionItemIsUndoMap.put(action.getID(), true);
		actionCount ++;

		actionList.addLast(action);
		actionItemList.addLast(menuItem);
		undoMenu.add(menuItem, 0);

		if (updateUI)
			updateUI();
	}
	
	private void batchUndo(int id) {
		
		while (canUndo() && actionList.peekLast().getID() != id) {
			undo(false);
		}
		undo(false);
	}
	
	private void batchRedo(int id) {
		
		while (canRedo() && undoneActionList.peekFirst().getID() != id) {
			redo(false);
		}
		redo(false);
	}

	public synchronized boolean canUndo() {
		return !actionList.isEmpty();
	}

	public synchronized boolean canRedo() {
		return !undoneActionList.isEmpty();
	}
	
	public boolean isModelModified() {
		return actionCountAtMark != actionCount || newActionCarriedOn;
	}
	
	public void reset() {
		actionList.clear();
		undoneActionList.clear();
		
		distinctActionID = 0;
		actionCount = 0;
		actionCountAtMark = 0;
		newActionCarriedOn = false;
		
		actionItemList.clear();
		undoneActionItemList.clear();
		
		if (undoMenu != null)
			undoMenu.removeAll();
		if (redoMenu != null)
			redoMenu.removeAll();
	}
	
	public void markModelSaved() {
		actionCountAtMark = actionCount;
		newActionCarriedOn = false;
		
		if (updateUIHook != null)
			updateUIHook.accept(this);
	}

	private JMenuItem createMenuItem(UIAction action) {
		JMenuItem menuItem = new JMenuItem(action.getReprWithID());
		menuItem.setName("" + action.getID());
		menuItem.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

		menuItem.addActionListener(this::batchRedoOrUndo);

		return menuItem;
	}
	
	private void batchRedoOrUndo(ActionEvent e) {
		JMenuItem menu = (JMenuItem) e.getSource();
		int id = Integer.parseInt(menu.getName());
		boolean isUndo = actionItemIsUndoMap.get(id);
		if (isUndo) {
			batchUndo(id);
		} else {
			batchRedo(id);
		}
		
		updateUI();
	}

	private void updateUI() {
		boolean canUndo = canUndo();
		boolean canRedo = canRedo();

		if (undoMenuItem != null)
			undoMenuItem.setEnabled(canUndo);
		if (redoMenuItem != null)
			redoMenuItem.setEnabled(canRedo);
		
		if (undoMenu != null)
			undoMenu.setEnabled(canUndo);
		if (redoMenu != null)
			redoMenu.setEnabled(canRedo);

		if (undoButton != null) {
			undoButton.setEnabled(canUndo);
			
			UIAction nextUndoAction = actionList.peekLast();
			String undoTooltip = nextUndoAction == null ? __("Undo") : String
					.format(__("Undo %s"), nextUndoAction.getReprWithID());
			undoButton.setToolTipText(undoTooltip);
		}
		if (redoButton != null) {
			redoButton.setEnabled(canRedo);
			
			UIAction nextRedoAction = undoneActionList.peekFirst();
			String redoTooltip = nextRedoAction == null ? __("Redo") : String
					.format(__("Redo %s"), nextRedoAction.getReprWithID());
			redoButton.setToolTipText(redoTooltip);
		}
		
		if (updateUIHook != null)
			updateUIHook.accept(this);
	}

}
