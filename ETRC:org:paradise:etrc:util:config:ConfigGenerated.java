/**
 * DO NOT MODIFY THIS FILE!
 * 
 * This file is automatically generated by ConfigClassGen.
 * If you want to modify this class, please subclass it 
 * moke modification in the subclass.
 * 
 * @author Jeff Gong
 */
package org.paradise.etrc.util.config

public class ConfigGenerated extends com.zearon.util.config.ConfigBase {

	// Static property fields declaration
	static final String prop_${item.propertyKeyName} 			= ${item.propertyName}; 

	com.zearon.util.config.ConfigBase() {
		super();
		
		Properties_File = "config.prop";
	}

	// Initialization
	protected void makeDefault() {
		super.makeDefault();
		
		// set default value of properties
		defaultProp.setProperty(prop_${item.propertyKeyName}, "${item.defaultValue}");
	}
	
	${initCode}

	// property getter and setter methods
		defaultProp.setProperty(prop_${item.propertyKeyName}, "${item.defaultValue}");
		
}