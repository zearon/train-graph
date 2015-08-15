package org.paradise.etrc.view.timetableedit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainRouteSection;
//import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.slice.ChartSlice;
import org.paradise.etrc.util.config.Config;

import com.zearon.util.data.Tuple2;
import com.zearon.util.ui.widget.table.JEditTable;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.DEBUG_MSG;

public class TimetableEditSheetTable extends JEditTable {
	private static final long serialVersionUID = 1L;

	JList<?> rowHeader;
	JTable columnHeader;
	JList<String> corner;

	TimetableEditSheetModel tableModel;
	RowHeaderModel rowHeaderModel;
	private SheetHeaderRenderer sheetHeaderRenderer;
	
	private TrainGraph trainGraph;
	
//	private TimetableEditView editView;
	private StopEditDialog stopEditDialog = new StopEditDialog(this);
	
	private JPopupMenu popupMenu;
	private JMenu editMenu;

	private boolean ui_inited;
	private boolean downGoing;
	
//	private int selectedColumnsBeginIndex = -1;
//	private int selectedColumnsEndIndex;

	public TimetableEditSheetTable(TrainGraph trainGraph, TimetableEditView _sheetView) {
		super(__("Detailed timetable"));
		setTrainGraph(trainGraph);
//		editView = _sheetView;

		initTable();
		
		ui_inited = true;
	}

	public void setTrainGraph(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		if (ui_inited) {
			tableModel.setModel(trainGraph);
			rowHeaderModel.setTrainGraph(trainGraph);
			sheetHeaderRenderer.setTrainGraph(trainGraph);
			
			switchChart(downGoing);
		}
	}
	
	public void switchChart(boolean downGoing) {
		this.downGoing = downGoing;
		
		rowHeaderModel.switchChart(downGoing);
        rowHeader.setFixedCellWidth(getRowHeaderWidth());
		buildeRowHeader();
		rowHeader.repaint();
		
		getTableModel().switchChart(downGoing);
		setupColumnWidth();
	}
	
	public TimetableEditSheetModel getTableModel() {
		return (TimetableEditSheetModel) super.getModel();
	}
	
	// {{ init table
	
	private void initTable() {
		setFont(new Font("Dialog", 0, 12));
		getTableHeader().setFont(new Font("Dialog", 0, 12));

		//设置数据
		tableModel = new TimetableEditSheetModel(this, trainGraph);
		setModel(tableModel);

		//设置渲染器
		setDefaultRenderer(TrainRouteSection.class, new SheetCellRenderer());
		//设置编辑器
//		setDefaultEditor(Stop.class, new SheetCellEditor());
		
		//禁止自动调整列宽
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		//响应行选择变化事件
		getSelectionModel().addListSelectionListener(this::onTableRowSelectionChanged);
		//响应列选择变化事件
		getColumnModel().getSelectionModel().addListSelectionListener(this::onTableColumnSelectionChanged);
		
		// 不画JTable默认的网格线,在cell renderer中根据格子的属性来画
		setIntercellSpacing(new Dimension(0, 0));
		
		//设置行列表头交接处的左上角
		buildCorner();
		
		//设置列表头
		setupTableHeader(this.getTableHeader());
		
		//设置行表头
		rowHeaderModel = new RowHeaderModel(trainGraph);
		rowHeader = buildeRowHeader();

		//设置列宽(包括column header table的列宽)
		setupColumnWidth();
		
		//创建右键菜单及用于主菜单的编辑菜单
		createMenues();
		
		// 鼠标双击编辑单元格及右键显示编辑菜单
		registerMouseListener();
		
		// 键盘按键编辑单元格
		registerKeyListener();
	}
	
