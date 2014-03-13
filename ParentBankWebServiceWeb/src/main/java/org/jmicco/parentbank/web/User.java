package org.jmicco.parentbank.web;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@XmlRootElement
@EqualsAndHashCode
@ToString
public class User {
	@Getter @Setter public String email;
	
	public User() {		
	}
	
	public User(String email) {
		this.email = email;
	}
}