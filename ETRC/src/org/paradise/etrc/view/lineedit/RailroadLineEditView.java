package org.paradise.etrc.view.lineedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.stream.Stream;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.RailNetwork;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.dialog.CircuitMakeDialog;
import org.paradise.etrc.dialog.InfoDialog;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.dialog.XianluSelectDialog;
import org.paradise.etrc.filter.CIRFilter;
import org.paradise.etrc.filter.CRSFilter;
import org.paradise.etrc.filter.CSVFilter;
import org.paradise.etrc.util.config.Config;

import com.zearon.util.ui.widget.table.ColorTableCellEditor;
import com.zearon.util.ui.widget.table.ColorTableCellRender;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.DEBUG;
import static org.paradise.etrc.ETRCUtil.IS_DEBUG;

public class RailroadLineEditView extends JPanel {
	private static final long serialVersionUID = 8501387955756137148L;
	MainFrame mainFrame;
	TrainGraph trainGraph;
	Vector<RailroadLine> circuitsInChart;

	RailroadLineTable railroadLineTable;
	RailroadLineTableModel railroadLineTableModel;
	StationTable stationTable;
	StationTableModel stationTableModel;

	private JTextField tfName;
	private JLabel lbLength;
	private JComboBox<String> cbMultiplicity;
	private JTextField tfDispScale;
//	private JCheckBox ckSuccesiveChange;

	private int circuitNum = 0;
	private RailroadLine tempCircuit;
	private int selectedCircuitIndex = 0;

	private ArrayList<Integer> scaledCircuitIndeces = new ArrayList<Integer>(8);
	private ArrayList<Integer> errorCircuitIndeces = new ArrayList<Integer>(8);
	private Hashtable<String, Integer> crossoverStations = new Hashtable<String, Integer>(
			16);
	private LinkedHashSet<String> stationNames = new LinkedHashSet<String>(100);

