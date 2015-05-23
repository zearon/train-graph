package org.paradise.etrc.view.timetableedit;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.view.sheet.SheetView;

public class SheetCellRanderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -3005467491964709634L;
	
	public SheetCellRanderer(){
        setOpaque(true);
        setHorizontalAlignment(CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		boolean isArriveLine = row % 2 == 0;
		Stop stop = (Stop) value;
		
		//设置文字
		setText((value == null) ? "" : 
			    (isArriveLine ? stop.getArriveTimeStr() : stop.getLeaveTimeStr()));
		
		int stationLine = row % 4;
		//设置背景色
		if(table.getSelectedColumn() == column || table.getSelectedRow() == row)
			setBackground(SheetView.selectBK);
		else
			setBackground(stationLine < 2 ? SheetView.lineBK1 : SheetView.lineBK2);
		
		//设置文字颜色
//		if(stop!=null)
//			if(!stop.isPassenger())
//				setForeground(Color.red);
//			else
//				setForeground(Color.black);
//		else
			setForeground(Color.black);
		
		return this;
	}
	
}
