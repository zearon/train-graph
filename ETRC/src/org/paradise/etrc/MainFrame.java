package org.paradise.etrc;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;

import org.paradise.etrc.data.ParsingException;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.skb.ETRCLCB;
import org.paradise.etrc.data.skb.ETRCSKB;
import org.paradise.etrc.data.v1.RailNetworkChart;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
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
import org.paradise.etrc.util.config.Config;
import org.paradise.etrc.view.IView;
import org.paradise.etrc.view.alltrains.AllTrainsView;
import org.paradise.etrc.view.lineedit.RailroadLineEditView;
import org.paradise.etrc.view.nav.Navigator;
import org.paradise.etrc.view.nav.Navigator.NavigatorNodeType;
import org.paradise.etrc.view.network.RailNetworkEditorView;
import org.paradise.etrc.view.runningchart.RunningChartView;
import org.paradise.etrc.view.runningchart.chart.ChartView;
import org.paradise.etrc.view.runningchart.dynamic.DynamicView;
import org.paradise.etrc.view.runningchart.sheet.SheetView;
import org.paradise.etrc.view.settings.SettingsView;
import org.paradise.etrc.view.timetableedit.TimetableEditView;
import org.paradise.etrc.view.timetables.TimetableListView;
import org.paradise.etrc.view.traintypes.TrainTypesView;

import com.zearon.util.os.OSXUtil;
import com.zearon.util.ui.controller.ActionManager;
import com.zearon.util.ui.databinding.UIBinding;
import com.zearon.util.ui.databinding.UIBindingManager;
import com.zearon.util.ui.map.GLWindowManager;
import com.zearon.util.ui.widget.StatusBar;

import static org.paradise.etrc.ETRC.__;

