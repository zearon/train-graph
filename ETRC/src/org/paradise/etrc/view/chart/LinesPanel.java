package org.paradise.etrc.view.chart;

import static org.paradise.etrc.ETRC.__;
import static org.paradise.etrc.ETRCUtil.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.data.Chart;
import org.paradise.etrc.data.Station;
import org.paradise.etrc.data.Stop;
import org.paradise.etrc.data.Train;
import org.paradise.etrc.dialog.TrainDialog;
import org.paradise.etrc.dialog.YesNoBox;
import org.paradise.etrc.slice.ChartSlice;
import org.paradise.etrc.view.chart.traindrawing.TrainDrawing;
import org.paradise.etrc.view.chart.traindrawing.TrainLine;
import org.paradise.etrc.view.sheet.SheetTable;
import org.paradise.etrc.wizard.Wizard;
import org.paradise.etrc.wizard.addtrain.AddTrainWizard;

/**
 * @author lguo@sina.com
 * @version 1.0
 */


public class LinesPanel extends JPanel implements MouseListener,MouseMotionListener {

/**********************************************************************
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
 **********************************************************************/

	private static final long serialVersionUID = 6196666089237432404L;

	private ChartView chartView;
	private boolean shouldRepaint = true;
	private BufferedImage bufferedImage;

