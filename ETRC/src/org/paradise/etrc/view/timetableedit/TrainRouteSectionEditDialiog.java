package org.paradise.etrc.view.timetableedit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Comparator;
import java.util.Vector;
import java.util.function.Function;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.v1.RailNetworkChart;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainRouteSection;
import org.paradise.etrc.data.v1.TrainType;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.util.ui.databinding.UIBindingManager;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.DEBUG_MSG;

import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JSeparator;

public class TrainRouteSectionEditDialiog extends JDialog {

	// Use static fields to keep this object because 
	// eclipse window builder only support static factory methods
	// to create ui components.
	private static Vector<Component> allDataBindingComponent = new Vector<>();
	private UIBindingManager uiBindingManager = UIBindingManager.getInstance(this);
	
	private MainFrame mainFrame;
	private boolean ui_inited;
	
	TrainGraph trainGraph;
	Train train, train2;
	TrainRouteSection section;
	boolean downGoing;
	int columnIndex;
	String trainNameInEditor;
	String origTrainName;
	String oldTrainName;

	private final JPanel contentPanel = new JPanel();
	private JTextArea txtrVehicleName;
	private final JTextArea txtrRemarks = new JTextArea();
	private JComboBox<String> cbTrainName;
	private JLabel lblTrainType;
	private JLabel lblTrainNameTip;
	private JTextField txtFulltrainname;
	private JComboBox<RailroadLineChart> cbTrain2Railline;
	private JComboBox<Train> cbTrain2FullTrainName;
	private JTextArea txtrTrain2VehicleName;
	

	/**
	 * Create the dialog.
	 */
	public TrainRouteSectionEditDialiog(TrainGraph trainGraph, TrainRouteSection section, int columnIndex) {

		mainFrame = MainFrame.getInstance();
		this.trainGraph = trainGraph;
		this.section = section;
		this.downGoing = section.downGoing;
		this.columnIndex = columnIndex;
		this.origTrainName = section.getName();
		this.trainNameInEditor = this.origTrainName;
		
		allDataBindingComponent.clear();
		prepareDataForUI();
		initUI();
		
		DB_setupUIDataBinding();
		
		ui_inited = true;
	}
	
	// {{ Init UI
	private String[] allTrainNames;
	private RailroadLineChart[] allRaillineCharts;
	private Vector<Train> allTrainsInRailline = new Vector<Train> ();
	private JLabel lblTrain2From;
	private JLabel lblTrain2To;
	private void prepareDataForUI() {
		allTrainNames = trainGraph == null ? new String[0] : trainGraph.currentNetworkChart.trains.stream()
				.map(train -> train.getTrainName(downGoing)).toArray(String[]::new);
		
		allRaillineCharts = trainGraph == null ? new RailroadLineChart[0] : trainGraph.currentNetworkChart
				.allRailLineCharts().toArray(new RailroadLineChart[0]);
	}
	
	private void initUI() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				onLoadUp();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				do_Cancel();
			}
		});
		
		setModal(false);
		setBounds(100, 100, 750, 334);
		contentPanel.setBounds(0, 0, 420, 208);
		contentPanel.setPreferredSize(new Dimension(420, 300));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
