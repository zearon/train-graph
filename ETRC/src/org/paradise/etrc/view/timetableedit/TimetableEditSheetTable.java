package org.paradise.etrc.view.timetableedit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ListUI;
import javax.swing.plaf.basic.BasicListUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainRouteSection;
import org.paradise.etrc.dialog.MessageBox;
//import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.slice.ChartSlice;
import org.paradise.etrc.util.Config;
import org.paradise.etrc.util.data.Tuple2;
import org.paradise.etrc.util.ui.widget.table.JEditTable;
import org.paradise.etrc.util.ui.widget.table.JEditTable.KeyAction;
import org.paradise.etrc.view.traintypes.TrainTypeTableCellRenderer;
import org.paradise.etrc.view.traintypes.TrainTypeTableModel;

import com.sun.xml.internal.messaging.saaj.packaging.mime.Header;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

public class TimetableEditSheetTable extends JEditTable {
	private static final long serialVersionUID = 1L;

	JList<?> rowHeader;
	JTable columnHeader;

	TimetableEditSheetModel tableModel;
	RowHeaderModel rowHeaderModel;
	private SheetHeaderRenderer sheetHeaderRenderer;
	
	private TrainGraph trainGraph;
	
	private TimetableEditView editView;
	private StopEditDialog stopEditDialog = new StopEditDialog(this);

	private boolean ui_inited;
	
	private int selectedColumnsBeginIndex = -1;
	private int selectedColumnsEndIndex;

	public TimetableEditSheetTable(TrainGraph trainGraph, TimetableEditView _sheetView) {
		super(__("Detailed timetable"));
		setTrainGraph(trainGraph);
		editView = _sheetView;

		initTable();
		
		ui_inited = true;
	}
	
	public void editingStopped(ChangeEvent e) {
		super.editingStopped(e);
	}
	
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
		
		//响应右键事件：开始编辑
		//注意：此处table无法响应左键事件
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				//偶尔也能响应到左键，虽然机会不多，但此时单元格会得不到焦点，因此此处不再判断是否右键
				if(me.getButton() == MouseEvent.BUTTON3) {
					Point p = me.getPoint();
					int rowIndex = TimetableEditSheetTable.this.rowAtPoint(p);
					int columnIndex = TimetableEditSheetTable.this.columnAtPoint(p);
					TimetableEditSheetTable.this.editCellAt(rowIndex, columnIndex);
//					SheetTable.this.getEditorComponent().requestFocus();
				}
			}
		});
		
		//响应行选择变化事件
		this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int row = TimetableEditSheetTable.this.getSelectedRow();
				TimetableEditSheetTable.this.getRowHeader().setSelectedIndex(row);
				TimetableEditSheetTable.this.getRowHeader().repaint();
				TimetableEditSheetTable.this.editCellAt(row, TimetableEditSheetTable.this.getSelectedColumn());
//				SheetTable.this.getEditorComponent().requestFocus();
			}
		});
		
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
		
		// 鼠标双击编辑单元格
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
				}
			}
		});
		
		// 键盘按键编辑单元格
		addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {}
			@Override public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (isEditLocked())
					return;
				
				char keyChar = e.getKeyChar();
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
				
				else if (keyChar == 'c' || keyChar == 'C') {
					toggleContinuousEditMode();
				} else if (keyChar == 'f' || keyChar == 'F') {
					toggleFocusVMove();
				}
			}
		});
	}
	
	public void setTrainGraph(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		if (ui_inited) {
			sheetHeaderRenderer.setTrainGraph(trainGraph);
		}
	}
	
	public void switchChart(boolean downGoing) {
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
	
	public boolean editCellAt(int row, int col) {
		boolean rt = super.editCellAt(row, col);
		
		if(rt)
			if(getEditorComponent()!=null)
				getEditorComponent().requestFocus();
		
		return rt;
	}	
	
	private void buildCorner() {
		JList<String> corner = new JList<>(new String[] {__("车次"), __("类型"), __("车辆名")});
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

	//－－－－列表头设置－－－－//
	private void setupTableHeader(final JTableHeader header) {
		//限制列交换 
		header.setReorderingAllowed(false);
		//限制重置列宽 
		header.setResizingAllowed(false);
		//设置表头渲染
		sheetHeaderRenderer = new SheetHeaderRenderer(this.trainGraph);
		header.setDefaultRenderer(sheetHeaderRenderer);
		
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int columnIndex = header.getColumnModel().getColumnIndexAtX(e.getX());
				selectedColumnsBeginIndex = columnIndex;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				int columnIndex = header.getColumnModel().getColumnIndexAtX(e.getX());
				TrainRouteSection section = tableModel.getTrainRouteSection(columnIndex);
				
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					TrainRouteSectionEditDialiog dialog = new TrainRouteSectionEditDialiog(trainGraph, section, columnIndex);
					dialog.showDialog();
				}
				if (e.getButton() == MouseEvent.BUTTON2) {
					// TODO: 复制粘贴 右键菜单
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				int columnIndex = header.getColumnModel().getColumnIndexAtX(e.getX());
				selectedColumnsEndIndex = columnIndex;

				setRowSelectionInterval(0, 0);
				setRowSelectionInterval(0, getRowCount() - 1);
				setColumnSelectionInterval(selectedColumnsBeginIndex, selectedColumnsEndIndex);
				
				repaint();
				header.repaint();
			}
			
		});
	}

//	int getPreferredWidthForCloumn(JTable table, int icol) {
//		TableColumnModel tcl = table.getColumnModel();
//		TableColumn col = tcl.getColumn(icol);
//		int c = col.getModelIndex(), width = 0, maxw = 0;
//
//		for (int r = 0; r < table.getRowCount(); ++r) {
//
//			TableCellRenderer renderer = table.getCellRenderer(r, c);
//			Component comp = renderer.getTableCellRendererComponent(table,
//					table.getValueAt(r, c), false, false, r, c);
//			width = comp.getPreferredSize().width;
//			maxw = width > maxw ? width : maxw;
//		}
//		return maxw;
//	}
	
	public JList<?> getRowHeader() {
		return rowHeader;
	}

	public void setupColumnWidth() {
		//设置列宽
		for (int i = 0; i < getColumnCount(); i++) {
//			int width = getPreferredWidthForCloumn(this, i) + 16;
			int width = getCellWidth();
			getColumnModel().getColumn(i).setPreferredWidth(width);
		}
	}	
	
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
	
	// Override key event listners in base class.
	@Override
	protected void setKeyStrokeAction(JComponent comp, int keycode,
			int modifier, KeyAction action) {}
	
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
}
