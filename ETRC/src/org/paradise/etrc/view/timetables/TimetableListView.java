package org.paradise.etrc.view.timetables;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.Font;

import javax.swing.SwingConstants;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.RailNetworkChart;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.util.ui.table.JEditTable;

import static org.paradise.etrc.ETRC.__;

public class TimetableListView extends JPanel {
	private JButton btnNewTimetable;
	private JButton btnRemoveTimetable;
	private JButton btnMoveUp;
	private JButton btnMoveDown;
	private JScrollPane scrollPane;
	private JEditTable table;
	private TimetableListTableModel tableModel;
	
	private TrainGraph trainGraph;
	private boolean ui_inited;
	
	MainFrame _mainFrame;

	/**
	 * Create the panel.
	 */
	public TimetableListView(TrainGraph trainGraph) {
		_mainFrame = MainFrame.getInstance();
		
		initUI();
		
		setModel(trainGraph);
		
		initTable();
		
		validate();
		
		ui_inited = true;
	}

	private void initUI() {
		scrollPane = new JScrollPane();
		
		btnNewTimetable = new JButton(__("New Timetable"));
		btnNewTimetable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_CreateTimetable();
			}
		});
		btnNewTimetable.setFont(new Font(__("Lucida Grande"), Font.PLAIN, 12));
		
		btnRemoveTimetable = TimetableListView.createButton(__("Remove Timetable"));
		btnRemoveTimetable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_RemoveTimetable();
			}
		});
		btnRemoveTimetable.setFont(new Font(__("Lucida Grande"), Font.PLAIN, 12));
		
		btnMoveUp = TimetableListView.createButton(__("Move Up"));
		btnMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_MoveUp();
			}
		});
		btnMoveUp.setFont(new Font(__("Lucida Grande"), Font.PLAIN, 12));
		
		btnMoveDown = TimetableListView.createButton(__("Move Down"));
		btnMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_MoveDown();
			}
		});
		btnMoveDown.setFont(new Font(__("Lucida Grande"), Font.PLAIN, 12));
		
		JLabel lblTimetables = new JLabel(__("Timetables"));
		lblTimetables.setFont(new Font(__("Lucida Grande"), Font.PLAIN, 12));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(20)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblTimetables)
							.addContainerGap())
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 338, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(btnNewTimetable, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
								.addComponent(btnRemoveTimetable, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnMoveUp, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
								.addComponent(btnMoveDown))
							.addGap(58))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(19)
					.addComponent(lblTimetables)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnNewTimetable)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createSequentialGroup()
								.addComponent(btnRemoveTimetable)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnMoveUp)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnMoveDown)))
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.linkSize(SwingConstants.VERTICAL, new Component[] {btnNewTimetable, btnRemoveTimetable, btnMoveUp, btnMoveDown});
		groupLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {btnNewTimetable, btnRemoveTimetable, btnMoveUp, btnMoveDown});
		setLayout(groupLayout);
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		if (ui_inited) {
			tableModel.trainGraph = trainGraph;
			
			tableModel.fireTableDataChanged();
		}
	}
	
	protected void initTable() {
		table = new JEditTable("timetable list");
		tableModel = new TimetableListTableModel(table);
		tableModel.trainGraph = this.trainGraph;
		
		scrollPane.getViewport().add(table);
	}
	
	protected void do_MoveDown() {
		// Move down a timetable
		int selectedStatonIndex = table.getSelectedRow();
		if (selectedStatonIndex == trainGraph.allCharts()
				.size() - 1) {
			new MessageBox(
					__("This is already the last timetable and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		
		ActionFactory.createTableElementMoveActionAndDoIt(__("timetable list"), 
				table, trainGraph.allCharts(), 
				selectedStatonIndex, selectedStatonIndex + 1, true,
				_mainFrame.navigator::updateNavigatorByTimetables);
	}

	protected void do_MoveUp() {
		// Move down a timetable
		int selectedStatonIndex = table.getSelectedRow();
		if (selectedStatonIndex == 0) {
			new MessageBox(
					__("This is already the first timetable and thus cannot be moved down any more."))
					.showMessage();
			return;
		}
		
		ActionFactory.createTableElementMoveActionAndDoIt(__("timetable list"), 
				table, trainGraph.allCharts(), 
				selectedStatonIndex, selectedStatonIndex - 1, true,
				_mainFrame.navigator::updateNavigatorByTimetables);
	}

	protected void do_CreateTimetable() {
		int selectedIndex = trainGraph.allCharts().size();

		RailNetworkChart chart = TrainGraphFactory.createInstance(RailNetworkChart.class);
		
		ActionFactory.createAddTableElementActionAndDoIt(__("timetable list"), 
				table, true, selectedIndex, chart, trainGraph.allCharts()::add,
				trainGraph.allCharts()::removeElementAt, 
				() -> {
					table.revalidate();
					
					_mainFrame.navigator.updateNavigatorByTimetables();
				});
	}

	protected void do_RemoveTimetable() {
		int index = table.getSelectedRow();
		if (index == 0 && trainGraph.allCharts().size() == 1) {
			new MessageBox(__("Cannot remove the last railroad line.")).showMessage();
			return;
		}

		ActionFactory.createRemoveTableElementActionAndDoIt(__("timetable list"), 
				table, true, new int[] {index}, 
				trainGraph.allCharts()::elementAt,
				trainGraph.allCharts()::add, 
				trainGraph.allCharts()::removeElementAt, 
				() -> {
					table.revalidate();
					
					_mainFrame.navigator.updateNavigatorByTimetables();
				});
	}

	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source text "New Timetable"
	 */
	public static JButton createButton(String text) {
		JButton button = new JButton(text);
		button.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		return button;
	}
}
