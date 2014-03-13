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
public class TransactionJournalEntry {
	@Getter @Setter long journalId;					  // The ID of the child journal entry
	@Getter @Setter TransactionType transactionType;  // The type of transaction
	@Getter @Setter long timestampMillis;			  // The time the transaction was made on the device

	@Getter @Setter long transactionId;				  // The transaction Id for this transaction on the device
	@Getter @Setter long childId;                     // The ID of the child being transacted
	@Getter @Setter String description;				  // The description of the transaction
	@Getter @Setter long dateMillis;				  // The date recorded for the transaction
	@Getter @Setter double amount;					  // THe transaction amount
	
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
}
