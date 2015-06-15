package org.paradise.etrc.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import org.paradise.etrc.controller.ActionManager;

import static org.paradise.etrc.ETRC.__;

public class Config {
	public static final String NEW_FILE_NAME = __("Unnamed Train Graph");
	static final int Prop_Recent_File_Size = 15;
	
	static String Prop_AUTO_LOAD_FILE = "Auto_Load_Last_Edit_File";
	static String prop_HTTP_Proxy_Use = "Use_HTTP_Proxy";
	static String Prop_HTTP_Proxy_Server = "HTTP_Proxy_Server";
	static String Prop_HTTP_Proxy_Port = "HTTP_Proxy_Port";
	
	static String Prop_Working_Chart = "Working_File";
	static String Prop_Recent_Open_File_Path = "Recent_Open_File_Path";
	static String Prop_LAST_RAILNETWORK_PATH = "Last_railroad_network_path";
	static String Prop_LAST_TRAIN_PATH = "Last_train_path";
	static String Prop_LAST_MAP_PATH = "Last_map_path";
	
	static String prop_KEYS_TE_NotGoThrough 			= "Keys_TimetableEditing_NotGoThrough";
	static String prop_KEYS_TE_PASS			 			= "Keys_TimetableEditing_PASS";
	static String prop_KEYS_TE_StopAtStartStation		= "Keys_TimetableEditing_StopAtStartStation";
	static String prop_KEYS_TE_StopAtTerminalStation	= "Keys_TimetableEditing_StopAtTerminalStation";
	static String prop_KEYS_TE_StopForPassenger			= "Keys_TimetableEditing_Stop";
	static String prop_KEYS_TE_StopNoPassenger 			= "Keys_TimetableEditing_StopTechnical";
	
	static String Properties_File = "config.prop";
	
	static Properties defaultProp;
	static Config instance;
	
	private Properties prop;
	
	public static synchronized Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		
		return instance;
	}

	String currentFile;

