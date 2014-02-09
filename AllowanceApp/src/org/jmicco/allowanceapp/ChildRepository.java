package org.jmicco.allowanceapp;

import java.util.List;
import java.util.Locale;

public abstract class ChildRepository {

	public static class ChildEntry {	

		private long childId;
		private String name;
		private double balance;
		
		ChildEntry(long childId, String name, double balance) {
			this.childId = childId;
			this.name = name;
			this.balance = balance;
		}
		
		public double getBalance() {
			return balance;
		}

		public void setBalance(double balance) {
			this.balance = balance;
		}

		public long getChildId() {
			return childId;
		}

		public String getName() {
			return name;
		}
		
		public void setChildId(long childId) {
			this.childId = childId;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return String.format(Locale.getDefault(), "%s : $%5.2f", name, balance);
		}
	}
	public abstract void open();
	
	public abstract void close();

	public abstract List<ChildEntry> getChildren();
	
	public abstract void updateChild(ChildEntry entry);
	
	public abstract long addChild(ChildEntry entry);
	
	public abstract ChildEntry getChild(String name);
}
