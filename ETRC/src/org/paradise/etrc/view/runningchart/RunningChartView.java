package org.paradise.etrc.view.runningchart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.dialog.DistSetDialog;
import org.paradise.etrc.dialog.FindTrainDialog;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.dialog.TimeSetDialog;
import org.paradise.etrc.view.IView;
import org.paradise.etrc.view.runningchart.chart.ChartView;
import org.paradise.etrc.view.runningchart.dynamic.DynamicView;
import org.paradise.etrc.view.runningchart.sheet.SheetView;

import com.zearon.util.interface_.IMultiInheritance;
import com.zearon.util.ui.widget.StatusBar;

import static org.paradise.etrc.ETRC.__;

import static com.zearon.util.debug.DebugUtil.DEBUG;

public class RunningChartView extends JPanel implements IView {

	private static final long	serialVersionUID	= -2666471321623572698L;

	TrainGraph trainGraph;
	Train activeTrain;
	
	private boolean ui_inited = false;
	private ChartView chartView;
	private DynamicView runView;
	private SheetView sheetView;

	// {{ UI component fields
	private static final int MAX_TRAIN_SELECT_HISTORY_RECORD = 12;
	private Vector<String> trainSelectHistory = new Vector<String>();
	
	private JToolBar mainToolBar;
	private JMenu editMenu;
	private StatusBar statusBar;
	
	private JSplitPane splitPaneV;
	private JSplitPane splitPaneH;
	private JToggleButton jtButtonDown;
	private JToggleButton jtButtonShowRun;
	private JToggleButton jtButtonUp;
	private JToggleButton jtButtonShowWatermark;
	private JToggleButton jtButtonAntiAliasing;
	private JComboBox<String> cbTrainSelectHistory;
	// }}

	// {{ UI status fields
	private boolean isShowRun = true;
	// }}
	
	/**
	 * Create the panel.
	 */
	public RunningChartView(TrainGraph trainGraph) {
		if (trainGraph != null)
			setModel(trainGraph);
		
		try {
			initUI();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		ui_inited = true;
	}
	
	// {{ init UI
	
	private void initUI() {
		chartView = new ChartView(trainGraph, this);
		runView = new DynamicView(trainGraph);
		sheetView = new SheetView(trainGraph, this);
		
		splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, chartView, sheetView);
    int dividerPosH = Toolkit.getDefaultToolkit().getScreenSize().width
    		- MainFrame.initialDividerLocation - 253;
		splitPaneH.setDividerLocation(dividerPosH);
		splitPaneH.setDividerSize(6);
		splitPaneH.setOneTouchExpandable(true);
		
		splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, runView, splitPaneH);
	    int dividerPosV = runView.getPreferredSize().height;
		splitPaneV.setDividerLocation(dividerPosV);
		splitPaneV.setDividerSize(6);
		splitPaneV.setOneTouchExpandable(true);
		
		add(splitPaneV, BorderLayout.CENTER);
		
		createMenues();
		
		createMainToolBar();
		add(mainToolBar, BorderLayout.NORTH);
		
