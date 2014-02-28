package org.jmicco.parentbank.parentdb;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GroupTest {
	private EntityManagerFactory emf;
	private EntityManager em;
	
	@Before
	public void setUp() {
		emf = Persistence.createEntityManagerFactory("GroupTest");
		em = emf.createEntityManager();
	}
	
	@After
	public void tearDown() {
		em.close();
		emf.close();
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
