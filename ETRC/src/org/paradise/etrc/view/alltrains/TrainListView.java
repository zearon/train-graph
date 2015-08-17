package org.paradise.etrc.view.alltrains;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainType;
import org.paradise.etrc.dialog.FindTrainsDialog;
import org.paradise.etrc.dialog.YesNoBox;
import org.paradise.etrc.filter.CSVFilter;
import org.paradise.etrc.filter.TRFFilter;
import org.paradise.etrc.util.config.Config;

import com.zearon.util.ui.map.GLWindowManager;

import static org.paradise.etrc.ETRC.__;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class TrainListView extends JPanel {
	private static final long serialVersionUID = -6188814889727919832L;

	TrainGraph trainGraph;

	MainFrame mainFrame = MainFrame.getInstance();

	TrainsTable table;
	TableRowSorter<TrainsTableModel> tableRowSorter;
	TrainsTableModel tableModel;

//	JCheckBox cbUnderColor;
	TrainView trainView;
	private JComboBox<TrainFilter> cbRaillineFilter;
	
	public TrainListView() {
		table = new TrainsTable();

		try {
			jbInit();
//			pack();
			doUpdateFilters();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setTrainView(TrainView trainView) {
		this.trainView = trainView;
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		tableRowSorter = new TableRowSorter<TrainsTableModel> ((TrainsTableModel)table.getModel()) {

			@Override
			public Comparator<?> getComparator(int column) {
				if (column == 0)
					return Train.getTrainNameComparator();
				else
					return super.getComparator(column);
			}
			
		};
		table.setRowSorter(tableRowSorter);
		
		refresh();
	}
	
	public void refresh() {

		doUpdateFilters();
		
		updateUI();
	}

	private void jbInit() throws Exception {
		tableModel = new TrainsTableModel();
		table.setModel(tableModel);
		table.setDefaultRenderer(String.class, new TrainCellRenderer());
		table.setDefaultRenderer(TrainType.class, new TrainTypeCellRenderer());
//		table.setDefaultEditor(Color.class, new ColorCellEditor());

		table.setFont(new Font("Dialog", 0, 12));
		table.getTableHeader().setFont(new Font("Dialog", 0, 12));
		JScrollPane spColorTable = new JScrollPane(table);
//
//		cbUnderColor = new JCheckBox();
//		cbUnderColor.setFont(new java.awt.Font("Dialog", 0, 12));
//		cbUnderColor.setText(__("Display opposite direction train using watermark"));
//		cbUnderColor.setSelected(!(mainFrame.chartView.underDrawingColor == null));
//		cbUnderColor.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				if (((JCheckBox) e.getSource()).isSelected())
//					mainFrame.chartView.underDrawingColor = ChartView.DEFAULT_UNDER_COLOR;
//				else
//					mainFrame.chartView.underDrawingColor = null;
//				
//				mainFrame.chartView.repaint();
//			}
//		});

		JPanel underColorPanel = new JPanel();
		underColorPanel.setLayout(new BorderLayout());
//		underColorPanel.add(cbUnderColor, BorderLayout.WEST);

		JPanel colorPanel = new JPanel();
		colorPanel.setLayout(new BorderLayout());
		colorPanel.add(spColorTable, BorderLayout.CENTER);
		colorPanel.add(underColorPanel, BorderLayout.SOUTH);

//		JButton btOK = new JButton(__("OK"));
//		btOK.setFont(new Font("dialog", 0, 12));
//		btOK.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
////				mainFrame.mainView.repaint();
//				TrainListView.this.setVisible(false);
//			}
//		});

//		JButton btCancel = new JButton("取 消");
//		btCancel.setFont(new Font("dialog", 0, 12));
//		btCancel.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				TrainsDialog.this.setVisible(false);
//			}
//		});

		JButton btAdd = new JButton(__("Add"));
		btAdd.setFont(new Font("dialog", 0, 12));
		btAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doNewTrain();
			}
		});
		
		JButton btLoad = new JButton(__("Load"));
		btLoad.setFont(new Font("dialog", 0, 12));
		btLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doLoadTrain();
			}
		});		
		
		JButton btImport = new JButton(__("Import Build-in Trains"));
		btImport.setFont(new Font("dialog", 0, 12));
		btImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doImportTrains();
			}
		});

		
		JButton btEdit = new JButton(__("Edit"));
		btEdit.setFont(new Font("dialog", 0, 12));
		btEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				TrainsDialog.this.setVisible(false);
				if(table.getSelectedRow() < 0)
					return;
				doEditTrain(trainGraph.currentNetworkChart.getTrain(table.convertRowIndexToModel(table.getSelectedRow())));
			}
		});
		
		JButton btDel = new JButton(__("Delete"));
		btDel.setFont(new Font("dialog", 0, 12));
		btDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getCellEditor() != null)
					table.getCellEditor().stopCellEditing();

				int[] selectedRows = table.getSelectedRows();
				Arrays.sort(selectedRows);
				for (int i = selectedRows.length - 1; i>=0; --i) {
					trainGraph.currentNetworkChart.removeTrainAt(selectedRows[i]);
				}

				table.revalidate();