	/**
	 * @wbp.parser.constructor
	 */
	public RailroadLineEditView(MainFrame _mainFrame) {
		mainFrame = _mainFrame;
		
		try {
			jbInit();
//			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public RailroadLineEditView(MainFrame _mainFrame,
			Vector<RailroadLine> existingCircuits) {
		this(_mainFrame);
//		super(_mainFrame, __("Railway Circuits: ")
//				+ _mainFrame.getRailNetworkName(), true);

		circuitsInChart = existingCircuits;


	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		railroadLineTableModel.setRailNetwork(this.trainGraph.railNetwork);
		stationTableModel.setRailroadLine(this.trainGraph, this.trainGraph.railNetwork
				.getRailroadLine(0));
		
		resetModel(null);
		System.gc();
	}

	/**
	 * doLoadCircuit
	 */
	private RailroadLine doLoadCircuit() {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Load Circuit"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new CSVFilter());
		chooser.addChoosableFileFilter(new CIRFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(Config.getInstance().getLastRailnetworkPath());
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {
		}

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			// System.out.println(f);

			RailroadLine c = TrainGraphFactory.createInstance(RailroadLine.class);
			try {
				c.loadFromFile2(f.getAbsolutePath());
				Config.getInstance().setLastRailnetworkPath(chooser
								.getSelectedFile().getParentFile()
								.getAbsolutePath());
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
			}
			return c;
		} else
			return null;
	}

	/**
	 * doLoadCircuits: Load circuits in .crs file and append them into circuit
	 * vector "circuits".
	 */
	private void doLoadCircuits(Vector<RailroadLine> circuits,
			boolean clearOriginalCircuits) {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Load Circuits"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new CSVFilter());
		chooser.addChoosableFileFilter(new CRSFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(Config.getInstance().getLastRailnetworkPath());
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {
		}

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			// System.out.println(f);
			
			RailroadLine circuit = null;
			try {				
//				in = new BufferedReader(new InputStreamReader(
//					new BOMStripperInputStream(new FileInputStream(f)),
//					"UTF-8"));
				
				RailNetwork railNetwork = TrainGraphFactory.loadPartFromFile(
						RailNetwork.class, f.getAbsolutePath());
				
				railNetwork.getAllRailroadLines().forEach(line -> {
					trainGraph.railNetwork.addRailroadLine(line);
				});
				
				updateRailroadListSelection(999999);
				
				Config.getInstance().setLastRailnetworkPath(chooser
						.getSelectedFile().getParentFile()
						.getAbsolutePath());
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
				new MessageBox(__("Cannot load railroad network due to\r\n")
						+ ex.getMessage()).showMessage();
				;
			} finally {
//				try {
//					if (in != null)
//						in.close();
//				} catch (IOException e) {
//				}
			}
		}
	}

	/**
	 * doSaveCircuit
	 */
	private void doSaveCircuit(RailroadLine circuit) throws IOException {

		stationTableModel.railroadLine.setName(tfName.getText());
		try {
			stationTableModel.railroadLine.multiplicity = Integer
					.parseInt(cbMultiplicity.getSelectedItem().toString());
		} catch (NumberFormatException e) {
			throw new IOException(__("Invalid rail count. It should be 1/2/4"));
		}
		try {
			stationTableModel.railroadLine.dispScale = Float.parseFloat(tfDispScale
					.getText());
		} catch (NumberFormatException e) {
			new MessageBox(__("Invalid display scale. It should be a decimal"))
					.showMessage();
			;
		}
		doSaveCircuits(circuit, null);
	}

	/**
	 * doSaveCircuits
	 */
	private void doSaveCircuits(RailroadLine circuit, Vector<RailroadLine> circuits)
			throws IOException {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);
		String suffix;

		if (circuit != null) {
			chooser.setDialogTitle(__("Save Railroad Line"));
			chooser.addChoosableFileFilter(new CIRFilter());
			suffix = CIRFilter.suffix;
			chooser.setSelectedFile(new File(circuit.getName()));
		} else if (circuits != null && circuits.size() > 0) {
			chooser.setDialogTitle(__("Export Railroad Network"));
			chooser.addChoosableFileFilter(new CRSFilter());
			suffix = CRSFilter.suffix;
			chooser.setSelectedFile(new File(Config.getInstance().getCurrentFileName()
					.replace(' ', '_')));
		} else {
			return;
		}
		chooser.setApproveButtonText(__("Save"));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(Config.getInstance().getLastRailnetworkPath());
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {
		}

		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String f = chooser.getSelectedFile().getAbsolutePath();
			if (!f.endsWith(suffix))
				f += suffix;

			try {
				FileOutputStream out = new FileOutputStream(f);
				
				trainGraph.railNetwork.saveToStream(out);
				
				out.close();
				Config.getInstance().setLastRailnetworkPath(chooser
								.getSelectedFile().getParentFile()
								.getAbsolutePath());
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
				throw ex;
			}
		}
	}

	private void jbInit() throws Exception {
		// JScrollPane spCircuit = new JScrollPane(table);

		// JPanel circuitPanel = new JPanel();
		// trainPanel.add(underColorPanel, BorderLayout.SOUTH);
		JButton btAddCircuit = new JButton(__("Add Line"));
		btAddCircuit.setFont(new Font("dialog", 0, 12));
		btAddCircuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_AddLine();
			}
		});

		JButton btImportCircuit = new JButton(__("Import Line"));
		btImportCircuit.setFont(new Font("dialog", 0, 12));
		btImportCircuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_ImportLine();
			}
		});

		JButton btMoveUp = new JButton(__("Move Up"));
		btMoveUp.setFont(new Font("dialog", 0, 12));
		btMoveUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_MoveUpLine();
			}
		});

		JButton btMoveDown = new JButton(__("Move Down"));
		btMoveDown.setFont(new Font("dialog", 0, 12));
		btMoveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_MoveDownLine();
			}
		});

		JButton btRemoveCircuit = new JButton(__("Remove Line"));
		btRemoveCircuit.setFont(new Font("dialog", 0, 12));
		btRemoveCircuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_RemoveLine();
			}
		});

		JButton btNewCircuits = new JButton(__("New Line"));
		btNewCircuits.setFont(new Font("dialog", 0, 12));
		btNewCircuits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_NewLine();
			}
		});

		JButton btSaveCircuits = new JButton(__("Export Network"));
		btSaveCircuits.setFont(new Font("dialog", 0, 12));
		btSaveCircuits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_SaveNetwork();
			}
		});

		JButton btLoadCircuits = new JButton(__("Load Network"));
		btLoadCircuits.setFont(new Font("dialog", 0, 12));
		btLoadCircuits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_LoadNetwork();
			}
		});

//		JButton btComplete = new JButton(__("Complete"));
//		btComplete.setFont(new Font("dialog", 0, 12));
//		btComplete.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				doRailNetwork_Complete();
//			}
//		});
//
//		JButton btCancel = new JButton(__("Cancel"));
//		btCancel.setFont(new Font("dialog", 0, 12));
//		btCancel.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				doRailNetwork_Cancel();
//			}
//		});

		JButton btAdjust = new JButton(__("Adjust"));
		btAdjust.setFont(new Font("dialog", 0, 12));
		btAdjust.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRailNetwork_Calculate();
			}
		});

		JButton btCommand2 = new JButton(__("Command2"));
		btCommand2.setFont(new Font("dialog", 0, 12));
		btCommand2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRailNetwork_Command2();
			}
		});

		btMoveUp.setForeground(Color.BLUE);
		btMoveDown.setForeground(Color.BLUE);
		btAddCircuit.setForeground(Color.BLUE);
		btRemoveCircuit.setForeground(Color.BLUE);
		btImportCircuit.setForeground(Color.BLUE);

		btAdjust.setForeground(new Color(0x660000));
		btCommand2.setForeground(new Color(0x660000));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 5));
		buttonPanel.add(btMoveUp);
		buttonPanel.add(btAddCircuit);
		buttonPanel.add(btRemoveCircuit);
		buttonPanel.add(btImportCircuit);
		buttonPanel.add(btAdjust);
