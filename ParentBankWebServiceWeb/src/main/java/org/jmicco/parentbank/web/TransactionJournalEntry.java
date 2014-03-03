package org.jmicco.parentbank.web;

import javax.xml.bind.annotation.XmlRootElement;

import org.jmicco.parentbank.parentdb.TransactionType;

@XmlRootElement
public class TransactionJournalEntry {
	long journalId;					  // The ID of the child journal entry
	TransactionType transactionType;  // The type of transaction
	long timestampMillis;			  // The time the transaction was made on the device

	long transactionId;				  // The transaction Id for this transaction on the device
	long childId;                     // The ID of the child being transacted
	String description;				  // The description of the transaction
	long dateMillis;				  // The date recorded for the transaction
	double amount;					  // THe transaction amount
	
	public TransactionJournalEntry() {
		this(0L, null, 0L, 0L, 0L, null, 0L, 0.0);
	}
	public TransactionJournalEntry(long journalId, TransactionType transactionType, long timestampMillis, 
			long transactionId, long childId, String description, long dateMillis, double amount) {
		this.journalId = journalId;
		this.transactionType = transactionType;
		this.timestampMillis = timestampMillis;
		this.transactionId = transactionId;
		this.childId = childId;
		this.description = description;
		this.dateMillis = dateMillis;
		this.amount = amount;
	}
	public long getJournalId() {
		return journalId;
	}
	public void setJournalId(long journalId) {
		this.journalId = journalId;
	}
	public TransactionType getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}
	public long getTimestampMillis() {
		return timestampMillis;
	}
	public void setTimestampMillis(long timestampMillis) {
		this.timestampMillis = timestampMillis;
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
	public long getDateMillis() {
		return dateMillis;
	}
	public void setDateMillis(long dateMillis) {
		this.dateMillis = dateMillis;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
}
