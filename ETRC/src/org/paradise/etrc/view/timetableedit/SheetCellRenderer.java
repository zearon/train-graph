package org.paradise.etrc.view.timetableedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainRouteSection;
import org.paradise.etrc.util.ui.string.VerticalStringPainter;

public class SheetCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -3005467491964709634L;
	private JTable table;
	private TimetableEditSheetModel model;
	private int rowIndex;
//	private int columnIndex;
	private boolean verticalText = false;
	private VerticalStringPainter stringPainter = new VerticalStringPainter();
	
	public SheetCellRenderer(){
        setOpaque(true);
        setHorizontalAlignment(CENTER);
	}

    @Override
	public void paint(Graphics g) {
    	if (verticalText) {
			g.setColor(getBackground());
			Rectangle rect = getBounds();
			g.fillRect(0, 0, rect.width, rect.height);
			
			if (getBounds().width >= 45) {
				stringPainter.setProperties(2, 1, true);
			} else {
				stringPainter.setProperties(1, 1, true);
			}
    	}
    	else {
    		super.paint(g);
		}
    	
    	g.setColor(Color.gray);
    	//下横线
		if (rowIndex % 2 == 1 || rowIndex == table.getRowCount() - 1)
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
		
		this.table = table;
		this.model = (TimetableEditSheetModel) table.getModel();
		rowIndex = row;
		//    	columnIndex = column;
		TrainRouteSection section = (TrainRouteSection) value;
		boolean isArriveLine = row % 2 == 0;
		if (section.isBlank()) {
			setText("");
			setBackground(table.getBackground());
			setForeground(table.getBackground());
			verticalText = false;			
			
			if (isSelected)
				setBackground(TimetableEditView.selectBK);
		} else if (model.isRemarksRow(row)) {
			setText(section.remarks);
			setBackground(table.getBackground());
			setForeground(Color.BLACK);
			verticalText = true;			
			
			if (isSelected)
				setBackground(TimetableEditView.selectBK);
		} else {
			Stop stop = model.getStop(row, column);
				
			//设置文字
			setText((stop == null) ? "" : 
				    (isArriveLine ? stop.getArriveTimeStr() : stop.getLeaveTimeStr()));
			
//			int stationLine = row % 4;
			//设置背景色
			if (isSelected)//if(table.getSelectedColumn() == column || table.getSelectedRow() == row)
				setBackground(TimetableEditView.selectBK);
//			else
//				setBackground(stationLine < 2 ? TimetableEditView.lineBK1 : TimetableEditView.lineBK2);
			else if (stop != null && stop.stopStatus == Stop.STOP_START_STATION)
				setBackground(TimetableEditView.startStationBK);
			else if (stop != null && stop.stopStatus == Stop.STOP_TERMINAL_STATION)
				setBackground(TimetableEditView.terminalStationBK);
			else
				setBackground(TimetableEditView.lineBK1);
			
			//设置文字颜色
			if(stop!=null && !stop.isPassenger())
					setForeground(Color.GRAY);
			else {
				Color color = Color.BLACK;
				TrainGraph trainGraph = model.getTrainGraph();
				if (trainGraph.settings.timetableEditUseTrainTypeFontColor) {
					String trainName = model.getTrainRouteSection(column).getName();
					color = trainGraph.guessTrainTypeByName(trainName).getFontColor();
				}
				setForeground(color);
			}
			
			verticalText = false;
		}
		
		return this;
	}
	
}
