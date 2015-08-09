package org.paradise.etrc.view.traintypes;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.paradise.etrc.data.v1.TrainType;

public class TrainTypeTableCellRenderer extends DefaultTableCellRenderer {
	
	int effectiveColumnIndex = -1;
	
	public TrainTypeTableCellRenderer(int columnIndex) {
		this.effectiveColumnIndex = columnIndex;
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@SuppressWarnings("serial")
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		Component component = null;
		TrainType trainType = ((TrainTypeTableModel) table.getModel()).getTrainTypeAtRow(row);
		
		if (trainType != null && column == effectiveColumnIndex) {
			if (column == 4) {
				// Line Style
				component = new JLabel("") {

					@Override
					protected void paintComponent(Graphics g) {
						Graphics2D g2d = (Graphics2D) g;
						Rectangle rect = g.getClipBounds();
						int x1 = rect.x + 2;
						int x2 = rect.width - 2;
						int y1 = rect.height / 2;
						int y2 = y1;
						
						g2d.clearRect(rect.x, rect.y, rect.width, rect.height);
						g2d.setColor(trainType.getLineColor());
						
						g2d.setStroke(trainType.getLineStroke());
						g2d.drawLine(x1, y1, x2, y2);
					}
					
				};
			} else {

				component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
				if (column == 3) {
					// 缩写栏
					
					Color textColor = trainType.getFontColor();
					component.setForeground(textColor);
					component.setFont(trainType.getFont());
				}
			}
		}
		
		return component;
	}
}
