package org.paradise.etrc.view.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.v1.ChartSettings;
import org.paradise.etrc.data.v1.RailroadLine;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.view.chart.traindrawing.DrawingModel;
import org.paradise.etrc.view.chart.traindrawing.TrainDrawing;

/**
 * @author lguo@sina.com
 * @version 1.0
 */
public class ChartView extends JPanel {
	/*
	 * X轴（时间轴）
	 * 名称        类型   单位
	 * clock       Date
	 * coordinate  int    分钟    = clock - startHour
	 * point.x     int    像素    = coordinate * timeScale + leftMargin
	 *
	 * Y轴
	 * 名称         类型  单位
	 * dist        int   公里
	 * point.y     int   像素     = dist * distScale + topMargin
	 */
	private static final long serialVersionUID = -2507031211251679563L;

	public static final int DEFAULT_LEFT_MARGIN = 45;
	public static final int DEFAULT_RIGHT_MARGIN = 45;
	public static final int DEFAULT_TOP_MARGIN = 66;
	public static final int DEFAULT_BOTTOM_MARGIN = 66;

	public int leftMargin = DEFAULT_LEFT_MARGIN;
	public int rightMargin = DEFAULT_RIGHT_MARGIN;
	public int topMargin = DEFAULT_TOP_MARGIN;
	public int bottomMargin = DEFAULT_BOTTOM_MARGIN;

	public static int trainNameRecMargin = 1;
	public static int trainNameRecHeight = 54;
	public static int circuitPanelWidth = 80;
	public static int clockPanelHeight = 16;

	public static final int ScrollUnitIncrement = 16;

	boolean shouldUpdateDrawingModel = true;
//	public TrainDrawing activeTrainDrawing;
//	Vector<TrainDrawing> normalDrawings = new Vector<TrainDrawing>();
//	Vector<TrainDrawing> underDrawings  = new Vector<TrainDrawing>();
	
	HashMap<Integer, DrawingModel> drawingModelMap = new HashMap<Integer, DrawingModel> ();
	DrawingModel currentDrawingModel;

	public CircuitPanel panelCircuit;
	public LinesPanel panelLines;
	public ClockPanel panelClock;

	private JScrollPane spLines = new JScrollPane();

	public Color gridColor = new Color(0xE0E0E0); //MOD Color.GRAY;
	public Color activeGridColor = Color.DARK_GRAY;
	

	public TrainGraph trainGraph;
	public ChartSettings settings;
	public RailroadLineChart activeLineChart;
	
	public Train activeTrain;
	public Station activeStation;
	
	public MainFrame mainFrame;

	private boolean ui_inited;

