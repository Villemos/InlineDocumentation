package com.clearstream.build.inlinedoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import com.clearstream.build.inlinedoc.IDocumentModel.DocumentEntry;

/** 
 * @TITLE Github Publisher
 * The Github Publisher renders the documentation into a Github wiki. The documentation
 * is pushed in 'Textile' format over HTTP. 
 * @CATEGORY Publisher
 * @END
 * 
 * */

public class GithubPublisher implements IPublisher {

	/**
	 * @TITLE Configuring the Github Publisher
	 * 
	 * The Github publisher is configured in the projects POM, as part of the 'configuration' section of the plugin configuration. The entries marked in bold are required.
	 * 
	 * bq. < configuration >
	 * 	 < publishers >
	 *     < publisher implementation="com.clearstream.build.inlinedoc.GithubPublisher" >
	 *       *< username>user</username >*
	 *       *< userpassword>password</userpassword >*						
	 *       *< project>name/project</project >*
	 *       < proxyEnabled>false</proxyEnabled >
	 *       < proxyProtocol>http</proxyProtocol >
	 *       < proxyServer>localhost</proxyServer >
	 *       < proxyPort>8080</proxyPort > 
	 *       < format > [format] < /format >
	 *     < /publisher >						
	 *   < /publishers >
	 * < /configuration >
	 * 
	 * h2. Specific Settings
	 * 
	 * The (set of) regular patterns which can be embedded in the existing Github wiki pages to ensure that the
	 * page is not overwritten.
	 * @parameter default-value="<input type="hidden" id="writeprotection"/>"
	 */	
	String[] writeProtections = new String[] {"<\\s+input type=\"hidden\" id=\"writeprotection\"/\\s+>"};
	
	/**
	 * Tags which this parser supports.
	 * @parameter default-value="@(CATEGORY)\\s+(.+?)$"
	 */
	String[] tags = new String[] {"@(CATEGORY)\\s+(.+?)$"}; 

	/**
	 * Project on Github to publish too.
	 * @parameter
	 * @required
	 */
	String project;

	/**
	 * The user name to login to github as and to post the wiki entries as.
	 * @parameter
	 * @required
	 */
	String username;

	/**
	 * The user password to login to github as
	 * @parameter
	 * @required
	 */
	String userpassword;

	/**
	 * The header text to be displayed in the wiki
	 * @parameter default-value="_*IMPORTANT: Do not edit. This entry has been auto generated using \"ClearStream Inline Documentation\":http://github.com/Villemos/InlineDocumentation/wiki.*_"
	 */
	String header = "_*IMPORTANT: Do not edit. This entry has been auto generated using \"ClearStream Inline Documentation\":http://github.com/Villemos/InlineDocumentation/wiki.*_";

	/**
	 * Flag enabling / disabling proxy. If you are behind a firewall, then most likely you need to configure this, i.e. enable it and set
	 * the proxyServer and proxyPort as needed.
	 * @parameter default-value="false"
	 */	
	boolean proxyEnabled = false;

	/**
	 * Protocol of the proxy
	 * @parameter default-value="http
	 */
	String proxyProtocol = "http";

	/**
	 * The IP address of the proxy
	 * @parameter default-value="localhost"
	 */
	String proxyServer = "localhost";

	/**
	 * The port of the proxy
	 * @parameter default-value="8080"
	 */
	int proxyPort = 8080;

	/**
	 * Flag indicating whether the proxy requires authentication
	 * @parameter default-value="false"
	 */
	boolean proxyAuthenticationEnabled = false;

	/**
	 * Proxy authentication user
	 * @parameter default-value=""
	 */
	String proxyUser;

	/**
	 * Proxy authentication password
	 * @parameter default-value=""
	 */
	String proxyPassword;

