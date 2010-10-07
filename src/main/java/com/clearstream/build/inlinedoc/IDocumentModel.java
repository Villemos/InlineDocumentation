package com.clearstream.build.inlinedoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IDocumentModel {
	
	public class DocumentEntry {
		String type;
		String title;
		String text = "";
		String packageName;
		String file;
		
		public DocumentEntry(String type, String title) {
			this.type = type;
			this.title = title;
		}
		
		public void addTag(String tag, String value) {
			if (tags.containsKey(tag) == false) {
				tags.put(tag, new ArrayList<String>());
			}
			
			tags.get(tag).add(value);
		}
		
		Map<String, List<String>> tags = new HashMap<String, List<String>>();
	}
	
	public DocumentEntry createEntry(String type, String title);
	public DocumentEntry getEntry(String type, String title);
	
	public Map<String, DocumentEntry> getAllEntries(String type);
}
