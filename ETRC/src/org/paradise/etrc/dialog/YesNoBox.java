package org.paradise.etrc.dialog;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.paradise.etrc.ETRC;

import static org.paradise.etrc.ETRC.__;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class YesNoBox extends JDialog implements ActionListener {
	private static final long serialVersionUID = 256443803232381267L;
	
	Boolean saidYes = false;

	JPanel panel1 = new JPanel();

	JButton buttonYes = new JButton();
	JButton buttonNo = new JButton();
	JButton buttonCancel = new JButton();

	JLabel imageLabel = new JLabel();
	JLabel msgLabel = new JLabel();

	ImageIcon image1 = new ImageIcon();

	String msg = __("Message Content");
	String title = __("Message");

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel jPanel1 = new JPanel();
	JPanel jPanel2 = new JPanel();

	Frame frame;

	public YesNoBox(Frame parent, String _msg, String _title) {
		super(parent);
		msg = _msg;
		title = _title;
		frame = parent;
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public YesNoBox(Frame parent, String _msg) {
		this(parent, _msg, __("Question"));
	}

	public YesNoBox(String _msg) {
		this(ETRC.getInstance().getMainFrame(), _msg, __("Question"));
	}

	//Component initialization
	private void jbInit() throws Exception {
		image1 = new ImageIcon(org.paradise.etrc.MainFrame.class
				.getResource("/pic/msg.png"));
		imageLabel.setIcon(image1);
		this.setTitle(title);
		panel1.setLayout(borderLayout1);
		
		buttonYes.setFont(new java.awt.Font("Dialog", 0, 12));
		buttonYes.setActionCommand("Yes");
		buttonYes.setText(__("Yes"));
		buttonYes.addActionListener(this);
		
		buttonNo.setFont(new java.awt.Font("Dialog", 0, 12));
		buttonNo.setActionCommand("No");
		buttonNo.setText(__("No"));
		buttonNo.addActionListener(this);
		
		buttonCancel.setFont(new java.awt.Font("Dialog", 0, 12));
		buttonCancel.setActionCommand("Cancel");
		buttonCancel.setText(__("Cancel"));
		buttonCancel.addActionListener(this);

		msgLabel.setFont(new java.awt.Font("Dialog", 0, 12));
		msgLabel.setText(msg);

		panel1.setBorder(BorderFactory.createRaisedBevelBorder());
		panel1.add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(buttonYes, null);
		jPanel1.add(buttonNo, null);
		jPanel1.add(buttonCancel, null);
		panel1.add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(imageLabel, null);
		jPanel2.add(msgLabel, null);
		this.getContentPane().add(panel1, BorderLayout.CENTER);

		int w = imageLabel.getPreferredSize().width
				+ msgLabel.getPreferredSize().width + 40;
		int h = jPanel2.getPreferredSize().height
				+ jPanel1.getPreferredSize().height + 20;
		this.setSize(w, h);

		setResizable(false);
	}

	//Overridden so we can exit when window is closed
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			sayNo();
		}
		super.processWindowEvent(e);
	}

	public Boolean askForYes() {
		Boolean yes = askForYesNoOrCancel();
		return yes != null && yes == true;
	}

	/**
	 * 
	 * @return True if yes button is pressed, false if no button is pressed, 
	 * and null if cancel button is pressed
	 */
	public Boolean askForYesNoOrCancel() {
		Dimension dlgSize = this.getPreferredSize();
		Dimension frmSize;
		Point loc;
		if (frame != null) {
			frmSize = frame.getSize();

			if(frmSize.width == 0 || frmSize.height == 0)
				frmSize = Toolkit.getDefaultToolkit().getScreenSize();
			
			loc = frame.getLocation();
		}
		else {
			frmSize = Toolkit.getDefaultToolkit().getScreenSize();
			loc = new Point(0,0);
		}
		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		this.setModal(true);
		this.pack();
		super.setVisible(true);
		
		return saidYes;
	}

	//Close the dialog
	private void sayNo() {
		saidYes = false;
		dispose();
	}
	
	private void sayYes() {
		saidYes = true;
		dispose();
	}
	
	private void sayCancel() {
		saidYes = null;
		dispose();
	}

	//Close the dialog on a button event
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonNo) {
			sayNo();
		}
		else if (e.getSource() == buttonYes) {
			sayYes();
		}
		else if (e.getSource() == buttonCancel) {
			sayCancel();
		}
	}
}
