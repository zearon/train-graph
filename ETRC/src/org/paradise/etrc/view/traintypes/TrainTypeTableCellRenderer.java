package org.paradise.etrc.view.traintypes;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.paradise.etrc.data.v1.TrainGraph;
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
						g2d.setColor(trainType.color);

						Stroke lineStyle = new BasicStroke(trainType.lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
								10.0f, new float[] {6, 2, 2, 2}, 0f);
						// Dash
//						Stroke lineStyle = new BasicStroke(trainType.lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
//								10.0f, new float[] {10, 5}, 0f);
						// Dot
//						Stroke lineStyle = new BasicStroke(trainType.lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
//								10.0f, new float[] {2, 1}, 0f);
						// Dash and dot
//						Stroke lineStyle = new BasicStroke(trainType.lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
//								10.0f, new float[] {6, 2, 2, 2}, 0f);
						g2d.setStroke(lineStyle);
						g2d.drawLine(x1, y1, x2, y2);
					}
					
				};
			} else {

				component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
				if (column == 3) {
					// 缩写栏
					
					Color textColor = trainType.fontColor;
					component.setForeground(textColor);
					Font font = new Font(trainType.fontFamily, trainType.fontStyle, trainType.fontSize);
					component.setFont(font);
				}
			}
		}
		
		return component;
	}
}
