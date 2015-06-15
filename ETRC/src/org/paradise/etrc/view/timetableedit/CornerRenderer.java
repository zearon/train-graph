package org.paradise.etrc.view.timetableedit;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;

import org.paradise.etrc.data.v1.TrainRouteSection;

public class CornerRenderer extends JLabel implements ListCellRenderer<String> {

	TimetableEditSheetTable table;
	int rowIndex;
	
	public CornerRenderer(JTable table) {
		this.table = (TimetableEditSheetTable) table;
//        setOpaque(true);
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
    	super.paint(g);
    	
    	g.setColor(Color.gray);
    	//下横线
		g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    	//上横线
    	if (rowIndex == 0)
    		g.drawLine(0, 0, getWidth(), 0);
    	
    	//左竖线
		g.drawLine(0, 0, 0, getHeight()-1);
    	//右竖线
    	g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
    }
    
	@Override
	public Component getListCellRendererComponent(JList<? extends String> list,
			String value, int index, boolean isSelected, boolean cellHasFocus) {
		
		rowIndex = index;
		setText(value);
		return this;
	}

}
