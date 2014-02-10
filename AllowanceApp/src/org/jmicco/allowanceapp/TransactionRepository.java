package org.jmicco.allowanceapp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class TransactionRepository {
	
	public static class TransactionEntry implements Serializable {	
		private static final long serialVersionUID = 1L;
		private long transactionId;
		private long childId;
		private String description;
		private Date date;
		private double amount;
		
		TransactionEntry(long transactionId, long childId, Date date, String description, double amount) {
			this.transactionId = transactionId;
			this.childId = childId;
			this.date = date;
			this.description = description;
			this.amount = amount;
		}
		
		public long getTransactionId() {
			return transactionId;
		}

		public void setTransactionId(long transactionId) {
			this.transactionId = transactionId;
		}

		public long getChildId() {
			return childId;
		}

		public void setChildId(long childId) {
			this.childId = childId;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public double getAmount() {
			return amount;
		}

		public void setAmount(double amount) {
			this.amount = amount;
		}
		
		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
		
		public String getFormattedAmount() {
			return String.format("$%5.2f", amount);
		}

		@Override
		public String toString() {
			return String.format(Locale.getDefault(), "%s: $%5.2f %s", date.toString(), amount, description);
		}
	}
	public abstract void open();
	
	public abstract void close();

	public abstract List<TransactionEntry> getTransactions(long childId);
		
	public abstract long addTransaction(TransactionEntry entry);
	
	public abstract void deleteTransaction(TransactionEntry entry);
}