		createStatusBar();
		add(statusBar, BorderLayout.SOUTH);
	}
	
	private void createMenues() {
		editMenu = getEditMenu();
		editMenu.setText(__("Timetable"));
		editMenu.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		
//		createMenuItem(__("Add a new train"), false, true,
//				KeyEvent.VK_B, 0, () -> insertNewTrain(false));
//		createMenuItem(__("Insert a new train"), false, true,
//				KeyEvent.VK_B, KeyEvent.ALT_DOWN_MASK, () -> insertNewTrain(true));
//		editMenu.addSeparator();
	}
	
	private void createStatusBar() {
		statusBar = getStatusBar();
	}

	private JToolBar createMainToolBar() {
		mainToolBar = getToolBar();
		// mainToolBar.setFloatable(false);
		
		//查找车次
		createToolBarButton(JButton.class, "findTrain.png", __("Find a Train"), this::doFindTrain);
		mainToolBar.addSeparator();

		//坐标设置
		createToolBarButton(JButton.class, "setupH.png", __("Timeline Settings"), this::doTimeSet);
		createToolBarButton(JButton.class, "setupV.png", __("Distance Bar Settings"), this::doDistSet);
		mainToolBar.addSeparator();
		
		//动态图是否开启
		jtButtonShowRun = createToolBarButton(JToggleButton.class, "show_run.png", __("Show Dynamic Chart"), this::changeShowRunState);
		mainToolBar.addSeparator();
		updateShowRunState();
		
		//上下行显示选择
		jtButtonDown = createToolBarButton(JToggleButton.class, "down.png", __("Display Up-going Trains"), chartView::changeShowDown);
		jtButtonUp = createToolBarButton(JToggleButton.class, "up.png", __("Display Down-going Trains"), chartView::changShownUp);
		jtButtonShowWatermark = createToolBarButton(JToggleButton.class, "showAsWatermark.png", 
				__("Show undisplayed trains as watermarks"), 
				()->{
					if (jtButtonShowWatermark.isSelected())
						chartView.underDrawingColor = ChartView.DEFAULT_UNDER_COLOR;
					else
						chartView.underDrawingColor = null;
					
					chartView.repaint();
				});
		
		//读配置文件设置上下行状态按钮
		chartView.showUpDownState = ChartView.SHOW_ALL;
		jtButtonDown.setSelected(true);
		jtButtonUp.setSelected(true);
		jtButtonShowWatermark.setSelected(true);
		
		//让ChartView右下角的上下行状态显示图标显示正确的内容
		chartView.updateUpDownDisplay();


		
		//抗锯齿
		mainToolBar.addSeparator();
		jtButtonAntiAliasing = createToolBarButton(JToggleButton.class, "AA.png", 
				__("Use anti-aliasing drawing"), "chartSettings.useAntiAliasing");
		mainToolBar.add(jtButtonAntiAliasing);
		
		//历史记录
		cbTrainSelectHistory = new JComboBox<String>(new DefaultComboBoxModel<String>(trainSelectHistory));
		cbTrainSelectHistory.setFont(new Font("Dialog", Font.PLAIN, 12));
		cbTrainSelectHistory.setMinimumSize(new Dimension(64, 20));
		cbTrainSelectHistory.setMaximumSize(new Dimension(64, 20));
		cbTrainSelectHistory.setEditable(true);
		cbTrainSelectHistory.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				//当用键盘输入的时候会触发两次Action
				//一次是comboBoxChanged，另一次是comboBoxEdited
				//我们只处理与下拉选择一样的那一次：comboBoxChanged
				if(!ae.getActionCommand().equalsIgnoreCase("comboBoxChanged"))
					return;
				
				String trainToFind = (String) cbTrainSelectHistory.getSelectedItem();

				if(trainToFind == null)
					return;

				if(trainToFind.trim().equalsIgnoreCase(""))
					return;
				
				if(!chartView.findAndMoveToTrain(trainToFind))
					new MessageBox(MainFrame.getInstance(), String.format(__("Cannot find train information: %s"), trainToFind)).showMessage();
			}
		});
		mainToolBar.addSeparator();
		mainToolBar.add(cbTrainSelectHistory);

		return mainToolBar;
	}
	
	// UI Data binding
	{
		IMI_initInterface();
	}
	@Override
	public IMultiInheritance IMI_getThisObject() {
		return this;
	}
	
	@Override
	public Object UIDB_getModelObject(String objID) {
		if ("chartSettings".equals(objID)) {
			return trainGraph.settings;
		} else {
			return null;
		}
	}
	
	@Override
	public String UIDB_getPropertyDesc(String propertyName) {
		return propertyName;
	}

	@Override
	public void UIDB_updateUIforModel(String objectID) {
		chartView.updateData();
	}
	
	// }}

	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		if (ui_inited) {
			chartView.setModel(trainGraph);
			runView.setModel(trainGraph);
			sheetView.setModel(trainGraph);
			
			chartView.updateData();
			chartView.resetSize();
	
			sheetView.updateData();
			sheetView.refresh();
			
			runView.refresh();
		}
	}
	
	public void switchLineChart() {
		chartView.switchLineChart();
		runView.setModel(trainGraph);
		sheetView.setModel(trainGraph);
	}
	
	public void refresh() {
		chartView.updateData();
		sheetView.updateData();
		runView.updateUI();
	}
	
	public void updateTrainTypeDisplayOrder() {
		chartView.updateTrainTypeDisplayOrder();
	}
	
	// {{ Model operations

	public void setActiveTrain(Train activeTrain) {
		this.activeTrain = activeTrain;
		String activeTrainName = activeTrain.getTrainName(trainGraph.currentLineChart.railroadLine);
		
		//_name不为空的时候，加入hitory
		if (!activeTrainName.equalsIgnoreCase("")) {
			//如果已经存在于history中则删除之，以保证新加入的位于第一个
			if(trainSelectHistory.contains(activeTrainName))
				trainSelectHistory.remove(activeTrainName);
			
			trainSelectHistory.add(0, activeTrainName);
			
			//超过最大历史记录数，则删除最老的
			if(trainSelectHistory.size() > MAX_TRAIN_SELECT_HISTORY_RECORD)
				for(int i=12; i<trainSelectHistory.size(); i++)
					trainSelectHistory.remove(i);
			
			cbTrainSelectHistory.setModel(new DefaultComboBoxModel<String>(trainSelectHistory));
		}
		
		//ADD For SheetView
		sheetView.selectTrain(activeTrain);
		runView.setActiveTrain(activeTrain);
	}

	public Train getActiveTrain() {
		return activeTrain;
	}

	public void updateUpDownDisplay(int showUpDownState) {
		switch (showUpDownState) {
		case ChartView.SHOW_ALL:
			jtButtonDown.setSelected(true);
			jtButtonUp.setSelected(true);
			break;
		case ChartView.SHOW_DOWN:
			jtButtonDown.setSelected(true);
			jtButtonUp.setSelected(false);
			break;
		case ChartView.SHOW_UP:
			jtButtonDown.setSelected(false);
			jtButtonUp.setSelected(true);
			break;
		case ChartView.SHOW_NONE:
			jtButtonDown.setSelected(false);
			jtButtonUp.setSelected(false);
			break;
		}
	}
	
	// }}
	
	// {{ Getters and Setters

	public ChartView getChartView() {
		return chartView;
	}

	public DynamicView getRunView() {
		return runView;
	}

	public SheetView getSheetView() {
		return sheetView;
	}
	
	//}}
	
	// {{ Tool bar event handlers
	
	/******************************************************
	 **	Tool Bar operations 
	 ******************************************************/		
	private void changeShowRunState() {
		isShowRun = isShowRun ? false : true;
		updateShowRunState();
	}
	
	private void updateShowRunState() {
		jtButtonShowRun.setSelected(isShowRun);
		runView.setRunState(isShowRun);
		runView.setVisible(isShowRun);
		if (isShowRun)
			splitPaneV.setDividerLocation(runView.getPreferredSize().height);
	}

	/**
	 * doFindTrain
	 */
	private void doFindTrain() {
		FindTrainDialog dlg = new FindTrainDialog(MainFrame.getInstance());
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(false);
		dlg.pack();
		dlg.setVisible(true);
	}

	/**
	 * doDistSet
	 */
	private void doDistSet() {
		DistSetDialog dlg = new DistSetDialog(MainFrame.getInstance(), trainGraph);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(false);
		dlg.pack();
		dlg.setVisible(true);
	}

	/**
	 * doTimeSet
	 */
	private void doTimeSet() {
		TimeSetDialog dlg = new TimeSetDialog(trainGraph.settings, MainFrame.getInstance());
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(false);
		dlg.pack();
		dlg.setVisible(true);
	}
	// }}
	
}
