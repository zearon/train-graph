package org.paradise.etrc.wizard.addtrain;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;

public class TrainTableCellEditor extends AbstractCellEditor implements TableCellEditor {
	private static final long serialVersionUID = 1L;

	private JTextField editor;
	private String oldTime;
	private Stop stop;
	private boolean isArrive;
	
	public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
		editor = new JTextField();
		
		editor.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				editor.selectAll();
			}
			public void focusLost(FocusEvent arg0) {
			}
		});

		editor.setBorder(BorderFactory.createEmptyBorder());
		editor.setHorizontalAlignment(SwingConstants.CENTER);
		
		//设置文本
		stop = (Stop) value;
		isArrive = (column == 1);
		oldTime = (value == null) ? "" : (isArrive ? stop.getArrive(): stop.getLeave());
		editor.setText(oldTime);
		
		return editor;
	}

	public Object getCellEditorValue() {
		String time = Train.formatTime(oldTime, editor.getText());
		
		if(isArrive)
			stop.setArrive(time);
		else
			stop.setLeave(time);
		
		return stop;
	}
	
}