//				mainFrame.chartView.repaint();
//		        mainFrame.runView.refresh();
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(btLoad);
		buttonPanel.add(btImport);
		buttonPanel.add(btAdd);
		buttonPanel.add(btEdit);
		buttonPanel.add(btDel);
//		buttonPanel.add(btOK);

//		JPanel rootPanel = new JPanel();
		setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		
		JLabel lblFilter = new JLabel("Railline Filter");
		lblFilter.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		panel.add(lblFilter);
		
		cbRaillineFilter = new JComboBox<>();
		cbRaillineFilter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					doUpdateTrains();
				}
			}
		});
		cbRaillineFilter.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		panel.add(cbRaillineFilter);
		
		JButton btnUpdateFilters = new JButton("Update Filters");
		btnUpdateFilters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doUpdateFilters();
			}
		});
		btnUpdateFilters.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		panel.add(btnUpdateFilters);
		add(colorPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

//		getContentPane().add(rootPanel);
	}

	protected void doEditTrain(Train train) {
		trainView.setModel(train);

		trainView.editTrain(editedTrain -> {
			//没有改车次的情况，更新
			if(trainGraph.currentNetworkChart.containsTrain(editedTrain)) {
				trainGraph.currentNetworkChart.updateTrain(editedTrain);
			}
			//改了车次的情况，删掉原来的，增加新的
			else {
				trainGraph.currentNetworkChart.removeTrain(train);
				trainGraph.currentNetworkChart.addTrain(editedTrain);
			}
			
			table.revalidate();
		});
		
//		if(!dialog.isCanceled) {
//			Train editedTrain = dialog.getTrain();
//			//没有改车次的情况，更新
//			if(trainGraph.allTrains.contains(editedTrain)) {
//				trainGraph.allTrains.updateTrain(editedTrain);
//			}
//			//改了车次的情况，删掉原来的，增加新的
//			else {
//				trainGraph.allTrains.remove(train);
//				trainGraph.allTrains.add(editedTrain);
//			}
//			
//			table.revalidate();
//		}
	}
	
	protected void doNewTrain() {
		Train newTrain = TrainGraphFactory.createInstance(Train.class);
		newTrain.setName("XXXX/YYYY");
		newTrain.trainNameDown = "DDDD";
		newTrain.trainNameUp   = "UUUU";
//		newTrain.stopNum = 3;
//		newTrain.getStops()[0] = new Stop(_("Departure"), "00:00", "00:00", false);
//		newTrain.getStops()[1] = new Stop(_("Middle"), "00:00", "00:00", false);
//		newTrain.getStops()[2] = new Stop(_("Terminal"), "00:00", "00:00", false);
		newTrain.appendStop(TrainGraphFactory.createInstance(Stop.class, __("Departure"))
				.setProperties(newTrain.getName(), Stop.STOP_START_STATION, 0, 0));
		newTrain.appendStop(TrainGraphFactory.createInstance(Stop.class, __("Middle"))
				.setProperties(newTrain.getName(), Stop.STOP_PASSENGER, 0, 0));
		newTrain.appendStop(TrainGraphFactory.createInstance(Stop.class, __("Terminal"))
				.setProperties(newTrain.getName(), Stop.STOP_TERMINAL_STATION, 0, 0));
		

		trainView.setModel(newTrain);

		trainView.editTrain(addingTrain -> {
			if(trainGraph.currentNetworkChart.containsTrain(addingTrain)) {
				if(new YesNoBox(mainFrame, String.format(__("%s is already in the graph. Overwrite?"), addingTrain.getTrainName())).askForYes())
					trainGraph.currentNetworkChart.updateTrain(addingTrain);
			}
			else {
				trainGraph.currentNetworkChart.addTrain(addingTrain);
			}
			
			table.revalidate();
		});
		
//		if(!dialog.isCanceled) {
//			Train addingTrain = dialog.getTrain();
//			if(trainGraph.allTrains.contains(addingTrain)) {
//				if(new YesNoBox(mainFrame, String.format(__("%s is already in the graph. Overwrite?"), addingTrain.getTrainName())).askForYes())
//					trainGraph.allTrains.updateTrain(addingTrain);
//			}
//			else {
//				trainGraph.allTrains.add(addingTrain);
//			}
//			
//			table.revalidate();
//		}
	}
	
	public void doUpdateFilters() {
		cbRaillineFilter.setModel(new DefaultComboBoxModel<>(
				TrainFilter.createFiltersForAllRaillines(trainGraph)));
		cbRaillineFilter.repaint();
		
		cbRaillineFilter.setSelectedIndex(0);
		doUpdateTrains();
	}
	
	public void doUpdateTrains() {
		if (tableRowSorter == null)
			return;
		
		TrainFilter trainFilter = (TrainFilter) cbRaillineFilter.getSelectedItem();
		tableRowSorter.setRowFilter(trainFilter);
	}

	public void doImportTrains() {
		//new MessageBox(this, "todo：从网络获取数据生成车次描述文件(.trf文件)。").showMessage();
		if(new YesNoBox(mainFrame, __("<html>This operation will delete all the train information on the current railnetwork chart, <br/>"
				+ "then import the train information from the default time table for this circuit. Continue?</html>")).askForYes()) {
			FindTrainsDialog waitingBox = new FindTrainsDialog(mainFrame);
			waitingBox.findTrains();
		}
	}

	/**
	 * doLoadTrain
	 */
	public void doLoadTrain() {
		if(!(new YesNoBox(mainFrame, __("Load train information file and overwrite the existing information. Continue?")).askForYes()))
			return;

		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Load Train Information"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(true);
		chooser.addChoosableFileFilter(new CSVFilter());
		chooser.addChoosableFileFilter(new TRFFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(Config.getInstance().getLastFilePath(""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}
		
		int returnVal = GLWindowManager.showDialogOnFloatingGLWindow(
				() -> chooser.showOpenDialog(this));
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f[] = chooser.getSelectedFiles();
			if (f.length > 0)
				System.out.println(f[0]);
			else
				System.out.println("No File selected!");

			Train loadingTrain = null;
			for (int j = 0; j < f.length; j++) {
				loadingTrain = TrainGraphFactory.createInstance(Train.class);
				try {
					loadingTrain.loadFromFile2(f[j].getAbsolutePath());
//					prop.setProperty(Prop_Recent_Open_File_Path, chooser.getSelectedFile().getParentFile().getAbsolutePath());
				} catch (IOException ex) {
					System.err.println("Error: " + ex.getMessage());
				}

//				System.out.println(loadingTrain.getTrainName() + "次列车从"
//						+ loadingTrain.startStation + "到"
//						+ loadingTrain.terminalStation + "，共经停"
//						+ loadingTrain.stopNum + "个车站");
//				for (int i = 0; i < loadingTrain.stopNum; i++)
//					System.out.println(loadingTrain.stops[i].stationName + "站 "
//							+ Train.toTrainFormat(loadingTrain.stops[i].arrive)
//							+ " 到 "
//							+ Train.toTrainFormat(loadingTrain.stops[i].leave)
//							+ " 发");

				if(loadingTrain.isDownTrain(trainGraph.currentLineChart.railroadLine, false) > 0)
					trainGraph.currentLineChart.addTrain(loadingTrain);
			}

			//System.out.println("1.Move to: "+loadingTrain.getTrainName());
			//mainView.buildTrainDrawings();
			mainFrame.runningChartView.getChartView().findAndMoveToTrain(loadingTrain.getTrainName(trainGraph.currentLineChart.railroadLine));
			mainFrame.runningChartView.refresh();
//			chartView.repaint();
//			sheetView.updateData();
//			chartView.findAndMoveToTrain(loadingTrain.getTrainName(trainGraph.currentLineChart.railroadLine));
//			runView.refresh();
			//panelChart.panelLines.repaint();
		}

	}

	public class ColorCellRenderer implements TableCellRenderer {
		/**
		 * For Table Cell
		 *
		 * @param table JTable
		 * @param value Object
		 * @param isSelected boolean
		 * @param hasFocus boolean
		 * @param row int
		 * @param column int
		 * @return Component
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (value == null)
				return new JLabel("null");
			if (!(value instanceof Color))
				return new JLabel("wrong");

			final Color color = (Color) value;
//			final boolean selected = isSelected;
//			final Color background = table.getSelectionBackground();

			JLabel colorLabel = new JLabel();
//			{
//				/**
//				 * 
//				 */
//				private static final long serialVersionUID = -3994087135150899720L;
//				
//				{
//					setForeground(color);
//				}
//
//				public void paint(Graphics g) {
//					super.paint(g);
//					Rectangle cellRect = table.getCellRect(row, column, false);
//					Rectangle clip = g.getClipBounds();
//					if (selected) {
//						g.setColor(background);
//						g.fillRect(clip.x, clip.y, clip.width, clip.height);
//					}
//					
//					g.setColor(color);
//					g.drawLine(clip.x, clip.y + clip.height - 3, clip.x
//							+ clip.width, clip.y + clip.height - 3);
//				}
//			};
			int trainIndex = table.convertRowIndexToModel(row);
			String trainName = (String) table.getRowSorter().getModel().getValueAt(trainIndex, 0);
			colorLabel.setText(String.format("<html><u>%s</u></html>", trainName));
			colorLabel.setForeground(color);
			colorLabel.setHorizontalAlignment(SwingConstants.CENTER);
			colorLabel.setVerticalAlignment(SwingConstants.CENTER);
			return colorLabel;
		}

	}

	@SuppressWarnings("serial")
	public class TrainCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component component = super.getTableCellRendererComponent(table, value, 
					isSelected, hasFocus, row, column);
			
			if (column != 0) {
				component.setForeground(table.getForeground());
				return component;
			}
			
			Color color = tableModel.getTrain(row).trainType.getFontColor();
