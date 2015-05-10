package org.paradise.etrc.view.nav;

import static org.paradise.etrc.ETRC.__;
import static org.paradise.etrc.ETRCUtil.*;

import java.util.Hashtable;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.paradise.etrc.data.RailNetwork;
import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.data.RailroadLineChart;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.data.event.RailroadLineChangeType;

import com.sun.org.apache.bcel.internal.generic.RETURN;

/**
 * 主界面导航栏
 * @author Jeff Gong
 *
 */
public class Navigator extends JTree {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2262708630560630874L;
	
	public static enum NavigatorNodeType {
		UNKNOWN, ROOT,
		COMPREHENSIVE_VIEW, GLOBAL_SETTINGS, RAILROAD_NETWORK, RAILROAD_LINES,
		RAILROAD_LINE_SPECIFIC, TRAIN_TYPES, TRAIN_TYPE_SPECIFIC,
		ALL_TRAINS, TIME_TABLES, TIME_TABLE_SPECIFIC, TIME_TABLE_LINE,
		TIME_TABLE_LINE_DOWN, TIME_TABLE_LINE_UP, 
		REMARKS
		};
		
	@FunctionalInterface
	public static interface NavigatorNodeSelectionListener {
		public void onNavigatorNodeChanged(NavigatorNodeType nodeType, 
				int index, Object... param);
	}

	static final String DOWNWARD_LABEL = __("DOWNWARD");
	static final String UPWARD_LABEL = __("UPWARD");
	
	
	protected DefaultTreeModel treeModel;
	
	protected DefaultMutableTreeNode rootNode;
	protected DefaultMutableTreeNode comprehensiveViewNode;
	protected DefaultMutableTreeNode globalSettingsNode;
	protected DefaultMutableTreeNode railroadNetworkNode;
	protected DefaultMutableTreeNode railroadLinesNode;
	protected DefaultMutableTreeNode trainTypesNode;
	protected DefaultMutableTreeNode allTrainsNode;
	protected DefaultMutableTreeNode timeTablesNode;
	protected DefaultMutableTreeNode remarksNode;
	
	protected Vector<DefaultMutableTreeNode> railroadLineNodes;
	protected Vector<DefaultMutableTreeNode> timeTableNodes;
	protected Vector<DefaultMutableTreeNode> railroadLineTimeTableNodes;
	
	public NavigatorNodeSelectionListener nodeSelectionListener;
	protected TrainGraph trainGraph;

	public Navigator() {
		railroadLineNodes = new Vector<DefaultMutableTreeNode> ();
		timeTableNodes = new Vector<DefaultMutableTreeNode> ();
		railroadLineTimeTableNodes = new Vector<DefaultMutableTreeNode> ();
		
		buildUI();		
	}
	
	private void buildUI() {
		rootNode = new DefaultMutableTreeNode(__("Train Graph"));
		comprehensiveViewNode = new DefaultMutableTreeNode(__("Comprehensive View"));
		globalSettingsNode = new DefaultMutableTreeNode(__("Global Settings"));
		railroadNetworkNode = new DefaultMutableTreeNode(__("Railroad Network"));
		railroadLinesNode = new DefaultMutableTreeNode(__("Railroad Lines"));
		trainTypesNode = new DefaultMutableTreeNode(__("Train Types"));
		allTrainsNode = new DefaultMutableTreeNode(__("All Trains"));
		timeTablesNode = new DefaultMutableTreeNode(__("Timetables"));
		remarksNode = new DefaultMutableTreeNode(__("Remarks"));
		
		
		rootNode.add(comprehensiveViewNode);
		rootNode.add(globalSettingsNode);
		rootNode.add(railroadNetworkNode);
		rootNode.add(railroadLinesNode);
		rootNode.add(trainTypesNode);
		rootNode.add(allTrainsNode);
		rootNode.add(timeTablesNode);
		rootNode.add(remarksNode);

		timeTableNodes = new Vector<DefaultMutableTreeNode>();
		
		railroadLineTimeTableNodes = new Vector<DefaultMutableTreeNode>();
		
		treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);
		
		// Event handler
		addTreeSelectionListener(
				(TreeSelectionEvent e) -> nodeSelectionChanged(e) );
	}
	
	public void setTrainGraph(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		updateNavigatorByTimetables();
		
		updateNavigatorByRailNetwork();
		
		updateNavigatorByTrainTypes();
	}
	
	public void updateNavigatorByTimetables() {
		DEBUG_MSG("可以优化, 参照updateNavigatorByRailNetwork");
		
		trainGraph.syncLineChartsWithRailNetworks();
		
		timeTableNodes.clear();
		timeTablesNode.removeAllChildren();
		trainGraph.getCharts().forEach(railnetworkChart -> {
			DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode(railnetworkChart);
			timeTableNodes.add(parentNode);
			timeTablesNode.add(parentNode);
			
			trainGraph.railNetwork.getAllRailroadLines().forEach(line -> {
				
				RailroadLineChart lineChart = railnetworkChart.findRailLineChart(line);
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(lineChart);
				parentNode.add(node);
				
				node.add(new DefaultMutableTreeNode(DOWNWARD_LABEL));
				node.add(new DefaultMutableTreeNode(UPWARD_LABEL));
			});
		});
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
		
		updateUI();
	}

	public void updateNavigatorByTrainTypes() {
		
	}
	
	public void nodeSelectionChanged(TreeSelectionEvent e) {
		if (nodeSelectionListener == null)
			return;
		
		TreePath path = e.getNewLeadSelectionPath();
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
		} else if (node == allTrainsNode) {
			nodeType = NavigatorNodeType.ALL_TRAINS;
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
					nodeType = NavigatorNodeType.TIME_TABLE_LINE;
					// get model node
					params = new Object[2];
					params[0] = ((DefaultMutableTreeNode) node).getUserObject();
					params[1] = ((DefaultMutableTreeNode) parentNode).getUserObject();
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
				}
			}
		}
		
		nodeSelectionListener.onNavigatorNodeChanged(nodeType, index, params);
	}

}