	public ChartView(TrainGraph trainGraph, MainFrame _mainFrame) {
		mainFrame = _mainFrame;
		
		setModel(trainGraph);
		
		panelCircuit = new CircuitPanel(trainGraph, this);
		panelClock = new ClockPanel(trainGraph, this);
		panelLines = new LinesPanel(trainGraph, this);
		
		try {
			jbInit();
			ui_inited = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		activeLineChart = trainGraph.currentLineChart;
		this.settings = trainGraph.settings;
		
		currentDrawingModel = new DrawingModel();
		drawingModelMap.put(activeLineChart.getID(), currentDrawingModel);
		
		activeTrain = null;
		
		if (ui_inited) {
			panelCircuit.setModel(trainGraph);
			panelClock.setModel(trainGraph);
			panelLines.setModel(trainGraph);
			cornerControl.setModel(trainGraph);
		
			shouldUpdateDrawingModel = true;
			repaint();
		}
	}
	
	public void switchLineChart() {
		activeLineChart = trainGraph.currentLineChart;
		
		currentDrawingModel = drawingModelMap.get(activeLineChart.getID());
		if (currentDrawingModel == null) {
			currentDrawingModel = new DrawingModel();
			drawingModelMap.put(activeLineChart.getID(), currentDrawingModel);
		
			shouldUpdateDrawingModel = true;
		} else {
			shouldUpdateDrawingModel = currentDrawingModel.empty;
		}
		
		if (ui_inited) {
			if (currentDrawingModel.empty || activeLineChart.chartChanged) {
				updateData();
			}
			
			panelCircuit.setModel(trainGraph);
			panelClock.setModel(trainGraph);
			panelLines.setModel(trainGraph);
			cornerControl.setModel(trainGraph);
			repaint();
		}
	}

	public boolean findAndMoveToTrain(String trainName) {
//		System.out.println("findAndMoveToTrain:" + trainName);
//		
		boolean found = false;
		TrainDrawing trainDrawing = currentDrawingModel.findTrainDrawingByName(trainName);
		if (trainDrawing != null) {
			found = true;
			panelLines.moveToTrainDrawing(trainDrawing);
		}

	    return found;
	}
	
	/**
	 * moveToTrain
	 *
	 * @param loadingTrain Train
	 */
	private void moveToTrain(Train train) {
//		System.out.println("moveToTrain:" + train.getTrainName());
//		
		if (train == null)
			return;
		
		TrainDrawing trainDrawing = currentDrawingModel.findTrainDrawing(train);
		panelLines.moveToTrainDrawing(trainDrawing);
	}
	
	public void setActiveTrain(Train train) {
		activeTrain = train;
		
		// 设置MainFrame的标题和ToolTip
		if (activeTrain != null) {
			mainFrame.setActiceTrainName(activeTrain.getTrainName(trainGraph.currentLineChart.railroadLine));
			//ADD For SheetView
			mainFrame.sheetView.selectTrain(activeTrain);
			//打开ToolTip
			panelLines.setToolTipText("");
		} else {
			mainFrame.setActiceTrainName("");
			//关闭ToolTip
			panelLines.setToolTipText(null);
		}
		
		currentDrawingModel.setActiveTrain(train, showUpDownState);

		// 重绘
		repaint();
	}

	public void updateDrawingModel() {
		if (shouldUpdateDrawingModel) {
			shouldUpdateDrawingModel = false;
		} else {
			return;
		}
		
		currentDrawingModel.updateCurrentCircuit(activeLineChart, activeLineChart.railroadLine, settings, this);
		currentDrawingModel.updateUpDownTrainOption(showUpDownState);
		currentDrawingModel.setActiveTrain(activeTrain, showUpDownState);
		
		currentDrawingModel.empty = false;
	}
	
	public void updateTrainTypeDisplayOrder() {
		currentDrawingModel.updateTrainTypeDisplayOrder();
	}

	//水印颜色
	public final static Color DEFAULT_UNDER_COLOR = new Color(220, 220, 220);

	//当水印色为null的时候不画反向车次(也作为反向车次是否能被选择的依据)
	public Color underDrawingColor = DEFAULT_UNDER_COLOR;
	
	//普通图线的到发点是否画出
	//在有详细点单数据的时候所有通过点都画出会影响图的美观。
	//不管该选项是否开启，当前选中车次的到发点仍然会画出来。
	public boolean isDrawNormalPoint = true;

	/**
	 * 从相对时间计算X坐标
	 * @param coordinate int
	 * @return int
	 */
	public int getPelsX(int coordinate) {
		return Math.round (coordinate * settings.minuteScale) + leftMargin;
	}

	/**
	 * 从绝对时间计算X坐标
	 * @param clock Date
	 * @return int
	 */
	public int getPelsX(String clock) {
		return getPelsX(getCoordinate(clock));
	}

	/**
	 * 从距离值计算Y坐标
	 * @param dist int
	 * @return int
	 */
	public int getPelsY(int dist) {
		return Math.round(dist * settings.distScale) + topMargin;
	}

	/**
	 * 从站名计算Y坐标
	 * @param stationName String
	 * @return int
	 */
	public int getPelsY(String stationName) {
		return getPelsY(trainGraph.currentLineChart.railroadLine.getStationDist(stationName));
	}

	/**
	 * 从Y坐标计算距离值
	 * @param py int
	 * @return int
	 */
	public int getDist(int py) {
		return Math.round((py - topMargin) / settings.distScale);
	}

	public String getStationName(int py) {
		return trainGraph.currentLineChart.railroadLine.getStationName(getDist(py));
	}

	public int getMinute(int px) {
		return Math.round ((px - leftMargin) / settings.minuteScale);
	}

	/**
	 * 从绝对时间计算相对时间
	 * @param time Date
	 * @return int
	 */
	public int getCoordinate(String time) {
//		SimpleDateFormat dfHour = new SimpleDateFormat("H");
//		SimpleDateFormat dfMinute = new SimpleDateFormat("m");
		
//		int h = time.getHours() - mainFrame.chart.startHour;
		try {
		int h = Integer.parseInt(time.split(":")[0]) 
				- settings.startHour;
		if (h < 0)
			h += 24;

//		int m = time.getMinutes();
		int m = Integer.parseInt(time.split(":")[1]);

		return h * 60 + m;
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static final int SHOW_NONE = 0; //0000

	public static final int SHOW_DOWN = 1; //0001

	public static final int SHOW_UP = 2; //0010

	public static final int SHOW_ALL = 3; //0011 = SHOW_UP | SHOW_DOWN

	public int showUpDownState = SHOW_NONE;

	private ImageIcon imAll = new ImageIcon(ChartView.class
			.getResource("/pic/go_all_chart.gif"));

	private ImageIcon imDown = new ImageIcon(ChartView.class
			.getResource("/pic/go_down_chart.gif"));

	private ImageIcon imUp = new ImageIcon(ChartView.class
			.getResource("/pic/go_up_chart.gif"));

	private ImageIcon imNone = new ImageIcon(ChartView.class
			.getResource("/pic/go_none_chart.gif"));

	public void changeShowUpDownState() {
		showUpDownState = showUpDownState >= 3 ? 0 : showUpDownState + 1;
		updateUpDownDisplay();
	}

	public void changeShowDown() {
		showUpDownState ^= SHOW_DOWN;
		updateUpDownDisplay();
	}

	public void changShownUp() {
		showUpDownState ^= SHOW_UP;
		updateUpDownDisplay();
	}

	public void updateUpDownDisplay() {
		switch (showUpDownState) {
		case SHOW_ALL:
			cornerUpDown.setIcon(imAll);
			mainFrame.jtButtonDown.setSelected(true);
			mainFrame.jtButtonUp.setSelected(true);
			break;
		case SHOW_DOWN:
			cornerUpDown.setIcon(imDown);
			mainFrame.jtButtonDown.setSelected(true);
			mainFrame.jtButtonUp.setSelected(false);
			break;
		case SHOW_UP:
			cornerUpDown.setIcon(imUp);
			mainFrame.jtButtonDown.setSelected(false);
			mainFrame.jtButtonUp.setSelected(true);
			break;
		case SHOW_NONE:
			cornerUpDown.setIcon(imNone);
			mainFrame.jtButtonDown.setSelected(false);
			mainFrame.jtButtonUp.setSelected(false);
			break;
		}
		
		currentDrawingModel.updateUpDownTrainOption(showUpDownState);
		updateTrainTypeDisplayOrder();
		
		panelLines.updateBuffer();
	}

	public int distUpDownState = SHOW_DOWN;

	public Color downDistColor = Color.blue;

	public Color upDistColor = Color.red;

	public void changeDistUpDownState() {
		distUpDownState = distUpDownState == SHOW_DOWN ? SHOW_UP : SHOW_DOWN;
		if (distUpDownState == SHOW_DOWN)
			cornerCoordinate.setForeground(downDistColor);
		else
			cornerCoordinate.setForeground(upDistColor);
		panelCircuit.repaint();
	}

	public void setCoordinateCorner(Point p) {
		int dist = Math.round((p.y - topMargin) / settings.distScale);
		int minutes = Math.round( ((p.x - leftMargin) / settings.minuteScale) );
		cornerCoordinate.setText("(" + getClockString(minutes) + ","
				+ getDistString(dist) + ")");
		mainFrame.statusBarMain.setText(trainGraph.currentLineChart.railroadLine.getStationName(dist, true));
	}

	public String getDistString(Point p) {
		int dist = Math.round((p.y - topMargin) / settings.distScale);
		return getDistString(dist);
	}
	
	public String getClockString(Point p) {
		int minutes = Math.round( (p.x - leftMargin) / settings.minuteScale );
		return getClockString(minutes);
	}

	private String getDistString(int dist) {
		if (distUpDownState == SHOW_DOWN)
			return "" + dist;
		else
			return "" + (trainGraph.currentLineChart.railroadLine.length - dist);
	}
	
	private String getClockString(int minutes) {
		int hours = minutes < 0 ? -1 : minutes / 60;

		int clockMinute = minutes - hours * 60;
		if (clockMinute < 0)
			clockMinute += 60;

		String strMinute = clockMinute < 10 ? "0" + clockMinute : ""
				+ clockMinute;

		int clockHour = settings.startHour + hours;
		if (clockHour < 0)
			clockHour += 24;
		if (clockHour >= 24)
			clockHour -= 24;

		String strHour = clockHour < 10 ? "0" + clockHour : ""
			+ clockHour;

		return strHour + ":" + strMinute;
	}

	public String getTime(int px) {
		return getClockString(getMinute(px));
	}

	private JLabel cornerCoordinate = new JLabel("(0,0:00)");
	private JLabel cornerUpDown = new JLabel();
	private ControlPanel cornerControl;

	void jbInit() throws Exception {
		setLayout(new BorderLayout());
		add(spLines, BorderLayout.CENTER);

		spLines.getViewport().add(panelLines);
		spLines.setRowHeaderView(panelCircuit);
		spLines.setColumnHeaderView(panelClock);

		//在左上角显示坐标
		spLines.setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerCoordinate);
		cornerCoordinate.setHorizontalAlignment(SwingConstants.CENTER);
		cornerCoordinate.setVerticalAlignment(SwingConstants.CENTER);
		cornerCoordinate.setForeground(Color.blue);
		cornerCoordinate.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		cornerCoordinate.setFont(new Font("Dialog", Font.BOLD, 12));
		//处理坐标角鼠标点击事件
		cornerCoordinate.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				changeDistUpDownState();
			}
		});

