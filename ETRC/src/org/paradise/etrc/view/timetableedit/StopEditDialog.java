package org.paradise.etrc.view.timetableedit;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.dialog.DialogBase;
import org.paradise.etrc.util.config.Config;

import com.zearon.util.data.Tuple2;
import com.zearon.util.ui.databinding.UIBindingManager;

import static org.paradise.etrc.ETRC.__;

public class StopEditDialog extends DialogBase implements KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8586856799047966658L;
	
	// Use static fields to keep this object because 
	// eclipse window builder only support static factory methods
	// to create ui components.
	private static StopEditDialog instance;
	private static Vector<Tuple2<Object, String>> allDataBindingComponent = new Vector<>();
	private UIBindingManager uiBindingManager = UIBindingManager.getInstance(this);
	
	private MainFrame mainFrame;
	private TimetableEditSheetTable table;
	private Stop stop;
	private int row;
	private int column;
	
	private Runnable taskOnDialogOpened = null;

	private final JPanel contentPanel = new JPanel();
	private JTextField txtLeaveTime;
	private JTextField txtArriveTime;
	private JTextField txtStoptime;
	private JButton okButton;
	private JLabel lblArriveTime;
	private JLabel lblDepartureTime;
	private JLabel lblDefaultStopTime;
	private JPanel buttonPane;
	private JButton cancelButton;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JSeparator separator_1;
	private JSeparator separator_2;
	private JSeparator separator_3;
	private JRadioButton rdbtnNotGoThrough;
	private JRadioButton rdbtnPass;
	private JRadioButton rdbtnStopWithPassenger;
	private JRadioButton rdbtnStopWithoutPassenger;
	private JCheckBox chckbxChangeFollowingTimes;
	private JRadioButton rdbtnStopAtStartStation;
	private JRadioButton rdbtnStopAtTerminalStation;

	

	/**
	 * Create the dialog.
	 */
	public StopEditDialog(TimetableEditSheetTable table) {
		mainFrame = MainFrame.getInstance();
		this.table = table;
		
		allDataBindingComponent.clear();
		initUI();
		allDataBindingComponent.add(Tuple2.of(buttonGroup, "stop.stopStatus"));
	}
	
	// {{ init UI

	private synchronized void initUI() {
		instance = this;
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				updateRadioButtonText();
				
				if (taskOnDialogOpened != null) {
					taskOnDialogOpened.run();
					taskOnDialogOpened = null;
				}
			}
		});
		
		setModal(true);
		setBounds(100, 100, 443, 228);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel labelPassStatus = createJLabel("Passing Status", new Font("Lucida Grande", Font.PLAIN, 12));
		labelPassStatus.setOpaque(true);
		labelPassStatus.setBackground(SystemColor.window);
		labelPassStatus.setBounds(12, 6, 89, 16);
		contentPanel.add(labelPassStatus);
		{
			separator_3 = new JSeparator();
			separator_3.setOrientation(SwingConstants.VERTICAL);
			separator_3.setBounds(428, 14, 12, 146);
			contentPanel.add(separator_3);
		}
		{
			txtLeaveTime = StopEditDialog.createJTextField(new Font("Lucida Grande", Font.PLAIN, 12), 10, "stop.leaveTime");
			txtLeaveTime.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			txtLeaveTime.setBounds(330, 97, 64, 28);
			contentPanel.add(txtLeaveTime);
		}
		{
			txtArriveTime = StopEditDialog.createJTextField(new Font("Lucida Grande", Font.PLAIN, 12), 10, "stop.arriveTime");
			txtArriveTime.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			txtArriveTime.setBounds(330, 62, 64, 28);
			contentPanel.add(txtArriveTime);
		}
		{
			lblArriveTime = StopEditDialog.createJLabel("Arrival Time", new Font("Lucida Grande", Font.PLAIN, 12));
			lblArriveTime.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblArriveTime.setBounds(215, 67, 98, 16);
			contentPanel.add(lblArriveTime);
		}
		{
			lblDepartureTime = StopEditDialog.createJLabel("Departure Time", new Font("Lucida Grande", Font.PLAIN, 12));
			lblDepartureTime.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblDepartureTime.setBounds(215, 102, 97, 16);
			contentPanel.add(lblDepartureTime);
		}
		{
			txtStoptime = createJTextField(new Font("Lucida Grande", Font.PLAIN, 12), 10, "stop.DEFAULT_STOP_TIME");
			txtStoptime.setBounds(330, 24, 64, 28);
			contentPanel.add(txtStoptime);
		}
		{
			lblDefaultStopTime = StopEditDialog.createJLabel("Default Stop Time", new Font("Lucida Grande", Font.PLAIN, 12));
			lblDefaultStopTime.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblDefaultStopTime.setBounds(215, 30, 103, 15);
			contentPanel.add(lblDefaultStopTime);
		}
		
		JSeparator separator = new JSeparator();
		separator.setForeground(Color.LIGHT_GRAY);
		separator.setBounds(202, 52, 232, 16);
		contentPanel.add(separator);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel.setBounds(6, 15, 197, 145);
		contentPanel.add(panel);
		panel.setLayout(null);
		
		rdbtnNotGoThrough = new JRadioButton("<html>Not go through (<u>Q</u>)</html>\n");
		rdbtnNotGoThrough.setName("Not go through");
		rdbtnNotGoThrough.setMnemonic('n');
		rdbtnNotGoThrough.setBounds(4, 6, 187, 23);
		panel.add(rdbtnNotGoThrough);
		buttonGroup.add(rdbtnNotGoThrough);
		rdbtnNotGoThrough.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		rdbtnPass = new JRadioButton("Pass");
		rdbtnPass.setName("Pass");
		rdbtnPass.setBounds(4, 29, 187, 23);
		panel.add(rdbtnPass);
		buttonGroup.add(rdbtnPass);
		rdbtnPass.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		rdbtnStopAtStartStation = new JRadioButton("Stop at start station");
		rdbtnStopAtStartStation.setActionCommand("Stop at start station");
		buttonGroup.add(rdbtnStopAtStartStation);
		rdbtnStopAtStartStation.setName("Stop at start station");
		rdbtnStopAtStartStation.setMnemonic('p');
		rdbtnStopAtStartStation.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		rdbtnStopAtStartStation.setBounds(4, 52, 187, 23);
		panel.add(rdbtnStopAtStartStation);
		
		rdbtnStopAtTerminalStation = new JRadioButton("Stop at terminal station");
		buttonGroup.add(rdbtnStopAtTerminalStation);
		rdbtnStopAtTerminalStation.setName("Stop at terminal station");
		rdbtnStopAtTerminalStation.setMnemonic('p');
		rdbtnStopAtTerminalStation.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		rdbtnStopAtTerminalStation.setBounds(4, 75, 187, 23);
		
		rdbtnStopWithPassenger = new JRadioButton("Stop");
		rdbtnStopWithPassenger.setName("Stop");
		rdbtnStopWithPassenger.setMnemonic('p');
		rdbtnStopWithPassenger.setBounds(4, 98, 187, 23);
		panel.add(rdbtnStopWithPassenger);
		buttonGroup.add(rdbtnStopWithPassenger);
		rdbtnStopWithPassenger.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		
		rdbtnStopWithoutPassenger = new JRadioButton("Technical Stop");
		rdbtnStopWithoutPassenger.setName("Technical Stop");
		rdbtnStopWithoutPassenger.setMnemonic('s');
		rdbtnStopWithoutPassenger.setBounds(4, 121, 187, 23);
		panel.add(rdbtnStopWithoutPassenger);
		buttonGroup.add(rdbtnStopWithoutPassenger);
		rdbtnStopWithoutPassenger.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		panel.add(rdbtnStopAtTerminalStation);
		{
			separator_1 = new JSeparator();
			separator_1.setForeground(Color.LIGHT_GRAY);
			separator_1.setBounds(202, 10, 232, 16);
			contentPanel.add(separator_1);
		}
		{
			separator_2 = new JSeparator();
			separator_2.setForeground(Color.LIGHT_GRAY);
			separator_2.setBounds(202, 154, 232, 16);
			contentPanel.add(separator_2);
		}
		
		chckbxChangeFollowingTimes = new JCheckBox("<html>E<u>d</u>it following times accordingly</html>");
		chckbxChangeFollowingTimes.setName("table.continuousEditMode");
		chckbxChangeFollowingTimes.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		chckbxChangeFollowingTimes.setBounds(202, 127, 233, 23);
		allDataBindingComponent.add(Tuple2.of(chckbxChangeFollowingTimes, chckbxChangeFollowingTimes.getName()));
		contentPanel.add(chckbxChangeFollowingTimes);
		{
			buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (table != null)
							table.moveToNextCell();
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setVisible(false);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		setTitle(__("Edit Stop"));
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source text "Passing Status"
	 * @wbp.factory.parameter.source font new java.awt.Font("Lucida Grande", java.awt.Font.PLAIN, 12)
	 */
	public static JLabel createJLabel(String text, Font font) {
		JLabel label = new JLabel(__(text));
		label.setFont(font);
		return label;
	}
	
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source font new java.awt.Font("Lucida Grande", java.awt.Font.PLAIN, 12)
	 * @wbp.factory.parameter.source columns 10
	 * @wbp.factory.parameter.source name "stop.DEFAULT_STOP_TIME"
	 */
	public static JTextField createJTextField(Font font, int columns, String name) {
		JTextField textField = new JTextField();
		textField.setFont(font);
		textField.setColumns(columns);
		textField.setName(name);
		textField.addKeyListener(instance);
		
		allDataBindingComponent.add(Tuple2.of(textField, name));
		
		return textField;
	}
	
	// }}
	
	public void setStop(Stop stop, int row, int column) {
		this.stop = stop;
		this.row = row;
		this.column = column;
		
		setupUIDataBinding();
	}

	public void showDialog(TimetableEditSheetTable table, boolean isArrivalTimeRow) {
		showDialog(table, isArrivalTimeRow, (char) 0); 
	}
	
	public void showDialog(TimetableEditSheetTable table,
			boolean isArrivalTimeRow, char pressedKey) {
		
		// make the dialog in the center
		Rectangle dlgSize = getBounds();
		Dimension frmSize;
		Point loc;
		frmSize = Toolkit.getDefaultToolkit().getScreenSize();
		loc = new Point(0,0);
		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		
		// update data with data bindings
		uiBindingManager.updateUI(null);
		
		// Called when dialog is opened.
		taskOnDialogOpened = () -> {
			// set focus
			JTextField timeField = txtArriveTime;
			if (!isArrivalTimeRow)
				timeField = txtLeaveTime;
			timeField.requestFocus();
			JTextField timeField0 = timeField;
			
			// A one-time temp focus listener.
			FocusListener listener = new FocusListener() {
				@Override public void focusLost(FocusEvent e) {}
				@Override
				public void focusGained(FocusEvent e) {
					if (pressedKey >= '0' && pressedKey <= '9') {
						timeField0.setText("" + pressedKey);
						timeField0.select(1, 1);
					}
					timeField0.removeFocusListener(this);
				}
			};
			timeField.addFocusListener(listener);
			
			// set passing status
			if (pressedKey != (char) 0) {
				if (pressedKey >= '0' && pressedKey <= '9') {
					rdbtnStopWithPassenger.setSelected(true);
				} else {
					setPassingStatusByPressingKey(pressedKey);
				}
			}
		};
		
		// show dialog
		super.setVisible(true);
	}
	
	private void setPassingStatusByPressingKey(char keyPressed) {
		String keyString = String.valueOf(keyPressed).toUpperCase();
		Config config = Config.getInstance();
		if (keyString.equalsIgnoreCase(config.getKeyTE_NotGoThrough()))
			rdbtnNotGoThrough.setSelected(true);
		else if (keyString.equalsIgnoreCase(config.getKeyTE_PASS()))
			rdbtnPass.setSelected(true);
		else if (keyString.equalsIgnoreCase(config.getKeyTE_StopAtStartStation()))
			rdbtnStopAtStartStation.setSelected(true);
		else if (keyString.equalsIgnoreCase(config.getKeyTE_StopAtTerminalStation()))
			rdbtnStopAtTerminalStation.setSelected(true);
		else if (keyString.equalsIgnoreCase(config.getKeyTE_StopForPassenger()))
			rdbtnStopWithPassenger.setSelected(true);
		else if (keyString.equalsIgnoreCase(config.getKeyTE_StopNoPassenger()))
			rdbtnStopWithoutPassenger.setSelected(true);
		
		repaint();
	}
	
	private void updateRadioButtonText() {
		Config config = Config.getInstance();

		rdbtnNotGoThrough.setText(String.format("<html>%s (<u>%s</u>)</html>",
				rdbtnNotGoThrough.getName(), config.getKeyTE_NotGoThrough()));
		rdbtnPass.setText(String.format("<html>%s (<u>%s</u>)</html>",
				rdbtnPass.getName(), config.getKeyTE_PASS()));
		rdbtnStopAtStartStation.setText(String.format("<html>%s (<u>%s</u>)</html>", 
				rdbtnStopAtStartStation.getName(), config.getKeyTE_StopAtStartStation()));
		rdbtnStopAtTerminalStation.setText(String.format("<html>%s (<u>%s</u>)</html>", 
				rdbtnStopAtTerminalStation.getName(), config.getKeyTE_StopAtTerminalStation()));
		rdbtnStopWithPassenger.setText(String.format("<html>%s (<u>%s</u>)</html>", 
				rdbtnStopWithPassenger.getName(), config.getKeyTE_StopForPassenger()));
		rdbtnStopWithoutPassenger.setText(String.format("<html>%s (<u>%s</u>)</html>", 
				rdbtnStopWithoutPassenger.getName(), config.getKeyTE_StopNoPassenger()));
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char keyChar = e.getKeyChar();
		if ((keyChar >= 'A' && keyChar <= 'Z') || (keyChar >= 'a' && keyChar <= 'z'))
			e.consume();

		if (keyChar == 'd' || keyChar == 'E') {
			chckbxChangeFollowingTimes.setSelected(!chckbxChangeFollowingTimes.isSelected());
			return;
		}
		
		setPassingStatusByPressingKey(keyChar);
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	// {{ Data Bindings
	
	private void setupUIDataBinding() {
		uiBindingManager.clearDataBinding();
		for (Tuple2<Object, String> componentTuple : allDataBindingComponent) {
			uiBindingManager.addDataBinding(componentTuple.A, componentTuple.B, 
					this::getModelObject, this::getPropertyDesc, null, this::updateUIforModel);
		}
	}
	
	private Object getModelObject(String propertyGroup) {
		if ("stop".equals(propertyGroup)) {
			return stop;
		} else if ("table".equals(propertyGroup)) {
			return table;
		}
		
		return null;
	}
	
	private void updateUIforModel(String propertyGroup) {
			mainFrame.runningChartView.refresh();
			
			mainFrame.timetableEditView.refreshStopCell(row, column);
	}
	
	private String getPropertyDesc(String propertyName) {
		String desc = propertyName;
		
		if ("DEFAULT_STOP_TIME".equals(propertyName)) {
			desc = String.format(__("%s"), __("default stop minutes at any stop.") );
		} else if ("stopStatus".equals(propertyName)) {
			desc = String.format(__("%s at %s"), __("passing status"), stop.getName() );
		} else if ("arriveTime".equals(propertyName)) {
			desc = String.format(__("%s at %s"), __("arrival time"), stop.getName() );
		} else if ("leaveTime".equals(propertyName)) {
			desc = String.format(__("%s at %s"), __("departure time"), stop.getName() );
		}
		
		else if ("continuousEditMode".equals(propertyName)){
			desc = String.format(__("continuous edit mode of timetable") );
		}
		
		return desc;
	}
}
