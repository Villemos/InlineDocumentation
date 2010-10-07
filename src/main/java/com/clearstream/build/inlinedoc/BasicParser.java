package com.clearstream.build.inlinedoc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clearstream.build.inlinedoc.IDocumentModel.DocumentEntry;

public class BasicParser implements IParser {

	/**
	 * The tag used to identify the start of an entry
	 * @parameter
	 */
	String titleTag = "@TITLE\\s+(.+?)$";
	
	/**
	 * List of tags
	 * @parameter
	 */
	String[] tags = new String[] {};
	
	/**
	 * The tag used to mark the end of an entry
	 * @parameter
	 */
	String endTag = "@END";

	
	protected String type = "Document";
	protected Pattern titlePattern = null;
	protected List<Pattern> categorizationPatterns = new ArrayList<Pattern>();
	protected Pattern endPattern = null;

	protected Pattern packagePattern = Pattern.compile("package\\s+(.+?)$");
	
	protected boolean initialised = false;

	public void parse(File file, IDocumentModel model) {

		initialise();

		try {
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine; 

			String packageName = "";
			String title = null;

			while ((strLine = br.readLine()) != null) {
				
				/** Detect the package. */
				Matcher packageMatcher = packagePattern.matcher(strLine);
				if (packageMatcher.matches() == true) {
					packageName = packageMatcher.group(1); 
				}
				
				Matcher titleMatcher = titlePattern.matcher(strLine);
				if (titleMatcher.find() == true) {
					title = titleMatcher.group(1);
					DocumentEntry entry = model.createEntry(type, title);
					entry.file = file.getName();
					entry.packageName = packageName;

					while ((strLine = br.readLine()) != null) {

						Matcher endMatcher = endPattern.matcher(strLine);
						if (endMatcher.find() == true) {
							break;
						}

						boolean isTag = false;
						for (Pattern categoryPattern : categorizationPatterns) {
							Matcher categoryMatcher = categoryPattern.matcher(strLine);
							if (categoryMatcher.find() == true) {
								entry.addTag(categoryMatcher.group(1), categoryMatcher.group(2));
								isTag = true;
							}
						}

						if (isTag == false) {
							String deltaText = strLine.replaceFirst("\\*", "");
							if (deltaText.equals("") == true) {
								entry.text = entry.text + "<br/>";
							}
							else {						
								entry.text = entry.text + deltaText + " ";
							}
						}
					}
				}
			}

			in.close();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}

	protected void initialise() {
		if (initialised == false) {
			initialised = true;
			titlePattern = Pattern.compile(titleTag);

			for (String tag : tags) {
				categorizationPatterns.add(Pattern.compile(tag));
			}

			endPattern = Pattern.compile(endTag);
		}
	}
	
	@Override
	public void setTags(String[] tags) {
		this.tags = tags;
	}
}
