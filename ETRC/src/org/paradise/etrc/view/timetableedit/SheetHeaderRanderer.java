package org.paradise.etrc.view.timetableedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainRouteSection;

public class SheetHeaderRanderer extends JList<TrainRouteSection> implements TableCellRenderer {
	private static final long serialVersionUID = -3005467491964709634L;
	TimetableEditSheetTable table;
	
	public SheetHeaderRanderer(){
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		this.table = (TimetableEditSheetTable) table;
		TimetableEditSheetModel model = (TimetableEditSheetModel) table.getModel();
		TrainRouteSection trainSection = model.getTrainRouteSection(column);
		
		HeaderModel listModel = new HeaderModel(trainSection);
		setModel(listModel);
		setCellRenderer(new HeaderRenderer(table, column, trainSection));
		
		setToolTipText(trainSection.getName());
		ToolTipManager.sharedInstance().setDismissDelay(15000);
		
		return this;
	}
}

class HeaderModel extends AbstractListModel<TrainRouteSection> {
	private static final long serialVersionUID = -8105353935408719117L;
	TrainRouteSection trainSection;
	boolean downGoing;

	public HeaderModel(TrainRouteSection trainSection) {
		this.trainSection = trainSection;
	}
	
	@Override
	public int getSize() {
		return 3;
	}

	@Override
	public TrainRouteSection getElementAt(int index) {
		return trainSection;
	}
	
}

class HeaderRenderer extends JLabel implements ListCellRenderer<TrainRouteSection> {
	private static final long serialVersionUID = 1094199972478724379L;
	TimetableEditSheetTable table;
	int rowIndex;
	int column;
	TrainRouteSection trainSection;
	
    public HeaderRenderer(JTable table, int column, TrainRouteSection trainSection) {
    	this.table = (TimetableEditSheetTable) table;
    	this.column = column;
    	this.trainSection = trainSection;
        JTableHeader header = table.getTableHeader();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        setHorizontalAlignment(CENTER);
        setFont(header.getFont());
    }
    
    @Override
	public Dimension getPreferredSize() {
    	Dimension d = super.getPreferredSize();
		if (rowIndex == 2) {
			// vehicle name
			d.height = table.getVehicleNameRowHeight();
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
//		if (column == 0)
//			g.drawLine(0, 0, 0, getHeight()-1);
    	//右竖线
    	g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
    }

    @Override
	public Component getListCellRendererComponent(JList<? extends TrainRouteSection> list, 
			TrainRouteSection value, int index, boolean isSelected, boolean cellHasFocus) {

		rowIndex = index;
		setBackground(table.getBackground());
		String text;
		
		Train train = value.getTrain();
		if (train == null) {
			setText("1");
			setForeground(table.getBackground());
			return this;
		}
		
		if (index == 0) {
			// Train name (up-going / down-going)
			text = value.downGoing ? train.trainNameDown : train.trainNameUp;
			if (text == null || "".equals(text)) {
				text = train.getName();
				setForeground(Color.RED);
			} else {
				setForeground(table.getForeground());
			}
			setText(text);
		} else if (index == 1) {
			// Train type
			text = train.trainType.abbriveation;
			setText(text);
			setForeground(train.trainType.getLineColor());		} else {
			// Vehicle Name
			text = "";
			setText(text);
			setForeground(table.getForeground());
		}
		
		return this;
	}
}

