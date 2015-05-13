package org.paradise.etrc;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.DEBUG_ACTION;
import static org.paradise.etrc.ETRCUtil.DEBUG_MSG;
import static org.paradise.etrc.ETRCUtil.DEBUG;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.Vector;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.paradise.etrc.controller.ActionManager;
import org.paradise.etrc.data.RailNetworkChart;
import org.paradise.etrc.data.RailroadLineChart;
import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.data.Train;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.skb.ETRCLCB;
import org.paradise.etrc.data.skb.ETRCSKB;
import org.paradise.etrc.dialog.AboutBox;
import org.paradise.etrc.dialog.CircuitMakeDialog;
import org.paradise.etrc.dialog.DistSetDialog;
import org.paradise.etrc.dialog.FindTrainDialog;
import org.paradise.etrc.dialog.FindTrainsDialog;
import org.paradise.etrc.dialog.MarginSetDialog;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.dialog.TimeSetDialog;
import org.paradise.etrc.dialog.XianluSelectDialog;
import org.paradise.etrc.dialog.YesNoBox;
import org.paradise.etrc.filter.CSVFilter;
import org.paradise.etrc.filter.GIFFilter;
import org.paradise.etrc.filter.TRCFilter;
import org.paradise.etrc.filter.TRFFilter;
import org.paradise.etrc.util.Config;
import org.paradise.etrc.util.ui.UIBinding;
import org.paradise.etrc.view.alltrains.AllTrainsView;
import org.paradise.etrc.view.alltrains.TrainListView;
import org.paradise.etrc.view.chart.ChartView;
import org.paradise.etrc.view.dynamic.DynamicView;
import org.paradise.etrc.view.lineedit.RailroadLineEditView;
import org.paradise.etrc.view.nav.Navigator;
import org.paradise.etrc.view.nav.Navigator.NavigatorNodeType;
import org.paradise.etrc.view.network.RailNetworkEditorView;
import org.paradise.etrc.view.settings.SettingsView;
import org.paradise.etrc.view.sheet.SheetView;
import org.paradise.etrc.view.timetables.TimetableListTableModel;
import org.paradise.etrc.view.timetables.TimetableListView;
import org.paradise.etrc.view.traintypes.TrainTypesView;