	public LinesPanel(ChartView _mainView) {
		chartView = _mainView;
		chartView.mainFrame.chart.addChartChangedListener(chart->this.updateBuffer());
		
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

//	private Timer mouseTimerLeft;
//	private Timer mouseTimerRight;
//	public void mouseClicked(final MouseEvent e) {
//		System.out.println(e.getWhen());
//		if(e.getButton() == MouseEvent.BUTTON1) {
//			if (e.getClickCount() == 1) {
//				mouseTimerLeft = new Timer(200, new ActionListener() {
//					public void actionPerformed(ActionEvent evt) {
//						mouseTimerLeft.stop();
//						System.out.println("L Single");
//						mouseClickedOneLeft(e);
//					}
//				});
//				mouseTimerLeft.restart();
//			} else if (e.getClickCount() == 2 && mouseTimerLeft.isRunning()) {
//				mouseTimerLeft.stop();
//				System.out.println("L Double");
//				mouseClickedDoubleLeft(e);
//			}
//		}
//		else if(e.getButton() == MouseEvent.BUTTON3) {
//			if (e.getClickCount() == 1) {
//				mouseTimerRight = new Timer(200, new ActionListener() {
//					public void actionPerformed(ActionEvent evt) {
//						mouseTimerRight.stop();
//						System.out.println("R Single");
//						mouseClickedOneRight(e);
//					}
//				});
//				mouseTimerRight.restart();
//			} else if (e.getClickCount() == 2 && mouseTimerRight.isRunning()) {
//				mouseTimerRight.stop();
//				System.out.println("R Double");
//				mouseClickedDoubleRight(e);
//			}
//		}
//	}
	
//	private Timer mouseTimer;
//	public void mouseClicked(final MouseEvent e) {
//		if (e.getClickCount() == 1) {
//			mouseTimer = new Timer(200, new ActionListener() {
//				public void actionPerformed(ActionEvent evt) {
//					mouseTimer.stop();
//					System.out.println("Single");
//					
//					if(e.getButton() == MouseEvent.BUTTON1)
//						mouseClickedOneLeft(e);
//					else
//						mouseClickedOneRight(e);
//				}
//			});
//			mouseTimer.restart();
//		} else if (e.getClickCount() == 2 && mouseTimer.isRunning()) {
//			mouseTimer.stop();
//			System.out.println("Double");
//			
//			if(e.getButton() == MouseEvent.BUTTON3)
//				mouseClickedDoubleLeft(e);
//			else
//				mouseClickedDoubleRight(e);
//		}
//	}

	private java.util.Timer myTimer;
	private boolean hasMouseDoubleClicked;
    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 1) {
            myTimer = new java.util.Timer();
            myTimer.schedule(new TimerTask() {
                public void run() {
                    if (!hasMouseDoubleClicked) {     
    					//System.out.println("Single");
	    				if (e.getButton() == MouseEvent.BUTTON1)
							mouseClickedOneLeft(e);
						else
							mouseClickedOneRight(e);
                    }
                    
                    hasMouseDoubleClicked = false;
                    myTimer.cancel();
                }
            }, 250);
        }
        else if (e.getClickCount() == 2) {
            hasMouseDoubleClicked = true;
            
			//System.out.println("Double");
			if(e.getButton() == MouseEvent.BUTTON1)
				mouseClickedDoubleLeft(e);
			else
				mouseClickedDoubleRight(e);
       }
    }
    
    //单击左键，选车
	private void mouseClickedOneLeft(MouseEvent e) {
		selectTrain(e.getPoint());
	}
	
	//单击右键，弹出菜单
	private void mouseClickedOneRight(MouseEvent e) {
		//如果有选中车次则
		if(chartView.activeTrain != null) {
			//如果是在选中车次的车次上单击右键，则弹出车次相关菜单
			if(chartView.drawingModel.getActiveTrainDrawing().pointOnMe(e.getPoint()) || 
			   chartView.drawingModel.getActiveTrainDrawing().pointOnMyRect(e.getPoint())) {
				popupMenuOnActiveTrain(e.getPoint());
			}
			//不是在选中车次上单击右键，弹出车次无关菜单
			else {
				popupMenuNoTrain(e.getPoint());
			}
		}
		//没有选中车次，弹出车次无关菜单
		else {
			popupMenuNoTrain(e.getPoint());
		}
	}
	
	//双击左键
	private void mouseClickedDoubleLeft(MouseEvent e) {
		//如果没有选中的车则先做一次选车
		if(chartView.activeTrain == null)
			selectTrain(e.getPoint());

		//如果有选中车次则
		if(chartView.activeTrain != null) {
			//双击左键，设定到站时刻
			setStationTime(e.getPoint(), true);
		}
		//没有选中车次则选择活跃车站
		else {
			chartView.setActiveSation(e.getPoint().y);
		}
	}
	
	//双击右键
	private void mouseClickedDoubleRight(MouseEvent e) {
		//如果没有选中的车则先做一次选车
		if(chartView.activeTrain == null)
			selectTrain(e.getPoint());
		
		//如果有选中车次则
		if(chartView.activeTrain != null) {
			//双击右键，设定发车时刻
			setStationTime(e.getPoint(), false);
		}
		//如果没有选中车次，弹出车次无关菜单
		else {
			popupMenuNoTrain(e.getPoint());
		}
	}
	
	void jbInit() throws Exception {
		this.setBackground(Color.white);
		this.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		this.setDebugGraphicsOptions(0);
		this.setLayout(new BorderLayout());
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
	}
	
	public void updateBuffer() {
//		DEBUG("updateBuffer");
		shouldRepaint = true;
		super.repaint();
	}

	public void paint(Graphics g) {
		super.paint(g);

//		if (shouldRepaint)
			paintOnBuffer(g);
		
//		Dimension d = getSize();
//		g.drawImage(bufferedImage, 0, 0, d.width, d.height, this);
	}
	
	public void paintOnBuffer(Graphics g) {
//		DEBUG_STACKTRACE(10, "PAINT");
//		int[] widthNheight = new int[2];
//		getBuffersize(widthNheight);
//		if (bufferedImage == null) {
//			bufferedImage = new BufferedImage(widthNheight[0], widthNheight[1], BufferedImage.TYPE_INT_ARGB);
//		}
//		if (bufferedImage != null) {
//			bufferedImage.flush();
//			bufferedImage = null;
//		}
//		Dimension d = getPreferredSize();
//		bufferedImage = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		
//		Graphics g = bufferedImage.getGraphics();
//		g.setColor(getBackground());
//		g.fillRect(0, 0, d.width, d.height);
		
		for (int i = 0; i < 24; i++) {
			drawClockLine(g, i);
		}

		Chart chart = chartView.mainFrame.chart;
		if (chart.trunkCircuit != null) {
			for (int i = 0; i < chart.trunkCircuit.getStationNum(); i++) {
				drawStationLine(g, chart.trunkCircuit.getStation(i), chart.distScale);
			}
		}

		drawTrains(g);
		
//		if(chartView.activeTrainDrawing!= null)
//			moveToTrainDrawing(chartView.activeTrainDrawing);
		
//		g.finalize();
		shouldRepaint = false;
	}

	/**
	 * DrawTrains
	 * 
	 * @param g
	 *            Graphics
	 */
	private void drawTrains(Graphics g) {
		chartView.updateDrawingModel();

		// 防锯齿
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// 先画水印部分
		for (TrainDrawing trainDrawing : chartView.drawingModel.getUnderDrawings() ) {
			// 当前选中的车次放在最后画，确保在最上面
			if (trainDrawing != chartView.drawingModel.getActiveTrainDrawing()) {
				trainDrawing.draw(g);
			}			
		}

		// 再画非选中部分
		for (Enumeration<TrainDrawing> e = chartView.drawingModel.getNormalDrawings().elements(); e
				.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof TrainDrawing) {
				TrainDrawing trainDrawing = ((TrainDrawing) obj);
				// 当前选中的车次放在最后画，确保在最上面
				if (trainDrawing != chartView.drawingModel.getActiveTrainDrawing()) {
					trainDrawing.draw(g);
				}
				// 把选中车次删掉再添加，以便在最后比较容易选择TrainLine
				else {
					// trainDrawings.removeElement(trainDrawing);
					// trainDrawings.add(trainDrawing);
				}
			}
		}

		// 最后画当前选中的车次
		if (chartView.drawingModel.getActiveTrainDrawing() != null) {
			// &&(mainFrame.chart.trainDrawings.contains(mainFrame.chart.activeTrain))) {
			// System.out.println("Ac " + activeTrain.getTrainName());
			chartView.drawingModel.getActiveTrainDrawing().draw(g);
		}
	}

	public Dimension getPreferredSize() {
		Chart chart = chartView.mainFrame.chart;
		int w, h;
		
		w = 60 * 24 * chart.minuteScale 
		  + chartView.leftMargin 
		  + chartView.rightMargin;
		
		if (chart.trunkCircuit != null)
			h = Math.round(chart.trunkCircuit.length * chart.distScale )
			  + chartView.topMargin
			  + chartView.bottomMargin;
		else
			h = 480;
		
		return new Dimension(w, h);
	}
	
	public void getBuffersize(int[] widthNHeight) {
		Chart chart = chartView.mainFrame.chart;
		int w, h;
		
		w = 60 * 24 * 3//Chart.MAX_MINUTE_SCALE 
		  + chartView.leftMargin 
		  + chartView.rightMargin;
		
		if (chart.trunkCircuit != null)
			h = Math.round(chart.trunkCircuit.length * 3) //Chart.MAX_DIST_SCALE )
			  + chartView.topMargin
			  + chartView.bottomMargin;
		else
			h = 480;
		
		widthNHeight[0] = w;
		widthNHeight[1] = h;
	}

	/**
	 * DrawHour
	 * 
	 * @param g
	 *            Graphics
	 */
	private void drawClockLine(Graphics g, int clock) {
		// 设置坐标线颜色
		Color oldColor = g.getColor();
		g.setColor(chartView.gridColor);

		Chart chart = chartView.mainFrame.chart;
		int start = clock * 60 * chart.minuteScale + chartView.leftMargin;
		int h = Math.round(chart.trunkCircuit.length * chart.distScale);
		
		// 0～23点整点竖线
		g.drawLine(start, 0, 
				   start, h + chartView.topMargin + chartView.bottomMargin);
		g.drawLine(start + 1, 0, 
				   start + 1, h + chartView.topMargin + chartView.bottomMargin);
		
		// 整点之间的timeInterval分钟间隔竖线
		for (int i = 1; i < 60 / chart.timeInterval; i++) {
			int x = start + chart.timeInterval * chart.minuteScale * i;
			int y1 = chartView.topMargin;
			int y2 = y1 + h;
			g.drawLine(x, y1, x, y2);
		}

		// 24点整点竖线
		if (clock == 23) {
			int end = 24 * 60 * chart.minuteScale + chartView.leftMargin;
			
			g.drawLine(end, 0, 
					   end, h + chartView.topMargin + chartView.bottomMargin);
			g.drawLine(end + 1, 0, 
					   end + 1, h + chartView.topMargin + chartView.bottomMargin);
		}

		// 恢复原色
		g.setColor(oldColor);
	}

	private void drawStationLine(Graphics g, Station st, float scale) {
		if (st.hide)
			return;

		Chart chart = chartView.mainFrame.chart;

		// 设置坐标线颜色
		Color oldColor = g.getColor();
		
		if(st.equals(chartView.activeStation))
			g.setColor(chartView.activeGridColor);
		else
			g.setColor(chartView.gridColor);

		// 画坐标线
		int y = Math.round(st.dist * scale) + chartView.topMargin;
		int w = 60 * 24 * chart.minuteScale 
		        + chartView.leftMargin
				+ chartView.rightMargin;

		if (st.level <= chart.displayLevel) {
			g.drawLine(0, y, w, y);
			if (st.level <= chart.boldLevel) {
				g.drawLine(0, y + 1, w, y + 1);
			}
		}

		// 恢复原色
		g.setColor(oldColor);
	}

	/**
	 * mouseDragged
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * mouseMoved
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseMoved(MouseEvent e) {
		//设置跟随坐标
		Image img = null;	
		try {
			img = ImageIO.read(ChartView.class.getResource("/pic/cursor.gif"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if(img != null) {
			Toolkit tk = Toolkit.getDefaultToolkit();
			Graphics g = img.getGraphics();
			g.setFont(new Font("Dialog", Font.PLAIN, 10));
			g.setColor(Color.black);
			g.fillRect(2,10,30,11);
			g.setColor(Color.white);
			g.fillRect(3,11,28,9);
			g.setColor(Color.black);
			g.drawString(chartView.getClockString(e.getPoint()), 4, 19);

			Cursor cursor = tk.createCustomCursor(img, new Point(0,0),"Name");
			this.setCursor(cursor);
		}
		
		Point p = e.getPoint();
		chartView.setCoordinateCorner(p);
		Train nearTrain[] = findTrains(p);
		for (int i = 0; i < nearTrain.length; i++)
			chartView.mainFrame.statusBarMain.setText(
					nearTrain[i].getTrainName(chartView.mainFrame.chart.trunkCircuit) + "次 " 
					+ chartView.mainFrame.statusBarMain.getText());
	}

	/**
	 * findTrain
	 * 
	 * @param p
	 *            Point
	 * @return Train[]
	 */
	private Train[] findTrains(Point p) {
		Vector<Train> trainsFound = new Vector<Train>();
		// 正常部分
		for (TrainDrawing trainDrawing : chartView.drawingModel.getNormalDrawings() ) {
			if (trainDrawing.pointOnMe(p)) {
				trainsFound.add(trainDrawing.train);
			}
		}
		
		// 水印部分
		// 如果“水印显示反向车次”未选中则不给选水印车次
		if(chartView.underDrawingColor != null) {
			for (TrainDrawing trainDrawing : chartView.drawingModel.getUnderDrawings()) {
				if (trainDrawing.pointOnMe(p)) {
					trainsFound.add(trainDrawing.train);
				}
			}
		}
		
		Train array[] = new Train[trainsFound.size()];
		int i = 0;
		for (Enumeration<Train> e = trainsFound.elements(); e.hasMoreElements();) {
			array[i++] = (Train) e.nextElement();
		}
		
		return array;
	}

	private void setStationTime(Point p, boolean isArrive) {
		chartView.setActiveSation(p.y);
		
		SheetTable table = chartView.mainFrame.sheetView.table;
		if(table.getCellEditor() != null)
			table.getCellEditor().stopCellEditing();

		String theTime = chartView.getTime(p.x);
		Train theTrain = chartView.activeTrain;
		String staName = chartView.activeStation.name;

		if(theTrain.hasStop(staName)) {
			if(isArrive)
				theTrain.setArrive(staName, theTime);
			else
				theTrain.setLeave(staName, theTime);
		}
		else {
			Stop stop = new Stop(staName, theTime, theTime, false);
			chartView.mainFrame.chart.insertNewStopToTrain(theTrain, stop);
//			Stop prevStop = chartView.mainFrame.chart.findPrevStop(theTrain, stop.stationName);
//			if(prevStop == null)
//				theTrain.appendStop(stop);
//			else
//				theTrain.insertStopAfter(prevStop, stop);
		}

//		((SheetModel) table.getModel()).fireTableDataChanged();
		updateBuffer();
	}

	/**
	 * 选择p点附近的Train作为ActiveTrain 若p点不靠近任何一个车次则ActiveTrain设为null
	 * 
	 * @param p
	 *            Point
	 */
	private void selectTrain(Point p) {
		TrainDrawing selectedTrainDrawing = null;

		// 先查水印部分的
		// 如果“水印显示反向车次”未选中则不给选水印车次
		if(chartView.underDrawingColor != null) {
			for (Enumeration<TrainDrawing> e = chartView.drawingModel.getUnderDrawings().elements(); e.hasMoreElements();) {
				TrainDrawing trainDrawing = (TrainDrawing) e.nextElement();
				if (trainDrawing.pointOnMyRect(p) || trainDrawing.pointOnMe(p)) {
					// System.out.println(trainDrawing.getTrainName()+ " A");
					selectedTrainDrawing = trainDrawing;
				}
			}
		}

		// 后查正常部分的，这样正常部分比较容易选中
		for (Enumeration<TrainDrawing> e = chartView.drawingModel.getNormalDrawings().elements(); e
				.hasMoreElements();) {
			TrainDrawing trainDrawing = (TrainDrawing) e.nextElement();
			if (trainDrawing.pointOnMyRect(p) || trainDrawing.pointOnMe(p)) {
				// System.out.println(trainDrawing.getTrainName()+ " A");
				selectedTrainDrawing = trainDrawing;
			}
		}

//		if (selectedTrainDrawing != null) {
//			TrainDrawing.TrainLine line = selectedTrainDrawing.findNearTrainLine(p);
//			selectedTrainDrawing.selectedTrainLine = line;
//		}

		chartView.setActiveTrain((selectedTrainDrawing == null) ? null: selectedTrainDrawing.train);
	}

