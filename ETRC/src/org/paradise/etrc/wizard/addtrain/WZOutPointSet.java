package org.paradise.etrc.wizard.addtrain;

import static org.paradise.etrc.ETRC.__;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Station;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.wizard.WizardDialog;

public class WZOutPointSet extends WizardDialog {
	private static final long serialVersionUID = 1558550027322954767L;

	private Train train;
	private RailroadLineChart chart;
	JTextArea info;
	JTextField tfTime;
	
	String oldTime = "00:00";
	Station curStation;

	private JList<String> circuitList;
	
	public WZOutPointSet(JFrame _frame, int _step, String _wizardTitle, String _stepTitle) {
		super(_frame, _step, _wizardTitle, _stepTitle);
		
		this.canNext = false;
	}

	public void setData(RailroadLineChart _chart, Train _train) {
		chart = _chart;
		train = _train;
		
		if(train == null)
			setTitle(wizardTitle);
		else
			setTitle(wizardTitle + " (" +train.getTrainName() + ")");
	}

	protected JPanel createStepPane() {
		JPanel panel = new JPanel();

		circuitList = new JList<String>();
		circuitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		circuitList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent lse) {
				int row = ((JList<?>) lse.getSource()).getSelectedIndex();
				
				if(row < 0) {
					curStation = null;
					tfTime.setText("");
				}
				else {
					curStation = chart.railroadLine.getStation(row);
					tfTime.setText(calculTime());
				}
			}
        });
		
		JScrollPane spList = new JScrollPane(circuitList);
		
		JPanel upPanel = new JPanel();
		upPanel.setLayout(new BorderLayout());
		upPanel.add(spList, BorderLayout.CENTER);
		upPanel.add(createTimePane(), BorderLayout.SOUTH);
		
		panel.setLayout(new BorderLayout());
		panel.add(upPanel, BorderLayout.CENTER);
		panel.add(createInfoField(), BorderLayout.SOUTH);

		return panel;
	}

	private Component createTimePane() {
		JPanel panel = new JPanel();
		
		JLabel lb = new JLabel(__("Terminal/End Time:"));
		tfTime = new JTextField();
		tfTime.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				tfTime.setText(calculTime());
			}

			public void focusLost(FocusEvent arg0) {
				setTime(tfTime.getText().trim());
			}
		});
		
		panel.setLayout(new BorderLayout());
		panel.add(lb, BorderLayout.WEST);
		panel.add(tfTime, BorderLayout.CENTER);
		
		panel.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));
		
		return panel;
	}

	//计算当前选中车站的推算通过时间（如果有图定时间则取到达时间）
	private String calculTime() {
		if(curStation == null)
			return "";
		
		//有这个停站
		int stopIndex = train.findStopIndex(curStation.getName());
		if(stopIndex >= 0) {
			return train.getStop(stopIndex).getLeave();
		}
		else {
			Station lastStation = chart.railroadLine.getLastStopOnMe(train);
			
			if(lastStation == null)
				return "";
			
			int distGap = curStation.dist - lastStation.dist;
			int timeGap = distGap * 60 / Train.getDefaultVelocityByName(train.getTrainName());
			
			Stop stop = train.findStop(lastStation.getName());
			
			int timeA = Train.trainTimeToInt(stop.getLeave());
			int timeIn = 0;
			if(train.isDownTrain(chart.railroadLine) == Train.DOWN_TRAIN)
				timeIn = timeA + timeGap;
			else
				timeIn = timeA - timeGap;
			
			return Train.intToTrainTime(timeIn);
		}
	}
	
	protected void setTime(String input) {
		oldTime = Train.formatTime(oldTime, input.trim());
		tfTime.setText(oldTime);
	}

	private JComponent createInfoField() {
		info = new JTextArea();
		
		info.setFont(new Font(__("FONT_NAME"), Font.PLAIN, 12));
		info.setCaret(new DefaultCaret() {  
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {  
				return false;
			}
		});
		info.setEditable(false);
		info.setFocusable(false);
		info.setLineWrap(true);
		info.setForeground(new Color(40,80,40));
		info.setBackground(this.getBackground());
		info.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		return info;
	}

	protected void updateStepPane() {
		//设置List选项
		String[] dispNames = new String[chart.railroadLine.getStationNum()];
		for(int i=0; i<chart.railroadLine.getStationNum(); i++) {
			String strDist = "" + chart.railroadLine.getStation(i).dist;
			while(strDist.length() < 4) {
				strDist = " " + strDist;
			}
			dispNames[i] = String.format(__(" %s down-going direction %s km from %s station: %s station"), 
					chart.railroadLine.getName(), strDist, chart.railroadLine.getStation(0).getName(),   chart.railroadLine.getStation(i).getName()); 

		}
		circuitList.setListData(dispNames);

		//设置当前选中的车站
		if(chart.railroadLine.isEndInsideMe(train)) {
			info.setText(String.format(__("  Train %s terminates at %s station in this section, no need to set end point"), train.getTrainName(), train.getTerminalStation()));
			curStation = chart.railroadLine.getStation(train.getTerminalStation());
		}
		else {
			curStation = chart.railroadLine.getLastStopOnMe(train);
			if(curStation != null) {
				info.setText(String.format(__("  The last stop of train %s in this section is %s, change the station and time if this is not correct."), train.getTrainName(), curStation.getName()));
			}
			else {
				info.setText(String.format(__("  The train %s passes this section, set the end point manually."), train.getTrainName()));
			}
		}

		int index = -1;
		if(curStation != null)
			index = chart.railroadLine.getStationIndex(curStation.getName());
		
		circuitList.setSelectedIndex(index);
		circuitList.ensureIndexIsVisible(index);
	}

	public Dimension getPreferredSize() {
		return new Dimension(380, 380);
	}
	
	public void doFinish() {
		String time = tfTime.getText().trim();
		if(time.equalsIgnoreCase(""))
			return;
		else {
			//TODO: train.xstop.setTime;
			Stop stop = TrainGraphFactory.createInstance(Stop.class, curStation.getName())
					.setProperties(null, time, time, false);
			chart.insertNewStopToTrain(train, stop);
			
			train.setTrainNameForAllStops();
			
			super.doFinish();
		}
	}
}
