package org.paradise.etrc.util.ui.widget.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ColorTableCellRender extends DefaultTableCellRenderer {

	private static final long	serialVersionUID	= 4255462264740546547L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		Color color = (Color) value;
		
		JLabel label = new JLabel();
		label.setOpaque(true);
		label.setBackground(color);

		return label;
	}
}