//		buttonPanel.add(btComplete);

		buttonPanel.add(btMoveDown);
		buttonPanel.add(btNewCircuits);
		buttonPanel.add(btSaveCircuits);
		buttonPanel.add(btLoadCircuits);
		buttonPanel.add(btCommand2);
//		buttonPanel.add(btCancel);

//		JPanel rootPanel = new JPanel();
		setLayout(new BorderLayout());
		add(buildCircuitListPanel(), BorderLayout.WEST);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.EAST);
		panel.setLayout(null);
		add(buildCircuitPanel(), BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.NORTH);

		
//		add(rootPanel, BorderLayout.CENTER);
//		getContentPane().add(rootPanel);
	}

	private JPanel buildCircuitListPanel() {

		buildCircuitTable();

		JScrollPane spCircuitList = new JScrollPane(railroadLineTable);

		JPanel circuitPanel = new JPanel();
		circuitPanel.setLayout(new BorderLayout());
		circuitPanel.add(spCircuitList, BorderLayout.CENTER);

		return circuitPanel;
	}

	private void buildCircuitTable() {
		railroadLineTable = new RailroadLineTable();
		railroadLineTable.setFont(new Font("Dialog", 0, 12));
		railroadLineTable.getTableHeader().setFont(new Font("Dialog", 0, 12));

		railroadLineTableModel = new RailroadLineTableModel(railroadLineTable, null);
		railroadLineTable.setModel(railroadLineTableModel);
		railroadLineTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		railroadLineTable.getColumnModel().getColumn(1).setPreferredWidth(160);
		railroadLineTable.getColumnModel().getColumn(2).setPreferredWidth(20);
		railroadLineTable.getColumnModel().getColumn(3).setPreferredWidth(20);

		railroadLineTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						// Selected circirt changed in circirt table
						if (arg0.getValueIsAdjusting()) {
							doRailNetwork_ChangeLine();
						}
					}
				});

		@SuppressWarnings("serial")
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {

				if (isErrorCircuit(row)) {
					setBackground(Color.RED);
				} else {
					setBackground(table.getBackground());
				}

				// DEBUG(String.format("[% 2d,% 2d] %s", row, column, value));
				return super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
			}

		};

//		for (int i = 0; i < circuitTable.getColumnCount(); i++) {
			railroadLineTable.getColumn(railroadLineTable.getColumnName(1))
					.setCellRenderer(renderer);
			
			// 颜色栏的渲染器及编辑器
			ColorTableCellRender colorRenderer = new ColorTableCellRender();
			railroadLineTable.setDefaultRenderer(Color.class, colorRenderer);

			ColorTableCellEditor colorEditor = new ColorTableCellEditor();
			railroadLineTable.setDefaultEditor(Color.class, colorEditor);
//		}
	}

	private JPanel buildCircuitPanel() {
//		ckSuccesiveChange = new JCheckBox(__("Succesive Change"), false);

		buildStationTable();

		JButton btMoveUp = new JButton(__("Move Up"));
		btMoveUp.setFont(new Font("dialog", 0, 12));
		btMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// doRailline_Load();
				doRailline_MoveUpStation();
			}
		});

		JButton btMoveDown = new JButton(__("Move Down"));
		btMoveDown.setFont(new Font("dialog", 0, 12));
		btMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// doRailline_Save();
				doRailline_MoveDownStation();
			}
		});

		JButton btDel = new JButton(__("Delete"));
		btDel.setFont(new Font("dialog", 0, 12));
		btDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRailline_DeleteStation();
			}
		});

		JButton btRevert = new JButton(__("Revert"));
		btRevert.setFont(new Font("dialog", 0, 12));
		btRevert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRailline_RevertStation();
			}
		});

		JButton btNormalize = new JButton(__("Normalize"));
		btNormalize.setFont(new Font("dialog", 0, 12));
		btNormalize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRailline_NormalizeDistance();
			}
		});

		JButton btInsert = new JButton(__("Insert"));
		btInsert.setFont(new Font("dialog", 0, 12));
		btInsert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRailline_InsertStation();
			}
		});

		JButton btAdd = new JButton(__("Add"));
		btAdd.setFont(new Font("dialog", 0, 12));
		btAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRailline_AddStation();
			}
		});

		JLabel lbLengthLabel = new JLabel(__("Total Length:"));
		lbLength = new JLabel("0 " + __("km"));

