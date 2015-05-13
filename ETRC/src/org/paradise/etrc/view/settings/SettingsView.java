package org.paradise.etrc.view.settings;
import java.awt.BorderLayout;
import java.awt.Color;
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
import org.paradise.etrc.data.ChartSettings;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.util.Config;
import org.paradise.etrc.util.ui.databinding.JComboBoxBinding;
import org.paradise.etrc.util.ui.databinding.JTextFieldBinding;
import org.paradise.etrc.util.ui.databinding.UIBinding;
import org.paradise.etrc.util.ui.databinding.UIBindingFactory;

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
	private static Vector<JTextField> allTextFields = new Vector<>();
	private static Vector<JComboBox> allComboBoxes = new Vector<JComboBox>();
	
	private Vector<UIBinding<? extends Object, ? extends Object>> allUIBindings = 
			new Vector<UIBinding<? extends Object, ? extends Object>>();
	private JTextField txtGlobalhttpproxyserver;
	private JTextField txtGlobalhttpproxyport;

	/**
	 * Create the panel.
	 */
	public SettingsView(TrainGraph trainGraph) {
		mainFrame = MainFrame.getInstance();
		setModel(trainGraph);

		allTextFields.clear();
		allComboBoxes.clear();
		initUI();
		
		setupUIDataBinding();
		
		ui_inited = true;
	}
	
	public void setModel(TrainGraph trainGraph) {
		settings = trainGraph.settings;
		
		if(ui_inited) {
			allUIBindings.forEach(binding -> binding.setModel(settings));
		}
	}

	private void initUI() {
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
		lblDistanceUnit.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblPixelsunit = new JLabel(__("Pixels/Unit"));
		lblPixelsunit.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblDisplaylevel = new JLabel(__("Min Station Display Level"));
		lblDisplaylevel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblStationLevelFor = new JLabel(__("Level of Station as Bold Lines"));
		lblStationLevelFor.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JComboBox<String> cbDistanceUnit = createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<String>(new String[] {"km", "hm", "dam", "m", "mile"}), 0, "runningChart.distUnit");
		
		txtDistscale = createJTextField("runningChart.distScale");
		
		txtDisplayLevel = createJTextField("runningChart.displayLevel");
		
		txtBoldlinelevel = SettingsView.createJTextField("runningChart.boldLevel");
		txtBoldlinelevel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtBoldlinelevel.setColumns(5);
		
		JLabel lblPixelsmin = new JLabel(__("Pixels/minute"));
		lblPixelsmin.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblNewLabel = new JLabel(__("Start hour in chart"));
		lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JLabel lblMinsXaxis = new JLabel(__("Minute/x-axis scale"));
		lblMinsXaxis.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		txtMinutescale = SettingsView.createJTextField("runningChart.minuteScale");
		txtMinutescale.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtMinutescale.setColumns(5);
		
		txtStarthour = SettingsView.createJTextField("runningChart.startHour");
		txtStarthour.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtStarthour.setColumns(5);
		
		JComboBox<Integer> comboBox = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel(new Integer[] {60, 30, 20, 15, 10, 5}), 4, "runningChart.timeInterval");
