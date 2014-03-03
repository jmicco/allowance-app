package org.jmicco.parentbank.parentdb;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jmicco.parentbank.web.ChildJournalEntry;
import org.jmicco.parentbank.web.ClientSynchronizationRequest;
import org.jmicco.parentbank.web.ClientSynchronizationResponse;
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
	DeviceHistory findOrCreateDeviceHistory(String deviceId, String email) {
		DeviceHistory deviceHistory = em.find(DeviceHistory.class, deviceId);
		if (deviceHistory != null) {
			return deviceHistory;
		}
		UUID masterUUID = UUID.randomUUID();
		Group group = new Group(masterUUID.toString());
		deviceHistory = new DeviceHistory(deviceId, group, email, 0L, 0L, 0L, 0L);
		EntityTransaction tx = em.getTransaction();
		
		tx.begin();
		em.persist(group);
		em.persist(deviceHistory);
		tx.commit();
		
		return deviceHistory;
	}
}
