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

import org.paradise.etrc.controller.ActionManager;

public class Config {
	public static final String NEW_FILE_NAME = __("Unnamed Train Graph");
	static final int Prop_Recent_File_Size = 15;
	
	static Properties prop;
	static Properties defaultProp;

	static String currentFile;

//	static String Prop_Show_UP = "Show_UP";
//	static String Prop_Show_Down = "Show_Down";
//	static String Prop_Show_Run = "Show_Run";
	
	static String Prop_AUTO_LOAD_FILE = "Auto_Load_Last_Edit_File";
	static String Prop_Working_Chart = "Working_File";
	static String Prop_Recent_Open_File_Path = "Recent_Open_File_Path";
	static String Prop_LAST_RAILNETWORK_PATH = "Last_railroad_network_path";
	static String Prop_LAST_TRAIN_PATH = "Last_train_path";
	static String Prop_LAST_MAP_PATH = "Last_map_path";
	static String Prop_HTTP_Proxy_Server = "HTTP_Proxy_Server";
	static String Prop_HTTP_Proxy_Port = "HTTP_Proxy_Port";
	
	private static String Properties_File = "config.prop";
	
	static {
		makeDefault();
		init();
	}	
	
	static void makeDefault() {
		defaultProp = new Properties();
		
		defaultProp.setProperty(Prop_AUTO_LOAD_FILE, "no");
		defaultProp.setProperty(Prop_Working_Chart, "");
		defaultProp.setProperty(Prop_Recent_Open_File_Path, "");
		defaultProp.setProperty(Prop_LAST_RAILNETWORK_PATH, "");
		defaultProp.setProperty(Prop_LAST_TRAIN_PATH, "");
		defaultProp.setProperty(Prop_LAST_MAP_PATH, "");
		defaultProp.setProperty(Prop_HTTP_Proxy_Server, "");
		defaultProp.setProperty(Prop_HTTP_Proxy_Port, "80");
	}
	
	public static void init() {
		prop = new Properties(defaultProp);
		
		try {
			prop.load(new FileInputStream(Properties_File));
		} catch (Exception e) {
			e.printStackTrace();
			resetToDefault();
			save();
		}
	}
	
	/*****************************************************************/
	/* This part are in memory and has no need to be write to disks. */
	/*****************************************************************/
	public static String getCurrentFile() {
		return currentFile;
	}
	
	public static String getCurrentFileName() {
		return new File(getCurrentFile()).getName();
	}
	
	public static void setCurrentFileToNew() {
		currentFile = NEW_FILE_NAME;
	}
	
	public static void setCurrentFile(String filePath) {
		currentFile = filePath;
	}
	
	public static boolean isNewFile() {
		return NEW_FILE_NAME.equalsIgnoreCase(currentFile);
	}
	/*****************************************************************/

	
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
	}
	
	public static String getValue(String key) {
		return prop.getProperty(key, "");
	}
	
	public static String getValue(String key, String defaultValue) {
		return prop.getProperty(key, defaultValue);
	}
	
	public static void setValue(String key, String value) {
		prop.setProperty(key, value);
		
		save();
	}
	
	public static boolean getAutoLoadLastFile() {
		return getValue(Prop_AUTO_LOAD_FILE, "false").equalsIgnoreCase("yes");
	}
	
	public static void setAutoLoadLastFile(boolean value) {
		setValue(Prop_AUTO_LOAD_FILE, value ? "yes" : "no");
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
	
	public static String getLastFile(String defaultValue) {
		String[] recentFiles = getRecentOpenedFiles();
		if (recentFiles.length < 1)
			return defaultValue;
		else {
			return getRecentOpenedFiles()[0];
		}
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
	
	public static boolean isFileModified() {
		return ActionManager.getInstance().isModelModified();
	}
	
	public static String getLastRailnetworkPath() {
		return getValue(Prop_LAST_RAILNETWORK_PATH);
	}
	
	public static void setLastRailnetworkPath(String value) {
		setValue(Prop_LAST_RAILNETWORK_PATH, value);
	}
	
	public static String getLastTrainPath() {
		return getValue(Prop_LAST_TRAIN_PATH);
	}
	
	public static void setLastTrainPath(String value) {
		setValue(Prop_LAST_TRAIN_PATH, value);
	}
	
	public static String getLastMapPath() {
		return getValue(Prop_LAST_MAP_PATH);
	}
	
	public static void setLastMapPath(String value) {
		setValue(Prop_LAST_MAP_PATH, value);
	}
	
	public static String getHttpProxyServer() {
		return getValue(Prop_HTTP_Proxy_Server);
	}
	
	public static void setHttpProxyServer(String value) {
		setValue(Prop_HTTP_Proxy_Server, value);
	}
	
	public static int getHttpProxyPort() {
		int port = 0;
		try {
			port = Integer.parseInt(getValue(Prop_HTTP_Proxy_Port));
		} catch (Exception e) {}
		
		return port < 0 || port > 65535 ? 80 : port;
	}
	
	public static void setHttpProxyPort(int value) {
		if (value < 0 || value > 65535)
			throw new IllegalArgumentException(__("Port number should be within 0 - 65535."));
		
		setValue(Prop_HTTP_Proxy_Port, value + "");
	}
}
