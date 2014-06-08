package org.jmicco.parentbank.web;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ParentBankSynchronizationTest {
	ParentBankSynchronization sync;
	
	@Before
	public void setUp() {
		sync = new ParentBankSynchronization();
	}

	@Test
	public void testGetHello() {
		User expectedUser = new User("john.micco@gmail.com");
		assertEquals(expectedUser.getEmail(), sync.getHello().getEmail());
		assertTrue(false);
	}
}