		//在右下角显示“上下行”显示状态
		spLines.setCorner(JScrollPane.LOWER_RIGHT_CORNER, cornerUpDown);
		cornerUpDown.setIcon(imAll);
		cornerUpDown.setHorizontalAlignment(SwingConstants.CENTER);
		cornerUpDown.setVerticalAlignment(SwingConstants.CENTER);
		//处理上下行状态角鼠标点击事件
		cornerUpDown.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				changeShowUpDownState();
			}
		});
		
		//在左下角显示上行车次数和下行车次数－－放到sheetView的左上角去
		//左下角改放快速缩放控制按钮
		cornerControl = new ControlPanel(trainGraph, this);
		spLines.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerControl);

		//永远显示横竖滚动条，以便右下角的“上下行”状态显示能出现
		spLines.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		spLines.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		spLines.getVerticalScrollBar().setUnitIncrement(ScrollUnitIncrement);
		spLines.getHorizontalScrollBar().setUnitIncrement(ScrollUnitIncrement);
	}

	/**
	 * 取在图chart上画的停站
	 */
	public Stop[] getDrawStops(Train train) {
		RailroadLine circuit = trainGraph.currentLineChart.railroadLine;
		Stop[] drawStops = new Stop[circuit.getStationNum()];

		int iDraw = 0;
		for (int i = 0; i < train.getStopNum(); i++) {
			if (circuit.getStationDist(train.getStop(i).getName()) >= 0) {
				drawStops[iDraw] = train.getStop(i);
				iDraw++;
			}
		}

		Stop[] rtStops = new Stop[iDraw];
		for (int j = 0; j < iDraw; j++) {
			rtStops[j] = drawStops[j];
		}

		return rtStops;
	}

	public int getDrawStopIndex(Train train, String stationName) {
		Stop[] drawStops = getDrawStops(train);

		for (int i = 0; i < drawStops.length; i++) {
			if (drawStops[i].getName().equalsIgnoreCase(stationName))
				return i;
		}

		return -1;
	}

	public void resetSize() {
		panelCircuit.setSize(panelCircuit.getPreferredSize());
		panelClock.setSize(panelClock.getPreferredSize());
		panelLines.setSize(panelLines.getPreferredSize());
		
		currentDrawingModel.updateScale();
	}
	
