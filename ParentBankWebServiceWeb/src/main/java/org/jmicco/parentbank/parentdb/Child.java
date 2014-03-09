package org.jmicco.parentbank.parentdb;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;

import org.joda.time.Instant;

@Entity(name = "child")
@Table(name = "child", schema = "parentdb")
public class Child {
	@EmbeddedId
	private Key key;
	
	String name;
	
	public Child() {
		this("", "");
	}
	
	public Child(String deviceId, String name) {
		this.key = new Key();
		this.key.deviceId = deviceId;
		this.name = name;
	}
	public long getChildId() {
		return key.childId;
	}
	
	public String getDeviceId() {
		return key.deviceId;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Embeddable
	private static class Key {
		@GeneratedValue(strategy=GenerationType.IDENTITY)
		private long childId;
		private String deviceId;
		
		private Key() {
			this(0L, null);
		}
		
		private Key(long childId, String deviceId) {
			this.childId = childId;
			this.deviceId = deviceId;
		}		
	}
	
	public static Child find(EntityManager em, long childId, DeviceHistory deviceHistory) {
		Key key = new Key(childId, deviceHistory.getDeviceId());
		return em.find(Child.class, key);
	}
	
	public void persist(EntityManager em, DeviceHistory deviceHistory) {
		em.persist(this);
		em.flush();
		long journalId = Sequence.generateId(em);
		ChildJournal journalEntry = new ChildJournal(journalId, deviceHistory, TransactionType.CREATE, new Instant(), getChildId(), getName());
		em.persist(journalEntry);
	}
	
	public void merge(EntityManager em, DeviceHistory deviceHistory) {
		em.merge(this);
		long journalId = Sequence.generateId(em);
		ChildJournal journalEntry = new ChildJournal(journalId, deviceHistory, TransactionType.UPDATE, new Instant(), getChildId(), getName());
		em.persist(journalEntry);		
	}
	
	public void delete(EntityManager em, DeviceHistory deviceHistory) {
		em.detach(this);
		long journalId = Sequence.generateId(em);
		ChildJournal journalEntry = new ChildJournal(journalId, deviceHistory, TransactionType.DELETE, new Instant(), getChildId(), getName());
		em.persist(journalEntry);
	}
}