//	public void selectTrain(TrainDrawing trainDrawing) {
//		chartView.activeTrain = (trainDrawing == null) ? null: trainDrawing.train;
////		chartView.activeTrainDrawing = trainDrawing;
//		
//		// 设置MainFrame的标题和ToolTip
//		if (chartView.activeTrain != null) {
//			//ADD For SheetView
//			chartView.mainFrame.sheetView.selectTrain(chartView.activeTrain);
//			
//			chartView.mainFrame.setActiceTrainName(chartView.activeTrain.getTrainName());
//
////			TrainDrawing.TrainLine line = chartView.activeTrainDrawing.selectedTrainLine;
////			if (line != null)
////				setToolTipText(line.getInfo());
////			else
////				setToolTipText(trainDrawing.getInfo());
//		} else {
//			chartView.mainFrame.setActiceTrainName("");
//			setToolTipText(null);
//		}
//
//		// 重绘
//		repaint();
//	}

	private void doEditActiveTrain() {
		TrainDialog dialog = new TrainDialog(chartView.mainFrame, chartView.activeTrain);

		dialog.editTrain();
		
		if(!dialog.isCanceled) {
			Train editedTrain = dialog.getTrain();
			Chart chart = chartView.mainFrame.chart;
			//没有改车次的情况，更新
			if(chart.isLoaded(editedTrain)) {
				chart.updateTrain(editedTrain);
			}
			//改了车次的情况，删掉原来的，增加新的
			else {
				chart.delTrain(chartView.activeTrain);
				chart.addTrain(editedTrain);
			}
			
			chartView.setActiveTrain(editedTrain);
			chartView.mainFrame.sheetView.updateData();
			chartView.mainFrame.runView.refresh();
		}
	}

	private void doDeleteActiveTrain() {
		Chart chart = chartView.mainFrame.chart;
		chart.delTrain(chartView.activeTrain);
		chartView.setActiveTrain(null);
		chartView.mainFrame.sheetView.updateData();
		chartView.mainFrame.runView.refresh();
	}

	/**
	 * mouseEntered
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * mouseExited
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * mousePressed
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mousePressed(MouseEvent e) {
		//TODO: 待完善
//		if(e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() >= 2) {
//			String time = chartView.getClockString(e.getPoint());
//			SheetTable table = chartView.mainFrame.sheetView.table;
//			int row = table.getEditingRow();
//			int col = table.getEditingColumn();
//			JTextField editor = ((JTextField) table.getEditorComponent());
//			if(editor != null) {
//				editor.setText(time);
//				table.getCellEditor().stopCellEditing();
//				((SheetModel) table.getModel()).fireTableCellUpdated(row, col);
//				repaint();
//			}
//		}
	}

	/**
	 * mouseReleased
	 * 
	 * @param e
	 *            MouseEvent
	 */
	public void mouseReleased(MouseEvent e) {
	}

	public String getToolTipText(MouseEvent event) {
		Point p = event.getPoint();

		if (chartView.drawingModel.getActiveTrainDrawing() != null) {
			TrainLine line = chartView.drawingModel.getActiveTrainDrawing().findNearTrainLine(p);
			if (line != null)
				return line.getInfo();
			else if (chartView.drawingModel.getActiveTrainDrawing().pointOnMyRect(p))
				return chartView.drawingModel.getActiveTrainDrawing().getInfo();
		}

		return null;
	}

	public JToolTip createToolTip() {
		JToolTip toolTip = new JToolTip();
		toolTip.setFont(new Font("Dialog", 0, 12));
		return toolTip;
	}
	
	//无选中车次的时候弹出菜单
	//new MessageBox(chartView.mainFrame, "TODO: 弹出菜单“输入新车|选择线路|导入车次|载入运行图|保存运行图|另存运行图”").showMessage();
	private void popupMenuNoTrain(final Point p) {
//		// 如果没有选中则返回
//		if (chartView.activeTrain != null) {
//			popupMenuOnActiveTrain(p);
//			return;
//		}
		
		//菜单项
		MenuItem miNewTrain = new MenuItem(__("Add New Train"));
		miNewTrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAddNewTrain();
			}
		});
		//MenuItem miSelectCircuit = new MenuItem("选择线路");
		//miSelectCircuit.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent e) {
