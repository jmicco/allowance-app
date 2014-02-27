package org.jmicco.parentbank.web;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
}
