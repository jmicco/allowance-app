package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TransactionJournalTest {
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
		TransactionJournal expectedTransactionJournal = new TransactionJournal(1234L, deviceHistory, TransactionType.CREATE, new Instant(20000L), 
				1000L, 1010L, "description", new Instant(1000L), 10.50);
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(expectedTransactionJournal);
		tx.commit();
		
		TransactionJournal actualTransactionJournal = TransactionJournal.find(em, 1234L, deviceHistory);
		assertEquals(expectedTransactionJournal.getJournalId(), actualTransactionJournal.getJournalId());
		assertEquals(expectedTransactionJournal.getTimestamp(), actualTransactionJournal.getTimestamp());
		assertEquals(expectedTransactionJournal.getTransactionType(), actualTransactionJournal.getTransactionType());
		assertEquals(expectedTransactionJournal.getChildId(), actualTransactionJournal.getChildId());
		assertEquals(expectedTransactionJournal.getDeviceHistory(), actualTransactionJournal.getDeviceHistory());
		assertEquals(expectedTransactionJournal.getAmount(), actualTransactionJournal.getAmount(), 0.0);
		assertEquals(expectedTransactionJournal.getDescription(), actualTransactionJournal.getDescription());
	}
}
