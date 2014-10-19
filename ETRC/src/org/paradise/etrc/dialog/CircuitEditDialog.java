package org.paradise.etrc.dialog;

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
import java.util.Vector;

import javax.sound.midi.MidiDevice.Info;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import static org.paradise.etrc.ETRC._;
import static org.paradise.etrc.ETRCUtil.*;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.BOMStripperInputStream;
import org.paradise.etrc.data.Chart;
import org.paradise.etrc.data.Circuit;
import org.paradise.etrc.data.Station;
import org.paradise.etrc.filter.CIRFilter;
import org.paradise.etrc.filter.CRSFilter;
import org.paradise.etrc.filter.CSVFilter;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import sun.swing.table.DefaultTableCellHeaderRenderer;

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
	private JComboBox cbMultiplicity;
	private JTextField tfDispScale;
	private JCheckBox ckSuccesiveChange;
	
	private int circuitNum = 0;
	private Circuit tempCircuit;
	
	private ArrayList<Integer> scaledCircuitIndeces = new ArrayList<Integer>(8);
	private ArrayList<String> crossoverStations = new ArrayList<String> (16);

	public CircuitEditDialog(MainFrame _mainFrame, Vector<Circuit> existingCircuits) {
		super(_mainFrame, _("Railway Circuits: ") + _mainFrame.getRailNetworkName(), true);
		
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

		chooser.setDialogTitle(_("Load Circuit"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new CSVFilter());
		chooser.addChoosableFileFilter(new CIRFilter());
		chooser.setFont(new java.awt.Font(_("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(mainFrame.prop.getProperty(MainFrame.Prop_Recent_Open_File_Path, ""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}
		
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
//			System.out.println(f);

			Circuit c = new Circuit();
			try {
				c.loadFromFile(f.getAbsolutePath());
				mainFrame.prop.setProperty(MainFrame.Prop_Recent_Open_File_Path, chooser.getSelectedFile().getParentFile().getAbsolutePath());
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
			}
			return c;
		}
		else
			return null;
	}

	/**
	 * doLoadCircuits: Load circuits in .crs file and append them into circuit vector "circuits".
	 */
	private void doLoadCircuits(Vector<Circuit> circuits, boolean clearOriginalCircuits) {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(_("Load Circuits"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new CSVFilter());
		chooser.addChoosableFileFilter(new CRSFilter());
		chooser.setFont(new java.awt.Font(_("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(mainFrame.prop.getProperty(MainFrame.Prop_Recent_Open_File_Path, ""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}
		
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
//			System.out.println(f);

			BufferedReader in = null;
			Vector<Circuit> loadedCircuits = new Vector<Circuit>(8);  // in most cases, there are no more than 8 circuits.
			Circuit circuit = null;
			int lineNum = 0;
			try {
				in = new BufferedReader(new InputStreamReader(new BOMStripperInputStream(new FileInputStream(f)),"UTF-8"));
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
					throw new IOException(_("Loaded circuits are empty."));
				}
				
				if (clearOriginalCircuits)
					circuits.clear();
				circuits.addAll(loadedCircuits);
				loadedCircuits.clear();
				
				mainFrame.prop.setProperty(MainFrame.Prop_Recent_Open_File_Path, chooser.getSelectedFile().getParentFile().getAbsolutePath());
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
				InfoDialog.showErrorDialog(this, _("Cannot load circuits due to\r\n") + ex.getMessage());
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {}
			}
		}
	}

	/**
	 * doSaveCircuit
	 */
	private void doSaveCircuit(Circuit circuit) throws IOException {

		stationTableModel.circuit.name = tfName.getText();
		try {
			stationTableModel.circuit.multiplicity = Integer.parseInt(cbMultiplicity.getSelectedItem().toString());
		} catch (NumberFormatException e) {
			throw new IOException(_("Invalid rail count. It should be 1/2/4"));
		}
		try {
			stationTableModel.circuit.dispScale = Float.parseFloat(tfDispScale.getText());
		} catch (NumberFormatException e) {
			InfoDialog.showErrorDialog(this, _("Invalid display scale. It should be a decimal"));
		}
		doSaveCircuits(circuit, null);
	}
	
	/**
	 * doSaveCircuits
	 */
	private void doSaveCircuits(Circuit circuit, Vector<Circuit> circuits) throws IOException {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);
		String suffix;

		if (circuit != null) {
			chooser.setDialogTitle(_("Save Circuit"));
			chooser.addChoosableFileFilter(new CIRFilter());
			suffix = CIRFilter.suffix;
			chooser.setSelectedFile(new File(circuit.name));
		} else if (circuits != null && circuits.size() > 0) {
			chooser.setDialogTitle(_("Save Circuits"));
			chooser.addChoosableFileFilter(new CRSFilter());
			suffix = CRSFilter.suffix;
			chooser.setSelectedFile(new File(mainFrame.getRailNetworkName().replace(' ', '_')));
		} else {
			return;
		}
		chooser.setApproveButtonText(_("Save"));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFont(new java.awt.Font(_("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(mainFrame.prop.getProperty(MainFrame.Prop_Recent_Open_File_Path, ""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}

		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String f = chooser.getSelectedFile().getAbsolutePath();
			if(!f.endsWith(suffix))
				f += suffix;

			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
				if (circuit != null)
					circuit.writeTo(out);
				else if (circuits != null)
					for (Circuit cir : circuits) {
						out.write(Chart.circuitPattern);
						out.newLine();
						cir.writeTo(out);
					}
				out.close();
				mainFrame.prop.setProperty(MainFrame.Prop_Recent_Open_File_Path, chooser.getSelectedFile().getParentFile().getAbsolutePath());
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
				throw ex;
			}
		}
	}
	
	private void jbInit() throws Exception {
//		JScrollPane spCircuit = new JScrollPane(table);

//		JPanel circuitPanel = new JPanel();
		//trainPanel.add(underColorPanel,  BorderLayout.SOUTH);
		JButton btAddCircuit = new JButton(_("Add Circuit"));
		btAddCircuit.setFont(new Font("dialog", 0, 12));
		btAddCircuit.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircutis_AddCircuit();
			}
		});

		JButton btImportCircuit = new JButton(_("Import Circuit"));
		btImportCircuit.setFont(new Font("dialog", 0, 12));
		btImportCircuit.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_ImportCircuit();
			}
		});

		JButton btMoveUp = new JButton(_("Move Up"));
		btMoveUp.setFont(new Font("dialog", 0, 12));
		btMoveUp.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_MoveUpCircuit();
			}
		});

		JButton btMoveDown = new JButton(_("Move Down"));
		btMoveDown.setFont(new Font("dialog", 0, 12));
		btMoveDown.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_MoveDownCircuit();
			}
		});

		JButton btRemoveCircuit = new JButton(_("Remove Circuit"));
		btRemoveCircuit.setFont(new Font("dialog", 0, 12));
		btRemoveCircuit.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_RemoveCircuit();
			}
		});

		JButton btNewCircuits = new JButton(_("New Circuits"));
		btNewCircuits.setFont(new Font("dialog", 0, 12));
		btNewCircuits.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_NewCircuit();
			}
		});

		JButton btSaveCircuits = new JButton(_("Save Circuits"));
		btSaveCircuits.setFont(new Font("dialog", 0, 12));
		btSaveCircuits.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_SaveCircuits();
			}
		});

		JButton btLoadCircuits = new JButton(_("Load Circuits"));
		btLoadCircuits.setFont(new Font("dialog", 0, 12));
		btLoadCircuits.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_LoadCircuits();
			}
		});

		JButton btComplete = new JButton(_("Complete"));
		btComplete.setFont(new Font("dialog", 0, 12));
		btComplete.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCircuits_Complete();
			}
		});

		JButton btCancel = new JButton(_("Cancel"));
		btCancel.setFont(new Font("dialog", 0, 12));
		btCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuits_Cancel();
			}
		});

		JButton btAdjust = new JButton(_("Adjust"));
		btAdjust.setFont(new Font("dialog", 0, 12));
		btAdjust.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuits_Command();
			}
		});

		JButton btCommand2 = new JButton(_("Command2"));
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
		circuitTable.getColumnModel().getColumn(0).setPreferredWidth(120);
		circuitTable.getColumnModel().getColumn(1).setPreferredWidth(60);
		
		circuitTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// Selected circirt changed in circirt table
				if (arg0.getValueIsAdjusting()) {
					doChangeCircuit();
				}
			}
		});
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int column) {

				if (isScaledCircuit(row)) {
					setBackground(Color.YELLOW);
				}  else {
					setBackground(table.getBackground());
				}
				
				// DEBUG(String.format("[% 2d,% 2d] %s", row, column, value));
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
			
		};
		
		for (int i = 0; i < circuitTable.getColumnCount(); i++) {
			circuitTable.getColumn(circuitTable.getColumnName(i)).setCellRenderer(renderer);
		}
	}
	
	private JPanel buildCircuitPanel() {
	    ckSuccesiveChange = new JCheckBox(_("Succesive Change"), false);
		
		buildStationTable();

		JButton btOK = new JButton(_("OK"));
		btOK.setFont(new Font("dialog", 0, 12));
		btOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// OK button pressed
				doCircuit_OK();
			}
		});

		JButton btLoad = new JButton(_("Load"));
		btLoad.setFont(new Font("dialog", 0, 12));
		btLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_Load();
			}
		});

		JButton btSave = new JButton(_("Save"));
		btSave.setFont(new Font("dialog", 0, 12));
		btSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_Save();
			}
		});
		
		JButton btDel = new JButton(_("Delete"));
		btDel.setFont(new Font("dialog", 0, 12));
		btDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_DeleteStation();
			}
		});
		
		JButton btRevert = new JButton(_("Revert"));
		btRevert.setFont(new Font("dialog", 0, 12));
		btRevert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_RevertStation();
			}
		});

		JButton btInsert = new JButton(_("Insert"));
		btInsert.setFont(new Font("dialog", 0, 12));
		btInsert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_InsertStation();
			}
		});

		JButton btAdd = new JButton(_("Add"));
		btAdd.setFont(new Font("dialog", 0, 12));
		btAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCircuit_AddStation();
			}
		});

		JLabel lbName = new JLabel(_("Circuit Name:"));
		lbName.setFont(new Font("dialog", 0, 12));
		
		tfName = new JTextField(12);
		tfName.setFont(new Font("dialog", 0, 12));
		tfName.setText(stationTableModel.circuit.name);
		
		JLabel lbLengthLabel = new JLabel(_("Total Length:"));
		lbLength = new JLabel("0 "+ _("km"));

		JLabel lbMultiplicity = new JLabel(_("Rail Count:"));
		lbName.setFont(new Font("dialog", 0, 12));
		
	    cbMultiplicity = new JComboBox();
	    cbMultiplicity.setFont(new Font("dialog", 0, 12));
	    cbMultiplicity.setEditable(true);
	    cbMultiplicity.addItem(_("1"));
	    cbMultiplicity.addItem(_("2"));
	    cbMultiplicity.addItem(_("4"));
	    cbMultiplicity.setSelectedItem(stationTableModel.circuit.multiplicity);
	    
	    JLabel lbDispScale = new JLabel(_("Display Scale:"));
		lbName.setFont(new Font("dialog", 0, 12));
		
		tfDispScale = new JTextField(2);
		tfDispScale.setFont(new Font("dialog", 0, 12));
		tfDispScale.setText("" + stationTableModel.circuit.dispScale);
		tfDispScale.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				stationTableModel.circuit.dispScale = Float.parseFloat(tfDispScale.getText());
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
	    namePanel.setBorder(new EmptyBorder(1,1,1,1));

	    JPanel tipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    JLabel lbTip = new JLabel(_("                 Note: Press OK button after editing."));
	    tipPanel.add(lbLengthLabel);
	    tipPanel.add(lbLength);
	    tipPanel.add(ckSuccesiveChange);
	    tipPanel.add(lbTip);
	    

	    JPanel buttonPanel1 = new JPanel(new GridLayout(9, 1, 0, 0));
	    buttonPanel1.add(new JPanel().add(new JLabel(" ")));
	    buttonPanel1.add(btInsert);
	    buttonPanel1.add(btAdd);
	    buttonPanel1.add(btDel);
	    buttonPanel1.add(btRevert);
	    buttonPanel1.add(btLoad);
		buttonPanel1.add(btSave);
		buttonPanel1.add(btOK);
	    buttonPanel1.add(new JPanel().add(new JLabel(" ")));
		
		JScrollPane spCircuit = new JScrollPane(stationTable);
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(spCircuit, BorderLayout.CENTER);
		contentPanel.add(tipPanel, BorderLayout.SOUTH);
		contentPanel.add(namePanel, BorderLayout.NORTH);

		JPanel circuitPanel = new JPanel();
		circuitPanel.setLayout(new BorderLayout());
		circuitPanel.add(buttonPanel1, BorderLayout.EAST);
		circuitPanel.add(contentPanel, BorderLayout.CENTER);
