package org.paradise.etrc.util.ui;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class JColorChooserLabel extends JLabel implements MouseListener, ActionListener {
	private static JColorChooser colorChooser = new JColorChooser();
	private JDialog dialog;
	
	private Color color = Color.BLACK;
	
	public JColorChooserLabel() {
		init();
	}
	
	public JColorChooserLabel(String text) {
		super(text);
		init();
	}
	
	public JColorChooserLabel(Color color) {
		init();
	}
	
	private void init() {
		setOpaque(true);
		addMouseListener(this);
		setBackground(color);
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		if (color != null) {
			Color oldColor = this.color;
			this.color = color;
			setBackground(color);
			
//			DEBUG_MSG("Set color from %s to: %s by %s id=(%d)", 
//					ValueTypeConverter.colorToString(oldColor), 
//					ValueTypeConverter.colorToString(color),
//					getName(), hashCode());
			
			firePropertyChange("color", oldColor, color);
		}
	}
	
	private void do_ShowColorChooser() {
		colorChooser.setColor(color);
		dialog = JColorChooser.createDialog(null,  	// parent component
				"Please choose a color", 			// title
				true, 								// modal
				colorChooser, 						// JColorChooser instance
				this, 								// OK listener
				this);								// Cancel listener
		
		dialog.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("OK")) {
			Color color = colorChooser.getColor();
			
			if (color != null)
				setColor(color);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		do_ShowColorChooser();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}


}
