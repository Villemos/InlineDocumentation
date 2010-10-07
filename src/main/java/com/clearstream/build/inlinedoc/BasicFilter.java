package com.clearstream.build.inlinedoc;

import java.io.File;
import java.io.FilenameFilter;

public class BasicFilter implements FilenameFilter {

	private String[] exclude = new String[]{};

	public BasicFilter() {}
	
	public BasicFilter(String exclude) {
		if (exclude != null) {
			this.exclude = exclude.split("\\|");
		}
	}

	public boolean accept(File file, String name) {
		String absoluteName = file.getAbsolutePath() + File.pathSeparator + name; 
		for (String token : exclude) {
			if (absoluteName.matches(token) == true) { 
				return false;
			}
		}

		return true;
	}
}
