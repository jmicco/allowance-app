package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.assertEquals;

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
		Group expectedGroup = new Group();
		expectedGroup.setMasterId(7777);
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(expectedGroup);
		tx.commit();
		
		Group actualGroup = em.find(Group.class, expectedGroup.getGroupId());
		assertEquals(expectedGroup.getMasterId(), actualGroup.getMasterId());
	}

}