//	static String Prop_Show_UP = "Show_UP";
//	static String Prop_Show_Down = "Show_Down";
//	static String Prop_Show_Run = "Show_Run";
	
	static {
		makeDefault();
	}	
	
	static void makeDefault() {
		defaultProp = new Properties();
		
		defaultProp.setProperty(Prop_AUTO_LOAD_FILE, "no");
		defaultProp.setProperty(Prop_Working_Chart, "");
		defaultProp.setProperty(Prop_Recent_Open_File_Path, "");
		defaultProp.setProperty(Prop_LAST_RAILNETWORK_PATH, "");
		defaultProp.setProperty(Prop_LAST_TRAIN_PATH, "");
		defaultProp.setProperty(Prop_LAST_MAP_PATH, "");
		defaultProp.setProperty(prop_HTTP_Proxy_Use, "no");
		defaultProp.setProperty(Prop_HTTP_Proxy_Server, "");
		defaultProp.setProperty(Prop_HTTP_Proxy_Port, "80");
		
		defaultProp.setProperty(prop_KEYS_TE_NotGoThrough, "T");
		defaultProp.setProperty(prop_KEYS_TE_PASS, "Q");
		defaultProp.setProperty(prop_KEYS_TE_StopAtStartStation, "W");
		defaultProp.setProperty(prop_KEYS_TE_StopAtTerminalStation, "E");
		defaultProp.setProperty(prop_KEYS_TE_StopForPassenger, "R");
		defaultProp.setProperty(prop_KEYS_TE_StopNoPassenger, "Y");
	}
	
	private Config() {
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
	public  String getCurrentFile() {
		return currentFile;
	}
	
	public  String getCurrentFileName() {
		return new File(getCurrentFile()).getName();
	}
	
	public  void setCurrentFileToNew() {
		currentFile = NEW_FILE_NAME;
	}
	
	public  void setCurrentFile(String filePath) {
		currentFile = filePath;
	}
	
	public  boolean isNewFile() {
		return NEW_FILE_NAME.equalsIgnoreCase(currentFile);
	}
	/*****************************************************************/

	
	public  void load() throws FileNotFoundException, IOException {
		prop.load(new FileInputStream(Properties_File));
	}
	
	public  void save() {
		try {
			prop.store(new FileOutputStream(Properties_File), "Common settings");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public  void resetToDefault() {
		prop.clear();
	}
	
	public  String getValue(String key) {
		return prop.getProperty(key, "");
	}
	
	public  String getValue(String key, String defaultValue) {
		return prop.getProperty(key, defaultValue);
	}
	
	public  void setValue(String key, String value) {
		prop.setProperty(key, value);
		
		save();
	}
	
	public  Boolean getAutoLoadLastFile() {
		return getValue(Prop_AUTO_LOAD_FILE, "false").equalsIgnoreCase("yes");
	}
	
	public  void setAutoLoadLastFile(Boolean value) {
		setValue(Prop_AUTO_LOAD_FILE, value ? "yes" : "no");
	}
	
	public  String[] getRecentOpenedFiles() {
		return getValue(Prop_Recent_Open_File_Path, "").split(";");
	}
	
	public  void addToRecentOpenedFiles(String filePath) {
		if (filePath == null || "".equals(filePath))
			return;
		
		String newList = Arrays.stream(getRecentOpenedFiles())
				.filter(path -> !path.equalsIgnoreCase(filePath))
				.limit(Prop_Recent_File_Size - 1)
				.collect(Collectors.joining(";"));
		newList = "".equals(newList) ? filePath : filePath + ";" + newList;
		setValue(Prop_Recent_Open_File_Path, newList);
	}
	
	public  String getLastFile(String defaultValue) {
		String[] recentFiles = getRecentOpenedFiles();
		if (recentFiles.length < 1)
			return defaultValue;
		else {
			return getRecentOpenedFiles()[0];
		}
	}
	
	public  String getLastFilePath(String defaultValue) {
		String[] recentFiles = getRecentOpenedFiles();
		if (recentFiles.length < 1)
			return defaultValue;
		else {
			String file = getRecentOpenedFiles()[0];
			return new File(file).getParentFile().getAbsolutePath();
		}
	}
	
	public  boolean isFileModified() {
		return ActionManager.getInstance().isModelModified();
	}
	
	public  String getLastRailnetworkPath() {
		return getValue(Prop_LAST_RAILNETWORK_PATH);
	}
	
	public  void setLastRailnetworkPath(String value) {
		setValue(Prop_LAST_RAILNETWORK_PATH, value);
	}
	
	public  String getLastTrainPath() {
		return getValue(Prop_LAST_TRAIN_PATH);
	}
	
	public  void setLastTrainPath(String value) {
		setValue(Prop_LAST_TRAIN_PATH, value);
	}
	
	public  String getLastMapPath() {
		return getValue(Prop_LAST_MAP_PATH);
	}
	
	public  void setLastMapPath(String value) {
		setValue(Prop_LAST_MAP_PATH, value);
	}
	
	public  Boolean getHttpProxyUse() {
		return getValue(prop_HTTP_Proxy_Use, "false").equalsIgnoreCase("yes");
	}
	
	public  void setHttpProxyUse(Boolean value) {
		setValue(prop_HTTP_Proxy_Use, value ? "yes" : "no");
	}
	
	public  String getHttpProxyServer() {
		return getValue(Prop_HTTP_Proxy_Server);
	}
	
	public  void setHttpProxyServer(String value) {
		setValue(Prop_HTTP_Proxy_Server, value);
	}
	
	public  int getHttpProxyPort() {
		int port = 0;
		try {
			port = Integer.parseInt(getValue(Prop_HTTP_Proxy_Port));
		} catch (Exception e) {}
		
		return port < 0 || port > 65535 ? 80 : port;
	}
	
	public  void setHttpProxyPort(int value) {
		if (value < 0 || value > 65535)
			throw new IllegalArgumentException(__("Port number should be within 0 - 65535."));
		
		setValue(Prop_HTTP_Proxy_Port, value + "");
	}	
	
	// {{ Keys settings 
	
	/* Timetable editing keys */
	
	public boolean isTimetableEditingKey(char key) {
		String keyString = "" + key;
		keyString = keyString.toUpperCase();
		
		return keyString.equals(getKeyTE_NotGoThrough()) ||
				keyString.equals(getKeyTE_PASS()) ||
				keyString.equals(getKeyTE_StopAtStartStation()) ||
				keyString.equals(getKeyTE_StopAtTerminalStation()) ||
				keyString.equals(getKeyTE_StopForPassenger()) ||
				keyString.equals(getKeyTE_StopNoPassenger());
	}
	
	public  String getKeyTE_NotGoThrough() {
		return getValue(prop_KEYS_TE_NotGoThrough);
	}
	
	public  void setKeyTE_NotGoThrough(String value) {
		setValue(prop_KEYS_TE_NotGoThrough, value);
	}
	
	public  String getKeyTE_PASS() {
		return getValue(prop_KEYS_TE_PASS);
	}
	
	public  void setKeyTE_PASS(String value) {
		setValue(prop_KEYS_TE_PASS, value);
	}
	
	public  String getKeyTE_StopAtStartStation() {
		return getValue(prop_KEYS_TE_StopAtStartStation);
	}
	
	public  void setKeyTE_StopAtStartStation(String value) {
		setValue(prop_KEYS_TE_StopAtStartStation, value);
	}
	
	public  String getKeyTE_StopAtTerminalStation() {
		return getValue(prop_KEYS_TE_StopAtTerminalStation);
	}
	
	public  void setKeyTE_StopAtTerminalStation(String value) {
		setValue(prop_KEYS_TE_StopAtTerminalStation, value);
	}
	
	public  String getKeyTE_StopForPassenger() {
		return getValue(prop_KEYS_TE_StopForPassenger);
	}
	
	public  void setKeyTE_StopForPassenger(String value) {
		setValue(prop_KEYS_TE_StopForPassenger, value);
	}
	
	public  String getKeyTE_StopNoPassenger() {
		return getValue(prop_KEYS_TE_StopNoPassenger);
	}
	
	public  void setKeyTE_StopNoPassenger(String value) {
		setValue(prop_KEYS_TE_StopNoPassenger, value);
	}
	
	// }}

}
