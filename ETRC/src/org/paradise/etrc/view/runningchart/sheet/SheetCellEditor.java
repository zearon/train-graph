package org.paradise.etrc.view.runningchart.sheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;

public class SheetCellEditor extends AbstractCellEditor implements
		TableCellEditor {
	private static final long serialVersionUID = 1L;

	private Stop stop;
	private String trainName;
	private boolean isArriveLine;
	private JTextField editor;
	private String oldTime;

	public Component getTableCellEditorComponent(final JTable table,
			Object value, boolean isSelected, final int row, final int column) {
		isArriveLine = row % 2 == 0;
		stop = (Stop) value;
		Train train = ((SheetModel) table.getModel()).getTrain(column);
		trainName = train != null ? train.getName() : null;

		if (editor == null) {
			editor = new JTextField();

			// 响应双击事件，左键双击改为办客，右键双击改为非办客
			editor.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					if (me.getClickCount() >= 2
							&& me.getButton() == MouseEvent.BUTTON3) {
						if (stop != null) {
							stop.setPassenger(false);
							table.updateUI();
							setEditorColor();
						}
					} else if (me.getClickCount() >= 2
							&& me.getButton() == MouseEvent.BUTTON1) {
						if (stop != null) {
							stop.setPassenger(true);
							table.updateUI();
							setEditorColor();
						}
					}
				}

				private void setEditorColor() {
					// 设置文字颜色
					if (stop != null)
						if (!stop.isPassenger()) {
							editor.setForeground(Color.red);
							editor.setSelectedTextColor(Color.red);
						} else {
							editor.setForeground(Color.black);
							editor.setSelectedTextColor(Color.white);
						}
					else {
						editor.setForeground(Color.black);
						editor.setSelectedTextColor(Color.white);
					}
				}
			});

			editor.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent arg0) {
					editor.selectAll();
				}

				public void focusLost(FocusEvent arg0) {
				}
			});

			editor.setBorder(BorderFactory.createEmptyBorder());
			editor.setHorizontalAlignment(SwingConstants.CENTER);
		}

		table.changeSelection(row, column, false, false);
		table.repaint();
		table.getTableHeader().repaint();
		// ((SheetTable) table).getRowHeader().setSelectedIndex(row);
		// ((SheetTable) table).getRowHeader().repaint();

		// 设置文字颜色
		if (stop != null)
			if (!stop.isPassenger()) {
				editor.setForeground(Color.red);
				editor.setSelectedTextColor(Color.red);
			} else {
				editor.setForeground(Color.black);
				editor.setSelectedTextColor(Color.white);
			}
		else {
			editor.setForeground(Color.black);
			editor.setSelectedTextColor(Color.white);
		}

		// 设置文本
		oldTime = (value == null) ? "" : (isArriveLine ? stop.getArrive()
				: stop.getLeave());
		editor.setText(oldTime);

		return editor;
	}

	public Object getCellEditorValue() {
		String time = Train.formatTime(oldTime, editor.getText());

		// 判断原来是否有数据，既原来的stop是否为null
		if (stop == null) {
			// 原来没有数据，并且用户输入了时间－－设置标志，通知DataModel加入
			if (!time.equals("")) {
				stop = TrainGraphFactory.createInstance(Stop.class, null)
						.setProperties(trainName, time, time, false);
			}
			// 原来没有数据，并且没有输入时间则直接返回空值（什么也不做，让stop=null返回）
		} else {
			// 原来有数据，并且用户删除了时间－－设置标志，通知DataModel删除
			if (!oldTime.equals("") && time.equals("")) {
				stop.setArrive("DEL");
				stop.setLeave("DEL");
			} else {
				if (isArriveLine)
					stop.setArrive(time);
				else
					stop.setLeave(time);
			}
		}

		return stop;
	}
}