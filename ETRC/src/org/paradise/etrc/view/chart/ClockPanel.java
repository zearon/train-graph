package org.paradise.etrc.view.chart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.paradise.etrc.data.v1.ChartSettings;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.TrainGraph;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class ClockPanel extends JPanel {
	private static final long serialVersionUID = 2449059376608773861L;

	private ChartView chartView;
	private ChartSettings settings;

	private boolean ui_inited;

	public ClockPanel(TrainGraph trainGraph, ChartView chartView) {
		this.chartView = chartView;
		setModel(trainGraph);
		
		try {
			jbInit();
			ui_inited = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.settings = trainGraph.settings;
		
		if (ui_inited) {
			repaint();
		}
	}

	void jbInit() throws Exception {
		this.setLayout(new BorderLayout());
		
		JPanel scalePanel = new JPanel();
		scalePanel.setLayout(new GridLayout(1,2));
		JButton jb1 = createScaleButton("+");
		JButton jb2 = createScaleButton("-");
		scalePanel.setPreferredSize(new Dimension(24, 16));
		scalePanel.add(jb1);
		scalePanel.add(jb2);
		add(scalePanel, BorderLayout.WEST);
		
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					int x = e.getPoint().x;
					
					double h = settings.startHour + ((x - chartView.leftMargin)/settings.minuteScale)/60.0;
	
					if(Math.abs(h - Math.round(h)) < 16.0/settings.minuteScale/60.0) {
						int theHour = (int) (Math.round(h) >= 24 ? Math.round(h) - 24 : Math.round(h));
						settings.startHour = theHour;
						chartView.scrollToLeft();
						chartView.repaint();
					}
				}
			}
		});
	}

	private JButton createScaleButton(String func) {
		ImageIcon im;
		if(func.equals("+"))
			im = new ImageIcon(this.getClass().getResource("/pic/add.gif"));
		else
			im = new ImageIcon(this.getClass().getResource("/pic/sub.gif"));
		
		JButton jb = new JButton(im);
		
		jb.setFocusPainted(false);
		jb.setBorderPainted(false);
		
		jb.setActionCommand(func);
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if(ae.getActionCommand().equals("+")) {
					increaseMinuteGap(1);
				}
				else {
					increaseMinuteGap(-1);
				}
			}
		});
		
		return jb;
	}
	
	private int[] minuteGrids = {20,10,10,10,10,5,5,5,5,5};
	private void increaseMinuteGap(int i) {
		settings.minuteScale += i;
		
		if(settings.minuteScale > RailroadLineChart.MAX_MINUTE_SCALE) {
			settings.minuteScale = RailroadLineChart.MAX_MINUTE_SCALE;
			return;
		}
		
		if(settings.minuteScale < 1) {
			settings.minuteScale = 1;
			return;
		}
		
		// TODO: settings.minuteScale 改为浮点
		settings.timeInterval = minuteGrids[(int) settings.minuteScale-1];
		chartView.resetSize();
		chartView.panelLines.updateBuffer();
	}

	public void paint(Graphics g) {
		super.paint(g);
		
		// 文字防锯齿
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		int h = settings.startHour;
		for (int i = 0; i < 24; i++) {
			if (h >= 24)
				h -= 24;

			//System.out.println("Clock: " + h);
			DrawHour(g, h);
			h++;
		}

		if (settings.startHour == 0)
			DrawEndHour(g, 24);
		else
			DrawEndHour(g, settings.startHour);
	}

	public Dimension getPreferredSize() {
		int w, h;
		w = Math.round( 60 * 24 * settings.minuteScale + chartView.leftMargin 
				+ chartView.rightMargin );
		h = ChartView.clockPanelHeight;
		return new Dimension(w, h);
	}

	/**
	 * DrawHour
	 *
	 * @param g Graphics
	 */
	private void DrawHour(Graphics g, int clock) {
		int coordinate = getCoordinate(clock);

		int startPos = Math.round( coordinate * 60 * settings.minuteScale + chartView.leftMargin - 12 );
		String stClock = clock + ":00";
		g.drawString(stClock, startPos, ChartView.clockPanelHeight - 2);
	}

	private void DrawEndHour(Graphics g, int clock) {
		int start = Math.round( 24 * 60 * settings.minuteScale + chartView.leftMargin - 12 );
		String stClock = clock + ":00";
		g.drawString(stClock, start, ChartView.clockPanelHeight - 2);
	}

	private int getCoordinate(int clock) {
		int drawClock = clock - settings.startHour;
		if (drawClock < 0)
			drawClock += 24;

		return drawClock;
	}
}