	// 鼠标双击编辑单元格及右键显示编辑菜单
	private void registerMouseListener() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				getTableHeader().repaint();
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isEditLocked()) {
					if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
						int columnIndex = getColumnModel().getColumnIndexAtX(e.getX());
						int rowIndex = rowAtPoint(e.getPoint());
						
						if (tableModel.isRemarksRow(rowIndex)) {
							TrainRouteSection section = tableModel.getTrainRouteSection(columnIndex);
							TrainRouteSectionEditDialiog dialog = new TrainRouteSectionEditDialiog(trainGraph, section, columnIndex);
							dialog.showDialog();
						} else if (!tableModel.isNewTrainColumn(columnIndex)) {
							Stop stop = tableModel.getStop(rowIndex, columnIndex);
							stopEditDialog.setStop(stop, rowIndex, columnIndex);
							stopEditDialog.showDialog(TimetableEditSheetTable.this, rowIndex % 2 == 0);
						}
					}
					
					if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
						// 复制粘贴等 右键菜单
						Point point = getLocationOnScreen();
						int popupMenuX = e.getXOnScreen() - point.x;
						int popupMenuY = e.getYOnScreen() - point.y;
						
						popupMenu.show(TimetableEditSheetTable.this, popupMenuX, popupMenuY);
					}
				}
			}
		});
	}
	
	// 键盘按键编辑单元格
	private void registerKeyListener() {
		addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {}
			@Override public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				char keyChar = e.getKeyChar();
				
				if (!isEditLocked()) {
					if ((keyChar >= '0' && keyChar <= '9')
							|| Config.getInstance().isTimetableEditingKey(keyChar)
							|| keyChar == ' ' || keyChar == '\n') {
						
						int columnIndex = getSelectedColumn();
						int rowIndex = getSelectedRow();
	
						if (!tableModel.isNewTrainColumn(columnIndex)) {
							Stop stop = tableModel.getStop(rowIndex, columnIndex);
							stopEditDialog.setStop(stop, rowIndex, columnIndex);
							stopEditDialog.showDialog(TimetableEditSheetTable.this, rowIndex % 2 == 0, keyChar);
						}
						
						if (keyChar == '\n')
							e.consume();
					}
					
//					else if (keyChar == KeyEvent.VK_BACK_SPACE || keyChar == KeyEvent.VK_DELETE) {
//						removeColumns();
//					}
//					
					// 这几个按键虽然在菜单中通过setAccelerator注册过，但是实际使用的时候没有反应。因此再次通过keyListener注册。
					int modifiers = ETRC.isOSX10_7OrAbove() ? 
							KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK;
					boolean ctrlDown = (e.getModifiersEx() & modifiers) != 0;
					boolean shiftDown = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
					if (ctrlDown) {
						if (keyChar == 'c' || keyChar == 'C') {
							if (shiftDown)
								copyCells();
							else
								copyColumns();
						} else if (keyChar == 'x' || keyChar == 'X') {
							cutColumns();
						} else if (keyChar == 'v' || keyChar == 'V') {
							boolean altDown = (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0;
							if (shiftDown)
								pasteCells(altDown);
							else
								pasteColumns(altDown);
						}
					}
				}
				
				if (keyChar == 'l' || keyChar == 'L') {
					toggleEditLocked();
				}
				else if (keyChar == 'd' || keyChar == 'D') {
					toggleContinuousEditMode();
				} else if (keyChar == 'f' || keyChar == 'F') {
					toggleFocusVMove();
				}
			}
		});
	}
	
	private void buildCorner() {
		corner = new JList<>(new String[] {__("车次"), __("类型"), __("车辆名")});
		corner.setCellRenderer(new CornerRenderer(this));
		
		JScrollPane timeTableScrollPane = getScrollPane();
		timeTableScrollPane.setRowHeaderView(rowHeader);
		
		timeTableScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);
	}
	
	/* 行表头 */
	private JList<?> buildeRowHeader() {
        rowHeader = new JList<Object>(rowHeaderModel);
        rowHeader.setFixedCellWidth(getRowHeaderWidth());
//        rowHeader.setFixedCellHeight(getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(this));
        
        //只能选择单行
        rowHeader.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        rowHeader.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() >= 2 && me.getButton() == MouseEvent.BUTTON1) {
					@SuppressWarnings("unchecked")
					String rowHeaderName = ((Tuple2<String, Boolean>) ((JList<?>)me.getSource()).getSelectedValue()).A;
					
					String stationRowString = RowHeaderModel.ARRIVE_STR;
					int beginIndex = stationRowString.indexOf("%s");
					int endIndex = rowHeaderName.length() - (stationRowString.length() - 1) + 1;
					if (rowHeaderName.length() < endIndex)
						return;
					
					String staName = rowHeaderName.substring(beginIndex, endIndex);
					Station station = trainGraph.currentLineChart.railroadLine.getStation(staName);
					if (station != null)
						new ChartSlice(trainGraph.currentLineChart).makeStationSlice(station);
				}
			}
        });
        
        rowHeader.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent lse) {
				int row = ((JList<?>) lse.getSource()).getSelectedIndex();
				changeSelection(row, getSelectedColumn(), false, false);
				editCellAt(row, getSelectedColumn());
			}
        });
        
		JScrollPane timeTableScrollPane = getScrollPane();
		timeTableScrollPane.setRowHeaderView(rowHeader);
        
        return rowHeader;
	}

	//－－－－列表头设置－－－－//
	private void setupTableHeader(final JTableHeader header) {
		//限制列交换 
		header.setReorderingAllowed(false);
		//限制重置列宽 
		header.setResizingAllowed(false);
		//设置表头渲染
		sheetHeaderRenderer = new SheetHeaderRenderer(this.trainGraph);
		header.setDefaultRenderer(sheetHeaderRenderer);
		
		//鼠标点击列表头选择train, 新建Train, 以及弹出train操作的右键菜单
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
//				if (e.getButton() == MouseEvent.BUTTON1) {
//					int columnIndex = header.getColumnModel().getColumnIndexAtX(e.getX());
//					selectedColumnsBeginIndex = columnIndex;
//				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				int columnIndex = header.getColumnModel().getColumnIndexAtX(e.getX());
				TrainRouteSection section = tableModel.getTrainRouteSection(columnIndex);
				
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					if (tableModel.isNewTrainColumn(columnIndex)) {
						// Create a new train route section.
						section = tableModel.createNewTrainRouteSection(columnIndex);
					}
					TrainRouteSectionEditDialiog dialog = new TrainRouteSectionEditDialiog(trainGraph, section, columnIndex);
					dialog.showDialog();
				}
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
					// 复制粘贴等 右键菜单
					Point point = getLocationOnScreen();
					int popupMenuX = e.getXOnScreen() - point.x;
					int popupMenuY = e.getYOnScreen() - point.y;
					
					popupMenu.show(TimetableEditSheetTable.this, popupMenuX, popupMenuY);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
