package org.paradise.etrc.filter;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.filechooser.FileFilter;

public class ImageFilter extends FileFilter {
	static String[] suffixStream = {".gif", ".jpg", ".jpeg", ".png"};
	static String desc = null;
	
	@Override
	public boolean accept(File pathName) {
		if (pathName.isDirectory() || Arrays.stream(suffixStream).map(suffix -> suffix.toLowerCase())
					.anyMatch(suffix -> pathName.getName().toLowerCase().endsWith(suffix))  )
			return true;
		else
			return false;
	}

	@Override
	public String getDescription() {
		if (desc == null) {
			desc = Arrays.stream(suffixStream).map(suffix -> "*" + suffix)
					.collect(Collectors.joining(",", __("Train Graph File ("), "") );
		}
		
		return desc;
	}

}
