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
 *			System.out.println(args[0]);
 *			new ConfigClassGen().generateConfigClass(args[0] + "/src", 
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