//		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);
		{
			JLabel lblTrainname = createJLabel("Train Name", new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainname.setBounds(6, 44, 72, 16);
			contentPanel.add(lblTrainname);
		}
		{
			JLabel lblTrainTypeLabel = TrainRouteSectionEditDialiog.createJLabel("Train Type", new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainTypeLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainTypeLabel.setBounds(6, 72, 66, 16);
			contentPanel.add(lblTrainTypeLabel);
		}
		{
			JLabel lblVehicleName = TrainRouteSectionEditDialiog.createJLabel("Vehicle Name", new Font("Lucida Grande", Font.PLAIN, 12));
			lblVehicleName.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblVehicleName.setBounds(6, 100, 85, 16);
			contentPanel.add(lblVehicleName);
		}
		{
			JLabel lblRemarks = TrainRouteSectionEditDialiog.createJLabel("Remarks", new Font("Lucida Grande", Font.PLAIN, 12));
			lblRemarks.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblRemarks.setBounds(6, 156, 61, 16);
			contentPanel.add(lblRemarks);
		}
		cbTrainName = TrainRouteSectionEditDialiog.createJComboBox(
				new DefaultComboBoxModel<String>(allTrainNames), 
				"section.name", new Font("Lucida Grande", Font.PLAIN, 12));
		cbTrainName.setToolTipText(__("Use odd numbers for down-going trains and even numbers for up-going trains."));
		((JTextField) cbTrainName.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				fireChange(e);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				fireChange(e);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				fireChange(e);
			}
			
			public void fireChange(DocumentEvent e) {
				if (!ui_inited)
					return;
				
				int length = e.getDocument().getLength();
				trainNameInEditor = "";
				try {
					trainNameInEditor = e.getDocument().getText(0, length);
				} catch (BadLocationException e1) {
					trainNameInEditor = (String) cbTrainName.getEditor().getItem();
				}
//				DEBUG_MSG("document %s", trainNameInEditor);
				
				do_UpdateTrainType();
				do_updateTrainNameTip();
			}
		});
		cbTrainName.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED)
					do_UpdateByTrainNameComboBox(true);
				else if (e.getStateChange() == ItemEvent.SELECTED)
					do_UpdateByTrainNameComboBox(false);
			}
		});
		cbTrainName.setEditable(true);
		cbTrainName.setBounds(110, 40, 113, 27);
		contentPanel.add(cbTrainName);
		
		txtrVehicleName = new JTextArea();
		txtrVehicleName.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		allDataBindingComponent.add(txtrVehicleName);
		txtrVehicleName.setName("section.vehicleName");
		txtrVehicleName.setBounds(114, 100, 294, 49);
		contentPanel.add(txtrVehicleName);
		txtrVehicleName.setColumns(10);
		txtrRemarks.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtrRemarks.setName("section.remarks");
		txtrRemarks.setLineWrap(true);
		txtrRemarks.setBounds(114, 156, 294, 88);
		allDataBindingComponent.add(txtrRemarks);
		contentPanel.add(txtrRemarks);
		{
			lblTrainType = new JLabel("trainType");
			lblTrainType.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainType.setBounds(114, 72, 294, 16);
			contentPanel.add(lblTrainType);
		}
		
		lblTrainNameTip = new JLabel("This is a new train");
		lblTrainNameTip.setOpaque(true);
		lblTrainNameTip.setBackground(SystemColor.window);
		lblTrainNameTip.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblTrainNameTip.setHorizontalAlignment(SwingConstants.CENTER);
		lblTrainNameTip.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblTrainNameTip.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		lblTrainNameTip.setBounds(235, 44, 173, 20);
		contentPanel.add(lblTrainNameTip);
		getContentPane().setLayout(new BorderLayout(0, 0));
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						do_OK();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						do_Cancel();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		{
			JSplitPane splitPane = new JSplitPane();
			splitPane.setEnabled(false);
			splitPane.setDividerSize(2);
			getContentPane().add(splitPane);
			splitPane.setDividerLocation(420);
			
			splitPane.setLeftComponent(contentPanel);
			
			JPanel LinkTrainPanel = new JPanel();
			splitPane.setRightComponent(LinkTrainPanel);
			LinkTrainPanel.setLayout(null);
			{
				cbTrain2Railline = new JComboBox<>();
				cbTrain2Railline.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							doTrain2_updateFullTrainNameList();
						}
					}
				});
				cbTrain2Railline.setModel(new DefaultComboBoxModel<RailroadLineChart>(allRaillineCharts));
				cbTrain2Railline.setSelectedIndex(-1);
				cbTrain2Railline.setBounds(109, 40, 211, 27);
				LinkTrainPanel.add(cbTrain2Railline);
			}
			{
				JLabel lblRailLine = TrainRouteSectionEditDialiog.createJLabel("Rail Line", new Font("Lucida Grande", Font.PLAIN, 12));
				lblRailLine.setBounds(13, 44, 61, 16);
				LinkTrainPanel.add(lblRailLine);
			}
			{
				JLabel lblTrain2FullName = TrainRouteSectionEditDialiog.createJLabel("Full Train Name", new Font("Lucida Grande", Font.PLAIN, 12));
				lblTrain2FullName.setBounds(13, 77, 98, 15);
				LinkTrainPanel.add(lblTrain2FullName);
			}
			{
				cbTrain2FullTrainName = new JComboBox<>();
				cbTrain2FullTrainName.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							doTrain2_updateTrain2Info();
						}
					}
				});
				cbTrain2FullTrainName.setModel(new DefaultComboBoxModel<>(allTrainsInRailline));
				cbTrain2FullTrainName.setBounds(109, 72, 211, 27);
				LinkTrainPanel.add(cbTrain2FullTrainName);
			}
			{
				JLabel lblTo = TrainRouteSectionEditDialiog.createJLabel("➡︎", new Font("Lucida Grande", Font.PLAIN, 20));
				lblTo.setBounds(153, 101, 17, 25);
				LinkTrainPanel.add(lblTo);
			}
			{
				JLabel lblTrain2 = new JLabel("Train 2");
				lblTrain2.setBounds(13, 12, 61, 16);
				LinkTrainPanel.add(lblTrain2);
			}
			
			JSeparator separator = new JSeparator();
			separator.setForeground(Color.LIGHT_GRAY);
			separator.setOrientation(SwingConstants.VERTICAL);
			separator.setBounds(0, 0, 8, 269);
			LinkTrainPanel.add(separator);
			
			JButton btnAppendTrain2 = new JButton("NewTrain = Train 1 + Train 2");
			btnAppendTrain2.setBounds(6, 214, 314, 29);
			LinkTrainPanel.add(btnAppendTrain2);
			
			JButton btnInsertTrain2 = new JButton("NewTrain = Train 2 + Train 1");
			btnInsertTrain2.setBounds(6, 240, 314, 29);
			LinkTrainPanel.add(btnInsertTrain2);
			
			JLabel label = createJLabel("Vehicle Name", new Font("Lucida Grande", Font.PLAIN, 12));
			label.setBounds(13, 134, 85, 16);
			LinkTrainPanel.add(label);
			
			txtrTrain2VehicleName = new JTextArea();
			txtrTrain2VehicleName.setEditable(false);
			txtrTrain2VehicleName.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			txtrTrain2VehicleName.setBounds(13, 156, 296, 52);
			LinkTrainPanel.add(txtrTrain2VehicleName);
			
			lblTrain2From = new JLabel("From");
			lblTrain2From.setHorizontalAlignment(SwingConstants.RIGHT);
			lblTrain2From.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrain2From.setBounds(13, 105, 136, 16);
			LinkTrainPanel.add(lblTrain2From);
			
			lblTrain2To = new JLabel("To");
			lblTrain2To.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrain2To.setBounds(173, 105, 136, 16);
			LinkTrainPanel.add(lblTrain2To);
		}
		
		txtFulltrainname = new JTextField();
		txtFulltrainname.setText("fullTrainName");
		txtFulltrainname.setBounds(110, 6, 186, 28);
		contentPanel.add(txtFulltrainname);
		txtFulltrainname.setColumns(10);
		
		JLabel lblFullTrainName = createJLabel("Full Train Name", new Font("Lucida Grande", Font.PLAIN, 12));
		lblFullTrainName.setBounds(6, 12, 92, 16);
		contentPanel.add(lblFullTrainName);
		
		JToggleButton tglbtnLinkTrain = new JToggleButton("Merge Train");
		tglbtnLinkTrain.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				do_toggleLinkTrainPanel(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		tglbtnLinkTrain.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		tglbtnLinkTrain.setBounds(298, 7, 114, 29);
		contentPanel.add(tglbtnLinkTrain);
		
		setTitle(__("Edit Train"));
	}

	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source text "Train Name"
	 * @wbp.factory.parameter.source font new java.awt.Font("Lucida Grande", java.awt.Font.PLAIN, 12)
	 */
	public static JLabel createJLabel(String text, Font font) {
		JLabel label = new JLabel(__(text));
		label.setFont(font);
		return label;
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source model new javax.swing.DefaultComboBoxModel(new java.lang.String[] {})
	 * @wbp.factory.parameter.source name "train.trainType"
	 * @wbp.factory.parameter.source font new java.awt.Font("Lucida Grande", java.awt.Font.PLAIN, 12)
	 */
	public static <E> JComboBox<E> createJComboBox(ComboBoxModel<E> model, String name, Font font) {
		JComboBox<E> comboBox = new JComboBox<>();
		comboBox.setModel(model);
		comboBox.setName(name);
		comboBox.setFont(font);
		
		return comboBox;
	}
	
	// }}
	
	// {{ Event handlers for UI

	public void showDialog() {
		Rectangle dlgSize = getBounds();
		Dimension frmSize;
		Point loc;
		frmSize = Toolkit.getDefaultToolkit().getScreenSize();
		loc = new Point(0,0);
		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		
		do_toggleLinkTrainPanel(false);
		
		super.setVisible(true);
	}
	
	private void onLoadUp() {
		trainNameInEditor = origTrainName;
		do_updateTrainNameTip();
		do_updateTrainNameTip();
	}
	
	private void do_toggleLinkTrainPanel(boolean panelExpanded) {
		int width = panelExpanded ? contentPanel.getPreferredSize().width + 325
				: contentPanel.getPreferredSize().width + 5;
		int height = getBounds().height;
		Point position = getLocation();
		setBounds(position.x, position.y, width, height);
		
		revalidate();
	}
	
	/**
	 * This method is invoked by the first item listener of cbTrainName added in initUI().
	 * @param oldValue
	 */
	private void do_UpdateByTrainNameComboBox(boolean oldValue) {
		if (oldValue) {
			oldTrainName = (String) cbTrainName.getSelectedItem();
		} else {
			do_UpdateTrainType();
		}
	}
	
	private void do_UpdateTrainType() {
		String trainName = trainNameInEditor; // ((JTextField) cbTrainName.getEditor().getEditorComponent()).getText();
		
//		DEBUG_MSG("Train Name:" + trainName);
		TrainType type = trainGraph.guessTrainTypeByName(trainName);
		lblTrainType.setText(type.getName());
		lblTrainType.setForeground(type.getLineColor());
		lblTrainType.repaint();
	}
	
	private void do_updateTrainNameTip() {
		DEBUG_MSG("Train Name:" + trainNameInEditor);
		validateTrainName(trainNameInEditor);
	}
	
	/**
	 * This method is a validator method of UI data binding for cbTrainName.
	 * It will be invoked by item listener of cbTrainName created by its data binding, 
	 * and thus will be invoked after do_UpdateByTrainNameComboBox() is invoked.
	 * @param trainName
	 * @return
	 */
	private Boolean validateTrainName(String trainName) {
		boolean duplicateName = !trainName.equals(origTrainName) && 
				section.getRailLineChart().containTrainSectionWithTrainName(downGoing, trainName);
		
		if (duplicateName) {
			updateTrainNameTip(TrainNameStatus.DuplicateTrain);
			new MessageBox(__("Cannot use duplicate train name. \nPlease use another train name.")).showMessage();
		} else {
			RailNetworkChart chart = section.getRailLineChart().getRailNetworkChart();
			Train train = chart.findTrain(trainName);
			if (train == null) {
				updateTrainNameTip(TrainNameStatus.NewTrain);
			} else {
				updateTrainNameTip(TrainNameStatus.ExistingTrain);
			}
		}
		
		return !duplicateName;
	}
	
	private void updateTrainNameTip(TrainNameStatus status) {
		String text = "";
		Color fgColor = getForeground();
		Color bgColor = getBackground();
		switch (status) {
		case NewTrain:
			text = __("This is a new train");
			fgColor = Color.BLACK;
			bgColor = Color.decode("#99FF99");
			break;
		case ExistingTrain:
			text = __("This is an existing train");
			fgColor = Color.BLACK;
			bgColor = Color.decode("#ffff66");
			break;
		case DuplicateTrain:
			text = __("Error! Duplicate Train Name");
			fgColor = Color.WHITE;
			bgColor = Color.decode("#ff0000");
			break;
		default:
			break;
		}
		
		lblTrainNameTip.setText(text);
		lblTrainNameTip.setForeground(fgColor);
		lblTrainNameTip.setBackground(bgColor);
		
		repaint();
	}
	
	private void doTrain2_updateFullTrainNameList() {
		RailroadLineChart lineChart = (RailroadLineChart) cbTrain2Railline.getSelectedItem();
		
		allTrainsInRailline.clear();
		allTrainsInRailline.addAll(lineChart.trains);
		allTrainsInRailline.sort(Train.getTrainComparatorByTrainName());
	}
	
	private void doTrain2_updateTrain2Info() {
		train2 = (Train) cbTrain2FullTrainName.getSelectedItem();
		txtrTrain2VehicleName.setText(train2.vehicleName);

		RailroadLineChart lineChart = (RailroadLineChart) cbTrain2Railline.getSelectedItem();
		RailroadLine line = lineChart.railroadLine;
		Station firstStopInLine = line.getFirstStopOnMe(train2);
		Station lastStopInLine = line.getLastStopOnMe(train2);
		lblTrain2From.setText(firstStopInLine.getName());
		lblTrain2To.setText(lastStopInLine.getName());
		
		repaint();
	}
	
	private void do_OK() {
		setVisible(false);
	}
	
	private void do_Cancel() {
		uiBindingManager.cancelAllEditing();
		setVisible(false);
	}
	
	//}}
	
	// {{ Data Bindings
	
	private void DB_setupUIDataBinding() {
		for (Component component : allDataBindingComponent) {
			uiBindingManager.addDataBinding(component, this::DB_getModelObject, 
					this::DB_getPropertyDesc, this::DB_updateUIforModel);
		}

		uiBindingManager.addDataBinding(cbTrainName, this::DB_getModelObject, 
				this::DB_getPropertyDesc, (Function<String, Boolean>)this::validateTrainName, 
				this::DB_updateUIforModel);
		
		uiBindingManager.updateUI(null);
	}
	
	private Object DB_getModelObject(String propertyGroup) {
		if ("train".equals(propertyGroup)) {
//			return Config.getInstance();
		} else if ("section".equals(propertyGroup)) {
			return section;
		}
		
		return null;
	}
	
	private void DB_updateUIforModel(String propertyGroup) {
		mainFrame.chartView.updateData();
		mainFrame.sheetView.updateData();
		mainFrame.runView.repaint();
		
		mainFrame.timetableEditView.refreshColumn(columnIndex);
	}
	
	private String DB_getPropertyDesc(String propertyName) {
		String desc = propertyName;
		
		if ("name".equals(propertyName)) {
			desc = String.format(__("%s of selected train"), __("train name") );
		} else if ("vehicleName".equals(propertyName)) {
			desc = String.format(__("%s of selected train"), __("vehicle name") );
		} else if ("remarks".equals(propertyName)) {
			desc = String.format(__("%s of selected train"), __("remarks") );
		}
		
		return desc;
	}
}

enum TrainNameStatus {
	NewTrain, ExistingTrain, DuplicateTrain
}
