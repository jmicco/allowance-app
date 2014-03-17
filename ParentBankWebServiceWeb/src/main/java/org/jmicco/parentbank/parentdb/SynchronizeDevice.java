package org.jmicco.parentbank.parentdb;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NamedQuery;
import javax.persistence.TypedQuery;

import org.jmicco.parentbank.web.ChildJournalEntry;
import org.jmicco.parentbank.web.ClientPullResponse;
import org.jmicco.parentbank.web.ClientPushPullRequest;
import org.jmicco.parentbank.web.ClientPushResponse;
import org.jmicco.parentbank.web.TransactionJournalEntry;
import org.joda.time.Instant;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SynchronizeDevice {

	private final EntityManager em;

	public SynchronizeDevice(EntityManager em) {
		this.em = em;
	}
	
	//
	// During the push:
	// 1. Query all of the master journal entries > hwmPullX and return them
	// 2. Mirror all of the device journal entries into the device specific journals
	//    * Update hwmPushX to match the new top
	// 3. Return the new transactions and hwmPush to the client
	// 

	public ClientPushResponse push(ClientPushPullRequest request) {
		ClientPushResponse response = new ClientPushResponse();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		// Get the deviceHistory for this device
		DeviceHistory deviceHistory = findOrCreateDeviceHistory(request.getDeviceId(), request.getEmail());
		// Find the masterHistory as well
		DeviceHistory masterHistory = em.find(DeviceHistory.class, deviceHistory.getGroup().getMasterId());

		// Update the pull level only when the child says that it is done
		// This prevents crashes on either end from corrupting the data.		
		if (request.getHwmChildPull() > deviceHistory.getHwmChildPull()) {
			deviceHistory.setHwmChildPull(request.getHwmChildPull());			tx.commit();
		}
		
		if (request.getHwmTransPull() > deviceHistory.getHwmTransPull()) {
			deviceHistory.setHwmTransPull(request.getHwmTransPull());
		}
		
		// Fill in the response with new journal entries
		List<ChildJournalEntry> newChildEntries = Lists.newArrayList();
		response.setHwmChildPull(findNewChildJournalEntries(newChildEntries, masterHistory, request.getHwmChildPull()));
		response.setChildJournal(newChildEntries);		
		// Bundle and send any transaction journal entries back to the requestor
		List<TransactionJournalEntry> newTransactionEntries = Lists.newArrayList();
		response.setHwmTransPull(findNewTransactionJournalEntries(newTransactionEntries, masterHistory, request.getHwmTransPull()));
		response.setTransactionJournal(newTransactionEntries);

		// If there are new child journal entries mirror them
		mirrorChildJournalEntries(deviceHistory, request.getHwmChildPush(), request.getChildJournal());
		// If there are new transaction journal entries mirror them
		mirrorTransactionJournalEntries(deviceHistory, request.getHwmTransPush(), request.getTransactionJournal());
		// Mirror the transactions into the repository
		
		em.merge(deviceHistory);
		tx.commit();
		// Bundle and send any child journal entries back to the requestor
		// Finally apply new client transactions to the Master list - do not update the pull hwm until the client comes back			
		return response;
	}

	// During the pull
	// 1. Apply all of the transactions between hwmMasterPushX and hwmPushX to the master repo
	// 2. mirror all of the new transactions to the device transaction log
	// 3. set hwmPushX = hwmMasterPushX = highest transaction seen
	// 4. Update the hwmPullX = hwmPull from the client
	// 5. Commit and return the new hwmPush / Pull
	public ClientPullResponse pull(ClientPushPullRequest request) {
		ClientPullResponse response = new ClientPullResponse();
		EntityTransaction tx = em.getTransaction();
		tx.begin();	
		
		// Get the deviceHistory for this device - it should exist
		DeviceHistory deviceHistory = em.find(DeviceHistory.class, request.getDeviceId());
		// Find the masterHistory as well
		DeviceHistory masterHistory = em.find(DeviceHistory.class, deviceHistory.getGroup().getMasterId());

		response.setHwmChildPull(applyChildJournalToMaster(deviceHistory, masterHistory));
		response.setHwmTransPull(applyTransactionJournalToMaster(deviceHistory, masterHistory));
		
		mirrorChildJournalEntries(deviceHistory, request.getHwmChildPush(), request.getChildJournal());
		mirrorTransactionJournalEntries(deviceHistory, request.getHwmTransPush(), request.getTransactionJournal());
		
		deviceHistory.setHwmChildPush(request.getHwmChildPush());
		deviceHistory.setHwmChildMasterPush(request.getHwmChildPush());
		deviceHistory.setHwmTransPush(request.getHwmTransPush());
		deviceHistory.setHwmTransMasterPush(request.getHwmTransPush());
		deviceHistory.setHwmChildPull(response.getHwmChildPull());
		deviceHistory.setHwmTransPull(response.getHwmTransPull());
		
		em.merge(deviceHistory);
		em.merge(masterHistory);
		tx.commit();
		return response;
	}
	
	private long applyTransactionJournalToMaster(DeviceHistory deviceHistory,
			DeviceHistory masterHistory) {
		long result = deviceHistory.getHwmTransPull();
		if (deviceHistory.getHwmTransMasterPush() >= deviceHistory.getHwmTransMasterPush()) {
			return result;
		}
		TypedQuery<TransactionJournal> query = em.createNamedQuery("TransactionJournal.FindMasterPushJournalEntries", TransactionJournal.class);
		query.setParameter("deviceId", deviceHistory.getDeviceId());
		query.setParameter("masterPushJournalId", deviceHistory.getHwmTransMasterPush());
		query.setParameter("pushJournalId", deviceHistory.getHwmTransPush());
		
		List<TransactionJournal> resultList = query.getResultList();
		if (resultList.isEmpty()) {
			return result;
		}
		Map<Long, Long> childMap = produceChildMap(deviceHistory, masterHistory);
		Transaction transaction;
		for (TransactionJournal journal: resultList) {
			Long childId = childMap.get(journal.getChildId());
			if (childId == null) {
				throw new IllegalStateException("Unable to get a valid child ID for this transaction");
			}
			switch (journal.getTransactionType()) {
			case CREATE:
				transaction = new Transaction(masterHistory.getDeviceId(), childId, journal.getDate(), journal.getDescription(), journal.getAmount());
				result = Math.max(result, transaction.persist(em, deviceHistory));
				break;
			case DELETE:
				throw new UnsupportedOperationException("Only CREATE accepted for transactions");
			case UPDATE:
				throw new UnsupportedOperationException("Only CREATE accepted for transactions");
			default:
				throw new UnsupportedOperationException("Only CREATE / UPDATE / DELETE accepted for transactions");
			}
		}
		return result;
	}

	private long applyChildJournalToMaster(DeviceHistory deviceHistory,	DeviceHistory masterHistory) {
		long result = deviceHistory.getHwmChildPull();
		if (deviceHistory.getHwmChildMasterPush() >= deviceHistory.getHwmChildMasterPush()) {
			return result;
		}
		TypedQuery<ChildJournal> query = em.createNamedQuery("ChildJournal.FindMasterPushJournalEntries", ChildJournal.class);
		query.setParameter("deviceId", deviceHistory.getDeviceId());
		query.setParameter("masterPushJournalId", deviceHistory.getHwmChildMasterPush());
		query.setParameter("pushJournalId", deviceHistory.getHwmChildPush());
		
		List<ChildJournal> resultList = query.getResultList();
		for (ChildJournal journal: resultList) {
			if (journal.getTransactionType() != TransactionType.CREATE) {
				throw new UnsupportedOperationException("Only Create transactions are supported at this point");				
			}
			// TODO: Name collisions will cause duplicate named children - need to fix that.
			Child child = new Child(masterHistory.getDeviceId(), journal.getName());
			result = Math.max(result, child.persist(em, masterHistory));			
		}
		return result;		
	}

	private long findNewTransactionJournalEntries(
			List<TransactionJournalEntry> newTransactionEntries,
			DeviceHistory masterHistory, long hwmTransPull) {
		TypedQuery<TransactionJournal> query = em.createNamedQuery("TransactionJournal.FindNewJournalEntries", TransactionJournal.class);
		query.setParameter("journalId", hwmTransPull);
		query.setParameter("deviceId", masterHistory.getDeviceId());
		List<TransactionJournal> transactionJournalEntries = query.getResultList();
		long result = hwmTransPull;
		for (TransactionJournal journal : transactionJournalEntries) {
			newTransactionEntries.add(
					new TransactionJournalEntry(journal.getJournalId(), journal.getTransactionType(), journal.getTimestamp().getMillis(),
							journal.getTransactionId(), journal.getChildId(), journal.getDescription(), journal.getDate().getMillis(),
							journal.getAmount()));
			result = Math.max(result, journal.getJournalId());
		}
		return result;
	}

	private long findNewChildJournalEntries(List<ChildJournalEntry> newChildEntries, DeviceHistory masterHistory, long hwmChildPull) {
		TypedQuery<ChildJournal> query = em.createNamedQuery("ChildJournal.FindNewJournalEntries", ChildJournal.class);
		query.setParameter("journalId", hwmChildPull);
		query.setParameter("deviceId", masterHistory.getDeviceId());
		List<ChildJournal> childJournalEntries = query.getResultList();
		long result = hwmChildPull;
		for (ChildJournal journal : childJournalEntries) {
			newChildEntries.add(
					new ChildJournalEntry(journal.getJournalId(), journal.getTransactionType(), journal.getTimestamp().getMillis(),
							journal.getChildId(), journal.getName()));
			result = Math.max(result, journal.getJournalId());
		}
		return result;
	}

	@VisibleForTesting	
	void mirrorChildJournalEntries(DeviceHistory deviceHistory,	long hwmChildPush, List<ChildJournalEntry> childJournal) {
		if (childJournal.isEmpty()) {
			return;
		}
		
		for (ChildJournalEntry entry : childJournal) {
			if (entry.getJournalId() > deviceHistory.getHwmChildPush()) {
				em.persist(new ChildJournal(entry.getJournalId(), deviceHistory, entry.getTransactionType(), 
						new Instant(entry.getTimestampMillis()), entry.getChildId(), entry.getName()));
			}
		}
		deviceHistory.setHwmChildPush(hwmChildPush);
	}

	@VisibleForTesting	
	void mirrorTransactionJournalEntries(DeviceHistory deviceHistory, long hwmTransPush, List<TransactionJournalEntry> transactionJournal) {
		if (transactionJournal.isEmpty()) {
			return;
		}
		for (TransactionJournalEntry entry : transactionJournal) {
			if (entry.getJournalId() > deviceHistory.getHwmTransPush()) {
				em.persist(new TransactionJournal(entry.getJournalId(), deviceHistory, entry.getTransactionType(), 
					new Instant(entry.getTimestampMillis()), 
					entry.getTransactionId(), entry.getChildId(), entry.getDescription(), new Instant(entry.getDateMillis()),
					entry.getAmount()));
			}
		}
		deviceHistory.setHwmTransPush(hwmTransPush);
	}
	
	private Map<Long, Long> produceChildMap(DeviceHistory deviceHistory, DeviceHistory masterHistory) {
		Map<Long, Long> result = Maps.newHashMap();
		
		TypedQuery<Child> query = em.createNamedQuery("Child.FindAllChildren", Child.class);
		query.setParameter("deviceId", masterHistory.getDeviceId());
		List<Child> resultList = query.getResultList();
		Map<String, Child> nameToChild = Maps.newHashMap();
		for (Child c : resultList) {
			nameToChild.put(c.getName(), c);
		}
		TypedQuery<ChildJournal> journalQuery = em.createNamedQuery("ChildJournal.FindAllJournalEntries", ChildJournal.class);
		journalQuery.setParameter("deviceId", deviceHistory.getDeviceId());
		List<ChildJournal> journalEntries = journalQuery.getResultList();
		for (ChildJournal j : journalEntries) {
			Child masterChild = nameToChild.get(j.getName());
			if (masterChild != null) {
				result.put(j.getChildId(), masterChild.getChildId());
			}
		}
		return result;
	}

	@VisibleForTesting
	DeviceHistory findOrCreateDeviceHistory(String deviceId, String email) {
		DeviceHistory deviceHistory = em.find(DeviceHistory.class, deviceId);
		if (deviceHistory != null) {
			return deviceHistory;
		}
		TypedQuery<DeviceHistory> query = em.createNamedQuery("DeviceHistory.FindDeviceHistoryByEmail", DeviceHistory.class);
		query.setParameter("email", email);
		List<DeviceHistory> resultList = query.getResultList();
		
		Group group;

		if (resultList.isEmpty()) {
			UUID masterUUID = UUID.randomUUID();
			group = new Group(masterUUID.toString());
			DeviceHistory masterHistory = new DeviceHistory(group.getMasterId(), group, "", 0L, 0L, 0L, 0L, 0L, 0L);
			em.persist(masterHistory);
			em.persist(group);
		} else {
			group = resultList.get(0).getGroup();
		}
		
		deviceHistory = new DeviceHistory(deviceId, group, email, 0L, 0L, 0L, 0L, 0L, 0L);
		em.persist(deviceHistory);
		
		return deviceHistory;
	}
}
