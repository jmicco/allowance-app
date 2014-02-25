package org.jmicco.allowanceapp;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONTokener;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BackgroundReconcileAccountOnline {
	private static final String LOG_TAG = BackgroundReconcileAccountOnline.class.getSimpleName();

	private final String email;
	private final String deviceId;
	private final DatabaseHelper dbhelper;
	private final Context context;
	private final ScheduledExecutorService executor;
	private final Synchronizer synchronizer;
	
	public static class UnableToFindAccountException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnableToFindAccountException(String message) {
			super(message);
		}

		public UnableToFindAccountException(Exception e) {
			super(e.getMessage(), e);
		}
	}
	
	public BackgroundReconcileAccountOnline(Context context, DatabaseHelper dbhelper) throws UnableToFindAccountException {
		this.context = context;
		this.dbhelper = dbhelper;
		this.executor = Executors.newScheduledThreadPool(1);

		try {
			email = getEmail(context);
			// TODO(jmicco): This requires extra permissions - see if it can be avoided
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			deviceId = telephonyManager.getDeviceId();
		
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new UnableToFindAccountException(e.getMessage());
		}
		
		if (email == null) {
			throw new UnableToFindAccountException("Unable to find the main account for this device");
		}
		Log.d(LOG_TAG, "email: " + email);
		Log.d(LOG_TAG, "deviceId: " + deviceId);
		this.synchronizer = new Synchronizer(email, deviceId);
		executor.scheduleAtFixedRate(synchronizer, 0, 1, TimeUnit.MINUTES);
	}

	public void stop() {
		synchronizer.stop();
		executor.shutdown();
	}

	public void start() {
	}

	private static String getEmail(Context context) {
		AccountManager accountManager = AccountManager.get(context); 
		Account account = getAccount(accountManager);

		if (account == null) {
		  return null;
		} else {
		  return account.name;
		}
	}
	
	 private static Account getAccount(AccountManager accountManager) {
		 Account[] accounts = accountManager.getAccountsByType("com.google");
		 Account account;
		 if (accounts.length > 0) {
			 account = accounts[0];      
		 } else {
		     account = null;
		 }
		 return account;
	  }
}