//				if (e.getButton() == MouseEvent.BUTTON1) {
//					int columnIndex = header.getColumnModel().getColumnIndexAtX(e.getX());
//					selectedColumnsEndIndex = columnIndex;
//	
//					setRowSelectionInterval(0, 0);
//					setRowSelectionInterval(0, getRowCount() - 1);
//					setColumnSelectionInterval(selectedColumnsBeginIndex, selectedColumnsEndIndex);
//					
//					repaint();
//					header.repaint();
//				}
			}
			
		});
	}
	
	public JMenu getEditMenu() {
		return editMenu;
	}
	
	private void createMenues() {
		editMenu = new JMenu(__("Timetable"));
		editMenu.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		
		popupMenu = new JPopupMenu();
		createMenuItemForMenu(__("Add a new train"), false, true,
				KeyEvent.VK_B, 0, e -> insertNewTrain(false));
		createMenuItemForMenu(__("Insert a new train"), false, true,
				KeyEvent.VK_B, KeyEvent.ALT_DOWN_MASK, e -> insertNewTrain(true));
		createSeperatorForMenu();
		createMenuItemForMenu(__("Copy time"), false, false,
				KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK, e -> copyCells());
		createMenuItemForMenu(__("Paste time"), false, false,
				KeyEvent.VK_V, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK, e -> pasteCells(false));
		createMenuItemForMenu(__("Paste time with %d minutes offset"), true, false,
				KeyEvent.VK_V, KeyEvent.SHIFT_DOWN_MASK, e -> pasteCells(true));
		createSeperatorForMenu();
		createMenuItemForMenu(__("Remove trains"), false, false,
				KeyEvent.VK_DELETE, -1, e -> removeColumns());
		createMenuItemForMenu(__("Cut trains"), false, false,
				KeyEvent.VK_X, 0, e -> cutColumns());
		createMenuItemForMenu(__("Copy trains"), false, false,
				KeyEvent.VK_C, 0, e -> copyColumns());
		createMenuItemForMenu(__("Paste trains"), false, false,
				KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK, e -> pasteColumns(false));
		createMenuItemForMenu(__("Paste trains with %d minutes offset"), true, false,
				KeyEvent.VK_V, 0, e -> pasteColumns(true));
		
		add(popupMenu);
		
		updateMenuItem(popupMenu);
		updateMenuItem(editMenu.getPopupMenu());
	}
	
	private void updateMenuItem(JPopupMenu menu) {
		int selectedColumnCount = getSelectedColumnCount();
		boolean selectTrains = selectedColumnCount >= 1;
		boolean selectTime = selectedColumnCount == 1;
		boolean anyTrainSelected = selectedColumnCount > 0;
//		IntSupplier counter = null;
//		IntFunction<Component> itemGetter = null;
		
		for (int i = 0; i < menu.getComponentCount(); ++ i) {
			Component com = menu.getComponent(i);
			JMenuItem menuItem = com instanceof JMenuItem ? (JMenuItem) com : null;
			if (menuItem == null)
				continue;
			
			if (i < 3) 
				menuItem.setEnabled(anyTrainSelected);
			else if (i < 6)
				menuItem.setEnabled(selectTime);
			else
				menuItem.setEnabled(selectTrains);
			
			String name = menuItem.getName();
			if (name != null) {
				menuItem.setText(String.format(name, getIncrement()));
			}
		}
	}
	
	private void createSeperatorForMenu() {
		popupMenu.addSeparator();
		editMenu.addSeparator();
	}
	
	private void createMenuItemForMenu(String name, boolean setName, boolean enabled,
			int shortcutKey, int extraModifier, ActionListener listener) {
		
		JMenuItem item = createMenuItem(name, setName, enabled, shortcutKey, extraModifier, listener);
		popupMenu.add(item);
		
		JMenuItem item2 = createMenuItem(name, setName, enabled, shortcutKey, extraModifier, listener);
		editMenu.add(item2);
	}

	// 创建右键菜单的菜单项
	private JMenuItem createMenuItem(String name, boolean setName, boolean enabled,
			int shortcutKey, int extraModifier, ActionListener listener) {
		
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		menuItem.setEnabled(enabled);
		
		if (setName)
			menuItem.setName(name);

		if (listener != null)
			menuItem.addActionListener(listener);
		
		if (shortcutKey >= 0) {
			int ctrlOrCommand = InputEvent.CTRL_DOWN_MASK;
			if (ETRC.isOSX10_7OrAbove()) {
				ctrlOrCommand = InputEvent.META_DOWN_MASK; // command key down
				if (shortcutKey == KeyEvent.VK_DELETE)
					shortcutKey = KeyEvent.VK_BACK_SPACE;
			}
			
			int modifier = extraModifier >= 0 ? extraModifier | ctrlOrCommand : 0;
			
			menuItem.setAccelerator(
					KeyStroke.getKeyStroke(shortcutKey, modifier));

		}

		return menuItem;
	}

