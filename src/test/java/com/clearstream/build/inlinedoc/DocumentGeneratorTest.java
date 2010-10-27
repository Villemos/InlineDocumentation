package com.clearstream.build.inlinedoc;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

import com.clearstream.build.inlinedoc.DocumentGenerator;
import com.clearstream.build.inlinedoc.DummyPublisher;

import junit.framework.TestCase;



/**
 * @USERSTORY As a developer I can embed documentation in the source code and get it generated automatically, because documentation not in the source code is always out of synch.
 * 
 * */
public class DocumentGeneratorTest extends TestCase {

	public void testExecute() {
		DocumentGenerator generator = new DocumentGenerator();
		
		generator.input = new File("./");		
		DummyPublisher publisher = new DummyPublisher();
		generator.publishers = new IPublisher[] {publisher};
		
		try {
			generator.execute();
			
			assertTrue(publisher.publishCalled == true);
			publisher.print();
		} catch (MojoExecutionException e) {
			e.printStackTrace();
		}
		
		assertTrue(true);
	}
}
