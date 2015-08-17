package org.paradise.etrc.view.runningchart.dynamic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.paradise.etrc.data.v1.ChartSettings;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;

import static org.paradise.etrc.ETRC.__;


public class RunningPanel extends JPanel {
	private static final long serialVersionUID = -2001053748609300887L;

	private static final int SPACE_BETWEEN_RAIL = 20;
	private static final int SPACE_AT_STATION = 9;

	private RailroadLineChart chart;
	private Train activeTrain;
	
	private DynamicView dView;
	private JPopupMenu popupMenu;
	private JMenuItem popupMenuStation;

	private ChartSettings settings;
	

	public RunningPanel(TrainGraph trainGraph, DynamicView _dView) {
		dView = _dView;
		setModel(trainGraph);

		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.settings = trainGraph.settings;
		this.chart = trainGraph.currentLineChart;
	}

	public void setActiveTrain(Train activeTrain) {
		this.activeTrain = activeTrain;
	}
	
	void jbInit() throws Exception {
		this.setBackground(Color.white);
		this.setLayout(new BorderLayout());
		this.setFont(new Font(__("FONT_NAME"), 0, 12));
		buildPopupMenu();
	}

	private void buildPopupMenu() {
		popupMenu = new JPopupMenu();
		popupMenu.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		
		popupMenuStation = createMenuItem("Station Name", null);
		popupMenu.add(popupMenuStation);
		popupMenu.add(new JSeparator(JSeparator.HORIZONTAL));
		popupMenu.add(createMenuItem(__("Add to projection"), e->doPopupMenu_AddToProjection()));
		popupMenu.add(createMenuItem(__("Remove from projection"), e->doPopupMenu_RemoveFromProjection()));
		
		//add(popupMenu);
		
		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int mods=e.getModifiers();
				//鼠标右键
				if((mods&InputEvent.BUTTON3_MASK)!=0) {
					//弹出菜单
					String stationName = dView.getStationName(e.getX());
					popupMenuStation.setText(stationName);
					popupMenu.show(RunningPanel.this, e.getX(), e.getY());
				}
			}
			
		});
	}

	private Object doPopupMenu_AddToProjection() {
		return null;
	}

	private Object doPopupMenu_RemoveFromProjection() {
		return null;
	}

	private JMenuItem createMenuItem(String name, ActionListener listener) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));

