package org.jmicco.parentbank.web;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClientSynchronizationResponse {
	long hwmChildPull;  // The new hwm for child journal entries
	long hwmTransPull;  // The new hwm for transaction journal entries
	List<ChildJournalEntry> childJournal;
	List<TransactionJournalEntry> transactionJournal;
	
	public long getHwmChildPull() {
		return hwmChildPull;
	}
	public void setHwmChildPull(long hwmChildPull) {
		this.hwmChildPull = hwmChildPull;
	}
	public long getHwmTransPull() {
		return hwmTransPull;
	}
	public void setHwmTransPull(long hwmTransPull) {
		this.hwmTransPull = hwmTransPull;
	}
	public List<ChildJournalEntry> getChildJournal() {
		return childJournal;
	}
	public void setChildJournal(List<ChildJournalEntry> childJournal) {
		this.childJournal = childJournal;
	}
	public List<TransactionJournalEntry> getTransactionJournal() {
		return transactionJournal;
	}
	public void setTransactionJournal(
			List<TransactionJournalEntry> transactionJournal) {
		this.transactionJournal = transactionJournal;
	}
}