//		circuitPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		return circuitPanel;
	}

	private void buildStationTable() {
		stationTable = new StationTable();
		stationTable.setFont(new Font("Dialog", 0, 12));
		stationTable.getTableHeader().setFont(new Font("Dialog", 0, 12));
		
		stationTableModel = new StationTableModel(circuitsInChart.get(0).copy(), ckSuccesiveChange);
		stationTable.setModel(stationTableModel);
		stationTable.getColumnModel().getColumn(3).setPreferredWidth(40);
		stationTable.getColumnModel().getColumn(4).setPreferredWidth(40);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				
				if (column == 0 && isCrossOverStation((String) value)) {
					setBackground(Color.YELLOW);
				}  else {
					setBackground(table.getBackground());
				}
				
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
			
		};
		
		for (int i = 0; i < stationTable.getColumnCount(); i++) {
			stationTable.getColumn(stationTable.getColumnName(i)).setCellRenderer(renderer);
		}
	}
	
	protected void switchCircuit(Circuit circuit) {
		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor() .stopCellEditing();
		
		tfName.setText(circuit.name);
		cbMultiplicity.setSelectedItem(circuit.multiplicity);
		tfDispScale.setText("" + circuit.dispScale);
		stationTableModel.circuit = circuit;
		
		stationTable.revalidate();
		stationTable.updateUI();
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
		
		circuitTableModel.circuits.clear();
		circuitTableModel.circuits.addAll(circuitsInChart);
		
		if (circuit != null) {
			this.tempCircuit = circuit;
			circuit.zindex = 1;
			circuitTableModel.circuits.add(circuit);
			circuitTable.updateUI();
			
			switchCircuit(circuit);
		}
		
		this.setVisible(true);
	}
	
	private void doChangeCircuit() {
		int circuitIndex = circuitTable.getSelectedRow();
		Circuit circuit = circuitTableModel.circuits.get(circuitIndex).copy();
		switchCircuit(circuit);
	}

	private void doCircuit_OK() {
		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor().stopCellEditing();
		
		// Normalize
		Circuit c = ((StationTableModel)stationTable.getModel()).circuit;
		if (c.getStationNum() < 2)
		{
			new MessageBox(mainFrame, _("A circut must have at least two stations.")).showMessage();
			return;
		}
		int offset = c.getStation(0).dist;
		if (offset != 0) {
			if (new YesNoBox(mainFrame, _("The distance of the first station is not zero, do normalization?")).askForYes()) {
				for (int i = 0; i < c.getStationNum(); ++i) {
					c.getStation(i).dist -= offset; 
				}	
			}
		}
		c.length = c.getStation(c.getStationNum() - 1).dist;
		c.name = tfName.getText();
		try {
			c.multiplicity = Integer.parseInt(cbMultiplicity.getSelectedItem().toString());
			c.dispScale = Float.parseFloat(tfDispScale.getText());
		} catch (NumberFormatException e) {
			InfoDialog.showErrorDialog(this, _("Rail count should be an integer and display scale should be a decimal"));
		}
		
		int selectedCircuitIndex = circuitTable.getSelectedRow();
		if (selectedCircuitIndex >= 0)
			circuitTableModel.circuits.set(selectedCircuitIndex, c);
		
		circuitTable.updateUI();
	}

	private void doCircuit_Load() {
		Circuit cir = doLoadCircuit();
		if(cir != null) {
			switchCircuit(cir);
			mainFrame.isNewCircuit = true;
		}
	}

	private void doCircuit_Save() {
		try {
			doSaveCircuit(stationTableModel.circuit);
		} catch (IOException e1) {
			e1.printStackTrace();
			InfoDialog.showErrorDialog(this, _("Cannot save the circuit due to: \r\n") + e1.getMessage());
		}
	}

	private void doCircuit_DeleteStation() {
		if (stationTable.getCellEditor() != null)
			stationTable.getCellEditor().stopCellEditing();

		int[] selectedRows = stationTable.getSelectedRows();
		for (int i = selectedRows.length - 1; i>=0; --i) {
			((StationTableModel)stationTable.getModel()).circuit.delStation(selectedRows[i]);
		}
		//System.out.println(((StationTableModel)table.getModel()).circuit);

		stationTable.revalidate();
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

	private void doCircuit_InsertStation() {
		//        table.getCellEditor().stopCellEditing();
		Circuit cir = stationTableModel.circuit;
		int selectedIndex = stationTable.getSelectedRow();
		if(selectedIndex < 0) {
			InfoDialog.showInfoDialog(this, _("Please choose a station first."));
			return;
		}
		
		String name = _("Station");
		int dist = cir.getStation(selectedIndex).dist;
		int level = cir.getStation(selectedIndex).level;
		boolean hide = false;
		cir.insertStation(new Station(name, dist, level, hide), selectedIndex);
		//System.out.println(cir);

		stationTable.revalidate();
	}

	private void doCircuit_AddStation() {
		//        table.getCellEditor().stopCellEditing();
		Circuit cir = stationTableModel.circuit;
		int selectedIndex = stationTable.getSelectedRow();
		if(selectedIndex < 0) {
			InfoDialog.showInfoDialog(this, _("Please choose a station first."));
			return;
		}
		
		String name = _("Station");
		int dist = cir.getStation(selectedIndex).dist;
		int level = cir.getStation(selectedIndex).level;
		boolean hide = false;
		cir.insertStation(new Station(name, dist, level, hide), selectedIndex+1);
		//System.out.println(cir);

		stationTable.revalidate();
	}

	private void doCircutis_AddCircuit() {
		// Add a circuit
		Circuit newCircuit = new Circuit("New Circuit " + circuitNum++);
		circuitTableModel.circuits.add(newCircuit.copy());
		
		int circuitIndex = circuitTableModel.circuits.size() - 1;
		circuitTable.setRowSelectionInterval(circuitIndex, circuitIndex);
		circuitTable.updateUI();
		
		switchCircuit(newCircuit);
	}

	private void doCircuits_ImportCircuit() {
		// Import a circuit	
		//TODO: 8. Check station name duplication on clicking OK button
		//TODO: 9. Set circuit crossover
		String xianlu = new XianluSelectDialog(mainFrame).getXianlu();
		if(xianlu == null)
			return;
		
		Circuit circuit = new CircuitMakeDialog(mainFrame, xianlu).getCircuit();
		if(circuit == null)
			return;
		
		circuit.zindex = 1;
		circuitTableModel.circuits.add(circuit);
		int index0 = circuitTableModel.circuits.size() - 1;
		circuitTable.setRowSelectionInterval(index0, index0);
		circuitTable.updateUI();

		switchCircuit(circuit);
	}

	private void doCircuits_MoveUpCircuit() {
		// Move up a circuit
		int selectedCircuitIndex = circuitTable.getSelectedRow();
		if (selectedCircuitIndex == 0) {
			InfoDialog.showInfoDialog(this, _("This is already the first circuit and thus cannot be moved up any more."));
			return;
		}
		Circuit circuitMoved = circuitTableModel.circuits.remove(selectedCircuitIndex);
		circuitTableModel.circuits.insertElementAt(circuitMoved, selectedCircuitIndex - 1);
		
		circuitTable.setRowSelectionInterval(selectedCircuitIndex - 1, selectedCircuitIndex - 1); 
		circuitTable.updateUI();
	}

	private void doCircuits_MoveDownCircuit() {
		// Move down a circuit
		int selectedCircuitIndex = circuitTable.getSelectedRow();
		if (selectedCircuitIndex == circuitTableModel.circuits.size() - 1) {
			InfoDialog.showInfoDialog(this, _("This is already the last circuit and thus cannot be moved down any more."));
			return;
		}
		Circuit circuitMoved = circuitTableModel.circuits.remove(selectedCircuitIndex);
		circuitTableModel.circuits.insertElementAt(circuitMoved, selectedCircuitIndex + 1);
		
		circuitTable.setRowSelectionInterval(selectedCircuitIndex + 1, selectedCircuitIndex + 1); 
		circuitTable.updateUI();
	}

	private void doCircuits_RemoveCircuit() {
		// Remove a circuit
		int selectedCircuitIndex = circuitTable.getSelectedRow();
		if (selectedCircuitIndex == 0 && circuitTableModel.circuits.size() == 1) {
			InfoDialog.showErrorDialog(this, _("Cannot remove the last circuit."));
			return;
		}
		circuitTableModel.circuits.remove(selectedCircuitIndex);
		if (selectedCircuitIndex >= circuitTableModel.circuits.size())
			--selectedCircuitIndex;
		circuitTable.setRowSelectionInterval(selectedCircuitIndex, selectedCircuitIndex);
		circuitTable.updateUI();
		
		switchCircuit(circuitTableModel.circuits.get(selectedCircuitIndex));
	}

	private void doCircuits_NewCircuit() {
		// Create new circuits with only one circuit
		Circuit newCircuit = new Circuit("New Circuit " + circuitNum++);
		circuitTableModel.circuits.clear();
		circuitTableModel.circuits.add(newCircuit);
		circuitTable.updateUI();
		
		switchCircuit(newCircuit);
	}

	private void doCircuits_SaveCircuits() {
		// Save all circuits
		if (circuitTableModel.circuits.size() == 0) {
			InfoDialog.showErrorDialog(this, _("Cannot save empty circuits."));
			return;
		}
		try {
			doSaveCircuits(null, circuitTableModel.circuits);
		} catch (IOException e) {
			e.printStackTrace();
			InfoDialog.showErrorDialog(mainFrame, _("Cannot save the circuit due to: \r\n") + e.getMessage());
		}
	}

	private void doCircuits_LoadCircuits() {
		// Load all circuits
		try {
			doLoadCircuits(circuitTableModel.circuits, true);
			circuitTable.updateUI();
			
			switchCircuit(circuitTableModel.circuits.get(0));
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			InfoDialog.showErrorDialog(this, _("Cannot load circuits due to\r\n") + e.getMessage());
		}
	}

	protected void doCircuits_Command() {
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
			
			switchCircuit(circuitTableModel.circuits.get(0));
		}
		
		CircuitEditDialog.this.setVisible(false);
		System.gc();
	}
	
	private boolean isScaledCircuit(int circuitIndex) {
		return false;
	}
	
	private boolean isCrossOverStation(String stationName) {
		return false;
	}

	public static class CircuitTableModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5316084320996309203L;
		public Vector<Circuit> circuits;
		
		CircuitTableModel(Vector<Circuit> existingCircuits) {
			if (existingCircuits == null) {
				circuits = new Vector<Circuit> (8);
			} else {
				circuits = new Vector<Circuit>(existingCircuits.size() + 8);
				for(Circuit circuit : existingCircuits) {
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
			return 2;
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
		 * @param rowIndex int
		 * @param columnIndex int
		 * @return boolean
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex == 1);
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
				return String.class;
			case 1:
				return Integer.class;
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
				return circuits.get(rowIndex).name;
			case 1:
				return new Integer(circuits.get(rowIndex).zindex);
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
			switch (columnIndex) {
			case 0:
				// circuit.getStation(rowIndex).name = (String) aValue;
				break;
			case 1:
				circuits.get(rowIndex).zindex = ((Number) aValue).intValue();
				break;
			default:
			}
			
			fireTableCellUpdated(rowIndex, columnIndex);
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
				return _("Circuit");
			case 1:
				return _("zIndex");
			default:
				return null;
			}
		}

		/**
		 * addTableModelListener
		 *
		 * @param l TableModelListener
		 */
		public void addTableModelListener(TableModelListener l) {
		}

		/**
		 * removeTableModelListener
		 *
		 * @param l TableModelListener
		 */
		public void removeTableModelListener(TableModelListener l) {
		}
	}

	public static class StationTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6136704973824924463L;

		public Circuit circuit; 
		private JCheckBox ckSuccesiveChange;
		
		StationTableModel(Circuit _circuit, JCheckBox _ckSuccesiveChange) {
			circuit = _circuit.copy();
			ckSuccesiveChange = _ckSuccesiveChange;
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
		 * @param rowIndex int
		 * @param columnIndex int
		 * @return boolean
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex == 0) || (columnIndex == 1)
					|| (columnIndex == 3)|| (columnIndex == 4);
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
		 * @param rowIndex int
		 * @param columnIndex int
		 * @return Object
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return circuit.getStation(rowIndex).name;
			case 1:
				return new Integer(circuit.getStation(rowIndex).dist);
			case 2:
				return new Integer(Math.round(circuit.dispScale * circuit.getStation(rowIndex).dist));
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
		 * @param aValue Object
		 * @param rowIndex int
		 * @param columnIndex int
		 */
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				circuit.getStation(rowIndex).name = (String) aValue;
				break;
			case 1:
				int offset = ((Integer) aValue).intValue() - circuit.getStation(rowIndex).dist;
				circuit.getStation(rowIndex).dist = ((Integer) aValue).intValue();
				
				boolean succesiveChange = ckSuccesiveChange.isSelected();
				if (succesiveChange) {
					for (++rowIndex; rowIndex < circuit.getStationNum(); ++ rowIndex) {
						circuit.getStation(rowIndex).dist += offset;
					}
				}
				
				if (rowIndex == circuit.getStationNum() - 1 || succesiveChange)
					circuit.length += offset;
				break;
			case 2:
				break;
			case 3:
				circuit.getStation(rowIndex).level = ((Integer) aValue).intValue();
				break;
			case 4:
				circuit.getStation(rowIndex).hide = ((Boolean) aValue).booleanValue();
				break;
			default:
			}
			
			fireTableCellUpdated(rowIndex, columnIndex);
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
				return _("Station");
			case 1:
				return _("Distance");
			case 2:
				return _("Display Dist.");
			case 3:
				return _("Level");
			case 4:
				return _("Hidden");
			default:
				return null;
			}
		}

		/**
		 * addTableModelListener
		 *
		 * @param l TableModelListener
		 */
		public void addTableModelListener(TableModelListener l) {
		}

		/**
		 * removeTableModelListener
		 *
		 * @param l TableModelListener
		 */
		public void removeTableModelListener(TableModelListener l) {
		}
	}

	public static class CircuitTable extends JTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3790451911367790284L;

		public CircuitTable() {
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}

		public Dimension getPreferredScrollableViewportSize() {
			int r = this.getRowCount();
			int h = this.getRowHeight() * Math.min(r, 15);
			int w = 150;
			return new Dimension(w, h);
		}
	}

	public static class StationTable extends JTable {
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
			return new Dimension(w, h);
		}

		public boolean isRowSelected(int row) {
			//      return chart.trains[row].equals(chart.getActiveTrain());
			return super.isRowSelected(row);
		}
	}
}
