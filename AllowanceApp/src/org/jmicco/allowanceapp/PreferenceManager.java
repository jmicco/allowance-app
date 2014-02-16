package org.jmicco.allowanceapp;

public interface PreferenceManager<T> {
	T getPreferences();
	
	void updatePreferences(T t);
}
