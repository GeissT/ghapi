package org.idlesoft.libraries.ghapi;

import biz.source_code.base64Coder.Base64Coder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class APIAbstract {
	public GitHubAPI api;
	public String login, password, oauth_token;

	public static class Response {
		public int statusCode;
		public String resp;
		public String url;
	}

	public static class LiberalHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            /* Just return true */
            return true;
        }
	}

	public APIAbstract(GitHubAPI a)
	{
		api = a;
	}

	/**
	 * Sets login details for authentication
	 *
	 * @param l - String containing Github username
	 * @param p - String containing username's Github password OR their OAuth access token
	 * @param oauth - Whether or not we're using OAuth
	 */
	public void login(String l, String p, boolean oauth)
	{
		login = l;
		if (oauth) {
		    oauth_token = p;
		} else {
		    password = p;
		}
	}

	/**
	 * Sends a HTTP POST request
	 * 
	 * @param url
	 * @param postData
	 * @return Response object containing status code and response body.
	 */
	public Response HTTPPost(String url, String postData)
	{
		Response response = new Response();
		try {
		    if (api.api.oauth_token != null) {
		        if (url.contains("?")) {
		            url = url.concat("&access_token=" + api.api.oauth_token);
		        } else {
		            url = url.concat("?access_token=" + api.api.oauth_token);
		        }
		    }
			// Setup connection
			HttpsURLConnection conn = (HttpsURLConnection) (new URL(url)).openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			// Add HTTP Basic authentication details if we know them and we aren't using OAuth
			if (api.api.oauth_token == null) {
			    if (api.api.login != null && api.api.password != null) {
    				conn.setRequestProperty("Authorization", "Basic "
    										+ Base64Coder.encodeString(
    													(api.api.login + ":" + api.api.password)).replaceAll("\\n",""));
    			}
			}
			conn.setHostnameVerifier(new LiberalHostnameVerifier());
			conn.connect();

			// Send POST data to the server
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(postData);
			out.flush();

			// Get the response from the server
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();

			for (String line = in.readLine(); line != null; line = in.readLine()) {
				sb.append(line + '\n');
			}

			// Store response information in Response object
			try {
				response.statusCode = conn.getResponseCode();
			} catch (IOException e) {
			    e.printStackTrace();
				response.statusCode = 999;
			}
			response.resp = sb.toString();

			// Clean up
			conn.disconnect();
			conn = null;
			out = null;
			in = null;
			sb = null;
		} catch (IOException e) {
		    e.printStackTrace();
			response.statusCode = 999;
		}
		response.url = url;
		return response;
	}

	/**
	 * Sends a HTTP GET request
	 * 
	 * @param url
	 * @return Response object containing status code and response body.
	 */
	public Response HTTPGet(String url)
	{
		Response response = new Response();
		try {
		    if (api.api.oauth_token != null) {
                if (url.contains("?")) {
                    url = url.concat("&access_token=" + api.api.oauth_token);
                } else {
                    url = url.concat("?access_token=" + api.api.oauth_token);
                }
            }
			// Setup connection
			HttpsURLConnection conn = (HttpsURLConnection) (new URL(url)).openConnection();
			conn.setRequestMethod("GET");
			// Add HTTP Basic authentication details if we know them and we aren't using OAuth
            if (api.api.oauth_token == null) {
                if (api.api.login != null && api.api.password != null) {
                System.out.print("basic: " + api.api.login);
                conn.setRequestProperty("Authorization", "Basic "
                                        + Base64Coder.encodeString(
                                                    (api.api.login + ":" + api.api.password)).replaceAll("\\n",""));
                }
            }
			conn.setHostnameVerifier(new LiberalHostnameVerifier());
			conn.connect();

			// Get response from the server
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();

			for (String line = in.readLine(); line != null; line = in.readLine()) {
				sb.append(line + '\n');
			}

			// Store response in a Response object
			try {
				response.statusCode = conn.getResponseCode();
			} catch (IOException e) {
				response.statusCode = 999;
				e.printStackTrace();
			}
			response.resp = sb.toString();

			// Clean up
			conn.disconnect();
			conn = null;
			in = null;
			sb = null;
		} catch (IOException e) {
			response.statusCode = 999;
			e.printStackTrace();
		}
		response.url = url;
		return response;
	}

	public static String encode(String str)
	{
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(str);
		}
	}
}
