package org.paradise.etrc.dialog;

import static org.paradise.etrc.ETRC.__;
import static org.paradise.etrc.ETRCUtil.DEBUG;

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
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.BOMStripperInputStream;
import org.paradise.etrc.data.Chart;
import org.paradise.etrc.data.Circuit;
import org.paradise.etrc.data.Station;
import org.paradise.etrc.filter.CIRFilter;
import org.paradise.etrc.filter.CRSFilter;
import org.paradise.etrc.filter.CSVFilter;
import org.paradise.etrc.view.widget.DefaultJEditTableModel;
import org.paradise.etrc.view.widget.JEditTable;

public class CircuitEditDialog extends JDialog {
	private static final long serialVersionUID = 8501387955756137148L;
	MainFrame mainFrame;
	Vector<Circuit> circuitsInChart;

	CircuitTable circuitTable;
	CircuitTableModel circuitTableModel;
	StationTable stationTable;
	StationTableModel stationTableModel;

	private JTextField tfName;
	private JLabel lbLength;
	private JComboBox<String> cbMultiplicity;
	private JTextField tfDispScale;
//	private JCheckBox ckSuccesiveChange;

	private int circuitNum = 0;
	private Circuit tempCircuit;
	private int selectedCircuitIndex = 0;

	private ArrayList<Integer> scaledCircuitIndeces = new ArrayList<Integer>(8);
	private ArrayList<Integer> errorCircuitIndeces = new ArrayList<Integer>(8);
	private Hashtable<String, Integer> crossoverStations = new Hashtable<String, Integer>(
			16);
	private LinkedHashSet<String> stationNames = new LinkedHashSet<String>(100);

