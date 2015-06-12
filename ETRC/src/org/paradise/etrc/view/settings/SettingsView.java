package org.paradise.etrc.view.settings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import org.paradise.etrc.util.Config;
import org.paradise.etrc.util.ui.databinding.JComboBoxBinding;
import org.paradise.etrc.util.ui.databinding.JTextComponentBinding;
import org.paradise.etrc.util.ui.databinding.UIBinding;
import org.paradise.etrc.util.ui.databinding.UIBindingManager;

import static org.paradise.etrc.ETRC.__;
import javax.swing.LayoutStyle.ComponentPlacement;

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
	private JComboBox cbUseAntiAliasing;
	private JTextField txtTimetableEditRowHeaderWidth;
	private JTextField txtTimetableEditCellWidth;
	private JTextField timetableEditVehicleNameRowHeight;
	private JTextField timetableEditRemarksRowHeight;

	/**
	 * Create the panel.
	 */
	public SettingsView(TrainGraph trainGraph) {
		mainFrame = MainFrame.getInstance();
		setModel(trainGraph);

		initUI();
		
		setupUIDataBinding();
		
		ui_inited = true;
	}
	
	public void setModel(TrainGraph trainGraph) {
		settings = trainGraph.settings;
		
		if(ui_inited) {
			uiBindingManager.setModel(this::getModelObject, null);
		}
	}

	private synchronized void initUI() {
		allDataBindingComponent.clear();
		
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		
		JPanel panel_runningChart = new JPanel();
		panel_runningChart.setBounds(6, 162, 565, 152);
		panel_runningChart.setBorder(new LineBorder(new Color(192, 192, 192), 1, true));
		
		JLabel lblRuningChart = new JLabel(__("Runing Chart"));
		lblRuningChart.setBounds(19, 154, 85, 15);
		lblRuningChart.setOpaque(true);
		lblRuningChart.setHorizontalAlignment(SwingConstants.CENTER);
		lblRuningChart.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblDistanceUnit = new JLabel(__("Distance Unit"));
		lblDistanceUnit.setBounds(7, 24, 75, 15);
		lblDistanceUnit.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblPixelsunit = new JLabel(__("Pixels/Unit"));
		lblPixelsunit.setBounds(7, 57, 61, 15);
		lblPixelsunit.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblDisplaylevel = new JLabel(__("Min Station Display Level"));
		lblDisplaylevel.setBounds(7, 90, 141, 15);
		lblDisplaylevel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblStationLevelFor = new JLabel(__("Level of Station as Bold Lines"));
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
		
		JLabel lblPixelsmin = new JLabel(__("Pixels/minute"));
		lblPixelsmin.setBounds(355, 57, 78, 15);
		lblPixelsmin.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblNewLabel = new JLabel(__("Start hour in chart"));
		lblNewLabel.setBounds(355, 90, 103, 15);
		lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblMinsXaxis = new JLabel(__("Minute/x-axis scale"));
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
		
		JLabel lblUseAntialiasing = new JLabel(__("Use Anti-Aliasing"));
		lblUseAntialiasing.setBounds(355, 24, 98, 15);
		lblUseAntialiasing.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblTimetableEdit = new JLabel(__("Timetable Edit Sheet"));
		lblTimetableEdit.setBounds(19, 326, 126, 15);
		lblTimetableEdit.setOpaque(true);
		lblTimetableEdit.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JPanel panel_timetableEdit = new JPanel();
		panel_timetableEdit.setBounds(6, 333, 565, 95);
		panel_timetableEdit.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		
		JLabel lblGlobalSettings = new JLabel(__("Global Settings"));
		lblGlobalSettings.setBounds(19, 12, 85, 15);
		lblGlobalSettings.setOpaque(true);
		lblGlobalSettings.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JPanel panelGlobalSettings = new JPanel();
		panelGlobalSettings.setBounds(6, 20, 565, 122);
		panelGlobalSettings.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		
		JLabel lblAutoload = new JLabel(__("Auto Load"));
		lblAutoload.setBounds(7, 20, 58, 15);
		lblAutoload.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblUseHttpProxy = new JLabel(__("Use HTTP Proxy"));
		lblUseHttpProxy.setBounds(7, 54, 93, 15);
		lblUseHttpProxy.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblHttpProxyServer = new JLabel(__("HTTP Proxy Server:"));
		lblHttpProxyServer.setBounds(7, 92, 112, 15);
		lblHttpProxyServer.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JComboBox cbAutoLoadLastFile = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<String>(new String[] {"Yes", "No"}), 1, "global.AutoLoadLastFile:YesNo");
		cbAutoLoadLastFile.setBounds(112, 15, 93, 27);
		cbAutoLoadLastFile.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JComboBox cbHttpProxyUse = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<String>(new String[] {"Yes", "No"}), 1, "global.HttpProxyUse:YesNo");
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
		
		JLabel lblRowHeaderWidth = new JLabel(__("Row Header Width"));
		lblRowHeaderWidth.setBounds(7, 24, 107, 15);
		lblRowHeaderWidth.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblCellWidth = new JLabel(__("Cell Width"));
		lblCellWidth.setBounds(7, 57, 57, 15);
		lblCellWidth.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblVehicleNameRow = new JLabel(__("Vehicle Name Row Height"));
		lblVehicleNameRow.setBounds(318, 24, 148, 15);
		lblVehicleNameRow.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblRemarksRowHeight = new JLabel(__("Remarks Row Height"));
		lblRemarksRowHeight.setBounds(318, 57, 120, 15);
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
		
		JLabel lblPlanningSchedule = new JLabel("Planning Schedule");
		lblPlanningSchedule.setOpaque(true);
		lblPlanningSchedule.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		lblPlanningSchedule.setBounds(19, 440, 103, 15);
		panel.add(lblPlanningSchedule);
		
		JPanel panel_planningSchedule = new JPanel();
		panel_planningSchedule.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel_planningSchedule.setBounds(6, 447, 565, 94);
		panel.add(panel_planningSchedule);
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblTip = new JLabel(__("Tip: Move the focus to another control to save the change."));
		panel_1.add(lblTip);
		lblTip.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source name ""
	 */
	public static JTextField createJTextField(String name) {
		// Use text value of JTextfield to keep property of data source model
		
		JTextField textField = new JTextField();
		textField.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		textField.setColumns(5);
		textField.setName(name);
		
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
	
	// {{ Data Bindings
	
	private void setupUIDataBinding() {
		for (Component component : allDataBindingComponent) {
			uiBindingManager.addDataBinding(component, this::getModelObject, 
					this::getPropertyDesc, this::updateUIforModel);
		}
		
		uiBindingManager.updateUI(null);
	}
	
	public Object getModelObject(String propertyGroup) {
		if ("global".equals(propertyGroup)) {
			return Config.getInstance();
		} else if ("runningChart".equals(propertyGroup)) {
			return settings;
		} else if ("timetableEdit".equals(propertyGroup)) {
			return settings;
		}
		
		return null;
	}
	
	private void updateUIforModel(String propertyGroup) {
		if ("runningChart".equals(propertyGroup)) {
//			mainFrame.chartView.setModel(mainFrame.trainGraph, mainFrame.currentLineChart);
//			mainFrame.runView.setModel(mainFrame.trainGraph, mainFrame.currentLineChart);
			mainFrame.chartView.updateData();
			mainFrame.runView.updateUI();
		} else if ("timetableEdit".equals(propertyGroup)) {
			mainFrame.timetableEditView.refreshChart();
		}
	}
	
	public String getPropertyDesc(String propertyName) {
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
		}
		
		return desc;
	}
	
	// }}
}