import com.sun.corba.se.spi.orbutil.fsm.Action;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class MainFrame extends JFrame implements ActionListener, Printable {
	private static final long serialVersionUID = 1L;
	
	public JSplitPane navigatorSplitPane;
	public Navigator navigator;
	public JPanel navigatorContentPanel;
	CardLayout navigatorContentCard;
	public JSplitPane splitPaneV;
	public JSplitPane splitPaneH;

	public JPanel raillineChartView;
	public ChartView chartView;
	public DynamicView runView;
	public SheetView sheetView;
	
	public SettingsView settingsView;
	public RailNetworkEditorView railNetworkEditorView;
	public RailroadLineEditView railLineEditView;
	public TrainTypesView trainTypesView;
	public AllTrainsView allTrainsView;
	public TimetableListView timetableListView;
	
//	public J
	
//	private RailroadLineEditView circuitEditDialog;
	
	private boolean isShowRun = true;
	
	public JLabel statusBarMain = new JLabel();
	public JLabel statusBarRight = new JLabel();
	
//	public boolean isNewCircuit = false;
//	private String workingFileName;
	
	public TrainGraph trainGraph;
	RailNetworkChart currentNetworkChart;
	public RailroadLineChart currentLineChart;

	private static final int MAX_TRAIN_SELECT_HISTORY_RECORD = 12;
	public Vector<String> trainSelectHistory;
	public JComboBox<String> cbTrainSelectHistory;
	
	private boolean firstTimeLoading = true;

	private boolean ui_inited;
	
	public static MainFrame instance;
	public static MainFrame getInstance() { return instance; }
	//Construct the frame
	public MainFrame() {
		if (instance == null)
			instance = this;
		
		addFullScreenModeSupportOnOSX();
		
		trainSelectHistory = new Vector<String>();
		cbTrainSelectHistory = new JComboBox<String>(new DefaultComboBoxModel<String>(trainSelectHistory));
				
		initChart();
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ActionManager.getInstance().setUpdateUIHook(actionMgr -> this.setTitle());
		
		UIBinding.setExceptionHandler(e -> {
			e.printStackTrace();
			new MessageBox(e.getMessage()).showMessage();
		});

		ui_inited = true;
		setModel(trainGraph);
		
		// for DEBUG purposes
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				if (!firstTimeLoading)
					return;
				
				firstTimeLoading = false;
				
				// test circuitEditDialog
				// DEBUG_ACTION(() -> circuitEditDialog.showDialog());
				
				// Set full screen mode
				DEBUG_ACTION( () -> {
					//setFullScreenModeOnOSX(); 
					} , "Set full screen mode to false");
			}
		});
		
	}
	
	public void setModel(TrainGraph trainGraph) {
		setTitle();
		
		currentNetworkChart = trainGraph.getCharts().get(0);
		currentLineChart = currentNetworkChart.getRailLineCharts().get(0);
		
		if (ui_inited) {
			// Set Navigator in terms of train Graph
			navigator.setTrainGraph(trainGraph);
			railNetworkEditorView.setModel(trainGraph);
			railLineEditView.setModel(trainGraph);
			trainTypesView.setModel(trainGraph);
			allTrainsView.setModel(trainGraph);
			timetableListView.setModel(trainGraph);
			
	
			chartView.updateData();
			chartView.resetSize();
	
			sheetView.updateData();
			sheetView.refresh();
			
			runView.refresh();
		}
	}
	
	static Boolean isOSX = null;
	public boolean isOSX10_7OrAbove() {
		if (isOSX != null) {
			return isOSX;
		}
		
		isOSX = false;
		if ("Mac OS X".equalsIgnoreCase(System.getProperty("os.name"))) {
			String osVersionString = System.getProperty("os.version");
			String[] versionParts = osVersionString.split("\\.");
			if (versionParts.length >= 2) {
				try {
					int versionPart1Val = Integer.parseInt(versionParts[0]);
					int versionPart2Val = Integer.parseInt(versionParts[1]);
					if (versionPart1Val >= 10 && versionPart2Val >= 7) {
						isOSX = true;
					}
				} finally {}
			}
		}
		
		return isOSX;
	}
	
	/**
	 * If OS is Mac OS X and Version >= 10.7, then add full screen support.
	 */
	public void addFullScreenModeSupportOnOSX() {
		DEBUG_MSG("OS Name=%s, OS Version=%s\n", System.getProperty("os.name"), System.getProperty("os.version"));
		if (!isOSX10_7OrAbove()) 
			return;
		
		// com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(this,true);
		
		// In case of the need to compile and run on other platforms,
		// replace the method invocation with reflection style to avoid
		// ClassNotFound Exceptions.
		try {
			Class<?> clz = Class.forName("com.apple.eawt.FullScreenUtilities");
			java.lang.reflect.Method method = clz.getMethod("setWindowCanFullScreen", java.awt.Window.class, boolean.class);
			method.invoke(null, this, true);
		} catch (Exception e) {
			System.err.println("Cannot add full screen support.");
			System.err.println(e);
		}
	}
	
	public void setFullScreenModeOnOSX() {
		if (!isOSX10_7OrAbove()) 
			return;
		
		// com.apple.eawt.Application.getApplication().requestToggleFullScreen(MainFrame.this);	
		
		// In case of the need to compile and run on other platforms,
		// replace the method invocation with reflection style to avoid
		// ClassNotFound Exceptions.
		try {
			Class applicationClz = Class.forName("com.apple.eawt.Application");
			java.lang.reflect.Method getApplicationMethod = 
					applicationClz.getMethod("getApplication");
			Object app = getApplicationMethod.invoke(null);
			
			java.lang.reflect.Method requestToggleFullScreenMethod = 
					applicationClz.getMethod("requestToggleFullScreen",java.awt.Window.class);
			requestToggleFullScreenMethod.invoke(app, MainFrame.this);
		} catch (Exception e) {
			System.err.println("Cannot add full screen support.");
			System.err.println(e);
		}
	}

	public void doExit() {
		if(Config.isFileModified())
			if(new YesNoBox(this, __("Current train graph has changed.\nDo you want to save the changes?")).askForYes())
				doSaveChart(); 
		
		System.exit(0);
	}

	//Component initialization
	private void jbInit() throws Exception {
		
		chartView = new ChartView(trainGraph, currentLineChart, this);
		runView = new DynamicView(trainGraph, currentLineChart, this);
		sheetView = new SheetView(trainGraph, currentLineChart, this);
		
		settingsView = new SettingsView(trainGraph);
		railNetworkEditorView = new RailNetworkEditorView(trainGraph);
		railLineEditView = new RailroadLineEditView(this);
		trainTypesView = new TrainTypesView(trainGraph);
		allTrainsView = new AllTrainsView();
		timetableListView = new TimetableListView(trainGraph);
		
//		tbPane = new JTabbedPane();
//		tbPane.setFont(new Font("Dialog", Font.PLAIN, 12));
//		tbPane.add("点单1", sheetView);
//		tbPane.add("动态图", runView);
//		tbPane.setMinimumSize(tbPane.getPreferredSize());
//		
//		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, tbPane, chartView);
//		splitPane.setDividerLocation(tbPane.getPreferredSize().height);
//		splitPane.setDividerSize(5);
		
		raillineChartView = new JPanel();
		raillineChartView.setLayout(new BorderLayout());
		

		navigatorContentCard = new CardLayout();
		navigatorContentPanel = new JPanel(navigatorContentCard);
		navigatorContentPanel.add(NavigatorNodeType.GLOBAL_SETTINGS.name(),
				settingsView);
		navigatorContentPanel.add(NavigatorNodeType.RAILROAD_NETWORK.name(),
				railNetworkEditorView);
		navigatorContentPanel.add(NavigatorNodeType.RAILROAD_LINES.name(), 
				railLineEditView);
		navigatorContentPanel.add(NavigatorNodeType.TRAIN_TYPES.name(), 
				trainTypesView);
		navigatorContentPanel.add(NavigatorNodeType.ALL_TRAINS.name(), 
				allTrainsView);
		navigatorContentPanel.add(NavigatorNodeType.TIME_TABLES.name(),
				timetableListView);
		navigatorContentPanel.add(NavigatorNodeType.TIME_TABLE_LINE.name(), 
				raillineChartView);
		// TODO: 设置主视图启动时的编辑视图
				
		navigator = new Navigator();
		navigator.nodeSelectionListener = this::onNavigatorNodeChanged;
		
		navigatorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, 
				new JScrollPane(navigator), navigatorContentPanel);
		int dividerPosH0 = 200;
		navigatorSplitPane.setDividerLocation(dividerPosH0);
		navigatorSplitPane.setDividerSize(6);
		navigatorSplitPane.setOneTouchExpandable(true);
		
		
		//mainPanel.add(runView, BorderLayout.NORTH);
		
		splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, chartView, sheetView);
	    int dividerPosH = Toolkit.getDefaultToolkit().getScreenSize().width
	    		- dividerPosH0 - 253;
		splitPaneH.setDividerLocation(dividerPosH);
		splitPaneH.setDividerSize(6);
		splitPaneH.setOneTouchExpandable(true);
		
		splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, runView, splitPaneH);
	    int dividerPosV = runView.getPreferredSize().height;
		splitPaneV.setDividerLocation(dividerPosV);
		splitPaneV.setDividerSize(6);
		splitPaneV.setOneTouchExpandable(true);
		
		raillineChartView.add(splitPaneV, BorderLayout.CENTER);

		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setFont(new java.awt.Font(__("FONT_NAME"), 0, 10));
		this.setLocale(java.util.Locale.getDefault());
		this.setResizable(true);
		this.setState(Frame.NORMAL);
		this.setIconImage(new ImageIcon(org.paradise.etrc.MainFrame.class
				.getResource("/pic/icon.gif")).getImage());
		
		JPanel contentPane;
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(null);
		contentPane.setDebugGraphicsOptions(0);

		this.setJMenuBar(loadMenu());

		statusBarMain = loadStatusBar();
		statusBarRight = loadStatusBar();
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(statusBarMain, BorderLayout.CENTER);
		statusPanel.add(statusBarRight, BorderLayout.EAST);

		contentPane.add(statusPanel, BorderLayout.SOUTH);
		contentPane.add(loadToolBar(), BorderLayout.NORTH);
		contentPane.add(navigatorSplitPane, BorderLayout.CENTER);

		this.setTitle();
	}
