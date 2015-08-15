package com.zearon.util.ui.widget;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class TransparentLabel extends JLabel implements TransparentComponent {

	private static final long	serialVersionUID	= 470619531929429289L;

	public TransparentLabel(String text) {
		super(text);
	}

	@Override
	public JComponent getThisComponent() {
		return this;
	}

	@Override
	protected void paintComponent(Graphics g) {
		getPaintComponentMethod().paint(g);
		
		super.paintComponent(g);
	}
}
