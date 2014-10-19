package org.paradise.etrc.filter;

import static org.paradise.etrc.ETRC.__;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class GIFFilter extends FileFilter {

	@Override
	public boolean accept(File pathname) {
		if (pathname.getName().endsWith("gif") || pathname.isDirectory())
			return true;
		else
			return false;
	}

	@Override
	public String getDescription() {
		return __("GIF File (*.gif)");
	}

}
