package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeviceHistoryTest {
	private TestDatabaseHelper helper;
	private EntityManager em;
	
	@Before
	public void setUp() throws Exception {
		helper = new TestDatabaseHelper();
		em = helper.getEm();
	}

	@After
	public void tearDown() throws Exception {
		helper.close();
		helper = null;
		em = null;
	}

	@Test
	public void testDeviceHistory() {
		Group group = new Group();
		group.setMasterId("master1234");
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(group);
		tx.commit();
		
		DeviceHistory deviceHistory = new DeviceHistory();
		deviceHistory.setDeviceId("device1234");
		deviceHistory.setEmail("nobody@nowhere.com");
		deviceHistory.setGroup(group);
		deviceHistory.setHwmChildPull(0);
		deviceHistory.setHwmChildPush(0);
		deviceHistory.setHwmTransPull(0);
		deviceHistory.setHwmTransPush(0);
		
		tx = em.getTransaction();
		tx.begin();
		em.persist(deviceHistory);
		tx.commit();
	}

}
