package org.jmicco.allowanceapp;

import org.json.JSONException;
import org.json.JSONObject;

public class ConvertJsonUser {
	static User createUser(JSONObject obj) throws JSONException {
		String email = obj.getString("email");
		return new User(email);
	}
	
    static JSONObject createJson(User user) throws JSONException {
		JSONObject object = new JSONObject();
		object.put("email", user.getEmail());
		return object;
	}
}
