package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jmicco.parentbank.web.ChildJournalEntry;
import org.jmicco.parentbank.web.ClientPullResponse;
import org.jmicco.parentbank.web.ClientPushPullRequest;
import org.jmicco.parentbank.web.ClientPushResponse;
import org.jmicco.parentbank.web.TransactionJournalEntry;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SynchronizeDeviceTest {
	private static final long TIMESTAMP = 10000L;
	private TestDatabaseHelper helper;
	private EntityManager em;
	private SynchronizeDevice synchronizer;
	private EntityTransaction tx;
	
	@Before
	public void setUp() throws Exception {
		helper = new TestDatabaseHelper();
		em = helper.getEm();
		synchronizer = new SynchronizeDevice(em);
		tx = em.getTransaction();
	}

	@After
	public void tearDown() throws Exception {
		assertFalse(tx.isActive());
		helper.close();
		helper = null;
		em = null;
		synchronizer = null;
	}

	@Test
	public void testfindOrCreateDeviceHistoryNew() {
		tx.begin();
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		tx.commit();
		assertNotNull(deviceHistory);
		assertEquals("nobody@nowhere.com", deviceHistory.getEmail());
		assertEquals("device1234", deviceHistory.getDeviceId());
		assertNotNull(deviceHistory.getGroup().getMasterId());
		assertEquals(0L, deviceHistory.getHwmChildPull());
		assertEquals(0L, deviceHistory.getHwmChildPush());
		assertEquals(0L, deviceHistory.getHwmTransPull());
		assertEquals(0L, deviceHistory.getHwmTransPush());
		assertNotNull(em.find(DeviceHistory.class, deviceHistory.getGroup().getMasterId()));  // Make sure the sharing group has a device history
	}
	
	@Test
	public void testfindOrCreateDeviceHistoryExists() {
		tx.begin();
		DeviceHistory expectedDeviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		tx.commit();
		tx.begin();
		DeviceHistory actualDeviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		tx.commit();
		assertSame(expectedDeviceHistory, actualDeviceHistory);
	}
	
	@Test
	public void testfindOrCreateDeviceHistorySameEmail() {
		tx.begin();
		DeviceHistory deviceHistory1234 = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		tx.commit();
		tx.begin();
		DeviceHistory deviceHistory1235 = synchronizer.findOrCreateDeviceHistory("device1235", "nobody@nowhere.com");
		tx.commit();
		
		assertNotSame(deviceHistory1234, deviceHistory1235);
		assertSame(deviceHistory1234.getGroup(), deviceHistory1235.getGroup());
	}
	
	@Test
	public void testfindOrCreateDeviceHistoryDifferentEmail() {
		tx.begin();
		DeviceHistory deviceHistory1234 = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		tx.commit();
		tx.begin();
		DeviceHistory deviceHistory1235 = synchronizer.findOrCreateDeviceHistory("device1235", "noone@nowhere.com");
		tx.commit();

		assertNotSame(deviceHistory1234, deviceHistory1235);
		assertNotSame(deviceHistory1234.getGroup(), deviceHistory1235.getGroup());
	}
	
	@Test
	public void testMirrorChildJournalEntries() {
		tx.begin();
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");		
		ChildJournalEntry childJournalEntry = new ChildJournalEntry(1L, TransactionType.CREATE, 1000L, 1, "childname");
		List<ChildJournalEntry> childJournal = ImmutableList.of(childJournalEntry);
		assertEquals(0L, deviceHistory.getHwmChildPush());
		
		synchronizer.mirrorChildJournalEntries(
				deviceHistory, 1L, childJournal);
		ChildJournal journal = ChildJournal.find(em, 1L, deviceHistory);
		tx.commit();
		
		assertNotNull(journal);
		
		
		assertEquals(childJournalEntry.getChildId(), journal.getChildId());
		assertEquals(childJournalEntry.getJournalId(), journal.getJournalId());
		assertEquals(new Instant(childJournalEntry.getTimestampMillis()), journal.getTimestamp());
		assertEquals(childJournalEntry.getName(), journal.getName());
		assertEquals(childJournalEntry.getTransactionType(), journal.getTransactionType());
		assertEquals(deviceHistory, journal.getDeviceHistory());
		
		assertEquals(1L, deviceHistory.getHwmChildPush());
	}
	
	@Test
	public void testMirrorTransactionJournalEntries() {
		tx.begin();
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		TransactionJournalEntry transactionJournalEntry = new TransactionJournalEntry(1L, TransactionType.CREATE, 1000L, 1L, 2L, "description", 0L, 10.50);
		List<TransactionJournalEntry> transactionJournal = ImmutableList.of(transactionJournalEntry);
		assertEquals(0L, deviceHistory.getHwmChildPush());
		
		synchronizer.mirrorTransactionJournalEntries(
				deviceHistory, 1L, transactionJournal);
		tx.commit();
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
	
	@Test
	public void testPush() {
		tx.begin();
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		DeviceHistory masterHistory = em.find(DeviceHistory.class, deviceHistory.getGroup().getMasterId());
		
		Child child1 = new Child(masterHistory.getDeviceId(), "child1");
		child1.persist(em, masterHistory);	
		Child child2 = new Child(masterHistory.getDeviceId(), "child2");
		child2.persist(em, masterHistory);
		em.flush();
		Transaction transactionChild1 = new Transaction(masterHistory.getDeviceId(), child1.getChildId(), new Instant(1000L), "child1 MT1", 10.0);
		Transaction transactionChild2 = new Transaction(masterHistory.getDeviceId(), child2.getChildId(), new Instant(1000L), "child2 MT1", 5.0);
		transactionChild1.persist(em, masterHistory);
		transactionChild2.persist(em, masterHistory);
		tx.commit();
		
		List<ChildJournalEntry> childJournal = ImmutableList.of(
				new ChildJournalEntry(1L, TransactionType.CREATE, TIMESTAMP, 1L, "child2"),
				new ChildJournalEntry(2L, TransactionType.CREATE, TIMESTAMP, 2L, "child3"));
		
		List<TransactionJournalEntry> transactionJournal = ImmutableList.of(
				new TransactionJournalEntry(1L, TransactionType.CREATE, TIMESTAMP, 1L, 1L, "child2 CT1", 20000L, 3.0),
				new TransactionJournalEntry(2L, TransactionType.CREATE, TIMESTAMP, 2L, 2L, "child3 CT1", 20000L, 6.0)
		);
		ClientPushPullRequest request = 
				new ClientPushPullRequest(deviceHistory.getDeviceId(), "nobody@nowhere.com", 0L, 2L, 0L, 2L, childJournal, transactionJournal);
		ClientPushResponse response = synchronizer.push(request);
		assertEquals(2, response.getHwmChildPull());
		assertEquals(4, response.getHwmTransPull());
		ChildJournalEntry actualChildJournal = response.getChildJournal().get(0);
		assertEquals(new ChildJournalEntry(1, TransactionType.CREATE, actualChildJournal.getTimestampMillis(), 1, "child1"), actualChildJournal);
		actualChildJournal = response.getChildJournal().get(1);
		assertEquals(new ChildJournalEntry(2, TransactionType.CREATE, actualChildJournal.getTimestampMillis(), 2, "child2"), actualChildJournal);
		TransactionJournalEntry actualTransactionJournal = response.getTransactionJournal().get(0);
		assertEquals(new TransactionJournalEntry(3, TransactionType.CREATE, actualTransactionJournal.getTimestampMillis(), 1, 
				child1.getChildId(), "child1 MT1", transactionChild1.getDate().getMillis(), 10.0), actualTransactionJournal);
		actualTransactionJournal = response.getTransactionJournal().get(1);
		assertEquals(new TransactionJournalEntry(4, TransactionType.CREATE, actualTransactionJournal.getTimestampMillis(), 2, 
				child2.getChildId(), "child2 MT1", transactionChild1.getDate().getMillis(), 5.0), actualTransactionJournal);
		
		ChildJournal deviceChildJournal = ChildJournal.find(em, 1L, deviceHistory);
		assertNotNull(deviceChildJournal);
		assertEquals(new ChildJournal(1L, deviceHistory, TransactionType.CREATE, new Instant(TIMESTAMP), 1L, "child2"), 
				deviceChildJournal);
		deviceChildJournal = ChildJournal.find(em, 2L, deviceHistory);
		assertEquals(new ChildJournal(2L, deviceHistory, TransactionType.CREATE, new Instant(TIMESTAMP), 2L, "child3"), 
				deviceChildJournal);
		
		TransactionJournal deviceTransJournal = TransactionJournal.find(em, 1L, deviceHistory);
		assertEquals(new TransactionJournal(1L, deviceHistory, TransactionType.CREATE, new Instant(TIMESTAMP), 1L, 
				1L, "child2 CT1", deviceTransJournal.getDate(), 3.0), deviceTransJournal);
		deviceTransJournal = TransactionJournal.find(em, 2L, deviceHistory);
		assertEquals(new TransactionJournal(2L, deviceHistory, TransactionType.CREATE, new Instant(TIMESTAMP), 2L, 
				2L, "child3 CT1", deviceTransJournal.getDate(), 6.0), deviceTransJournal);
	}
	
	@Test
	public void testPushPastHwm() {
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory("device1234", "nobody@nowhere.com");
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		deviceHistory.setHwmChildPush(2L);
		deviceHistory.setHwmTransPush(2L);
		tx.commit();
		
		List<ChildJournalEntry> childJournal = ImmutableList.of(
				new ChildJournalEntry(1L, TransactionType.CREATE, TIMESTAMP, 1L, "child2"),
				new ChildJournalEntry(2L, TransactionType.CREATE, TIMESTAMP, 2L, "child3"));
		
		List<TransactionJournalEntry> transactionJournal = ImmutableList.of(
				new TransactionJournalEntry(1L, TransactionType.CREATE, TIMESTAMP, 1L, 1L, "child2 CT1", 20000L, 3.0),
				new TransactionJournalEntry(2L, TransactionType.CREATE, TIMESTAMP, 2L, 2L, "child3 CT1", 20000L, 6.0)
		);
		ClientPushPullRequest request = 
				new ClientPushPullRequest(deviceHistory.getDeviceId(), "nobody@nowhere.com", 0L, 2L, 0L, 2L, childJournal, transactionJournal);
		ClientPushResponse response = synchronizer.push(request);
		assertEquals(0L, response.getHwmChildPull());
		assertEquals(0L, response.getHwmTransPull());
		assertEquals(0L, response.getChildJournal().size());
		
		ChildJournal deviceChildJournal = ChildJournal.find(em, 1L, deviceHistory);
		assertNull(deviceChildJournal);
		deviceChildJournal = ChildJournal.find(em, 1L, deviceHistory);
		assertNull(deviceChildJournal);
		
		TransactionJournal deviceTransJournal = TransactionJournal.find(em, 1L, deviceHistory);
		assertNull(deviceTransJournal);
		deviceTransJournal = TransactionJournal.find(em, 2L, deviceHistory);
		assertNull(deviceTransJournal);
	}
	private static String DEVICE1 = UUID.randomUUID().toString();
	private static String DEVICE2 = UUID.randomUUID().toString();
	private static Child CHILD1 = new Child(DEVICE1, "child1");
	private static Child CHILD2 = new Child(DEVICE1, "child2");
	private static Child CHILD3 = new Child(DEVICE1, "child2");
	private static String EMAIL = "nobody@nowhere.com";
	
	private static ImmutableMultimap<Child, Transaction> SCENARIO1 = ImmutableMultimap.of(
			CHILD2, new Transaction(DEVICE1, 0, new Instant(TIMESTAMP), "child2 CT1", 3.0),
			CHILD3, new Transaction(DEVICE1, 0, new Instant(TIMESTAMP), "child3 CT1", 6.0)			
			);
	
	@Test
	public void testPushPullCycle() {

		
		ClientPushPullRequest request = createPushRequest(SCENARIO1);
		
		ClientPushResponse response = synchronizer.push(request);
		
		assertEquals(0, response.getChildJournal().size());
		assertEquals(0, response.getTransactionJournal().size());
		
		request = createPullRequest(SCENARIO1, request);
		
//		ClientPullResponse pullResponse = synchronizer.pull(request);
//		assertEquals(2, pullResponse.getHwmChildPull());
//		assertEquals(2, pullResponse.getHwmTransPull());
//
//		verifyMaster(SCENARIO1);
		
	}

	private void verifyMaster(ImmutableMultimap<Child, Transaction> scenario) {
		String deviceId = scenario.keySet().iterator().next().getDeviceId();
		DeviceHistory deviceHistory = em.find(DeviceHistory.class, deviceId);
		assertNotNull(deviceHistory);
		DeviceHistory masterHistory = em.find(DeviceHistory.class, deviceHistory.getGroup().getMasterId());
		assertNotNull(masterHistory);
		Set<Child> children = Sets.newHashSet(masterHistory.getChildren(em));
		assertEquals(scenario.keySet(), children);
		for (Child child: children) {
			ImmutableCollection<Transaction> actualTransactions = ImmutableSet.copyOf(child.getTransactions(em));
			ImmutableCollection<Transaction> expectedTransactions = scenario.get(child);
			assertEquals(expectedTransactions, actualTransactions);
		}
	}

	private ClientPushPullRequest createPullRequest(ImmutableMultimap<Child, Transaction> scenario, ClientPushPullRequest pushRequest) {		
		return new ClientPushPullRequest(pushRequest.getDeviceId(), pushRequest.getEmail(),
				pushRequest.getHwmChildPull(),
				pushRequest.getHwmChildPush(), 
				pushRequest.getHwmTransPull(), 
				pushRequest.getHwmTransPush(), 
				ImmutableList.<ChildJournalEntry>of(), 
				ImmutableList.<TransactionJournalEntry>of());
	}

	private ClientPushPullRequest createPushRequest(ImmutableMultimap<Child, Transaction> scenario) {		
		List<ChildJournalEntry> childJournal = createChildJournal(SCENARIO1);		
		List<TransactionJournalEntry> transactionJournal = createTransactionJournal(SCENARIO1);
		
		String deviceId = scenario.keySet().iterator().next().getDeviceId();
		return new ClientPushPullRequest(deviceId, EMAIL, 
				0L, childJournal.size(), 
				0L, transactionJournal.size(), 
				childJournal, transactionJournal);
	}

	private List<TransactionJournalEntry> createTransactionJournal(ImmutableMultimap<Child, Transaction> scenario) {
		long journalKey = 1;
		long transactionKey = 1;
		List<TransactionJournalEntry> result = Lists.newArrayList();
		for (Entry<Child, Transaction> entry : scenario.entries()) {
			Transaction transaction = entry.getValue();
			Child child = entry.getKey();
			
			transaction.setTransactionId(transactionKey++);
			result.add(new TransactionJournalEntry(journalKey++, TransactionType.CREATE, TIMESTAMP, transaction.getTransactionId(), 
					child.getChildId(), transaction.getDescription(), transaction.getDate().getMillis(), transaction.getAmount()));			
		}
		return result;
	}

	private List<ChildJournalEntry> createChildJournal(ImmutableMultimap<Child, Transaction> scenario) {
		List<ChildJournalEntry> result = Lists.newArrayList();
		long childKey = 1;
		long journalKey = 1;
		for (Child child : scenario.keySet()) {
			child.setChildId(childKey++);
			result.add(new ChildJournalEntry(journalKey++, TransactionType.CREATE, TIMESTAMP, child.getChildId(), child.getName()));
		}
		return result;
	}
}
