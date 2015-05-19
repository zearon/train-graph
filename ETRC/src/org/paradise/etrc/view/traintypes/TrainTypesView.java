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
import org.paradise.etrc.util.ui.table.JEditTable;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TrainTypesView extends JPanel {

	TrainGraph trainGraph;
	private boolean ui_inited;
	private MainFrame _mainFrame;
	
	private JEditTable table;
	private TrainTypeTableModel tableModel;
	private JScrollPane scrollPane;

	/**
	 * Create the panel.
	 */
	public TrainTypesView(TrainGraph trainGraph) {
		setModel(trainGraph);
		_mainFrame = MainFrame.getInstance();
		
		initUI();
		initTable();
		ui_inited = true;
	}

	private void initUI() {
		JLabel lblTrainTypes = new JLabel("Train Types");
		lblTrainTypes.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		scrollPane = new JScrollPane();
		
		JPanel panelTrainTypeEdit = new JPanel();
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
							.addComponent(lblTrainTypes)
							.addGap(32)
							.addComponent(panelListActions, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 591, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panelTrainTypeEdit, GroupLayout.PREFERRED_SIZE, 214, GroupLayout.PREFERRED_SIZE)
					.addGap(57))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblTrainTypes))
						.addComponent(panelListActions, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(panelTrainTypeEdit, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
						.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))
					.addContainerGap())
		);
		panelListActions.setLayout(null);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_CreateTrainType();
			}
		});
		btnCreate.setBounds(6, 0, 99, 29);
		panelListActions.add(btnCreate);
		
		JButton btnMoveUp = new JButton("Move Up");
		btnMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_MoveUp();
			}
		});
		btnMoveUp.setBounds(204, 0, 99, 29);
		panelListActions.add(btnMoveUp);
		
		JButton btnMoveDown = new JButton("Move Down");
		btnMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_MoveDown();
			}
		});
		btnMoveDown.setBounds(302, 0, 99, 29);
		panelListActions.add(btnMoveDown);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_RemoveTrainTeyp();
			}
		});
		btnRemove.setBounds(106, 0, 99, 29);
		panelListActions.add(btnRemove);
		setLayout(groupLayout);
	}

	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		if (ui_inited) {
			tableModel.trainGraph = trainGraph;
			
			tableModel.fireTableDataChanged();
		}
	}

	private void initTable() {
		table = new JEditTable(__("train types table"));
		tableModel = new TrainTypeTableModel(table);
		tableModel.trainGraph = this.trainGraph;
		scrollPane.setViewportView(table);

		// 设置列宽
		table.getColumnModel().getColumn(0).setPreferredWidth(20);
		table.getColumnModel().getColumn(1).setPreferredWidth(160);
		table.getColumnModel().getColumn(2).setPreferredWidth(260);
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
	}

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
}