/*
	private void jbInit() throws Exception {
		chartView = new ChartView(this);
		runView = new DynamicView(this);
		sheetView = new SheetView(this);
		
		JTabbedPane tbPane = new JTabbedPane();
		tbPane.setFont(new Font("Dialog", Font.PLAIN, 12));
		tbPane.add("点单", sheetView);
		tbPane.add("运行图", chartView);
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, runView, tbPane);
		splitPane.setDividerLocation(runView.getPreferredSize().height);
		splitPane.setDividerSize(3);
		runView.setMinimumSize(runView.getPreferredSize());

		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setFont(new java.awt.Font(_("FONT_NAME"), 0, 10));
		this.setLocale(java.util.Locale.getDefault());
		this.setResizable(true);
		this.setState(Frame.NORMAL);
		this.setIconImage(new ImageIcon(org.paradise.etrc.MainFrame.class
				.getResource("/pic/icon.gif")).getImage());
		
		JPanel contentPane;
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(null);
		contentPane.setDebugGraphicsOptions(0);

		this.setJMenuBar(loadMenu());

		statusBarMain = loadStatusBar();
		statusBarRight = loadStatusBar();
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(statusBarMain, BorderLayout.CENTER);
		statusPanel.add(statusBarRight, BorderLayout.EAST);

		contentPane.add(statusPanel, BorderLayout.SOUTH);
		contentPane.add(loadToolBar(), BorderLayout.NORTH);
		contentPane.add(splitPane, BorderLayout.CENTER);

		this.setTitle();
	}
*/
    private static final String titlePrefix = __("Jeff's Electronic Train Graph");

	private String activeTrainName = "";

	public void setActiceTrainName(String _name) {
		if (_name.equalsIgnoreCase(""))
			activeTrainName = "";
		else
			activeTrainName = " (" + _name + ") ";
		
		//_name不为空的时候，加入hitory
		if (!_name.equalsIgnoreCase("")) {
			//如果已经存在于history中则删除之，以保证新加入的位于第一个
			if(trainSelectHistory.contains(_name))
				trainSelectHistory.remove(_name);
			
			trainSelectHistory.add(0, _name);
			
			//超过最大历史记录数，则删除最老的
			if(trainSelectHistory.size() > MAX_TRAIN_SELECT_HISTORY_RECORD)
				for(int i=12; i<trainSelectHistory.size(); i++)
					trainSelectHistory.remove(i);
			
			cbTrainSelectHistory.setModel(new DefaultComboBoxModel<String>(trainSelectHistory));
		}

		setTitle();
	}

	public void setTitle() {
		setTitle(titlePrefix + " -- [" + Config.getCurrentFile()+ "] " + 
				(Config.isFileModified() ? __("Modified ") : "") + activeTrainName);
	}

	private JLabel loadStatusBar() {
		JLabel statusBar = new JLabel();
		Border border = BorderFactory.createLoweredBevelBorder();

		statusBar.setBorder(border);
		statusBar.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		statusBar.setText(__("Status: Normal"));
		return statusBar;
	}

	public JToggleButton jtButtonDown;
	public JToggleButton jtButtonShowRun;
	public JToggleButton jtButtonUp;
	public JToggleButton jtButtonShowWatermark;

	private JToolBar loadToolBar() {
		JToolBar jToolBar = new JToolBar();
		
		//文件操作
		JButton jbOpenFile = createTBButton("openFile", __("Open a Chart"), File_Load_Chart);
		JButton jbSaveFile = createTBButton("saveFile", __("Save Chart"), File_Save_Chart);
		JButton jbSaveAs   = createTBButton("saveAs", __("Save Chart As"), File_Save_Chart_As);
		jToolBar.add(jbOpenFile);
		jToolBar.add(jbSaveFile);
		jToolBar.add(jbSaveAs);
		
		//撤销,重做
		JButton jbUndo = createTBButton("undo", __("Undo"), Edit_Undo);
		JButton jbRedo  = createTBButton("redo", __("Redo"), Edit_Redo);
		ActionManager.getInstance().setToolbarButton(jbUndo, jbRedo);
		jToolBar.addSeparator();
		jToolBar.add(jbUndo);
		jToolBar.add(jbRedo);
		
		//车次、线路编辑
		JButton jbCircuitEdit = createTBButton("circuit", __("Edit Circuit"), Edit_Circuit);
		JButton jbTrainsEdit  = createTBButton("trains", __("Edit Train Information"), Edit_Trains);
		jToolBar.addSeparator();
		jToolBar.add(jbCircuitEdit);
		jToolBar.add(jbTrainsEdit);
		
		//查找车次
		JButton jbFindTrain = createTBButton("findTrain", __("Find a Train"), Edit_FindTrain);
		jToolBar.addSeparator();
		jToolBar.add(jbFindTrain);

		//坐标设置
		JButton jbSetupH = createTBButton("setupH", __("Timeline Settings"), Setup_Time);
		JButton jbSetupV = createTBButton("setupV", __("Distance Bar Settings"), Setup_Dist);
		jToolBar.addSeparator();
		jToolBar.add(jbSetupH);
		jToolBar.add(jbSetupV);
		
		//动态图是否开启
		ImageIcon imageRun = new ImageIcon(this.getClass().getResource("/pic/show_run.png"));
		jtButtonShowRun = new JToggleButton(imageRun);
		jtButtonShowRun.setToolTipText(__("Show Dynamic Chart"));
		jtButtonShowRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.changeShowRunState();
			}
		});
		
		//读配置文件设定是否显示动态图
