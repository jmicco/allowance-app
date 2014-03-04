package org.jmicco.parentbank.parentdb;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sun.istack.Nullable;

@Entity(name = "transaction_journal")
@Table(name = "transaction_journal", schema = "parentdb")
public class TransactionJournal {
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZZ");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

	@EmbeddedId
	private Key key;
	
	@ManyToOne
	@JoinColumn(name = "deviceId", insertable = false, updatable = false)
	private DeviceHistory deviceHistory;
	
	@Enumerated(EnumType.ORDINAL)
	private TransactionType transactionType;
	
	private String timestamp;

	private long transactionId;
	private long childId;
	private String description;
	private String date;
	private double amount;
	
	public TransactionJournal() {
		this(0, null, null, null, 0L, 0L, null, null, 0.0);
	}
	
	public TransactionJournal(long journalId, DeviceHistory deviceHistory, TransactionType transactionType, Instant timestamp, 
			long transactionId, long childId, String description, Instant date, double amount) {
		this.key = new Key();
		key.journalId = journalId;
		setDeviceHistory(deviceHistory);
		this.transactionType = transactionType;
		setTimestamp(timestamp);
		this.transactionId = transactionId;
		this.childId = childId;
		this.description = description;
		setDate(date);
		this.amount = amount;
	}
	
	public long getJournalId() {
		return key.journalId;
	}
	public void setKey(long journalId) {
		this.key.journalId = journalId;
	}
	public TransactionType getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}
	public Instant getTimestamp() {		
		return timestamp == null ? null : Instant.parse(timestamp, DATE_TIME_FORMAT);
	}
	public void setTimestamp(@Nullable Instant timestamp) {
		if (timestamp == null) {
			this.timestamp = null;
		} else {
			this.timestamp = timestamp.toString(DATE_TIME_FORMAT);
		}
	}
	public long getChildId() {
		return childId;
	}
	public void setChildId(long childId) {
		this.childId = childId;
	}

	public DeviceHistory getDeviceHistory() {
		return deviceHistory;
	}

	public void setDeviceHistory(@Nullable DeviceHistory deviceHistory) {
		this.deviceHistory = deviceHistory;
		this.key.deviceId = (deviceHistory == null) ? null : deviceHistory.getDeviceId();
	}
	
	public static TransactionJournal find(EntityManager em, long journalId, DeviceHistory deviceHistory) {
		Key key = new Key(journalId, deviceHistory.getDeviceId());
		return em.find(TransactionJournal.class, key);
	}
	
	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Embeddable
	private static class Key implements Serializable {
		private static final long serialVersionUID = 1L;

		long journalId;
		String deviceId;
		
		public Key() {
			this(0L, null);
		}

		public Key(long journalId, String deviceId) {
			this.journalId = journalId;
			this.deviceId = deviceId;
		}
	}
}

