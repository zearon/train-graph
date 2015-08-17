package org.paradise.etrc.view.network;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.filter.ImageFilter;
import org.paradise.etrc.util.config.Config;
import org.paradise.etrc.view.IView;

import com.zearon.util.interface_.IMultiInheritance;
import com.zearon.util.ui.map.MapPane;
import com.zearon.util.ui.widget.StatusBar;

import static org.paradise.etrc.ETRC.__;

import javax.swing.JToolBar;

import java.awt.Rectangle;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class RailNetworkEditorView extends JPanel implements IView {

	TrainGraph trainGraph;
	private boolean ui_inited;
	private MapPane mapPane;
	MainFrame mainFrame;
	private JToolBar toolBar;
	
	/**
	 * Create the panel.
	 */
	public RailNetworkEditorView(TrainGraph trainGraph) {
		setModel(trainGraph);
		mainFrame = MainFrame.getInstance();
		
		setLayout(new BorderLayout(0, 0));
		
		createToolBar();
		add(toolBar, BorderLayout.NORTH);
		
		mapPane = new MapPane(trainGraph);
		add(mapPane, BorderLayout.CENTER);
		
		JPanel leftPanel = new JPanel();
		add(leftPanel, BorderLayout.WEST);
		
		initUI();
		
		ui_inited=true;
	}
	
	private void createToolBar() {
		toolBar = getToolBar();
		toolBar.setFloatable(false);
		
		JButton btnLoadBackgroundMap = new JButton("Load Background Map");
		toolBar.add(btnLoadBackgroundMap);
		
		toolBar.addSeparator();
		
		JLabel lblNewLabel = new JLabel("  Railline:");
		toolBar.add(lblNewLabel);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setMaximumSize(new Dimension(150, 32767));
		toolBar.add(comboBox);
		
		JButton btnOpenGLWindow = new JButton("OpenGLWindow");
		btnOpenGLWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapPane.createGLWindow();
				
				System.out.println(mapPane.getBounds());
				System.gc();
			}
		});
		toolBar.add(btnOpenGLWindow);
		btnLoadBackgroundMap.addActionListener((e) -> {
			do_LoadMap();
		});
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		if (ui_inited) {
			mapPane.setModel(trainGraph);
		}
	}

	private void initUI() {
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
			File recentPath = new File(Config.getInstance().getLastMapPath());
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			System.out.println(f);
			

			ActionFactory.createDirectAction(__("Load a map as the background picture"), 
					() -> {
				try {
					trainGraph.map.loadFromFile(f);
					setModel(trainGraph);
					
					updateUI();
					
					Config.getInstance().setLastMapPath(chooser.getSelectedFile().getParentFile().getAbsolutePath());
				} catch (IOException ioe) {
					System.out.println("Loading map failed.");
					ioe.printStackTrace();
					new MessageBox(String.format(__("Load map failed. Please check the %s file."
							+ "\nReason:%s\nDetail:%s"), chooser.getSelectedFile(), ioe.getMessage(), 
							ioe.getCause() )).showMessage();
				}
			}).addToManagerAndDoIt();;
		}
	}

	@Override
	public JToolBar getToolBar() {
		if (trainGraph == null)
			return new JToolBar();
		else
			return getDefaultToolBar();
	}

	// {{ Data binding
	{
		IMI_initInterface();
	}
	@Override
	public IMultiInheritance IMI_getThisObject() {
		return this;
	}

	@Override
	public Object UIDB_getModelObject(String objectID) {
		return null;
	}

	@Override
	public String UIDB_getPropertyDesc(String propertyName) {
		return null;
	}

	@Override
	public void UIDB_updateUIforModel(String objectID) {
	}
	// }}
}
