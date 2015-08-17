package org.paradise.etrc.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.paradise.etrc.MainFrame;
//import com.borland.jbcl.layout.*;
import org.paradise.etrc.data.v1.ChartSettings;
import org.paradise.etrc.data.v1.RailroadLineChart;

import static org.paradise.etrc.ETRC.__;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class TimeSetDialog extends DialogBase implements ActionListener {
	private static final long serialVersionUID = -1303484994776431876L;

	MainFrame mainFrame = null;

  JPanel panel = new JPanel();
  VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
  JPanel jPanel2 = new JPanel();
  JLabel lbMinScale = new JLabel();
  JTextField tfMinScale = new JTextField();
  JLabel lbInterval = new JLabel();
  JTextField tfInterval = new JTextField();
  JButton btOK = new JButton();
  JButton btDefault = new JButton();
  TitledBorder titledBorder1;
  JLabel statusBar = new JLabel();
  JPanel jPanel3 = new JPanel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JLabel jLabel1 = new JLabel();

  String defaultStatus = __("Timeline Settings");
  JLabel jLabel2 = new JLabel();
  JTextField tfStart = new JTextField();
  JLabel lbStart = new JLabel();
  JLabel jLabel3 = new JLabel();

  RailroadLineChart chart;

private ChartSettings settings;
  public TimeSetDialog(ChartSettings settings, Frame frame) {
    super(frame, __("Timeline Settings"), false);

    if(frame instanceof MainFrame) {
      mainFrame = (MainFrame) frame;
    }
//    chart = _chart;
    setModel(settings);
    
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
	
	public void setModel(ChartSettings settings) {
		this.settings = settings;
	}

  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder("");
    panel.setLayout(verticalFlowLayout1);
    this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
    this.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    panel.setFont(new java.awt.Font("Dialog", 0, 12));
    panel.setBorder(BorderFactory.createRaisedBevelBorder());





    lbMinScale.setText(__("Pixel per min:"));
    lbMinScale.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    lbMinScale.setPreferredSize(new Dimension(8, 15));
    lbMinScale.setToolTipText("");
    lbMinScale.setHorizontalAlignment(SwingConstants.RIGHT);

    tfMinScale.setMinimumSize(new Dimension(12, 22));
    tfMinScale.setPreferredSize(new Dimension(20, 22));
    tfMinScale.setText(settings.minuteScale+"");
    tfMinScale.setColumns(0);

    lbInterval.setText(__("Y-axis gap (min):"));
    lbInterval.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    lbInterval.setPreferredSize(new Dimension(32, 15));
    lbInterval.setRequestFocusEnabled(true);
    lbInterval.setToolTipText("");
    lbInterval.setHorizontalAlignment(SwingConstants.RIGHT);

    tfInterval.setMinimumSize(new Dimension(24, 22));
    tfInterval.setPreferredSize(new Dimension(20, 22));
    tfInterval.setText(settings.timeInterval+"");

    btOK.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    btOK.setText(__("Set"));
    btOK.setActionCommand("Button_OK");
    btOK.addActionListener(this);

    btDefault.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    btDefault.setText(__("Default"));
    btDefault.setActionCommand("Button_Default");
    btDefault.addActionListener(this);





    titledBorder1.setTitlePosition(2);
    titledBorder1.setTitleFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    titledBorder1.setTitle(__("Timeline Settings"));

    statusBar.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
    statusBar.setText(__("Set timeline settings"));
    verticalFlowLayout1.setHgap(2);
    verticalFlowLayout1.setVgap(2);

    jPanel3.setBorder(titledBorder1);
    jPanel3.setInputVerifier(null);
    jPanel3.setLayout(gridBagLayout2);
    jLabel1.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    jLabel1.setHorizontalAlignment(SwingConstants.LEFT);
    jLabel1.setText(__("min"));
    jLabel2.setText(__(" Must be a divider of 60"));
    jLabel2.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    jLabel2.setToolTipText("");
    jLabel2.setHorizontalAlignment(SwingConstants.LEFT);
    jLabel2.setHorizontalTextPosition(SwingConstants.LEFT);

    tfStart.setColumns(0);
    tfStart.setText(settings.startHour+"");
    tfStart.setPreferredSize(new Dimension(20, 22));
    tfStart.setMinimumSize(new Dimension(12, 22));

    lbStart.setHorizontalAlignment(SwingConstants.RIGHT);
    lbStart.setToolTipText("");
    lbStart.setPreferredSize(new Dimension(8, 15));
    lbStart.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    lbStart.setText(__("Time for 0 pos:"));

    jLabel3.setText(" ");
    jLabel3.setHorizontalAlignment(SwingConstants.LEFT);
    jLabel3.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
    getContentPane().add(panel);

    panel.add(jPanel3, null);
    jPanel3.add(tfInterval,                        new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 75, 0));
    jPanel3.add(lbMinScale,                   new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 87, 7));
    jPanel3.add(tfMinScale,                      new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 75, 0));
    jPanel3.add(lbInterval,                   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 63, 7));

    panel.add(jPanel2, null);
    jPanel2.add(btDefault, null);
    jPanel2.add(btOK, null);
    panel.add(statusBar, null);
    jPanel3.add(jLabel1,                    new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 1, 0, 10), 0, 0));
    jPanel3.add(jLabel2,                         new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
    jPanel3.add(tfStart,                 new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    jPanel3.add(lbStart,           new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    jPanel3.add(jLabel3,       new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 1, 0, 0), 0, 0));
  }

  /**
   * actionPerformed
   *
   * @param e ActionEvent
   */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if(command.equalsIgnoreCase("Button_OK")) {
      doOK();
    }
    else if(command.equalsIgnoreCase("Button_Default")) {
      doDefault();
    }
    else if(command.equalsIgnoreCase("Button_Test")) {
      doTest();
    }
  }

  /**
   * doTest
   */
  private void doTest() {
    //this.mainFrame.remove(mainFrame.panelChart);
    //this.mainFrame.getContentPane().add(mainFrame.panelChart, BorderLayout.CENTER);
    //this.mainFrame.panelChart.spLines.getRootPane().setSize(400,800);
    mainFrame.repaint();
  }

  /**
   * doDefault
   */
  private void doDefault() {
    this.tfStart.setText("18");
    this.tfMinScale.setText("2");
    this.tfInterval.setText("10");

    this.statusBar.setText(defaultStatus);
  }

  /**
   * doOK
   */
  private void doOK() {
    String stStart = tfStart.getText();
    String stMinScale = tfMinScale.getText();
    String stInterval = tfInterval.getText();

    int start = 0;
    int minScale = 2;
    int interval = 10;
    try{
      start = Integer.parseInt(stStart);
      minScale = Integer.parseInt(stMinScale);
      interval = Integer.parseInt(stInterval);

      if(!( (start >=0 && start <=23)
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
              || (interval == 60)))) {
        this.statusBar.setText(__("Input data out of range."));
      }
      else{
        if(this.mainFrame != null) {
        	settings.startHour = start;
        	settings.minuteScale = minScale;
        	settings.timeInterval = interval;

          mainFrame.validate();
          mainFrame.getChartView().resetSize();
        }
        this.statusBar.setText(defaultStatus);
      }
    }
    catch(NumberFormatException e) {
      this.statusBar.setText(__("Invalid input"));
    }
  }
}
