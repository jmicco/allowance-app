package org.jmicco.parentbank.web;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jmicco.parentbank.parentdb.Group;


@Path("/testuri")
public class TestUri {
	private EntityManagerFactory emf;
	private final EntityManager em;
	
	public TestUri() {
		emf = Persistence.createEntityManagerFactory("production");
		em = emf.createEntityManager();
	}
	
	@GET
	@Produces("text/plain")
	public String getMessage() {
		
		Group expectedGroup = new Group();
		expectedGroup.setMasterId(UUID.randomUUID().toString());
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(expectedGroup);
		tx.commit();
		
		Group actualGroup = em.find(Group.class, expectedGroup.getGroupId());
				
		return String.format("Hello, World : %s, %s", actualGroup.getGroupId(), actualGroup.getMasterId());
	}
	
	@Override
	public void finalize() {
		em.close();
		emf.close();
	}
}
