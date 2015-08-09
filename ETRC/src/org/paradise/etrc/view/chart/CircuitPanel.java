package org.paradise.etrc.view.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.paradise.etrc.data.v1.ChartSettings;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.slice.ChartSlice;

import static org.paradise.etrc.ETRC.__;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class CircuitPanel extends JPanel {
	private static final long serialVersionUID = -4260259395001588542L;

	BorderLayout borderLayout1 = new BorderLayout();

	int myLeftMargin = 5;

	private ChartView chartView;

	private RailroadLineChart chart;
	private ChartSettings settings;

	private boolean ui_inited;

	public CircuitPanel(TrainGraph trainGraph, ChartView chartView) {
		this.chartView = chartView;
		setModel(trainGraph);

		try {
			jbInit();
			ui_inited = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		this.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		this.setLayout(borderLayout1);
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getPoint().x > ChartView.circuitPanelWidth - 25)
					chartView.changeDistUpDownState();
				else {
					chartView.setActiveSation(e.getPoint().y + 12);
					if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2)
						if(chartView.activeStation != null)
							new ChartSlice(chartView.activeLineChart).makeStationSlice(chartView.activeStation);
					chartView.panelLines.updateBuffer();
				}
			}
		});
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.chart = trainGraph.currentLineChart;
		this.settings = trainGraph.settings;
		
		if (ui_inited) {
			repaint();
		}
	}

	public void paint(Graphics g) {
		super.paint(g);

		if (chart == null)
			return;

		if (chart.railroadLine != null)
			for (int i = 0; i < chart.railroadLine.getStationNum(); i++) {
				DrawStation(g, chart.railroadLine.getStation(i));
			}
	}

	public Dimension getPreferredSize() {
		if (chart == null)
			return new Dimension(640, 480);

		if (chart.railroadLine == null)
			return new Dimension(ChartView.circuitPanelWidth, 480);

		int w = ChartView.circuitPanelWidth;
		int h = Math.round(chart.railroadLine.length * settings.distScale) + chartView.topMargin
				+ chartView.bottomMargin;
		return new Dimension(w, h);
	}

	public void DrawStation(Graphics g, Station station) {
		if (station.hide)
			return;

		int y = Math.round(station.dist * settings.distScale) + chartView.topMargin;

		if (station.level <= settings.displayLevel) {
			//设置坐标线颜色
			Color oldColor = g.getColor();
			
			if(station.equals(chartView.activeStation))
				g.setColor(chartView.activeGridColor);
			else
				g.setColor(chartView.gridColor);

			//画坐标线
			g.drawLine(myLeftMargin, y, ChartView.circuitPanelWidth, y);
			if (station.level <= settings.boldLevel) {
				g.drawLine(myLeftMargin, y + 1, ChartView.circuitPanelWidth,
						y + 1);
			}

			//恢复原色
			g.setColor(oldColor);

			//画站名与里程
			DrawName(g, station, y);
		}
	}

	private void DrawName(Graphics g, Station station, int y) {
		//站名
		g.drawString(station.getName(), myLeftMargin + 2, y - 2);

		Color oldColor = g.getColor();
		String stDist;
		//下行里程
		if (chartView.distUpDownState == ChartView.SHOW_DOWN) {
			stDist = station.dist == 0 ? "0" + settings.distUnit : "" + station.dist;
			g.setColor(chartView.downDistColor);
		}
		//上行里程
		else {
			stDist = station.dist == chart.railroadLine.length ? "0km" : "" + (chart.railroadLine.length - station.dist);
			g.setColor(chartView.upDistColor);
		}
		Font oldFont = g.getFont();
		g.setFont(new Font("Dialog", 0, 10));
		int x = ChartView.circuitPanelWidth
				- g.getFontMetrics().stringWidth(stDist);
		// 文字防锯齿
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.drawString(stDist, x, y - 2);
		g.setFont(oldFont);

		g.setColor(oldColor);
	}
}
