package org.paradise.etrc.util.ui.widget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.function.Consumer;

import javax.swing.JComponent;


public interface TransparentComponent {
	
	public JComponent getThisComponent();
	
	default void setBGOpaque(int alphaValue) {
		JComponent com = getThisComponent();
		int colorRGB = com.getBackground().getRGB() & 0x00FFFFFF;
		int colorRGBA = ((alphaValue & 0xFF) << 24) | colorRGB;
		com.setBackground(new Color(colorRGBA, true));
	}
	
	default int getBGOpaque(){
		JComponent com = getThisComponent();
		int alpha = (com.getBackground().getRGB() & 0xFF000000) >> 24;
		return alpha;
	}
	
	default Rectangle getBGBounds() {
		JComponent com = getThisComponent();
		return new Rectangle(0, 0, com.getWidth(), com.getHeight());
	}
	
	default PaintDelegate getPaintComponentMethod() {
		return g -> {
			JComponent com = getThisComponent();
			
			if (!com.isOpaque()) {
				Rectangle bounds = getBGBounds();
				int colorRGB = com.getBackground().getRGB();
				int colorRGBA = (getBGOpaque() << 24) | colorRGB;
				g.setColor(new Color(colorRGBA, true));
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		};
	}
}

@FunctionalInterface
interface PaintDelegate {
	public void paint(Graphics g);
}
