package org.jmicco.allowanceapp;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.http.AndroidHttpClient;
import android.util.Log;

public class ServerConnector {
	private static final String LOG_TAG = ServerConnector.class.getSimpleName();
	
	private static final String SERVER_URL = "http://192.168.1.154:10080/ParentBankWebServiceWeb/rest/sync";
	private static final String GET_USER = "/hello";
	private static final String SET_USER = "/setuser";
	
	private final AndroidHttpClient httpClient;
	
	public ServerConnector() {
		httpClient = AndroidHttpClient.newInstance("parentBankApp");
	}
	
	public User getUser() throws ClientProtocolException, IOException, JSONException {
		HttpGet request = new HttpGet(SERVER_URL + GET_USER);
		HttpResponse response = httpClient.execute(request);
		String content = EntityUtils.toString(response.getEntity());
		JSONTokener tokener = new JSONTokener(content);
		return ConvertJsonUser.createUser((JSONObject) tokener.nextValue());
	}
	
	public void sendUser(User user) throws JSONException, IOException {
		HttpPost post = new HttpPost(SERVER_URL + SET_USER);
		post.setHeader("content-type", "application/json");
		
		JSONObject object = ConvertJsonUser.createJson(user);
		StringEntity entity = new StringEntity(object.toString());
		post.setEntity(entity);
		HttpResponse response = httpClient.execute(post);	
		Log.d(LOG_TAG, response.getStatusLine().toString());
	}

	public void stop() {
		httpClient.close();
	}

}
