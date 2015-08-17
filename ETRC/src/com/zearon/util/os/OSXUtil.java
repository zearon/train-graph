package com.zearon.util.os;

import org.paradise.etrc.MainFrame;

import java.awt.Window;

import static com.zearon.util.debug.DebugUtil.DEBUG_MSG;

public class OSXUtil {
	static Boolean isOSX = null;
	
	public static boolean isOSX10_7OrAbove() {
		if (isOSX != null) {
			return isOSX;
		}
		
		isOSX = false;
		if ("Mac OS X".equalsIgnoreCase(System.getProperty("os.name"))) {
			String osVersionString = System.getProperty("os.version");
			String[] versionParts = osVersionString.split("\\.");
			if (versionParts.length >= 2) {
				try {
					int versionPart1Val = Integer.parseInt(versionParts[0]);
					int versionPart2Val = Integer.parseInt(versionParts[1]);
					if (versionPart1Val >= 10 && versionPart2Val >= 7) {
						isOSX = true;
					}
				} finally {}
			}
		}
		
		return isOSX;
	}

	/**
	 * If OS is Mac OS X and Version >= 10.7, then add full screen support.
	 */
	public static void addFullScreenModeSupportOnOSX(Window window) {
		DEBUG_MSG("OS Name=%s, OS Version=%s\n", System.getProperty("os.name"), System.getProperty("os.version"));
		if (!isOSX10_7OrAbove()) 
			return;
		
		// com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(this,true);
		
		// In case of the need to compile and run on other platforms,
		// replace the method invocation with reflection style to avoid
		// ClassNotFound Exceptions.
		try {
			Class<?> clz = Class.forName("com.apple.eawt.FullScreenUtilities");
			java.lang.reflect.Method method = clz.getMethod("setWindowCanFullScreen", java.awt.Window.class, boolean.class);
			method.invoke(null, window, true);
		} catch (Exception e) {
			System.err.println("Cannot add full screen support.");
			System.err.println(e);
		}
	}
	
	public static void setFullScreenModeOnOSX(Window window) {
		if (!isOSX10_7OrAbove()) 
			return;
		
		// com.apple.eawt.Application.getApplication().requestToggleFullScreen(MainFrame.this);	
		
		// In case of the need to compile and run on other platforms,
		// replace the method invocation with reflection style to avoid
		// ClassNotFound Exceptions.
		try {
			Class<?> applicationClz = Class.forName("com.apple.eawt.Application");
			java.lang.reflect.Method getApplicationMethod = 
					applicationClz.getMethod("getApplication");
			Object app = getApplicationMethod.invoke(null);
			
			java.lang.reflect.Method requestToggleFullScreenMethod = 
					applicationClz.getMethod("requestToggleFullScreen",java.awt.Window.class);
			requestToggleFullScreenMethod.invoke(app, window);
		} catch (Exception e) {
			System.err.println("Cannot add full screen support.");
			System.err.println(e);
		}
	}
}
