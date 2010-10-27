/**
 * 
 * @TITLE Home
 * The only thing on your project that is surely up to date, is your source code. Your documentation is
 * detached and thus per definition out of synch. Rest assured that no matter the effort from your side 
 * to keep the two aligned; next time a developer updates the source code, he will surely forget to update
 * the documentation. 
 * 
 * The ClearStream Inline Documentation plugin doesnt solve this problem, but it makes it much easier to solve.
 * The fundamental problem is that the developer will forget to update the document. And even if he thinks about 
 * it, where is it? And which version? And which part?
 * 
 * The inline documentation allows you to document the project inline, i.e. in the source code itself.
 * Nothing fancy; you likely do this anyway. The main difference is that the Clear Stream Inline Documentation 
 * allows you to define structured documents as comments in the code, linking them and categorizing them. The
 * documents can thereafter be published, for example here on Github.
 * @END
 *  
 *  
 *  
 * @TITLE Design
 * The inline documenter is a MOJO, i.e. a Maven plugin, creating documentation based on the
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
 * Internally the document generator contains four objects performing the actual processing
 * 1. The parser (implements IParser). The object performing the actual parsing of the source code files.
 * 2. The filter (implements FilenameFilter). The filename filter used to select files to be parsed.
 * 3. The document model (implements IDocumentModel). A structured model of the documentation entries extracted.
 * 4. The publisher (implements IPublisher). The object performing the actual rendering.
 * 
 * When started as part of the build, the document generator instantiates each of the fields
 * using reflection. For each field a corresponding class name configuration variable exist. It is
 * thus very simple to change the way that the component process; simply implement a new class, then
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
 * @CATEGORY Home
 * @END
 *
 * 
 * 
 * @TITLE Interfaces
 * The interface documentation describes the interfaces of each component.
 * @CATEGORY Home 
 * @END
 * 
 * 
 * @TITLE Howto
 * @CATEGORY Home
 * @END
 * 
 * 
 * @TITLE Protecting Wiki Entries
 * The document generator will overwrite any page already existing in Github. As Github has a version history on the wiki's, this
 * is not so bad, but might still lead to unwanted results. Someone adding a document entry with an already existing name might overwrite
 * an entry added manually by a user of the github project.
 * 
 * To protect against this, a special marker can be set within the existing pages. The marker is configured together with the
 * Publisher. The default marker is the following html snippet anywhere in the wiki page '<input type="hidden" id="writeprotection"/>'.
 * @CATEGORY Howto
 * @END
 * 
 * 
 * */
package com.clearstream.build.inlinedoc;