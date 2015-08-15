package org.paradise.etrc;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.paradise.etrc.util.config.Config;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.zearon.util.os.OSVersionUtil;
import com.zearon.util.ui.map.GLWindowManager;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.render.batch.BatchRenderDevice;
import de.lessvoid.nifty.renderer.jogl.render.JoglBatchRenderBackendCoreProfileFactory;
import de.lessvoid.nifty.spi.render.RenderDevice;


/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class ETRC {
  public static final String APP_NAME = "ETRC";
  
  
  boolean packFrame = false;
  
  private static ETRC instance = null;
  private MainFrame frame;


	private GLProfile	glp;
	public GLCapabilities	glcaps;
	public Nifty nifty;
  
  public static ResourceBundle res;

  public static ETRC getInstance() {
	  return instance;
  }
  
  public MainFrame getMainFrame() {
	  return frame;
  }
  
  public static String __(String str) {
	  if (res == null) {
		  return str; 
	  }
	  else  {
		  if (str == "FONT_NAME") {
		  	  return "Dialog";
		  }
		  else if (str == "FONT_NAME_FIXED") {
			  return "Monospace";
		  }
		  else {
			  try
			  {
				  return res.getString(str);
			  }
			  catch (MissingResourceException ex)
			  {
				  return str;
			  }
		  }
		  
	  }
  }
  
  //Construct the application
  public ETRC() {
  	init();
  	
		instance = this;
		try	{
			res = ResourceBundle.getBundle("resources.Messages");
		}
		catch (MissingResourceException ex)	{
			res = null;
		}

    frame = new MainFrame();
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
//    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setBounds(0,0,screenSize.width,screenSize.height-28);
    //frame.setLocation(0,0);
    frame.setVisible(true);
  }

  public static void setFont(Component c) {
    Font font = new Font("Dialog", 0, 12);

    c.setFont(font);
    //System.out.println("SET:" + c.getClass().getName());
    if(c instanceof Container) {
      //System.out.println("CONTAINER: " + c.getClass().getName());
      Component childs[] = ((Container)c).getComponents();
      for(int i=0; i<childs.length; i++) {
        setFont(childs[i]);
      }
    }
    if(c instanceof JComponent) {
      Border border = ((JComponent)c).getBorder();
      if(border instanceof TitledBorder)
        ((TitledBorder)border).setTitleFont(font);
    }
  }

	
	public static boolean isOSX10_7OrAbove() {
		return OSVersionUtil.isOSX10_7OrAbove();
	}

	public void init() {
  	initOpenGL();
		
  	Config.getInstance();
	}
	
	public void initOpenGL() {
		GLWindowManager.initOpenGL();
	}
	
  //Main method
  public static void main(String[] args) {
  	System.out.println(String.join(" ", args));
  	isOSX10_7OrAbove();
	
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    new ETRC();
  }
}