//		if(prop.getProperty(Prop_Show_Run).equalsIgnoreCase("Y")) {
//			isShowRun = true;
//		}
//		else {
//			isShowRun = false;
//		}
		updateShowRunState();
		
		jToolBar.addSeparator();
		jToolBar.add(jtButtonShowRun);
		
		//上下行显示选择
		ImageIcon imageDown = new ImageIcon(this.getClass().getResource("/pic/down.png"));
		ImageIcon imageUp = new ImageIcon(this.getClass().getResource("/pic/up.png"));
		ImageIcon imageWatermark = new ImageIcon(this.getClass().getResource("/pic/showAsWatermark.png"));
		jtButtonDown = new JToggleButton(imageDown);
		jtButtonUp = new JToggleButton(imageUp);
		jtButtonShowWatermark = new JToggleButton(imageWatermark);
		jtButtonUp.setToolTipText(__("Display Up-going Trains"));
		jtButtonDown.setToolTipText(__("Display Down-going Trains"));
		jtButtonShowWatermark.setToolTipText(__("Show undisplayed trains as watermarks"));
		jtButtonDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chartView.changeShowDown();
			}
		});
		jtButtonUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chartView.changShownUp();
			}
		});
		jtButtonShowWatermark.addActionListener(e->{
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
		
//		chartView.showUpDownState = ChartView.SHOW_NONE;
//		if(prop.getProperty(Prop_Show_Down).equalsIgnoreCase("Y")) {
//			jtButtonDown.setSelected(true);
//			chartView.showUpDownState ^= ChartView.SHOW_DOWN;
//		}
//		else
//			jtButtonDown.setSelected(false);
//		
//		if(prop.getProperty(Prop_Show_UP).equalsIgnoreCase("Y")) {
//			jtButtonUp.setSelected(true);
//			chartView.showUpDownState ^= ChartView.SHOW_UP;
//		}
//		else
//			jtButtonUp.setSelected(false);
		
		//让ChartView右下角的上下行状态显示图标显示正确的内容
		chartView.updateUpDownDisplay();

		jToolBar.addSeparator();
		jToolBar.add(jtButtonDown);
		jToolBar.add(jtButtonUp);
		jToolBar.add(jtButtonShowWatermark);
		
		//历史记录
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
					new MessageBox(MainFrame.this, String.format(__("Cannot find train information: %s"), trainToFind)).showMessage();
			}
		});
		jToolBar.addSeparator();
		jToolBar.add(cbTrainSelectHistory);

		return jToolBar;
	}
	
	private JButton createTBButton(String imgName, String toolTipText, String Command) {
		JButton jbOnToolBar = new JButton();

		ImageIcon  imageOpenFile = new ImageIcon(org.paradise.etrc.MainFrame.class
				.getResource("/pic/" + imgName + ".png"));

		jbOnToolBar.setIcon(imageOpenFile);
		jbOnToolBar.setToolTipText(toolTipText);
		jbOnToolBar.addActionListener(this);
		jbOnToolBar.setActionCommand(Command);
		
		return jbOnToolBar;
	}

	private final String File_Load_Chart = "File_Load_Chart";
	private final String File_Save_Chart = "File_Save_Chart";
	private final String File_Save_Chart_As = "File_Save_Chart_As";
	private final String File_New_Chart = "File_Clear_Chart";
