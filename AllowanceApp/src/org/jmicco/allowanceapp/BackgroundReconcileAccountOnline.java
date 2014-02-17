package org.jmicco.allowanceapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.telephony.TelephonyManager;

public class BackgroundReconcileAccountOnline {
	private String email = null;
	private String deviceId = null;

	public static class UnableToFindAccountException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnableToFindAccountException(String message) {
			super(message);
		}
	}
	
	public BackgroundReconcileAccountOnline(Context context) throws UnableToFindAccountException {
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
		// TODO Auto-generated method stub
	}

	public void start() {
		// TODO Auto-generated method stub
		
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