	/**
	 * The format in which the documentation has been written and in which it should be published into Github. The supported formats are
	 * 1. asciidoc. The "AsciiDoc":http://www.methods.co.nz/asciidoc/ format. 
	 * 2. creole. The "Creole":http://en.wikipedia.org/wiki/Creole_language
	 * 3. markdown. The "Markdown":http://en.wikipedia.org/wiki/Markdown format.
	 * 4. org. The "OrgMode":http://orgmode.org/ format.
	 * 5. pod. The "PerlPod":http://perldoc.perl.org/perlpod.html format.
	 * 6. rdoc. The "Ruby Documentation System":http://rdoc.sourceforge.net/doc/index.html format
	 * 7. textile. The "Textile":http://textism.com/tools/textile/ format.
	 * 8. reStructuredText. The "Restructured Text":http://docutils.sourceforge.net/rst.html format.
	 * @parameter default-value="textile"
	 */
	String format = "textile";
	/**
	 * @CATEGORY Configuration Guide
	 * @CATEGORY Github Publisher
	 * @CATEGORY Howto
	 * @END
	 */
	
	protected HttpClient client = new HttpClient();	

	// http://github.com/JohannesKlug/hummingbird/wiki/_new?wiki[name]=TEST
	protected String siteProtocol = "http";
	protected String siteServer = "github.com";
	protected int sitePort = 80;

	@Override
	public void publish(IDocumentModel model) {

		System.out.println("Publishing to Github repository " + project);

		// This is for the Logica proxy
		if (proxyEnabled == true) {
			client.getHostConfiguration().setProxy(proxyServer, proxyPort);

			if (proxyAuthenticationEnabled == true) {
				client.getState().setProxyCredentials(
						new AuthScope(proxyProtocol + "://" + proxyServer, proxyPort, AuthScope.ANY_REALM),
						new UsernamePasswordCredentials(proxyUser, proxyPassword));
			}
		}

		ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory( );
		Protocol easyhttps = new Protocol( "https", socketFactory, 443);
		Protocol.registerProtocol( "https", easyhttps );

		String token = login();

		/** Idetify all categories. */
		Map<String, List<String>> categories = new HashMap<String, List<String>>();

		Iterator<Entry<String, DocumentEntry>> it = model.getAllEntries("Document").entrySet().iterator();
		while (it.hasNext() == true) {
			Entry<String, DocumentEntry> entry = it.next();

			if (categories.containsKey(entry.getKey()) == false) {
				categories.put(entry.getKey(), new ArrayList<String>());
			}

			if (entry.getValue().tags.get("CATEGORY") != null) {
				for (String category : entry.getValue().tags.get("CATEGORY")) {

					if (categories.containsKey(category) == false) {
						categories.put(category, new ArrayList<String>());
					}

					categories.get(category).add(entry.getKey());
				}
			}
		}

		/** Reiterate and publish. */
		it = model.getAllEntries("Document").entrySet().iterator();
		while (it.hasNext() == true) {
			Entry<String, DocumentEntry> pair = it.next();
			String title = pair.getKey();

			String description = pair.getValue().text + "\n"; 

			String subs = "";

			if (categories.get(pair.getKey()).isEmpty() == false) {
				subs = "h2. Subsections\n\n";
				for (String text : categories.get(pair.getKey())) {
					subs += "[[" + text + "]]" + "\n";
				}
			}

			submit(title, header + "\n\n" + description + "\n\n" + subs, token);
		}

		String categoryPage = "<br/><br/>";
		Iterator<Entry<String, List<String>>> it2 = categories.entrySet().iterator();
		while (it2.hasNext() == true) {
			Entry<String, List<String>> entry4 = it2.next();
			categoryPage += "[[" + entry4.getKey() + "]] <br/>";
		}
		submit("Wiki Categories", header + "<br/><br/>" + categoryPage, token);
	}