//				editActiveTrain();
		//	}
		//});
		MenuItem miGif = new MenuItem(__("Export Graph..."));
		miGif.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ETRC.getInstance().getMainFrame().doExportChart();
			}
		});
		MenuItem miFindTrains = new MenuItem(__("Load Train..."));
		miFindTrains.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ETRC.getInstance().getMainFrame().doLoadTrain();
			}
		});
		MenuItem miLoad = new MenuItem(__("Open..."));
		miLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ETRC.getInstance().getMainFrame().doLoadChart();
			}
		});
		MenuItem miSave = new MenuItem(__("Save"));
		miSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ETRC.getInstance().getMainFrame().doSaveChart();
			}
		});
		MenuItem miSaveAs = new MenuItem(__("Save As..."));
		miSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ETRC.getInstance().getMainFrame().doSaveChartAs();
			}
		});
		
		// 弹出PopupMenu
		PopupMenu pop = new PopupMenu();
		pop.add(miNewTrain);
//		pop.add(miSelectCircuit);
        pop.add(miFindTrains);
		pop.addSeparator();
		pop.add(miGif);
		pop.add(miLoad);
		pop.add(miSave);
		pop.add(miSaveAs);
		this.add(pop);
		pop.show(this, p.x, p.y);
	}

    //在选中车次上弹出菜单“改变颜色|编辑点单|车次切片|删除本车次|保存为车次文件”").showMessage();
	private void popupMenuOnActiveTrain(final Point p) {
		// 如果没有选中则返回
		if (chartView.activeTrain == null)
			return;
		
		// 如果不在ActiveTrain上则返回
		if (!chartView.drawingModel.getActiveTrainDrawing().pointOnMe(p) &&
			!chartView.drawingModel.getActiveTrainDrawing().pointOnMyRect(p))
			return;
		
		//菜单项
		MenuItem miDelTrain = new MenuItem(__("Delete"));
		miDelTrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDeleteActiveTrain();
			}
		});
		MenuItem miEditTimes = new MenuItem(__("Edit Time Table"));
		miEditTimes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doEditActiveTrain();
			}
		});
		MenuItem miColor = new MenuItem(__("Change Color"));
		miColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSetColor();
			}
		});
		MenuItem miTrainSlice = new MenuItem(__("Train Slice"));
		miTrainSlice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ChartSlice(chartView.mainFrame.chart).makeTrainSlice(chartView.activeTrain);
			}
		});
		/*		MenuItem miSaveAs = new MenuItem("另存为...");
		miSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: 另存为
			}
		});
*/		
		// 弹出PopupMenu
		PopupMenu pop = new PopupMenu();
		pop.add(miDelTrain);
		pop.add(miColor);
		pop.add(miEditTimes);
        pop.add(miTrainSlice);
