package org.paradise.etrc.view.traintypes;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import java.awt.Component;

import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.data.v1.RailNetworkChart;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.TrainType;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.util.Config;
import org.paradise.etrc.util.data.ValueTypeConverter;
import org.paradise.etrc.util.ui.FontUtil;
import org.paradise.etrc.util.ui.JColorChooserLabel;
import org.paradise.etrc.util.ui.databinding.JComboBoxBinding;
import org.paradise.etrc.util.ui.databinding.JTextFieldBinding;
import org.paradise.etrc.util.ui.databinding.UIBinding;
import org.paradise.etrc.util.ui.databinding.UIBindingManager;
import org.paradise.etrc.util.ui.databinding.converter.FontStyleConverter;
import org.paradise.etrc.util.ui.databinding.converter.ValueConverterManager;
import org.paradise.etrc.util.ui.table.JEditTable;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ComboBoxModel;

import org.paradise.etrc.view.alltrains.TrainListView;

import javax.swing.border.Border;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class TrainTypesView extends JPanel {

	TrainGraph trainGraph;
	private boolean ui_inited;
	private MainFrame _mainFrame;
	
	// Use static fields to keep this object because 
	// eclipse window builder only support static factory methods
	// to create ui components.
	private static Vector<Component> allDataBindingComponent = new Vector<>();
	private UIBindingManager uiBindingManager = UIBindingManager.getInstance(this);
	
	private JEditTable table;
	private TrainTypeTableModel tableModel;
	private JScrollPane scrollPane;
	private JTextField txtFontsize;
	private JTextField txtLineWidth;
	
	private TrainType selectedTrainType;
	private int selectedTrainTypeIndex;
	private JPanel panelTrainTypeEdit;
	private JComboBox<String> cbLineStyle;
	private JTextField txtDashstroke;

	/**
	 * Create the panel.
	 */
	public TrainTypesView(TrainGraph trainGraph) {
		setModel(trainGraph);
		_mainFrame = MainFrame.getInstance();
		
		initUI();
		initTable();
		
		setupUIDataBinding();
		
		ui_inited = true;
	}

	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		selectedTrainType = trainGraph.getDefaultTrainType();
		
		if (ui_inited) {
			tableModel.trainGraph = trainGraph;
			tableModel.fireTableDataChanged();
			
			uiBindingManager.setModel(this::getModelObject, null);
		}
	}

	// {{ Init UI
	private synchronized void initUI() {
		allDataBindingComponent.clear();
		
		JLabel lblTrainTypes = new JLabel("Train Types");
		lblTrainTypes.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		scrollPane = new JScrollPane();
		
		panelTrainTypeEdit = new JPanel();
		panelTrainTypeEdit.setVisible(false);
		panelTrainTypeEdit.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JPanel panelListActions = new JPanel();
		panelListActions.setBorder(null);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 591, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(panelTrainTypeEdit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblTrainTypes)
							.addGap(18)
							.addComponent(panelListActions, GroupLayout.PREFERRED_SIZE, 757, GroupLayout.PREFERRED_SIZE)))
					.addGap(24))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblTrainTypes))
						.addComponent(panelListActions, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(panelTrainTypeEdit, GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))
					.addContainerGap())
		);
		panelTrainTypeEdit.setLayout(null);
		
		JLabel lblTextProperties = new JLabel("Text Properties");
		lblTextProperties.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblTextProperties.setBounds(6, 6, 95, 16);
		panelTrainTypeEdit.add(lblTextProperties);
		
		JLabel lblTextColor = new JLabel("Text Color");
		lblTextColor.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblTextColor.setBounds(16, 34, 66, 16);
		panelTrainTypeEdit.add(lblTextColor);
		
		JLabel lblFontFamily = new JLabel("Font Family");
		lblFontFamily.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblFontFamily.setBounds(16, 62, 73, 16);
		panelTrainTypeEdit.add(lblFontFamily);
		
		JLabel lblFontStyle = new JLabel("Font Style");
		lblFontStyle.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblFontStyle.setBounds(16, 90, 62, 16);
		panelTrainTypeEdit.add(lblFontStyle);
		
		JLabel lblFontSize = new JLabel("Font Size");
		lblFontSize.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblFontSize.setBounds(16, 118, 57, 16);
		panelTrainTypeEdit.add(lblFontSize);
		
		JComboBox<String> cbFontStyle = createJComboBox("trainType.fontStyle:FontStyle", 
				new Font("Dialog", Font.PLAIN, 12), 
				new DefaultComboBoxModel<String>(FontStyleConverter.FONT_STYLES), 0);
		cbFontStyle.setFont(new Font("Dialog", Font.PLAIN, 12));
		cbFontStyle.setBounds(90, 86, 98, 27);
		panelTrainTypeEdit.add(cbFontStyle);
		
		JLabel lblLineProperties = new JLabel("Line Properties");
		lblLineProperties.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblLineProperties.setBounds(6, 192, 93, 16);
		panelTrainTypeEdit.add(lblLineProperties);
		
		txtFontsize = createJTextField(new Font("Lucida Grande", Font.PLAIN, 12), 10, "trainType.fontSize");
		txtFontsize.setBounds(91, 113, 95, 28);
		panelTrainTypeEdit.add(txtFontsize);
		
		JLabel lblLineStyle = new JLabel("Line Style");
		lblLineStyle.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblLineStyle.setBounds(16, 277, 60, 16);
		panelTrainTypeEdit.add(lblLineStyle);
		
		JLabel lblLineWidth = new JLabel("Line Width");
		lblLineWidth.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblLineWidth.setBounds(16, 248, 66, 16);
		panelTrainTypeEdit.add(lblLineWidth);
		
		cbLineStyle = createJComboBox("trainType.lineStyle:LineStyle", 
				new Font("Dialog", Font.PLAIN, 12), 
				new DefaultComboBoxModel<String>(LineStyleConverter.LINE_STYLE_DESCS), 0);
		cbLineStyle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
				} else if (e.getStateChange() == ItemEvent.SELECTED) {
					do_ChangeLineStyle();
				}
			}
		});
		cbLineStyle.setFont(new Font("Dialog", Font.PLAIN, 12));
		cbLineStyle.setBounds(90, 272, 98, 27);
		panelTrainTypeEdit.add(cbLineStyle);
		
		txtLineWidth = TrainTypesView.createJTextField(new Font("Lucida Grande", Font.PLAIN, 12), 10, "trainType.lineWidth");
		txtLineWidth.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtLineWidth.setBounds(91, 242, 95, 28);
		panelTrainTypeEdit.add(txtLineWidth);
		
		JLabel lblLineColor = new JLabel("Line Color");
		lblLineColor.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblLineColor.setBounds(16, 220, 64, 16);
		panelTrainTypeEdit.add(lblLineColor);
		
		JLabel lblTraintypefontcolor = createJColorChooserLabel("trainType.fontColor", new LineBorder(new Color(0, 0, 0)));
		lblTraintypefontcolor.setBounds(94, 30, 88, 24);
		panelTrainTypeEdit.add(lblTraintypefontcolor);
		
		JLabel label = TrainTypesView.createJColorChooserLabel("trainType.lineColor", new LineBorder(new Color(0, 0, 0)));
		label.setBorder(new LineBorder(new Color(0, 0, 0)));
		label.setBounds(94, 216, 88, 24);
		panelTrainTypeEdit.add(label);
		
		JComboBox<String> comboBox_1 = createJComboBox("trainType.fontFamilyName", 
				new Font("Dialog", Font.PLAIN, 12), 
				new DefaultComboBoxModel<String>(FontUtil.getFontFamilyNames()), 0);
		comboBox_1.setMaximumRowCount(20);
		comboBox_1.setBounds(90, 58, 151, 27);
		panelTrainTypeEdit.add(comboBox_1);
		
		JLabel lblDashStroke = new JLabel("Dash Stroke");
		lblDashStroke.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		lblDashStroke.setBounds(16, 305, 69, 15);
		panelTrainTypeEdit.add(lblDashStroke);
		
		txtDashstroke = TrainTypesView.createJTextField(new Font("Lucida Grande", Font.PLAIN, 12), 10, "trainType.dashStroke:DashStroke");
		txtDashstroke.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		txtDashstroke.setBounds(91, 298, 95, 28);
		panelTrainTypeEdit.add(txtDashstroke);
		panelListActions.setLayout(null);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_CreateTrainType();
			}
		});
		btnCreate.setBounds(6, 0, 99, 29);
		panelListActions.add(btnCreate);
		
		JButton btnMoveUp = new JButton("Move Up");
		btnMoveUp.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		btnMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_MoveUp();
			}
		});
		btnMoveUp.setBounds(204, 0, 99, 29);
		panelListActions.add(btnMoveUp);
		
		JButton btnMoveDown = new JButton("Move Down");
		btnMoveDown.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		btnMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_MoveDown();
			}
		});
		btnMoveDown.setBounds(302, 0, 99, 29);
		panelListActions.add(btnMoveDown);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_RemoveTrainTeyp();
			}
		});
		btnRemove.setBounds(106, 0, 99, 29);
		panelListActions.add(btnRemove);
		
		JButton btnUpdateTrains = new JButton("Update Trains");
		btnUpdateTrains.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_UpdateTypeOfTrains();
			}
		});
		btnUpdateTrains.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		btnUpdateTrains.setBounds(399, 0, 112, 29);
		panelListActions.add(btnUpdateTrains);
		setLayout(groupLayout);
	}

	private void initTable() {
		table = new JEditTable(__("train types table"));
		tableModel = new TrainTypeTableModel(table);
		tableModel.trainGraph = this.trainGraph;
		scrollPane.setViewportView(table);

		// 设置列宽
		table.getColumnModel().getColumn(0).setPreferredWidth(20);
		table.getColumnModel().getColumn(1).setPreferredWidth(320);
		table.getColumnModel().getColumn(2).setPreferredWidth(120);
		table.getColumnModel().getColumn(3).setPreferredWidth(60);
		table.getColumnModel().getColumn(4).setPreferredWidth(180);
		
		table.setRowHeight(30);
		
		// 表格列不可移动
		table.getTableHeader().setReorderingAllowed(false);
		
		// 表头文本居中
		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
			.setHorizontalAlignment(SwingConstants.CENTER);
		
		// 字符串列Cells文本居中
		DefaultTableCellRenderer defaultCellRenderer = new DefaultTableCellRenderer();
		defaultCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.setDefaultRenderer(String.class, defaultCellRenderer);
		
		// 第三列 缩写栏
		TrainTypeTableCellRenderer col3CellRenderer = new TrainTypeTableCellRenderer(3);
		table.getColumnModel().getColumn(3).setCellRenderer(col3CellRenderer);
		
		// 第四列 图例栏
		TrainTypeTableCellRenderer col4CellRenderer = new TrainTypeTableCellRenderer(4);
		table.getColumnModel().getColumn(4).setCellRenderer(col4CellRenderer);
		
		// 设置选择变更时的行为
		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						if (arg0.getValueIsAdjusting()) {
							do_ChangeSelectedTrainType();
						}
					}
				});
	}

	// }}
	
	// {{ Event handlers for Buttons on top

	protected void do_CreateTrainType() {
		int selectedIndex = trainGraph.trainTypeCount();

		TrainType trainType = TrainGraphFactory.createInstance(TrainType.class);
		
		ActionFactory.createAddTableElementActionAndDoIt(__("train types table"), 
				table, true, selectedIndex, trainType, trainGraph::addTrainType,
				trainGraph::removeTrainTypeAt, 
				() -> {
					table.revalidate();
					
					_mainFrame.navigator.updateNavigatorByTrainTypes();
				});
	}

	protected void do_RemoveTrainTeyp() {
		int index = table.getSelectedRow();
		if (index < 0 || index >= trainGraph.trainTypeCount() )
			return;
		if (index == 0 && trainGraph.trainTypeCount() == 1) {
			new MessageBox(__("Cannot remove the last train typee.")).showMessage();
			return;
		}

		ActionFactory.createRemoveTableElementActionAndDoIt(__("train types table"), 
				table, true, new int[] {index}, 
				trainGraph::getTrainType,
				trainGraph::addTrainType, 
				trainGraph::removeTrainTypeAt, 
				() -> {
					table.revalidate();
					
					_mainFrame.navigator.updateNavigatorByTrainTypes();
				});
	}

	protected void do_MoveDown() {
		// Move down a timetable
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex < 0 || selectedIndex >= trainGraph.trainTypeCount())
			return;
		if (selectedIndex == trainGraph.trainTypeCount() - 1) {
			new MessageBox(
					__("This is already the last timetable and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		
		ActionFactory.createTableElementMoveActionAndDoIt(__("train types table"), 
				table, trainGraph.allTrainTypes(), 
				selectedIndex, selectedIndex + 1, true,
				_mainFrame.navigator::updateNavigatorByTrainTypes);
	}

	protected void do_MoveUp() {
		// Move down a timetable
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex < 0 || selectedIndex >= trainGraph.trainTypeCount())
			return;
		if (selectedIndex == 0) {
			new MessageBox(
					__("This is already the first timetable and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		
		ActionFactory.createTableElementMoveActionAndDoIt(__("train types table"), 
				table, trainGraph.allTrainTypes(), 
				selectedIndex, selectedIndex - 1, true,
				_mainFrame.navigator::updateNavigatorByTrainTypes);
	}

	protected void do_UpdateTypeOfTrains() {
		trainGraph.setTrainTypeByNameForAllTrains();
	}
	
	// }}

	// {{ Event handlers for train type table and editor
		
	protected void do_ChangeSelectedTrainType() {
		int rowIndex = table.getSelectedRow();
		selectedTrainTypeIndex = rowIndex;
		DEBUG_MSG("Train type selected row changed to " + rowIndex);
		
		if (rowIndex < 0 || rowIndex >= tableModel.getRowCount() ||
				(selectedTrainType = tableModel.getTrainTypeAtRow(rowIndex)) == null) {
			selectedTrainType = trainGraph.getDefaultTrainType();
			panelTrainTypeEdit.setVisible(false);
			return;
		}
		
		panelTrainTypeEdit.setVisible(true);
		
		uiBindingManager.updateUI(null);
	}

	protected void do_ChangeLineStyle() {
		if (cbLineStyle.getSelectedIndex() == 4) {
			// Custom line style is selected
			txtDashstroke.setEnabled(true);
		} else {
			txtDashstroke.setEnabled(false);
		}
	}
	
	// }}
	
	// {{ Property getters and setters for selected train type
	
	public Color getFontColor() {
		return selectedTrainType.fontColor;
	}
	
	public void setFontColor(Color color) {
		if (selectedTrainType != null)
			selectedTrainType.fontColor = color;
	}
	
	public String getFontFamilyName() {
		return selectedTrainType.fontFamily;
	}
	
	public void setFontFamilyName(String fontFamily) {
		if (selectedTrainType != null)
			selectedTrainType.fontFamily = fontFamily;
	}
	
	public Integer getFontStyle() {
		return selectedTrainType.fontStyle;
	}
	
	public void setFontStyle(Integer fontStyle) {
		if (selectedTrainType != null)
			selectedTrainType.fontStyle = fontStyle;
	}
	
	public Integer getFontSize() {
		return selectedTrainType.fontSize;
	}
	
	public void setFontSize(Integer fontSize) {
		if (selectedTrainType != null)
			selectedTrainType.fontSize = fontSize;
	}
	
	public Color getLineColor() {
		return selectedTrainType.getColor();
	}

	public void setLineColor(Color color) {
		if (selectedTrainType != null)
			selectedTrainType.setColor(color);
	}

	public float getLineWidth() {
		return selectedTrainType.getLineWidth();
	}

	public void setLineWidth(float lineWidth) {
		if (selectedTrainType != null)
			selectedTrainType.setLineWidth(lineWidth);
	}
	
	public String getLineStyle() {
		return selectedTrainType.getLineStyle();
	}

	public void setLineStyle(String lineStype) {
		if (selectedTrainType != null)
			selectedTrainType.setLineStyle(lineStype);
	}
	
	// }}

	
	// {{ Data Bindings
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source name "trainType.fontFamily"
	 * @wbp.factory.parameter.source font new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12)
	 * @wbp.factory.parameter.source model new javax.swing.DefaultComboBoxModel(new java.lang.String[] {"Dialog Font"})
	 * @wbp.factory.parameter.source selectedIndex 0
	 */
	public static <E> JComboBox<E> createJComboBox(String name, Font font, ComboBoxModel<E> model, int selectedIndex) {
		JComboBox<E> comboBox = new JComboBox<E>();
		comboBox.setName(name);
		comboBox.setFont(font);
		comboBox.setModel(model);
		comboBox.setSelectedIndex(selectedIndex);
		
		allDataBindingComponent.add(comboBox);
		
		return comboBox;
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source font new java.awt.Font("Lucida Grande", java.awt.Font.PLAIN, 12)
	 * @wbp.factory.parameter.source columns 10
	 * @wbp.factory.parameter.source name "trainType.fontSize"
	 */
	public static JTextField createJTextField(Font font, int columns, String name) {
		JTextField textField = new JTextField();
		textField.setFont(font);
		textField.setColumns(columns);
		textField.setName(name);
		
		allDataBindingComponent.add(textField);
		
		return textField;
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source name "trainType.fontColor"
	 * @wbp.factory.parameter.source border new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0))
	 */
	public static JLabel createJColorChooserLabel(String name, Border border) {
		JLabel label = new JColorChooserLabel("");
		label.setName(name);
		label.setBorder(border);
		
		allDataBindingComponent.add(label);
		
		return label;
	}

	private void setupUIDataBinding() {
		ValueConverterManager.registerConvert(new LineStyleConverter());
		
		for (Component component : allDataBindingComponent) {
			uiBindingManager.addDataBinding(component, this::getModelObject, 
					this::getPropertyDesc, this::updateUIforModel);
		}
		
		uiBindingManager.updateUI(null);
	}

	private void updateUIforModel(String propertyGroup) {
		if ("trainType".equals(propertyGroup)) {
			tableModel.fireTableRowsUpdated(selectedTrainTypeIndex, selectedTrainTypeIndex);
			MainFrame.instance.navigator.updateNavigatorByTrainTypes();
		}
	}
	
	public Object getModelObject(String propertyGroup) {
		if ("trainType".equals(propertyGroup)) {
			return this;
		}
		
		return null;
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
	
	// }}
}