//	private final String File_Circuit = "File_Circuit";
	private final String File_Load_Map = "File_Load_Map";
	private final String File_Train = "File_Train";
	private final String File_Export = "File_Export";
	private final String File_Exit = "File_Exit";

	private final String Edit_Undo = "Edit_Undo";
	private final String Edit_Redo = "Edit_Redo";
	private final String Edit_Action_History = "Edit_Action_History";
	private final String Edit_FindTrain = "Edit_FindTrain";
	private final String Edit_Circuit = "Edit_Circuit";
	private final String Edit_Trains = "Edit_Trains";

	private final String Setup_Margin = "Setup_MarginSet";
	private final String Setup_Time = "Setup_TimeSet";
	private final String Setup_Dist = "Setup_DistSet";

	private final String Tools_Circuit = "Tools_Circuit";
	private final String Tools_Train = "Tools_Train";
	
	private final String Help_About = "Help_About";

	private JMenuBar loadMenu() {
		JMenuBar jMenuBar = new JMenuBar();

		JMenu menuFile = createMenu(__("File"));
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuFile.add(createMenuItem(__("New"), File_New_Chart)).setMnemonic(KeyEvent.VK_N);
		menuFile.add(createMenuItem(__("Open..."), File_Load_Chart)).setMnemonic(KeyEvent.VK_O);
		menuFile.addSeparator();
		menuFile.add(createMenuItem(__("Save"), File_Save_Chart)).setMnemonic(KeyEvent.VK_S);
		menuFile.add(createMenuItem(__("Save As..."), File_Save_Chart_As)).setMnemonic(KeyEvent.VK_A);
		menuFile.addSeparator();
//		menuFile.add(createMenuItem("更改线路...", File_Circuit)); //Bug:更改线路后没有清空车次
		menuFile.add(createMenuItem(__("Load Train..."), File_Train)).setMnemonic(KeyEvent.VK_L);
		menuFile.addSeparator();
		menuFile.add(createMenuItem(__("Export..."), File_Export)).setMnemonic(KeyEvent.VK_P);
		menuFile.addSeparator();
		menuFile.add(createMenuItem(__("Exit"), File_Exit)).setMnemonic(KeyEvent.VK_X);

		JMenu menuSetup = createMenu(__("Settings"));
		menuSetup.setMnemonic(KeyEvent.VK_S);
		menuSetup.add(createMenuItem(__("Margin..."), Setup_Margin)).setMnemonic(KeyEvent.VK_M);
		menuSetup.addSeparator();
		menuSetup.add(createMenuItem(__("Timeline..."), Setup_Time)).setMnemonic(KeyEvent.VK_T);
		menuSetup.add(createMenuItem(__("Distance Bar..."), Setup_Dist)).setMnemonic(KeyEvent.VK_D);

        JMenu menuEdit = createMenu(__("EditMenu"));
		menuEdit.setMnemonic(KeyEvent.VK_E);
		JMenuItem undoMenuItem = menuEdit.add(createMenuItem(__("Undo"), Edit_Undo));
		JMenuItem redoMenuItem = menuEdit.add(createMenuItem(__("Redo"), Edit_Redo));
		if (isOSX10_7OrAbove()) {
			undoMenuItem.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK));
			redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
	    				InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		} else {
			undoMenuItem.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
			redoMenuItem.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
		}
		JMenu actionHistoryMenuItem = createMenu(__("Action History"));
		menuEdit.add(actionHistoryMenuItem);
		ActionManager.getInstance().setMenuItem(undoMenuItem, redoMenuItem, actionHistoryMenuItem);
		
		menuEdit.addSeparator();
        menuEdit.add(createMenuItem(__("Circuit..."), Edit_Circuit)).setMnemonic(KeyEvent.VK_C);
        menuEdit.add(createMenuItem(__("Train..."), Edit_Trains)).setMnemonic(KeyEvent.VK_R);
		menuEdit.addSeparator();
//		menuEdit.add(createMenuItem("车次录入...", Edit_NewTrain));
        menuEdit.add(createMenuItem(__("Find Train..."), Edit_FindTrain)).setMnemonic(KeyEvent.VK_F);
