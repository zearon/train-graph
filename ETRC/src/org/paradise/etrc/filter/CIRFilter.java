package org.paradise.etrc.filter;

import java.io.File;

import static org.paradise.etrc.ETRC.__;

public class CIRFilter extends javax.swing.filechooser.FileFilter {

	public static String suffix = ".cir";
	
	/**
	 * accept
	 *
	 * @param pathname File
	 * @return boolean
	 */
	public boolean accept(File pathname) {
		if (pathname.getName().endsWith(suffix) || pathname.isDirectory())
			return true;
		else
			return false;
	}

	public String getDescription() {
		return __("Line Description File (*"+suffix+")");
	}
}

