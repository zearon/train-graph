package org.paradise.etrc.view.network;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JButton;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.filter.ImageFilter;
import org.paradise.etrc.filter.TRCFilter;
import org.paradise.etrc.util.Config;

import javax.swing.BoxLayout;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JScrollPane;

public class RailNetworkEditorView extends JPanel {

	TrainGraph trainGraph;
	private boolean ui_inited;
	private MapPanel mapPanel;
	MainFrame mainFrame;
	
	/**
	 * Create the panel.
	 */
	public RailNetworkEditorView(TrainGraph trainGraph) {
		setModel(trainGraph);
		mainFrame = MainFrame.getInstance();
		
		
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		mapPanel = new MapPanel(trainGraph);
		scrollPane.setViewportView(mapPanel);
		mapPanel.setLayout(null);
		
		JButton btnLoadMap = new JButton("Load Map");
		btnLoadMap.setBounds(6, 6, 117, 29);
		mapPanel.add(btnLoadMap);
		btnLoadMap.addActionListener((e) -> {
			do_LoadMap();
		});
		
		JPanel topPanel = new JPanel();
		add(topPanel, BorderLayout.NORTH);
		
		JPanel leftPanel = new JPanel();
		add(leftPanel, BorderLayout.WEST);
		initUI();
		
		ui_inited=true;
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		if (ui_inited) {
			mapPanel.setModel(trainGraph);
		}
	}

	private void initUI() {
//		Icon bgImage = new ImageIcon("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/map.jpg");
		
	}
	
	private void do_LoadMap() {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Load Map"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new ImageFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(Config.getLastMapPath());
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			System.out.println(f);
			

			ActionFactory.createDirectActionAndDoIt(__("Load a map as the backgroup picture"), 
					() -> {
				try {
					trainGraph.map.loadFromFile(f);
					setModel(trainGraph);
					
					updateUI();
					
					Config.setLastMapPath(chooser.getSelectedFile().getParentFile().getAbsolutePath());
				} catch (IOException ioe) {
					System.out.println("Loading map failed.");
					ioe.printStackTrace();
					new MessageBox(String.format(__("Load map failed. Please check the %s file."
							+ "\nReason:%s\nDetail:%s"), chooser.getSelectedFile(), ioe.getMessage(), 
							ioe.getCause() )).showMessage();
				}
			});
		}
	}

}