//		pop.addSeparator();
//		pop.add(miSaveAs);
		this.add(pop);
		pop.show(this, p.x, p.y);
	}

	private void doAddNewTrain() {
		AddTrainWizard wizard = new AddTrainWizard(this.chartView);
		
		if(wizard.doWizard() == Wizard.FINISHED) {
			Train train = wizard.getTrain();
			
			if(train == null)
				return;
			
			Chart chart = chartView.mainFrame.chart;
			if(chart.containTrain(train)) {
				if(new YesNoBox(chartView.mainFrame, String.format(__("Train %s is already in the graph, overwrite?"), train.getTrainName())).askForYes()) {
					chartView.mainFrame.chart.delTrain(train);
					chartView.addTrain(train);
				}
			}
			else {
				chartView.addTrain(train);
			}
		}
	}
	
	private void doSetColor() {
		final JColorChooser colorChooser = new JColorChooser();
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chartView.drawingModel.getActiveTrainDrawing().train.color = colorChooser.getColor();
				LinesPanel.this.updateBuffer();
			}
		};

		JDialog dialog = JColorChooser.createDialog(chartView.mainFrame,
				__("Select the color for the line"), true, // modal
				colorChooser, listener, // OK button handler
				null); // no CANCEL button handler
		colorChooser.setColor(chartView.drawingModel.getActiveTrainDrawing().train.color);
		ETRC.setFont(dialog);

		Dimension dlgSize = dialog.getPreferredSize();
		Dimension frmSize = chartView.mainFrame.getSize();
		Point loc = chartView.mainFrame.getLocation();
		dialog.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dialog.setVisible(true);
	}

	//待优化：应该滚动到第一个段（可能分段显示）尽可能位于屏幕正中
	public void moveToTrainDrawing(TrainDrawing trainDrawing) {
		Rectangle bounds = trainDrawing.getPreferredBounds();
		bounds.y = 0;
		bounds.height = this.getHeight();
//		Rectangle bounds = trainDrawing.firstRect.getBounds();
		scrollRectToVisible(bounds);
//		selectTrain(trainDrawing);
//		chartView.setActiveTrain(trainDrawing.train);
		chartView.setActiveTrain((trainDrawing == null) ? null: trainDrawing.train);	
		updateBuffer();
	}
}
