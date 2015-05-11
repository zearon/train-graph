package org.paradise.etrc.view.settings;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import javafx.scene.control.ComboBox;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import java.awt.Font;

import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.border.LineBorder;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.GlobalSettings;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.util.ui.JComboBoxBinding;
import org.paradise.etrc.util.ui.JTextFieldBinding;
import org.paradise.etrc.util.ui.UIBinding;
import org.paradise.etrc.util.ui.UIBindingFactory;

import com.sun.xml.internal.org.jvnet.staxex.NamespaceContextEx.Binding;

import java.awt.Color;
import java.util.Vector;

import javax.swing.ComboBoxModel;

public class SettingsView extends JPanel {
	private JTextField txtDistscale;
	private JTextField txtDisplayLevel;
	private JTextField txtBoldlinelevel;
	private JTextField txtMinutescale;
	private JTextField txtStarthour;
	private boolean ui_inited;
	
	private GlobalSettings settings;
	
	private MainFrame mainFrame;
	
	// Use static fields to keep this object because 
	// eclipse window builder only support static factory methods
	// to create ui components.
	private static Vector<JTextField> allTextFields = new Vector<>();
	private static Vector<JComboBox> allComboBoxes = new Vector<JComboBox>();
	
	private Vector<UIBinding<? extends Object>> allUIBindings = new Vector<UIBinding<? extends Object>>();

	/**
	 * Create the panel.
	 */
	public SettingsView(TrainGraph trainGraph) {
		mainFrame = MainFrame.getInstance();
		setModel(trainGraph);

		allTextFields.clear();
		allComboBoxes.clear();
		initUI();
		
		setupUI();
		
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
		
		JLabel lblGlobalSettings = new JLabel(__("Global Settings"));
		lblGlobalSettings.setBounds(6, 6, 85, 15);
		lblGlobalSettings.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JPanel panel_runningChart = new JPanel();
		panel_runningChart.setBounds(6, 41, 565, 152);
		panel_runningChart.setBorder(new LineBorder(new Color(192, 192, 192), 1, true));
		
		JLabel lblRuningChart = new JLabel(__("Runing Chart"));
		lblRuningChart.setOpaque(true);
		lblRuningChart.setBounds(19, 33, 85, 15);
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
		
		JComboBox cbDistanceUnit = createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel(new String[] {"km", "hm", "dam", "m", "mile"}), 0, "runningChart.distUnit");
		
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
		
		JComboBox comboBox = SettingsView.createJComboBox(new Font("Lucida Grande", Font.PLAIN, 12), new DefaultComboBoxModel(new Integer[] {60, 30, 20, 15, 10, 5}), 4, "runningChart.timeInterval");
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
		panel.setLayout(null);
		
		JLabel lblPlanningSchedule = new JLabel(__("Planning Schedule"));
		lblPlanningSchedule.setOpaque(true);
		lblPlanningSchedule.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		lblPlanningSchedule.setBounds(16, 210, 103, 15);
		panel.add(lblPlanningSchedule);
		panel.add(lblRuningChart);
		panel.add(panel_runningChart);
		panel.add(lblGlobalSettings);
		
		JPanel panel_planningSchedule = new JPanel();
		panel_planningSchedule.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel_planningSchedule.setBounds(6, 216, 565, 178);
		panel.add(panel_planningSchedule);
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblTip = new JLabel(__("Tip: Move the focus to another control to save the change."));
		panel_1.add(lblTip);
		lblTip.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
	}
	
	private void setupUI() {
		for (JTextField tf : allTextFields) {
			// Such as "runningChart.distScale"
			String[] parts = tf.getText().split("\\.");
			String group = parts[0];
			String propertyName = parts[1];
			
			// TODO: 配置界面, 实现 getPropertyDesc, 如果GLobal settings不止包含运行图的配置,则
			// mainFrame.raillineChartView::updateUI 应该改为对应的界面更新.
			JTextFieldBinding binding = UIBindingFactory.getJTextFieldBinding(tf, 
					settings, propertyName, getPropertyDesc(propertyName),
					() -> {
						updateUIforModel(group);
					});
			tf.addFocusListener(binding);
			binding.updateUI();
			
			allUIBindings.add(binding);
		}
		
		for (JComboBox<? extends Object> cb : allComboBoxes) {
			// Such as "runningChart.distScale"
			String[] parts = cb.getName().split("\\.");
			String group = parts[0];
			String propertyName = parts[1];

			// TODO: 配置界面, 实现 getPropertyDesc, 如果GLobal settings不止包含运行图的配置,则
			// mainFrame.raillineChartView::updateUI 应该改为对应的界面更新.
			JComboBoxBinding<? extends Object> binding = UIBindingFactory.getJComboBoxBindingBinding(cb, 
					settings, propertyName, getPropertyDesc(propertyName), 
					() -> {
						updateUIforModel(group);
					});
			cb.addItemListener(binding);
			binding.updateUI();
			
			allUIBindings.add(binding);
		}
	}
	
	private void updateUIforModel(String propertyGroup) {
		if ("runningChart".equals(propertyGroup)) {
//			mainFrame.chartView.setModel(mainFrame.trainGraph, mainFrame.currentLineChart);
//			mainFrame.runView.setModel(mainFrame.trainGraph, mainFrame.currentLineChart);
			mainFrame.chartView.updateData();
			mainFrame.runView.updateUI();
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
	public static JComboBox createJComboBox(Font font, ComboBoxModel model, int selectedIndex, String name) {
		// Use name attribute of JComboBox to keep property of data source model
		
		JComboBox comboBox = new JComboBox();
		comboBox.setFont(font);
		comboBox.setModel(model);
		comboBox.setSelectedIndex(selectedIndex);
		comboBox.setName(name);
		
		allComboBoxes.add(comboBox);
		
		return comboBox;
	}
	
	public String getPropertyDesc(String propertyName) {
		String desc = "";
		
		if ("distScale".equals(propertyName)) {
			desc = __("pixels/Unit") + __(" of running chart");
		} else if ("displayLevel".equals(propertyName)) {
			desc = __("minimal station level to be displayed") + __(" of running chart");
		} else if ("boldLevel".equals(propertyName)) {
			desc = __("level of station as bold line") + __(" of running chart");
		} else if ("startHour".equals(propertyName)) {
			desc = __("starting hour") + __(" of running chart");
		} else if ("minuteScale".equals(propertyName)) {
			desc = __("pixels/minute") + __(" of running chart");
		} else if ("timeInterval".equals(propertyName)) {
			desc = __("minutes/x-axis gap") + __(" of running chart");
		} else if ("distUnit".equals(propertyName)) {
			desc = __("distance unit") + __(" of running chart");
		}
		
		return desc;
	}
}