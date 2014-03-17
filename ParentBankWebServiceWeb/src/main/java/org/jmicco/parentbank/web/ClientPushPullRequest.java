package org.jmicco.parentbank.web;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@XmlRootElement
@EqualsAndHashCode
@ToString
public class ClientPushPullRequest {
	@Getter @Setter String deviceId;    // The unique device ID for this device
	@Getter @Setter String email;       // The main user email for this device
	@Getter @Setter long hwmChildPull;  // The highest group transaction on the server already pulled
	@Getter @Setter long hwmChildPush;  // The new hwm child journal for this push
	@Getter @Setter long hwmTransPull;  // The highest group transaction on the server already pulled
	@Getter @Setter long hwmTransPush;  // The new hwm transaction journal for this push
	@Getter @Setter List<ChildJournalEntry> childJournal;
	@Getter @Setter List<TransactionJournalEntry> transactionJournal;
	
	public ClientPushPullRequest() {
		this(null, null, 0L, 0L, 0L, 0L, null, null);
	}
	public ClientPushPullRequest(String deviceId, String email, 
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
}
