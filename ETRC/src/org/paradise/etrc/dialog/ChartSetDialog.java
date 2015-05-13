package org.paradise.etrc.dialog;

import static org.paradise.etrc.ETRC.__;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.ChartSettings;
import org.paradise.etrc.data.RailroadLineChart;
import org.paradise.etrc.data.TrainGraph;
import org.paradise.etrc.view.chart.ChartView;

public class ChartSetDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private MainFrame mainFrame;
	private JTabbedPane tbPane;
	private JLabel statusBar;
	
	private JTextField d0;
	private JTextField d1;
	private JTextField d2;

	private JTextField t0;
	private JTextField t1;
	private JTextField t2;

	private ChartSettings settings;
	
	private static String defaultStatus = __("Settings for Train Graph");

	public ChartSetDialog(ChartSettings settings, MainFrame _mainFrame) {
		super(_mainFrame, __("Settings for Train Graph"), false);
		mainFrame = _mainFrame;
		setModel(settings);

		init();
	}
	
	public void setModel(ChartSettings settings) {
		this.settings = settings;
	}

	private void init() {
		tbPane = new JTabbedPane();
		
		RailroadLineChart chart = mainFrame.currentLineChart;
		
		d0 = createJTextField("" + settings.distScale);
		d1 = createJTextField("" + settings.displayLevel);
		d2 = createJTextField("" + settings.boldLevel);
		
		t0 = createJTextField("" + settings.startHour);
		t1 = createJTextField("" + settings.minuteScale);
		t2 = createJTextField("" + settings.timeInterval);
		
		JPanel distPanel = creatJPanel(
                createJLabelL(__("Pixels per km:")), d0, createJLabelR(" "),
				createJLabelL(__("Display level:")), d1, createJLabelR(" "),
				createJLabelL(__("Bold line level:")), d2, createJLabelR(" "),
                createJLabelM(__(" The highest station level is 0")));
		
		JPanel timePanel = creatJPanel(				
				createJLabelL(__("Time for 0 pos:")), t0, createJLabelR(" "),
				createJLabelL(__("Pixel per min:")), t1, createJLabelR(" "),
				createJLabelL(__("Y-axis gap:")), t2, createJLabelR("min"),
				createJLabelM(__(" Must be a divider of 60")));


        tbPane.add(__("Distance bar"), distPanel);
		tbPane.add(__("Timeline"), timePanel);

	    statusBar = new JLabel(defaultStatus);
	    statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		
		this.getRootPane().setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		this.setResizable(false);
		
		this.setLayout(new BorderLayout());
		this.add(tbPane, BorderLayout.NORTH);
		this.add(createDownPanel(), BorderLayout.CENTER);
		this.add(statusBar, BorderLayout.SOUTH);
	}
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		
		JPanel panelBT = new JPanel();
		JButton btDefault = createJButton(__("Default"));
		JButton btOK      = createJButton(__("Set"));
	
		panelBT.add(btDefault);
		panelBT.add(btOK);
		
		panel.setLayout(new BorderLayout());
		panel.add(panelBT, BorderLayout.EAST);
		
		return panel;
	}

	private JPanel createDownPanel() {
		JPanel panel = new JPanel();
		
		JCheckBox cbDrawPoint;
		cbDrawPoint = new JCheckBox();
		cbDrawPoint.setFont(new java.awt.Font("Dialog", 0, 12));
		cbDrawPoint.setText(__("Always highlight terminals"));
		cbDrawPoint.setSelected(mainFrame.chartView.isDrawNormalPoint);
		cbDrawPoint.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (((JCheckBox) e.getSource()).isSelected())
					mainFrame.chartView.isDrawNormalPoint = true;
				else
					mainFrame.chartView.isDrawNormalPoint = false;
				
				mainFrame.chartView.repaint();
			}
		});
		
		JCheckBox cbUnderColor;
		cbUnderColor = new JCheckBox();
		cbUnderColor.setFont(new java.awt.Font("Dialog", 0, 12));
		cbUnderColor.setText(__("Enable watermark display"));
		cbUnderColor.setSelected(!(mainFrame.chartView.underDrawingColor == null));
		cbUnderColor.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (((JCheckBox) e.getSource()).isSelected())
					mainFrame.chartView.underDrawingColor = ChartView.DEFAULT_UNDER_COLOR;
				else
					mainFrame.chartView.underDrawingColor = null;
				
				mainFrame.chartView.repaint();
			}
		});
		
		JPanel panelCB = new JPanel();
		panelCB.setLayout(new GridLayout(1,2));
		panelCB.add(cbUnderColor);
		panelCB.add(cbDrawPoint);
		
		panel.setLayout(new BorderLayout());
		panel.add(panelCB, BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);
		
		return panel;
	}
	
	private JButton createJButton(String name) {
		JButton bt = new JButton(name);
		bt.setActionCommand(name);
		
		bt.setPreferredSize(new Dimension(62, 24));
		bt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if(ae.getActionCommand().equals(__("Default")))
					setDefault();
				else
					setValues();
			}
		});
		
		return bt;
	}

	public void editSettings() {
		Dimension dlgSize = getPreferredSize();
		Dimension frmSize = mainFrame.getSize();
		Point loc = mainFrame.getLocation();
		setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				    (frmSize.height - dlgSize.height) / 2 + loc.y);
		
		pack();
		this.setSize(dlgSize);
		setVisible(true);
	}
	
	public Dimension getPreferredSize() {
		int w = 270;
		int h = tbPane.getPreferredSize().height + 108;
		return new Dimension(w, h);
	}
	
	private JPanel creatJPanel(JLabel lbL0, JTextField tfM0, JLabel lbR0,
			                   JLabel lbL1, JTextField tfM1, JLabel lbR1, 
			                   JLabel lbL2, JTextField tfM2, JLabel lbR2, 
			                   JLabel lbM3) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		panel.add(lbL0, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, 
				new Insets(10, 0, 0, 0), 75, 7));
		panel.add(tfM0, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 75, 0));
		panel.add(lbR0, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, 
				new Insets(0, 1, 0, 10), 0, 0));

		panel.add(lbL1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, 
				new Insets(0, 0, 0, 0), 75, 7));
		panel.add(tfM1, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 75, 0));
		panel.add(lbR1, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, 
				new Insets(0, 1, 0, 10), 0, 0));
		
		panel.add(lbL2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 75, 7));
		panel.add(tfM2, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(2, 2, 2, 2), 75, 0));
		panel.add(lbR2, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, 
				new Insets(0, 1, 0, 10), 0, 0));

		panel.add(lbM3, new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 5, 0), 0, 0));

		return panel;
	}

	private JLabel createJLabelL(String name) {
		JLabel lb = new JLabel(name);
		lb.setFont(new Font("Dialog", Font.PLAIN, 12));
		lb.setPreferredSize(new Dimension(32, 15));
		lb.setHorizontalAlignment(SwingConstants.RIGHT);

		return lb;
	}

	private JLabel createJLabelR(String name) {
		JLabel lb = new JLabel(name);
		lb.setFont(new Font("Dialog", Font.PLAIN, 12));
		lb.setPreferredSize(new Dimension(32, 15));
	    lb.setHorizontalAlignment(SwingConstants.LEFT);

		return lb;
	}
	
	private JLabel createJLabelM(String name) {
		JLabel lb = new JLabel(name);
		lb.setFont(new Font("Dialog", Font.PLAIN, 12));
		lb.setHorizontalAlignment(SwingConstants.LEFT);
		lb.setHorizontalTextPosition(SwingConstants.LEFT);

		return lb;
	}

	private JTextField createJTextField(String value) {
		JTextField tf = new JTextField();
		tf.setPreferredSize(new Dimension(12, 22));
		tf.setText(value);
		tf.setColumns(0);

		return tf;
	}

	private void setDefault() {
	    d0.setText("3");
	    d1.setText("4");
	    d2.setText("2");
	    
	    t0.setText("18");
	    t1.setText("2");
	    t2.setText("10");
	}
	
	private void setValues() {
		RailroadLineChart chart = mainFrame.currentLineChart;
		
	    String stDistScale = d0.getText();
	    String stDisplay = d1.getText();
	    String stBold = d2.getText();
	    
	    String stStart = t0.getText();
	    String stMinScale = t1.getText();
	    String stInterval = t2.getText();

	    float distScale = 3;
	    int display = 4;
	    int bold = 2;
	    
	    int start = 0;
	    int minScale = 2;
	    int interval = 10;
	    try{
	      distScale = Float.parseFloat(stDistScale);
	      display = Integer.parseInt(stDisplay);
	      bold = Integer.parseInt(stBold);

	      start = Integer.parseInt(stStart);
	      minScale = Integer.parseInt(stMinScale);
	      interval = Integer.parseInt(stInterval);

	      if(
	         (!((distScale >= 1 && distScale <= 10)
	           &&(display >= 0 && display <= 6)
	           &&(bold >=0 && bold <= 6)
	           &&(bold <= display))) 
	           
	         || 
             
	         (!( (start >=0 && start <=23)
        	     && (minScale >= 1 && minScale <= 10)
        	     && ((interval == 1)
    	              || (interval == 2)
    	              || (interval == 3)
    	              || (interval == 4)
    	              || (interval == 5)
    	              || (interval == 6)
    	              || (interval == 10)
    	              || (interval == 12)
    	              || (interval == 15)
    	              || (interval == 20)
    	              || (interval == 30)
    	              || (interval == 60))))	           
	        ){

	        this.statusBar.setText(__("Input data out of range."));
	      }
	      
	      else{
	          settings.distScale = distScale;
	          settings.displayLevel = display;
	          settings.boldLevel = bold;

	          settings.startHour = start;
	          settings.minuteScale = minScale;
	          settings.timeInterval = interval;

	          mainFrame.chartView.resetSize();
	          mainFrame.runView.refresh();
	          
	          this.statusBar.setText(defaultStatus);
	      }
	    }
	    catch(NumberFormatException e) {
	      this.statusBar.setText(__("Invalid input"));
	    }
	  }
}
