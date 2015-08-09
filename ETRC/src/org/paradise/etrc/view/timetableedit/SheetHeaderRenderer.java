package org.paradise.etrc.view.timetableedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ToolTipManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainRouteSection;
import org.paradise.etrc.data.v1.TrainType;
import org.paradise.etrc.util.ui.string.VerticalStringPainter;

import static org.paradise.etrc.ETRC.__;

public class SheetHeaderRenderer extends JList<TrainRouteSection> implements
		TableCellRenderer {
	private static final long	serialVersionUID	= -3005467491964709634L;
	TrainGraph								trainGraph;
	TimetableEditSheetTable		table;
	TrainRouteSection					trainSection;

	public SheetHeaderRenderer(TrainGraph trainGraph) {
		setTrainGraph(trainGraph);
	}

	public void setTrainGraph(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		this.table = (TimetableEditSheetTable) table;
		TimetableEditSheetModel model = (TimetableEditSheetModel) table.getModel();
		trainSection = model.getTrainRouteSection(column);

		HeaderModel listModel = new HeaderModel(trainSection);
		setModel(listModel);
		setCellRenderer(new HeaderRenderer(table, column, trainGraph, trainSection));

		if (column == table.getColumnCount() - 1)
			setToolTipText(__("Double click to create a new train."));
		else
			setToolTipText(trainSection.getName());
		ToolTipManager.sharedInstance().setDismissDelay(15000);

		return this;
	}
}

class HeaderModel extends AbstractListModel<TrainRouteSection> {
	private static final long	serialVersionUID	= -8105353935408719117L;
	TrainRouteSection					trainSection;
	boolean										downGoing;

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

class HeaderRenderer extends JLabel implements
		ListCellRenderer<TrainRouteSection> {
	private static final long			serialVersionUID	= 1094199972478724379L;
	TimetableEditSheetTable				table;
	int														rowIndex;
	int														column;
	boolean												isNewTrainColumn;
	TrainGraph										trainGraph;
	TrainRouteSection							trainSection;

	private boolean								verticalText			= false;
	private VerticalStringPainter	stringPainter			= new VerticalStringPainter();

	public HeaderRenderer(JTable table, int column, TrainGraph trainGraph,
			TrainRouteSection trainSection) {

		this.table = (TimetableEditSheetTable) table;
		this.column = column;
		this.trainGraph = trainGraph;
		this.trainSection = trainSection;
		this.isNewTrainColumn = column == table.getColumnCount() - 1;
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
		} else if (rowIndex == 0) {
			d.height = table.getRowHeight(0);
		}
		return d;
	}

	@Override
	public void paint(Graphics g) {
		if (verticalText) {
			g.setColor(getBackground());
			Rectangle rect = g.getClipBounds();
			g.fillRect(0, 0, rect.width, rect.height);
		} else {
			super.paint(g);
		}

		g.setColor(Color.gray);
		// 下横线
		g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
		// 上横线
		if (rowIndex == 0)
			g.drawLine(0, 0, getWidth(), 0);

		// 左竖线
		// if (column == 0)
		// g.drawLine(0, 0, 0, getHeight()-1);
		// 右竖线
		g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

		if (verticalText) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g.setColor(getForeground());
			stringPainter.setBounds(getBounds());
			stringPainter.paintString((Graphics2D) g, getText());
		}
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends TrainRouteSection> list, TrainRouteSection value,
			int index, boolean isSelected, boolean cellHasFocus) {

		rowIndex = index;
		setBackground(table.getBackground());
		String text;

		if (index == 0) {
			Font tableFont = table.getFont();
			Font font = new Font(tableFont.getFamily(), tableFont.getStyle(), 10);
			setFont(font);
		} else {
			setFont(table.getFont());
		}
		
		if (value.isBlank()) {
			// The column represents a blank train
			setText("blank");
			setForeground(table.getBackground());
			
			return this;
		}

		if (isNewTrainColumn) {
			if (index == 0) {
				setText(__("(NEW)"));
				setForeground(Color.BLACK);
				verticalText = false;
			} else if (index == 1) {
				setText(__("*"));
				setForeground(Color.BLACK);
				// setForeground(table.getBackground());
				verticalText = false;
			} else if (index == 2) {
				setText(__("*"));
				setForeground(Color.BLACK);
				// setForeground(table.getBackground());
			}
			return this;
		}

		if (index == 0) {
			// Train name (up-going / down-going)
			Train train = value.getTrain();
			if (train != null) {
				text = value.downGoing ? train.trainNameDown : train.trainNameUp;
				if (text == null || "".equals(text)) {
					text = train.getName();
					setForeground(Color.RED);
				} else {
					setForeground(table.getForeground());
				}
			} else {
				text = value.getName();
			}
			setText(text);
			verticalText = false;
		} else if (index == 1) {
			// Train type
			TrainType trainType = trainGraph.guessTrainTypeByName(value.getName());
			text = trainType.abbriveation;
			setText(text);
			setForeground(trainType.getLineColor());
			verticalText = false;
		} else {
			// Vehicle Name
			text = value.getVehicleName();
			setText(text);
			setForeground(table.getForeground());
			verticalText = true;
		}

		for (int selectedColumn : table.getSelectedColumns())
			if (column == selectedColumn) {
				setBackground(TimetableEditView.selectBK);
				break;
			}

		return this;
	}
}
