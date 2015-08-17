package org.paradise.etrc.view.settings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.v1.ChartSettings;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.util.config.Config;

import com.zearon.util.ui.databinding.UIBindingManager;

import static org.paradise.etrc.ETRC.__;

public class SettingsView extends JPanel {
	private JTextField txtDistscale;
	private JTextField txtDisplayLevel;
	private JTextField txtBoldlinelevel;
	private JTextField txtMinutescale;
	private JTextField txtStarthour;
	private boolean ui_inited;
	
	private ChartSettings settings;
	
	private MainFrame mainFrame;
	
	// Use static fields to keep this object because 
	// eclipse window builder only support static factory methods
	// to create ui components.
	private static Vector<Component> allDataBindingComponent = new Vector<>();
	private UIBindingManager uiBindingManager = UIBindingManager.getInstance(this);
	
	private JTextField txtGlobalhttpproxyserver;
	private JTextField txtGlobalhttpproxyport;
	private JComboBox<String> cbUseAntiAliasing;
	private JTextField txtTimetableEditRowHeaderWidth;
	private JTextField txtTimetableEditCellWidth;
	private JTextField timetableEditVehicleNameRowHeight;
	private JTextField timetableEditRemarksRowHeight;
	private JPanel panel;
	private JPanel panelKeySettings;
	private JTextField txtSE_notgothrough;
	private JTextField txtSE_stopAtTerminalStation;
	private JTextField txtSE_stop;
	private JTextField txtSE_technicalStop;
	private JTextField txtSE_pass;
	private JTextField txtSE_stopAtStartStation;
	private JTextField textField_1;

	/**
	 * Create the panel.
	 */
	public SettingsView(TrainGraph trainGraph) {
		mainFrame = MainFrame.getInstance();
		setModel(trainGraph);

		initUI();
		// set preferred size of panel in order to fit the scroll pane.
		Rectangle rect = panelKeySettings.getBounds();
		panel.setPreferredSize(new Dimension(rect.x + rect.width + 10, rect.y + rect.height + 10));
		
		UIDB_setupUIDataBinding();
		
		ui_inited = true;
	}
	
	public void setModel(TrainGraph trainGraph) {
		settings = trainGraph.settings;
		
		if(ui_inited) {
			uiBindingManager.setModel(this::UIDB_getModelObject, null);
		}
	}
	
	// {{ init UI

