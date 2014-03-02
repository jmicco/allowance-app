package org.jmicco.parentbank.parentdb;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "groups")
@Table(name = "groups", schema = "parentdb")
public class Group {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private long groupId;
	private String masterId;
	
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
	
	public long getGroupId() {
		return groupId;
	}
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	public String getMasterId() {
		return masterId;
	}
	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

}
