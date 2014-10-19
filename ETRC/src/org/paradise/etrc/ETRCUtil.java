package org.paradise.etrc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class
 * @author Jeff Gong
 *
 */
public class ETRCUtil {
	private static boolean isDebug;
	private static DateFormat dateFormat;
	
	@FunctionalInterface
	public static interface DebugAction {		
		void doAction();
	}
	
	static {
		isDebug = "true".equalsIgnoreCase(System.getenv("DEBUG"));
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	  
	/**
	 * Check if it is running in debug mode. 
	 * Define DEBUG environment variable to 'true' to activate debug mode.
	 * @return true if it is running in debug mode.
	 */
	  public static boolean DEBUG() {
		  return isDebug;
	  }
	  
	  /**
	   * Check if it is running in debug mode and print a message if yes.
	   * @param msg The message to be printed if in debug mode.
	   * @return true if in debug mode and false if else.
	   */
	  public static boolean DEBUG(String msg) {
		  if (isDebug)
			  _printMsg("DEBUG: ", msg, true, true, true, true);
		  
		  return isDebug;
	  }
	  
	  /**
	   * Check if it is running in debug mode and do an action if yes.
	   * @param action The action to be done if in debug mode.
	   * @return true if in debug mode and false if else.
	   */
	  public static boolean DEBUG(DebugAction action) {
		  if (isDebug) {
			  _printMsg("DEBUG ACTION:", "", true, true, false, true);
			  action.doAction();
		  }
		  
		  return isDebug;
	  }
	  
	  /**
	   * Print a message if it is running in debug mode.
	   * @param msg
	   */
	  public static void DEBUG_MSG(String msg) {
		  if (isDebug)
			  _printMsg(null, msg, false, false, false, false);
	  }
	  
	  private static void _printMsg(String prefix, String msg, boolean printTime, boolean printInvoker, 
			  boolean newLineBeforeMsg, boolean newLineAfterMsg) {
		  
		  if (prefix != null)
			  System.err.print(prefix);
		  
		  if (printTime) {
			  System.err.print(" [" + dateFormat.format(new Date()) + "]");
		  }
		  
		  if (printTime && printInvoker)
			  System.err.println("  ");
		  
		  if (printInvoker) {
			  StackTraceElement[] stackElements = new Throwable().getStackTrace();
			  // System.err.println(stackElements[0]);	 		_printMsg
			  // System.err.println(stackElements[1]);	  		DEBUG/DEBUG_MSG
			  System.err.print(stackElements[2]);		 // 	caller of DEBUG/DEBUG_MSG
		  }
		  
		  if (newLineBeforeMsg) {
			  System.err.println("");
			  System.err.print("  ");
		  }
		  
		  System.err.print(msg);
		  
		  if (newLineAfterMsg)
			  System.err.println();
	  }
}