//		menuEdit.addSeparator();
//		menuEdit.add(createMenuItem("颜色设定...", Edit_Color));
		
        JMenu menuTools = createMenu(__("Tool"));
		menuTools.setMnemonic(KeyEvent.VK_T);
        menuTools.add(createMenuItem(__("Import Circuit..."), Tools_Circuit)).setMnemonic(KeyEvent.VK_C);
        menuTools.add(createMenuItem(__("Import Train..."), Tools_Train)).setMnemonic(KeyEvent.VK_R);
        
        JMenu menuHelp = createMenu(__("Help"));
		menuHelp.setMnemonic(KeyEvent.VK_H);
        menuHelp.add(createMenuItem(__("About..."), Help_About)).setMnemonic(KeyEvent.VK_A);

		jMenuBar.add(menuFile);
		jMenuBar.add(menuEdit);
		jMenuBar.add(menuSetup);
		jMenuBar.add(menuTools);
		jMenuBar.add(menuHelp);

		return jMenuBar;
	}

	private JMenu createMenu(String name) {
		JMenu menu = new JMenu(name);
		menu.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

		return menu;
	}

	private JMenuItem createMenuItem(String name, String actionCommand) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

		menuItem.setActionCommand(actionCommand);
		menuItem.addActionListener(this);

		return menuItem;
	}

	private void initChart() {
		if (Config.getAutoLoadLastFile()) {
			File file = new File(Config.getLastFile(""));
			if (file.exists())
				do_OpenFile(file.getAbsolutePath());
			else {
				new MessageBox(__("The last edited file do not exists. Thus a new file is created instead."))
					.showMessage();
				do_NewFile();
			}
		} else {
			do_NewFile();
		}
	}
	
	private void do_NewFile() {
		// Create a default file.
		TrainGraphFactory.resetIDCounters();
		trainGraph = TrainGraphFactory.createDefaultTrainGraph();
		
		Config.setCurrentFileToNew();
		ActionManager.getInstance().reset();
		
		setTitle();
		
		setModel(trainGraph);
	}
	
	private void do_OpenFile(String filePath) {
		try {
			TrainGraphFactory.resetIDCounters();
			trainGraph = TrainGraphFactory.loadTrainGraphFromFile(filePath);
			
			Config.setCurrentFile(filePath);
			Config.addToRecentOpenedFiles(filePath);
			
			do_UpdateRecentFilesMenu();
			
			setModel(trainGraph);
			
		} catch (IOException ioe) {
			System.out.println("Loading graph failed.");
			ioe.printStackTrace();
			new MessageBox(String.format(__("Load train graph failed. Please check the %s file."
					+ "\nReason:%s\nDetail:%s"), filePath, ioe.getMessage(), 
					ioe.getCause() )).showMessage();
		}
	}
	
	private void do_SaveFile(String filePath) {
		try {
			trainGraph.saveToFile(filePath);
			
			Config.setCurrentFile(filePath);
			Config.addToRecentOpenedFiles(filePath);
			
			do_UpdateRecentFilesMenu();
		} catch (IOException ex) {
			System.err.println("Err:" + ex.getMessage());
			this.statusBarMain.setText(__("Unable to save the graph."));
		}
	}
	
	private void do_UpdateRecentFilesMenu() {
		
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equalsIgnoreCase(File_Exit)) {
			this.doExit();
		} else if (command.equalsIgnoreCase(Tools_Circuit)) {
			this.doCircuitTools();
		} else if (command.equalsIgnoreCase(Tools_Train)) {
			this.doTrainTools();
		} else if (command.equalsIgnoreCase(Setup_Margin)) {
			this.doMarginSet();
		} else if (command.equalsIgnoreCase(Setup_Time)) {
			this.doTimeSet();
		} else if (command.equalsIgnoreCase(Setup_Dist)) {
			this.doDistSet();
		} else if (command.equalsIgnoreCase(Edit_Undo)) {
			ActionManager.getInstance().undo();
		} else if (command.equalsIgnoreCase(Edit_Redo)) {
			ActionManager.getInstance().redo();
		} else if (command.equalsIgnoreCase(Edit_Circuit)) {
			this.doEditCircuit();
		} else if (command.equalsIgnoreCase(Edit_Trains)) {
			this.doEditTrains();
//		} else if (command.equalsIgnoreCase(Edit_NewTrain)) {
//			this.doNewTrain();
		} else if (command.equalsIgnoreCase(Edit_FindTrain)) {
			this.doFindTrain();
//		} else if (command.equalsIgnoreCase(Edit_Color)) {
//			this.doColorSet();
		} else if (command.equalsIgnoreCase(File_Train)) {
			this.doLoadTrain();
//		} else if (command.equalsIgnoreCase(File_Circuit)) {
//			this.doLoadCircuit();
		} else if (command.equalsIgnoreCase(File_New_Chart)) {
			this.doNewChart();
		} else if (command.equalsIgnoreCase(File_Save_Chart)) {
			this.doSaveChart();
		} else if (command.equalsIgnoreCase(File_Save_Chart_As)) {
			this.doSaveChartAs();
		} else if (command.equalsIgnoreCase(File_Load_Chart)) {
			this.doLoadChart();
		} else if (command.equalsIgnoreCase(File_Export)) {
			this.doExportChart();
		} else if (command.equalsIgnoreCase(Help_About)) {
			this.doHelpAbout();
		}
	}
	
	private void doTrainTools() {
		//new MessageBox(this, "todo：从网络获取数据生成车次描述文件(.trf文件)。").showMessage();
		if(new YesNoBox(this, __("This operation will delete all the train information on the graph, then import the train information from the default time table for this circuit. Continue?")).askForYes()) {
			FindTrainsDialog waitingBox = new FindTrainsDialog(this);
			waitingBox.findTrains();
		}
	}

	private void doCircuitTools() {
		//new MessageBox(this, "todo：从里程表获取数据生成线路描述文件(.cir文件)。").showMessage();
		
		String xianlu = new XianluSelectDialog(this).getXianlu();
		if(xianlu == null)
			return;
		
		RailroadLine circuit = new CircuitMakeDialog(this, xianlu).getCircuit();
		if(circuit == null)
			return;
		
		// TODO: 菜单中的导入线路工具. 因为线路编辑对话框改线路编辑视图, 因此拿掉
		DEBUG_ACTION(() -> {
			new MessageBox(this, "待完成工作:需要把新创建的线路加入路网.待完成工作:")
				.showMessage();
		});
		System.out.println(circuit);
//		circuitEditDialog.showDialogForCircuit(circuit);
		
		this.setTitle();
		chartView.repaint();
		runView.refresh();
	}

	private void doEditTrains() {
//		TrainListView dlg = new TrainListView(this);
//		Dimension dlgSize = dlg.getPreferredSize();
//		Dimension frmSize = getSize();
//		Point loc = getLocation();
//		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
//				(frmSize.height - dlgSize.height) / 2 + loc.y);
//		dlg.setModal(true);
//		dlg.pack();
//		dlg.setVisible(true);
//
//		chartView.repaint();
//		sheetView.updateData();
//		runView.refresh();
	}

	private void doEditCircuit() {
//		circuitEditDialog.showDialog();
//		
//		this.setTitle();
//		chartView.repaint();
//		runView.refresh();
	}

	/**
	 * doFindTrain
	 */
	private void doFindTrain() {
		FindTrainDialog dlg = new FindTrainDialog(this);
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
	 * doHelpAbout
	 */
	private void doHelpAbout() {
		AboutBox dlg = new AboutBox(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.pack();
		dlg.setVisible(true);
	}

	/**
	 * doChartEdit
	 */
//	private void doColorSet() {
//		TrainsDialog dlg = new TrainsDialog(this);
//		Dimension dlgSize = dlg.getPreferredSize();
//		Dimension frmSize = getSize();
//		Point loc = getLocation();
//		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
//				(frmSize.height - dlgSize.height) / 2 + loc.y);
//		dlg.setModal(false);
//		dlg.pack();
//		dlg.setVisible(true);
//	}

	/**
	 * doExportChart
	 */
	public void doExportChart() {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Export Train Graph"));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new GIFFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String fileName = currentLineChart.railroadLine.name + df.format(new Date());
		chooser.setSelectedFile(new File(fileName));

		int returnVal = chooser.showSaveDialog(this); 
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (!f.getAbsolutePath().endsWith(".gif"))
				f = new File(f.getAbsolutePath() + ".gif");
			try {
				BufferedImage image = chartView.getBufferedImage();
				ImageIO.write(image, "gif", f);
			}
			catch(Exception ioe) {
				ioe.printStackTrace();
				this.statusBarMain.setText(__("Unable to export the graph."));
			}
		}
		

//		//获取默认打印作业
//		PrinterJob myPrtJob = PrinterJob.getPrinterJob();
//
//		//获取默认打印页面格式
//		//Paper paper = new Paper();
//		//paper.setImageableArea(80,80,1600,1000);
//		PageFormat pageFormat = myPrtJob.defaultPage();
//		pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
//		//pageFormat.setPaper();
//
//		//设置打印工作
//		myPrtJob.setPrintable(this, pageFormat);
//
//		//显示打印对话框
//		if (myPrtJob.printDialog()) {
//			try {
//				//进行每一页的具体打印操作
//				//设置打印分辨率
//				//PrintRequestAttributeSet attrib = new HashPrintRequestAttributeSet();
//				//attrib.add(new PrinterResolution(290, 290, PrinterResolution.DPI));
//				myPrtJob.print();
//			}
//
//			catch (PrinterException pe) {
//				pe.printStackTrace();
//			}
//		}
	}

	/**
	 * print
	 *
	 * @param graphics Graphics
	 * @param pageFormat PageFormat
	 * @param pageIndex int
	 * @return int
	 */
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		Rectangle2D.Double rec1 = new Rectangle2D.Double(0.0, 0.0, pageFormat
				.getImageableWidth(), pageFormat.getImageableHeight());
		Rectangle2D.Double rec2 = new Rectangle2D.Double(0.0, 0.0, pageFormat
				.getImageableWidth() - 1.5,
				pageFormat.getImageableHeight() - 0.5);
		g2D.draw(rec1);
		g2D.draw(rec2);
		/*
		 Rectangle clip = g.getClipBounds();
		 g.drawRect(clip.x,clip.y,clip.width,clip.height);
		 g.drawLine(0,0,clip.width,clip.height);
		 g.drawLine(0,clip.height,clip.width,0);
		 g.drawString("W="+clip.width+" H="+clip.height,clip.x+20,clip.y+20);
		 System.out.println("W="+clip.width+" H="+clip.height);
		 return 0;
		 */
		return Printable.PAGE_EXISTS;
	}

	public void doNewChart() {
		if(Config.isFileModified())
			if(new YesNoBox(this, __("Current train graph has changed.\nDo you want to save the changes?")).askForYes())
				doSaveChart(); 
		
		do_NewFile();
	}

	public void doSaveChart() {
		//如果是新文件，则改调“另存为”
		if(Config.isNewFile()) {
			doSaveChartAs();
		} else {
			do_SaveFile(Config.getCurrentFile());
		}
	}

	/**
	 * doSaveChartAs
	 */
	public void doSaveChartAs() {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Save As"));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new TRCFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		
		try {
			File recentPath = new File(Config.getLastFilePath(""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}
		
		// SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String fileName = Config.isNewFile() ? Config.NEW_FILE_NAME : Config.getCurrentFileName();
		chooser.setSelectedFile(new File(fileName));

		int returnVal = chooser.showSaveDialog(this); 
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			String suffix = TRCFilter.suffix;
			if (!f.getAbsolutePath().endsWith(suffix)) {
				f = new File(f.getAbsolutePath() + suffix);
			}

			do_SaveFile(f.getAbsolutePath());
		}
	}

	/**
	 * doLoadChart
	 */
	public void doLoadChart() {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Open"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new TRCFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		
		try {
			File recentPath = new File(Config.getLastFilePath(""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			System.out.println(f);
			
			do_OpenFile(f.getAbsolutePath());
		}
	}

	/**
	 * doLoadTrain
	 */
	public void doLoadTrain() {
		if(!(new YesNoBox(this, __("Load train information file and overwrite the existing information. Continue?")).askForYes()))
			return;

		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Load Train Information"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(true);
		chooser.addChoosableFileFilter(new CSVFilter());
		chooser.addChoosableFileFilter(new TRFFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(Config.getLastFilePath(""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}
		
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f[] = chooser.getSelectedFiles();
			if (f.length > 0)
				System.out.println(f[0]);
			else
				System.out.println("No File selected!");

			Train loadingTrain = null;
			for (int j = 0; j < f.length; j++) {
				loadingTrain = TrainGraphFactory.createInstance(Train.class);
				try {
					loadingTrain.loadFromFile2(f[j].getAbsolutePath());
//					prop.setProperty(Prop_Recent_Open_File_Path, chooser.getSelectedFile().getParentFile().getAbsolutePath());
				} catch (IOException ex) {
					System.err.println("Error: " + ex.getMessage());
				}

//				System.out.println(loadingTrain.getTrainName() + "次列车从"
//						+ loadingTrain.startStation + "到"
//						+ loadingTrain.terminalStation + "，共经停"
//						+ loadingTrain.stopNum + "个车站");
//				for (int i = 0; i < loadingTrain.stopNum; i++)
//					System.out.println(loadingTrain.stops[i].stationName + "站 "
//							+ Train.toTrainFormat(loadingTrain.stops[i].arrive)
//							+ " 到 "
//							+ Train.toTrainFormat(loadingTrain.stops[i].leave)
//							+ " 发");

				if(loadingTrain.isDownTrain(currentLineChart.railroadLine, false) > 0)
					currentLineChart.addTrain(loadingTrain);
			}

			//System.out.println("1.Move to: "+loadingTrain.getTrainName());
			//mainView.buildTrainDrawings();
			chartView.repaint();
			sheetView.updateData();
			chartView.findAndMoveToTrain(loadingTrain.getTrainName(currentLineChart.railroadLine));
			runView.refresh();
			//panelChart.panelLines.repaint();
		}

	}

	/**
	 * doDistSet
	 */
	private void doDistSet() {
		DistSetDialog dlg = new DistSetDialog(this, trainGraph);
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
		TimeSetDialog dlg = new TimeSetDialog(trainGraph.settings, this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(false);
		dlg.pack();
		dlg.setVisible(true);
	}

	private void doMarginSet() {
		MarginSetDialog dlg = new MarginSetDialog(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(false);
		dlg.pack();
		dlg.setVisible(true);
	}
	
	private ETRCSKB skb = null;
	public ETRCSKB getSKB() {
		if(skb == null)
			try {
				skb = new ETRCSKB("eda/");
			} catch (IOException e) {
				new MessageBox(this, __("Unable to open time table.")).showMessage();
				e.printStackTrace();
			}
			
		return skb;
	}
	
	private ETRCLCB lcb = null;
	public ETRCLCB getLCB() {
		if(lcb == null)
			try {
				lcb = new ETRCLCB("eda/");
			} catch (IOException e) {
				new MessageBox(this, __("Unable to open circuit table.")).showMessage();
				e.printStackTrace();
			}
		
		return lcb;
	}
	
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
	
	private void onNavigatorNodeChanged(NavigatorNodeType nodeType, 
			int index, Object... params) {
		DEBUG_ACTION(() -> {
//			new MessageBox(this, 
//					String.format("Type=%s, index=%d, params=[%s]", 
//							nodeType.toString(), index, 
//							Stream.of(params).map(obj->obj==null?"null":obj.toString())
//							.reduce((a,b) -> a+", "+b).orElse("")
//							))
//				.showMessage();
		}, TrainGraphPart.reprJoining(params, "\r\n", true));
		
		switch (nodeType) {
		case GLOBAL_SETTINGS:
			navigatorContentCard.show(navigatorContentPanel, 
					NavigatorNodeType.GLOBAL_SETTINGS.name());
			break;
		case RAILROAD_NETWORK:
			navigatorContentCard.show(navigatorContentPanel, 
					NavigatorNodeType.RAILROAD_NETWORK.name());
			break;
		case RAILROAD_LINES:
			navigatorContentCard.show(navigatorContentPanel, 
					NavigatorNodeType.RAILROAD_LINES.name());
			break;
		case RAILROAD_LINE_SPECIFIC:
			railLineEditView.switchRailLine((RailroadLine) params[0]); 
			navigatorContentCard.show(navigatorContentPanel, 
					NavigatorNodeType.RAILROAD_LINES.name());
			break;
		case TRAIN_TYPES:
			navigatorContentCard.show(navigatorContentPanel, 
					NavigatorNodeType.TRAIN_TYPES.name());
			break;
		case TRAIN_TYPE_SPECIFIC:
			/**********************************************/
			currentLineChart = (RailroadLineChart) params[0];
			navigatorContentCard.show(navigatorContentPanel, 
					NavigatorNodeType.TRAIN_TYPES.name());
			break;
		case ALL_TRAINS:
			navigatorContentCard.show(navigatorContentPanel, 
					NavigatorNodeType.ALL_TRAINS.name());
			break;
		case TIME_TABLES:
			navigatorContentCard.show(navigatorContentPanel, 
					NavigatorNodeType.TIME_TABLES.name());
			break;
		case TIME_TABLE_LINE:
			currentLineChart = (RailroadLineChart) params[0];
			currentNetworkChart = (RailNetworkChart) params[1];

			chartView.setModel(trainGraph, currentLineChart);
			runView.setModel(trainGraph, currentLineChart);
			sheetView.setModel(trainGraph, currentLineChart);

			navigatorContentCard.show(navigatorContentPanel, 
					NavigatorNodeType.TIME_TABLE_LINE.name());
			break;
		default:
			break;
		}
		navigatorContentPanel.revalidate();
		
	}
	
	//Overridden so we can exit when window is closed
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			this.doExit();
			super.processWindowEvent(e);
		} else if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
			super.processWindowEvent(e);
			chartView.requestFocus();
		}
	}
}
