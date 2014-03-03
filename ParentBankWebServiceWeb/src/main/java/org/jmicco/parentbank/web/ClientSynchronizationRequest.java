package org.jmicco.parentbank.web;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClientSynchronizationRequest {
	String deviceId;    // The unique device ID for this device
	String email;       // The main user email for this device
	long hwmChildPull;  // The highest group transaction on the server already pulled
	long hwmChildPush;  // The new hwm child journal for this push
	long hwmTransPull;  // The highest group transaction on the server already pulled
	long hwmTransPush;  // The new hwm transaction journal for this push
	List<ChildJournalEntry> childJournal;
	List<TransactionJournalEntry> transactionJournal;
	
	public ClientSynchronizationRequest() {
		this(null, null, 0L, 0L, 0L, 0L, null, null);
	}
	public ClientSynchronizationRequest(String deviceId, String email, 
			long hwmChildPull, long hwmChildPush, long hwmTransPull, long hwmTransPush, 
			List<ChildJournalEntry> childJournal, List<TransactionJournalEntry> transactionJournal) {
		this.deviceId = deviceId;
		this.email = email;
		this.hwmChildPull = hwmChildPull;
		this.hwmChildPush = hwmChildPush;
		this.hwmTransPull = hwmTransPull;
		this.hwmTransPush = hwmTransPush;
		this.childJournal = childJournal;
		this.transactionJournal = transactionJournal;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public long getHwmChildPull() {
		return hwmChildPull;
	}
	public void setHwmChildPull(long hwmChildPull) {
		this.hwmChildPull = hwmChildPull;
	}
	public long getHwmChildPush() {
		return hwmChildPush;
	}
	public void setHwmChildPush(long hwmChildPush) {
		this.hwmChildPush = hwmChildPush;
	}
	public long getHwmTransPull() {
		return hwmTransPull;
	}
	public void setHwmTransPull(long hwmTransPull) {
		this.hwmTransPull = hwmTransPull;
	}
	public long getHwmTransPush() {
		return hwmTransPush;
	}
	public void setHwmTransPush(long hwmTransPush) {
		this.hwmTransPush = hwmTransPush;
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