//		JPanel tipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lbTip = new JLabel(
				__(" Press OK button after editing."));
		stationTable.addToStatusBar(lbLengthLabel);
		stationTable.addToStatusBar(lbLength);
		stationTable.addToStatusBar(lbTip);

		JPanel buttonPanel1 = new JPanel(new GridLayout(10, 1, 0, 0));
		// buttonPanel1.setPreferredSize(new Dimension(100, 500));
		buttonPanel1.add(new JPanel().add(new JLabel(" ")));
		buttonPanel1.add(btInsert);
		buttonPanel1.add(btAdd);
		buttonPanel1.add(btDel);
		buttonPanel1.add(btMoveUp);
		buttonPanel1.add(btMoveDown);
		buttonPanel1.add(btRevert);
		buttonPanel1.add(btNormalize);
		buttonPanel1.add(new JPanel().add(new JLabel(" ")));

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(stationTable.getContainerPanel(), BorderLayout.CENTER);

		JPanel circuitPanel = new JPanel();
		circuitPanel.setLayout(new BorderLayout());
		
		JLabel lbName = new JLabel(__("Circuit Name:"));
		lbName.setFont(new Font("dialog", 0, 12));
		
		tfName = new JTextField(12);
		tfName.setFont(new Font("dialog", 0, 12));
		tfName.setText(stationTableModel.railroadLine.getName());
		tfName.addFocusListener(new FocusListener() {
			String oldValue;
			
			@Override
			public void focusLost(FocusEvent e) {
				doRailline_UpdateName(oldValue);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				oldValue = ((JTextField) e.getSource()).getText();
			}
		});
		
		JLabel lbMultiplicity = new JLabel(__("Track Count:"));
		lbName.setFont(new Font("dialog", 0, 12));
		
		cbMultiplicity = new JComboBox<String>();
		cbMultiplicity.setFont(new Font("dialog", 0, 12));
		cbMultiplicity.setEditable(true);
		cbMultiplicity.addItem("1");
		cbMultiplicity.addItem("2");
		cbMultiplicity.addItem("4");
		cbMultiplicity.setSelectedItem(stationTableModel.railroadLine.multiplicity);
		cbMultiplicity.addItemListener(new ItemListener() {
			String oldValue;
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					oldValue = e.getItem().toString();
				} else if (e.getStateChange() == ItemEvent.SELECTED) {
					doRailline_UpdateMultiplicity(oldValue);
				}
			}
		});
		
		JLabel lbDispScale = new JLabel(__("Display Scale:"));
		lbName.setFont(new Font("dialog", 0, 12));
		
		tfDispScale = new JTextField(2);
		tfDispScale.setFont(new Font("dialog", 0, 12));
		tfDispScale.setText("" + stationTableModel.railroadLine.dispScale);
		tfDispScale.addFocusListener(new FocusListener() {
			String oldValue;
			
			@Override
			public void focusLost(FocusEvent e) {
				doRailline_UpdateDisplayScale(oldValue);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				oldValue = ((JTextField) e.getSource()).getText();
			}
		});
		
		JPanel namePanel = new JPanel();
		circuitPanel.add(namePanel, BorderLayout.NORTH);
		namePanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		GroupLayout gl_namePanel = new GroupLayout(namePanel);
		gl_namePanel.setHorizontalGroup(
			gl_namePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_namePanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lbName)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(tfName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lbMultiplicity, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(cbMultiplicity, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lbDispScale)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(tfDispScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(26, Short.MAX_VALUE))
		);
		gl_namePanel.setVerticalGroup(
			gl_namePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_namePanel.createSequentialGroup()
					.addGap(5)
					.addGroup(gl_namePanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lbName)
						.addComponent(tfName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbMultiplicity)
						.addComponent(cbMultiplicity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbDispScale)
						.addComponent(tfDispScale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		namePanel.setLayout(gl_namePanel);
		circuitPanel.add(buttonPanel1, BorderLayout.WEST);
		circuitPanel.add(contentPanel, BorderLayout.CENTER);
		// circuitPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

		return circuitPanel;
	}

	private void buildStationTable() {
		stationTable = new StationTable();
		stationTable.setFont(new Font("Dialog", 0, 12));
		stationTable.getTableHeader().setFont(new Font("Dialog", 0, 12));

		stationTableModel = new StationTableModel(stationTable, this.trainGraph, null);
		stationTable.setModel(stationTableModel);
		// stationTable.setPreferredSize(new Dimension(300,
		// stationTable.getRowHeight() * 20));
		stationTable.getColumnModel().getColumn(3).setPreferredWidth(40);
		stationTable.getColumnModel().getColumn(4).setPreferredWidth(40);

		// Hashtable<Integer, TableCellRenderer> columnRenderers = new
		// Hashtable<>();
		// for (int i = 0; i < stationTable.getColumnCount() - 1; i++) {
		// columnRenderers.put(i,
		// stationTable.getColumn(stationTable.getColumnName(i)).getCellRenderer());
		// }

		@SuppressWarnings("serial")
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {

				if (column == 0 && isCrossOverStation((String) value)) {
					setBackground(Color.YELLOW);
				} else {
					setBackground(table.getBackground());
				}

				return super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
			}

		};

		// Do not set the render for the last column, because it is a boolean column
		// and should be rendered as a check box.
//		for (int i = 0; i < stationTable.getColumnCount() - 1; i++) {
			stationTable.getColumn(stationTable.getColumnName(0))
					.setCellRenderer(renderer);
//		}
	}
	
	public void switchRailLine(RailroadLine line) {
		int index = railroadLineTableModel.raillines.indexOf(line);
		if (index < 0)
			return;
		
		switchRailLine(line, index);
		
		railroadLineTable.setRowSelectionInterval(index, index);
		railroadLineTable.setColumnSelectionInterval(1, 1);
		railroadLineTable.revalidate();
	}

	protected void switchRailLine(RailroadLine circuit, int circuitIindex) {
		
		selectedCircuitIndex = circuitIindex;

		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor().stopCellEditing();

		tfName.setText(circuit.getName());
		cbMultiplicity.setSelectedItem(circuit.multiplicity);
		tfDispScale.setText("" + circuit.dispScale);
		stationTableModel.railroadLine = circuit;
		
		findCrossoverStations();

		stationTable.revalidate();
		stationTable.updateUI();
		
		System.gc();
	}

	public void showDialog() {
		showDialogForCircuit(null);
	}

	private void showDialogForCircuit(RailroadLine circuit) {
		Dimension dlgSize = this.getPreferredSize();
		Dimension frmSize = mainFrame.getSize();
		Point loc = mainFrame.getLocation();
		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);

