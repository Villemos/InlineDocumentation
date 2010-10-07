package com.clearstream.build.inlinedoc;

import java.util.List;


/**
 * @TITLE IPublisher Interface
 * Each document generator has a publisher attached. The document model created by
 * the parser is pushed to the publisher for rendering. The specific rendering is most
 * likely technology specific and can be in any form; file on disk, database, website, etc.
 * 
 * To implement a new publisher, overload this interface 
 * @CATEGORY Document Generator
 * @CATEGORY Interface
 * @END
 * 
 * */
public interface IPublisher {
	public void publish(IDocumentModel model);
	
	public String[] getTags();
}
