package org.jmicco.parentbank.web;

import javax.xml.bind.annotation.XmlRootElement;

import org.jmicco.parentbank.parentdb.TransactionType;

@XmlRootElement
public class ChildJournalEntry {
	long journalId;					  // The ID of the child journal entry
	TransactionType transactionType;  // The type of transaction
	long timestampMillis;			  // The time the transaction was made on the device
	long childId;		              // The ID of the child on the device
	String name;					  // The name of the child on the device
	
	public ChildJournalEntry() {
		this(0L, null, 0L, 0L, null);
	}
	
	public ChildJournalEntry(
			long journalId, TransactionType transactionType, long timestampMillis, long childId, String name) {
		this.journalId = journalId;
		this.transactionType = transactionType;
		this.timestampMillis = timestampMillis;
		this.childId = childId;
		this.name = name;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}
	public long getChildId() {
		return childId;
	}
	public void setChildId(long childId) {
		this.childId = childId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public long getJournalId() {
		return journalId;
	}

	public void setJournalId(long journalId) {
		this.journalId = journalId;
	}

	public long getTimestampMillis() {
		return timestampMillis;
	}

	public void setTimestampMillis(long timestampMillis) {
		this.timestampMillis = timestampMillis;
	}
}
