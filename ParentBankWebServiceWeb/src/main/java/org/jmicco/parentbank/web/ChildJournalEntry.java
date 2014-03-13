package org.jmicco.parentbank.web;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.jmicco.parentbank.parentdb.TransactionType;

@XmlRootElement
@EqualsAndHashCode
@ToString
public class ChildJournalEntry {
	@Getter @Setter long journalId;					  // The ID of the child journal entry
	@Getter @Setter TransactionType transactionType;  // The type of transaction
	@Getter @Setter long timestampMillis;			  // The time the transaction was made on the device
	@Getter @Setter long childId;		              // The ID of the child on the device
	@Getter @Setter String name;					  // The name of the child on the device
	
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
}
