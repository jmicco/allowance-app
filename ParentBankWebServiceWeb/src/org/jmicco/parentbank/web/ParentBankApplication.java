package org.jmicco.parentbank.web;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/rest")
public class ParentBankApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        resources.add(TestUri.class);
        resources.add(ParentBankSynchronization.class);
        return resources;
	}
}
