package com.clearstream.build.inlinedoc;

import java.util.HashMap;
import java.util.Map;

public class BasicDocumentModel implements IDocumentModel {

	protected Map<String, Map<String, DocumentEntry>> entries = new HashMap<String, Map<String, DocumentEntry>>();
	
	@Override
	public DocumentEntry createEntry(String type, String title) {
		DocumentEntry entry = new DocumentEntry(type, title);
		
		if (entries.containsKey(type) == false) {
			entries.put(type, new HashMap<String, DocumentEntry>());
		}
		
		entries.get(type).put(title, entry);
		return entry;
	}

	@Override
	public DocumentEntry getEntry(String type, String title) {
		return entries.get(type).get(title);
	}
	
	@Override
	public Map<String, DocumentEntry> getAllEntries(String type) {
		return entries.get(type);
	}
}
