package org.jmicco.parentbank.parentdb;

import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.joda.time.Instant;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

@Entity(name = "child")
@Table(name = "child", schema = "parentdb")
@NamedQueries( {
	@NamedQuery(name = "Child.FindAllChildren", 
		query = "SELECT c FROM child c WHERE c.key.deviceId = :deviceId"),
	@NamedQuery(name = "Child.FindNamedChild",
		query = "SELECT c FROM child c where c.key.deviceId = :deviceId and c.name = :name")
})
@EqualsAndHashCode
@ToString
public class Child {
	@EmbeddedId
	private Key key;
	
	@Getter @Setter String name;
	
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

	@VisibleForTesting
	void setChildId(long childId) {
		key.childId = childId;
	}
	
	@EqualsAndHashCode
	@ToString
	@Embeddable
	static class Key {
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
	
	public static Optional<Child> findByName(EntityManager em, String name, DeviceHistory deviceHistory) {
		TypedQuery<Child> query = em.createNamedQuery("Child.FindNamedChild", Child.class);
		query.setParameter("deviceId", deviceHistory.getDeviceId());
		query.setParameter("name", name);
		List<Child> resultList = query.getResultList();
		return resultList.size() == 0 ? Optional.<Child>absent() : Optional.of(resultList.get(0));
	}
	
	public long persist(EntityManager em, DeviceHistory deviceHistory) {
		em.persist(this);
		em.flush();
		long journalId = Sequence.generateId(em);
		ChildJournal journalEntry = new ChildJournal(journalId, deviceHistory, TransactionType.CREATE, new Instant(), getChildId(), name);
		em.persist(journalEntry);
		em.flush();
		return journalEntry.getJournalId();
	}
	
	public void merge(EntityManager em, DeviceHistory deviceHistory) {
		em.merge(this);
		long journalId = Sequence.generateId(em);
		ChildJournal journalEntry = new ChildJournal(journalId, deviceHistory, TransactionType.UPDATE, new Instant(), getChildId(), name);
		em.persist(journalEntry);		
	}
	
	public void delete(EntityManager em, DeviceHistory deviceHistory) {
		em.detach(this);
		long journalId = Sequence.generateId(em);
		ChildJournal journalEntry = new ChildJournal(journalId, deviceHistory, TransactionType.DELETE, new Instant(), getChildId(), name);
		em.persist(journalEntry);
	}

	public List<Transaction> getTransactions(EntityManager em) {
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.FindAllTransactions", Transaction.class);
		query.setParameter("deviceId", getDeviceId());
		query.setParameter("childId", getChildId());
		return query.getResultList();
	}

}
