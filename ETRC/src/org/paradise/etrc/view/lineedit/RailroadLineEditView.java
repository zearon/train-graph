package org.paradise.etrc.view.lineedit;

import static org.paradise.etrc.ETRC.__;
import static org.paradise.etrc.ETRCUtil.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.data.util.BOMStripperInputStream;
import org.paradise.etrc.data.RailroadLineChart;
import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.data.Station;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.dialog.CircuitMakeDialog;
import org.paradise.etrc.dialog.InfoDialog;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.dialog.XianluSelectDialog;
import org.paradise.etrc.filter.CIRFilter;
import org.paradise.etrc.filter.CRSFilter;
import org.paradise.etrc.filter.CSVFilter;

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
		
		railroadLineTableModel.setRailLines(this.trainGraph.railNetwork
				.getAllRailroadLines());
		stationTableModel.setRailroadLine(this.trainGraph.railNetwork
				.getRailroadLine(0).copy());
		
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
			File recentPath = new File(mainFrame.prop.getProperty(
					MainFrame.Prop_Recent_Open_File_Path, ""));
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
				mainFrame.prop.setProperty(
						MainFrame.Prop_Recent_Open_File_Path, chooser
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
			File recentPath = new File(mainFrame.prop.getProperty(
					MainFrame.Prop_Recent_Open_File_Path, ""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {
		}

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			// System.out.println(f);

			BufferedReader in = null;
			Vector<RailroadLine> loadedCircuits = new Vector<RailroadLine>(8); // in most
																		// cases,
																		// there
																		// are
																		// no
																		// more
																		// than
																		// 8
																		// circuits.
			RailroadLine circuit = null;
			int lineNum = 0;
			try {
				in = new BufferedReader(new InputStreamReader(
						new BOMStripperInputStream(new FileInputStream(f)),
						"UTF-8"));
				String line = null;
				while ((line = in.readLine()) != null) {
					if (line.equalsIgnoreCase(RailroadLineChart.circuitPattern)) {
						circuit = TrainGraphFactory.createInstance(RailroadLine.class);
						loadedCircuits.add(circuit);
						lineNum = 0;
					} else {
						circuit.parseLine(line, lineNum++);
					}
				}

				if (loadedCircuits.size() < 1) {
					throw new IOException(__("Loaded circuits are empty."));
				}

				if (clearOriginalCircuits)
					circuits.clear();
				circuits.addAll(loadedCircuits);
				loadedCircuits.clear();

				mainFrame.prop.setProperty(
						MainFrame.Prop_Recent_Open_File_Path, chooser
								.getSelectedFile().getParentFile()
								.getAbsolutePath());
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
				new MessageBox(__("Cannot load circuits due to\r\n")
						+ ex.getMessage()).showMessage();
				;
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * doSaveCircuit
	 */
	private void doSaveCircuit(RailroadLine circuit) throws IOException {

		stationTableModel.railroadLine.name = tfName.getText();
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
			chooser.setDialogTitle(__("Save Circuit"));
			chooser.addChoosableFileFilter(new CIRFilter());
			suffix = CIRFilter.suffix;
			chooser.setSelectedFile(new File(circuit.name));
		} else if (circuits != null && circuits.size() > 0) {
			chooser.setDialogTitle(__("Save Circuits"));
			chooser.addChoosableFileFilter(new CRSFilter());
			suffix = CRSFilter.suffix;
			chooser.setSelectedFile(new File(mainFrame.getRailNetworkName()
					.replace(' ', '_')));
		} else {
			return;
		}
		chooser.setApproveButtonText(__("Save"));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(mainFrame.prop.getProperty(
					MainFrame.Prop_Recent_Open_File_Path, ""));
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
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(f), "UTF-8"));
				if (circuit != null)
					circuit.writeTo(out);
				else if (circuits != null)
					for (RailroadLine cir : circuits) {
						out.write(RailroadLineChart.circuitPattern);
						out.newLine();
						cir.writeTo(out);
					}
				out.close();
				mainFrame.prop.setProperty(
						MainFrame.Prop_Recent_Open_File_Path, chooser
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
		JButton btAddCircuit = new JButton(__("Add Circuit"));
		btAddCircuit.setFont(new Font("dialog", 0, 12));
		btAddCircuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_AddCircuit();
			}
		});

		JButton btImportCircuit = new JButton(__("Import Circuit"));
		btImportCircuit.setFont(new Font("dialog", 0, 12));
		btImportCircuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_ImportCircuit();
			}
		});

		JButton btMoveUp = new JButton(__("Move Up"));
		btMoveUp.setFont(new Font("dialog", 0, 12));
		btMoveUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_MoveUpCircuit();
			}
		});

		JButton btMoveDown = new JButton(__("Move Down"));
		btMoveDown.setFont(new Font("dialog", 0, 12));
		btMoveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_MoveDownCircuit();
			}
		});

		JButton btRemoveCircuit = new JButton(__("Remove Circuit"));
		btRemoveCircuit.setFont(new Font("dialog", 0, 12));
		btRemoveCircuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_RemoveCircuit();
			}
		});

		JButton btNewCircuits = new JButton(__("New Circuits"));
		btNewCircuits.setFont(new Font("dialog", 0, 12));
		btNewCircuits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_NewCircuit();
			}
		});

		JButton btSaveCircuits = new JButton(__("Save Circuits"));
		btSaveCircuits.setFont(new Font("dialog", 0, 12));
		btSaveCircuits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_SaveCircuits();
			}
		});

		JButton btLoadCircuits = new JButton(__("Load Circuits"));
		btLoadCircuits.setFont(new Font("dialog", 0, 12));
		btLoadCircuits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_LoadCircuits();
			}
		});

		JButton btComplete = new JButton(__("Complete"));
		btComplete.setFont(new Font("dialog", 0, 12));
		btComplete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRailNetwork_Complete();
			}
		});

		JButton btCancel = new JButton(__("Cancel"));
		btCancel.setFont(new Font("dialog", 0, 12));
		btCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRailNetwork_Cancel();
			}
		});

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
		buttonPanel.setLayout(new GridLayout(2, 6));
		buttonPanel.add(btMoveUp);
		buttonPanel.add(btAddCircuit);
		buttonPanel.add(btImportCircuit);
		buttonPanel.add(btSaveCircuits);
		buttonPanel.add(btAdjust);
		buttonPanel.add(btComplete);

		buttonPanel.add(btMoveDown);
		buttonPanel.add(btRemoveCircuit);
		buttonPanel.add(btNewCircuits);
		buttonPanel.add(btLoadCircuits);
		buttonPanel.add(btCommand2);
		buttonPanel.add(btCancel);

