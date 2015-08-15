package com.zearon.util.ui.widget;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JLabel;

import com.zearon.util.ui.string.VerticalStringPainter;

public class JVerticalLabel extends JLabel {

	private static final long	serialVersionUID	= 4041405651244189365L;
	
	private VerticalStringPainter painter = new VerticalStringPainter();
	
	public JVerticalLabel(String text) {
		super(text);
	}

	@Override
	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);

		Rectangle rect = getBounds();
		
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, rect.width, rect.height);
		}
		
		g.setColor(getForeground());
		g.setFont(getFont());
		painter.setBounds(rect);
		painter.paintString((Graphics2D) g, getText());
	}
	
}
