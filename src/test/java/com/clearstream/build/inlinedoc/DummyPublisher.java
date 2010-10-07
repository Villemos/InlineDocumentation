package com.clearstream.build.inlinedoc;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.clearstream.build.inlinedoc.IDocumentModel.DocumentEntry;

public class DummyPublisher implements IPublisher {

	String[] tags = new String[] {"@(CATEGORY)\\s+(.+?)$"};
	
	IDocumentModel model = null;
	boolean publishCalled = false;
	
	@Override
	public void publish(IDocumentModel model) {
		this.publishCalled = true;
		this.model = model;
	}
	
	public void print() {
		if (model == null) {
			System.out.println("Empty model.");
		}
		else {
			System.out.println("DOCUMENTS.");
			Iterator<Entry<String, DocumentEntry>> it = model.getAllEntries("Document").entrySet().iterator();
			while (it.hasNext() == true) {
				Entry<String, DocumentEntry> entry = it.next();
				
				System.out.println("Title: " + entry.getKey());
				
				Iterator<Entry<String, List<String>>> it2 = entry.getValue().tags.entrySet().iterator();
				while (it2.hasNext() == true) {
					Entry<String, List<String>> entry2 = it2.next();
					System.out.println("  Tag: " + entry2.getKey());
					for (String tag : entry2.getValue()) {
						System.out.println("    Value: " + tag);
					}
				}
			}			
		}
	}

	@Override
	public String[] getTags() {
		return tags;
	}
}
