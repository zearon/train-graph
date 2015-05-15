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

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;

import org.paradise.etrc.data.v1.TrainGraph;

public class TrainTypesView extends JPanel {

	TrainGraph trainGraph;
	private boolean ui_inited;

	/**
	 * Create the panel.
	 */
	public TrainTypesView(TrainGraph trainGraph) {
		setModel(trainGraph);
		
		
		
		JLabel lblTrainTypes = new JLabel("Train Types");
		lblTrainTypes.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		JScrollPane scrollPane = new JScrollPane();
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 449, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(panel, GroupLayout.PREFERRED_SIZE, 214, GroupLayout.PREFERRED_SIZE)
								.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addComponent(lblTrainTypes))
					.addGap(25))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(lblTrainTypes)
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
							.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 82, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 453, GroupLayout.PREFERRED_SIZE))
						.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE))
					.addContainerGap())
		);
		panel_1.setLayout(null);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.setBounds(6, 18, 99, 29);
		panel_1.add(btnCreate);
		
		JButton btnMoveUp = new JButton("Move Up");
		btnMoveUp.setBounds(6, 47, 99, 29);
		panel_1.add(btnMoveUp);
		
		JButton btnMoveDown = new JButton("Move Down");
		btnMoveDown.setBounds(109, 47, 99, 29);
		panel_1.add(btnMoveDown);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.setBounds(109, 18, 99, 29);
		panel_1.add(btnRemove);
		setLayout(groupLayout);

		
		
		
		
		ui_inited = true;
	}

	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		if (ui_inited) {
			
		}
	}
}
