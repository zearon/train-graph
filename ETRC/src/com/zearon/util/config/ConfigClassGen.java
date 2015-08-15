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
import org.paradise.etrc.ETRC;


/**
 * Config class generator. <br/>
 * Typical use can be achieved by following the the steps below: <br/>
 * 1.	Create a intermediate <b>ConfigGenerated</b> class and leave it unchanged (which is 
 * 		the destination of code generation). <br/>
 * 2.	Create a <b>Config</b> class extending ConfigGenerated, which is the final
 * 		config class.
 * 3.	Add a main method in Config class, which utilize ConfigClassGen to generate 
 * 		the source code for the intermediate class ConfigGenerated. <br/>
 * 		The class hierarchy is Config -> ConfigGenerated -> com.zearon.util.ConfigBase. <br/>
 * 		A sample main method is given as the following: <br/> <pre><code>
 * 		public static void main(String... args) {
 *			// set args[0] to ${project_loc:ETRC} in eclipse run configuration where ETRC is the project name
 *			
 *			ConfigItem[] properties = {
 *			  new ConfigItem (Boolean.class, "AutoLoadFile", "Auto_Load_Last_Edit_File", "no"),
 *			  new ConfigItem (String.class, "AutoLoadFilePath", "Auto_Load_File_Path", "/sdfsdfsdfd"),
 *			  new ConfigItem (int.class, "HttpProxyPort", "Http_Proxy_Port", "80"),
 *			};
 *			
 *			new ConfigClassGen().generateConfigClass(ETRC.APP_NAME, args[0] + "/src", 
 *				ConfigGenerated.class.getCanonicalName(), "config.prop", properties);
 *		}
 * 		</code></pre>
 *
 * @author Jeff Gong
 *
 */
public class ConfigClassGen {
	private String baseClassName = ConfigBase.class.getSimpleName();
	
	/**
	 * Generate a java source code for a config class.
	 * @param appName 	The name of the app which determines the saving path of config file according to OS specification.
	 * 									For example, "ETRC" will make the config file stored at ${user.home}/Library/Application Support/ETRC/
	 * @param configClassSourceFilePath 	The source file path of the project
	 * @param destClassName								The full class name of the class to be generated
	 * @param configFileName							The short file name (without path) of the app's config file to be saved on file system.
	 * @param properties									a ConfigItem array describing all config items in config file.
	 */
	public void generateConfigClass(String appName, String sourceFilePath, 
			String destClassName, String configFileName, ConfigItem[] properties) {
		
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

		ctx.put("appName", appName);
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