	private synchronized void initUI() {
		allDataBindingComponent.clear();
		
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		panel = new JPanel();
		scrollPane.setViewportView(panel);
		
		JPanel panel_runningChart = new JPanel();
		panel_runningChart.setBounds(6, 162, 565, 150);
		panel_runningChart.setBorder(new LineBorder(new Color(192, 192, 192), 1, true));
		
		JLabel lblRuningChart = SettingsView.createJLabel("Runing Chart", true, new Font("Lucida Grande", Font.PLAIN, 12));
		lblRuningChart.setBounds(19, 154, 85, 15);
		lblRuningChart.setHorizontalAlignment(SwingConstants.CENTER);
		lblRuningChart.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblDistanceUnit = SettingsView.createJLabel("Distance Unit", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblDistanceUnit.setBounds(7, 24, 165, 15);
		lblDistanceUnit.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblPixelsunit = SettingsView.createJLabel("Pixels/Unit", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblPixelsunit.setBounds(7, 57, 165, 15);
		lblPixelsunit.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblDisplaylevel = SettingsView.createJLabel("Min Station Display Level", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblDisplaylevel.setBounds(7, 90, 165, 15);
		lblDisplaylevel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblStationLevelFor = SettingsView.createJLabel("Level of Station as Bold Lines", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblStationLevelFor.setBounds(7, 123, 165, 15);
		lblStationLevelFor.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JComboBox<String> cbDistanceUnit = createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<String>(new String[] {"km", "hm", "dam", "m", "mile"}), 0, "runningChart.distUnit");
		cbDistanceUnit.setBounds(178, 18, 69, 27);
		
		txtDistscale = createJTextField("runningChart.distScale");
		txtDistscale.setBounds(178, 51, 69, 27);
		
		txtDisplayLevel = createJTextField("runningChart.displayLevel");
		txtDisplayLevel.setBounds(178, 84, 69, 27);
		
		txtBoldlinelevel = SettingsView.createJTextField("runningChart.boldLevel");
		txtBoldlinelevel.setBounds(178, 117, 69, 27);
		txtBoldlinelevel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtBoldlinelevel.setColumns(5);
		
		JLabel lblPixelsmin = SettingsView.createJLabel("Pixels/minute", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblPixelsmin.setBounds(355, 57, 114, 15);
		lblPixelsmin.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblNewLabel = SettingsView.createJLabel("Start hour in chart", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblNewLabel.setBounds(355, 90, 114, 15);
		lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblMinsXaxis = SettingsView.createJLabel("Minute/x-axis scale", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblMinsXaxis.setBounds(355, 123, 114, 15);
		lblMinsXaxis.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		txtMinutescale = SettingsView.createJTextField("runningChart.minuteScale");
		txtMinutescale.setBounds(481, 52, 72, 27);
		txtMinutescale.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtMinutescale.setColumns(5);
		
		txtStarthour = SettingsView.createJTextField("runningChart.startHour");
		txtStarthour.setBounds(481, 85, 72, 27);
		txtStarthour.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtStarthour.setColumns(5);
		
		JComboBox<Integer> comboBox = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<Integer>(new Integer[] {60, 30, 20, 15, 10, 5}), 4, "runningChart.timeInterval");
		comboBox.setBounds(481, 118, 72, 27);
		
		cbUseAntiAliasing = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<String>(new String[] {"Yes", "No"}), 1, "runningChart.useAntiAliasing:YesNo");
		cbUseAntiAliasing.setBounds(481, 18, 72, 27);
		
		JLabel lblUseAntialiasing = SettingsView.createJLabel("Use Anti-Aliasing", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblUseAntialiasing.setBounds(355, 24, 114, 15);
		lblUseAntialiasing.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblTimetableEdit = SettingsView.createJLabel("Timetable Edit Sheet", true, new Font("Lucida Grande", Font.PLAIN, 12));
		lblTimetableEdit.setBounds(19, 324, 126, 15);
		lblTimetableEdit.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JPanel panel_timetableEdit = new JPanel();
		panel_timetableEdit.setBounds(6, 331, 565, 116);
		panel_timetableEdit.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		
		JLabel lblGlobalSettings = createJLabel("Global Settings", true, new Font("Lucida Grande", Font.PLAIN, 12));
		lblGlobalSettings.setBounds(19, 12, 85, 15);
		
		JPanel panelGlobalSettings = new JPanel();
		panelGlobalSettings.setBounds(6, 20, 565, 122);
		panelGlobalSettings.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		
		JLabel lblAutoload = SettingsView.createJLabel("Auto Load", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblAutoload.setBounds(7, 20, 93, 15);
		lblAutoload.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblUseHttpProxy = SettingsView.createJLabel("Use HTTP Proxy", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblUseHttpProxy.setBounds(7, 54, 109, 15);
		lblUseHttpProxy.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblHttpProxyServer = SettingsView.createJLabel("HTTP Proxy Server:", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblHttpProxyServer.setBounds(7, 92, 112, 15);
		lblHttpProxyServer.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JComboBox<String> cbAutoLoadLastFile = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<String>(new String[] {"Yes", "No"}), 1, "global.AutoLoadLastFile:YesNo");
		cbAutoLoadLastFile.setBounds(112, 15, 93, 27);
		cbAutoLoadLastFile.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JComboBox<String> cbHttpProxyUse = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<String>(new String[] {"Yes", "No"}), 1, "global.HttpProxyUse:YesNo");
		cbHttpProxyUse.setBounds(112, 49, 93, 27);
		
		txtGlobalhttpproxyserver = SettingsView.createJTextField("global.HttpProxyServer");
		txtGlobalhttpproxyserver.setName("global.HttpProxyServer");
		txtGlobalhttpproxyserver.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtGlobalhttpproxyserver.setBounds(122, 85, 181, 28);
		txtGlobalhttpproxyserver.setColumns(10);
		
		JLabel label = new JLabel(":");
		label.setBounds(302, 91, 4, 16);
		
		txtGlobalhttpproxyport = SettingsView.createJTextField("global.HttpProxyPort");
		txtGlobalhttpproxyport.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtGlobalhttpproxyport.setBounds(306, 85, 109, 28);
		txtGlobalhttpproxyport.setColumns(10);
		panel.setLayout(null);
		
		JLabel lblKeySettings = SettingsView.createJLabel("Key Settings", true, new Font("Lucida Grande", Font.PLAIN, 12));
		lblKeySettings.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		lblKeySettings.setBounds(596, 12, 69, 15);
		panel.add(lblKeySettings);
		panel.add(lblGlobalSettings);
		panel.add(panelGlobalSettings);
		panelGlobalSettings.setLayout(null);
		panelGlobalSettings.add(cbAutoLoadLastFile);
		panelGlobalSettings.add(cbHttpProxyUse);
		panelGlobalSettings.add(txtGlobalhttpproxyserver);
		panelGlobalSettings.add(label);
		panelGlobalSettings.add(txtGlobalhttpproxyport);
		panelGlobalSettings.add(lblAutoload);
		panelGlobalSettings.add(lblUseHttpProxy);
		panelGlobalSettings.add(lblHttpProxyServer);
		
		JLabel lblFullScreenOn = SettingsView.createJLabel("Full Screen On Startup for OS X", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblFullScreenOn.setBounds(281, 19, 188, 16);
		panelGlobalSettings.add(lblFullScreenOn);
		
		JComboBox cbFullScreenOnStartup = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel(new String[] {"Yes", "No"}), 1, "global.FullScreenOnStartupForOSX:YesNo");
		cbFullScreenOnStartup.setBounds(481, 15, 72, 27);
		panelGlobalSettings.add(cbFullScreenOnStartup);
		panel.add(lblRuningChart);
		panel.add(panel_runningChart);
		panel_runningChart.setLayout(null);
		panel_runningChart.add(lblStationLevelFor);
		panel_runningChart.add(txtBoldlinelevel);
		panel_runningChart.add(lblMinsXaxis);
		panel_runningChart.add(lblPixelsunit);
		panel_runningChart.add(txtDistscale);
		panel_runningChart.add(lblDistanceUnit);
		panel_runningChart.add(cbDistanceUnit);
		panel_runningChart.add(lblPixelsmin);
		panel_runningChart.add(lblUseAntialiasing);
		panel_runningChart.add(cbUseAntiAliasing);
		panel_runningChart.add(comboBox);
		panel_runningChart.add(txtMinutescale);
		panel_runningChart.add(lblDisplaylevel);
		panel_runningChart.add(txtDisplayLevel);
		panel_runningChart.add(lblNewLabel);
		panel_runningChart.add(txtStarthour);
		panel.add(lblTimetableEdit);
		panel.add(panel_timetableEdit);
		
		txtTimetableEditRowHeaderWidth = SettingsView.createJTextField("timetableEdit.timetableEditRowHeaderWidth");
		txtTimetableEditRowHeaderWidth.setBounds(178, 18, 69, 27);
		txtTimetableEditRowHeaderWidth.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtTimetableEditRowHeaderWidth.setColumns(5);
		
		txtTimetableEditCellWidth = SettingsView.createJTextField("timetableEdit.timetableEditCellWidth");
		txtTimetableEditCellWidth.setBounds(178, 51, 69, 27);
		txtTimetableEditCellWidth.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtTimetableEditCellWidth.setColumns(5);
		
		timetableEditVehicleNameRowHeight = SettingsView.createJTextField("timetableEdit.timetableEditVehicleNameRowHeight");
		timetableEditVehicleNameRowHeight.setBounds(481, 18, 69, 27);
		timetableEditVehicleNameRowHeight.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		timetableEditVehicleNameRowHeight.setColumns(5);
		
		timetableEditRemarksRowHeight = SettingsView.createJTextField("timetableEdit.timetableEditRemarksRowHeight");
		timetableEditRemarksRowHeight.setBounds(481, 51, 69, 27);
		timetableEditRemarksRowHeight.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		timetableEditRemarksRowHeight.setColumns(5);
		
		JLabel lblRowHeaderWidth = SettingsView.createJLabel("Row Header Width", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblRowHeaderWidth.setBounds(7, 24, 159, 15);
		lblRowHeaderWidth.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblCellWidth = SettingsView.createJLabel("Cell Width", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblCellWidth.setBounds(7, 57, 159, 15);
		lblCellWidth.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblVehicleNameRow = SettingsView.createJLabel("Vehicle Name Row Height", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblVehicleNameRow.setBounds(318, 24, 159, 15);
		lblVehicleNameRow.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblRemarksRowHeight = SettingsView.createJLabel("Remarks Row Height", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblRemarksRowHeight.setBounds(318, 57, 159, 15);
		lblRemarksRowHeight.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		panel_timetableEdit.setLayout(null);
		panel_timetableEdit.add(txtTimetableEditRowHeaderWidth);
		panel_timetableEdit.add(txtTimetableEditCellWidth);
		panel_timetableEdit.add(timetableEditVehicleNameRowHeight);
		panel_timetableEdit.add(timetableEditRemarksRowHeight);
		panel_timetableEdit.add(lblRowHeaderWidth);
		panel_timetableEdit.add(lblCellWidth);
		panel_timetableEdit.add(lblVehicleNameRow);
		panel_timetableEdit.add(lblRemarksRowHeight);
		
		textField_1 = createJTextField("timetableEdit.timetableEditTrainNumberIncrement");
		textField_1.setBounds(178, 84, 69, 27);
		panel_timetableEdit.add(textField_1);
		textField_1.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		textField_1.setColumns(5);
		
		JLabel lblUseTrainTypeColor = createJLabel("Use train type font color", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblUseTrainTypeColor.setBounds(318, 90, 138, 15);
		panel_timetableEdit.add(lblUseTrainTypeColor);
		
		JComboBox comboBox_1 = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel(new String[] {"Yes", "No"}), 1, "timetableEdit.timetableEditUseTrainTypeFontColor:YesNo");
		comboBox_1.setBounds(481, 85, 72, 27);
		panel_timetableEdit.add(comboBox_1);
		comboBox_1.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"Yes", "No"}));
		
		JLabel lblTrainNumberIncrement = createJLabel("Train Number Increment", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblTrainNumberIncrement.setBounds(7, 90, 165, 15);
		panel_timetableEdit.add(lblTrainNumberIncrement);
		
		JLabel lblPlanningSchedule = SettingsView.createJLabel("Planning Schedule", true, new Font("Lucida Grande", Font.PLAIN, 12));
		lblPlanningSchedule.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		lblPlanningSchedule.setBounds(19, 459, 103, 15);
		panel.add(lblPlanningSchedule);
		
		JPanel panel_planningSchedule = new JPanel();
		panel_planningSchedule.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel_planningSchedule.setBounds(6, 466, 565, 94);
		panel.add(panel_planningSchedule);
		
		panelKeySettings = new JPanel();
		panelKeySettings.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panelKeySettings.setBounds(583, 21, 212, 539);
		panel.add(panelKeySettings);
		panelKeySettings.setLayout(null);
		
		JLabel lblStopEditing = SettingsView.createJLabel("Timetable Editing", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblStopEditing.setBounds(6, 5, 105, 15);
		panelKeySettings.add(lblStopEditing);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(6, 26, 200, 172);
		panelKeySettings.add(panel_1);
		panel_1.setLayout(null);
		
		txtSE_notgothrough = SettingsView.createKeyJTextField("global.KeyTE_NotGoThrough"); 
		txtSE_notgothrough.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtSE_notgothrough.setBounds(166, 0, 28, 28);
		panel_1.add(txtSE_notgothrough);
		txtSE_notgothrough.setColumns(1);
		
		txtSE_stopAtTerminalStation = SettingsView.createKeyJTextField("global.KeyTE_StopAtTerminalStation");
		txtSE_stopAtTerminalStation.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtSE_stopAtTerminalStation.setColumns(10);
		txtSE_stopAtTerminalStation.setBounds(166, 84, 28, 28);
		panel_1.add(txtSE_stopAtTerminalStation);
		
		txtSE_stop = SettingsView.createKeyJTextField("global.KeyTE_StopForPassenger");
		txtSE_stop.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtSE_stop.setColumns(10);
		txtSE_stop.setBounds(166, 112, 28, 28);
		panel_1.add(txtSE_stop);
		
		txtSE_technicalStop = SettingsView.createKeyJTextField("global.KeyTE_StopNoPassenger");
		txtSE_technicalStop.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtSE_technicalStop.setColumns(10);
		txtSE_technicalStop.setBounds(166, 140, 28, 28);
		panel_1.add(txtSE_technicalStop);
		
		JLabel lblNotGoThrough = SettingsView.createJLabel("Not Go Through", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblNotGoThrough.setBounds(6, 5, 141, 16);
		panel_1.add(lblNotGoThrough);
		
		JLabel lblPass = SettingsView.createJLabel("Pass", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblPass.setBounds(6, 33, 101, 16);
		panel_1.add(lblPass);
		
		JLabel lblStopForPassenger = SettingsView.createJLabel("Stop", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblStopForPassenger.setBounds(6, 117, 124, 16);
		panel_1.add(lblStopForPassenger);
		
		JLabel lblStopWithoutPassenger = SettingsView.createJLabel("Technical Stop", false, new Font("Lucida Grande", Font.PLAIN, 12));
		lblStopWithoutPassenger.setBounds(6, 145, 141, 16);
		panel_1.add(lblStopWithoutPassenger);
		
		txtSE_pass = createKeyJTextField("global.KeyTE_PASS");
		txtSE_pass.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtSE_pass.setColumns(1);
		txtSE_pass.setBounds(166, 28, 28, 28);
		panel_1.add(txtSE_pass);
		
		txtSE_stopAtStartStation = createKeyJTextField("global.KeyTE_StopAtStartStation");
		txtSE_stopAtStartStation.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtSE_stopAtStartStation.setColumns(1);
		txtSE_stopAtStartStation.setBounds(166, 56, 28, 28);
		panel_1.add(txtSE_stopAtStartStation);
		
		JLabel label_1 = createJLabel("Stop at Start Station", false, new Font("Lucida Grande", Font.PLAIN, 12));
		label_1.setBounds(6, 61, 124, 16);
		panel_1.add(label_1);
		
		JLabel label_2 = createJLabel("Stop at Terminal Station", false, new Font("Lucida Grande", Font.PLAIN, 12));
		label_2.setBounds(6, 89, 148, 16);
		panel_1.add(label_2);
		
		JPanel panelStatusBar = new JPanel();
		add(panelStatusBar, BorderLayout.SOUTH);
		panelStatusBar.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblTip = new JLabel("Tip: Move the focus to another control to save the change.");
		panelStatusBar.add(lblTip);
		lblTip.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source text ""
	 * @wbp.factory.parameter.source opaque false
	 * @wbp.factory.parameter.source font new java.awt.Font("Lucida Grande", java.awt.Font.PLAIN, 12)
	 */
	public static JLabel createJLabel(String text, boolean opaque, Font font) {
		JLabel label = new JLabel(__(text));
		label.setOpaque(opaque);
		label.setFont(font);
		return label;
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source name ""
	 */
	public static JTextField createJTextField(String name) {
		JTextField textField = new JTextField();
		textField.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		textField.setColumns(5);
		textField.setName(name);
		
		allDataBindingComponent.add(textField);
		
		return textField;
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source name ""
	 */
	public static JTextField createKeyJTextField(String name) {
		JTextField textField = new JTextField();
		textField.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		textField.setColumns(1);
		textField.setName(name);
		textField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				char keyChar = e.getKeyChar();
				if (keyChar >= 'a' && keyChar <= 'z')
					keyChar -= 32; // Change to upper case letter
				
				e.consume();
				
				if (keyChar < 'A' || keyChar > 'Z') {
					MessageBox mb = new MessageBox(__("Only letters allowed."));
					mb.showMessage();
					return;
				}
				
				textField.setText("" + keyChar);
			}
			
			@Override public void keyReleased(KeyEvent e) {}
			@Override public void keyPressed(KeyEvent e) {}
		});
		
		allDataBindingComponent.add(textField);
		
		return textField;
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source font new java.awt.Font("Lucida Grande", java.awt.Font.PLAIN, 12)
	 * @wbp.factory.parameter.source model new javax.swing.DefaultComboBoxModel(new java.lang.String[] {"km", "hm", "dam", "m", "mile"})
	 * @wbp.factory.parameter.source selectedIndex 0
	 * @wbp.factory.parameter.source name "distUnit"
	 */
	public static <E> JComboBox<E> createJComboBox(Font font, ComboBoxModel<E> model, int selectedIndex, String name) {
		// Use name attribute of JComboBox to keep property of data source model
		
		JComboBox<E> comboBox = new JComboBox<E>();
		comboBox.setFont(font);
		comboBox.setModel(model);
		comboBox.setSelectedIndex(selectedIndex);
		comboBox.setName(name);
		
		allDataBindingComponent.add(comboBox);
		
		return comboBox;
	}
	
	// }}
	
	// {{ Data Bindings
	
	private void UIDB_setupUIDataBinding() {
		for (Component component : allDataBindingComponent) {
			uiBindingManager.addDataBinding(component, this::UIDB_getModelObject, 
					this::UIDB_getPropertyDesc, this::UIDB_updateUIforModel);
		}
		
		uiBindingManager.updateUI(null);
	}
	
	private Object UIDB_getModelObject(String propertyGroup) {
		if ("global".equals(propertyGroup)) {
			return Config.getInstance();
		} else if ("runningChart".equals(propertyGroup)) {
			return settings;
		} else if ("timetableEdit".equals(propertyGroup)) {
			return settings;
		}
		
		return null;
	}
	
	private void UIDB_updateUIforModel(String propertyGroup) {
		if ("runningChart".equals(propertyGroup)) {
//			mainFrame.chartView.setModel(mainFrame.trainGraph, mainFrame.currentLineChart);
//			mainFrame.runView.setModel(mainFrame.trainGraph, mainFrame.currentLineChart);
			mainFrame.runningChartView.refresh();
		} else if ("timetableEdit".equals(propertyGroup)) {
			mainFrame.timetableEditView.refreshChart();
		}
	}
	
	private String UIDB_getPropertyDesc(String propertyName) {
		String desc = "";
		
		if ("distScale".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("pixels/Unit") );
		} else if ("displayLevel".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("minimal station level to be displayed") );
		} else if ("boldLevel".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("level of station as bold line") );
		} else if ("startHour".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("starting hour") );
		} else if ("minuteScale".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("pixels/minute") );
		} else if ("timeInterval".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("minutes/x-axis gap") );
		} else if ("distUnit".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("distance unit") );
		}
		
		else if ("AutoLoadLastFile".equals(propertyName)) {
			desc = String.format(__("%s in global settings"), __("auto load last file value") );
		} else if ("HttpProxyUse".equals(propertyName)) {
			desc = String.format(__("%s in global settings"), __("use http proxy value") );
		} else if ("HttpProxyServer".equals(propertyName)) {
			desc = String.format(__("%s in global settings"), __("http proxy server value") );
		}else if ("HttpProxyPort".equals(propertyName)) {
			desc = String.format(__("%s in global settings"), __("http port value") );
		}
		
		else if ("timetableEditRowHeaderWidth".equals(propertyName)) {
			desc = String.format(__("%s in timetable edit sheet settings"), __("row header width"));
		} else if ("timetableEditCellWidth".equals(propertyName)) {
			desc = String.format(__("%s in timetable edit sheet settings"), __("cell width"));
		} else if ("timetableEditVehicleNameRowHeight".equals(propertyName)) {
			desc = String.format(__("%s in timetable edit sheet settings"), __("vehicle name row height"));
		} else if ("timetableEditRemarksRowHeight".equals(propertyName)) {
			desc = String.format(__("%s in timetable edit sheet settings"), __("remarks row height"));
		} else if ("timetableEditTrainNumberIncrement".equals(propertyName)) {
			desc = String.format(__("%s in timetable edit sheet settings"), __("train number incrememt"));
		} else if ("timetableEditUseTrainTypeFontColor".equals(propertyName)) {
			desc = String.format(__("%s in timetable edit sheet settings"), __("use train type font color"));
		}
		
		else if ("KeyTE_NotGoThrough".equals(propertyName)) {
			desc = String.format(__("%s in key settings for timetable editing"), __("not go through key"));
		} else if ("KeyTE_PASS".equals(propertyName)) {
			desc = String.format(__("%s in key settings for timetable editing"), __("pass key"));
		} else if ("KeyTE_StopForPassenger".equals(propertyName)) {
			desc = String.format(__("%s in key settings for timetable editing"), __("stop for passenger key"));
		} else if ("KeyTE_StopNoPassenger".equals(propertyName)) {
			desc = String.format(__("%s in key settings for timetable editing"), __("stop without passenger key"));
		}
		
		return desc;
	}
}