package org.jmicco.parentbank.parentdb;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "sequence")
@Table(name = "sequence", schema = "parentdb")
public class Sequence {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private long sequence;	
	private long notUsed = 0;
	
	private Sequence() {		
	}
	
	public static long generateId(EntityManager em) {
		Sequence seq = new Sequence();
		em.persist(seq);
		return seq.sequence;
	}
}
