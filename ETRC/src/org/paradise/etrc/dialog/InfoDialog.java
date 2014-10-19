package org.paradise.etrc.dialog;

import java.awt.*;
import javax.swing.*;

import static org.paradise.etrc.ETRC._;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class InfoDialog /* extends JDialog */ {
  /**
	 * 
	 */
	private static final long serialVersionUID = 4118036852990630326L;
//JPanel panel1 = new JPanel();
//  BorderLayout borderLayout1 = new BorderLayout();

//  public InfoDialog(Frame frame, String title, boolean modal) {
////    super(frame, title, modal);
////    try {
////      jbInit();
////      pack();
////    }
////    catch(Exception ex) {
////      ex.printStackTrace();
////    }
//  }
  
  public static void showErrorDialog(Component parent, String msg) {
	  JOptionPane.showMessageDialog(parent, msg, _("Error"), JOptionPane.ERROR_MESSAGE);
  }
  
  public static void showInfoDialog(Component parent, String msg) {
	  JOptionPane.showMessageDialog(parent, msg, _("Tip"), JOptionPane.INFORMATION_MESSAGE);
  }

//  public InfoDialog() {
//    this(null, "", false);
//  }
//
//  private void jbInit() throws Exception {
//    panel1.setLayout(borderLayout1);
//    getContentPane().add(panel1);
//  }
  
 
}
