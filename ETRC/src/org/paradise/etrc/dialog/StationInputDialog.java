package org.paradise.etrc.dialog;

import static org.paradise.etrc.ETRC.__;

import java.awt.BorderLayout;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.view.widget.JEditTable;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class StationInputDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 9065758632068382095L;
	JPanel panel = new JPanel();
	JTable jTable1 = new JEditTable();
	JPanel panel2 = new JPanel();
	JPanel panel3 = new JPanel();
	JLabel lbStationName = new JLabel();
	JTextField tfStationName = new JTextField();
	JButton btFind = new JButton();
	JButton btCancel = new JButton();
	GridBagLayout gridBagLayout = new GridBagLayout();
	String stationName = null;
	MainFrame mainFrame = null;

	public StationInputDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		try {
			jbInit();
			pack();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public StationInputDialog(MainFrame frame) {
		this(frame, __("Input Station Name"), false);
		mainFrame = frame;
	}

	private void jbInit() throws Exception {
		panel.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		panel.setFont(new java.awt.Font("Dialog", 0, 12));
		panel.setBorder(BorderFactory.createRaisedBevelBorder());

		lbStationName.setText(__("Station name:"));
		lbStationName.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		lbStationName.setPreferredSize(new Dimension(8, 15));
		lbStationName.setToolTipText("");
		lbStationName.setHorizontalAlignment(SwingConstants.RIGHT);

		tfStationName.setMinimumSize(new Dimension(12, 22));
		tfStationName.setPreferredSize(new Dimension(20, 22));
		tfStationName.setColumns(0);
		//tfTrainName.addKeyListener(this);
		tfStationName.setActionCommand("Button_Find");
		tfStationName.addActionListener(this);

		btFind.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		btFind.setText(__("Find"));
		btFind.setActionCommand("Button_Find");
		btFind.addActionListener(this);
		btFind.requestFocus(false);

		btCancel.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		btCancel.setText(__("Cancel"));
		btCancel.setActionCommand("Button_Cancel");
		btCancel.addActionListener(this);

		panel3.setBorder(null);
		panel3.setInputVerifier(null);
		panel3.setLayout(gridBagLayout);

		getContentPane().add(panel);

		panel.add(panel3, BorderLayout.NORTH);

		panel3.add(lbStationName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 87, 7));
		panel3.add(tfStationName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
				,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 75, 0));

		panel.add(panel2, BorderLayout.SOUTH);
		panel2.add(btCancel, null);
		panel2.add(btFind, null);
	}
	/**
	 * actionPerformed
	 *
	 * @param e ActionEvent
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equalsIgnoreCase("Button_Find")) {
			doFind();
		}
		else if(command.equalsIgnoreCase("Button_Cancel")) {
			doCancel();
		}
	}

	public void doFind()
	{
		stationName = tfStationName.getText();
		dispose();
	}

	public void doCancel()
	{
		stationName = null;
		dispose();
	}
}
