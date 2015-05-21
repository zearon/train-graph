package org.paradise.etrc.view.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.paradise.etrc.data.v1.ChartSettings;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.dialog.ChartSetDialog;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class ControlPanel extends JPanel {
	private static final long serialVersionUID = 2449059376608773861L;

	private ChartView chartView;

	private ChartSettings settings;

	public ControlPanel(TrainGraph trainGraph, ChartView _chartView) {
		chartView = _chartView;
		setModel(trainGraph);
		
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.settings = trainGraph.settings;
	}

	void jbInit() throws Exception {
		this.setLayout(new GridLayout(1, 5));
		
		setBorder(BorderFactory.createCompoundBorder(
       				BorderFactory.createLineBorder(Color.lightGray), 
					BorderFactory.createEmptyBorder(2, 0, 0, 0)));
		
		//横加
		JButton btdl = createButton("hAdd");
		btdl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				increaseMinuteGap(1);
			}
		});
		
		//竖加
		JButton btsl = createButton("vAdd");
		btsl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				increaseDistGap(1);
			}
		});
		
		//竖减
		JButton btsr = createButton("vDel");
		btsr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				increaseDistGap(-1);
			}
		});
		
		//横减
		JButton btdr = createButton("hDel");
		btdr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				increaseMinuteGap(-1);
			}
		});

		//设置
		JButton btmd = createButton("hvDefault");
		btmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setup();
			}
		});
		
		add(btdl);
		add(btdr);
		add(btmd);
		add(btsl);
		add(btsr);
	}
	
	public JButton createButton(String pic) {
		ImageIcon im = new ImageIcon(this.getClass().getResource("/pic/" + pic + ".png"));
		JButton jb = new JButton(im);
		
		jb.setFocusPainted(false);
		jb.setBorderPainted(false);
		
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
//		settings.timeInterval = minuteGrids[(int) settings.minuteScale-1];
		chartView.resetSize();
		chartView.panelLines.updateBuffer();
	}

	private void increaseDistGap(int i) {
		settings.distScale += i;
		
		if(settings.distScale > RailroadLineChart.MAX_DIST_SCALE) {
			settings.distScale = RailroadLineChart.MAX_DIST_SCALE;
			return;
		}
		
		if(settings.distScale < 1) {
			settings.distScale = 1;
			return;
		}

		chartView.resetSize();
		chartView.panelLines.updateBuffer();
	}
	
	private void setup() {
		ChartSetDialog dlg = new ChartSetDialog(settings, chartView.mainFrame);
		dlg.editSettings();
	}

	public Dimension getPreferredSize() {
		int w, h;
		w = ChartView.circuitPanelWidth;
		h = ChartView.clockPanelHeight;
		return new Dimension(w, h);
	}
}
