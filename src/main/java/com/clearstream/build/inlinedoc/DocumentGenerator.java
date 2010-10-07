package com.clearstream.build.inlinedoc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @TITLE Document Generator User Guide
 * The Document Generator takes the following configuration parameters
 * 
 *   parserClass (optional). The full name of the class to be used as a source code parser. Must implement IParser.
 *   
 *   filterClass (optional). The full name of the class to be used as a filter. Must implement FilenameFilter.
 *   
 *   docModelClass (optional). The full name of the class to be used for the document model. Must implement IDocumentModel.
 *   
 *   publisherClass (optional). The full name of the class used to publish the document model. Must implement IPublisher.   
 * @CATEGORY Document Parser
 * @CATEGORY User Guide
 * @END
 *
 * @TITLE Document Generator Configuration Guide
 * The document generator is a maven MOJO, i.e. is configured together with the Maven build environment as a plugin.
 * 
 * To include the plugin in the build, include the following in the project pom.xml file
 * 
 * @<plugins>@
 * 
 * @  <plugin>@
 * 
 * @    <groupId>com.logica.hummingbird</groupId>@
 * 
 * @    <artifactId>maven-docgenerator-plugin</artifactId>@
 * 
 * @    <version>0.0.1</version>@
 * 
 * @    <configuration>@
 * 
 * @      <input>[full path to root directory of the project. Default to project.dir]</input>@
 * 
 * @      <exclude>[List of regx expressions, excluding files. List should be separated with '|'. Default is no exclusion]</exclude>@
 * 
 * @      <publisherClass>[Qualified name of the class to be used to publish the documents. Defaults to the 'GithubPublisher']</publisherClass>@
 * 
 * @      <parserClass>[Qualified name of the class to be used to parse the documentation. Defaults to 'BasicParser']<parserClass/>
 * 
 * @      <filterClass>[Qualified name of the class to be used to filter files. Defaults to 'BasicFilter']<filterClass/>
 * 
 * @      <docModelClass>[Qualified name of the class to be used to create the document model. Defaults to 'BasicDocumentModel']<docModelClass/>
 * 
 * @    </configuration>
 * 
 * @  </plugin>@
 * 
 * @  ...@
 * 
 * @</plugins>@
 * 
 * @CATEGORY Configuration Guide
 * @Document Generator
 * @END
 * 
 * @goal parse
 * 
 * @phase process-sources
 */
public class DocumentGenerator extends AbstractMojo {
	/**
	 * Location of the file.
	 * @parameter expression="${project.dir}"
	 * @required
	 */
	public File input;

	/**
	 * Patterns to exclude
	 * @parameter
	 */
	public String[] exclude = new String[]{};

	/**
	 * The class to be used for the publisher. Defaults to 'GithubPublisher'
	 * @parameter
	 */
	public IPublisher[] publishers = new IPublisher[] {new GithubPublisher()};

	/**
	 * The class to be used for the parser. Defaults to 'BasicParser'
	 * @parameter
	 */
	public IParser[] parsers = new IParser[] {new BasicParser()};

	/**
	 * The class to be used for the filtering of input files. Defaults to 'BasicFilter'.
	 * @parameter
	 */
	public FilenameFilter filter = new BasicFilter();

	/**
	 * The class to be used for the document model. Defaults to 'BasicDocumentModel'.
	 * @parameter
	 */
	public IDocumentModel documentModel = new BasicDocumentModel();


	public void execute() throws MojoExecutionException {

		System.out.println("Stating ClearStream inline documentation generator.");

		/** Get all tags required by the publishers. */
		String[] tags = new String[]{}; 
		for (IPublisher publisher : publishers) {
			List<String> existingTags = new ArrayList<String>(Arrays.asList(tags));
			existingTags.addAll(new ArrayList<String>(Arrays.asList(publisher.getTags())));
			tags = (String[]) existingTags.toArray(new String[0]);
		}

		/** Set all the tags on the parser. */
		for (IParser parser : parsers) {
			parser.setTags(tags);
		}

		/** Iterate through source code in a recursive manner. */
		iterate(input);

		/** Publish. */
		for (IPublisher publisher : publishers) {
			publisher.publish(documentModel);
		}
	}

	protected void iterate(File dir) {
		if (dir.isDirectory()) {
			/** Iterativly go through the directory structure. */
			for (String child : dir.list(filter)) {
				iterate(new File(dir, child));				
			}
		}
		else {
			/** Submit the file to each parser. The parser will update the document model accordingly. */
			for (IParser parser : parsers) {
				parser.parse(dir, documentModel);
			}
		}
	}
}
