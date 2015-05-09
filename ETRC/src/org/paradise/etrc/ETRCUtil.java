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
	
	private static String lastInvoker;
	
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
	  public static boolean IS_DEBUG() {
		  return isDebug;
	  }
	  
	  /**
	   * Check if it is running in debug mode and print a message if yes.
	   * @param msg The message to be printed if in debug mode.
	   * @return true if in debug mode and false if else.
	   */
	  public static boolean DEBUG(String msg) {
		  if (isDebug)
			  _printMsg("\r\nDEBUG: ", msg, true, true, true, true, true, 0);
		  
		  return isDebug;
	  }
	  
	  /**
	   * Check if it is running in debug mode and print a message if yes.
	   * @param msgFormat The string format of message to be printed if in debug mode.
	   * @param msgArgs	 The arguments of the message format.
	   * @return true if in debug mode and false if else.
	   */
	  public static boolean DEBUG(String msgFormat, Object... msgArgs) {
		  if (isDebug)
			  _printMsg("\r\nDEBUG: ", String.format(msgFormat, msgArgs), true, true, true, true, true, 0);
		  
		  return isDebug;
	  }
	  
	  /**
	   * Check if it is running in debug mode and do an action if yes.
	   * @param action The action to be done if in debug mode.
	   * @return true if in debug mode and false if else.
	   */
	  public static boolean DEBUG_ACTION(DebugAction action) {
		  if (isDebug) {
			  _printMsg("\r\nDEBUG ACTION:", "", true, true, false, true, true, 0);
			  action.doAction();
		  }
		  
		  return isDebug;
	  }	  

	  
	  /**
	   * Check if it is running in debug mode. If yes, then print a message
	   * and do an action.
	   * @param action The action to be done if in debug mode.
	   * @param msgFormat The string format of message to be printed if in debug mode.
	   * @param msgArgs	 The arguments of the message format.
	   * @return true if in debug mode and false if else.
	   */
	  public static boolean DEBUG_ACTION(DebugAction action, String msgFormat, Object... msgArgs) {
		  if (isDebug) {
			  _printMsg("\r\nDEBUG ACTION:", String.format(msgFormat, msgArgs), true, true, true, true, true, 0);
			  action.doAction();
		  }
		  
		  return isDebug;
	  }
	  
	  /**
	   * Print a message if it is running in debug mode.
	   * @param msg
	   */
	  public static void DEBUG_MSG(String msgFormat, Object... msgArgs) {
		  if (isDebug)
			  _printMsg("\r\nDEBUG_MSG: ", String.format(msgFormat, msgArgs), false, false, false, false, false, 0);
	  }
	  
	  /**
	   * Print a message if it is running in debug mode.
	   * @param msg
	   */
	  public static void DEBUG_STACKTRACE(int level, String msgFormat, Object... msgArgs) {
		  if (isDebug)
			  _printMsg("\r\nDEBUG: ", String.format(msgFormat, msgArgs), true, true, true, true, false, level);
	  }
	  
	  private static void _printMsg(String prefix, String msg, boolean printTime, boolean printInvoker, 
			  boolean newLineBeforeMsg, boolean newLineAfterMsg, boolean skipSameInvoker, int stackLevel) {

		  StringBuilder invoker = new StringBuilder();
		  boolean notSameInvoker = true;
		  if (printInvoker) {
			  int startLevel = 2;
			  StackTraceElement[] stackElements = new Throwable().getStackTrace();
			  // System.err.println(stackElements[0]);	 		_printMsg
			  // System.err.println(stackElements[1]);	  		DEBUG/DEBUG_MSG
			  // System.err.println(stackElements[2]);	 		caller of DEBUG/DEBUG_MSG
			  invoker.append( stackElements[startLevel].toString() );
			  for (int level = 0; level < stackLevel && level < stackElements.length - 2; ++ level) {
				  invoker.append( "\r\n" + stackElements[startLevel + level + 1] );
			  }
			  
			  if (lastInvoker == null || !lastInvoker.equals(invoker.toString())) {
				  notSameInvoker = true;
				  lastInvoker = invoker.toString();
			  } else {
//				  notSameInvoker = false;
				  notSameInvoker = true;
				  lastInvoker = invoker.toString();
			  }
		  }
		  
		  notSameInvoker = skipSameInvoker ? skipSameInvoker && notSameInvoker : true;
		  
		  if (prefix != null  && notSameInvoker)
			  System.err.print(prefix);
		  
		  if (printTime  && notSameInvoker) {
			  System.err.print(" [" + dateFormat.format(new Date()) + "]");
		  }
		  
		  if (printTime  && printInvoker && notSameInvoker)
			  System.err.print("  ");
		  
		  if (printInvoker  && notSameInvoker) {
			  System.err.print(invoker);
		  }
		  
		  if (newLineBeforeMsg && notSameInvoker) {
			  System.err.println("");
		  }
		  
		  if (newLineBeforeMsg) {
			  System.err.print("  ");
		  }
		  
		  System.err.print(msg);
		  
		  if (newLineAfterMsg)
			  System.err.println();
	  }
}
