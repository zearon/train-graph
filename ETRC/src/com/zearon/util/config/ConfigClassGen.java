package com.zearon.util.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Hashtable;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;


public class ConfigClassGen {
	private String baseClassName = ConfigBase.class.getSimpleName();
	
	/**
	 * 
	 * @param configClassSourceFilePath
	 * @param configFileName
	 * @param properties
	 */
	public void generateConfigClass(String sourceFilePath, String destClassName, String configFileName, ConfigItem[] properties) {
		
		String packageName, className;
		
		int lastDotIndex = destClassName.lastIndexOf(".");
		if (lastDotIndex > 0) {
			packageName = destClassName.substring(0, lastDotIndex);
			className = destClassName.substring(lastDotIndex + 1);
		} else {
			packageName = null;
			className = destClassName;
		}
		
		String filePath = sourceFilePath + File.separator + destClassName.replace(".", File.separator);
		if (!filePath.endsWith(".java"))
			filePath += ".java";
		
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();
		
		Template template = ve.getTemplate("/com/zearon/util/config/ConfigTemplate");
		VelocityContext ctx = new VelocityContext();
		
		ctx.put("packageName", packageName);
		ctx.put("className", className);
		ctx.put("baseClassName", baseClassName);
		ctx.put("configFileName", configFileName);
		ctx.put("properties", Arrays.asList(properties));
		
		
		try {
			System.out.println("Writing java file to " + filePath);
			Writer outputWriter = new FileWriter(filePath);
			
			template.merge(ctx, outputWriter);
			outputWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done");
	}

}