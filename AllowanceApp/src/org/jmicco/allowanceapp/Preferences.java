package org.jmicco.allowanceapp;

public class Preferences {
	private boolean connectOnline;

	public Preferences(boolean connectOnline) {
		this.connectOnline = connectOnline;
	}
	public Preferences() {
		this(false);
	}

	public boolean isConnectOnline() {
		return connectOnline;
	}

	public void setConnectOnline(boolean connectOnline) {
		this.connectOnline = connectOnline;
	}
}
