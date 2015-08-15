/**
 * DO NOT MODIFY THIS FILE!
 * 
 * This file is automatically generated by ConfigClassGen.
 * If you want to modify this class, please subclass it 
 * moke modification in the subclass.
 * 
 * @author Jeff Gong
 */
package org.paradise.etrc.util.config;

import com.zearon.util.config.ConfigBase;

public class ConfigGenerated extends ConfigBase {

	/**
	 * Static property fields declaration
	 */
	static final String prop_LastRailnetworkPath = "Last_railroad_network_path"; 
	static final String prop_LastTrainPath = "Last_train_path"; 
	static final String prop_LastMapPath = "Last_map_path"; 
	static final String prop_KeyTE_NotGoThrough = "Keys_TimetableEditing_NotGoThrough"; 
	static final String prop_KeyTE_PASS = "Keys_TimetableEditing_PASS"; 
	static final String prop_KeyTE_StopAtStartStation = "Keys_TimetableEditing_StopAtStartStation"; 
	static final String prop_KeyTE_StopAtTerminalStation = "Keys_TimetableEditing_StopAtTerminalStation"; 
	static final String prop_KeyTE_StopForPassenger = "Keys_TimetableEditing_Stop"; 
	static final String prop_KeyTE_StopNoPassenger = "Keys_TimetableEditing_StopTechnical"; 

	ConfigGenerated() {
		super();
	}

	/**
	 * Initialization 
	 */
	protected void makeDefault() {
		super.makeDefault();
		
		// set default value of properties
		defaultProp.setProperty(prop_LastRailnetworkPath, "");
		defaultProp.setProperty(prop_LastTrainPath, "");
		defaultProp.setProperty(prop_LastMapPath, "");
		defaultProp.setProperty(prop_KeyTE_NotGoThrough, "T");
		defaultProp.setProperty(prop_KeyTE_PASS, "Q");
		defaultProp.setProperty(prop_KeyTE_StopAtStartStation, "W");
		defaultProp.setProperty(prop_KeyTE_StopAtTerminalStation, "E");
		defaultProp.setProperty(prop_KeyTE_StopForPassenger, "R");
		defaultProp.setProperty(prop_KeyTE_StopNoPassenger, "Y");
	}
	
	@Override
	protected String getAppName() {
		return "ETRC";
	}
	
	@Override
	protected String getConfigFileName() { 
		return "config.prop"; 
	}
	
	/**
	 * property getter and setter methods
	 */
	 
	/* property getter and setter methods for LastRailnetworkPath */
	public String getLastRailnetworkPath() {
		return afterGetLastRailnetworkPath(getValue(prop_LastRailnetworkPath));
	}
	public void setLastRailnetworkPath(String value) {
		setValue(prop_LastRailnetworkPath, beforeSetLastRailnetworkPath(value));
	}
	protected String afterGetLastRailnetworkPath(String value) {
		return value;
	}
	protected String beforeSetLastRailnetworkPath(String value) {
		return value;
	}
	
	/* property getter and setter methods for LastTrainPath */
	public String getLastTrainPath() {
		return afterGetLastTrainPath(getValue(prop_LastTrainPath));
	}
	public void setLastTrainPath(String value) {
		setValue(prop_LastTrainPath, beforeSetLastTrainPath(value));
	}
	protected String afterGetLastTrainPath(String value) {
		return value;
	}
	protected String beforeSetLastTrainPath(String value) {
		return value;
	}
	
	/* property getter and setter methods for LastMapPath */
	public String getLastMapPath() {
		return afterGetLastMapPath(getValue(prop_LastMapPath));
	}
	public void setLastMapPath(String value) {
		setValue(prop_LastMapPath, beforeSetLastMapPath(value));
	}
	protected String afterGetLastMapPath(String value) {
		return value;
	}
	protected String beforeSetLastMapPath(String value) {
		return value;
	}
	
	/* property getter and setter methods for KeyTE_NotGoThrough */
	public String getKeyTE_NotGoThrough() {
		return afterGetKeyTE_NotGoThrough(getValue(prop_KeyTE_NotGoThrough));
	}
	public void setKeyTE_NotGoThrough(String value) {
		setValue(prop_KeyTE_NotGoThrough, beforeSetKeyTE_NotGoThrough(value));
	}
	protected String afterGetKeyTE_NotGoThrough(String value) {
		return value;
	}
	protected String beforeSetKeyTE_NotGoThrough(String value) {
		return value;
	}
	
	/* property getter and setter methods for KeyTE_PASS */
	public String getKeyTE_PASS() {
		return afterGetKeyTE_PASS(getValue(prop_KeyTE_PASS));
	}
	public void setKeyTE_PASS(String value) {
		setValue(prop_KeyTE_PASS, beforeSetKeyTE_PASS(value));
	}
	protected String afterGetKeyTE_PASS(String value) {
		return value;
	}
	protected String beforeSetKeyTE_PASS(String value) {
		return value;
	}
	
	/* property getter and setter methods for KeyTE_StopAtStartStation */
	public String getKeyTE_StopAtStartStation() {
		return afterGetKeyTE_StopAtStartStation(getValue(prop_KeyTE_StopAtStartStation));
	}
	public void setKeyTE_StopAtStartStation(String value) {
		setValue(prop_KeyTE_StopAtStartStation, beforeSetKeyTE_StopAtStartStation(value));
	}
	protected String afterGetKeyTE_StopAtStartStation(String value) {
		return value;
	}
	protected String beforeSetKeyTE_StopAtStartStation(String value) {
		return value;
	}
	
	/* property getter and setter methods for KeyTE_StopAtTerminalStation */
	public String getKeyTE_StopAtTerminalStation() {
		return afterGetKeyTE_StopAtTerminalStation(getValue(prop_KeyTE_StopAtTerminalStation));
	}
	public void setKeyTE_StopAtTerminalStation(String value) {
		setValue(prop_KeyTE_StopAtTerminalStation, beforeSetKeyTE_StopAtTerminalStation(value));
	}
	protected String afterGetKeyTE_StopAtTerminalStation(String value) {
		return value;
	}
	protected String beforeSetKeyTE_StopAtTerminalStation(String value) {
		return value;
	}
	
	/* property getter and setter methods for KeyTE_StopForPassenger */
	public String getKeyTE_StopForPassenger() {
		return afterGetKeyTE_StopForPassenger(getValue(prop_KeyTE_StopForPassenger));
	}
	public void setKeyTE_StopForPassenger(String value) {
		setValue(prop_KeyTE_StopForPassenger, beforeSetKeyTE_StopForPassenger(value));
	}
	protected String afterGetKeyTE_StopForPassenger(String value) {
		return value;
	}
	protected String beforeSetKeyTE_StopForPassenger(String value) {
		return value;
	}
	
	/* property getter and setter methods for KeyTE_StopNoPassenger */
	public String getKeyTE_StopNoPassenger() {
		return afterGetKeyTE_StopNoPassenger(getValue(prop_KeyTE_StopNoPassenger));
	}
	public void setKeyTE_StopNoPassenger(String value) {
		setValue(prop_KeyTE_StopNoPassenger, beforeSetKeyTE_StopNoPassenger(value));
	}
	protected String afterGetKeyTE_StopNoPassenger(String value) {
		return value;
	}
	protected String beforeSetKeyTE_StopNoPassenger(String value) {
		return value;
	}
	
}