package org.jmicco.allowanceapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class BackgroundReconcileAccountOnline {
	private final String email;
	private final String deviceId;
	private final DatabaseHelper dbhelper;
	private final Context context;
	private Intent intent = null;
	
	public static class UnableToFindAccountException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnableToFindAccountException(String message) {
			super(message);
		}
	}
	
	public BackgroundReconcileAccountOnline(Context context, DatabaseHelper dbhelper) throws UnableToFindAccountException {
		this.context = context;
		this.dbhelper = dbhelper;
		
		try {
			email = getEmail(context);
			// TODO(jmicco): This requires extra permissions - see if it can be avoided
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			deviceId = telephonyManager.getDeviceId();
		} catch (RuntimeException e) {
			throw new UnableToFindAccountException(e.getMessage());
		}
		if (email == null) {
			throw new UnableToFindAccountException("Unable to find the main account for this device");
		}
		System.out.println("email: " + email);
		System.out.println("deviceId: " + deviceId);
	}

	public void stop() {
		if (intent != null) {
			context.stopService(intent);
			intent = null;
		}
	}

	public void start() {
//		SQLiteOpenHelperRegistry.register(ExtraTagConstants.EXTRA_HELPER_KEY, dbhelper);
//		
//		intent = new Intent(context, SymmetricService.class);
//		
//		intent.putExtra(SymmetricService.INTENTKEY_SQLITEOPENHELPER_REGISTRY_KEY, ExtraTagConstants.EXTRA_HELPER_KEY);
//		intent.putExtra(SymmetricService.INTENTKEY_REGISTRATION_URL, "http://192.168.1.154:9090/sync/parentdb-000");
//		intent.putExtra(SymmetricService.INTENTKEY_EXTERNAL_ID, "001");
//		intent.putExtra(SymmetricService.INTENTKEY_NODE_GROUP_ID, "androidapp");
//		intent.putExtra(SymmetricService.INTENTKEY_START_IN_BACKGROUND, true);
//		
//		Properties properties = new Properties();
//		// initial load existing notes from the Client to the Server
//		properties.setProperty(ParameterConstants.AUTO_RELOAD_REVERSE_ENABLED, "true");
//		intent.putExtra(SymmetricService.INTENTKEY_PROPERTIES, properties);

//		context.startService(intent);
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