//			((JLabel) component).setText((String) value);
			component.setForeground(color);
			
			return component;
		}
	}

	@SuppressWarnings("serial")
	public class TrainTypeCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component component = super.getTableCellRendererComponent(table, value, 
					isSelected, hasFocus, row, column);
			
			((JLabel) component).setText(((TrainType) value).getName());
			component.setForeground(((TrainType) value).getFontColor());
			
			return component;
		}
	}
	
	public class TrainsTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3894954492927225594L;
		
		TrainsTableModel() {
		}

		/**
		 * getColumnCount
		 *
		 * @return int
		 */
		public int getColumnCount() {
			return 4;
		}

		/**
		 * getRowCount
		 *
		 * @return int
		 */
		public int getRowCount() {
			return trainGraph.currentNetworkChart.trainCount();
		}

		/**
		 * isCellEditable
		 *
		 * @param rowIndex int
		 * @param columnIndex int
		 * @return boolean
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		/**
		 * getColumnClass
		 *
		 * @param columnIndex int
		 * @return Class
		 */
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
			case 1:
			case 2:
				return String.class;
			case 3:
				return TrainType.class;
			default:
				return null;
			}
		}

		/**
		 * getValueAt
		 *
		 * @param rowIndex int
		 * @param columnIndex int
		 * @return Object
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			Train train = getTrain(rowIndex);
			switch (columnIndex) {
			case 0:
				return train.getTrainName();
			case 1:
				return train.getStartStation();
			case 2:
				return train.getTerminalStation();
			case 3:
				return train.trainType;
			default:
				return null;
			}
		}

		public Train getTrain(int rowIndex) {
			return trainGraph.currentNetworkChart.getTrain(rowIndex);
		}

		/**
		 * setValueAt
		 *
		 * @param aValue Object
		 * @param rowIndex int
		 * @param columnIndex int
		 */
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 3) {
				getTrain(rowIndex).trainType = (TrainType) aValue;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}

		/**
		 * getColumnName
		 *
		 * @param columnIndex int
		 * @return String
		 */
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return __("Number");
			case 1:
				return __("Departure");
			case 2:
				return __("Terminal");
			case 3:
				return __("Train Type");
			default:
				return null;
			}
		}
	}

	public class TrainsTable extends JTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5356639615057465995L;

		public TrainsTable() {
			setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			
			addMouseListener(new MouseAdapter() {  
				public void mouseClicked(MouseEvent e) {
					if   (e.getClickCount()   ==   2){ //双击  
	                    int row = getSelectedRow();
	                    doEditTrain(trainGraph.currentNetworkChart.getTrain(convertRowIndexToModel(row)));
					}
				}
			});

		}

		public boolean isRowSelected(int row) {
			//      return chart.trains[row].equals(chart.getActiveTrain());
			return super.isRowSelected(row);
		}
	}

}
