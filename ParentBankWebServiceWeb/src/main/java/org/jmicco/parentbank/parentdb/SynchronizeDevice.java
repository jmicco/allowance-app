package org.jmicco.parentbank.parentdb;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.jmicco.parentbank.web.ChildJournalEntry;
import org.jmicco.parentbank.web.ClientSynchronizationRequest;
import org.jmicco.parentbank.web.ClientSynchronizationResponse;
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
	// During the pull
	// 1. mirror all transactions to the master between hwmMasterPush and hwmPush
	//    * update hwmMasterPushX to hwmPushX
	//    * update hwmPull to new top
	// 2. Client request contains new journal entries that should be mirrored into the 
	//    device-specific journal that were already applied to the master
	//    * mirror the transactions to the device journal
	//    * update hwmMasterPush and hwmPush to the new top
	// 3. return the new hwmPush / hwmPull to the client.
	//	
	public ClientSynchronizationResponse push(ClientSynchronizationRequest request) {
		ClientSynchronizationResponse response = new ClientSynchronizationResponse();
		
		// Get the deviceHistory for this device
		DeviceHistory deviceHistory = findOrCreateDeviceHistory(request.getDeviceId(), request.getEmail());
		// Find the masterHistory as well
		DeviceHistory masterHistory = em.find(DeviceHistory.class, deviceHistory.getGroup().getMasterId());

		// Fill in the response with new journal entries
		List<ChildJournalEntry> newChildEntries = Lists.newArrayList();
		response.setHwmChildPull(findNewChildJournalEntries(newChildEntries, masterHistory, request.getHwmChildPull()));
		response.setChildJournal(newChildEntries);		
		// Bundle and send any transaction journal entries back to the requestor
		List<TransactionJournalEntry> newTransactionEntries = Lists.newArrayList();
		response.setHwmTransPull(findNewTransactionJournalEntries(newTransactionEntries, masterHistory, request.getHwmTransPull()));
		response.setTransactionJournal(newTransactionEntries);
		EntityTransaction tx = em.getTransaction();
		
		// Update the pull level only when the child says that it is done
		// This prevents crashes on either end from corrupting the data.		
		if (request.getHwmChildPull() > deviceHistory.getHwmChildPull()) {
			tx.begin();
			deviceHistory.setHwmChildPull(request.getHwmChildPull());
			tx.commit();
		}
		
		if (request.getHwmTransPull() > deviceHistory.getHwmTransPull()) {
			tx.begin();
			deviceHistory.setHwmTransPull(request.getHwmTransPull());
			tx.commit();
		}
		
		// If there are new child journal entries mirror them
		mirrorChildJournalEntries(deviceHistory, masterHistory, request.getHwmChildPush(), request.getChildJournal());
		// If there are new transaction journal entries mirror them
		mirrorTransactionJournalEntries(deviceHistory, masterHistory, request.getHwmTransPush(), request.getTransactionJournal());
		// Mirror the transactions into the repository
		
		// Bundle and send any child journal entries back to the requestor
		// Finally apply new client transactions to the Master list - do not update the pull hwm until the client comes back			
		return new ClientSynchronizationResponse();
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
			result = Math.max(result, journal.getTransactionId());
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
					new ChildJournalEntry(journal.getChildJournalId(), journal.getTransactionType(), journal.getTimestamp().getMillis(),
							journal.getChildId(), journal.getName()));
			result = Math.max(result, journal.getChildJournalId());
		}
		return result;
	}

	@VisibleForTesting	
	void mirrorChildJournalEntries(DeviceHistory deviceHistory,
			DeviceHistory masterHistory, long hwmChildPush, List<ChildJournalEntry> childJournal) {
		if (childJournal.isEmpty()) {
			return;
		}
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		for (ChildJournalEntry entry : childJournal) {
			em.persist(new ChildJournal(entry.getJournalId(), deviceHistory, entry.getTransactionType(), 
					new Instant(entry.getTimestampMillis()), entry.getChildId(), entry.getName()));
		}
		deviceHistory.setHwmChildPush(hwmChildPush);
		em.merge(deviceHistory);
		tx.commit();
	}

	@VisibleForTesting	
	void mirrorTransactionJournalEntries(DeviceHistory deviceHistory,
			DeviceHistory masterHistory, long hwmTransPush, List<TransactionJournalEntry> transactionJournal) {
		if (transactionJournal.isEmpty()) {
			return;
		}
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		for (TransactionJournalEntry entry : transactionJournal) {
			em.persist(new TransactionJournal(entry.getJournalId(), deviceHistory, entry.getTransactionType(), 
					new Instant(entry.getTimestampMillis()), 
					entry.getTransactionId(), entry.getChildId(), entry.getDescription(), new Instant(entry.getDateMillis()),
					entry.getAmount()));
		}
		deviceHistory.setHwmTransPush(hwmTransPush);
		em.merge(deviceHistory);
		tx.commit();
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
		EntityTransaction tx = em.getTransaction();
		tx.begin();

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
		tx.commit();
		
		return deviceHistory;
	}
}
