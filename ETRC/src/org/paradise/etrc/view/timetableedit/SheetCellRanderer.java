package org.paradise.etrc.view.timetableedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.TrainRouteSection;
import org.paradise.etrc.util.ui.string.VerticalStringPainter;
import org.paradise.etrc.view.timetableedit.TimetableEditSheetModel;

public class SheetCellRanderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -3005467491964709634L;
	private int rowIndex;
	private boolean verticalText = false;
	private VerticalStringPainter stringPainter = new VerticalStringPainter();
	
	public SheetCellRanderer(){
        setOpaque(true);
        setHorizontalAlignment(CENTER);
	}

    @Override
	public void paint(Graphics g) {
    	if (verticalText) {
			g.setColor(getBackground());
			Rectangle rect = g.getClipBounds();
			g.fillRect(0, 0, rect.width, rect.height);
    	} 
    	else {
    		super.paint(g);
		}
    	
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
    	
    	if (verticalText) {
    		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
    				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    		
    		g.setColor(getForeground());
    		stringPainter.setBounds(getBounds());
    		stringPainter.paintString((Graphics2D) g, getText());
    	}
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
			setForeground(Color.BLACK);
			verticalText = true;
		} else {
			Stop stop = model.getStop(row, column);
				
			//设置文字
			setText((stop == null) ? "" : 
				    (isArriveLine ? stop.getArriveTimeStr() : stop.getLeaveTimeStr()));
			
			int stationLine = row % 4;
			//设置背景色
			if (isSelected)//if(table.getSelectedColumn() == column || table.getSelectedRow() == row)
				setBackground(TimetableEditView.selectBK);
			else
				setBackground(stationLine < 2 ? TimetableEditView.lineBK1 : TimetableEditView.lineBK2);
			
			//设置文字颜色
			if(stop!=null && !stop.isPassenger())
					setForeground(Color.GRAY);
			else
				setForeground(Color.BLACK);
			
			verticalText = false;
		}
		
		return this;
	}
	
}
