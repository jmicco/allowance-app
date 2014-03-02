package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GroupTest {
	private TestDatabaseHelper helper;
	private EntityManager em;
	
	@Before
	public void setUp() throws IOException, SQLException {
		helper = new TestDatabaseHelper();
		em = helper.getEm();
	}
	
	@After
	public void tearDown() {
		helper.close();
		helper = null;
		em = null;
	}
	
	@Test
	public void testGroup() {
		Group expectedGroup = new Group("7777");
		Group expectedGroup2 = new Group("8888");
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(expectedGroup);
		em.persist(expectedGroup2);
		tx.commit();
		
		Group actualGroup = em.find(Group.class, expectedGroup.getGroupId());
		assertEquals(expectedGroup.getMasterId(), actualGroup.getMasterId());
		assertEquals(expectedGroup.getGroupId(), actualGroup.getGroupId());
		Group actualGroup2 = em.find(Group.class, expectedGroup2.getGroupId());
		assertEquals(expectedGroup2.getMasterId(), actualGroup2.getMasterId());
		assertEquals(expectedGroup2.getGroupId(), actualGroup2.getGroupId());
		assertTrue(actualGroup.getGroupId() != actualGroup2.getGroupId());
	}
}
