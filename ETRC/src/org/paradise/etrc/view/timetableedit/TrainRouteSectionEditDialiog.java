package org.paradise.etrc.view.timetableedit;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainRouteSection;
import org.paradise.etrc.data.v1.TrainType;
import org.paradise.etrc.util.Config;
import org.paradise.etrc.util.ui.databinding.UIBindingManager;

import java.awt.Font;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ComboBoxModel;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TrainRouteSectionEditDialiog extends JDialog {

	// Use static fields to keep this object because 
	// eclipse window builder only support static factory methods
	// to create ui components.
	private static Vector<Component> allDataBindingComponent = new Vector<>();
	private UIBindingManager uiBindingManager = UIBindingManager.getInstance(this);

	private final JPanel contentPanel = new JPanel();
	private JTextField txtVehicleName;
	private final JTextArea txtrRemarks = new JTextArea();
	
	private MainFrame mainFrame;
	private boolean ui_inited;
	
	TrainGraph trainGraph;
	Train train;
	TrainRouteSection section;
	boolean downGoing;
	private JComboBox<String> cbTrainName;
	private JLabel lblTrainType;

	/**
	 * Create the dialog.
	 */
	public TrainRouteSectionEditDialiog(TrainGraph trainGraph, TrainRouteSection section) {
		mainFrame = MainFrame.getInstance();
		this.trainGraph = trainGraph;
		this.section = section;
		this.downGoing = section.downGoing;
		
		allDataBindingComponent.clear();
		initUI();
		
		setupUIDataBinding();
		
		ui_inited = true;
	}
	
	private void initUI() {
		setBounds(100, 100, 450, 275);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblTrainname = createJLabel("Train Name", new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainname.setBounds(6, 6, 72, 16);
			contentPanel.add(lblTrainname);
		}
		{
			JLabel lblTrainTypeLabel = TrainRouteSectionEditDialiog.createJLabel("Train Type", new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainTypeLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainTypeLabel.setBounds(6, 34, 66, 16);
			contentPanel.add(lblTrainTypeLabel);
		}
		{
			JLabel lblVehicleName = TrainRouteSectionEditDialiog.createJLabel("Vehicle Name", new Font("Lucida Grande", Font.PLAIN, 12));
			lblVehicleName.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblVehicleName.setBounds(6, 62, 85, 16);
			contentPanel.add(lblVehicleName);
		}
		{
			JLabel lblRemarks = TrainRouteSectionEditDialiog.createJLabel("Remarks", new Font("Lucida Grande", Font.PLAIN, 12));
			lblRemarks.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblRemarks.setBounds(6, 90, 61, 16);
			contentPanel.add(lblRemarks);
		}
		
		String[] allTrainNames = trainGraph == null ? new String[0] : trainGraph.currentNetworkChart.trains.stream()
				.map(train -> train.getTrainName(downGoing)).toArray(String[]::new);
		cbTrainName = TrainRouteSectionEditDialiog.createJComboBox(
				new DefaultComboBoxModel<String>(allTrainNames), 
				"section.name", new Font("Lucida Grande", Font.PLAIN, 12));
		cbTrainName.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				do_UpdateTrainType();
			}
		});
		cbTrainName.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				do_UpdateTrainType();
			}
		});
		cbTrainName.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					do_UpdateTrainType();
			}
		});
		cbTrainName.setEditable(true);
		cbTrainName.setBounds(110, 2, 134, 27);
		contentPanel.add(cbTrainName);
		
		TrainType[] allTrainTypes = trainGraph.allTrainTypes().toArray(new TrainType[0]);
		
		txtVehicleName = new JTextField();
		allDataBindingComponent.add(txtVehicleName);
		txtVehicleName.setName("section.vehicleName");
		txtVehicleName.setBounds(110, 56, 298, 28);
		contentPanel.add(txtVehicleName);
		txtVehicleName.setColumns(10);
		txtrRemarks.setName("section.remarks");
		txtrRemarks.setLineWrap(true);
		txtrRemarks.setBounds(114, 90, 294, 110);
		allDataBindingComponent.add(txtrRemarks);
		contentPanel.add(txtrRemarks);
		{
			lblTrainType = new JLabel("trainType");
			lblTrainType.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainType.setBounds(114, 34, 294, 16);
			contentPanel.add(lblTrainType);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setVisible(false);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}
	
	protected void do_UpdateTrainType() {
		String trainName = (String) cbTrainName.getSelectedItem();
		TrainType type = trainGraph.guessTrainTypeByName(trainName);
		lblTrainType.setText(type.getName());
		lblTrainType.setForeground(type.getLineColor());
		lblTrainType.repaint();
	}

	public void showDialog() {
		Rectangle dlgSize = getBounds();
		Dimension frmSize;
		Point loc;
		frmSize = Toolkit.getDefaultToolkit().getScreenSize();
		loc = new Point(0,0);
		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		super.setVisible(true);
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
		if ("train".equals(propertyGroup)) {
//			return Config.getInstance();
		} else if ("section".equals(propertyGroup)) {
			return section;
		}
		
		return null;
	}
	
	private void updateUIforModel(String propertyGroup) {
			mainFrame.chartView.updateData();
			mainFrame.sheetView.updateData();
			mainFrame.runView.repaint();
			
			mainFrame.timetableEditView.refreshChart();
	}
	
	public String getPropertyDesc(String propertyName) {
		String desc = propertyName;
		
		if ("name".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("pixels/Unit") );
		} else if ("vehicleName".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("minimal station level to be displayed") );
		} else if ("remarks".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("level of station as bold line") );
		}
		
		return desc;
	}
	
	// }}
}
