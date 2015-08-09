package org.paradise.etrc.view.timetableedit;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;

public class CornerRenderer extends JLabel implements ListCellRenderer<String> {

	TimetableEditSheetTable table;
	int rowIndex;
	
	public CornerRenderer(JTable table) {
		this.table = (TimetableEditSheetTable) table;
		// setOpaque(true);
		setHorizontalAlignment(CENTER);
		setFont(table.getTableHeader().getFont());
	}
	
    @Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (rowIndex == 2) {
			// vehicle name
			d.height = table.getVehicleNameRowHeight();
		} else if (rowIndex == 0) {
			d.height = table.getRowHeight(0);
		}
		return d;
	}

    @Override
	public void paint(Graphics g) {
		int height = rowIndex == 2 ? table.getVehicleNameRowHeight() : getHeight();
		setBounds(0, 0, getWidth(), height);
		g.setClip(0, 0, getWidth(), height);

		super.paint(g);

		g.setColor(Color.gray);

		// 下横线
		g.drawLine(0, height - 1, getWidth(), height - 1);

		// 上横线
		if (rowIndex == 0)
			g.drawLine(0, 0, getWidth(), 0);

		// 左竖线
		g.drawLine(0, 0, 0, height - 1);
		// 右竖线
		g.drawLine(getWidth() - 1, 0, getWidth() - 1, height);
	}
    
	@Override
	public Component getListCellRendererComponent(JList<? extends String> list,
			String value, int index, boolean isSelected, boolean cellHasFocus) {
		
		rowIndex = index;
		setText(value);
		return this;
	}

}
