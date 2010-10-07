/**
 * @TITLE Document Generator
 * The document parser is a MOJO, i.e. a Maven plugin, creating documentation based on the
 * source code. It is executed together with the build and follows the API of Maven plugins.
 * 
 * The only consistent set of information regarding the system is the source code. All other
 * documentation is detached and thus very likely out of synch. It is very likely that a 
 * developer updating the source code will happily forget about the documentation.
 * 
 * We therefore believe that the documentation must be in the source code itself. The 
 * Document Generator is such a tool. It parses the source code and extracts documentation based
 * on annotations inserted in the source code. The documentation is thereafter organised and 
 * published.
 * 
 * @CATEGORY Components
 * @END
 * 
 * 
 * @TITLE Document Generator Design
 * Internally the document generator contains four objects performing the actual processing
 * 1. The parser (implements IParser). The object performing the actual parsing of the source code files.
 * 2. The filter (implements FilenameFilter). The filename filter used to select files to be parsed.
 * 3. The document model (implements IDocumentModel). A structured model of the documentation entries extracted.
 * 4. The publisher (implements IPublisher). The object performing the actual rendering.
 * 
 * When started as part of the build, the document generator instantiates each of the fields
 * using reflection. For each field a corresponding class name configuration variable exist. It is
 * thus vry simpel to change the way that the component process; simply implement a new class, then
 * use the classname configuration attribute of the document generator in the build POM to specify 
 * your new class and restart the build.  
 *
 * The generator iterates through the source code tree, starting from the project root. All files
 * parsing the filter are included and submitted to the parser. The parser build a document model,
 * representing the documentation entries. The document model is thereafter send to the publisher
 * for rendering to some external format.  
 * 
 * The processing will look for a series of special tags in the source code. These are explained in detail 
 * in the user guide. The tags are:
 *   TITLE [title]. Marks the beginning of an entry. All lines until next marker is considered description.
 *   CATEGORY [category]. Marks the end of an entry and marks the entry as being a specific category.
 *   END. Marks the end of a entry.
 *   
 * @CATEGORY Design
 * @CATEGORY Document Generator
 * @END
 * 
 * @TITLE Design
 * The design documentation describes the implementation of each component. 
 * @END
 * 
 * @TITLE Interface
 * The interface documentation describes the interfaces of each component. 
 * @END
 * 
 * @TITLE User Guide
 * The user guides describes the usage of the system at runtime. 
 * @END
 * 
 * @TITLE Configuration Guide
 * The configuration guides describes the configuration and setup of the system. 
 * @END
 * 
 * 
 * */
package com.clearstream.build.inlinedoc;