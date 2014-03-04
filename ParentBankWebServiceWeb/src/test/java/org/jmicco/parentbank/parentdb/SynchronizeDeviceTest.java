package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.List;

import javax.persistence.EntityManager;

import org.jmicco.parentbank.web.ChildJournalEntry;
import org.jmicco.parentbank.web.TransactionJournalEntry;
import org.joda.time.DateTimeField;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SynchronizeDeviceTest {
	private TestDatabaseHelper helper;
	private EntityManager em;
	private SynchronizeDevice synchronizer;
	
	@Before
	public void setUp() throws Exception {
		helper = new TestDatabaseHelper();
		em = helper.getEm();
		synchronizer = new SynchronizeDevice(em);
	}

	@After
	public void tearDown() throws Exception {
		helper.close();
		helper = null;
		em = null;
		synchronizer = null;
	}

	@Test
	public void testfindOrCreateDeviceHistoryNew() {
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		assertNotNull(deviceHistory);
		assertEquals("nobody@nowhere.com", deviceHistory.getEmail());
		assertEquals("device1234", deviceHistory.getDeviceId());
		assertNotNull(deviceHistory.getGroup().getMasterId());
		assertEquals(0L, deviceHistory.getHwmChildPull());
		assertEquals(0L, deviceHistory.getHwmChildPush());
		assertEquals(0L, deviceHistory.getHwmTransPull());
		assertEquals(0L, deviceHistory.getHwmTransPush());
	}
	
	@Test
	public void testfindOrCreateDeviceHistoryExists() {
		DeviceHistory expectedDeviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		DeviceHistory actualDeviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		
		assertSame(expectedDeviceHistory, actualDeviceHistory);
	}
	
	@Test
	public void testfindOrCreateDeviceHistorySameEmail() {
		DeviceHistory deviceHistory1234 = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		DeviceHistory deviceHistory1235 = synchronizer.findOrCreateDeviceHistory("device1235", "nobody@nowhere.com");
		
		assertNotSame(deviceHistory1234, deviceHistory1235);
		assertSame(deviceHistory1234.getGroup(), deviceHistory1235.getGroup());
	}
	
	@Test
	public void testfindOrCreateDeviceHistoryDifferentEmail() {
		DeviceHistory deviceHistory1234 = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		DeviceHistory deviceHistory1235 = synchronizer.findOrCreateDeviceHistory("device1235", "noone@nowhere.com");
		
		assertNotSame(deviceHistory1234, deviceHistory1235);
		assertNotSame(deviceHistory1234.getGroup(), deviceHistory1235.getGroup());
	}
	
	@Test
	public void testMirrorChildJournalEntries() {
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		ChildJournalEntry childJournalEntry = new ChildJournalEntry(1L, TransactionType.CREATE, 1000L, 1, "childname");
		List<ChildJournalEntry> childJournal = ImmutableList.of(childJournalEntry);
		assertEquals(0L, deviceHistory.getHwmChildPush());
		
		synchronizer.mirrorChildJournalEntries(deviceHistory, 1L, childJournal);
		ChildJournal journal = ChildJournal.find(em, 1L, deviceHistory);
		assertNotNull(journal);
		
		
		assertEquals(childJournalEntry.getChildId(), journal.getChildId());
		assertEquals(childJournalEntry.getJournalId(), journal.getChildJournalId());
		assertEquals(new Instant(childJournalEntry.getTimestampMillis()), journal.getTimestamp());
		assertEquals(childJournalEntry.getName(), journal.getName());
		assertEquals(childJournalEntry.getTransactionType(), journal.getTransactionType());
		assertEquals(deviceHistory, journal.getDeviceHistory());
		
		assertEquals(1L, deviceHistory.getHwmChildPush());
	}
	
	@Test
	public void testMirrorTransactionJournalEntries() {
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		TransactionJournalEntry transactionJournalEntry = new TransactionJournalEntry(1L, TransactionType.CREATE, 1000L, 1L, 2L, "description", 0L, 10.50);
		List<TransactionJournalEntry> transactionJournal = ImmutableList.of(transactionJournalEntry);
		assertEquals(0L, deviceHistory.getHwmChildPush());
		
		synchronizer.mirrorTransactionJournalEntries(deviceHistory, 1L, transactionJournal);
		TransactionJournal journal = TransactionJournal.find(em, 1L, deviceHistory);
		assertNotNull(journal);
		
		assertEquals(transactionJournalEntry.getChildId(), journal.getChildId());
		assertEquals(transactionJournalEntry.getJournalId(), journal.getJournalId());
		assertEquals(new Instant(transactionJournalEntry.getTimestampMillis()), journal.getTimestamp());
		assertEquals(transactionJournalEntry.getTransactionType(), journal.getTransactionType());
		assertEquals(transactionJournalEntry.getDescription(), journal.getDescription());
		assertEquals(transactionJournalEntry.getAmount(), journal.getAmount(), 0.0);
		// TODO: Figure out how to test round-trip dates to / from TransactionJournal date	
		TransactionJournal entry = new TransactionJournal();
		entry.setDate(new Instant(transactionJournalEntry.getDateMillis()));		
		assertEquals(entry.getDate(), journal.getDate());
		assertEquals(deviceHistory, journal.getDeviceHistory());
		
		assertEquals(1L, deviceHistory.getHwmTransPush());
	}


}
