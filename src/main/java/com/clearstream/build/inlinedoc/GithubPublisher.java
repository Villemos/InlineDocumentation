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
 * @CATEGORY Document Generator Design
 * @CATEGORY Document Generator Publisher
 * @END
 * 
 * */

public class GithubPublisher implements IPublisher {

	/**
	 * Tags which this parser supports.
	 * @parameter
	 */
	String[] tags = new String[] {"@(CATEGORY)\\s+(.+?)$"}; 
	
	/**
	 * Project on Github to publish too.
	 * @parameter
	 */
	String project = "Villemos/InlineDocumentation";

	/**
	 * The user name to login to github as
	 * @parameter
	 */
	String username = "gert.villemos@logica.com";
	
	/**
	 * The user password to login to github as
	 * @parameter
	 */
	String userpassword = "path2000";

	/**
	 * The header text to be displayed in the wiki
	 * @parameter
	 */
	String header = "_*IMPORTANT: Do not edit. This entry has been auto generated from the source code by the ClearStream inline maven plugin.*_";

	/**
	 * Flag enabling / disabling proxies.
	 * @parameter
	 */	
	boolean proxyEnabled = false;
	
	/**
	 * Protocol of the proxy
	 * @parameter
	 */
	String proxyProtocol = "http";
	
	/**
	 * The IP address of the proxy
	 * @parameter
	 */
	String proxyServer = "10.48.187.80";
	
	/**
	 * The port of the proxy
	 * @parameter
	 */
	int proxyPort = 8080;
	
	/**
	 * Flag indicating whether the proxy requires authentication
	 * @parameter
	 */
	boolean proxyAuthenticationEnabled = false;
	
	/**
	 * Proxy authentication user
	 * @parameter
	 */
	String proxyUser;
	
	/**
	 * Proxy authentication password
	 * @parameter
	 */
	String proxyPassword;


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
				subs = "Subsections:<br/>";
				for (String text : categories.get(pair.getKey())) {
					subs += "* [[" + text + "]]" + "\n";
				}
			}

			submit(title, header + "<br/>" + description + "<br/>" + subs, token);
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
				String page = readFully(reader);
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
			NameValuePair format = new NameValuePair("wiki[format]", "textile");
			NameValuePair method = new NameValuePair("_method", "put");

			client.executeMethod(get);
			InputStream input = get.getResponseBodyAsStream();
			InputStreamReader reader = new InputStreamReader(input); 
			String text = readFully(reader);

			PostMethod post = null; 
			if (text.indexOf("New Wiki Page") == -1) {
				/** Entry exist. Update. */
				uri = "http://github.com/" + project + "/wiki/" + title.replaceAll("\\s", "-");

				post = new PostMethod(uri);
				post.setDoAuthentication(true);

				post.setRequestBody(new NameValuePair[] {name, token, body, commit, format, method});
			}
			else {
				/** New entry. Create. */
				uri = "http://github.com/" + project + "/wiki";

				post = new PostMethod(uri);
				post.setDoAuthentication(true);

				post.setRequestBody(new NameValuePair[] {name, token, body, commit, format});
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