	public CircuitEditDialog(MainFrame _mainFrame,
			Vector<Circuit> existingCircuits) {
		super(_mainFrame, __("Railway Circuits: ")
				+ _mainFrame.getRailNetworkName(), true);

		mainFrame = _mainFrame;
		circuitsInChart = existingCircuits;

		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * doLoadCircuit
	 */
	private Circuit doLoadCircuit() {
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

			Circuit c = new Circuit();
			try {
				c.loadFromFile(f.getAbsolutePath());
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
	private void doLoadCircuits(Vector<Circuit> circuits,
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
			Vector<Circuit> loadedCircuits = new Vector<Circuit>(8); // in most
																		// cases,
																		// there
																		// are
																		// no
																		// more
																		// than
																		// 8
																		// circuits.
			Circuit circuit = null;
			int lineNum = 0;
			try {
				in = new BufferedReader(new InputStreamReader(
						new BOMStripperInputStream(new FileInputStream(f)),
						"UTF-8"));
				String line = null;
				while ((line = in.readLine()) != null) {
					if (line.equalsIgnoreCase(Chart.circuitPattern)) {
						circuit = new Circuit();
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
	private void doSaveCircuit(Circuit circuit) throws IOException {

		stationTableModel.circuit.name = tfName.getText();
		try {
			stationTableModel.circuit.multiplicity = Integer
					.parseInt(cbMultiplicity.getSelectedItem().toString());
		} catch (NumberFormatException e) {
			throw new IOException(__("Invalid rail count. It should be 1/2/4"));
		}
		try {
			stationTableModel.circuit.dispScale = Float.parseFloat(tfDispScale
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
	private void doSaveCircuits(Circuit circuit, Vector<Circuit> circuits)
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
					for (Circuit cir : circuits) {
						out.write(Chart.circuitPattern);
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
				doCircutis_AddCircuit();
			}
		});

		JButton btImportCircuit = new JButton(__("Import Circuit"));
		btImportCircuit.setFont(new Font("dialog", 0, 12));
		btImportCircuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_ImportCircuit();
			}
		});

		JButton btMoveUp = new JButton(__("Move Up"));
		btMoveUp.setFont(new Font("dialog", 0, 12));
		btMoveUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_MoveUpCircuit();
			}
		});

		JButton btMoveDown = new JButton(__("Move Down"));
		btMoveDown.setFont(new Font("dialog", 0, 12));
		btMoveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_MoveDownCircuit();
			}
		});

		JButton btRemoveCircuit = new JButton(__("Remove Circuit"));
		btRemoveCircuit.setFont(new Font("dialog", 0, 12));
		btRemoveCircuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_RemoveCircuit();
			}
		});

		JButton btNewCircuits = new JButton(__("New Circuits"));
		btNewCircuits.setFont(new Font("dialog", 0, 12));
		btNewCircuits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_NewCircuit();
			}
		});

		JButton btSaveCircuits = new JButton(__("Save Circuits"));
		btSaveCircuits.setFont(new Font("dialog", 0, 12));
		btSaveCircuits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_SaveCircuits();
			}
		});

		JButton btLoadCircuits = new JButton(__("Load Circuits"));
		btLoadCircuits.setFont(new Font("dialog", 0, 12));
		btLoadCircuits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_LoadCircuits();
			}
		});

		JButton btComplete = new JButton(__("Complete"));
		btComplete.setFont(new Font("dialog", 0, 12));
		btComplete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_Complete();
			}
		});

		JButton btCancel = new JButton(__("Cancel"));
		btCancel.setFont(new Font("dialog", 0, 12));
		btCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuits_Cancel();
			}
		});

		JButton btAdjust = new JButton(__("Adjust"));
		btAdjust.setFont(new Font("dialog", 0, 12));
		btAdjust.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuits_Calculate();
			}
		});

		JButton btCommand2 = new JButton(__("Command2"));
		btCommand2.setFont(new Font("dialog", 0, 12));
		btCommand2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuits_Command2();
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

		JPanel rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.add(buildCircuitListPanel(), BorderLayout.WEST);
		rootPanel.add(buildCircuitPanel(), BorderLayout.CENTER);
		rootPanel.add(buttonPanel, BorderLayout.SOUTH);

		getContentPane().add(rootPanel);
	}

	private JPanel buildCircuitListPanel() {

		buildCircuitTable();

		JScrollPane spCircuitList = new JScrollPane(circuitTable);

		JPanel circuitPanel = new JPanel();
		circuitPanel.setLayout(new BorderLayout());
		circuitPanel.add(spCircuitList, BorderLayout.CENTER);

		return circuitPanel;
	}

	private void buildCircuitTable() {
		circuitTable = new CircuitTable();
		circuitTable.setFont(new Font("Dialog", 0, 12));
		circuitTable.getTableHeader().setFont(new Font("Dialog", 0, 12));

		circuitTableModel = new CircuitTableModel(circuitsInChart);
		circuitTable.setModel(circuitTableModel);
		circuitTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		circuitTable.getColumnModel().getColumn(1).setPreferredWidth(160);
		circuitTable.getColumnModel().getColumn(2).setPreferredWidth(60);

		circuitTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						// Selected circirt changed in circirt table
						if (arg0.getValueIsAdjusting()) {
							doCircuits_ChangeCircuit();
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
			circuitTable.getColumn(circuitTable.getColumnName(1))
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
				doCircuit_OK();
			}
		});

		JButton btMoveUp = new JButton(__("Move Up"));
		btMoveUp.setFont(new Font("dialog", 0, 12));
		btMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// doCircuit_Load();
				doCircuit_MoveUpStation();
			}
		});

		JButton btMoveDown = new JButton(__("Move Down"));
		btMoveDown.setFont(new Font("dialog", 0, 12));
		btMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// doCircuit_Save();
				doCircuit_MoveDownStation();
			}
		});

		JButton btDel = new JButton(__("Delete"));
		btDel.setFont(new Font("dialog", 0, 12));
		btDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_DeleteStation();
			}
		});

		JButton btRevert = new JButton(__("Revert"));
		btRevert.setFont(new Font("dialog", 0, 12));
		btRevert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_RevertStation();
			}
		});

		JButton btNormalize = new JButton(__("Normalize"));
		btNormalize.setFont(new Font("dialog", 0, 12));
		btNormalize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_NormalizeDistance();
			}
		});

		JButton btInsert = new JButton(__("Insert"));
		btInsert.setFont(new Font("dialog", 0, 12));
		btInsert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_InsertStation();
			}
		});

		JButton btAdd = new JButton(__("Add"));
		btAdd.setFont(new Font("dialog", 0, 12));
		btAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_AddStation();
			}
		});

		JLabel lbName = new JLabel(__("Circuit Name:"));
		lbName.setFont(new Font("dialog", 0, 12));

		tfName = new JTextField(12);
		tfName.setFont(new Font("dialog", 0, 12));
		tfName.setText(stationTableModel.circuit.name);

		JLabel lbLengthLabel = new JLabel(__("Total Length:"));
		lbLength = new JLabel("0 " + __("km"));

		JLabel lbMultiplicity = new JLabel(__("Rail Count:"));
		lbName.setFont(new Font("dialog", 0, 12));

		cbMultiplicity = new JComboBox<String>();
		cbMultiplicity.setFont(new Font("dialog", 0, 12));
		cbMultiplicity.setEditable(true);
		cbMultiplicity.addItem(__("1"));
		cbMultiplicity.addItem(__("2"));
		cbMultiplicity.addItem(__("4"));
		cbMultiplicity.setSelectedItem(stationTableModel.circuit.multiplicity);

		JLabel lbDispScale = new JLabel(__("Display Scale:"));
		lbName.setFont(new Font("dialog", 0, 12));

		tfDispScale = new JTextField(2);
		tfDispScale.setFont(new Font("dialog", 0, 12));
		tfDispScale.setText("" + stationTableModel.circuit.dispScale);
		tfDispScale.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				stationTableModel.circuit.dispScale = Float
						.parseFloat(tfDispScale.getText());
				updateDisplayDistance();
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				//
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

		stationTableModel = new StationTableModel(
				circuitsInChart.get(0).copy());
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

	protected void switchCircuit(Circuit circuit, int circuitIindex) {
		selectedCircuitIndex = circuitIindex;

		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor().stopCellEditing();

		tfName.setText(circuit.name);
		cbMultiplicity.setSelectedItem(circuit.multiplicity);
		tfDispScale.setText("" + circuit.dispScale);
		stationTableModel.circuit = circuit;

		stationTable.revalidate();
		stationTable.updateUI();
		
		System.gc();
	}

	protected void updateDisplayDistance() {
		stationTable.updateUI();
	}

	public void showDialog() {
		showDialogForCircuit(null);
	}

	public void showDialogForCircuit(Circuit circuit) {
		Dimension dlgSize = this.getPreferredSize();
		Dimension frmSize = mainFrame.getSize();
		Point loc = mainFrame.getLocation();
		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);

		this.setModal(true);
		this.pack();

		initModel(circuit);

		this.setVisible(true);
	}

	private void initModel(Circuit circuit) {
		circuitTableModel.circuits.clear();
		circuitTableModel.circuits.addAll(circuitsInChart);

		if (circuit != null) {
			this.tempCircuit = circuit;
			circuit.zindex = 1;
			circuitTableModel.circuits.add(circuit);
			int index = circuitTableModel.circuits.size() - 1;
			circuitTable.setRowSelectionInterval(index, index);
			circuitTable.updateUI();

			switchCircuit(circuit, index);
		} else {
			circuit = circuitTableModel.circuits.get(0);
			circuitTable.setRowSelectionInterval(0, 0);
			circuitTable.updateUI();

			switchCircuit(circuit.copy(), 0);
		}

		findCrossoverStations();
	}

	private void findCrossoverStations() {
		// Find out crossover stations
		stationNames.clear();
		circuitTableModel.circuits.stream()
				.flatMap(cir -> cir.getAllStations().stream())
				.map(station -> station.name).forEachOrdered(name -> {
					if (stationNames.contains(name)) {
						crossoverStations.put(name, 0);
					}
					stationNames.add(name);
				});

		// Record distance of crossover stations
		circuitTableModel.circuits
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

	private void doCircuits_ChangeCircuit() {
		int circuitIndex = circuitTable.getSelectedRow();
		Circuit circuitInTable = circuitTableModel.circuits.get(circuitIndex);
		Circuit circuitForEdit = circuitInTable.copy();
		switchCircuit(circuitForEdit, circuitIndex);
	}

	private void doCircuit_OK() {
		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor().stopCellEditing();

		// Normalize
		Circuit c = ((StationTableModel) stationTable.getModel()).circuit;
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

		int selectedCircuitIndex = circuitTable.getSelectedRow();
		if (selectedCircuitIndex >= 0)
			circuitTableModel.circuits.set(selectedCircuitIndex, c);

		circuitTable.updateUI();
	}

	private void doCircuit_Load() {
		Circuit cir = doLoadCircuit();
		if (cir != null) {
			switchCircuit(cir, selectedCircuitIndex);
			mainFrame.isNewCircuit = true;
		}
	}

	private void doCircuit_Save() {
		try {
			doSaveCircuit(stationTableModel.circuit);
		} catch (IOException e1) {
			e1.printStackTrace();
			new MessageBox(__("Cannot save the circuit due to: \r\n")
					+ e1.getMessage()).showMessage();
			;
		}
	}

	private void doCircuit_DeleteStation() {
		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor().stopCellEditing();

		int[] selectedRows = stationTable.getSelectedRows();
		for (int i = selectedRows.length - 1; i >= 0; --i) {
			((StationTableModel) stationTable.getModel()).circuit
					.delStation(selectedRows[i]);
		}
		// System.out.println(((StationTableModel)table.getModel()).circuit);

		stationTable.revalidate();
		stationTable.updateUI();
	}

	private void doCircuit_MoveUpStation() {
		// Move down a station
		int selectedStatonIndex = stationTable.getSelectedRow();
		if (selectedStatonIndex == 0) {
			new MessageBox(
					__("This is already the first circuit and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		Station stationMoved = stationTableModel.circuit.getAllStations()
				.remove(selectedStatonIndex);
		stationTableModel.circuit.getAllStations().insertElementAt(
				stationMoved, selectedStatonIndex - 1);

		stationTable.setRowSelectionInterval(selectedStatonIndex - 1,
				selectedStatonIndex - 1);
		stationTable.updateUI();
	}

	private void doCircuit_MoveDownStation() {
		// Move down a station
		int selectedStatonIndex = stationTable.getSelectedRow();
		if (selectedStatonIndex == stationTableModel.circuit.getAllStations()
				.size() - 1) {
			new MessageBox(
					__("This is already the last station and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		Station stationMoved = stationTableModel.circuit.getAllStations()
				.remove(selectedStatonIndex);
		stationTableModel.circuit.getAllStations().insertElementAt(
				stationMoved, selectedStatonIndex + 1);

		stationTable.setRowSelectionInterval(selectedStatonIndex + 1,
				selectedStatonIndex + 1);
		stationTable.updateUI();
	}

	protected void doCircuit_RevertStation() {
		// TODO: Revert station
		Circuit circuit = stationTableModel.circuit;
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

	protected void doCircuit_NormalizeDistance() {
		Circuit c = stationTableModel.circuit;
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

	private void doCircuit_InsertStation() {
		// table.getCellEditor().stopCellEditing();
		Circuit cir = stationTableModel.circuit;
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
		cir.insertStation(new Station(name, dist, level, hide), selectedIndex);
		// System.out.println(cir);

		stationTable.revalidate();
		stationTable.updateUI();
	}

	private void doCircuit_AddStation() {
		// table.getCellEditor().stopCellEditing();
		Circuit cir = stationTableModel.circuit;
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
		cir.insertStation(new Station(name, dist, level, hide),
				selectedIndex + 1);
		// System.out.println(cir);

		stationTable.revalidate();
		stationTable.updateUI();
	}

	private void doCircutis_AddCircuit() {
		// Add a circuit
		Circuit newCircuit = new Circuit("New Circuit " + circuitNum++);
		circuitTableModel.circuits.add(newCircuit.copy());

		int circuitIndex = circuitTableModel.circuits.size() - 1;
		circuitTable.setRowSelectionInterval(circuitIndex, circuitIndex);
		circuitTable.updateUI();

		switchCircuit(newCircuit, circuitIndex);
	}

	private void doCircuits_ImportCircuit() {
		// Import a circuit
		// TODO: 8. Check station name duplication on clicking OK button
		// TODO: 9. Set circuit crossover
		String xianlu = new XianluSelectDialog(mainFrame).getXianlu();
		if (xianlu == null)
			return;

		Circuit circuit = new CircuitMakeDialog(mainFrame, xianlu).getCircuit();
		if (circuit == null)
			return;

		circuit.zindex = 1;
		circuitTableModel.circuits.add(circuit);
		int index0 = circuitTableModel.circuits.size() - 1;
		circuitTable.setRowSelectionInterval(index0, index0);
		circuitTable.updateUI();

		switchCircuit(circuit, index0);
	}

	private void doCircuits_MoveUpCircuit() {
		// Move up a circuit
		int selectedCircuitIndex = circuitTable.getSelectedRow();
		if (selectedCircuitIndex == 0) {
			new MessageBox(
					__("This is already the first circuit and thus cannot be moved up any more."))
					.showMessage();
			;
			return;
		}
		Circuit circuitMoved = circuitTableModel.circuits
				.remove(selectedCircuitIndex);
		circuitTableModel.circuits.insertElementAt(circuitMoved,
				selectedCircuitIndex - 1);

		circuitTable.setRowSelectionInterval(selectedCircuitIndex - 1,
				selectedCircuitIndex - 1);
		circuitTable.updateUI();
	}

	private void doCircuits_MoveDownCircuit() {
		// Move down a circuit
		int selectedCircuitIndex = circuitTable.getSelectedRow();
		if (selectedCircuitIndex == circuitTableModel.circuits.size() - 1) {
			new MessageBox(
					__("This is already the last circuit and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		Circuit circuitMoved = circuitTableModel.circuits
				.remove(selectedCircuitIndex);
		circuitTableModel.circuits.insertElementAt(circuitMoved,
				selectedCircuitIndex + 1);

		circuitTable.setRowSelectionInterval(selectedCircuitIndex + 1,
				selectedCircuitIndex + 1);
		circuitTable.updateUI();
	}

	private void doCircuits_RemoveCircuit() {
		// Remove a circuit
		int selectedCircuitIndex = circuitTable.getSelectedRow();
		if (selectedCircuitIndex == 0 && circuitTableModel.circuits.size() == 1) {
			new MessageBox(__("Cannot remove the last circuit.")).showMessage();
			;
			return;
		}
		circuitTableModel.circuits.remove(selectedCircuitIndex);
		if (selectedCircuitIndex >= circuitTableModel.circuits.size())
			--selectedCircuitIndex;
		circuitTable.setRowSelectionInterval(selectedCircuitIndex,
				selectedCircuitIndex);
		circuitTable.updateUI();

		switchCircuit(circuitTableModel.circuits.get(selectedCircuitIndex),
				selectedCircuitIndex);
	}

	private void doCircuits_NewCircuit() {
		// Create new circuits with only one circuit
		Circuit newCircuit = new Circuit("New Circuit " + circuitNum++);
		circuitTableModel.circuits.clear();
		circuitTableModel.circuits.add(newCircuit);
		circuitTable.updateUI();

		int circuitIndex = circuitTableModel.circuits.size() - 1;
		circuitTable.setRowSelectionInterval(circuitIndex, circuitIndex);
		switchCircuit(newCircuit, circuitIndex);
	}

	private void doCircuits_SaveCircuits() {
		// Save all circuits
		if (circuitTableModel.circuits.size() == 0) {
			new MessageBox(__("Cannot save empty circuits.")).showMessage();
			;
			return;
		}
		try {
			doSaveCircuits(null, circuitTableModel.circuits);
		} catch (IOException e) {
			e.printStackTrace();
			InfoDialog
					.showErrorDialog(
							mainFrame,
							__("Cannot save the circuit due to: \r\n")
									+ e.getMessage());
		}
	}

	private void doCircuits_LoadCircuits() {
		// Load all circuits
		try {
			doLoadCircuits(circuitTableModel.circuits, true);
			circuitTable.updateUI();

			switchCircuit(circuitTableModel.circuits.get(0), 0);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			new MessageBox(__("Cannot load circuits due to\r\n")
					+ e.getMessage()).showMessage();
			;
		}
	}

	protected void doCircuits_Calculate() {
		// TODO: ERROR
		StringBuilder sb = new StringBuilder();
		int index = -1;
		for (Circuit circuit : circuitTableModel.circuits) {

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

			if (DEBUG())
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

	protected void doCircuits_Command2() {
	}

	private void doCircuits_Complete() {
		mainFrame.chart.allCircuits.clear();
		mainFrame.chart.allCircuits.addAll(circuitTableModel.circuits);
		mainFrame.chart.trunkCircuit = circuitTableModel.circuits.get(0);

		if (tempCircuit != null) {
			tempCircuit = null;
		}

		CircuitEditDialog.this.setVisible(false);
		System.gc();
	}

	private void doCircuits_Cancel() {
		if (tempCircuit != null) {
			circuitTableModel.circuits.remove(tempCircuit);
			tempCircuit = null;

			switchCircuit(circuitTableModel.circuits.get(0), 0);
		}

		CircuitEditDialog.this.setVisible(false);
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

	public static class CircuitTableModel extends DefaultJEditTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5316084320996309203L;
		public Vector<Circuit> circuits;

		CircuitTableModel(Vector<Circuit> existingCircuits) {
			if (existingCircuits == null) {
				circuits = new Vector<Circuit>(8);
			} else {
				circuits = new Vector<Circuit>(existingCircuits.size() + 8);
				for (Circuit circuit : existingCircuits) {
					circuits.add(circuit.copy());
				}
			}
		}

		/**
		 * getColumnCount
		 *
		 * @return int
		 */
		public int getColumnCount() {
			return 3;
		}

		/**
		 * getRowCount
		 *
		 * @return int
		 */
		public int getRowCount() {
			return circuits.size();
		}

		/**
		 * isCellEditable
		 *
		 * @param rowIndex
		 *            int
		 * @param columnIndex
		 *            int
		 * @return boolean
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex != 1);
		}

		/**
		 * getColumnClass
		 *
		 * @param columnIndex
		 *            int
		 * @return Class
		 */
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 1:
				return String.class;
			case 2:
				return Integer.class;
			case 0:
				return Boolean.class;
			default:
				return null;
			}

		}

		/**
		 * getValueAt
		 *
		 * @param rowIndex
		 *            int
		 * @param columnIndex
		 *            int
		 * @return Object
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 1:
				return circuits.get(rowIndex).name;
			case 2:
				return new Integer(circuits.get(rowIndex).zindex);
			case 0:
				return new Boolean(circuits.get(rowIndex).visible);
			default:
				return null;
			}
		}

		/**
		 * setValueAt
		 *
		 * @param aValue
		 *            Object
		 * @param rowIndex
		 *            int
		 * @param columnIndex
		 *            int
		 */
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 1:
				// circuit.getStation(rowIndex).name = (String) aValue;
				break;
			case 2:
				circuits.get(rowIndex).zindex = ((Number) aValue).intValue();
				break;
			case 0:
				circuits.get(rowIndex).visible = ((Boolean) aValue).booleanValue();
				break;
			default:
			}

			fireTableCellUpdated(rowIndex, columnIndex);
		}

		/**
		 * getColumnName
		 *
		 * @param columnIndex
		 *            int
		 * @return String
		 */
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 1:
				return __("Circuit");
			case 2:
				return __("zIndex");
			case 0:
				return __("Visible");
			default:
				return null;
			}
		}

		/**
		 * addTableModelListener
		 *
		 * @param l
		 *            TableModelListener
		 */
		public void addTableModelListener(TableModelListener l) {
		}

		/**
		 * removeTableModelListener
		 *
		 * @param l
		 *            TableModelListener
		 */
		public void removeTableModelListener(TableModelListener l) {
		}

		@Override
		public boolean nextCellIsBelow(int row, int column, int increment) {
			return true;
		}

		@Override
		public boolean columnIsTimeString(int column) {
			return false;
		}
	}

	public static class StationTableModel extends DefaultJEditTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6136704973824924463L;

		public Circuit circuit;

		StationTableModel(Circuit _circuit) {
			circuit = _circuit.copy();
		}

		/**
		 * getColumnCount
		 *
		 * @return int
		 */
		public int getColumnCount() {
			return 5;
		}

		/**
		 * getRowCount
		 *
		 * @return int
		 */
		public int getRowCount() {
			return circuit.getStationNum();
		}

		/**
		 * isCellEditable
		 *
		 * @param rowIndex
		 *            int
		 * @param columnIndex
		 *            int
		 * @return boolean
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex == 0) || (columnIndex == 1)
					|| (columnIndex == 3) || (columnIndex == 4);
		}

		/**
		 * getColumnClass
		 *
		 * @param columnIndex
		 *            int
		 * @return Class
		 */
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return String.class;
			case 1:
			case 2:
			case 3:
				return Integer.class;
			case 4:
				return Boolean.class;
			default:
				return null;
			}

		}

		/**
		 * getValueAt
		 *
		 * @param rowIndex
		 *            int
		 * @param columnIndex
		 *            int
		 * @return Object
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return circuit.getStation(rowIndex).name;
			case 1:
				return new Integer(circuit.getStation(rowIndex).dist);
			case 2:
				return new Integer(Math.round(circuit.dispScale
						* circuit.getStation(rowIndex).dist));
			case 3:
				return new Integer(circuit.getStation(rowIndex).level);
			case 4:
				return new Boolean(circuit.getStation(rowIndex).hide);
			default:
				return null;
			}
		}

		/**
		 * setValueAt
		 *
		 * @param aValue
		 *            Object
		 * @param rowIndex
		 *            int
		 * @param columnIndex
		 *            int
		 */
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				circuit.getStation(rowIndex).name = (String) aValue;
				break;
			case 1:
				int offset = ((Integer) aValue).intValue()
						- circuit.getStation(rowIndex).dist;
				circuit.getStation(rowIndex).dist = ((Integer) aValue)
						.intValue();

				if (rowIndex == circuit.getStationNum() - 1)
					circuit.length += offset;
				break;
			case 2:
				break;
			case 3:
				circuit.getStation(rowIndex).level = ((Integer) aValue)
						.intValue();
				break;
			case 4:
				circuit.getStation(rowIndex).hide = ((Boolean) aValue)
						.booleanValue();
				break;
			default:
			}

			fireTableCellUpdated(rowIndex, columnIndex);
		}

		/**
		 * getColumnName
		 *
		 * @param columnIndex
		 *            int
		 * @return String
		 */
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return __("Station");
			case 1:
				return __("Distance");
			case 2:
				return __("Display Dist.");
			case 3:
				return __("Level");
			case 4:
				return __("Hidden");
			default:
				return null;
			}
		}

		/**
		 * addTableModelListener
		 *
		 * @param l
		 *            TableModelListener
		 */
		public void addTableModelListener(TableModelListener l) {
		}

		/**
		 * removeTableModelListener
		 *
		 * @param l
		 *            TableModelListener
		 */
		public void removeTableModelListener(TableModelListener l) {
		}

		@Override
		public boolean nextCellIsBelow(int row, int column, int increment) {
			return true;
		}

		@Override
		public boolean columnIsTimeString(int column) {
			return false;
		}
	}

	public static class CircuitTable extends JEditTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3790451911367790284L;

		public CircuitTable() {
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//			setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		}

		public Dimension getPreferredScrollableViewportSize() {
			int r = this.getRowCount();
			int h = this.getRowHeight() * Math.min(r, 15);
//			int w = super.getPreferredScrollableViewportSize().width;
			int w = 200;
			return new Dimension(w, h);
		}
	}

	public static class StationTable extends JEditTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8627865729136595002L;

		public StationTable() {
			setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		}

		public Dimension getPreferredScrollableViewportSize() {
			int r = this.getRowCount();
			int h = this.getRowHeight() * Math.min(r, 15);
			int w = super.getPreferredScrollableViewportSize().width;
//			int w = 400;
			return new Dimension(w, h);
		}
	}
}