//		menuItem.setActionCommand(actionCommand);
		if (listener != null)
			menuItem.addActionListener(listener);

		return menuItem;
	}

	public void paint(Graphics g) {
		super.paint(g);
		
		// 文字防锯齿
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		//上行线
		drawRailwayLine(g, 0, dView.topMargin, getPreferredSize().width, 0);
		drawRailwayLine(g, 0, dView.topMargin+4, getPreferredSize().width, 0);
		//下行线
		drawRailwayLine(g, 0, this.getHeight() - dView.bottomMargin, getPreferredSize().width, 1);
		drawRailwayLine(g, 0, this.getHeight() - dView.bottomMargin - 4, getPreferredSize().width, 1);
		
		if (chart == null)
			return;

		if (chart.railroadLine != null)
			drawStationsLine(g);
		
		drawTrains(g);
	}

	public Dimension getPreferredSize() {
		int w, h;
		w = chart.railroadLine.length * dView.scale + dView.leftMargin + dView.rightMargin;
		h = dView.runningPanelHeight;
		return new Dimension(w, h);
	}
	
	//画铁路效果线
	//isWhiteFirst = 1 时先画白色 = 0 时先画黑色
	private void drawRailwayLine(Graphics g, int x, int y, int width, int isWhiteFirst) {
		Color oldColor = g.getColor();
		g.setColor(Color.lightGray);

		g.drawLine(x, y - 1, width, y - 1);
		g.drawLine(x, y + 1, width, y + 1);

		int gap = 20;
		for(int i = x; i < width; i += gap) {
			if(i / gap % 2 == isWhiteFirst) {
				if(i + gap <= width)
					g.fillRect(i, y - 1, gap, 3);
				else
					g.fillRect(i, y - 1, width - i + 1, 3);
			}
		}
		
		g.setColor(oldColor);
	}
	
	//车站线
	private void drawStationsLine(Graphics g) {
		Color oldColor = g.getColor();
				
		g.setColor(Color.lightGray);
		g.drawLine(dView.leftMargin, 
				   this.getHeight()/2, 
				   this.getPreferredSize().width - dView.rightMargin, 
				   this.getHeight()/2);
		
		g.setColor(oldColor);

		for (int i = 0; i < chart.railroadLine.getStationNum(); i++) {
			drawStation(g, chart.railroadLine.getStation(i));
		}
	}
	
	private void drawStation(Graphics g, Station station) {
		if (station.hide)
			return;

		int x = station.dist * dView.scale + dView.leftMargin;
		
		int y1 = 0;
		int y2 = getHeight();

		if (station.level <= settings.displayLevel) {
			Color oldColor = g.getColor();
			//设置坐标线颜色
			if (station.level <= settings.boldLevel) {
				g.setColor(new Color(64, 64, 64));
			}
			else {
				g.setColor(new Color(192, 192, 192));
			}
					
			//画坐标线
			g.drawLine(x, y1, x, y2);
			//恢复原色
			g.setColor(oldColor);

			//画站名
			drawStationName(g, station, x);
		}
	}

	//站名
	private void drawStationName(Graphics g, Station station, int x) {
		Color oldColor = g.getColor();

		Rectangle r = g.getFontMetrics().getStringBounds(station.getName(), g).getBounds();
		int left = x - r.width / 2;
		int top = getHeight() / 2 + r.height / 2 - 2;

		g.setColor(Color.white);
		g.fillRect(left - 1, top - r.height + 2, r.width + 2, r.height);
		
		g.setColor(Color.black);
		g.drawString(station.getName(), left, top);
		g.setColor(oldColor);
	}
	
	private Hashtable<Train, String> runningTrains = new Hashtable<Train, String>();
	private Hashtable<Train, String>  atStationTrains = new Hashtable<Train, String> ();
	
	private void drawTrains(Graphics g) {
		//找出停靠中的车
		atStationTrains = findTrainsAtStation(dView.getCurrentTime());
//		System.out.println(atStationTrains.size());
		//遍历一下，看看哪些车是运行中的
		runningTrains.clear();
		for(int i = 0; i < chart.getTrainNum(); i++) {
			int dist = chart.railroadLine.getDistOfTrain(chart.getTrain(i), dView.getCurrentTime());
			
			if(dist >= 0) {
				if(!atStationTrains.keySet().contains(chart.getTrain(i)))
					runningTrains.put(chart.getTrain(i), "" + dist);
			}
			
//			String station = chart.circuit.getStationName(dist, true);
////			System.out.println(dist + ":" + station);
//			if(station.endsWith("附近")) {
//				runningTrains.put(chart.trains[i], "" + dist);
////				System.out.println("R: " + chart.trains[i].getTrainName());
//			}
//			else {
//				atStationTrains.put(chart.trains[i], station);
////				System.out.println("A: " + chart.trains[i].getTrainName());
//			}
		}
		
		//画运行中的列车
		Enumeration<Train> en = runningTrains.keys();
		while(en.hasMoreElements()) {
			Train theRunningTrain = (Train) en.nextElement();
			int theDist = Integer.parseInt((String) runningTrains.get(theRunningTrain));
			if(theRunningTrain.isDownTrain(chart.railroadLine) == Train.DOWN_TRAIN) {
				drawRunningTrainDown(g, theRunningTrain, theDist);
			}
			else {
				drawRunningTrainUp(g, theRunningTrain, theDist);
			}
		}

		//画停站的列车
		for(int i=0; i<chart.railroadLine.getStationNum(); i++) {
			String theStation = chart.railroadLine.getStation(i).getName();
			int stationDist = chart.railroadLine.getStation(i).dist;
			Vector<Train> myTrains = new Vector<Train>();
			
			Enumeration<Train> stoped = atStationTrains.keys();
			while(stoped.hasMoreElements()) {
				Train theTrain = (Train) stoped.nextElement();
				String sta = (String) atStationTrains.get(theTrain);
				
				if(sta.equalsIgnoreCase(theStation))
					myTrains.add(theTrain);
			}
			
			drawTrainsAtStation(g, myTrains, stationDist);
		}
	}
	
	//找出停靠于本线区间车站上的列车
	private Hashtable<Train, String> findTrainsAtStation(int time) {
		Hashtable<Train, String> trains = new Hashtable<Train, String>();
		for(int i = 0; i < chart.getTrainNum(); i++) {
			String station = chart.railroadLine.getStationNameAtTheTime(chart.getTrain(i), time);
			
			if(!station.equalsIgnoreCase("")) {
				trains.put(chart.getTrain(i), station);
			}
		}
		return trains;
	}

	private void drawTrainsAtStation(Graphics g, Vector<Train> myTrains, int dist) {
		int dNum = 0;
		int uNum = 0;
		for(int i = 0; i < myTrains.size(); i++) {
			Train train = (Train) myTrains.get(i);
			
			if(train.isDownTrain(chart.railroadLine) == Train.DOWN_TRAIN) {
				drawTrainAtStationDown(g, dNum, train, dist);
				dNum ++;
			}
			else {
				drawTrainAtStationUp(g, uNum, train, dist);
				uNum ++;
			}
		}
	}

	private void drawTrainRect(Graphics g, Train train, int x, int y) {
		g.setColor(train.trainType.getLineColor());
		g.fillRect(x-5, y-2, 11, 5);

		//当前选中的车次
		try {
			if(train.equals(activeTrain)) {
				g.drawRect(x-7, y-4, 14, 8);
				g.drawLine(x, 0, x, getHeight());
			}
		}
		catch(NullPointerException npe) {
			
		}
	}

	private void drawTrainAtStationUp(Graphics g, int i, Train train, int dist) {
		int x = dView.getPelsX(dist);
		int y = getHeight() - dView.bottomMargin + SPACE_BETWEEN_RAIL + i * SPACE_AT_STATION;
		// MOD  int y = getHeight() - dView.bottomMargin + (i+1) * SPACE_AT_STATION;

		drawTrainRect(g, train, x, y);
		
		String trainName = train.getTrainName(chart.railroadLine);
		
		Font oldFont = g.getFont();
		g.setFont(new Font("Dialog", Font.PLAIN, 10));
		
		// MOD   int w = (int) g.getFontMetrics().getStringBounds(trainName, g).getWidth();
		// g.drawString(trainName, x - w/2, y + 13);
		g.drawString(trainName, x + 9, y + 5);
		
		g.setFont(oldFont);
	}

	private void drawTrainAtStationDown(Graphics g, int i, Train train, int dist) {
		int x = dView.getPelsX(dist);
		//MOD   int y = dView.topMargin - (i+1) * SPACE_AT_STATION;
		int y = dView.topMargin - SPACE_BETWEEN_RAIL -  i * SPACE_AT_STATION;
		
		drawTrainRect(g, train, x, y);

		String trainName = train.getTrainName(chart.railroadLine);
		
		Font oldFont = g.getFont();
		g.setFont(new Font("Dialog", Font.PLAIN, 10));
		
		// int w = (int) g.getFontMetrics().getStringBounds(trainName, g).getWidth();
		//MOD   g.drawString(trainName, x - w/2, y - 5);
		g.drawString(trainName, x + 9, y + 5);
		
		g.setFont(oldFont);
	}

	private void drawRunningTrainUp(Graphics g, Train train, int dist) {
		int x = dView.getPelsX(dist);
		int y = getHeight() - dView.bottomMargin;
		
		drawTrainRect(g, train, x, y);
		
		String trainName = train.getTrainName(chart.railroadLine);
		
		Font oldFont = g.getFont();
		g.setFont(new Font("Dialog", Font.PLAIN, 10));
		
		int w = (int) g.getFontMetrics().getStringBounds(trainName, g).getWidth();
		g.drawString(trainName, x - w/2, y + 13);
		
		g.setFont(oldFont);
	}
	
	private void drawRunningTrainDown(Graphics g, Train train, int dist) {
		int x = dView.getPelsX(dist);
		int y = dView.topMargin;
		
		drawTrainRect(g, train, x, y);
		
		String trainName = train.getTrainName(chart.railroadLine);
		
		Font oldFont = g.getFont();
		g.setFont(new Font("Dialog", Font.PLAIN, 10));
		
		int w = (int) g.getFontMetrics().getStringBounds(trainName, g).getWidth();
		g.drawString(trainName, x - w/2, y - 5);
		
		g.setFont(oldFont);
	}
}