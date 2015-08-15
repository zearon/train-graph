package com.zearon.util.os;

public class OSVersionUtil {
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
}
