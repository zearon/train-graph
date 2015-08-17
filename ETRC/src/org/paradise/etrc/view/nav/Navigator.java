package org.paradise.etrc.view.nav;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainType;

import static org.paradise.etrc.ETRC.__;

import static com.zearon.util.debug.DebugUtil.DEBUG_MSG;

/**
 * 主界面导航栏
 * @author Jeff Gong
 *
 */
public class Navigator extends JTree implements TreeSelectionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2262708630560630874L;
	
	public static enum NavigatorNodeType {
		UNKNOWN, ROOT,
		COMPREHENSIVE_VIEW, GLOBAL_SETTINGS, RAILROAD_NETWORK, RAILROAD_LINES,
		RAILROAD_LINE_SPECIFIC, TRAIN_TYPES, TRAIN_TYPE_SPECIFIC,
		RAILNETWORK_ALL_TRAINS, TIME_TABLES, TIME_TABLE_SPECIFIC, TIME_TABLE_LINE,
		TIME_TABLE_LINE_DOWN, TIME_TABLE_LINE_UP, 
		REMARKS
		};
		
	@FunctionalInterface
	public static interface NavigatorNodeSelectionListener {
		public void onNavigatorNodeChanged(boolean isTriggerdByMouseLeftButton,
				TreePath treePath, NavigatorNodeType nodeType, int index, Object... param);
	}

	static final String ALL_TRAIN_LABEL = __("All Trains");
	static final String DOWNWARD_LABEL = __("Down-going Trains");
	static final String UPWARD_LABEL = __("Up-going Trains");
	public static Navigator instance;
	
	
	protected DefaultTreeModel treeModel;
	
	protected DefaultMutableTreeNode rootNode;
	protected DefaultMutableTreeNode comprehensiveViewNode;
	protected DefaultMutableTreeNode globalSettingsNode;
	protected DefaultMutableTreeNode railroadNetworkNode;
	protected DefaultMutableTreeNode railroadLinesNode;
	protected DefaultMutableTreeNode trainTypesNode;
	protected DefaultMutableTreeNode timeTablesNode;
	protected DefaultMutableTreeNode remarksNode;
	
	protected Vector<DefaultMutableTreeNode> railroadLineNodes;
	protected Vector<DefaultMutableTreeNode> trainTypeNodes;
	protected Vector<DefaultMutableTreeNode> timeTableNodes;
	protected Vector<DefaultMutableTreeNode> railroadLineTimeTableNodes;

	private JPopupMenu trainTypePopupMenu;
	private JMenuItem miHideTrainType;
	private JMenuItem miShowTrainType;
	private JMenuItem miHighLightTrainType;
	
	private JPopupMenu allTrainTypePopupMenu;
	private JMenuItem miHideAllTrainTypes;
	private JMenuItem miShowAllTrainTypes;
	
	private JPopupMenu foldingNodesPopupMenu;
	private JMenuItem miFoldAllNodes;
	private JMenuItem miExpandAllNodes;
	private TreePath foldingNodeTreePath;
	
	private MouseAdapter contextMenuAdapter;
	private int popupMenuX, popupMenuY;
	
	private Vector<NavigatorNodeSelectionListener> nodeSelectionListeners;
	protected TrainGraph trainGraph;
	protected TrainType selectedTrainType;

	public Navigator() {
		railroadLineNodes = new Vector<> ();
		trainTypeNodes = new Vector<> ();
		timeTableNodes = new Vector<> ();
		railroadLineTimeTableNodes = new Vector<> ();
		
		nodeSelectionListeners = new Vector<> ();
		
		buildUI();	
		
		instance = this;
	}
	
	private void buildUI() {
		rootNode = new DefaultMutableTreeNode(__("Train Graph"));
		comprehensiveViewNode = new DefaultMutableTreeNode(__("Comprehensive View"));
		globalSettingsNode = new DefaultMutableTreeNode(__("Settings"));
		railroadNetworkNode = new DefaultMutableTreeNode(__("Railroad Network"));
		railroadLinesNode = new DefaultMutableTreeNode(__("Railroad Lines"));
		trainTypesNode = new DefaultMutableTreeNode(__("Train Types"));
		timeTablesNode = new DefaultMutableTreeNode(__("Timetables"));
		remarksNode = new DefaultMutableTreeNode(__("Remarks"));
		
		
		rootNode.add(comprehensiveViewNode);
		rootNode.add(globalSettingsNode);
		rootNode.add(railroadNetworkNode);
		rootNode.add(railroadLinesNode);
		rootNode.add(trainTypesNode);
		rootNode.add(timeTablesNode);
		rootNode.add(remarksNode);

		timeTableNodes = new Vector<DefaultMutableTreeNode>();
		
		railroadLineTimeTableNodes = new Vector<DefaultMutableTreeNode>();
		
		setCellRenderer(new NavigatorTreeCellRenderer());
		
		setPreferredSize(new Dimension(200, 600));
		
		// Event handler for showing context menu
		addContextMenu();
		
		addTreeSelectionListener(this);
		
		treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);
	}
	
	public void expandAll(boolean expand) {
		expandAll(this, new TreePath(rootNode), true);
	}
	
	private void expandAll(JTree tree, TreePath parent, boolean expand) {

		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() > 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}

		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}  
	
	// {{ Train Type 右键菜单

	/**
	 * 添加右键菜单
	 */
	protected void addContextMenu() {
		trainTypePopupMenu = new JPopupMenu();
		trainTypePopupMenu.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

		miHideTrainType = createMenuItem(__("Hide Trains"), e -> toggleTrainTypeVisible(false));
		trainTypePopupMenu.add(miHideTrainType);
		
		miShowTrainType = createMenuItem(__("Show Trains"), e -> toggleTrainTypeVisible(true));
		trainTypePopupMenu.add(miShowTrainType);

		miHighLightTrainType = createMenuItem(__("Highlight Trains"), e -> toggleHighlightTrainType());
		trainTypePopupMenu.add(miHighLightTrainType);
		
		
		allTrainTypePopupMenu = new JPopupMenu();
		trainTypePopupMenu.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		
		miShowAllTrainTypes = createMenuItem(__("Show All Trains"), e -> toggleAllTrainTypeVisible(true));
		allTrainTypePopupMenu.add(miShowAllTrainTypes);
		
		miHideAllTrainTypes = createMenuItem(__("Hide All Trains"), e -> toggleAllTrainTypeVisible(false));
		allTrainTypePopupMenu.add(miHideAllTrainTypes);
		
		
		foldingNodesPopupMenu = new JPopupMenu();
		foldingNodesPopupMenu.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		
		miExpandAllNodes = createMenuItem(__("Expand All Nodes"), e -> toggleTreeNodeExpandingStatus(true));
		foldingNodesPopupMenu.add(miExpandAllNodes);
		
		miFoldAllNodes = createMenuItem(__("Fold All Nodes"), e -> toggleTreeNodeExpandingStatus(false));
		foldingNodesPopupMenu.add(miFoldAllNodes);
		

		// Event handler for showing pop up menu
		contextMenuAdapter = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int mods = e.getModifiers();
				// 鼠标右键
				if ((mods & InputEvent.BUTTON3_MASK) != 0) {
					// 选中鼠标位置处节点
					Point point = getLocationOnScreen();
					TreePath path = getPathForLocation(e.getX(), e.getY());
//					TreePath[] paths = getSelectionPaths();
					
					popupMenuX = e.getXOnScreen() - point.x;
					popupMenuY = e.getYOnScreen() - point.y;
					
//					setSelectionPath(path);
					nodeSelectionChanged(false, path);
				}
			}

		};
		
		addNodeSelectionChangedListener(this::onNodeChangedByRightButtonClick);
		addMouseListener(contextMenuAdapter);
	}

	/**
	 * 创建右键菜单的菜单项
	 * 
	 * @param name
	 * @param listener
	 * @return
	 */
	private JMenuItem createMenuItem(String name, ActionListener listener) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

		// menuItem.setActionCommand(actionCommand);
		if (listener != null)
			menuItem.addActionListener(listener);

		return menuItem;
	}
	
	private void onNodeChangedByRightButtonClick(boolean triggeredByLeftButton, TreePath treePath,
			NavigatorNodeType nodeType, int index, Object... params) {
		
		if (triggeredByLeftButton)
			return;
		
		// Reset selected Train type
		selectedTrainType = null;
		
		switch (nodeType) {
		case TRAIN_TYPES:
			showAllTrainTypePopupMenu(popupMenuX, popupMenuY);
			break;
		case TRAIN_TYPE_SPECIFIC:
			selectedTrainType = (TrainType) params[0];
			// 弹出菜单
			showTrainTypePopupMenu(popupMenuX, popupMenuY);
			break;
		default:
			if (!getModel().isLeaf(treePath.getLastPathComponent())) {
				foldingNodeTreePath = treePath;
				showFoldingNodesPopupMenu(popupMenuX, popupMenuY);
			}
			break;
		}
	}
	
	private void showTrainTypePopupMenu(int x, int y) {
		if (selectedTrainType == null)
			return;
		
		trainTypePopupMenu.show(Navigator.this, x, y);
	}
	
	private void toggleTrainTypeVisible(Boolean visible) {
		if (visible == null)
			selectedTrainType.visible = ! selectedTrainType.visible;
		else
			selectedTrainType.visible = visible;
		
		MainFrame.instance.runningChartView.updateTrainTypeDisplayOrder();
		MainFrame.instance.repaint();
	}
	
	private void toggleHighlightTrainType() {
//		if (selectedTrainType.visible)
		trainGraph.forEachTrainType(trainType -> trainType.visible = false);
		selectedTrainType.visible = true;

		MainFrame.instance.runningChartView.updateTrainTypeDisplayOrder();
		MainFrame.instance.repaint();
	}
	
	private void showAllTrainTypePopupMenu(int x, int y) {
		allTrainTypePopupMenu.show(Navigator.this, x, y);
	}
	
	private void toggleAllTrainTypeVisible(boolean visible) {
		trainGraph.forEachTrainType(trainType -> trainType.visible = visible);
		
		MainFrame.instance.runningChartView.updateTrainTypeDisplayOrder();
		MainFrame.instance.repaint();
	}
	
	private void showFoldingNodesPopupMenu(int x, int y) {
		foldingNodesPopupMenu.show(Navigator.this, x, y);
	}
	
	private void toggleTreeNodeExpandingStatus(Boolean expanded) {
		expandAll(this, foldingNodeTreePath, expanded);
	}
	
	// }}
	
	// {{ 设置TrainGraph模型, 以及根据TimeTables, RailNetwork, 或者Traintypes的变化更新的操作
	
	public void setTrainGraph(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		// invoked by updateNavigatorByRailNetwork()
//		updateNavigatorByTimetables();
		
		updateNavigatorByRailNetwork();
		
		updateNavigatorByTrainTypes();
	}

	public void updateNavigatorByTimetables() {
		DEBUG_MSG("可以优化, 参照updateNavigatorByRailNetwork");
		
		trainGraph.syncLineChartsWithRailNetworks();
		
		timeTableNodes.clear();
		timeTablesNode.removeAllChildren();
		trainGraph.allCharts().forEach(railnetworkChart -> {
			DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode(railnetworkChart);
			timeTableNodes.add(parentNode);
			timeTablesNode.add(parentNode);

			DefaultMutableTreeNode allTrainsNode = new DefaultMutableTreeNode(ALL_TRAIN_LABEL);
			parentNode.add(allTrainsNode);
			
			trainGraph.railNetwork.getAllRailroadLines().forEach(line -> {
				
				RailroadLineChart lineChart = railnetworkChart.findRailLineChart(line);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(lineChart);
				parentNode.add(node);
				
				node.add(new DefaultMutableTreeNode(DOWNWARD_LABEL));
				node.add(new DefaultMutableTreeNode(UPWARD_LABEL));
			});
		});
		
		refresh();
	}
	
	public void updateNavigatorByRailNetwork() {
		trainGraph.syncLineChartsWithRailNetworks();
		
		// Sync Railroad lines
		int raillineNodeCount = railroadLineNodes.size();
		int raillineCount = trainGraph.railNetwork.getAllRailroadLines().size();
		int i = 0, j = 0;
		for (; i < raillineNodeCount && j < raillineCount; ++i, ++j) {
			RailroadLine line = trainGraph.railNetwork.getRailroadLine(i);
			railroadLineNodes.get(i).setUserObject(line);
			
//			trainGraph.getCharts().for
//			RailroadLineChart lineChart = trainGraph.get
		}
		if (raillineNodeCount > raillineCount) {
			for (int k = raillineNodeCount - 1; k >= raillineCount; -- k) {
				DefaultMutableTreeNode node = railroadLineNodes.get(k);
				railroadLineNodes.remove(k);
				railroadLinesNode.remove(node);
			}
		} else if (raillineNodeCount < raillineCount) {
			for (int k = raillineNodeCount; k < raillineCount; ++ k) {
				RailroadLine line = trainGraph.railNetwork.getRailroadLine(k);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(line);
				railroadLineNodes.add(node);
				railroadLinesNode.add(node);
			}
		}
		
		// Sync Railroad network chart
		updateNavigatorByTimetables();
		
		refresh();
	}

	public void updateNavigatorByTrainTypes() {
		trainTypeNodes.clear();
		trainTypesNode.removeAllChildren();
		
		trainGraph.forEachTrainType(trainType -> {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(trainType);
			trainTypeNodes.add(node);
			trainTypesNode.add(node);
		});
		
		refresh();
	}
	
	public void refresh() {
		expandAll(true);
		updateUI();
	}
	
	// }}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		nodeSelectionChanged(true, e.getNewLeadSelectionPath());
	}
	
	public void nodeSelectionChanged(boolean triggedByLeftButton, TreePath path) {
		
//		TreePath path = e.getNewLeadSelectionPath();
		if (path == null)
			return;
		
		Object node = path.getLastPathComponent();
		NavigatorNodeType nodeType = NavigatorNodeType.UNKNOWN;
		int index = -1;
		Object[] params = new Object[0];
		
		if(node == rootNode) {
			nodeType = NavigatorNodeType.ROOT;
		} else if (node == comprehensiveViewNode) {
			nodeType = NavigatorNodeType.COMPREHENSIVE_VIEW;
		} else if (node == globalSettingsNode) {
			nodeType = NavigatorNodeType.GLOBAL_SETTINGS;
		} else if (node == railroadNetworkNode) {
			nodeType = NavigatorNodeType.RAILROAD_NETWORK;
		} else if (node == railroadLinesNode) {
			nodeType = NavigatorNodeType.RAILROAD_LINES;
		} else if (node == trainTypesNode) {
			nodeType = NavigatorNodeType.TRAIN_TYPES;
		} else if (node == timeTablesNode) {
			nodeType = NavigatorNodeType.TIME_TABLES;
		} else if (node == remarksNode) {
			nodeType = NavigatorNodeType.REMARKS;
		} else {
			int pathNodeCount = path.getPathCount();
			Object parentNode, grandParentNode, grandGrandParentNode;
			
			if (pathNodeCount > 4) {
				grandGrandParentNode = path.getPathComponent(pathNodeCount - 4);
				grandParentNode = path.getPathComponent(pathNodeCount - 3);
				parentNode = path.getPathComponent(pathNodeCount - 2);
				if (grandGrandParentNode == timeTablesNode) {
					Object objInNode = ((DefaultMutableTreeNode) node).getUserObject();
					if (objInNode == DOWNWARD_LABEL) {
						nodeType = NavigatorNodeType.TIME_TABLE_LINE_DOWN;
					} else if (objInNode == UPWARD_LABEL) {
						nodeType = NavigatorNodeType.TIME_TABLE_LINE_UP;
					}
					// get model node
					params = new Object[2];
					params[0] = ((DefaultMutableTreeNode) parentNode).getUserObject();
					params[1] = ((DefaultMutableTreeNode) grandParentNode).getUserObject();
				}
			} else if (pathNodeCount > 3) {
				grandParentNode = path.getPathComponent(pathNodeCount - 3);
				parentNode = path.getPathComponent(pathNodeCount - 2);
				if (grandParentNode == timeTablesNode) {
					// get model node
					params = new Object[2];
					params[0] = ((DefaultMutableTreeNode) node).getUserObject();
					params[1] = ((DefaultMutableTreeNode) parentNode).getUserObject();
					
					if (params[0] instanceof RailroadLineChart)
						nodeType = NavigatorNodeType.TIME_TABLE_LINE;
					else
						nodeType = NavigatorNodeType.RAILNETWORK_ALL_TRAINS;
				}
			} else if (pathNodeCount > 2) {
				parentNode = path.getPathComponent(pathNodeCount - 2);
				if (parentNode == railroadLinesNode) {
					nodeType = NavigatorNodeType.RAILROAD_LINE_SPECIFIC;
					// get model node
					params = new Object[1];
					params[0] = ((DefaultMutableTreeNode) node).getUserObject();
				} else if (parentNode == timeTablesNode) {
					nodeType = NavigatorNodeType.TIME_TABLE_SPECIFIC;
					// get model node
					params = new Object[1];
					params[0] = ((DefaultMutableTreeNode) node).getUserObject();
				} else if (parentNode == trainTypesNode) {
					nodeType = NavigatorNodeType.TRAIN_TYPE_SPECIFIC;
					// get model node
					params = new Object[1];
					params[0] = ((DefaultMutableTreeNode) node).getUserObject();
				}
			}
		}
//		NavigatorNodeType nodeType0 = nodeType;
//		Object[] params0 = params;
//		Runnable switchTask = () -> fireNodeSelectionChanged(triggedByLeftButton, path, nodeType0, index, params0);
		
		fireNodeSelectionChanged(triggedByLeftButton, path, nodeType, index, params);
	}
	
	public void addNodeSelectionChangedListener(NavigatorNodeSelectionListener listener) {
		nodeSelectionListeners.add(listener);
	}

	private void fireNodeSelectionChanged(Boolean triggedByLeftButton, TreePath treePath,
			NavigatorNodeType nodeType, int index, Object... params) {
		
		nodeSelectionListeners.forEach(listener -> 
		listener.onNavigatorNodeChanged(triggedByLeftButton, treePath, nodeType, index, params));
	}
}
