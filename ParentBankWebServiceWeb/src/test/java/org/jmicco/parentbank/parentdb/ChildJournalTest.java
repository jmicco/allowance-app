package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChildJournalTest {
	private TestDatabaseHelper helper;
	private EntityManager em;
	private DeviceHistory deviceHistory;
	
	@Before
	public void setUp() throws Exception {
		helper = new TestDatabaseHelper();
		em = helper.getEm();
		
		Group group = new Group();
		group.setMasterId("master1234");
		deviceHistory = new DeviceHistory("device1234", group, "nobody@nowhere.com", 0L, 0L, 0L, 0L);
		
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
		ChildJournal expectedChildJournal = new ChildJournal(1234L, deviceHistory, TransactionType.CREATE, new Instant(20000L), 
				1000L, "childname");
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(expectedChildJournal);
		tx.commit();
		
		ChildJournal actualChildJournal = ChildJournal.find(em, 1234L, deviceHistory);
		assertEquals(expectedChildJournal.getChildJournalId(), actualChildJournal.getChildJournalId());
		assertEquals(expectedChildJournal.getTimestamp(), actualChildJournal.getTimestamp());
		assertEquals(expectedChildJournal.getTransactionType(), actualChildJournal.getTransactionType());
		assertEquals(expectedChildJournal.getChildId(), actualChildJournal.getChildId());
		assertEquals(expectedChildJournal.getName(), actualChildJournal.getName());
		assertEquals(expectedChildJournal.getDeviceHistory(), actualChildJournal.getDeviceHistory());
	}
}
