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
	private Group group;
	
	@Before
	public void setUp() throws Exception {
		helper = new TestDatabaseHelper();
		em = helper.getEm();
		
		group = new Group();
		group.setMasterId("master1234");
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(group);
		tx.commit();
	}

	@After
	public void tearDown() throws Exception {
		helper.close();
		helper = null;
		em = null;
	}

	@Test
	public void testDeviceHistory() {		
		DeviceHistory expectedDeviceHistory = new DeviceHistory("device1234", group, "nobody@nowhere.com",
				0L, 0L, 0L, 0L, 0L, 0L);
		
		EntityTransaction tx = em.getTransaction();
		tx = em.getTransaction();
		tx.begin();
		em.persist(expectedDeviceHistory);
		tx.commit();
		
		DeviceHistory actualDeviceHistory = em.find(DeviceHistory.class, "device1234");
		
		assertNotNull(actualDeviceHistory);
		assertEquals(expectedDeviceHistory.getDeviceId(), actualDeviceHistory.getDeviceId());
		assertEquals(expectedDeviceHistory.getGroup(), actualDeviceHistory.getGroup());
		assertEquals(expectedDeviceHistory.getEmail(), actualDeviceHistory.getEmail());
		assertEquals(expectedDeviceHistory.getHwmChildPull(), actualDeviceHistory.getHwmChildPull());
		assertEquals(expectedDeviceHistory.getHwmChildPush(), actualDeviceHistory.getHwmChildPush());
		assertEquals(expectedDeviceHistory.getHwmTransPull(), actualDeviceHistory.getHwmTransPull());
		assertEquals(expectedDeviceHistory.getHwmTransPush(), actualDeviceHistory.getHwmTransPush());
	}

}
