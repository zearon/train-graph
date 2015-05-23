package org.paradise.etrc.view.timetableedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.JTableHeader;

public class RowHeaderRenderer extends JLabel implements ListCellRenderer<Object> {
	private static final long serialVersionUID = -3720951686492113933L;

	TimetableEditSheetTable table;
	int rowIndex;
	
    public RowHeaderRenderer(TimetableEditSheetTable _table) {
    	table = _table;
        JTableHeader header = table.getTableHeader();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        setHorizontalAlignment(RIGHT);
        setFont(header.getFont());
    }
    
    @Override
	public Dimension getPreferredSize() {
    	Dimension d = super.getPreferredSize();
    	d.height = rowIndex == table.getRowCount() - 1 ? 
    			table.getRemarksRowHeight() : table.getRowHeight(rowIndex);
    	return d;
	}

    @Override
	public void paint(Graphics g) {
    	super.paint(g);
    	
    	g.setColor(Color.gray);
    	//下横线
    	if (rowIndex % 2 == 1)
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
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		setText((value == null) ? "" : value.toString());
		rowIndex = index;
		
		if(isSelected) {
			setBackground(TimetableEditView.selectBK);
		}
		else {
			setBackground(index % 4 < 2 ? TimetableEditView.lineBK1 : TimetableEditView.lineBK2);
		}
		
		return this;
	}
}
