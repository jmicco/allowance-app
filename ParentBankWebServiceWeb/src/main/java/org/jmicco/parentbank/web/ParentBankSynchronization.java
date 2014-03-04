package org.jmicco.parentbank.web;

import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Persistence;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jmicco.parentbank.parentdb.TransactionType;

import com.google.common.collect.ImmutableList;

@Path("/sync")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ParentBankSynchronization {
	Logger logger = Logger.getLogger(ParentBankSynchronization.class.getName());

	@GET
	@Path("/hello")
	public User getHello() {
		logger.info("getting email");
		return new User("john.micco@gmail.com");
	}
	
	@POST
	@Path("/setuser")
	public void setUser(User user) {
		logger.info(String.format("User: %s", user.getEmail()));
	}
	
	@GET
	@Path("/synchronize")
	public ClientSynchronizationRequest synchronize() {
		Persistence.createEntityManagerFactory("production").createEntityManager();
		List<ChildJournalEntry> childJournal = ImmutableList.of(new ChildJournalEntry(5L, TransactionType.CREATE, 10000L, 6L, "childname"));
		List<TransactionJournalEntry> transactionJournal = 
				ImmutableList.of(new TransactionJournalEntry(7L, TransactionType.UPDATE, 20000L, 8L, 9L, "Allowance", 30000L, 10.0));
		return new ClientSynchronizationRequest("device1234", "nobody@nowhere.com", 1L, 2L, 3L, 4L, childJournal, transactionJournal);
	}
}
