package org.jmicco.allowanceapp;

import android.util.Log;

public class Synchronizer implements Runnable {
	private static final String LOG_TAG = Synchronizer.class.getSimpleName();

	private final ServerConnector serverConnector;
	private final String email;
	private final String deviceId;
	
	public Synchronizer(String email, String deviceId) {
		serverConnector = new ServerConnector();
		this.email = email;
		this.deviceId = deviceId;
	}
	
	@Override
	public void run() {
		try {
			User user = serverConnector.getUser();
			Log.d(LOG_TAG, String.format("User: %s", user.getEmail()));
			serverConnector.sendUser(user);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(LOG_TAG, "Unable to reconcile");
		}
	}

	public void stop() {
		serverConnector.stop();
	}

}
