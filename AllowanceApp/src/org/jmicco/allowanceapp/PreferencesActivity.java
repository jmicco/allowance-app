package org.jmicco.allowanceapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class PreferencesActivity extends Activity {
	private PreferenceManager<Preferences> preferenceManager;
	private CheckBox connectOnline;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_activity);
		preferenceManager = new PreferenceManagerAndroid(this);
		connectOnline = (CheckBox) findViewById(R.id.checkbox_connect_online);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Preferences preferences = preferenceManager.getPreferences();
		connectOnline.setChecked(preferences.isConnectOnline());
	}
	
	public void toggleConnectOnline(View view) {
		Preferences preferences = preferenceManager.getPreferences();
		preferences.setConnectOnline(connectOnline.isChecked());
		preferenceManager.updatePreferences(preferences);
	}
}
