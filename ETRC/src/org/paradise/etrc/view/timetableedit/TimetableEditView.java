package org.paradise.etrc.view.timetableedit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.view.IView;

import com.zearon.util.interface_.IMultiInheritance;
import com.zearon.util.ui.databinding.UIBindingManager;
import com.zearon.util.ui.widget.StatusBar;

import static org.paradise.etrc.ETRC.__;

@SuppressWarnings("serial")
public class TimetableEditView extends JPanel implements IView {
	
	public static final Color CROSSOVER_STATION_COLOR = Color.YELLOW;
	public static final Color lineBK1 = Color.white;
	public static final Color lineBK2 = new Color(224, 255, 255);
	public static final Color startStationBK = new Color(224, 255, 255);
	public static final Color terminalStationBK = Color.decode("#ffeceb");
	public static final Color headerBK = new Color(255, 224, 224);
	public static final Color selectBK = new Color(192, 192, 255);
	
	
	private JToolBar toolBar;
	
	private boolean ui_inited;
	private JButton btnNewButton;
	private JButton btnNewButton_1;
	
	TimetableEditSheetTable table;

	private TrainGraph trainGraph;
	private RailroadLineChart lineChart;
	private boolean downGoing;
	
	/**
	 * Create the panel.
	 */
	public TimetableEditView(TrainGraph trainGraph) {
		setModel(trainGraph);
		
		initUI();
		UIDB_updateUI(null);
		
		ui_inited = true;
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		lineChart = trainGraph.currentLineChart;
		
		if (ui_inited) {
			// update ui
			UIDB_updateUI(null);
			
			table.setTrainGraph(trainGraph);
		}
	}
	
	public void switchChart(boolean downGoing) {
		lineChart = trainGraph.currentLineChart;
		this.downGoing = downGoing;
		table.switchChart(downGoing);
		
		// update ui:
		UIDB_updateUI(null);
		table.setupColumnWidth();
		repaint();
	}
	
	public void refreshChart() {
		table.switchChart(downGoing);
		
		// update ui:
		UIDB_updateUI(null);
		table.updateData();
		repaint();
	}
	
	public void refreshColumn(int column) {
		table.refreshColumn(column);
	}
	
	public void refreshStopCell(int row, int column) {
		table.refreshStopCell(row, column);
	}

	private void initUI() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel statusPanel = new JPanel();
		add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new BorderLayout(0, 0));
		
		statusPanel.add(getStatusBar());
		
//		timeTableScrollPane = new JScrollPane();
//		add(timeTableScrollPane, BorderLayout.CENTER);
		
		toolBar = getToolBar();
		add(toolBar, BorderLayout.NORTH);
		
		btnNewButton = createToolBarButton(JButton.class, __("Create data for imported trains"), 
				__("Create data for imported trains"), this::do_CreateDataForImportedTrains);
		toolBar.add(btnNewButton);
		
		btnNewButton_1 = createToolBarButton(JButton.class, "New button 2", "New button 2", ()->{});
		toolBar.add(btnNewButton_1);
//		toolBar.add(btnNewButton_1);
		
		buildTable();
	}
	
	private void buildTable() {
		table = new TimetableEditSheetTable(trainGraph, this);
		table.getContainerPanel().setBorder(BorderFactory.createEmptyBorder());
		
		add(table.getContainerPanel(), BorderLayout.CENTER);
	}
	
	private void do_CreateDataForImportedTrains() {
		lineChart.createTrainRouteSectionsForAllTrains();
		table.updateData();
	}
	
	
	// {{ Data Bindings
	{
		IMI_initInterface();
	}
	@Override
	public IMultiInheritance IMI_getThisObject() {
		return this;
	}
	
	@Override
	public Object UIDB_getModelObject(String objectID) {
		if ("chartSettings".equals(objectID)) {
			return trainGraph.settings;
		} else if ("chart".equals(objectID)) {
			return trainGraph.currentLineChart;
		}
		
		return null;
	}

	@Override
	public String UIDB_getPropertyDesc(String propertyName) {
		String desc = "";
		
		if ("distScale".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("pixels/Unit") );
		} else if ("displayLevel".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("minimal station level to be displayed") );
		} else if ("boldLevel".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("level of station as bold line") );
		} else if ("startHour".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("starting hour") );
		} else if ("minuteScale".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("pixels/minute") );
		} else if ("timeInterval".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("minutes/x-axis gap") );
		} else if ("distUnit".equals(propertyName)) {
			desc = String.format(__("%s of running chart"), __("distance unit") );
		}
		
		else if ("AutoLoadLastFile".equals(propertyName)) {
			desc = String.format(__("%s in global settings"), __("auto load last file value") );
		} else if ("HttpProxyUse".equals(propertyName)) {
			desc = String.format(__("%s in global settings"), __("use http proxy value") );
		} else if ("HttpProxyServer".equals(propertyName)) {
			desc = String.format(__("%s in global settings"), __("http proxy server value") );
		}else if ("HttpProxyPort".equals(propertyName)) {
			desc = String.format(__("%s in global settings"), __("http port value") );
		}
		
		return desc;
	}

	@Override
	public void UIDB_updateUIforModel(String objectID) {
		if ("chartSettings".equals(objectID)) {
			// mainFrame.chartView.updateData();
			// mainFrame.runView.updateUI();
		} else if ("chart".equals(objectID)) {
		}
	}

	@Override
	public JMenu getEditMenu() {
		return table.getEditMenu();
	}
	
	// }}
	
}
