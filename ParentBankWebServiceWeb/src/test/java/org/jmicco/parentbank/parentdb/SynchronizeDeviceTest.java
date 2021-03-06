package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import lombok.Getter;

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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SynchronizeDeviceTest {
	private static final long TIMESTAMP = 10000L;
	private TestDatabaseHelper helper;
	private EntityManager em;
	private SynchronizeDevice synchronizer;
	private EntityTransaction tx;
	
	private static String DEVICE1 = UUID.randomUUID().toString();
	private static String DEVICE2 = UUID.randomUUID().toString();
	private static String CHILD1 = "child1";
	private static String CHILD2 = "child2";
	private static String CHILD3 = "child3";
	private static Child DEV1_CHILD1 = new Child(DEVICE1, CHILD1);
	private static Child DEV2_CHILD1 = new Child(DEVICE2, CHILD1);
	private static Child DEV1_CHILD2 = new Child(DEVICE1, CHILD2);
	private static Child DEV2_CHILD2 = new Child(DEVICE2, CHILD2);
    private static Child DEV1_CHILD3 = new Child(DEVICE1, CHILD3);
	private static Child DEV2_CHILD3 = new Child(DEVICE2, CHILD3);
	private static String EMAIL = "nobody@nowhere.com";
	private static String EMAIL2 = "noone@nowhere.com";
	    
	private static Scenario SIMPLE_SCENARIO = new Scenario(
	        ImmutableMap.of(DEV1_CHILD1.getName(), DEV1_CHILD1),
            ImmutableMultimap.of(
                    DEV1_CHILD1.getName(), new Transaction(DEVICE1, 0, new Instant(TIMESTAMP), "dev1 child1 CT1", 1.0)
            ));
	
	private static Scenario SCENARIO1 = new Scenario(
	        ImmutableMap.of(DEV1_CHILD2.getName(), DEV1_CHILD2, DEV1_CHILD3.getName(), DEV1_CHILD3),
	        ImmutableMultimap.of(
	        DEV1_CHILD2.getName(), new Transaction(DEVICE1, 0, new Instant(TIMESTAMP), "dev1 child2 CT1", 3.0),
	        DEV1_CHILD3.getName(), new Transaction(DEVICE1, 0, new Instant(TIMESTAMP), "dev1 child3 CT1", 6.0)          
	        ));
	    
	private static Scenario SCENARIO2 = new Scenario(
	        ImmutableMap.of(DEV2_CHILD1.getName(), DEV2_CHILD1, DEV2_CHILD2.getName(), DEV2_CHILD2),
	        ImmutableMultimap.of(
	                DEV2_CHILD1.getName(), new Transaction(DEVICE2, 0, new Instant(TIMESTAMP), "dev2 child1 CT1", 9.0),
	                DEV2_CHILD2.getName(), new Transaction(DEVICE2, 0, new Instant(TIMESTAMP), "dev2 child2 CT1", 12.0)         
	        ));
	
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
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory(DEVICE1, EMAIL);
		tx.commit();
		assertNotNull(deviceHistory);
		assertEquals(EMAIL, deviceHistory.getEmail());
		assertEquals(DEVICE1, deviceHistory.getDeviceId());
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
		DeviceHistory expectedDeviceHistory = synchronizer.findOrCreateDeviceHistory(DEVICE1, EMAIL);
		tx.commit();
		tx.begin();
		DeviceHistory actualDeviceHistory = synchronizer.findOrCreateDeviceHistory(DEVICE1, EMAIL);
		tx.commit();
		assertSame(expectedDeviceHistory, actualDeviceHistory);
	}
	
	@Test
	public void testfindOrCreateDeviceHistorySameEmail() {
		tx.begin();
		DeviceHistory deviceHistory1234 = synchronizer.findOrCreateDeviceHistory(DEVICE1, EMAIL);
		tx.commit();
		tx.begin();
		DeviceHistory deviceHistory1235 = synchronizer.findOrCreateDeviceHistory(DEVICE2, EMAIL);
		tx.commit();
		
		assertNotSame(deviceHistory1234, deviceHistory1235);
		assertSame(deviceHistory1234.getGroup(), deviceHistory1235.getGroup());
	}
	
	@Test
	public void testfindOrCreateDeviceHistoryDifferentEmail() {
		tx.begin();
		DeviceHistory deviceHistory1234 = synchronizer.findOrCreateDeviceHistory(DEVICE1, EMAIL);
		tx.commit();
		tx.begin();
		DeviceHistory deviceHistory1235 = synchronizer.findOrCreateDeviceHistory(DEVICE2, EMAIL2);
		tx.commit();

		assertNotSame(deviceHistory1234, deviceHistory1235);
		assertNotSame(deviceHistory1234.getGroup(), deviceHistory1235.getGroup());
	}
	
	@Test
	public void testMirrorChildJournalEntries() {
	    ClientPushPullRequest request = SIMPLE_SCENARIO.createPushRequest();
	    synchronizer.push(request);

		DeviceHistory deviceHistory = em.find(DeviceHistory.class, DEVICE1);
		assertNotNull(deviceHistory);
        ChildJournal journal = ChildJournal.find(em, 1L, deviceHistory);
        assertNotNull(journal);		
		
        ChildJournalEntry childJournalEntry = request.getChildJournal().get(0);
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
        ClientPushPullRequest request = SIMPLE_SCENARIO.createPushRequest();
        synchronizer.push(request);
        
        DeviceHistory deviceHistory = em.find(DeviceHistory.class, DEVICE1);
        assertNotNull(deviceHistory);

		TransactionJournalEntry transactionJournalEntry = request.getTransactionJournal().get(0);		
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
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory(DEVICE1, EMAIL);
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
				new ClientPushPullRequest(deviceHistory.getDeviceId(), EMAIL, 0L, 2L, 0L, 2L, childJournal, transactionJournal);
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
		DeviceHistory deviceHistory = synchronizer.findOrCreateDeviceHistory(DEVICE1, EMAIL);
		
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
				new ClientPushPullRequest(deviceHistory.getDeviceId(), EMAIL, 0L, 2L, 0L, 2L, childJournal, transactionJournal);
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

	
	@Test
	public void testPushPullCycle() {		
		ClientPushPullRequest request = SCENARIO1.createPushRequest();
		
		ClientPushResponse response = synchronizer.push(request);
		
		assertEquals(0, response.getChildJournal().size());
		assertEquals(0, response.getTransactionJournal().size());
		
		request = SCENARIO1.createPullRequest(request);
		
		ClientPullResponse pullResponse = synchronizer.pull(request);
		assertEquals(2, pullResponse.getHwmChildPull());
		assertEquals(4, pullResponse.getHwmTransPull());

		verifyMaster(SCENARIO1);
	}

	@Test
	public void testBiDirectional() {		
		ClientPushPullRequest request = SCENARIO1.createPushRequest();
		ClientPushResponse response = synchronizer.push(request);
		ClientPullResponse pullResponse = synchronizer.pull(SCENARIO1.createPullRequest(request));
		
		ClientPushPullRequest request2 = SCENARIO1.createPushRequest();
		ClientPushResponse response2 = synchronizer.push(request);
		verifyScenario1MirroredFromMaster(SCENARIO1, SCENARIO2, response2);

	}

	private void verifyScenario1MirroredFromMaster(Scenario scenario1, Scenario scenario2, ClientPushResponse response) {
		Set<String> scenario1Names = scenario1.getChildMap().keySet();
		assertEquals(scenario1Names.size(), response.getChildJournal().size());
		Map <Long, String> childMap = Maps.newHashMap();
		for (ChildJournalEntry entry : response.getChildJournal()) {
			assertTrue(scenario1Names.contains(entry.getName()));
			//TODO: What happens when you already got this child and there is not already one.
			// Need to store the mapping from Remote Child ID to local Child ID somehow?
			childMap.put(entry.getChildId(), entry.getName());
		}
		assertEquals(scenario1.getTransactionMap().values().size(), response.getTransactionJournal().size());
		for (TransactionJournalEntry entry : response.getTransactionJournal()) {
			String childName = childMap.get(entry.getChildId());
			boolean found = false;
			for (Entry<String, Transaction> transactionEntry : scenario1.getTransactionMap().entries()) {
				String transactionChildName = transactionEntry.getKey();
				Transaction transaction = transactionEntry.getValue();
				if (childName.equals(transactionChildName) 
						&& transaction.getAmount() == entry.getAmount()
						&& transaction.getDescription().equals(entry.getDescription())
						&& transaction.getDate().getMillis() == entry.getDateMillis()) {
					found = true;
				}
			}
			assertTrue(found);
		}
	}

	private void verifyMaster(Scenario scenario) {
		String deviceId = scenario.getChildMap().values().iterator().next().getDeviceId();
		DeviceHistory deviceHistory = em.find(DeviceHistory.class, deviceId);
		assertNotNull(deviceHistory);
		DeviceHistory masterHistory = em.find(DeviceHistory.class, deviceHistory.getGroup().getMasterId());
		assertNotNull(masterHistory);
		ImmutableCollection<Child> actualChildren = ImmutableSet.copyOf(Sets.newHashSet(masterHistory.getChildren(em)));
		ImmutableCollection<Child> expectedChildren = scenario.getChildMap().values();		
		assertEquals(expectedChildren.size(), actualChildren.size());
		for (Child actualChild: actualChildren) {
			ImmutableCollection<Transaction> actualTransactions = ImmutableSet.copyOf(actualChild.getTransactions(em));
			ImmutableCollection<Transaction> expectedTransactions = scenario.getTransactionMap().get(actualChild.getName());
			assertEquals(expectedTransactions.size(), actualTransactions.size());
		}
	}

    private static class Scenario {
        @Getter private final ImmutableMap<String, Child> childMap;
        @Getter private final ImmutableMultimap<String, Transaction> transactionMap;
        
        public Scenario(ImmutableMap<String, Child> childMap, 
                ImmutableMultimap<String, Transaction> transactionMap) {
            this.childMap = childMap;
            this.transactionMap = transactionMap;
        }
        
        ClientPushPullRequest createPushRequest() {        
            List<ChildJournalEntry> childJournal = createChildJournal();       
            List<TransactionJournalEntry> transactionJournal = createTransactionJournal();
            
            String deviceId = getChildMap().values().iterator().next().getDeviceId();
            return new ClientPushPullRequest(deviceId, EMAIL, 
                    0L, childJournal.size(), 
                    0L, transactionJournal.size(), 
                    childJournal, transactionJournal);        
        }

        List<ChildJournalEntry> createChildJournal() {
            List<ChildJournalEntry> result = Lists.newArrayList();
            long childKey = 1;
            long journalKey = 1;
            for (Child child : getChildMap().values()) {
                child.setChildId(childKey++);
                result.add(new ChildJournalEntry(journalKey++, TransactionType.CREATE, TIMESTAMP, child.getChildId(), child.getName()));
            }
            return result;
        }
        
        List<TransactionJournalEntry> createTransactionJournal() {
            long journalKey = 1;
            long transactionKey = 1;
            List<TransactionJournalEntry> result = Lists.newArrayList();
            for (Entry<String, Transaction> entry : getTransactionMap().entries()) {
                Transaction transaction = entry.getValue();
                Child child = getChildMap().get(entry.getKey());
                
                transaction.setTransactionId(transactionKey++);
                result.add(new TransactionJournalEntry(journalKey++, TransactionType.CREATE, TIMESTAMP, transaction.getTransactionId(), 
                        child.getChildId(), transaction.getDescription(), transaction.getDate().getMillis(), transaction.getAmount()));         
            }
            return result;
        }
        
        private ClientPushPullRequest createPullRequest(ClientPushPullRequest pushRequest) {     
            return new ClientPushPullRequest(pushRequest.getDeviceId(), pushRequest.getEmail(),
                    pushRequest.getHwmChildPull(),
                    pushRequest.getHwmChildPush(), 
                    pushRequest.getHwmTransPull(), 
                    pushRequest.getHwmTransPush(), 
                    ImmutableList.<ChildJournalEntry>of(), 
                    ImmutableList.<TransactionJournalEntry>of());
        }
    }
}
