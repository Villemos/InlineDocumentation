package com.clearstream.build.inlinedoc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @TITLE Configuration Guide
 * To include the inline documentation plugin in you maven build, add the following to the projects pom.xml file;
 *  
 * bq. < plugins > 
 *   < plugin >   
 *     < groupId>com.clearstream.build</groupId >     
 *     < artifactId>maven-inlinedoc-plugin</artifactId >
 *     < version>0.0.8</version >
 *     < configuration >
 *       < input > [full path to your project root folder] < /input >
 *       < publishers >
 *         < publisher implementation="com.clearstream.build.inlinedoc.GithubPublisher" >
 *           < project >[Github repository name, such as "Villemos/InlineDocumentation"]< /project >
 *           < username >[Name of the user with access to the repository]< /username >
 *           < userpassword >[Password of the user in clear text (sorry!)]< /userpassword >
 *         < /publisher >
 *       < /publishers >
 *       < parsers >
 *         < parser implementation="com.clearstream.build.inlinedoc.BasicParser" > < /parser >
 *       < /parsers >
 *     < /configuration >
 *   < /plugin >
 * < /plugins >
 *
 * Note that the plugin is not yet available from any public maven repositories. You have to build and deploy
 * it in your localt repository manually.
 * 
 * Note that this configuration is a basic Github publishing. Additional settings are available, such
 * as proxy and authentication settings. 
 * 
 * To run the plugin, as part of the maven build execute the goal 'com.clearstream.build:maven-inlinedoc-plugin:0.0.8:compile', for
 * example
 * 
 *  *mvn clean com.clearstream.build:maven-inlinedoc-plugin:0.0.8:compile package*
 *  
 * h2. Configuring the Publisher(s)
 * 
 * A publisher 'publish' the documentation. The physical location of the publication can be a web site, file or 
 * something else. It depends on the implementation of the publisher.
 *  
 * The document generator can publish to one or more locations, by configuring multiple publishers. Each publisher
 * is configured in the '< publishers >' section of the plugin configuration.
 * 
 * You can easily write your own publisher, as described in [[IPublisher Interface]] or you can use existing publishers. To 
 * use a publiser, you first of all needs to make sure that its in the claspath of the system. So make sure the 
 * jar containing the publisher is defined as one of the dependencies of the project, i.e. in the project pom.xml.
 * 
 * To add multiple publishers, in the '< configuration >' section of the plugin specification in the project pom.xml, insert
 * 
 * bq. < publishers > 
 *         < publisher implementation="[Full name of publisher class, for example com.clearstream.build.inlinedoc.GithubPublisher]" >
 *           < [Publisher configuration property] >[VALUE] </ [Publisher configuration property] >
 *         < /publisher >
 *         < publisher implementation="[Full name of publisher class, for example com.clearstream.build.inlinedoc.GithubPublisher]" >
 *           < [Publisher configuration property] >[VALUE] </ [Publisher configuration property] >
 *         < /publisher >
 *       < /publishers >
 * 
 * Within the '< publisher >' tag configuration settings can be added. These are specific for the publisher in question.
 * 
 * You can configure the same 'type' of publisher multiple times, for example publishing your documetation to multiple
 * github repositories. Please dont use this to spam people.
 * 
 * @CATEGORY Home
 * @END
 * 
 * @goal compile
 * 
 */
public class DocumentGenerator extends AbstractMojo {
	/**
	 * Location of the file.
	 * @parameter default-value="./"
	 * @required
	 */
	public File input;

	/**
	 * Patterns to exclude
	 * @parameter default-value=""
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

	
	protected FilenameFilter filter = new BasicFilter();
	protected IDocumentModel documentModel = new BasicDocumentModel();


	public void execute() throws MojoExecutionException {

		System.out.println("Stating ClearStream inline documentation generator.");

		System.out.println("Settings:");
		System.out.println("  Input = " + input);
		System.out.println("  Exclude = " + exclude);
		
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
