package com.zearon.util.ui.map;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.time.Clock;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.paradise.etrc.data.v1.TrainGraph;

import com.zearon.util.image.ChangableColorModelBufferedImage;
import com.zearon.util.image.GrayFilterColorModel;
import com.zearon.util.image.ImageUtil;
import com.zearon.util.image.ReverseColorFilterColorModel;

import static org.paradise.etrc.ETRCUtil.DEBUG;
import static org.paradise.etrc.ETRCUtil.DEBUG_MSG;
import static org.paradise.etrc.ETRCUtil.DEBUG_STACKTRACE;

@SuppressWarnings("serial")
public class MapPanel extends JPanel {

	JScrollPane scrollPane;
	
	TrainGraph trainGraph;
	BufferedImage bgImage;
	BufferedImage bgImageCached;
	int width, origWidth;
	int height, origHeight;
	Dimension size;
	private boolean ui_inited;
	
	private boolean nightMode; 
	private boolean grayMode;
	private int alpha_value = 155;
	private double zoomValue;
	
	/**
	 * Create the panel.
	 */
	public MapPanel(TrainGraph trainGraph, JScrollPane parentScrollPane) {
		this.scrollPane = parentScrollPane;
		
		if (trainGraph != null)
			setModel(trainGraph);
		
		initUI();
		
		ui_inited=true;
	}

	public int getAlphaValue() {
		return alpha_value;
	}

	public void setAlphaValue(int alpha_value) {
		this.alpha_value = alpha_value;
	}
	
	static int counteri = 0;
	public void setModel(TrainGraph trainGraph) {
		DEBUG_STACKTRACE(Integer.MAX_VALUE, "第%d次执行", ++ counteri);
		
		this.trainGraph = trainGraph;
		bgImage = trainGraph.map.getImage();
		repaintBgImageCached();
		
		if (bgImage != null) {
			origWidth = width = trainGraph.map.getWidth();
			origHeight = height = trainGraph.map.getHeight();
		} else {
			origWidth = width = 0;
			origHeight = height = 0;
		}
		
		resize();
	}

	private void initUI() {
		setDoubleBuffered(true);
		
		if (bgImage != null) {
			// release
		}
	}
	
	private void repaintBgImageCached() {
	//	bgImageCached = bgImage;
		bgImageCached = ImageUtil.cloneImage(bgImage);
		System.gc();
	}
	
	public void setZoomValue(double zoomValue) {
		this.zoomValue = zoomValue;
		
		width = (int) (origWidth * zoomValue);
		height = (int) (origHeight * zoomValue);
		
		resize();
	}
	
	public void resize() {
		if (ui_inited) {
			if (width > 0) {
				int hInc = width / 200;
				hInc = hInc > 0 ? hInc : 1;
				scrollPane.getHorizontalScrollBar().setUnitIncrement(hInc);
			}
			
			if (height > 0) {
				int vInc = height / 200;
				vInc = vInc > 0 ? vInc : 1;
				scrollPane.getVerticalScrollBar().setUnitIncrement(vInc);
			}

			size = new Dimension(width, height);
			scrollPane.setPreferredSize(size);
			
			scrollPane.updateUI();
		}
	}
	
	public void setViewMode(boolean nightMode, boolean grayMode) {
		this.nightMode = nightMode;
		this.grayMode = grayMode;
		
		setViewModeInternal();
	}

	private void setViewModeInternal() {
		long start = System.nanoTime(), t1, t2, end;
		ImageUtil.copyImageData(bgImage, bgImageCached);
		t1 = System.nanoTime();
		
		if (nightMode && grayMode) {
			ImageUtil.reverseColorAndToGrayImage(bgImageCached);
		} else if (nightMode && !grayMode) {
			ImageUtil.reverseColor(bgImageCached);
		} else if (!nightMode && grayMode) {
			ImageUtil.toGrayImage(bgImageCached);
		} else {
		}
		
		t2 = System.nanoTime();
		repaint();
		end = System.nanoTime();
		
		DEBUG_MSG("Set view mode NightMode: %b & GrayMode: %b takes %,d (%,d + %,d + %,d) nanoseconds", 
				nightMode, grayMode, end - start, t1 - start, t2 - t1, end - t2);
		

		System.gc();
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (bgImage == null)
			return super.getPreferredSize();
		else
			return size;
	}

	@Override
	public void paintComponent(Graphics g) {		//paintComponent
//		super.paintComponent(g);
		
		// 绘图Optimize
		int left = 0, top = 0, width = this.width, height = this.height;
		if (scrollPane != null && scrollPane.getViewport() != null) {
			Point topleftPoint = scrollPane.getViewport().getViewPosition();
			Dimension viewSize = scrollPane.getViewport().getExtentSize();
			left = topleftPoint.x;
			top = topleftPoint.y;
			width = viewSize.width;
			height = viewSize.height;
			g.setClip(left, top, width, height);
		}
		
		if (bgImage != null) {
			if (zoomValue == 1.0) {
				g.drawImage(bgImageCached, 0, 0, null);
			} else {
				g.drawImage(bgImageCached, 0, 0, this.width, this.height, null);
			}
//			g.drawImage(bgImage, 
//					0, 0, viewSize.width, viewSize.height, 
//					0, 0, viewSize.width, viewSize.height, 
//					null);
		}
		
		// Add a semi-opaque cover above the map
		g.setColor(new Color(255, 255, 255, getAlphaValue()));
		g.fillRect(left, top, width, height);
		
		

		// 绘制FPS
//		if (true) {
//			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//			g.setFont(fpsFont);
//			g.setColor(Color.BLACK);
//			g.drawString(fpsString, 10, 20);
//		}
	}

}
