package org.paradise.etrc.view.network;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import org.paradise.etrc.data.v1.TrainGraph;

public class MapPanel extends JPanel {

	TrainGraph trainGraph;
	Image bgImage;
	int width;
	int height;
	Dimension size;
	private boolean ui_inited;
	
	public int alpha_value = 155;
	
	/**
	 * Create the panel.
	 */
	public MapPanel(TrainGraph trainGraph) {
		setModel(trainGraph);
		
		initUI();
		
		ui_inited=true;
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		this.bgImage = trainGraph.map.getImage();
		if (bgImage != null) {
			this.width = trainGraph.map.getWidth();
			this.height = trainGraph.map.getHeight();
		} else {
			this.width = 0;
			this.height = 0;
		}
		size = new Dimension(width, height);
		
		if (ui_inited) {
			repaint();
		}
	}

	private void initUI() {
		if (bgImage != null) {
			// release
		}
		
//		ImageIcon icon = new ImageIcon("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/map.jpg");
//		width = icon.getIconWidth();
//		height = icon.getIconHeight();
//		bgImage = icon.getImage();
		
//		size = new Dimension(width, height);
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (bgImage == null)
			return super.getPreferredSize();
		else
			return size;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// TODO: 绘图Optimize
		g.setClip(0, 0, width, height);
		
		if (bgImage != null) {
			g.drawImage(bgImage, 0, 0, null);
		}
		
		// Add a opaque cover above the map
		g.setColor(new Color(255, 255, 255, alpha_value));
		g.fillRect(0, 0, width, height);
	}

}
