package org.paradise.etrc.dialog;

import static org.paradise.etrc.ETRC.__;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.RailroadLine;
import org.paradise.etrc.data.Station;
import org.paradise.etrc.data.skb.ETRCLCB;
import org.paradise.etrc.data.skb.LCBStation;

public class CircuitMakeDialog extends JDialog {
	private static final long serialVersionUID = -5891299808461231142L;

	MainFrame mainFrame;

	private JList<String> list1;
	private JList<String> list2;
	
	private ETRCLCB lcb;
	private String xianlu;
	
	private Vector<LCBStation> allStations;
	private RailroadLine circuit;
	
	public CircuitMakeDialog(MainFrame _mainFrame, String _xianlu) {
		super(_mainFrame, _xianlu, true);
		
		mainFrame = _mainFrame;
		lcb = mainFrame.getLCB();
		xianlu = _xianlu;

		allStations = lcb.findLCBStation(xianlu);
		//Circuit edtingCircuit = makeCircuit(lcb.findLCBStation(xianlu));

		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private RailroadLine makeCircuit(Vector<LCBStation> staLCB) {
		System.out.println(staLCB);
		
		RailroadLine cir = new RailroadLine();
		cir.name = xianlu;
		
		Station firstStation = (Station) staLCB.get(0);
		Station lastStation = (Station) staLCB.get(staLCB.size()-1);
		
		int fisrtDist = firstStation.dist;
		
		for(int i=0; i<staLCB.size(); i++) {
			Station station = (Station) staLCB.get(i);
			
			station.dist = Math.abs(station.dist - fisrtDist);
			cir.appendStation(station);
		}

		if( xianluFirstStationName().equalsIgnoreCase(firstStation.name) &&
			xianluLastStationName().equalsIgnoreCase(lastStation.name))
			cir.name = xianlu;
		else if( xianluFirstStationName().equalsIgnoreCase(lastStation.name) &&
			     xianluLastStationName().equalsIgnoreCase(firstStation.name))
			cir.name = xianlu;
		else
			cir.name = xianlu + firstStation.getOneName()
			           + lastStation.getOneName() + __(" Section");
		
		cir.length = cir.getStation(cir.getStationNum() - 1).dist;

		return cir;
	}
	
	private String xianluFirstStationName() {
		Vector<LCBStation> allStations = lcb.findLCBStation(xianlu);
		return ( (Station) (allStations.get(0)) ).name; 
	}

	private String xianluLastStationName() {
		Vector<LCBStation> allStations = lcb.findLCBStation(xianlu);
		return ( (Station) (allStations.get(allStations.size()-1)) ).name; 
	}

	private void jbInit() throws Exception {
		JButton btOK = new JButton(__("OK"));
		btOK.setFont(new Font("dialog", 0, 12));
		btOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name1 = (String) list1.getSelectedValue();
				String name2 = (String) list2.getSelectedValue();
				
				if ((name1 == null) || (name2 == null))
				{
					(new MessageBox(String.format(__("Please select both the begin station and end station.")))).showMessage();
					return;
				}
				if(new YesNoBox(String.format(__("Please confirm the down-going direction for circuit %s is from %s to %s."), xianlu, name1, name2)).askForYes()) {
					Vector<LCBStation> selectedStations = new Vector<LCBStation>();
					int index1 = list1.getSelectedIndex();
					int index2 = list2.getSelectedIndex();
					
					if(index1 <= index2) {
						for(int i=index1; i<=index2; i++) {
							selectedStations.add(allStations.get(i));
						}
					}
					else {
						for(int i=index1; i>=index2; i--) {
							selectedStations.add(allStations.get(i));
						}
					}
					
					circuit = makeCircuit(selectedStations);
					
					CircuitMakeDialog.this.setVisible(false);
				}
			}
		});

		JButton btCancel = new JButton(__("Cancel"));
		btCancel.setFont(new Font("dialog", 0, 12));
		btCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CircuitMakeDialog.this.setVisible(false);
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(btOK);
		buttonPanel.add(btCancel);

		JPanel rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.add(buildXianluPanel(), BorderLayout.CENTER);
		rootPanel.add(buttonPanel, BorderLayout.SOUTH);

		getContentPane().add(rootPanel);
	}
	
	private JPanel buildXianluPanel() {
		Vector<String> allNames = new Vector<String>();
		for(int i=0; i<allStations.size(); i++) {
			Station sta = (Station) (allStations.get(i));
			allNames.add(sta.name);
		}

		JLabel lb1 = new JLabel(__("From:"));
		lb1.setFont(new Font("dialog", 0, 12));
		list1 = new JList<String>(allNames);
		list1.setFont(new Font("dialog", 0, 12));
		JScrollPane jsp1 = new JScrollPane(list1);
		
		JLabel lb2 = new JLabel(__("To:"));
		lb2.setFont(new Font("dialog", 0, 12));
		list2 = new JList<String>(allNames);
		list2.setFont(new Font("dialog", 0, 12));
		JScrollPane jsp2 = new JScrollPane(list2);
		
		JPanel panel1 = new JPanel();
		panel1.setLayout(new VerticalFlowLayout());
		panel1.add(lb1);
		panel1.add(jsp1);
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new VerticalFlowLayout());
		panel2.add(lb2);
		panel2.add(jsp2);
		
		JPanel xlPanel = new JPanel();
		xlPanel.add(panel1);
		xlPanel.add(panel2);
		
		return xlPanel;
	}

	public RailroadLine getCircuit() {
		Dimension dlgSize = this.getPreferredSize();
		
		if(mainFrame != null) {
			Dimension frmSize = mainFrame.getSize();
			Point loc = mainFrame.getLocation();
			this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
					(frmSize.height - dlgSize.height) / 2 + loc.y);
		}
		
		this.setModal(true);
		this.setVisible(true);
		
		return circuit;
	}
}