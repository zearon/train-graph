package org.paradise.etrc.util.ui.table;

import static org.paradise.etrc.ETRC.__;
import static org.paradise.etrc.ETRCUtil.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.NumberFormat;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import org.paradise.etrc.controller.action.ActionFactory;

/**
 * A table suitable for massive data input.
 * 
 * @author Jeff Gong
 *
 */
public class JEditTable extends JTable {

	public static Color onColor;
	public static Color offColor;

	private boolean editLocked = false;
	private boolean isContinuousEditMode = true;
	private boolean isFocusVMove = true;
	private JPanel containerPanel;
	private JScrollPane scrollPane;
	private JPanel statusBar;
	private JPopupMenu popupMenu;
	private JMenuItem miContinuousEdit;
	private JMenuItem miFocusVMove;
	private JMenuItem miLockEdit;
	private JTextField tfIncrement;
	private JButton btAppyIncrement;
	private JLabel lbLockEdit;
	private JLabel lbContinuousEdit;
	private JLabel lbFocusVMove;
	// private Color origGridColor;
	private Color continuousInputGridColor = Color.decode("0xC3CFFE");

	private IncrementalCellTableModel incrementalModel;
	private Object origCellValue;
	private boolean cellChanged;

	private HashSet<Integer> registeredCellEditor = new HashSet<>(100);
	private boolean parentListenerAdded = false;
	private MouseAdapter contextMenuAdapter;
	private MouseWheelListener wheelAdapter;
	
	private String tableName;

	static {
		onColor = Color.decode("0xB3FBAD");
		offColor = Color.decode("0xFF9BB1");
	}

	public JEditTable(String tableName) {
		this(tableName, false);
	}

	public JEditTable(String tableName, boolean continuousEdit) {
		this.isContinuousEditMode = continuousEdit;
		setTableName(tableName);
		initUI();
	}

	public JEditTable(String tableName, JEditTableModel dm) {
		super(dm);
		setTableName(tableName);
		incrementalModel = dm;
		initUI();
	}

	public JEditTable(String tableName, JEditTableModel dm, TableColumnModel cm) {
		super(dm, cm);
		setTableName(tableName);
		incrementalModel = dm;
		initUI();
	}

	public JEditTable(String tableName, int numRows, int numColumns) {
		super(numRows, numColumns);
		setTableName(tableName);
		initUI();
	}

	public JEditTable(String tableName, Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
		setTableName(tableName);
		initUI();
	}

	public JEditTable(String tableName, Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
		setTableName(tableName);
		initUI();
	}

	public JEditTable(String tableName, JEditTableModel dm, TableColumnModel cm,
			ListSelectionModel sm) {
		super(dm, cm, sm);
		setTableName(tableName);
		incrementalModel = dm;
		initUI();
	}
	
	protected void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Property getter
	 * 
	 * @return
	 */
	protected IncrementalCellTableModel getIncrementalModel() {
		if (incrementalModel == null) {
			RuntimeException exception = new IllegalArgumentException(
					"The table model of JEditTable must be an instace of interface JEditTableModel");
			exception.printStackTrace();
			throw exception;
		}

		return incrementalModel;
	}

	public JPanel getContainerPanel() {
		return containerPanel;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public void addToStatusBar(Component component) {
		statusBar.add(component);
	}

	protected void initUI_buildContainerPanel() {
		containerPanel = new JPanel(new BorderLayout());
		containerPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		scrollPane = new JScrollPane(this);
		scrollPane.getViewport().addMouseListener(contextMenuAdapter);
		scrollPane.addMouseListener(contextMenuAdapter);

		containerPanel.add(scrollPane, BorderLayout.CENTER);
	}

	protected void initUI_buildStatusBar() {
		statusBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 0));

		NumberFormat incrementFormat = NumberFormat.getIntegerInstance();
		incrementFormat.setMaximumFractionDigits(0);
		tfIncrement = new JFormattedTextField(incrementFormat);
		tfIncrement.setText("0");
		tfIncrement.setColumns(2);
		tfIncrement.setMargin(new Insets(0, 10, 0, 10));

