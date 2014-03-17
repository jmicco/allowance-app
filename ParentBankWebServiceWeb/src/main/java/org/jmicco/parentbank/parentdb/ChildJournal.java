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

import com.sun.istack.Nullable;

@Entity(name = "child_journal")
@Table(name = "child_journal", schema = "parentdb")
@NamedQueries( {
	@NamedQuery(name = "ChildJournal.FindNewJournalEntries", 
		query = "SELECT c FROM child_journal c WHERE c.key.deviceId = :deviceId AND c.key.journalId > :journalId"),
	@NamedQuery(name = "ChildJournal.FindAllJournalEntries",
		query = "SELECT c from child_journal c where c.key.deviceId = :deviceId"),
	@NamedQuery(name = "ChildJournal.FindMasterPushJournalEntries",
		query = "SELECT c from child_journal c where c.key.deviceId = :deviceId and c.key.journalId > :masterPushJournalId and c.key.journalId <= :pushJournalId")
})
@EqualsAndHashCode
@ToString
public class ChildJournal {
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZZ");

	@EmbeddedId
	private Key key;
	
	@ManyToOne
	@JoinColumn(name = "deviceId", insertable = false, updatable = false)
	@Getter private DeviceHistory deviceHistory;
	
	@Enumerated(EnumType.ORDINAL)
	@Getter @Setter private TransactionType transactionType;
	
	private String timestamp;
	@Getter @Setter private long childId;
	@Getter @Setter private String name;
	
	public ChildJournal() {
		this(0, null, null, null, 0L, null);
	}
	
	public ChildJournal(long journalId, DeviceHistory deviceHistory, TransactionType transactionType, Instant timestamp, 
			long childId, String name) {
		this.key = new Key();
		key.journalId = journalId;
		setDeviceHistory(deviceHistory);
		this.transactionType = transactionType;
		setTimestamp(timestamp);
		this.childId = childId;
		this.name = name;		
	}
	
	public long getJournalId() {
		return key.journalId;
	}
	
	public void setJournalId(long childJournalId) {
		this.key.journalId = childJournalId;
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

	public void setDeviceHistory(@Nullable DeviceHistory deviceHistory) {
		this.deviceHistory = deviceHistory;
		this.key.deviceId = (deviceHistory == null) ? null : deviceHistory.getDeviceId();
	}
	
	public static ChildJournal find(EntityManager em, long journalId, DeviceHistory deviceHistory) {
		Key key = new Key(journalId, deviceHistory.getDeviceId());
		return em.find(ChildJournal.class, key);
	}
	
	@Embeddable
	@EqualsAndHashCode
	@ToString
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
