package org.paradise.etrc.util.config;
import java.io.File;

import org.paradise.etrc.ETRC;

import com.zearon.util.config.ConfigClassGen;
import com.zearon.util.config.ConfigItem;
import com.zearon.util.os.OSXUtil;
import com.zearon.util.ui.controller.ActionManager;

import static com.zearon.util.debug.DebugUtil.IS_DEBUG;

import static org.paradise.etrc.ETRC.__;

public class Config extends ConfigGenerated {
	static Config instance;
	
	public static synchronized Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		
		return instance;
	}
	
	// {{ 实例部分代码
	
	private Config() {
		super();
	}
	
	@Override
	protected void init() {
		super.init();
		
		if (OSXUtil.isOSX10_7OrAbove() && IS_DEBUG()) {
			configFilePath = "./" + getConfigFileName();
		}
	}
	
	/*****************************************************************/
	/* This part are in memory and has no need to be write to disks. */
	/*****************************************************************/

	public static final String NEW_FILE_NAME = __("Unnamed Train Graph");

	String currentFile;
	
	public String getCurrentFile() {
		return currentFile;
	}
	
	public void setCurrentFile(String filePath) {
		currentFile = filePath;
	}
	
	public String getCurrentFileName() {
		return new File(getCurrentFile()).getName();
	}
	
	public void setCurrentFileToNew() {
		currentFile = NEW_FILE_NAME;
	}
	
	public boolean isNewFile() {
		return NEW_FILE_NAME.equalsIgnoreCase(currentFile);
	}
	/*****************************************************************/

	
	public boolean isFileModified() {
		return ActionManager.getInstance().isModelModified();
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
	
	// }}
	
	// }}

	
	public static void main(String... args) {
		// set args[0] to ${project_loc:ETRC} in eclipse run configuration
		
		ConfigItem[] properties = {
				new ConfigItem (String.class, "LastRailnetworkPath", "Last_railroad_network_path", ""),
				new ConfigItem (String.class, "LastTrainPath", "Last_train_path", ""),
				new ConfigItem (String.class, "LastMapPath", "Last_map_path", ""),
				
				new ConfigItem (String.class, "KeyTE_NotGoThrough", "Keys_TimetableEditing_NotGoThrough", "T"),
				new ConfigItem (String.class, "KeyTE_PASS", "Keys_TimetableEditing_PASS", "Q"),
				new ConfigItem (String.class, "KeyTE_StopAtStartStation", "Keys_TimetableEditing_StopAtStartStation", "W"),
				new ConfigItem (String.class, "KeyTE_StopAtTerminalStation", "Keys_TimetableEditing_StopAtTerminalStation", "E"),
				new ConfigItem (String.class, "KeyTE_StopForPassenger", "Keys_TimetableEditing_Stop", "R"),
				new ConfigItem (String.class, "KeyTE_StopNoPassenger", "Keys_TimetableEditing_StopTechnical", "Y"),
		};
		
		new ConfigClassGen().generateConfigClass(ETRC.APP_NAME, args[0] + "/src",
				ConfigGenerated.class.getCanonicalName(), "config.prop", properties);
	}
}
