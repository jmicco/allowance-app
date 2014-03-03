package org.jmicco.parentbank.parentdb;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jmicco.parentbank.web.ClientSynchronizationRequest;
import org.jmicco.parentbank.web.ClientSynchronizationResponse;

import com.google.common.annotations.VisibleForTesting;

public class SynchronizeDevice {

	private final EntityManager em;

	public SynchronizeDevice(EntityManager em) {
		this.em = em;
	}
	
	public ClientSynchronizationResponse synchronize(ClientSynchronizationRequest request) {
		DeviceHistory deviceHistory = findOrCreateDeviceHistory(request.getDeviceId(), request.getEmail());
		return new ClientSynchronizationResponse();
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
