package com.clearstream.build.inlinedoc;

import java.io.File;

public interface IParser {
	public void parse(File file, IDocumentModel model);
	public void setTags(String[] tags);
}
