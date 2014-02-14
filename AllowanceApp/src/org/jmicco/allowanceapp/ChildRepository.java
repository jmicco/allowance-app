package org.jmicco.allowanceapp;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;

/**
 * Hello
 * @author John
 *
 */
public abstract class ChildRepository {

	public static class ChildEntry implements Serializable {	

		private static final long serialVersionUID = 1L;
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
		
		public String getFormattedBalance() {
			return String.format("$%5.2f", balance);
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

	public abstract ChildEntry getChild(long childId);
}