import static com.zearon.util.debug.DebugUtil.DEBUG_ACTION;
import static com.zearon.util.debug.DebugUtil.DEBUG_MSG;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class MainFrame extends JFrame implements ActionListener, Printable {
	private static final long serialVersionUID = 1L;
	
	public static final int	initialDividerLocation	= 200;
	
	private UIBindingManager uiBindingManager = UIBindingManager.getInstance(this);
	
	private JMenu openRecentFilesMenu;
	private Vector<JMenuItem> recentFileMenuItems = new Vector<>();
	
	public JSplitPane navigatorSplitPane;
	public Navigator navigator;
	public JPanel navigatorContentPanel;
	CardLayout navigatorContentCard;
	CardLayout toolbarContentCard;
	CardLayout statusbarContentCard;
	
	private JPanel statusPanel;
	private JMenuBar menuBar;
	private JToolBar mainToolBar;
	private JPanel toolBarContainer;
	private JMenu menuTools; // Anchor of new edit menu of IView
	private JLabel statusBarEmpty;
	
	private HashMap<String, Container> viewMap = new HashMap<> ();
	public SettingsView settingsView;
	public RailNetworkEditorView railNetworkEditorView;
	public RailroadLineEditView railLineEditView;
	public TrainTypesView trainTypesView;
	public AllTrainsView allTrainsView;
	public TimetableListView timetableListView;
	public RunningChartView runningChartView;
	public TimetableEditView timetableEditView;
	
	public StatusBar statusBarMain;
	public StatusBar statusBarRight;
	
	public TrainGraph trainGraph;

	
	private boolean firstTimeLoading = true;

	private boolean ui_inited;
	
	public static MainFrame instance;
	public static MainFrame getInstance() { return instance; }
	
	//Construct the frame
	public MainFrame() {
		if (instance == null)
			instance = this;
				
		initChart();
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		uiBindingManager.updateUI(null);

		ActionManager.getInstance().setUpdateUIHook(actionMgr -> this.setTitle());
		
		UIBinding.setExceptionHandler(e -> {
			e.printStackTrace();
			new MessageBox(e.getMessage()).showMessage();
		});

		ui_inited = true;
		setModel(trainGraph);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				if (!firstTimeLoading)
					return;
				
				firstTimeLoading = false;
				
				// test circuitEditDialog
				// DEBUG_ACTION(() -> circuitEditDialog.showDialog());

				OSXUtil.addFullScreenModeSupportOnOSX(MainFrame.this);
				if (Config.getInstance().getFullScreenOnStartupForOSX()) {
					OSXUtil.setFullScreenModeOnOSX(MainFrame.this);
				}
				
				navigator.expandAll(true);
			}
		});
		
	}
	
	public void setModel(TrainGraph trainGraph) {
		if (trainGraph.currentNetworkChart == null)
			throw new ParsingException("There is no network chart in save file.");
		
		if (trainGraph.currentLineChart == null)
			throw new ParsingException("There is no valid line chart in save file.");
		
		setTitle();
		
		if (ui_inited) {
			// Set Navigator in terms of train Graph
			navigator.setTrainGraph(trainGraph);
			railNetworkEditorView.setModel(trainGraph);
			railLineEditView.setModel(trainGraph);
			trainTypesView.setModel(trainGraph);
			allTrainsView.setModel(trainGraph);
			runningChartView.setModel(trainGraph);
			timetableListView.setModel(trainGraph);
			timetableEditView.setModel(trainGraph);
			
			navigator.expandAll(true);
		}
	}
	
	public void validateModel() {
		
	}

	public void doExit() {
		Boolean confirmed = null;
		boolean isFileModified;
		if((isFileModified = Config.getInstance().isFileModified()) &&
				(confirmed = confirmSaveCurrentFile()) == null) 
			return;
		
		if (isFileModified && confirmed)
			doSaveChart();
		
		System.exit(0);
	}
	
	// {{ init UI

	//Component initialization
	private void jbInit() throws Exception {
		
		settingsView = new SettingsView(trainGraph);
		viewMap.put(NavigatorNodeType.GLOBAL_SETTINGS.name(), settingsView);
		railNetworkEditorView = new RailNetworkEditorView(trainGraph);
		viewMap.put(NavigatorNodeType.RAILROAD_NETWORK.name(), railNetworkEditorView);
		railLineEditView = new RailroadLineEditView(this);
		viewMap.put(NavigatorNodeType.RAILROAD_LINES.name(), railLineEditView);
		trainTypesView = new TrainTypesView(trainGraph);
		viewMap.put(NavigatorNodeType.TRAIN_TYPES.name(), trainTypesView);
		allTrainsView = new AllTrainsView();
		viewMap.put(NavigatorNodeType.RAILNETWORK_ALL_TRAINS.name(), allTrainsView);
		runningChartView = new RunningChartView(trainGraph);
		viewMap.put(NavigatorNodeType.TIME_TABLE_LINE.name(), runningChartView);
		timetableListView = new TimetableListView(trainGraph);
		viewMap.put(NavigatorNodeType.TIME_TABLES.name(), timetableListView);
		timetableEditView = new TimetableEditView(trainGraph);
		viewMap.put(NavigatorNodeType.TIME_TABLE_LINE_DOWN.name(), timetableEditView);
		viewMap.put(NavigatorNodeType.TIME_TABLE_LINE_UP.name(), timetableEditView);

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
		navigatorContentPanel.add(NavigatorNodeType.RAILNETWORK_ALL_TRAINS.name(), 
				allTrainsView);
		navigatorContentPanel.add(NavigatorNodeType.TIME_TABLES.name(),
				timetableListView);
		navigatorContentPanel.add(NavigatorNodeType.TIME_TABLE_LINE.name(), 
				runningChartView);
		navigatorContentPanel.add(NavigatorNodeType.TIME_TABLE_LINE_DOWN.name(),
				timetableEditView);
		navigatorContentPanel.add(NavigatorNodeType.TIME_TABLE_LINE_UP.name(),
				timetableEditView);
		// TODO: 设置主视图启动时的编辑视图
				
		navigator = new Navigator();
		navigator.addNodeSelectionChangedListener(this::onNavigatorNodeChanged);
		
		navigatorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, 
				new JScrollPane(navigator), navigatorContentPanel);
		navigatorSplitPane.setDividerLocation(initialDividerLocation);
		navigatorSplitPane.setDividerSize(6);
		navigatorSplitPane.setOneTouchExpandable(true);
		
		
		//mainPanel.add(runView, BorderLayout.NORTH);

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

		statusBarMain = loadStatusBar(__("Status: Normal"));
		statusBarEmpty = loadStatusBar("");
		statusBarRight = loadStatusBar(__("Status: Normal"));
		
		statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(statusBarMain, BorderLayout.WEST);
		statusPanel.add(statusBarEmpty, BorderLayout.CENTER);
		statusPanel.add(statusBarRight, BorderLayout.EAST);
		
		loadMainToolBar();
		toolBarContainer = new JPanel();
		toolBarContainer.setLayout(new BoxLayout(toolBarContainer, BoxLayout.X_AXIS));
		toolBarContainer.setPreferredSize(new Dimension(200, mainToolBar.getPreferredSize().height + 2));