	protected String login() {
		String tokenstr = "";
		try {
			URI uri = new URI("https://github.com/login", true);

			GetMethod get = new GetMethod(uri.toString());
			get.setDoAuthentication(true);
			int status = client.executeMethod(get);

			InputStream input = get.getResponseBodyAsStream();
			InputStreamReader reader = new InputStreamReader(input); 
			Pattern tokenPattern = Pattern.compile("<input name=\"authenticity_token\" type=\"hidden\" value=\"(.+?)\" \\/>");
			Matcher tokenMatcher = tokenPattern.matcher(readFully(reader));
			if (tokenMatcher.find() == true) {

				tokenstr = tokenMatcher.group(1);

				input.close();
				reader.close();

				uri = new URI("https://github.com/session", true);
				PostMethod  post = new PostMethod(uri.toString());
				post.setDoAuthentication(true);

				NameValuePair name = new NameValuePair("login", username);
				NameValuePair password = new NameValuePair("password", userpassword);				
				NameValuePair token = new NameValuePair("authenticity_token", tokenstr);
				NameValuePair commit = new NameValuePair("commit", "Log in");

				post.setRequestBody(new NameValuePair[] {name, password, token, commit});
				status = client.executeMethod(post);

				input = post.getResponseBodyAsStream();
				reader = new InputStreamReader(input); 
				readFully(reader);
				System.out.println("Login completed with status '" + status + "'. Session token is '" + tokenMatcher.group(1) + "'.");
			}			
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// release any connection resources used by the method
			// authpost.releaseConnection();
		}		

		return tokenstr;
	}

	protected void submit(String title, String page, String tokenstr) {
		try {
			/** Check if the entry already exist. */
			String uri = "http://github.com/" + project + "/wiki/" + title.replaceAll("\\s", "-");
			GetMethod get = new GetMethod(uri);
			get.setDoAuthentication(true);

			NameValuePair name = new NameValuePair("wiki[name]", title);			
			NameValuePair token = new NameValuePair("authenticity_token", tokenstr);
			NameValuePair body = new NameValuePair("wiki[body]", page);
			NameValuePair commit = new NameValuePair("wiki[commit]", "");
			NameValuePair formatField = new NameValuePair("wiki[format]", format);
			NameValuePair method = new NameValuePair("_method", "put");

			System.out.println("TEST format = " + format);
			
			client.executeMethod(get);
			InputStream input = get.getResponseBodyAsStream();
			InputStreamReader reader = new InputStreamReader(input); 
			String text = readFully(reader);

			PostMethod post = null; 
			if (text.indexOf("New Wiki Page") == -1) {

				for (String writeProtection : writeProtections) {
					Pattern pattern = Pattern.compile(writeProtection);
					Matcher matcher = pattern.matcher(page);
					if (matcher.find() == true) {
						System.out.println("Did not post '" + title + "' as write protection has been set.");
						return;
					}
				}

				/** Entry exist. Update. */
				uri = "http://github.com/" + project + "/wiki/" + title.replaceAll("\\s", "-");

				post = new PostMethod(uri);
				post.setDoAuthentication(true);

				post.setRequestBody(new NameValuePair[] {name, token, body, commit, formatField, method});
			}
			else {
				/** New entry. Create. */
				uri = "http://github.com/" + project + "/wiki";

				post = new PostMethod(uri);
				post.setDoAuthentication(true);

				post.setRequestBody(new NameValuePair[] {name, token, body, commit, formatField});
			}

			int status = client.executeMethod(post);

			if (status == 302) {
				System.out.println("Successfully posted '" + title + "' to github.");
			}
			else {
				System.out.println("Failed to post '" + title + "' to github. HTTP code " + status + ".");
			}

			input = post.getResponseBodyAsStream();
			reader = new InputStreamReader(input); 
			text = readFully(reader);
			// System.out.println(text);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// release any connection resources used by the method
			// authpost.releaseConnection();
		}
	}

	public String readFully(Reader input) throws IOException {
		BufferedReader bufferedReader = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
		StringBuffer result = new StringBuffer();
		char[] buffer = new char[4 * 1024];
		int charsRead;
		while ((charsRead = bufferedReader.read(buffer)) != -1) {
			result.append(buffer, 0, charsRead);
		}	        
		return result.toString();
	}

	@Override
	public String[] getTags() {
		return tags;
	}

}
