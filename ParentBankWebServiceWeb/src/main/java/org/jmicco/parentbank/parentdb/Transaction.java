package org.jmicco.parentbank.parentdb;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Entity(name = "transactions")
@Table(name = "transactions", schema = "parentdb")
@NamedQueries( {
	@NamedQuery(name = "Transaction.FindAllTransactions", 
		query = "SELECT t FROM transactions t WHERE t.key.deviceId = :deviceId and t.childId = :childId")
})
@EqualsAndHashCode
@ToString
public class Transaction {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

	@EmbeddedId
	private Key key;
	@Getter @Setter private long childId;
	private String date;
	@Getter @Setter private String description;
	@Getter @Setter private double amount;
	
	public Transaction() {
		this(null, 0L, null, null, 0.0);
	}
	
	public Transaction(String deviceId, long childId, Instant date, String description, double amount) {
		this.key = new Key();
		this.key.deviceId = deviceId;
		this.childId = childId;
		setDate(date);
		this.description = description;
		this.amount = amount;
	}
	
	public Instant getDate() {
		return date == null ? null : Instant.parse(date, DATE_FORMAT);
	}

	public void setDate(Instant date) {
		if (date == null) {
			this.date = null;
		} else {
			this.date = date.toString(DATE_FORMAT);
		}
	}

	@Embeddable
	@EqualsAndHashCode
	@ToString
	static class Key {
		@GeneratedValue(strategy=GenerationType.IDENTITY)
		private long transactionId;
		private String deviceId;
		
		private Key() {
			this(0, null);
		}
		
		private Key(long transactionId, String deviceId) {
			this.transactionId = transactionId;
			this.deviceId = deviceId;
		}
	}
	
	public long getTransactionId() {
		return key.transactionId;
	}

	void setTransactionId(long transactionId) {
		key.transactionId = transactionId;
	}
	
	public String getDeviceId() {
		return key.deviceId;
	}
	
	public static Transaction find(EntityManager em, long transactionId, DeviceHistory deviceHistory) {
		Key key = new Key(transactionId, deviceHistory.getDeviceId());
		return em.find(Transaction.class, key);
	}
	
	public long persist(EntityManager em, DeviceHistory deviceHistory) {
		em.persist(this);
		long journalId = Sequence.generateId(em);
		TransactionJournal journalEntry = new TransactionJournal(journalId, deviceHistory, TransactionType.CREATE, new Instant(), getTransactionId(), getChildId(), getDescription(), getDate(), getAmount());
		em.persist(journalEntry);
		em.flush();
		return journalEntry.getJournalId();
	}

	public void merge(EntityManager em, DeviceHistory deviceHistory) {
		em.merge(this);
		long journalId = Sequence.generateId(em);
		TransactionJournal journalEntry = new TransactionJournal(journalId, deviceHistory, TransactionType.UPDATE, new Instant(), getTransactionId(), getChildId(), getDescription(), getDate(), getAmount());
		em.persist(journalEntry);		
	}
	
	public void delete(EntityManager em, DeviceHistory deviceHistory) {
		em.detach(this);
		long journalId = Sequence.generateId(em);
		TransactionJournal journalEntry = new TransactionJournal(journalId, deviceHistory, TransactionType.DELETE, new Instant(), getTransactionId(), getChildId(), getDescription(), getDate(), getAmount());
		em.persist(journalEntry);
	}
}
