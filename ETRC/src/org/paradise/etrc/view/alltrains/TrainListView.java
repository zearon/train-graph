package org.paradise.etrc.view.alltrains;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.dialog.YesNoBox;

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

//	JCheckBox cbUnderColor;
	TrainView trainView;
	
	public TrainListView() {
		table = new TrainsTable();

		try {
			jbInit();
//			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setTrainView(TrainView trainView) {
		this.trainView = trainView;
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		table.setRowSorter(new TableRowSorter<TrainsTableModel> ((TrainsTableModel)table.getModel()));
		
		updateUI();
	}

	private void jbInit() throws Exception {
		table.setModel(new TrainsTableModel());
		table.setDefaultRenderer(Color.class, new ColorCellRenderer());
		table.setDefaultEditor(Color.class, new ColorCellEditor());

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
				mainFrame.doLoadTrain();
			}
		});		
		
		JButton btImport = new JButton(__("Import From Schedule"));
		btImport.setFont(new Font("dialog", 0, 12));
		btImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.doImportTrains();
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
		newTrain.name = "XXXX/YYYY";
		newTrain.trainNameDown = "DDDD";
		newTrain.trainNameUp   = "UUUU";
//		newTrain.stopNum = 3;
//		newTrain.getStops()[0] = new Stop(_("Departure"), "00:00", "00:00", false);
//		newTrain.getStops()[1] = new Stop(_("Middle"), "00:00", "00:00", false);
//		newTrain.getStops()[2] = new Stop(_("Terminal"), "00:00", "00:00", false);
		newTrain.appendStop(TrainGraphFactory.createInstance(Stop.class, __("Departure"))
				.setProperties("00:00", "00:00", false));
		newTrain.appendStop(TrainGraphFactory.createInstance(Stop.class, __("Middle"))
				.setProperties("00:00", "00:00", false));
		newTrain.appendStop(TrainGraphFactory.createInstance(Stop.class, __("Terminal"))
				.setProperties("00:00", "00:00", false));
		

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

	public class ColorCellEditor extends AbstractCellEditor implements
			TableCellEditor, ActionListener {
		private static final long serialVersionUID = -5449669328932839469L;

		Color currentColor;

		JButton colorButton = new JButton();

		JColorChooser colorChooser;

		JDialog dialog;

		protected static final String EDIT = "edit";

		public ColorCellEditor() {
			//Set up the editor (from the table's point of view),
			//which is a button.
			//This button brings up the color chooser dialog,
			//which is the editor from the user's point of view.

			//Set up the dialog that the button brings up.
			colorChooser = new JColorChooser();
			dialog = JColorChooser.createDialog(TrainListView.this, "Select the color for the line",
					true, //modal
					colorChooser, this, //OK button handler
					null); //no CANCEL button handler
		}

		/**
		 * Handles events from the editor button and from
		 * the dialog's OK button.
		 */
		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				//The user has clicked the cell, so
				//bring up the dialog.
				colorButton.setForeground(currentColor);
				colorChooser.setColor(currentColor);
				ETRC.setFont(dialog);
				dialog.setVisible(true);

				//Make the renderer reappear.
				fireEditingStopped();

			} else { //User pressed dialog's "OK" button.
				currentColor = colorChooser.getColor();
			}
		}

		//Implement the one CellEditor method that AbstractCellEditor doesn't.
		public Object getCellEditorValue() {
			return currentColor;
		}

		//Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			currentColor = (Color) value;

			colorButton = new JButton() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void paint(Graphics g) {
					Rectangle clip = g.getClipBounds();
					g.setColor(currentColor);
					g.drawLine(clip.x, clip.y + clip.height - 3, clip.x
							+ clip.width, clip.y + clip.height - 3);
					super.paint(g);
				}
			};
			colorButton.setText(trainGraph.currentNetworkChart.getTrain(
					table.convertRowIndexToModel(row))
					.getTrainName());
			colorButton.setForeground(currentColor);
			colorButton.setBackground(Color.white);

			colorButton.setHorizontalAlignment(SwingConstants.CENTER);
			colorButton.setVerticalAlignment(SwingConstants.CENTER);

			colorButton.setActionCommand(EDIT);
			colorButton.addActionListener(this);
			colorButton.setBorderPainted(false);

			return colorButton;
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
			return columnIndex == 3;
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
				return Color.class;
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
			switch (columnIndex) {
			case 0:
				return trainGraph.currentNetworkChart.getTrain(rowIndex).getTrainName();
			case 1:
				return trainGraph.currentNetworkChart.getTrain(rowIndex).getStartStation();
			case 2:
				return trainGraph.currentNetworkChart.getTrain(rowIndex).getTerminalStation();
			case 3:
				return trainGraph.currentNetworkChart.getTrain(rowIndex).color;
			default:
				return null;
			}
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
				trainGraph.currentNetworkChart.getTrain(rowIndex).color = (Color) aValue;
				//System.out.println("SET: " + ((Color)aValue));
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
				return __("Color");
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
