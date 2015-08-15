package com.zearon.util.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import com.zearon.util.os.OSVersionUtil;

public abstract class ConfigBase {
	static final String configFileName = "config.prop";
	
	static final int Prop_Recent_File_Size = 15;
	static final String Prop_Recent_Open_File_Path = "Recent_Open_File_Path";
	
	static String Prop_AUTO_LOAD_FILE = "Auto_Load_Last_Edit_File";
	static String Prop_FULLSCREEN_ON_STARTUP = "FullScreen_On_Startup_MAC";
	
	static String prop_HTTP_Proxy_Use = "Use_HTTP_Proxy";
	static String Prop_HTTP_Proxy_Server = "HTTP_Proxy_Server";
	static String Prop_HTTP_Proxy_Port = "HTTP_Proxy_Port";
	
	static Properties __defaultProp = new Properties();
	static ConfigBase instance;
	static boolean inited = false;
	
	protected Properties prop;
	protected Properties defaultProp = __defaultProp;
	protected String configFilePath = configFileName;
	
	protected ConfigBase() {
		initInternal();
		
		prop = new Properties(defaultProp);
		
		try {
			prop.load(new FileInputStream(configFilePath));
		} catch (Exception e) {
			e.printStackTrace();
			resetToDefault();
			save();
		}
	}
	
	private void initInternal() {
		if (inited)
			return;
		
		init();
		System.err.println("Use config file " + configFilePath);
		
		inited = true;
	}
	
	protected void init() {
		if (OSVersionUtil.isOSX10_7OrAbove()) {
			configFilePath = System.getProperty("user.home") + "/Library/Application Support/" + getAppName() + "/" + getConfigFileName();
			new File(configFilePath).getParentFile().mkdirs();
		}
		
		makeDefault();
	}
	
	protected void makeDefault() {
		defaultProp.setProperty(Prop_AUTO_LOAD_FILE, "no");
		defaultProp.setProperty(Prop_FULLSCREEN_ON_STARTUP, "yes");
		defaultProp.setProperty(Prop_Recent_Open_File_Path, "");
		
		defaultProp.setProperty(prop_HTTP_Proxy_Use, "no");
		defaultProp.setProperty(Prop_HTTP_Proxy_Server, "");
		defaultProp.setProperty(Prop_HTTP_Proxy_Port, "80");
	}



	
	/*************************************************************
	 ** template methods for sub-classes
	 ************************************************************/
	abstract protected String getAppName();
	protected String getConfigFileName() { return "config.prop"; }
	
	/*************************************************************
	 ** Load and save methods
	 ************************************************************/
	
	public void load() throws FileNotFoundException, IOException {
		prop.load(new FileInputStream(configFilePath));
	}
	
	public void save() {
		try {
			prop.store(new FileOutputStream(configFilePath), "Common settings");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void resetToDefault() {
		prop.clear();
	}

	
	/*************************************************************
	 ** Common getter and setter methods
	 ************************************************************/
	
	protected String getValue(String key) {
		return prop.getProperty(key, "");
	}
	
	protected String getValue(String key, String defaultValue) {
		return prop.getProperty(key, defaultValue);
	}
	
	protected void setValue(String key, String value) {
		prop.setProperty(key, value);
		
		save();
	}
	

	
	/*************************************************************
	 ** Recent files related properties
	 ************************************************************/
	
	public Boolean getAutoLoadLastFile() {
		return getValue(Prop_AUTO_LOAD_FILE, "false").equalsIgnoreCase("yes");
	}
	
	public void setAutoLoadLastFile(Boolean value) {
		setValue(Prop_AUTO_LOAD_FILE, value ? "yes" : "no");
	}
	
	public Boolean getFullScreenOnStartupForOSX() {
		return getValue(Prop_FULLSCREEN_ON_STARTUP, "false").equalsIgnoreCase("yes");
	}
	
	public void setFullScreenOnStartupForOSX(Boolean value) {
		setValue(Prop_FULLSCREEN_ON_STARTUP, value ? "yes" : "no");
	}
	
	public String[] getRecentOpenedFiles() {
		return getValue(Prop_Recent_Open_File_Path, "").split(";");
	}
	
	public void addToRecentOpenedFiles(String filePath) {
		if (filePath == null || "".equals(filePath))
			return;
		
		String newList = Arrays.stream(getRecentOpenedFiles())
				.filter(path -> !path.equalsIgnoreCase(filePath))
				.limit(Prop_Recent_File_Size - 1)
				.collect(Collectors.joining(";"));
		newList = "".equals(newList) ? filePath : filePath + ";" + newList;
		setValue(Prop_Recent_Open_File_Path, newList);
	}
	
	public String getLastFile(String defaultValue) {
		String[] recentFiles = getRecentOpenedFiles();
		if (recentFiles.length < 1)
			return defaultValue;
		else {
			return getRecentOpenedFiles()[0];
		}
	}
	
	public String getLastFilePath(String defaultValue) {
		String[] recentFiles = getRecentOpenedFiles();
		if (recentFiles.length < 1)
			return defaultValue;
		else {
			String file = getRecentOpenedFiles()[0];
			return new File(file).getParentFile().getAbsolutePath();
		}
	}
	
	
	/*************************************************************
	 ** HTTP proxy related properties
	 ************************************************************/
	
	public Boolean getHttpProxyUse() {
		return getValue(prop_HTTP_Proxy_Use, "false").equalsIgnoreCase("yes");
	}
	
	public void setHttpProxyUse(Boolean value) {
		setValue(prop_HTTP_Proxy_Use, value ? "yes" : "no");
	}
	
	public String getHttpProxyServer() {
		return getValue(Prop_HTTP_Proxy_Server);
	}
	
	public void setHttpProxyServer(String value) {
		setValue(Prop_HTTP_Proxy_Server, value);
	}
	
	public int getHttpProxyPort() {
		int port = 0;
		try {
			port = Integer.parseInt(getValue(Prop_HTTP_Proxy_Port));
		} catch (Exception e) {}
		
		return port < 0 || port > 65535 ? 80 : port;
	}
	
	public void setHttpProxyPort(int value) {
		if (value < 0 || value > 65535)
			throw new IllegalArgumentException(("Port number should be within 0 - 65535."));
		
		setValue(Prop_HTTP_Proxy_Port, value + "");
	}	
	

}
