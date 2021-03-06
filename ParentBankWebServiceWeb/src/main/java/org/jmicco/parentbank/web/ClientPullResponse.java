package org.jmicco.parentbank.web;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@XmlRootElement
@EqualsAndHashCode
@ToString
public class ClientPullResponse {
	@Getter @Setter long hwmChildPull;  // The new hwm for child journal entries
	@Getter @Setter long hwmTransPull;  // The new hwm for transaction journal entries
}
