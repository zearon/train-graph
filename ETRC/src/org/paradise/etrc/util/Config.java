package org.paradise.etrc.util;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {
	static Properties prop;
	static Properties defaultProp;
	
	static int Prop_Recent_File_Size = 15;
	
	static String Prop_Working_Chart = "Working_File";
	static String Prop_Show_UP = "Show_UP";
	static String Prop_Show_Down = "Show_Down";
	static String Prop_Show_Run = "Show_Run";
	static String Prop_HTTP_Proxy_Server = "HTTP_Proxy_Server";
	static String Prop_HTTP_Proxy_Port = "HTTP_Proxy_Port";
	static String Prop_Recent_Open_File_Path = "Recent_Open_File_Path";
	
	private static String Sample_Chart_File = "sample.trc";
	private static String Properties_File = "config.prop";
	
	static {
		init();
	}
	
	static void makeDefault() {
		defaultProp = new Properties();
//		defaultProp.setProperty(Prop_Working_Chart, Sample_Chart_File);
		defaultProp.setProperty(Prop_Show_UP, "Y");
		defaultProp.setProperty(Prop_Show_Down, "Y");
		defaultProp.setProperty(Prop_Show_Run, "Y");
		defaultProp.setProperty(Prop_HTTP_Proxy_Server, "");
		defaultProp.setProperty(Prop_HTTP_Proxy_Port, "");
		defaultProp.setProperty(Prop_Recent_Open_File_Path, "");
	}
	
	public static void init() {
		try {
			prop.load(new FileInputStream(Properties_File));
		} catch (IOException e) {
			resetToDefault();
			save();
		}
	}
	
	public static void load() throws FileNotFoundException, IOException {
		prop.load(new FileInputStream(Properties_File));
	}
	
	public static void save() {
		try {
			prop.store(new FileOutputStream(Properties_File), "Common settings");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void resetToDefault() {
		prop.clear();			
		prop = new Properties(defaultProp);
	}
	
	public static String getValue(String key) {
		return prop.getProperty(key);
	}
	
	public static String getValue(String key, String defaultValue) {
		return prop.getProperty(key, defaultValue);
	}
	
	public static void setValue(String key, String value) {
		prop.setProperty(key, value);
		
		save();
	}
	
	public static String getCurrentFile() {
		return getValue(Prop_Working_Chart);
	}
	
	public static void setCurrentFile(String value) {
		setValue(Prop_Working_Chart, value);
	}
	
	public static String[] getRecentOpenedFiles() {
		return getValue(Prop_Recent_Open_File_Path, "").split(";");
	}
	
	public static void addToRecentOpenedFiles(String filePath) {
		if (filePath == null || "".equals(filePath))
			return;
		
		String newList = Arrays.stream(getRecentOpenedFiles())
				.filter(path -> !path.equalsIgnoreCase(filePath))
				.limit(Prop_Recent_File_Size - 1)
				.collect(Collectors.joining(";"));
		newList = "".equals(newList) ? filePath : filePath + ";" + newList;
		setValue(Prop_Recent_Open_File_Path, newList);
	}
	
	public static String getLastFilePath(String defaultValue) {
		String[] recentFiles = getRecentOpenedFiles();
		if (recentFiles.length < 1)
			return defaultValue;
		else {
			String file = getRecentOpenedFiles()[0];
			return new File(file).getParentFile().getAbsolutePath();
		}
	}
	
	public static String getHttpProxyServer() {
		return getValue(Prop_HTTP_Proxy_Server);
	}
	
	public static void setHttpProxyServer(String value) {
		setValue(Prop_HTTP_Proxy_Server, value);
	}
	
	public static String getHttpProxyPort() {
		return getValue(Prop_HTTP_Proxy_Port);
	}
	
	public static void setHttpProxyPort(int value) {
		if (value < 0 || value > 65535)
			throw new IllegalArgumentException(__("Port number should be within 0 - 65535."));
		
		setValue(Prop_HTTP_Proxy_Port, value + "");
	}
}