//		JPanel rootPanel = new JPanel();
		setLayout(new BorderLayout());
		add(buildCircuitListPanel(), BorderLayout.WEST);
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
		railroadLineTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		railroadLineTable.getColumnModel().getColumn(1).setPreferredWidth(160);
		railroadLineTable.getColumnModel().getColumn(2).setPreferredWidth(60);

		railroadLineTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						// Selected circirt changed in circirt table
						if (arg0.getValueIsAdjusting()) {
							doRailNetwork_ChangeCircuit();
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
				} else if (row == 0) {
					setBackground(Color.YELLOW);
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
//		}
	}

	private JPanel buildCircuitPanel() {
//		ckSuccesiveChange = new JCheckBox(__("Succesive Change"), false);

		buildStationTable();

		JButton btOK = new JButton(__("OK"));
		btOK.setFont(new Font("dialog", 0, 12));
		btOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// OK button pressed
				doRailline_OK();
			}
		});

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

		JLabel lbName = new JLabel(__("Circuit Name:"));
		lbName.setFont(new Font("dialog", 0, 12));

		tfName = new JTextField(12);
		tfName.setFont(new Font("dialog", 0, 12));
		tfName.setText(stationTableModel.railroadLine.name);
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

		JLabel lbLengthLabel = new JLabel(__("Total Length:"));
		lbLength = new JLabel("0 " + __("km"));

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
		namePanel.add(lbName);
		namePanel.add(tfName);
		namePanel.add(lbMultiplicity);
		namePanel.add(cbMultiplicity);
		namePanel.add(lbDispScale);
		namePanel.add(tfDispScale);
		namePanel.setBorder(new EmptyBorder(1, 1, 1, 1));

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
		buttonPanel1.add(btOK);
		buttonPanel1.add(new JPanel().add(new JLabel(" ")));

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(stationTable.getContainerPanel(), BorderLayout.CENTER);
//		contentPanel.add(tipPanel, BorderLayout.SOUTH);
		contentPanel.add(namePanel, BorderLayout.NORTH);

		JPanel circuitPanel = new JPanel();
		circuitPanel.setLayout(new BorderLayout());
		circuitPanel.add(buttonPanel1, BorderLayout.EAST);
		circuitPanel.add(contentPanel, BorderLayout.CENTER);
		// circuitPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

		return circuitPanel;
	}

	private void buildStationTable() {
		stationTable = new StationTable();
		stationTable.setFont(new Font("Dialog", 0, 12));
		stationTable.getTableHeader().setFont(new Font("Dialog", 0, 12));

		stationTableModel = new StationTableModel(stationTable, null);
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
		railroadLineTable.revalidate();
	}

	protected void switchRailLine(RailroadLine circuit, int circuitIindex) {
		selectedCircuitIndex = circuitIindex;

		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor().stopCellEditing();

		tfName.setText(circuit.name);
		cbMultiplicity.setSelectedItem(circuit.multiplicity);
		tfDispScale.setText("" + circuit.dispScale);
		stationTableModel.railroadLine = circuit;

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

	public void resetModel(RailroadLine circuit) {
		railroadLineTableModel.raillines.clear();
		railroadLineTableModel.raillines.addAll(
				trainGraph.railNetwork.getAllRailroadLines());

		if (circuit != null) {
			this.tempCircuit = circuit;
			circuit.zindex = 1;
			railroadLineTableModel.raillines.add(circuit);
			int index = railroadLineTableModel.raillines.size() - 1;
			railroadLineTable.setRowSelectionInterval(index, index);
			railroadLineTable.updateUI();

			switchRailLine(circuit, index);
		} else {
			circuit = railroadLineTableModel.raillines.get(0);
			railroadLineTable.setRowSelectionInterval(0, 0);
			railroadLineTable.updateUI();

			switchRailLine(circuit.copy(), 0);
		}

		findCrossoverStations();
	}

	private void findCrossoverStations() {
		// Find out crossover stations
		stationNames.clear();
		railroadLineTableModel.raillines.stream()
				.flatMap(cir -> cir.getAllStations().stream())
				.map(station -> station.name).forEachOrdered(name -> {
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
						station.name))
				.distinct()
				.forEachOrdered(station -> {
					// DEBUG("Crossover station: %s@%dkm", station.name,
					// station.dist);
						crossoverStations.compute(station.name,
								(k, v) -> station.dist);
					});
	}

	private void doRailNetwork_ChangeCircuit() {
		int circuitIndex = railroadLineTable.getSelectedRow();
		RailroadLine circuitInTable = railroadLineTableModel.raillines.get(circuitIndex);
		RailroadLine circuitForEdit = circuitInTable.copy();
		switchRailLine(circuitForEdit, circuitIndex);
	}
	
	private void doRailline_UpdateName(String oldValue) {
		RailroadLine line = ((StationTableModel) stationTable.getModel()).railroadLine;
		String newValue = tfName.getText();
		
		ActionFactory.createSetValueActionAndDoIt(__("railroad line name"), 
				oldValue, newValue, value -> {
					
			line.name = (String) value;
			tfName.setText((String) value);
		});
	}
	
	private void doRailline_UpdateDisplayScale(String oldValue) {
		RailroadLine line = ((StationTableModel) stationTable.getModel()).railroadLine;
		String newValue = tfDispScale.getText();
		
		ActionFactory.createSetValueActionAndDoIt(__("display scale of railroad line"), 
				oldValue, newValue, value -> {
					
			try {
				line.dispScale = Float.parseFloat((String) value);

				tfDispScale.setText((String) value);
				((AbstractTableModel) stationTable.getModel()).fireTableDataChanged();
			} catch (NumberFormatException e) {
				new MessageBox(__("Display scale should be a decimal."))
						.showMessage();
			}	
		});
	}
	
	private void doRailline_UpdateMultiplicity(String oldValue) {
		RailroadLine line = ((StationTableModel) stationTable.getModel()).railroadLine;
		String newValue = cbMultiplicity.getSelectedItem().toString();
		
		ActionFactory.createSetValueActionAndDoIt(__("track count of railroad line"), 
				oldValue, newValue, value -> {
					
			try {
				line.multiplicity = Integer.parseInt((String) value);

				cbMultiplicity.setSelectedItem((String) value);
			} catch (NumberFormatException e) {
				new MessageBox(__("Rail count should be an integer."))
						.showMessage();
			}
		});
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
		c.name = tfName.getText();
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
			mainFrame.isNewCircuit = true;
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
		for (int i = selectedRows.length - 1; i >= 0; --i) {
			((StationTableModel) stationTable.getModel()).railroadLine
					.delStation(selectedRows[i]);
		}
		// System.out.println(((StationTableModel)table.getModel()).circuit);

		stationTable.revalidate();
		stationTable.updateUI();
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
		
		ActionFactory.createTableElementMoveActionAndDoIt(__("station table"), 
				stationTable, stationTableModel.railroadLine.getAllStations(), 
				selectedStatonIndex, selectedStatonIndex - 1, true);
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
		
		ActionFactory.createTableElementMoveActionAndDoIt(__("station table"), 
				stationTable, stationTableModel.railroadLine.getAllStations(), 
				selectedStatonIndex, selectedStatonIndex + 1, true);
	}

	protected void doRailline_RevertStation() {
		// TODO: Revert station
		RailroadLine circuit = stationTableModel.railroadLine;
		int stationCount = circuit.getStationNum();

		for (int i = 0, j = stationCount - 1; i < j; ++i, --j) {
			Station station1 = circuit.getStation(i);
			Station station2 = circuit.getStation(j);
			int tempDist = station2.dist;
			station2.dist = station1.dist;
			station1.dist = tempDist;

			circuit.delStation(j);
			circuit.delStation(i);
			circuit.insertStation(station2, i);
			circuit.insertStation(station1, j);
		}

		int rowSelection = stationTable.getSelectedRow();
		if (rowSelection >= 0) {
			rowSelection = stationCount - 1 - rowSelection;
			stationTable.setRowSelectionInterval(rowSelection, rowSelection);
		}
		stationTable.updateUI();
	}

	protected void doRailline_NormalizeDistance() {
		RailroadLine c = stationTableModel.railroadLine;
		int offset = c.getStation(0).dist;
		if (offset != 0) {
			// if (new YesNoBox(mainFrame,
			// __("The distance of the first station is not zero, do normalization?")).askForYes())
			// {
			for (int i = 0; i < c.getStationNum(); ++i) {
				c.getStation(i).dist -= offset;
			}
			// }
		}

		stationTable.updateUI();
	}

	private void doRailline_InsertStation() {
		// table.getCellEditor().stopCellEditing();
		RailroadLine cir = stationTableModel.railroadLine;
		int selectedIndex = stationTable.getSelectedRow();
		if (selectedIndex < 0) {
			new MessageBox(__("Please choose a station first.")).showMessage();
			;
			return;
		}

		String name = __("Station");
		int dist = cir.getStation(selectedIndex).dist;
		int level = cir.getStation(selectedIndex).level;
		boolean hide = false;
		cir.insertStation(TrainGraphFactory.createInstance(Station.class, name)
				.setProperties(dist, level, hide), 
				selectedIndex);
		// System.out.println(cir);

		stationTable.revalidate();
		stationTable.updateUI();
	}

	private void doRailline_AddStation() {
		// table.getCellEditor().stopCellEditing();
		RailroadLine cir = stationTableModel.railroadLine;
		int selectedIndex = stationTable.getSelectedRow();
		if (selectedIndex < 0) {
			new MessageBox(__("Please choose a station first.")).showMessage();
			;
			return;
		}

		String name = __("Station");
		int dist = cir.getStation(selectedIndex).dist;
		int level = cir.getStation(selectedIndex).level;
		boolean hide = false;
		cir.insertStation(TrainGraphFactory.createInstance(Station.class, name)
				.setProperties(dist, level, hide),
				selectedIndex + 1);
		// System.out.println(cir);

		stationTable.revalidate();
		stationTable.updateUI();
	}

	private void doRailNetwork_AddCircuit() {
		// Add a circuit
		RailroadLine newCircuit = TrainGraphFactory.createInstance(RailroadLine.class, 
				"New Circuit " + circuitNum++);
		railroadLineTableModel.raillines.add(newCircuit.copy());

		int circuitIndex = railroadLineTableModel.raillines.size() - 1;
		railroadLineTable.setRowSelectionInterval(circuitIndex, circuitIndex);
		railroadLineTable.updateUI();

		switchRailLine(newCircuit, circuitIndex);
	}

	private void doRailNetwork_ImportCircuit() {
		// Import a circuit
		// TODO: 8. Check station name duplication on clicking OK button
		// TODO: 9. Set circuit crossover
		String xianlu = new XianluSelectDialog(mainFrame).getXianlu();
		if (xianlu == null)
			return;

		RailroadLine circuit = new CircuitMakeDialog(mainFrame, xianlu).getCircuit();
		if (circuit == null)
			return;

		circuit.zindex = 1;
		railroadLineTableModel.raillines.add(circuit);
		int index0 = railroadLineTableModel.raillines.size() - 1;
		railroadLineTable.setRowSelectionInterval(index0, index0);
		railroadLineTable.updateUI();

		switchRailLine(circuit, index0);
	}

	private void doRailNetwork_MoveUpCircuit() {
		// Move up a circuit
		int selectedCircuitIndex = railroadLineTable.getSelectedRow();
		if (selectedCircuitIndex == 0) {
			new MessageBox(
					__("This is already the first circuit and thus cannot be moved up any more."))
					.showMessage();
			;
			return;
		}
		
		ActionFactory.createTableElementMoveActionAndDoIt(__("railroad line table"), 
				railroadLineTable, railroadLineTableModel.raillines, 
				selectedCircuitIndex, selectedCircuitIndex - 1, true);
	}

	private void doRailNetwork_MoveDownCircuit() {
		// Move down a circuit
		int selectedCircuitIndex = railroadLineTable.getSelectedRow();
		if (selectedCircuitIndex == railroadLineTableModel.raillines.size() - 1) {
			new MessageBox(
					__("This is already the last circuit and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		
		ActionFactory.createTableElementMoveActionAndDoIt(__("railroad line table"), 
				railroadLineTable, railroadLineTableModel.raillines, 
				selectedCircuitIndex, selectedCircuitIndex + 1, true);
	}

	private void doRailNetwork_RemoveCircuit() {
		// Remove a circuit
		int selectedCircuitIndex = railroadLineTable.getSelectedRow();
		if (selectedCircuitIndex == 0 && railroadLineTableModel.raillines.size() == 1) {
			new MessageBox(__("Cannot remove the last circuit.")).showMessage();
			;
			return;
		}
		railroadLineTableModel.raillines.remove(selectedCircuitIndex);
		if (selectedCircuitIndex >= railroadLineTableModel.raillines.size())
			--selectedCircuitIndex;
		railroadLineTable.setRowSelectionInterval(selectedCircuitIndex,
				selectedCircuitIndex);
		railroadLineTable.updateUI();

		switchRailLine(railroadLineTableModel.raillines.get(selectedCircuitIndex),
				selectedCircuitIndex);
	}

	private void doRailNetwork_NewCircuit() {
		// Create new circuits with only one circuit
		RailroadLine newCircuit = TrainGraphFactory.createInstance(RailroadLine.class,
				"New Circuit " + circuitNum++);
		railroadLineTableModel.raillines.clear();
		railroadLineTableModel.raillines.add(newCircuit);
		railroadLineTable.updateUI();

		int circuitIndex = railroadLineTableModel.raillines.size() - 1;
		railroadLineTable.setRowSelectionInterval(circuitIndex, circuitIndex);
		switchRailLine(newCircuit, circuitIndex);
	}

	private void doRailNetwork_SaveCircuits() {
		// Save all circuits
		if (railroadLineTableModel.raillines.size() == 0) {
			new MessageBox(__("Cannot save empty circuits.")).showMessage();
			;
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

	private void doRailNetwork_LoadCircuits() {
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
		// TODO: ERROR
		StringBuilder sb = new StringBuilder();
		int index = -1;
		for (RailroadLine circuit : railroadLineTableModel.raillines) {

			// Calculate offset(s) in crossover station(s)
			Station[] crossoverStationsInCircuit = circuit
					.getAllStations()
					.stream()
					.filter(station -> crossoverStations.keySet().contains(
							station.name)).toArray(Station[]::new);
			Integer[] offsets = Stream
					.of(crossoverStationsInCircuit)
					.map(station -> (crossoverStations.get(station.name) - station.dist))
					.toArray(Integer[]::new);

			if (IS_DEBUG())
				for (int i = 0; i < offsets.length; ++i) {
					DEBUG("Offset at crossover station %s on circuit %s is %d",
							crossoverStationsInCircuit[i].name, circuit.name,
							offsets[i]);
				}

			if (offsets.length > 2) {
				DEBUG("Error circuit: There are more than 2 crossover stations on %s with circuit index %d. "
						+ "They should be splitted into circuits with at most 2 crossover stations",
						circuit.name, index);
				errorCircuitIndeces.add(++index);
			} else if (offsets.length > 1) {
				float scale = 1.0f;
				int offsetDiff = offsets[1] - offsets[0];
				int distDiff = crossoverStationsInCircuit[1].dist
						- crossoverStationsInCircuit[0].dist;
				if (distDiff == 0) {
					sb.append(String
							.format(__("The distance of two crossover stations on circuit %s are the same. Please modify them."),
									circuit.name));
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
		mainFrame.currentLineChart.allCircuits.clear();
		mainFrame.currentLineChart.allCircuits.addAll(railroadLineTableModel.raillines);
		mainFrame.currentLineChart.railroadLine = railroadLineTableModel.raillines.get(0);
		
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
