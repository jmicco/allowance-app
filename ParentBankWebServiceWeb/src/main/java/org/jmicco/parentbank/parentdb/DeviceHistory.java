package org.jmicco.parentbank.parentdb;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author John
 *
 */
@Entity(name = "device_history")
@Table(name = "device_history", schema = "parentdb")
public class DeviceHistory {
	@Id
	private String deviceId;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="groupId", nullable=false, updatable=false)
	private Group group;
	
	private long hwmChildPush;
	private long hwmTransPush;
	private long hwmChildPull;
	private long hwmTransPull;
	
	private String email;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public long getHwmChildPush() {
		return hwmChildPush;
	}

	public void setHwmChildPush(long hwmChildPush) {
		this.hwmChildPush = hwmChildPush;
	}

	public long getHwmTransPush() {
		return hwmTransPush;
	}

	public void setHwmTransPush(long hwmTransPush) {
		this.hwmTransPush = hwmTransPush;
	}

	public long getHwmChildPull() {
		return hwmChildPull;
	}

	public void setHwmChildPull(long hwmChildPull) {
		this.hwmChildPull = hwmChildPull;
	}

	public long getHwmTransPull() {
		return hwmTransPull;
	}

	public void setHwmTransPull(long hwmTransPull) {
		this.hwmTransPull = hwmTransPull;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
