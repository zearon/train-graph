package org.paradise.etrc.view.traintypes;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.data.TrainType;

public class TrainTypeTableCellRenderer extends DefaultTableCellRenderer {
	
	int effectiveColumnIndex = -1;
	
	public TrainTypeTableCellRenderer(int columnIndex) {
		this.effectiveColumnIndex = columnIndex;
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		TrainGraph trainGraph = ((TrainTypeTableModel) table.getModel()).trainGraph;
		TrainType trainType = trainGraph.allTrainTypes.get(row);

		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		
		if (column == effectiveColumnIndex) {
			if (column == 3) {
				// 缩写栏
				
				Color textColor = trainType.fontColor;
				component.setForeground(textColor);
				Font font = new Font(trainType.fontFamily, trainType.fontStyle, trainType.fontSize);
				component.setFont(font);
			} else {
				
			}
		}
		
		return component;
	}

}
