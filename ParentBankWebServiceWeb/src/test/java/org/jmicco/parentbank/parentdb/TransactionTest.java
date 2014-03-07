package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TransactionTest {
	private TestDatabaseHelper helper;
	private EntityManager em;
	private DeviceHistory deviceHistory;
	private Child child1;
	private Child child2;
	
	@Before
	public void setUp() throws Exception {
		helper = new TestDatabaseHelper();
		em = helper.getEm();
		
		Group group = new Group();
		group.setMasterId("master1234");
		deviceHistory = new DeviceHistory("master1234", group, "nobody@nowhere.com", 0L, 0L, 0L, 0L);
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(group);
		em.persist(deviceHistory);
		
		child1 = new Child(deviceHistory.getDeviceId(), "Emily");
		child2 = new Child(deviceHistory.getDeviceId(), "Angela");
		
		child1.persist(em, deviceHistory);
		child2.persist(em, deviceHistory);
		
		tx.commit();
	}

	@After
	public void tearDown() throws Exception {
		helper.close();
		helper = null;
		em = null;
	}

	@Test
	public void testTransactionJournal() {
		
		Transaction transaction1 = new Transaction(deviceHistory.getDeviceId(), child1.getChildId(), new Instant(), "description1", 10.0);
		Transaction transaction2 = new Transaction(deviceHistory.getDeviceId(), child1.getChildId(), new Instant(), "description2", 12.0);
		Transaction transaction3 = new Transaction(deviceHistory.getDeviceId(), child2.getChildId(), new Instant(), "description3", 14.0);
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		transaction1.persist(em, deviceHistory);
		transaction2.persist(em, deviceHistory);
		transaction3.persist(em, deviceHistory);
		
		transaction3.setAmount(28.0);
		transaction3.merge(em, deviceHistory);
				
		tx.commit();
		
		Transaction actualTransaction1 = Transaction.find(em, transaction1.getTransactionId() , deviceHistory);
		Transaction actualTransaction2 = Transaction.find(em, transaction2.getTransactionId() , deviceHistory);
		Transaction actualTransaction3 = Transaction.find(em, transaction3.getTransactionId() , deviceHistory);
		
		assertNotNull(actualTransaction1);
		assertNotNull(actualTransaction2);
		assertNotNull(actualTransaction3);
		
		TypedQuery<TransactionJournal> query = em.createNamedQuery("TransactionJournal.FindNewJournalEntries", TransactionJournal.class);
		query.setParameter("deviceId", deviceHistory.getDeviceId());
		query.setParameter("journalId", 0);
		List<TransactionJournal> resultList = query.getResultList();
		
		assertEquals(4, resultList.size());
		
//		Child actualChild = Child.find(em, expectedChild.getChildId(), deviceHistory);
//		assertNotNull(actualChild);
//		assertEquals(expectedChild.getChildId(), actualChild.getChildId());
//		assertEquals(expectedChild.getDeviceId(), actualChild.getDeviceId());
//		assertEquals(expectedChild.getName(), actualChild.getName());
//		
//		TypedQuery<ChildJournal> query = em.createNamedQuery("ChildJournal.FindNewJournalEntries", ChildJournal.class);
//		query.setParameter("journalId", 0L);
//		query.setParameter("deviceId", actualChild.getDeviceId());
//		List<ChildJournal> resultList = query.getResultList();
//		
//		assertEquals(2, resultList.size());
//		ChildJournal actualJournal = resultList.get(0);
//		
//		assertEquals(deviceHistory, actualJournal.getDeviceHistory());
//		assertEquals(actualChild.getChildId(), actualJournal.getChildId());
//		assertEquals(actualChild.getName(), actualJournal.getName());
//		assertEquals(TransactionType.CREATE, actualJournal.getTransactionType());
//		
//		expectedChild.setName("Amanda");
//		tx.begin();
//		expectedChild.merge(em, deviceHistory);
//		tx.commit();
//		
//		query.setParameter("journalId", 2L);
//		resultList = query.getResultList();
//		assertEquals(1, resultList.size());
//		
//		actualJournal = resultList.get(0);
//		assertEquals(TransactionType.UPDATE, actualJournal.getTransactionType());
//		
//		tx.begin();
//		expectedChild.delete(em, deviceHistory);
//		tx.commit();
//		
//		query.setParameter("journalId", 3L);
//		resultList = query.getResultList();
//		assertEquals(1, resultList.size());
//		
//		actualJournal = resultList.get(0);
//		assertEquals(TransactionType.DELETE, actualJournal.getTransactionType());
	}
}
