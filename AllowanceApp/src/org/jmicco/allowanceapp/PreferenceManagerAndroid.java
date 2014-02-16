package org.jmicco.allowanceapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceManagerAndroid implements PreferenceManager<Preferences> {
	private static final String CONNECT_ONLINE_KEY = "connect_online";

	private SharedPreferences sharedPrefs;
	
	public PreferenceManagerAndroid(Context context) {
		sharedPrefs = context.getSharedPreferences(
		        context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
	}

	@Override
	public Preferences getPreferences() {
		return new Preferences(
				sharedPrefs.getBoolean(CONNECT_ONLINE_KEY, false));		
	}

	@Override
	public void updatePreferences(Preferences preferences) {
		Editor editor = sharedPrefs.edit();
		editor.putBoolean(CONNECT_ONLINE_KEY, preferences.isConnectOnline());
		editor.commit();
	}

}