//	public BufferedImage getBufferedImage() {
//		Rectangle rect = panelLines.getBounds();
//		BufferedImage bufImage = new BufferedImage(rect.width, rect.height,
//				BufferedImage.TYPE_INT_RGB);
//		Graphics g = bufImage.getGraphics();
////		g.translate(-rect.x, -rect.y);
//		panelLines.paint(g);
//		
//		return bufImage;
//		//		return this.panelLines.createVolatileImage(panelLines.getWidth(), panelLines.getHeight()).getSnapshot();
//	}
	
	private static final int IMG_TYPE = BufferedImage.TYPE_INT_RGB;
	
	public BufferedImage getBufferedImage() {
		int w = panelLines.getWidth() + circuitPanelWidth;
		int h = panelLines.getHeight() + clockPanelHeight;
		BufferedImage myImage = new BufferedImage(w, h, IMG_TYPE);
		Graphics g = myImage.getGraphics();
		
		BufferedImage circuitImage = getPanelsBufferedImage(panelCircuit);
		BufferedImage clockImage = getPanelsBufferedImage(panelClock);
		BufferedImage linesImage = getPanelsBufferedImage(panelLines);
		
		g.drawImage(circuitImage, 0, clockPanelHeight, null);
		g.drawImage(clockImage, circuitPanelWidth, 0, null);
		g.drawImage(linesImage, circuitPanelWidth, clockPanelHeight, null);
		
		g.setColor(panelClock.getBackground());
		g.fillRect(0, 0, circuitPanelWidth + 24, clockPanelHeight);
		g.setColor(Color.black);
		g.drawString("LGuo's ETRC", 4, 12);
		
		return myImage;
	}

	public BufferedImage getPanelsBufferedImage(JPanel panel) {
		BufferedImage bufImage = new BufferedImage(panel.getWidth(), panel.getHeight(), IMG_TYPE);
		panel.paint(bufImage.getGraphics());
		
		return bufImage;
	}

	public void setActiveSation(int y) {
		int dist = this.getDist(y);
		int index = trainGraph.currentLineChart.railroadLine.getStationIndex(dist);
		setActiveStation(trainGraph.currentLineChart.railroadLine.getStation(index));
	}
	
	public void setActiveStation(Station station) {
		activeStation = station;
		repaint();
		mainFrame.sheetView.selectStation(station);
	}

	public void updateData() {
		activeTrain = null;
		activeStation = null;
		shouldUpdateDrawingModel = true;
		repaint();
	}

	public void scrollToLeft() {
		spLines.getHorizontalScrollBar().setValue(0);
	}

	public void addTrain(Train train) {
		trainGraph.currentLineChart.addTrain(train);
		mainFrame.sheetView.updateData();
		
		this.setActiveTrain(train);
	}
	
	public void repaint() {
		if (panelLines != null)
			panelLines.updateBuffer();
		super.repaint();
	}
}