//		toolBarContainer.setAlignmentY(CENTER_ALIGNMENT);
		toolBarContainer.add(mainToolBar);

		contentPane.add(statusPanel, BorderLayout.SOUTH);
		contentPane.add(toolBarContainer, BorderLayout.NORTH);
		contentPane.add(navigatorSplitPane, BorderLayout.CENTER);

		this.setTitle();
	}
	
	private static final String titlePrefix = __("Jeff's Electronic Train Graph");

//	private String activeTrainName = "";
//
//	public void setActiceTrainName(String _name) {
//		if (_name.equalsIgnoreCase(""))
//			activeTrainName = "";
//		else
//			activeTrainName = " (" + _name + ") ";
//		
//		//_name不为空的时候，加入hitory
//		if (!_name.equalsIgnoreCase("")) {
//			//如果已经存在于history中则删除之，以保证新加入的位于第一个
//			if(trainSelectHistory.contains(_name))
//				trainSelectHistory.remove(_name);
//			
//			trainSelectHistory.add(0, _name);
//			
//			//超过最大历史记录数，则删除最老的
//			if(trainSelectHistory.size() > MAX_TRAIN_SELECT_HISTORY_RECORD)
//				for(int i=12; i<trainSelectHistory.size(); i++)
//					trainSelectHistory.remove(i);
//			
//			cbTrainSelectHistory.setModel(new DefaultComboBoxModel<String>(trainSelectHistory));
//		}
//
//		setTitle();
//	}

	public void setTitle() {
		setTitle(String.format("%s -- [%s] %s", titlePrefix, Config.getInstance().getCurrentFile(),
				(Config.getInstance().isFileModified() ? __("Modified ") : "")));
	}

	private StatusBar loadStatusBar(String text) {
		@SuppressWarnings("serial")
		StatusBar statusBar = new StatusBar(text) {
			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				size.width = navigatorSplitPane.getDividerLocation();
				return size;
			}
		};
		
		return statusBar;
	}

	private JToolBar loadMainToolBar() {
		mainToolBar = new JToolBar();
		mainToolBar.setFloatable(false);
		
		//文件操作
		JButton jbOpenFile = createTBButton("openFile", __("Open a Chart"), File_Load_Chart);
		JButton jbSaveFile = createTBButton("saveFile", __("Save Chart"), File_Save_Chart);
		JButton jbSaveAs   = createTBButton("saveAs", __("Save Chart As"), File_Save_Chart_As);
		mainToolBar.add(jbOpenFile);
		mainToolBar.add(jbSaveFile);
		mainToolBar.add(jbSaveAs);
		
		//撤销,重做
		JButton jbUndo = createTBButton("undo", __("Undo"), Edit_Undo);
		JButton jbRedo  = createTBButton("redo", __("Redo"), Edit_Redo);
		ActionManager.getInstance().setToolbarButton(jbUndo, jbRedo);
		mainToolBar.addSeparator();
		mainToolBar.add(jbUndo);
		mainToolBar.add(jbRedo);

		return mainToolBar;
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
//	private final String File_Load_Map = "File_Load_Map";
	private final String File_Train = "File_Train";
	private final String File_Export = "File_Export";
	private final String File_FullScreen = "File_FullScreen";
	private final String File_Exit = "File_Exit";

	private final String Edit_Undo = "Edit_Undo";
	private final String Edit_Redo = "Edit_Redo";
//	private final String Edit_Action_History = "Edit_Action_History";
	private final String Edit_Cut = "Edit_Cut";
	private final String Edit_Copy = "Edit_Copy";
	private final String Edit_Paste = "Edit_Paste";
	private final String Edit_FindTrain = "Edit_FindTrain";
	private final String Edit_Circuit = "Edit_Circuit";
	private final String Edit_Trains = "Edit_Trains";

	private final String Setup_Margin = "Setup_MarginSet";
//	private final String Setup_Time = "Setup_TimeSet";
//	private final String Setup_Dist = "Setup_DistSet";

	private final String Tools_Circuit = "Tools_Circuit";
	private final String Tools_Train = "Tools_Train";
	
	private final String Help_About = "Help_About";

	private JMenuBar loadMenu() {
		menuBar = new JMenuBar();

		JMenu menuFile = createMenu(__("File"));
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuFile.add(createMenuItem(__("New"), File_New_Chart, KeyEvent.VK_N)).setMnemonic(KeyEvent.VK_N);
		menuFile.add(createMenuItem(__("Open..."), File_Load_Chart, KeyEvent.VK_O)).setMnemonic(KeyEvent.VK_O);
		openRecentFilesMenu = createMenu(__("Open Recent Files"));
		menuFile.add(openRecentFilesMenu);
		do_UpdateRecentFilesMenu();
		menuFile.addSeparator();
		menuFile.add(createMenuItem(__("Save"), File_Save_Chart, KeyEvent.VK_S)).setMnemonic(KeyEvent.VK_S);
		menuFile.add(createMenuItem(__("Save As..."), File_Save_Chart_As, KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK)).setMnemonic(KeyEvent.VK_A);
		menuFile.addSeparator();
//		menuFile.add(createMenuItem("更改线路...", File_Circuit)); //Bug:更改线路后没有清空车次
		menuFile.add(createMenuItem(__("Load Train..."), File_Train)).setMnemonic(KeyEvent.VK_L);
		menuFile.addSeparator();
		menuFile.add(createMenuItem(__("Export..."), File_Export)).setMnemonic(KeyEvent.VK_P);
		menuFile.addSeparator();
		menuFile.add(createMenuItem(__("Toggle Full Screen Mode"), File_FullScreen, KeyEvent.VK_F, InputEvent.SHIFT_DOWN_MASK)).setMnemonic(KeyEvent.VK_X);
		menuFile.addSeparator();
		menuFile.add(createMenuItem(__("Exit"), File_Exit)).setMnemonic(KeyEvent.VK_X);

//		JMenu menuSetup = createMenu(__("Settings"));
//		menuSetup.setMnemonic(KeyEvent.VK_S);
//		menuSetup.add(createMenuItem(__("Margin..."), Setup_Margin)).setMnemonic(KeyEvent.VK_M);
//		menuSetup.addSeparator();
//		menuSetup.add(createMenuItem(__("Timeline..."), Setup_Time)).setMnemonic(KeyEvent.VK_T);
//		menuSetup.add(createMenuItem(__("Distance Bar..."), Setup_Dist)).setMnemonic(KeyEvent.VK_D);

        JMenu menuEdit = createMenu(__("Edit"));
		menuEdit.setMnemonic(KeyEvent.VK_E);
		JMenuItem undoMenuItem = menuEdit.add(createMenuItem(__("Undo"), Edit_Undo, KeyEvent.VK_Z));
		JMenuItem redoMenuItem = menuEdit.add(createMenuItem(__("Redo"), Edit_Redo, KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK));
		JMenu undoMenu = createMenu(__("Undo..."));
		JMenu redoMenu = createMenu(__("Redo..."));
		menuEdit.add(undoMenu);
		menuEdit.add(redoMenu);
//		menuFile.addSeparator();
//		menuEdit.add(createMenuItem(__("Cut"), Edit_Cut, KeyEvent.VK_X));
//		menuEdit.add(createMenuItem(__("Copy"), Edit_Copy, KeyEvent.VK_C));
//		menuEdit.add(createMenuItem(__("Paste"), Edit_Paste, KeyEvent.VK_V));
		ActionManager.getInstance().setMenuItem(undoMenuItem, redoMenuItem, undoMenu, redoMenu);
		
		menuEdit.addSeparator();
        menuEdit.add(createMenuItem(__("Circuit..."), Edit_Circuit)).setMnemonic(KeyEvent.VK_C);
        menuEdit.add(createMenuItem(__("Train..."), Edit_Trains)).setMnemonic(KeyEvent.VK_R);
		menuEdit.addSeparator();
//		menuEdit.add(createMenuItem("车次录入...", Edit_NewTrain));
        menuEdit.add(createMenuItem(__("Find Train..."), Edit_FindTrain)).setMnemonic(KeyEvent.VK_F);
//		menuEdit.addSeparator();
//		menuEdit.add(createMenuItem("颜色设定...", Edit_Color));
		
        menuTools = createMenu(__("Tool"));
		menuTools.setMnemonic(KeyEvent.VK_T);
        menuTools.add(createMenuItem(__("Import Circuit..."), Tools_Circuit)).setMnemonic(KeyEvent.VK_C);
        menuTools.add(createMenuItem(__("Import Train..."), Tools_Train)).setMnemonic(KeyEvent.VK_R);
        
        JMenu menuHelp = createMenu(__("Help"));
		menuHelp.setMnemonic(KeyEvent.VK_H);
        menuHelp.add(createMenuItem(__("About..."), Help_About)).setMnemonic(KeyEvent.VK_A);

		menuBar.add(menuFile);
		menuBar.add(menuEdit);
//		menuBar.add(menuSetup);
		menuBar.add(menuTools);
		menuBar.add(menuHelp);

		return menuBar;
	}

	private JMenu createMenu(String name) {
		JMenu menu = new JMenu(name);
		menu.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

		return menu;
	}

	private JMenuItem createMenuItem(String name, String actionCommand) {
		return createMenuItem(name, actionCommand, -1, 0);
	}

	private JMenuItem createMenuItem(String name, String actionCommand, int shortcutKey) {
		return createMenuItem(name, actionCommand, shortcutKey, 0);
	}

	private JMenuItem createMenuItem(String name, String actionCommand, int shortcutKey, int extraModifier) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

		menuItem.setActionCommand(actionCommand);
		menuItem.addActionListener(this);
		
		if (shortcutKey >= 0) {
			if (ETRC.isOSX10_7OrAbove()) {
				menuItem.setAccelerator(
						KeyStroke.getKeyStroke(shortcutKey, extraModifier | InputEvent.META_DOWN_MASK));
			} else {
				menuItem.setAccelerator(
						KeyStroke.getKeyStroke(shortcutKey, extraModifier | InputEvent.CTRL_DOWN_MASK));
			}
		}

		return menuItem;
	}
	
	// }}

	private void initChart() {
		if (Config.getInstance().getAutoLoadLastFile()) {
			File file = new File(Config.getInstance().getLastFile(""));
			if (file.exists())
				try {
					do_OpenFile(file.getAbsolutePath(), true);
				} catch (Exception e) {
					new MessageBox(__("A new file is created."))
						.showMessage();
					do_NewFile();
				}
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
		
		Config.getInstance().setCurrentFileToNew();
		ActionManager.getInstance().reset();
		
		setTitle();
		
		setModel(trainGraph);
	}
	
	private void do_OpenFile(String filePath, boolean throwExceptions) {
		try {
			TrainGraphFactory.resetIDCounters();
			trainGraph = TrainGraphFactory.loadTrainGraphFromFile(filePath);
			
			Config.getInstance().setCurrentFile(filePath);
			Config.getInstance().addToRecentOpenedFiles(filePath);
			ActionManager.getInstance().reset();
			
			do_UpdateRecentFilesMenu();
			
			setModel(trainGraph);
			
		} catch (Exception ioe) {
			String msg = String.format(__("Load train graph failed. Please check the %s file."
					+ "\nReason:%s\nDetail:%s"), filePath, ioe.getMessage(), 
					ioe.getCause() );
			ioe.printStackTrace();
			new MessageBox(msg).showMessage();
			
			if (throwExceptions)
				throw new RuntimeException();
		}
	}
	
	private void do_SaveFile(String filePath) {
		try {
			trainGraph.saveToFile(filePath);
			
			Config.getInstance().setCurrentFile(filePath);
			Config.getInstance().addToRecentOpenedFiles(filePath);
			ActionManager.getInstance().markModelSaved();
			
			do_UpdateRecentFilesMenu();
			statusBarMain.setTempStatus(__("File Saved."));
		} catch (IOException ex) {
			System.err.println("Err:" + ex.getMessage());
			new MessageBox(__("Unable to save the graph.")).showMessage();
		}
	}
	
	private void do_UpdateRecentFilesMenu() {
		if (openRecentFilesMenu == null)
			return;
		
		recentFileMenuItems.forEach(item -> openRecentFilesMenu.remove(item) );
		recentFileMenuItems.clear();
		
		for (String filePath : Config.getInstance().getRecentOpenedFiles()) {
			JMenuItem item = new JMenuItem(filePath);
			item.setFont(new Font(__("FONT_NAME"), 0, 12));
			item.setName(filePath);
			item.addActionListener(this::menuItem_OpenRecentFile);
			recentFileMenuItems.add(item);
			openRecentFilesMenu.add(item);
		}
	}
	
	private void menuItem_OpenRecentFile(ActionEvent e) {
		Boolean confirmed = null;
		boolean isFileModified;
		if((isFileModified = Config.getInstance().isFileModified()) &&
				(confirmed = confirmSaveCurrentFile()) == null) 
			return;
		
		if (isFileModified && confirmed)
			doSaveChart();
		
		JMenuItem item = (JMenuItem) e.getSource();
		String filePath = item.getName();
		do_OpenFile(filePath, false);
	}

	/**
	 * Show a Yes/No/Cancel message box to user and let him/her choose whether 
	 * saving the  current file is required.
	 * @return True if Yes button is pressed, false if No button is pressed, 
	 * and null if Cancel button is pressed
	 */
	private Boolean confirmSaveCurrentFile() {
		return new YesNoBox(this, __("Current train graph has been changed.\n"
				+ "Do you want to save the changes?")).askForYesNoOrCancel();
		
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equalsIgnoreCase(File_Exit)) {
			this.doExit();
		} else if (command.equalsIgnoreCase(File_FullScreen)) {
			OSXUtil.setFullScreenModeOnOSX(this);
		} 
//		else if (command.equalsIgnoreCase(Tools_Circuit)) {
//			this.doCircuitTools();
//		} 
		else if (command.equalsIgnoreCase(Setup_Margin)) {
			this.doMarginSet();
		} else if (command.equalsIgnoreCase(Edit_Undo)) {
			ActionManager.getInstance().undo();
		} else if (command.equalsIgnoreCase(Edit_Redo)) {
			ActionManager.getInstance().redo();
		} else if (command.equalsIgnoreCase(Edit_Cut)) {
			this.doCut();
		} else if (command.equalsIgnoreCase(Edit_Copy)) {
			this.doCopy();
		} else if (command.equalsIgnoreCase(Edit_Paste)) {
			this.doPaste();
		} else if (command.equalsIgnoreCase(Edit_Circuit)) {
			this.doEditCircuit();
		} else if (command.equalsIgnoreCase(Edit_Trains)) {
			this.doEditTrains();
//		} else if (command.equalsIgnoreCase(Edit_NewTrain)) {
//			this.doNewTrain();
//		}
//		else if (command.equalsIgnoreCase(Edit_Color)) {
//			this.doColorSet();
//		} 
//		else if (command.equalsIgnoreCase(File_Train)) {
//			this.doLoadTrain();
//		} 
//		else if (command.equalsIgnoreCase(File_Circuit)) {
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
	
	private void doCut() {
		DEBUG_MSG("Do clipboard cut");
	}
	
	public void doCopy() {
		DEBUG_MSG("Do clipboard copy");
	}
	
	public void doPaste() {
		DEBUG_MSG("Do clipboard paste");
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
		String fileName = trainGraph.currentLineChart.railroadLine.getName() + df.format(new Date());
		chooser.setSelectedFile(new File(fileName));

		int returnVal = GLWindowManager.showDialogOnFloatingGLWindow(
				() -> chooser.showSaveDialog(this)); 
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (!f.getAbsolutePath().endsWith(".gif"))
				f = new File(f.getAbsolutePath() + ".gif");
			try {
				BufferedImage image = runningChartView.getChartView().getBufferedImage();
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
		Boolean confirmed = null;
		boolean isFileModified;
		if((isFileModified = Config.getInstance().isFileModified()) &&
				(confirmed = confirmSaveCurrentFile()) == null) 
			return;
		
		if (isFileModified && confirmed)
			doSaveChart();
		
		do_NewFile();
	}

	public void doSaveChart() {
		//如果是新文件，则改调“另存为”
		if(Config.getInstance().isNewFile()) {
			doSaveChartAs();
		} else {
			do_SaveFile(Config.getInstance().getCurrentFile());
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
			File recentPath = new File(Config.getInstance().getLastFilePath(""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}
		
		// SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String fileName = Config.getInstance().isNewFile() ? Config.NEW_FILE_NAME : Config.getInstance().getCurrentFileName();
		chooser.setSelectedFile(new File(fileName));

		int returnVal = GLWindowManager.showDialogOnFloatingGLWindow(
				() -> chooser.showSaveDialog(this)); 
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
		Boolean confirmed = null;
		boolean isFileModified;
		if((isFileModified = Config.getInstance().isFileModified()) &&
				(confirmed = confirmSaveCurrentFile()) == null) 
			return;
		
		if (isFileModified && confirmed)
			doSaveChart();
		
		
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Open"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new TRCFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		
		try {
			File recentPath = new File(Config.getInstance().getLastFilePath(""));
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {}

		int returnVal = GLWindowManager.showDialogOnFloatingGLWindow(
				() -> chooser.showOpenDialog(this));
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			System.out.println(f);
			
			do_OpenFile(f.getAbsolutePath(), false);
		}
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
	
	private void onNavigatorNodeChanged(boolean triggeredByLeftButton, TreePath treePath,
			NavigatorNodeType nodeType, int index, Object... params) {
//		DEBUG_ACTION(() -> {
//		}, TrainGraphPart.reprJoining(params, "\r\n", true));
		if (!triggeredByLeftButton)
			return;
		
		switch (nodeType) {
		case GLOBAL_SETTINGS:
			switchView(NavigatorNodeType.GLOBAL_SETTINGS.name());
			break;
		case RAILROAD_NETWORK:
			switchView(NavigatorNodeType.RAILROAD_NETWORK.name());
			break;
		case RAILROAD_LINE_SPECIFIC:
			railLineEditView.switchRailLine((RailroadLine) params[0]); 
		case RAILROAD_LINES:
			switchView(NavigatorNodeType.RAILROAD_LINES.name());
			break;
		case TRAIN_TYPES:
			switchView(NavigatorNodeType.TRAIN_TYPES.name());
			break;
		case TRAIN_TYPE_SPECIFIC:
			/**********************************************/
//			trainGraph.currentLineChart = (TrainType) params[0];
//			switchView(navigatorContentPanel, 
//					NavigatorNodeType.TRAIN_TYPES.name());
			break;
		case TIME_TABLES:
			switchView(NavigatorNodeType.TIME_TABLES.name());
			break;
		case RAILNETWORK_ALL_TRAINS:
			trainGraph.currentNetworkChart = (RailNetworkChart) params[1];

			runningChartView.switchLineChart();
			allTrainsView.refresh();
			
			switchView(NavigatorNodeType.RAILNETWORK_ALL_TRAINS.name());
			break;
		case TIME_TABLE_LINE:
			trainGraph.currentLineChart = (RailroadLineChart) params[0];
			trainGraph.currentNetworkChart = (RailNetworkChart) params[1];

			runningChartView.switchLineChart();
			
			switchView(NavigatorNodeType.TIME_TABLE_LINE.name());
			break;
		case TIME_TABLE_LINE_DOWN:
			trainGraph.currentLineChart = (RailroadLineChart) params[0];
			trainGraph.currentNetworkChart = (RailNetworkChart) params[1];
			
			timetableEditView.switchChart(true);
			
			switchView(NavigatorNodeType.TIME_TABLE_LINE_UP.name());
			break;

		case TIME_TABLE_LINE_UP:
			trainGraph.currentLineChart = (RailroadLineChart) params[0];
			trainGraph.currentNetworkChart = (RailNetworkChart) params[1];
			
			timetableEditView.switchChart(false);
			
			switchView(NavigatorNodeType.TIME_TABLE_LINE_UP.name());
			break;
		default:
			break;
		}
		navigatorContentPanel.revalidate();
		
	}
	
	private void switchView(String cardName) {
		navigatorContentCard.show(navigatorContentPanel, cardName);
		Container view = viewMap.get(cardName);
		GLWindowManager.switchTopContainer(view);
		
		int anchorMenuIndex = 2;
		JMenu editMenu = null, origEditMenu = menuBar.getMenu(anchorMenuIndex);
		JToolBar toolBar = null;
		Component statusBar = null;
		
		origEditMenu = origEditMenu == menuTools ? null : origEditMenu;
		if (view instanceof IView) {
			IView view0 =(IView) view;
			editMenu = view0.getEditMenu();
			toolBar = view0.getToolBar();
			statusBar = view0.getStatusBar();
		}

		if (origEditMenu != null)
			menuBar.remove(anchorMenuIndex);
		if (editMenu != null && editMenu.getPopupMenu().getComponentCount() > 0) 
			menuBar.add(editMenu, anchorMenuIndex);
		menuBar.revalidate();
		
		if (statusBar != null) {
			statusPanel.removeAll();
			statusPanel.add(statusBarMain, BorderLayout.WEST);
			statusPanel.add(statusBar, BorderLayout.CENTER);
			statusPanel.add(statusBarRight, BorderLayout.EAST);
		} else {
			statusPanel.removeAll();
			statusPanel.add(statusBarMain, BorderLayout.WEST);
			statusPanel.add(statusBarEmpty, BorderLayout.CENTER);
			statusPanel.add(statusBarRight, BorderLayout.EAST);
		}

		while (toolBarContainer.getComponentCount() > 1) {
			toolBarContainer.remove(1);
		}
		if (toolBar != null && toolBar.getComponentCount() > 0) {
			toolBarContainer.add(toolBar);
		}
		toolBarContainer.repaint();
	}
	
	//Overridden so we can exit when window is closed
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			this.doExit();
			super.processWindowEvent(e);
		} else if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
			super.processWindowEvent(e);
		}
	}
	
	// {{ 遗留代码中对话框部分的依赖
	
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

	public ChartView getChartView() {
			return runningChartView.getChartView();
		}
	
	public DynamicView getRunView() {
		return runningChartView.getRunView();
	}

	public SheetView getSheetView() {
		return runningChartView.getSheetView();
	}
	
	// }}
	
}