/*	int getPreferredWidthForCloumn(JTable table, int icol) {
		TableColumnModel tcl = table.getColumnModel();
		TableColumn col = tcl.getColumn(icol);
		int c = col.getModelIndex(), width = 0, maxw = 0;

		for (int r = 0; r < table.getRowCount(); ++r) {

			TableCellRenderer renderer = table.getCellRenderer(r, c);
			Component comp = renderer.getTableCellRendererComponent(table,
					table.getValueAt(r, c), false, false, r, c);
			width = comp.getPreferredSize().width;
			maxw = width > maxw ? width : maxw;
		}
		return maxw;
	} */

	public void setupColumnWidth() {
		//设置列宽
		for (int i = 0; i < getColumnCount(); i++) {
//			int width = getPreferredWidthForCloumn(this, i) + 16;
			int width = getCellWidth();
			getColumnModel().getColumn(i).setPreferredWidth(width);
		}
	}
	
	// }}
	
	// {{ table UI attributes
	
	public int getRemarksRowHeight() {
		return trainGraph.settings.timetableEditRemarksRowHeight;
	}
	
	public int getRowHeaderWidth() {
		return trainGraph.settings.timetableEditRowHeaderWidth;
	}
	
	public int getCellWidth() {
		return trainGraph.settings.timetableEditCellWidth;
	}
	
	public int getVehicleNameRowHeight() {
		return trainGraph.settings.timetableEditVehicleNameRowHeight;
	}	
	
	// }}

	// {{ Override parent methods and Event handlers
	
	@Override
	public void editingStopped(ChangeEvent e) {
		super.editingStopped(e);
	}

	@Override
	public boolean editCellAt(int row, int col) {
		boolean rt = super.editCellAt(row, col);
		
		if(rt)
			if(getEditorComponent()!=null)
				getEditorComponent().requestFocus();
		
		return rt;
	}	
	
	// Override key event listeners in base class.
	@Override
	protected void setKeyStrokeAction(JComponent comp, int keycode,
			int modifier, KeyAction action) {}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
		if (ui_inited && (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW)) {
			// If fireTableStructureChanged is invoked on table model, 
			// the column with and remarks row height should be reset.
			setupColumnWidth();
			setRowHeight(getRowCount() - 1, getRemarksRowHeight());
		}
	}

	private void onTableRowSelectionChanged(ListSelectionEvent e) {
	}
	
	private void onTableColumnSelectionChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
		
		if (getSelectedColumnCount() > 1) {
			setRowSelectionInterval(0, getRowCount() - 1);
			repaint();
			tableHeader.repaint();
		}
		
		updateMenuItem(editMenu.getPopupMenu());
		updateMenuItem(popupMenu);
	}
	
	// }}
	
	public void updateData() {
		setupColumnWidth();
		getTableModel().fireTableStructureChanged();
	}
	
	public void refreshColumn(int column) {
		getTableHeader().repaint();
		tableModel.fireTableCellUpdated(getRowCount() - 1, column);
	}
	
	public void refreshStopCell(int row, int column) {
		int row1 = row;
		int row2 = row % 2 == 0 ? row + 1 : row - 1;
		tableModel.fireTableCellUpdated(row1, column);
		tableModel.fireTableCellUpdated(row2, column);
	}
	
	public void moveToNextCell() {
		int columnIndex = getSelectedColumn();
		int rowIndex = getSelectedRow();
		
		if (isFocusVMove() && rowIndex < getRowCount() - 3) {
			rowIndex += 2;
			setRowSelectionInterval(rowIndex, rowIndex);
		}
		else if (!isFocusVMove() && columnIndex < getColumnCount() - 2) {
			columnIndex += 1;
			setColumnSelectionInterval(columnIndex, columnIndex);
		}
	}
	
	// {{ Clip board operations and other context menu operations.
	
	public void cutSelection() {
		switch (getSelectedColumnCount()) {
		case 0:
			break;
		case 1:
			break;
		default:
			cutColumns();
			break;
		}
	}
	
	public void copySelection() {
		switch (getSelectedColumnCount()) {
		case 0:
			break;
		case 1:
			copyCells();
			break;
		default:
			copyColumns();
			break;
		}
	}
	
	public void pasteSelection(boolean withOffset) {
		switch (getSelectedColumnCount()) {
		case 0:
			break;
		case 1:
			pasteCells(withOffset);
			break;
		default:
			pasteColumns(withOffset);
			break;
		}
	}
	
	public void copyCells() {
		DEBUG_MSG("copy cells");
		tableModel.copyStopTime(getSelectedRows(), getSelectedColumn());
	}
	
	public void pasteCells(boolean withOffset) {
		DEBUG_MSG("paste cells");
		tableModel.pasteStopTimeWithOffset(getSelectedRow(), getSelectedColumn(), getIncrement());
	}
	
	public void removeColumns() {
		DEBUG_MSG("remove columns");
		tableModel.removeTrainRouteSection(getSelectedColumns());
	}
	
	public void cutColumns() {
		DEBUG_MSG("cut columns");
		tableModel.cutTrainRouteSection(getSelectedColumns());
	}
	
	public void copyColumns() {
		DEBUG_MSG("copy columns");
		tableModel.copyTrainRouteSection(getSelectedColumns());
	}
	
	public void pasteColumns(boolean withOffset) {
		DEBUG_MSG("paste columns");
		int targetColumn = getLastSelectedColumnIndex() + 1;
		if (targetColumn >= getColumnCount())
			targetColumn = getColumnCount() - 1;
		tableModel.pasteTrainRouteSectionWithOffset(targetColumn, getIncrement());
	}
	
	public void insertNewTrain(boolean insertBeforeSelected) {
		DEBUG_MSG("insert new train");
		int columnIndex = getSelectedColumn();
		if (!insertBeforeSelected) {
			columnIndex = getLastSelectedColumnIndex() + 1;
		}
		if (columnIndex < 0)
			return;
		
		if (columnIndex >= getColumnCount()) 
			columnIndex = getColumnCount() - 1;
		
		// Create a new train route section.
		TrainRouteSection section = tableModel.createNewTrainRouteSection(columnIndex);
		
		// Show edit dialog
		TrainRouteSectionEditDialiog dialog = new TrainRouteSectionEditDialiog(trainGraph, section, columnIndex);
		dialog.showDialog();
	}
	
	private int getLastSelectedColumnIndex() {
			int[] selection = getSelectedColumns();
			int count = selection.length;
			return count > 0 ? selection[count - 1] : -1;
	}
	
	// }}
	
}
