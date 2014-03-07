package org.jmicco.parentbank.parentdb;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;

import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Entity(name = "transactions")
@Table(name = "transactions", schema = "parentdb")
public class Transaction {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

	@EmbeddedId
	private Key key;
	private long childId;
	private String date;
	private String description;
	private double amount;
	
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
	private static class Key {
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
	
	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;		
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description= description ;
	}
	
	public long getChildId() {
		return childId;
	}
	
	public long getTransactionId() {
		return key.transactionId;
	}
	
	public String getDeviceId() {
		return key.deviceId;
	}
	
	public static Transaction find(EntityManager em, long transactionId, DeviceHistory deviceHistory) {
		Key key = new Key(transactionId, deviceHistory.getDeviceId());
		return em.find(Transaction.class, key);
	}
	
	public void persist(EntityManager em, DeviceHistory deviceHistory) {
		em.persist(this);
		long journalId = Sequence.generateId(em);
		TransactionJournal journalEntry = new TransactionJournal(journalId, deviceHistory, TransactionType.CREATE, new Instant(), getTransactionId(), getChildId(), getDescription(), getDate(), getAmount());
		em.persist(journalEntry);
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
