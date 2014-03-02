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
	private long masterId;
	
	public long getGroupId() {
		return groupId;
	}
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	public long getMasterId() {
		return masterId;
	}
	public void setMasterId(long masterId) {
		this.masterId = masterId;
	}

}
