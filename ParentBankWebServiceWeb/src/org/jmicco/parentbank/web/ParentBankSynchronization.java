package org.jmicco.parentbank.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/sync")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ParentBankSynchronization {
	@GET
	@Path("/hello")
	public User getHello() {
		return new User("john.micco@gmail.com");
	}
}
