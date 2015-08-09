package org.paradise.etrc.util.ui.widget.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.util.ui.JColorChooserLabel;
import org.paradise.etrc.view.alltrains.TrainListView;

public class ColorTableCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	private static final long serialVersionUID = -5449669328932839469L;

	JColorChooserLabel colorButton;

	public ColorTableCellEditor() {
		colorButton = new JColorChooserLabel();
	}
	
	public ColorTableCellEditor(Color color) {
		colorButton = new JColorChooserLabel(color);
	}
	
	public Color getColor() {
		return colorButton.getColor();
	}

	public void setColor(Color color) {
		colorButton.setColor(color);
	}

	//Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column) {
		
		colorButton.setColor((Color) value);

		return colorButton;
	}

	@Override
	public Object getCellEditorValue() {
		return colorButton.getColor();
	}
}