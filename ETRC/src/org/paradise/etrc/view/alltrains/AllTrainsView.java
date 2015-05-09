package org.paradise.etrc.view.alltrains;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JSplitPane;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.TrainGraph;

public class AllTrainsView extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -935841188408833448L;
	
	
	TrainGraph trainGraph;
	public TrainListView trainListView;
	public TrainView trainView;
	
	/**
	 * Create the panel.
	 */
	public AllTrainsView() {

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void jbInit() {

		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane, BorderLayout.CENTER);

		trainView = new TrainView();
		trainListView = new TrainListView();
		trainListView.setTrainView(trainView);
		
		splitPane.setLeftComponent(trainListView);
		splitPane.setRightComponent(trainView);
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		trainListView.setModel(trainGraph);
	}

}
