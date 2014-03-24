package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChildTest {
	private TestDatabaseHelper helper;
	private EntityManager em;
	private DeviceHistory deviceHistory;
	
	@Before
	public void setUp() throws Exception {
		helper = new TestDatabaseHelper();
		em = helper.getEm();
		
		Group group = new Group();
		group.setMasterId("master1234");
		deviceHistory = new DeviceHistory("master1234", group, "nobody@nowhere.com", 0L, 0L, 0L, 0L, 0L, 0L);
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(group);
		em.persist(deviceHistory);
		tx.commit();
	}

	@After
	public void tearDown() throws Exception {
		helper.close();
		helper = null;
		em = null;
	}

	@Test
	public void testChildJournal() {
		
		Child expectedChild = new Child(deviceHistory.getDeviceId(), "Angela");

		EntityTransaction tx = em.getTransaction();
		tx.begin();
		expectedChild.persist(em, deviceHistory);
		new Child(deviceHistory.getDeviceId(), "Emily").persist(em, deviceHistory);
		tx.commit();
		
		Child actualChild = Child.find(em, expectedChild.getChildId(), deviceHistory);
		assertNotNull(actualChild);
		assertEquals(expectedChild.getChildId(), actualChild.getChildId());
		assertEquals(expectedChild.getDeviceId(), actualChild.getDeviceId());
		assertEquals(expectedChild.getName(), actualChild.getName());
		
		TypedQuery<ChildJournal> query = em.createNamedQuery("ChildJournal.FindNewJournalEntries", ChildJournal.class);
		query.setParameter("journalId", 0L);
		query.setParameter("deviceId", actualChild.getDeviceId());
		List<ChildJournal> resultList = query.getResultList();
		
		assertEquals(2, resultList.size());
		ChildJournal actualJournal = resultList.get(0);
		
		assertEquals(deviceHistory, actualJournal.getDeviceHistory());
		assertEquals(actualChild.getChildId(), actualJournal.getChildId());
		assertEquals(actualChild.getName(), actualJournal.getName());
		assertEquals(TransactionType.CREATE, actualJournal.getTransactionType());
		
		expectedChild.setName("Amanda");
		tx.begin();
		expectedChild.merge(em, deviceHistory);
		tx.commit();
		
		query.setParameter("journalId", 2L);
		resultList = query.getResultList();
		assertEquals(1, resultList.size());
		
		actualJournal = resultList.get(0);
		assertEquals(TransactionType.UPDATE, actualJournal.getTransactionType());
		
		tx.begin();
		expectedChild.delete(em, deviceHistory);
		tx.commit();
		
		query.setParameter("journalId", 3L);
		resultList = query.getResultList();
		assertEquals(1, resultList.size());
		
		actualJournal = resultList.get(0);
		assertEquals(TransactionType.DELETE, actualJournal.getTransactionType());
	}
}