//		comboBox.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
//		comboBox.setModel(new DefaultComboBoxModel(new Integer[] {60, 30, 20, 15, 10, 5}));
		GroupLayout gl_panel_runningChart = new GroupLayout(panel_runningChart);
		gl_panel_runningChart.setHorizontalGroup(
			gl_panel_runningChart.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_runningChart.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_runningChart.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addComponent(lblDistanceUnit)
							.addGap(96)
							.addComponent(cbDistanceUnit, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addComponent(lblPixelsunit)
							.addGap(110)
							.addComponent(txtDistscale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(108)
							.addComponent(lblPixelsmin)
							.addGap(48)
							.addComponent(txtMinutescale, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addComponent(lblDisplaylevel)
							.addGap(30)
							.addComponent(txtDisplayLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(108)
							.addComponent(lblNewLabel)
							.addGap(23)
							.addComponent(txtStarthour, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addComponent(lblStationLevelFor)
							.addGap(6)
							.addComponent(txtBoldlinelevel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(108)
							.addComponent(lblMinsXaxis)
							.addGap(12)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(13, Short.MAX_VALUE))
		);
		gl_panel_runningChart.setVerticalGroup(
			gl_panel_runningChart.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_runningChart.createSequentialGroup()
					.addContainerGap(29, Short.MAX_VALUE)
					.addGroup(gl_panel_runningChart.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(5)
							.addComponent(lblDistanceUnit))
						.addComponent(cbDistanceUnit, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(6)
					.addGroup(gl_panel_runningChart.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(6)
							.addComponent(lblPixelsunit))
						.addComponent(txtDistscale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(6)
							.addComponent(lblPixelsmin))
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(1)
							.addComponent(txtMinutescale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(5)
					.addGroup(gl_panel_runningChart.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(6)
							.addComponent(lblDisplaylevel))
						.addComponent(txtDisplayLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(6)
							.addComponent(lblNewLabel))
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(1)
							.addComponent(txtStarthour, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(5)
					.addGroup(gl_panel_runningChart.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(6)
							.addComponent(lblStationLevelFor))
						.addComponent(txtBoldlinelevel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(6)
							.addComponent(lblMinsXaxis))
						.addGroup(gl_panel_runningChart.createSequentialGroup()
							.addGap(1)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		panel_runningChart.setLayout(gl_panel_runningChart);
		
		JLabel lblPlanningSchedule = new JLabel(__("Planning Schedule"));
		lblPlanningSchedule.setBounds(19, 326, 103, 15);
		lblPlanningSchedule.setOpaque(true);
		lblPlanningSchedule.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JPanel panel_planningSchedule = new JPanel();
		panel_planningSchedule.setBounds(6, 333, 565, 94);
		panel_planningSchedule.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		
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
		
		JComboBox cbAutoLoadLastFile = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<String>(new String[] {"Yes", "No"}), 1, "global.AutoLoadLastFile");
		cbAutoLoadLastFile.setBounds(112, 15, 93, 27);
		cbAutoLoadLastFile.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JComboBox cbHttpProxyUse = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel<String>(new String[] {"Yes", "No"}), 1, "global.HttpProxyUse");
		cbHttpProxyUse.setBounds(112, 49, 93, 27);
		
		txtGlobalhttpproxyserver = SettingsView.createJTextField("global.HttpProxyServer");
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
		panel.add(lblPlanningSchedule);
		panel.add(panel_planningSchedule);
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblTip = new JLabel(__("Tip: Move the focus to another control to save the change."));
		panel_1.add(lblTip);
		lblTip.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
	}
	
	private void setupUIDataBinding() {
		for (JTextField tf : allTextFields) {
			// Such as "runningChart.distScale"
			String[] parts = tf.getText().split("\\.");
			String propertyGroup = parts[0];
			String propertyName = parts[1];
			
			// TODO: 配置界面, 实现 getPropertyDesc, 如果GLobal settings不止包含运行图的配置,则
			// mainFrame.raillineChartView::updateUI 应该改为对应的界面更新.
			JTextFieldBinding binding = UIBindingFactory.getJTextFieldBinding(tf, 
					getModelObject(propertyGroup), propertyName, getPropertyDesc(propertyName),
					() -> {
						updateUIforModel(propertyGroup);
					});
			tf.addFocusListener(binding);
			binding.updateUI();
			
			allUIBindings.add(binding);
		}
		
		for (JComboBox<? extends Object> cb : allComboBoxes) {
			// Such as "runningChart.distScale"
			String[] parts = cb.getName().split("\\.");
			String propertyGroup = parts[0];
			String propertyName = parts[1];

			// TODO: 配置界面, 实现 getPropertyDesc, 如果GLobal settings不止包含运行图的配置,则
			// mainFrame.raillineChartView::updateUI 应该改为对应的界面更新.
			JComboBoxBinding<Object, ? extends Object> binding = UIBindingFactory.getJComboBoxBindingBinding(cb, 
					getModelObject(propertyGroup), propertyName, getPropertyDesc(propertyName), 
					() -> {
						updateUIforModel(propertyGroup);
					});
			cb.addItemListener(binding);
			binding.updateUI();
			
			allUIBindings.add(binding);
		}
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source text ""
	 */
	public static JTextField createJTextField(String text) {
		// Use text value of JTextfield to keep property of data source model
		
		JTextField textField = new JTextField();
		textField.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		textField.setColumns(5);
		textField.setText(text);
		
		allTextFields.add(textField);
		
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
		
		allComboBoxes.add(comboBox);
		
		return comboBox;
	}
	
	public Object getModelObject(String propertyGroup) {
		if ("global".equals(propertyGroup)) {
			return Config.getInstance();
		} else if ("runningChart".equals(propertyGroup)) {
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
		
		return desc;
	}
}