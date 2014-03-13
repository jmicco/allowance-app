package org.jmicco.parentbank.parentdb;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "groups")
@Table(name = "groups", schema = "parentdb")
@EqualsAndHashCode
@ToString
public class Group {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Getter @Setter private long groupId;
	@Getter @Setter private String masterId;
	
	public Group() {
		this(null);
	}
	
	public Group(String masterId) {
		this(0, masterId);
	}
	
	public Group(long groupId, String masterId) {
		this.groupId = groupId;
		this.masterId = masterId;
	}
}
