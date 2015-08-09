package org.paradise.etrc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.paradise.etrc.data.TrainGraphPart;

/**
 * Utility class
 * @author Jeff Gong
 *
 */
public class ETRCUtil {
	private static boolean isDebug;
	private static DateFormat dateFormat;
	
	private static String lastInvoker;
	
	public static String joinString(String str1, String str2) { 
		return str1 + "," + str2; 
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
	   * Print a representation of the object
	   * if it is running in debug mode.
	   * @param tgp
	   */
	  public static void DEBUG_PRINT(Object o) {
		  if (IS_DEBUG()) {
			  System.err.println(o.toString());
		  }
	  }
	  
	  /**
	   * Print a debug representation of the train graph part
	   * if it is running in debug mode.
	   * @param tgp
	   */
	  public static void DEBUG_PRINT_TGP(TrainGraphPart tgp) {
		  if (IS_DEBUG()) {
			  System.err.println(tgp.toDebugString());
		  }
	  }
	  
	  /**
	   * Check if it is running in debug mode and print a message if yes.
	   * @param msg The message to be printed if in debug mode.
	   * @return true if in debug mode and false if else.
	   */
	  public static boolean DEBUG(String msg) {
		  if (isDebug)
			  _printMsg("DEBUG: ", msg, true, true, true, true, true, 0);
		  
		  return isDebug;
	  }
	  
	  /**
	   * Check if it is running in debug mode and print a message if yes.
	   * @param msgFormat The string format of message to be printed if in debug mode.
	   * @param msgArgs	 The arguments of the message format.
	   * @return true if in debug mode and false if else.
	   */
	  public static boolean DEBUG(String msgFormat, Object... msgArgs) {
		  DEBUG_MSG(msgFormat, msgArgs);
		  
		  return isDebug;
	  }
	  
	  /**
	   * Print a message if it is running in debug mode.
	   * @param msg
	   */
	  public static void DEBUG_MSG(String msgFormat, Object... msgArgs) {
		  
		  if (IS_DEBUG())
			  _printMsg("DEBUG_MSG: ", formatMessage(msgFormat, msgArgs), false, false, false, true, false, 0);
	  }
	  
	  /**
	   * Check if it is running in debug mode and do an action if yes.
	   * @param action The action to be done if in debug mode.
	   * @return true if in debug mode and false if else.
	   */
	  public static boolean DEBUG_ACTION(Runnable action) {
		  if (isDebug) {
			  _printMsg("\r\nDEBUG ACTION:", "", true, true, false, true, true, 0);
			  action.run();
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
	  public static boolean DEBUG_ACTION(Runnable action, String msgFormat, Object... msgArgs) {
		  if (isDebug) {
			  _printMsg("\r\nDEBUG ACTION:", formatMessage(msgFormat, msgArgs), true, true, true, true, true, 0);
			  action.run();
		  }
		  
		  return isDebug;
	  }
	  
	  /**
	   * Print a message if it is running in debug mode.
	   * @param msg
	   */
	  public static void DEBUG_STACKTRACE(int level, String msgFormat, Object... msgArgs) {
		  if (IS_DEBUG())
			  _printMsg("\r\nDEBUG: ", formatMessage(msgFormat, msgArgs), true, true, true, true, false, level);
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
	  
	  private static String formatMessage(String msgFormat, Object... args) {
		  if (msgFormat == null)
			  return "(NULL MESSAGE)";
		  
		  if (args.length > 0)
			  return String.format(msgFormat, args);
		  else
			  return msgFormat;
	  }
}
