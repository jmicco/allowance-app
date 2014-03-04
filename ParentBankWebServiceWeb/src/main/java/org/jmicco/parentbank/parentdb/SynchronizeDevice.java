package org.jmicco.parentbank.parentdb;

import java.util.List;
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

public class SynchronizeDevice {

	private final EntityManager em;

	public SynchronizeDevice(EntityManager em) {
		this.em = em;
	}
	
	public ClientSynchronizationResponse push(ClientSynchronizationRequest request) {
		// Get the deviceHistory for this device
		DeviceHistory deviceHistory = findOrCreateDeviceHistory(request.getDeviceId(), request.getEmail());
		// If there are new child journal entries mirror them
		mirrorChildJournalEntries(deviceHistory, request.getHwmChildPush(), request.getChildJournal());
		// If there are new transaction journal entries mirror them
		mirrorTransactionJournalEntries(deviceHistory, request.getHwmTransPush(), request.getTransactionJournal());
		return new ClientSynchronizationResponse();
	}

	@VisibleForTesting	
	void mirrorChildJournalEntries(DeviceHistory deviceHistory,
			long hwmChildPush, List<ChildJournalEntry> childJournal) {
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
			long hwmTransPush, List<TransactionJournalEntry> transactionJournal) {
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
			em.persist(group);
		} else {
			group = resultList.get(0).getGroup();
		}
		
		deviceHistory = new DeviceHistory(deviceId, group, email, 0L, 0L, 0L, 0L);
		em.persist(deviceHistory);
		tx.commit();
		
		return deviceHistory;
	}
}