//		this.setModal(true);
//		this.pack();

		resetModel(circuit);

		this.setVisible(true);
	}

	public void resetModel(RailroadLine line) {

		if (line != null) {
			this.tempCircuit = line;
			line.zindex = 1;
			railroadLineTableModel.raillines.add(line);
			int index = railroadLineTableModel.raillines.size() - 1;
			railroadLineTable.setRowSelectionInterval(index, index);

			switchRailLine(line, index);
		} else {
			line = railroadLineTableModel.raillines.get(0);
			railroadLineTable.setRowSelectionInterval(0, 0);

			switchRailLine(line.copy(), 0);
		}

		findCrossoverStations();
	}

	private void findCrossoverStations() {
		// Find out crossover stations
		stationNames.clear();
		railroadLineTableModel.raillines.stream()
				.flatMap(cir -> cir.getAllStations().stream())
				.map(station -> station.getName()).forEachOrdered(name -> {
					if (stationNames.contains(name)) {
						crossoverStations.put(name, 0);
					}
					stationNames.add(name);
				});

		// Record distance of crossover stations
		railroadLineTableModel.raillines
				.stream()
				.flatMap(cir -> cir.getAllStations().stream())
				.filter(station -> crossoverStations.keySet().contains(
						station.getName()))
				.distinct()
				.forEachOrdered(station -> {
					// DEBUG("Crossover station: %s@%dkm", station.name,
					// station.dist);
						crossoverStations.compute(station.getName(),
								(k, v) -> station.dist);
					});
	}

	private void doRailNetwork_ChangeLine() {
		int circuitIndex = railroadLineTable.getSelectedRow();
		if (circuitIndex < 0)
			return;
		
		RailroadLine circuitInTable = railroadLineTableModel.raillines.get(circuitIndex);
		RailroadLine circuitForEdit = circuitInTable;
		switchRailLine(circuitForEdit, circuitIndex);
	}
	
	private void doRailline_UpdateName(String oldValue) {
		RailroadLine line = ((StationTableModel) stationTable.getModel()).railroadLine;
		String newValue = tfName.getText();
		
		ActionFactory.createSetValueAction(__("railroad line name"), 
				oldValue, newValue, value -> {
					
			line.setName((String) value);
			tfName.setText((String) value);
		}, null).addToManagerAndDoIt();
	}
	
	private void doRailline_UpdateDisplayScale(String oldValue) {
		RailroadLine line = ((StationTableModel) stationTable.getModel()).railroadLine;
		String newValue = tfDispScale.getText();
		
		ActionFactory.createSetValueAction(__("display scale of railroad line"), 
				oldValue, newValue, value -> {
					
			try {
				line.dispScale = Float.parseFloat((String) value);

				tfDispScale.setText((String) value);
				((AbstractTableModel) stationTable.getModel()).fireTableDataChanged();
			} catch (NumberFormatException e) {
				new MessageBox(__("Display scale should be a decimal."))
						.showMessage();
			}	
		}, mainFrame.raillineChartView::updateUI).addToManagerAndDoIt();
	}
	
	private void doRailline_UpdateMultiplicity(String oldValue) {
		RailroadLine line = ((StationTableModel) stationTable.getModel()).railroadLine;
		String newValue = cbMultiplicity.getSelectedItem().toString();
		
		ActionFactory.createSetValueAction(__("track count of railroad line"), 
				oldValue, newValue, value -> {
					
			try {
				line.multiplicity = Integer.parseInt((String) value);

				cbMultiplicity.setSelectedItem((String) value);
			} catch (NumberFormatException e) {
				new MessageBox(__("Rail count should be an integer."))
						.showMessage();
			}
		}, mainFrame.raillineChartView::updateUI).addToManagerAndDoIt();
	}

	private void doRailline_OK() {
		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor().stopCellEditing();

		// Normalize
		RailroadLine c = ((StationTableModel) stationTable.getModel()).railroadLine;
		if (c.getStationNum() < 2) {
			new MessageBox(mainFrame,
					__("A circut must have at least two stations."))
					.showMessage();
			return;
		}

		c.length = c.getStation(c.getStationNum() - 1).dist;
		c.setName(tfName.getText());
		try {
			c.multiplicity = Integer.parseInt(cbMultiplicity.getSelectedItem()
					.toString());
			c.dispScale = Float.parseFloat(tfDispScale.getText());
		} catch (NumberFormatException e) {
			new MessageBox(
					__("Rail count should be an integer and display scale should be a decimal"))
					.showMessage();
			;
		}

		int selectedCircuitIndex = railroadLineTable.getSelectedRow();
		if (selectedCircuitIndex >= 0)
			railroadLineTableModel.raillines.set(selectedCircuitIndex, c);

		railroadLineTable.updateUI();
	}

	private void doRailline_Load() {
		RailroadLine cir = doLoadCircuit();
		if (cir != null) {
			switchRailLine(cir, selectedCircuitIndex);
		}
	}

	private void doRailline_Save() {
		try {
			doSaveCircuit(stationTableModel.railroadLine);
		} catch (IOException e1) {
			e1.printStackTrace();
			new MessageBox(__("Cannot save the circuit due to: \r\n")
					+ e1.getMessage()).showMessage();
			;
		}
	}

	private void doRailline_DeleteStation() {
		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor().stopCellEditing();

		int[] selectedRows = stationTable.getSelectedRows();
		RailroadLine line = ((StationTableModel) stationTable.getModel()).railroadLine;
		
		ActionFactory.createRemoveTableElementsAction(__("station table"), 
				stationTable, true, selectedRows, line::getStation, 
				line::insertStation, line::delStation, stationTable::revalidate).addToManagerAndDoIt();
	}

	private void doRailline_MoveUpStation() {
		// Move down a station
		int selectedStatonIndex = stationTable.getSelectedRow();
		if (selectedStatonIndex == 0) {
			new MessageBox(
					__("This is already the first circuit and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		
		ActionFactory.createTableElementMoveAction(__("station table"), 
				stationTable, stationTableModel.railroadLine.getAllStations(), 
				selectedStatonIndex, selectedStatonIndex - 1, true).addToManagerAndDoIt();
	}

	private void doRailline_MoveDownStation() {
		// Move down a station
		int selectedStatonIndex = stationTable.getSelectedRow();
		if (selectedStatonIndex == stationTableModel.railroadLine.getAllStations()
				.size() - 1) {
			new MessageBox(
					__("This is already the last station and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		
		ActionFactory.createTableElementMoveAction(__("station table"), 
				stationTable, stationTableModel.railroadLine.getAllStations(), 
				selectedStatonIndex, selectedStatonIndex + 1, true).addToManagerAndDoIt();
	}

	protected void doRailline_RevertStation() {
		ActionFactory.createRevertRaillineAction(stationTable, stationTableModel).addToManagerAndDoIt();
	}

	protected void doRailline_NormalizeDistance() {
		RailroadLine c = stationTableModel.railroadLine;
		int offset = c.getStation(0).dist * -1;
		
		ActionFactory.createTableRowColumnIncrementAction(__("Normalize railroad line"), null, 
				stationTable, offset, 0, stationTable.getRowCount() - 1, 1, true).addToManagerAndDoIt();
		
		
//		if (offset != 0) {
//			// if (new YesNoBox(mainFrame,
//			// __("The distance of the first station is not zero, do normalization?")).askForYes())
//			// {
//			for (int i = 0; i < c.getStationNum(); ++i) {
//				c.getStation(i).dist -= offset;
//			}
//			// }
//		}
//
//		stationTable.updateUI();
	}

	private void doRailline_InsertStation() {
		// table.getCellEditor().stopCellEditing();
		RailroadLine line = stationTableModel.railroadLine;
		int selectedIndex = stationTable.getSelectedRow();
		if (selectedIndex < 0) {
			new MessageBox(__("Please choose a station first.")).showMessage();
			return;
		}

		int dist = line.getStation(selectedIndex).dist;
		int level = line.getStation(selectedIndex).level;
		Station station = TrainGraphFactory.createInstance(Station.class)
				.setProperties(dist, level, false);
		
		ActionFactory.createAddTableElementAction(__("station table"), 
				stationTable, true, selectedIndex, station, line::insertStation, 
				line::delStation, stationTable::revalidate).addToManagerAndDoIt();
	}

	private void doRailline_AddStation() {
		// table.getCellEditor().stopCellEditing();
		RailroadLine line = stationTableModel.railroadLine;
		int selectedIndex = stationTable.getSelectedRow();
		if (selectedIndex < 0) {
			new MessageBox(__("Please choose a station first.")).showMessage();
			return;
		}

		int dist = line.getStation(selectedIndex).dist;
		int level = line.getStation(selectedIndex).level;
		Station station = TrainGraphFactory.createInstance(Station.class)
				.setProperties(dist, level, false);
		
		ActionFactory.createAddTableElementAction(__("station table"), 
				stationTable, true, selectedIndex + 1, station, line::insertStation, 
				line::delStation, stationTable::revalidate).addToManagerAndDoIt();
	}

	private void doRailNetwork_AddLine() {
		// Add a circuit
		RailroadLine line = TrainGraphFactory.createInstance(RailroadLine.class);

		int index = railroadLineTableModel.raillines.size();

		
		ActionFactory.createAddTableElementAction(__("railroad network table"), 
				railroadLineTable, true, index, line, 
				railroadLineTableModel.raillines::add, 
				railroadLineTableModel.raillines::removeElementAt, 
				() -> {
					trainGraph.railNetwork.findCrossoverStations();
					updateRailroadListSelection(index);
					stationTable.repaint();
				} ).addToManagerAndDoIt();
	}

	private void doRailNetwork_ImportLine() {
		// Import a circuit
		// TODO: 8. Check station name duplication on clicking OK button
		// TODO: 9. Set circuit crossover
		String xianlu = new XianluSelectDialog(mainFrame).getXianlu();
		if (xianlu == null)
			return;

		RailroadLine line = new CircuitMakeDialog(mainFrame, xianlu).getCircuit();
		if (line == null)
			return;

		int index = railroadLineTableModel.raillines.size();
		
		ActionFactory.createAddTableElementAction(__("railroad network table"), 
				railroadLineTable, true, index, line, 
				railroadLineTableModel.raillines::add, 
				railroadLineTableModel.raillines::removeElementAt, 
				() -> {
					trainGraph.railNetwork.findCrossoverStations();
					updateRailroadListSelection(index);
					stationTable.repaint();
				} ).addToManagerAndDoIt();
	}

	private void doRailNetwork_MoveUpLine() {
		// Move up a circuit
		int selectedCircuitIndex = railroadLineTable.getSelectedRow();
		if (selectedCircuitIndex == 0) {
			new MessageBox(
					__("This is already the first circuit and thus cannot be moved up any more."))
					.showMessage();
			;
			return;
		}
		
		ActionFactory.createTableElementMoveAction(__("railroad line table"), 
				railroadLineTable, railroadLineTableModel.raillines, 
				selectedCircuitIndex, selectedCircuitIndex - 1, true).addToManagerAndDoIt();
	}

	private void doRailNetwork_MoveDownLine() {
		// Move down a circuit
		int selectedCircuitIndex = railroadLineTable.getSelectedRow();
		if (selectedCircuitIndex == railroadLineTableModel.raillines.size() - 1) {
			new MessageBox(
					__("This is already the last circuit and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		
		ActionFactory.createTableElementMoveAction(__("railroad line table"), 
				railroadLineTable, railroadLineTableModel.raillines, 
				selectedCircuitIndex, selectedCircuitIndex + 1, true).addToManagerAndDoIt();
	}

	private void doRailNetwork_RemoveLine() {
		// Remove a circuit
		int index = railroadLineTable.getSelectedRow();
		if (index == 0 && railroadLineTableModel.raillines.size() == 1) {
			new MessageBox(__("Cannot remove the last railroad line.")).showMessage();
			return;
		}

		ActionFactory.createRemoveTableElementsAction(__("railroad network table"), 
				railroadLineTable, true, new int[] {index}, 
				railroadLineTableModel.raillines::elementAt,
				railroadLineTableModel.raillines::add, 
				railroadLineTableModel.raillines::removeElementAt, 
				() -> {
					trainGraph.railNetwork.findCrossoverStations();
					updateRailroadListSelection(index);
					stationTable.repaint();
				} ).addToManagerAndDoIt();
	}

	private void doRailNetwork_NewLine() {
		// Create new circuits with only one circuit
		RailroadLine line = TrainGraphFactory.createInstance(RailroadLine.class);

		int index = railroadLineTableModel.raillines.size();
		
		ActionFactory.createAddTableElementAction(__("railroad network table"), 
				railroadLineTable, true, index, line, 
				railroadLineTableModel.raillines::add, 
				railroadLineTableModel.raillines::removeElementAt, 
				() -> updateRailroadListSelection(index) ).addToManagerAndDoIt();
	}

	private void doRailNetwork_SaveNetwork() {
		// Save all circuits
		if (railroadLineTableModel.raillines.size() == 0) {
			new MessageBox(__("Cannot save empty circuits.")).showMessage();
			return;
		}
		try {
			doSaveCircuits(null, railroadLineTableModel.raillines);
		} catch (IOException e) {
			e.printStackTrace();
			InfoDialog
					.showErrorDialog(
							mainFrame,
							__("Cannot save the circuit due to: \r\n")
									+ e.getMessage());
		}
	}

	private void doRailNetwork_LoadNetwork() {
		// Load all circuits
		try {
			doLoadCircuits(railroadLineTableModel.raillines, true);
			railroadLineTable.updateUI();

			switchRailLine(railroadLineTableModel.raillines.get(0), 0);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			new MessageBox(__("Cannot load circuits due to\r\n")
					+ e.getMessage()).showMessage();
			;
		}
	}

	protected void doRailNetwork_Calculate() {
		// TODO: temp_ERROR
		StringBuilder sb = new StringBuilder();
		int index = -1;
		for (RailroadLine circuit : railroadLineTableModel.raillines) {

			// Calculate offset(s) in crossover station(s)
			Station[] crossoverStationsInCircuit = circuit
					.getAllStations()
					.stream()
					.filter(station -> crossoverStations.keySet().contains(
							station.getName())).toArray(Station[]::new);
			Integer[] offsets = Stream
					.of(crossoverStationsInCircuit)
					.map(station -> (crossoverStations.get(station.getName()) - station.dist))
					.toArray(Integer[]::new);

			if (IS_DEBUG())
				for (int i = 0; i < offsets.length; ++i) {
					DEBUG("Offset at crossover station %s on circuit %s is %d",
							crossoverStationsInCircuit[i].getName(), circuit.getName(),
							offsets[i]);
				}

			if (offsets.length > 2) {
				DEBUG("Error circuit: There are more than 2 crossover stations on %s with circuit index %d. "
						+ "They should be splitted into circuits with at most 2 crossover stations",
						circuit.getName(), index);
				errorCircuitIndeces.add(++index);
			} else if (offsets.length > 1) {
				float scale = 1.0f;
				int offsetDiff = offsets[1] - offsets[0];
				int distDiff = crossoverStationsInCircuit[1].dist
						- crossoverStationsInCircuit[0].dist;
				if (distDiff == 0) {
					sb.append(String
							.format(__("The distance of two crossover stations on circuit %s are the same. Please modify them."),
									circuit.getName()));
					continue;
				}
				if (offsetDiff != 0)
					scale = offsetDiff * 1.0f / distDiff;
				int scale0Dist = crossoverStationsInCircuit[0].dist;
				circuit.dispScale = scale;

				circuit.getAllStations()
						.stream()
						.forEach(
								station -> station.dist = offsets[0]
										+ Math.round(circuit.dispScale
												* (station.dist - scale0Dist)));

				DEBUG("There are 2 crossover stations on circuit %s and thus apply a display scale rate %f",
						circuit, scale);
			} else if (offsets.length > 0) {
				circuit.getAllStations().stream()
						.forEach(station -> station.dist += offsets[0]);

				DEBUG("There are only 1 crossover stations on circuit %s and thus simply apply the offset",
						circuit);
			}
		}

		stationTable.updateUI();
	}

	protected void doRailNetwork_Command2() {
	}

	private void doRailNetwork_Complete() {
		mainFrame.trainGraph.currentLineChart.allCircuits.clear();
		mainFrame.trainGraph.currentLineChart.allCircuits.addAll(railroadLineTableModel.raillines);
		mainFrame.trainGraph.currentLineChart.railroadLine = railroadLineTableModel.raillines.get(0);
		
		mainFrame.trainGraph.railNetwork.replaceAllRailroadLines(
				railroadLineTableModel.raillines);

		if (tempCircuit != null) {
			tempCircuit = null;
		}

		RailroadLineEditView.this.setVisible(false);
		System.gc();
	}

	private void doRailNetwork_Cancel() {
		if (tempCircuit != null) {
			railroadLineTableModel.raillines.remove(tempCircuit);
			tempCircuit = null;

			switchRailLine(railroadLineTableModel.raillines.get(0), 0);
		}

		RailroadLineEditView.this.setVisible(false);
		System.gc();
	}
	
	public void updateRailroadListSelection(int index) {
		railroadLineTable.revalidate();

		int newIndex = index;
		if (index >= railroadLineTable.getRowCount()) {
			newIndex = railroadLineTable.getRowCount() - 1; 
		}
		railroadLineTable.setRowSelectionInterval(newIndex, newIndex);
		switchRailLine(railroadLineTableModel.raillines.get(newIndex),
				newIndex);
		
		mainFrame.navigator.updateNavigatorByRailNetwork();
	}

	private boolean isScaledCircuit(int circuitIndex) {
		return true;
	}

	private boolean isErrorCircuit(int circuitIndex) {
		return errorCircuitIndeces.contains(circuitIndex);
	}

	private boolean isCrossOverStation(String stationName) {
		return stationName != null
				&& crossoverStations.keySet().contains(stationName);
	}
}