		btAppyIncrement = new JButton(__("A.I."));
		btAppyIncrement
				.setToolTipText(__("<html>Apply Increment. <br/>First select a cell in the table, and then press this button. <br/>You can also rotate your mouse wheel <br/>while hold CTRL key to apply increments.</html>"));
		btAppyIncrement.setPreferredSize(new Dimension(25, 20));
		btAppyIncrement.addActionListener(e -> {
			int startRow = getSelectedRow();
			int startColumn = getSelectedColumn();
			if (startRow >= 0 && startRow < getRowCount() && startColumn >= 0
					&& startColumn < getColumnCount()) {
				int increment = Integer.parseInt(tfIncrement.getText());
				doContinuousIncreaseCell(startRow, startColumn, increment,
						false, false);
			}
		});

		lbLockEdit = new JLabel(__("L.E."));
		lbLockEdit.setToolTipText(__("<html>Lock Edit<br/>Lock table for editing for data secure<br/>in case that accidental changes are made.</html>"));
		lbLockEdit.setOpaque(true);
		lbLockEdit.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));
		lbLockEdit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				toggleEditLocked();
			}
		});

		lbContinuousEdit = new JLabel(__("C.E."));
		lbContinuousEdit.setToolTipText(__("<html>Continuous Edit<br/>A change applied to a cell will also <br>apply to all its following cells.</html>"));
		lbContinuousEdit.setOpaque(true);
		lbContinuousEdit.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));
		lbContinuousEdit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				toggleContinuousEditMode();
			}
		});

		lbFocusVMove = new JLabel(__("F.M.V."));
		lbFocusVMove.setToolTipText(__("<html>Focus Move Vertically<br/>When this mode is on, the focus will move to the cell below " + 
				"<br/>the cell in edit if you press Enter key. <br/>Otherwise, the focus will move to the cell to the right of <br/>" + 
				"the cell in edit.</html>"));
		lbFocusVMove.setOpaque(true);
		lbFocusVMove.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));
		lbFocusVMove.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				toggleFocusVMove();
			}
		});

		statusBar.add(tfIncrement);
		statusBar.add(btAppyIncrement);
		statusBar.add(lbLockEdit);
		statusBar.add(lbContinuousEdit);
		statusBar.add(lbFocusVMove);

		containerPanel.add(statusBar, BorderLayout.SOUTH);
	}

	/**
	 * 初始化控件
	 */
	protected void initUI() {
		setColumnSelectionAllowed(true);
		setGridColor(continuousInputGridColor);

		initUI_buildContainerPanel();
		initUI_buildStatusBar();

		addContextMenu();
		setSelectionBehavior();
		setKeyActonListen();
	}

	/**
	 * 添加右键菜单
	 */
	protected void addContextMenu() {
		isContinuousEditMode = false;

		popupMenu = new JPopupMenu();
		popupMenu.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		popupMenu.setBackground(onColor);

		miLockEdit = createMenuItem(__("Lock Edit"), e -> toggleEditLocked());
		popupMenu.add(miLockEdit);
		setEditLocked(editLocked);

		// continuousInputMenuItem = createMenuItem(
		// __("Toggle continuous input mode"),
		// e -> toggleContinuousInputMode());
		// popupMenu.add(continuousInputMenuItem);
		// setContinuousInputMode(isContinuousInputMode);

		miContinuousEdit = createMenuItem(__("Toggle continuous edit mode"),
				e -> toggleContinuousEditMode());
		popupMenu.add(miContinuousEdit);
		setContinuousEditMode(isContinuousEditMode);
		// popupMenu.add(new JSeparator(JSeparator.HORIZONTAL));

		miFocusVMove = createMenuItem(
				__("Toggle focus vertical move direction"),
				e -> toggleFocusVMove());
		popupMenu.add(miFocusVMove);
		setFocusVMove(isFocusVMove);

		// Show context menu
		contextMenuAdapter = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int mods = e.getModifiers();
				// 鼠标右键
				if ((mods & InputEvent.BUTTON3_MASK) != 0) {
					// 弹出菜单
					Point point = getLocationOnScreen();
					popupMenu.show(JEditTable.this, e.getXOnScreen() - point.x,
							e.getYOnScreen() - point.y);
				}
			}

		};
		wheelAdapter = new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					int increment = e.getWheelRotation();
					int row = getEditingRow(), column = getEditingColumn();
					if (row >= 0 && column >= 0)
						doContinuousIncreaseCell(row, column, increment, false, true);
					e.consume();
				} else
					getParent().dispatchEvent(e);
			}
		};
		addMouseListener(contextMenuAdapter);
		// addMouseWheelListener(wheelAdapter);
		// getTableHeader().addMouseListener(contextMenuAdapter);
	}

	/**
	 * 设置单元格选中行为
	 */
	private void setSelectionBehavior() {

		// setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ListSelectionListener listener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// if (!isContinuousInputMode)
				// return;

				if (getCellEditor() != null)
					getCellEditor().stopCellEditing();

				editCellAt(JEditTable.this.getSelectedRow(),
						JEditTable.this.getSelectedColumn(), null);
			}
		};

		getSelectionModel().addListSelectionListener(listener);
		getColumnModel().getSelectionModel().addListSelectionListener(listener);
	}

	/**
	 * 创建右键菜单的菜单项
	 * 
	 * @param name
	 * @param listener
	 * @return
	 */
	private JMenuItem createMenuItem(String name, ActionListener listener) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

		// menuItem.setActionCommand(actionCommand);
		if (listener != null)
			menuItem.addActionListener(listener);

		return menuItem;
	}

	public enum KeyAction {
		ENTER, ARROW_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT
	}

	/**
	 * 设置JEditTable的键盘事件处理程序
	 */
	protected void setKeyActonListen() {
		setKeyStrokeActon(this, KeyEvent.VK_ENTER, 0, KeyAction.ENTER);
		setKeyStrokeActon(this, KeyEvent.VK_UP, 0, KeyAction.ARROW_UP);
		setKeyStrokeActon(this, KeyEvent.VK_DOWN, 0, KeyAction.ARROW_DOWN);
		setKeyStrokeActon(this, KeyEvent.VK_LEFT, 0, KeyAction.ARROW_LEFT);
		setKeyStrokeActon(this, KeyEvent.VK_RIGHT, 0, KeyAction.ARROW_RIGHT);
	}

	protected void setKeyStrokeActon(JComponent comp, int keycode,
			int modifier, KeyAction action) {
		comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(keycode, modifier), action.name());
		comp.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(keycode, modifier), action.name());

		comp.getActionMap().put(action.name(), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				keyboardMovingAction(action);
			}
		});
	}

	protected void keyboardMovingAction(KeyAction action) {
		int rowSelected = getSelectedRow();
		int rowCount = getRowCount();
		int columnSelected = getSelectedColumn();
		int columnCount = getColumnCount();

		if (rowSelected >= 0 && rowSelected < rowCount && columnSelected >= 0
				&& columnSelected < columnCount) {

			int[] rowNcolumn = new int[] { rowSelected, columnSelected };

			switch (action) {
			case ENTER:
				getSelctedCellByEnter(rowNcolumn, rowCount, columnCount);
				break;
			case ARROW_UP:
				getSelectedCellByArrowUp(rowNcolumn, rowCount, columnCount);
				break;
			case ARROW_DOWN:
				getSelectedCellByArrowDown(rowNcolumn, rowCount, columnCount);
				break;
			case ARROW_LEFT:
				getSelectedCellByArrowLeft(rowNcolumn, rowCount, columnCount);
				break;
			case ARROW_RIGHT:
				getSelectedCellByArrowRight(rowNcolumn, rowCount, columnCount);
				break;
			default:
				break;
			}

//			if (getCellEditor() != null)
//				getCellEditor().stopCellEditing();
			int nextRow = rowNcolumn[0], nextColumn = rowNcolumn[1];
			changeSelection(nextRow, nextColumn, false, false);
			editCellAt(nextRow, nextColumn, null);
		}
	}

	protected void getSelctedCellByEnter(int[] rowNcolumn, int rowCount,
			int columnCount) {

		if (isFocusVMove) {
			// Move down
			getSelectedCellByArrowDown(rowNcolumn, rowCount, columnCount);
		} else {
			// Move rightward
			getSelectedCellByArrowRight(rowNcolumn, rowCount, columnCount);
		}
	}

	protected void getSelectedCellByArrowUp(int[] rowNcolumn, int rowCount,
			int columnCount) {
		int rowSelected = rowNcolumn[0];
		int columnSelected = rowNcolumn[1];
		int nextRow, nextColumn;

		// Move up
		if (rowSelected > 0) {
			nextRow = rowSelected - 1;
			nextColumn = columnSelected;
		} else {
			nextRow = rowCount - 1;
			nextColumn = (columnSelected - 1 + columnCount) % columnCount;
		}

		rowNcolumn[0] = nextRow;
		rowNcolumn[1] = nextColumn;
	}

	protected void getSelectedCellByArrowDown(int[] rowNcolumn, int rowCount,
			int columnCount) {
		int rowSelected = rowNcolumn[0];
		int columnSelected = rowNcolumn[1];
		int nextRow, nextColumn;

		// Move down
		if (rowSelected < rowCount - 1) {
			nextRow = rowSelected + 1;
			nextColumn = columnSelected;
		} else {
			nextRow = 0;
			nextColumn = (columnSelected + 1) % columnCount;
		}

		rowNcolumn[0] = nextRow;
		rowNcolumn[1] = nextColumn;
	}

	protected void getSelectedCellByArrowLeft(int[] rowNcolumn, int rowCount,
			int columnCount) {
		int rowSelected = rowNcolumn[0];
		int columnSelected = rowNcolumn[1];
		int nextRow, nextColumn;

		// Move left
		if (columnSelected > 0) {
			nextRow = rowSelected;
			nextColumn = columnSelected - 1;
		} else {
			nextRow = (rowSelected - 1 + rowCount) % rowCount;
			nextColumn = columnCount - 1;
		}

		rowNcolumn[0] = nextRow;
		rowNcolumn[1] = nextColumn;
	}

	protected void getSelectedCellByArrowRight(int[] rowNcolumn, int rowCount,
			int columnCount) {
		int rowSelected = rowNcolumn[0];
		int columnSelected = rowNcolumn[1];
		int nextRow, nextColumn;

		// Move right
		if (columnSelected < columnCount - 1) {
			nextRow = rowSelected;
			nextColumn = columnSelected + 1;
		} else {
			nextRow = (rowSelected + 1) % rowCount;
			nextColumn = 0;
		}

		rowNcolumn[0] = nextRow;
		rowNcolumn[1] = nextColumn;
	}

	/**
	 * 获取编辑是否被锁定
	 * 
	 * @return
	 */
	public boolean isEditLocked() {
		return editLocked;
	}

	/**
	 * 设置编辑锁定状态
	 * 
	 * @param locked
	 */
	public void setEditLocked(boolean locked) {
		editLocked = locked;

		miLockEdit.setBackground(editLocked ? offColor : onColor);
		lbLockEdit.setBackground(editLocked ? offColor : onColor);

		if (locked) {
			if (getCellEditor() != null)
				getCellEditor().cancelCellEditing();
		}
	}

	/**
	 * 切换编辑锁定状态
	 */
	public void toggleEditLocked() {
		setEditLocked(!editLocked);
	}

	/**
	 * 获取是否设置为连续输入模式
	 * 
	 * @return
	 */
	// public boolean isConinuousInputMode() {
	// return this.isContinuousInputMode;
	// }

	/**
	 * 设置连续输入模式
	 * 
	 * @param mode
	 */
	// public void setContinuousInputMode(boolean mode) {
	// isContinuousInputMode = mode;
	// continuousInputMenuItem.setBackground(isContinuousInputMode ? onColor
	// : offColor);
	//
	// if (origGridColor == null)
	// origGridColor = getGridColor();
	//
	// if (isContinuousInputMode) {
	// setGridColor(continuousInputGridColor);
	// setColumnSelectionAllowed(true);
	// } else {
	// if (origGridColor != null)
	// setGridColor(origGridColor);
	// setColumnSelectionAllowed(false);
	// }
	//
	// updateUI();
	// }

	/**
	 * 切换连续输入模式
	 */
	// public void toggleContinuousInputMode() {
	// setContinuousInputMode(!isContinuousInputMode);
	// }

	/**
	 * 获取是否设置为连续编辑模式
	 * 
	 * @return
	 */
	public boolean isConinuousEditMode() {
		return this.isContinuousEditMode;
	}

	/**
	 * 设置连续编辑模式
	 * 
	 * @param mode
	 */
	public void setContinuousEditMode(boolean mode) {
		isContinuousEditMode = mode;
		miContinuousEdit.setBackground(isContinuousEditMode ? onColor
				: offColor);
		lbContinuousEdit.setBackground(isContinuousEditMode ? onColor
				: offColor);
	}

	/**
	 * 切换连续编辑模式
	 */
	public void toggleContinuousEditMode() {
		setContinuousEditMode(!isContinuousEditMode);
	}

	/**
	 * 获取是否设置为焦点向下移动模式
	 * 
	 * @return
	 */
	public boolean isFocusVMove() {
		return this.isFocusVMove;
	}

	/**
	 * 设置焦点向下移动模式
	 * 
	 * @param yesorno
	 *            true为向下移动;false为向右移动
	 */
	public void setFocusVMove(boolean yesorno) {
		isFocusVMove = yesorno;
		miFocusVMove.setBackground(isFocusVMove ? onColor : offColor);
		lbFocusVMove.setBackground(isFocusVMove ? onColor : offColor);
	}

	/**
	 * 切换焦点向下移动模式
	 */
	public void toggleFocusVMove() {
		setFocusVMove(!isFocusVMove);
	}

	/**
	 * 设置包含该JEditTable的JScrollPane
	 * 
	 * @param parentScrollPane
	 */
	public void setParentScrollPane(JScrollPane parentScrollPane) {
		this.scrollPane = parentScrollPane;
	}

	/**
	 * 重载函数.用于为单元格编辑器添加响应上下左右以及回车等按键事件的事件处理程序
	 */
	@Override
	public boolean editCellAt(int row, int column) {
		return editCellAt(row, column, null);
	}

	/**
	 * 重载函数.用于为单元格编辑器添加响应上下左右以及回车等按键事件的事件处理程序
	 */
	@Override
	public boolean editCellAt(int row, int column, EventObject e) {
		// DEBUG_STACKTRACE(5, "Edit cell (%d,%d)", row, column);
		if (editLocked)
			return false;

		if (row < 0 || column < 0)
			return false;

		// Save original cell value.
		origCellValue = getModel().getValueAt(row, column);
		origCellValue = getIncrementalModel().getCopyOfMutableCellValue(
				origCellValue);
		cellChanged = false;
//		DEBUG_STACKTRACE(10,"EDIT: CELL(%d,%d) = %s", row, column, origCellValue);

		boolean result = super.editCellAt(row, column, e);
		final JComponent editor = (JComponent) getEditorComponent();
		if (editor == null || !(editor instanceof JTextComponent)) {
			return result;
		}

		// DEBUG_STACKTRACE(0, "#Editing cell (%d,%d)", row, column);
		editor.requestFocus();
		setCellEditorEventListener(editor);
		if (e instanceof MouseEvent) {
			EventQueue.invokeLater(() -> {
				((JTextComponent) editor).selectAll();
			});
		} else {
			((JTextComponent) editor).selectAll();
		}
		return result;
	}

	/**
	 * 重载函数. 用于连续编辑后续单元格
	 */
	@Override
	public void editingStopped(ChangeEvent e) {
		int row = getEditingRow(), column = getEditingColumn();
		super.editingStopped(e);

		if (row < 0 || column < 0 || cellChanged)
			return;

		// DEBUG("row=%d, column=%d", row, column);
		Object newCellValue = getModel().getValueAt(row, column);

		int increment = getIncrementalModel().getIncrement(origCellValue,
				newCellValue);

//		DEBUG("EDIT Stoped: CELL(%d,%d) = %s, oldValue=%s, inc=%d", row, column, newCellValue, origCellValue, increment);
		if (increment != 0)
			doContinuousIncreaseCell(row, column, increment, true, false);		

		cellChanged = true;
	}

	@Override
	public void setModel(TableModel tableModel) {
		if (tableModel instanceof JEditTableModel)
			incrementalModel = (JEditTableModel) tableModel;

		super.setModel(tableModel);
	}

	/**
	 * Change the object in model that is corresponding with the editing row
	 * 
	 * @param startRow
	 * @param startColumn
	 * @param increment
	 * @param skipTheFirst
	 * @param mouseAction
	 */
	public void doContinuousIncreaseCell(int startRow, int startColumn,
			int increment, boolean skipTheFirst, boolean mouseAction) {
		
		ActionFactory.createTableCellIncrementActionAndDoIt(tableName, 
				this, startRow, startColumn, increment, skipTheFirst, 
				isContinuousEditMode, mouseAction);
	}
	
	public void _doContinuousIncreaseCell(int startRow, int startColumn,
			int increment, boolean skipTheFirst, boolean isContinuousEditMode) {
		boolean valueChanged = false;

		if (!skipTheFirst) {
			Object obj = getModel().getValueAt(startRow, startColumn);
			valueChanged = getIncrementalModel().increaseCell(startRow,
					startColumn, increment, true);
			obj = getModel().getValueAt(startRow, startColumn);

			if (valueChanged) {
				if (getCellEditor() != null)
					getCellEditor().cancelCellEditing();
				editCellAt(startRow, startColumn, null);
			}
		}

		if (!isContinuousEditMode)
			return;

		int rowCount = getRowCount();
		int columnCount = getColumnCount();

		valueChanged = false;
		int rowColIndex = startRow + 1;
		Predicate<Integer> loopPredicate = var -> var < rowCount;
		boolean moveDown = getIncrementalModel().nextCellIsBelow(startRow,
				startColumn, increment);

		if (!moveDown) {
			rowColIndex = startRow - 1;
			loopPredicate = var -> var >= 0;
		}

		for (; loopPredicate.test(rowColIndex);) {

			if (increaseCell(rowColIndex, startColumn, startRow, startColumn,
					increment))
				valueChanged = true;

			if (moveDown)
				++rowColIndex;
			else
				--rowColIndex;
		}

		if (valueChanged)
			updateUI();
	}
	
	/**
	 * Change a batch of values in the table
	 * @param increment How much to be changed on cells in the table.
	 * @param start Start index.
	 * @param end End index.
	 * @param coord 
	 * @param vertical If true then start and end are row indexes and coord are column index.
	 * Otherwise, start and end are column indexes and coord are row index.
	 */
	public void batchChangeValue(int increment, int start, int end, int coord,
			boolean vertical) {
		
		IncrementalCellTableModel model = getIncrementalModel();
		if (vertical) {
			for (int row = start; row <= end; ++ row) {
				model.increaseCell(row, coord, increment, false);
			}
		} else {
			for (int column = start; column <= end; ++ column) {
				model.increaseCell(coord, column, increment, false);
			}
		}
	}

	protected boolean increaseCell(int row, int column, int startRow,
			int startColumn, int increment) {

		return getIncrementalModel()
				.increaseCell(row, column, increment, false);
	}

	/**
	 * 设置单元格编辑器的事件响应程序
	 * 
	 * @param editor
	 */
	private void setCellEditorEventListener(JComponent editor) {
		if (editor == null || !(editor instanceof JTextComponent)) {
			return;
		}

		int editorID = System.identityHashCode(editor);
		if (registeredCellEditor.contains(editorID))
			return;
		else
			registeredCellEditor.add(editorID);

		editor.addMouseListener(contextMenuAdapter);
		editor.addMouseWheelListener(wheelAdapter);

		setKeyStrokeActon(editor, KeyEvent.VK_ENTER, 0, KeyAction.ENTER);
		setKeyStrokeActon(editor, KeyEvent.VK_UP, 0, KeyAction.ARROW_UP);
		setKeyStrokeActon(editor, KeyEvent.VK_DOWN, 0, KeyAction.ARROW_DOWN);
		setKeyStrokeActon(editor, KeyEvent.VK_LEFT, 0, KeyAction.ARROW_LEFT);
		setKeyStrokeActon(editor, KeyEvent.VK_RIGHT, 0, KeyAction.ARROW_RIGHT);
		//
		// KeyAdapter keyAdapter = new KeyAdapter() {
		// @Override
		// public void keyPressed(KeyEvent e) {
		// if (!isContinuousInputMode)
		// return;
		//
		// Optional<KeyAction> action;
		// switch (e.getKeyCode()) {
		// case KeyEvent.VK_ENTER:
		// action = Optional.of(KeyAction.ENTER);
		// break;
		// case KeyEvent.VK_UP:
		// action = Optional.of(KeyAction.ARROW_UP);
		// break;
		// case KeyEvent.VK_DOWN:
		// action = Optional.of(KeyAction.ARROW_DOWN);
		// break;
		// case KeyEvent.VK_LEFT:
		// action = Optional.of(KeyAction.ARROW_LEFT);
		// break;
		// case KeyEvent.VK_RIGHT:
		// action = Optional.of(KeyAction.ARROW_RIGHT);
		// break;
		// default:
		// action = Optional.empty();
		// }
		//
		// if (action.isPresent())
		// keyboardMovingAction(action.get());
		// }
		// };
		// editor.addKeyListener(keyAdapter);
	}

}
