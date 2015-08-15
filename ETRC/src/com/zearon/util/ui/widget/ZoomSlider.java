package com.zearon.util.ui.widget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;

public class ZoomSlider extends JSlider implements TransparentComponent {
	private static final long	serialVersionUID	= -3617773806212126083L;
	
	private double[] zoomValues;
	private LabelDictionary labels;
	
	public ZoomSlider(double[] zoomValues) {
		setAvailableZoomValues(zoomValues);

		setSnapToTicks(true);
		setPaintLabels(true);
		setPaintTicks(true);
		setMajorTickSpacing(1);
		setMinorTickSpacing(0);
	}
	
	public void setLabelStyle(boolean showMultiplySign, boolean showPercentage) {
		if (labels != null)
			labels.setLabelStyle(showMultiplySign, showPercentage);
	}
	
	public void setAvailableZoomValues(double[] zoomValues) {
		if (zoomValues == null)
			throw new IllegalArgumentException("zoom values can not be null");
		
		this.zoomValues = zoomValues;
		labels = new LabelDictionary(zoomValues);
		setMinimum(0);
		setMaximum(zoomValues.length - 1);
		
		setLabelTable(labels);
	}
	
	public double getZoomValue() {
		return zoomValues[getValue()];
	}

	@Override
	public void paintComponent(Graphics g) {
//		Rectangle bounds = getBounds();
//		g.setColor(new Color(255, 255, 255, 100));
//		g.fillRect(26, 0, bounds.width, bounds.height);
		getPaintComponentMethod().paint(g);
		
		super.paintComponent(g);
	}

	@Override
	public Rectangle getBGBounds() {
		return new Rectangle(26, 0, getWidth(), getHeight());
	}

	@Override
	public JComponent getThisComponent() {
		return this;
	}
}

@SuppressWarnings("serial")
class LabelDictionary extends Hashtable<Integer, JLabel> {

	private double[] zoomValues;
	private boolean showMultiplySign = false;
	private boolean showPercentage = true;
	private DecimalFormat df = new DecimalFormat("0.#%");
	
	public LabelDictionary(double[] zoomValues) {
		this.zoomValues = zoomValues;
		
		for (Integer i = 0; i < zoomValues.length; ++ i) {
			put(i, new JLabel(getLabelText(zoomValues[i])));
		}
	}
	
	public void setLabelStyle(boolean showMultiplySign, boolean showPercentage) {
		for (Integer i = 0; i < zoomValues.length; ++ i) {
			JLabel label = get(i);
			label.setText(getLabelText(zoomValues[i]));
			label.updateUI();
		}
	}
	
	private String getLabelText(double zoomValue) {
		return (showMultiplySign ? "×" : "") + 
				(showPercentage ? "" + df.format(zoomValue) : "" + zoomValue);
	}
	
}
