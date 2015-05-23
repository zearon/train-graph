package org.paradise.etrc.view.timetableedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.TrainRouteSection;
import org.paradise.etrc.view.timetableedit.TimetableEditSheetModel;

public class SheetCellRanderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -3005467491964709634L;
	private int rowIndex;
	
	public SheetCellRanderer(){
        setOpaque(true);
        setHorizontalAlignment(CENTER);
	}

    @Override
	public void paint(Graphics g) {
    	super.paint(g);
    	
    	g.setColor(Color.gray);
    	//下横线
		if (rowIndex % 2 == 1)
			g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    	//上横线
//		g.drawLine(0, 0, getWidth(), 0);
    	
    	//左竖线
//		g.drawLine(0, 0, 0, getHeight()-1);
    	//右竖线
    	g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
    }

    @Override
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		
    	rowIndex = row;
		boolean isArriveLine = row % 2 == 0;
		TimetableEditSheetModel model = (TimetableEditSheetModel) table.getModel();
		if (model.isRemarksRow(row)) {
			setText(((TrainRouteSection) value).remarks);
			setBackground(table.getBackground());
		} else {
			Stop stop = model.getStop(row, column);
				
			//设置文字
			setText((stop == null) ? "" : 
				    (isArriveLine ? stop.getArriveTimeStr() : stop.getLeaveTimeStr()));
			
			int stationLine = row % 4;
			//设置背景色
			if(table.getSelectedColumn() == column || table.getSelectedRow() == row)
				setBackground(TimetableEditView.selectBK);
			else
				setBackground(stationLine < 2 ? TimetableEditView.lineBK1 : TimetableEditView.lineBK2);
			
			//设置文字颜色
			if(stop!=null && !stop.isPassenger())
					setForeground(Color.GRAY);
			else
				setForeground(Color.BLACK);
		}
		
		int remarksRow = table.getRowCount() - 1;
		if (row == remarksRow && column == 0) {
			EventQueue.invokeLater(() -> {
				table.setRowHeight(remarksRow, ((TimetableEditSheetTable) table).getRemarksRowHeight());
			});
		}
		
		return this;
	}
	
}
