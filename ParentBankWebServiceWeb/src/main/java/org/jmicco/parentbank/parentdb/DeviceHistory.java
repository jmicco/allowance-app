package org.jmicco.parentbank.parentdb;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author John
 *
 */
@Entity(name = "device_history")
@Table(name = "device_history", schema = "parentdb")
@NamedQueries( {
	@NamedQuery(name = "DeviceHistory.FindDeviceHistoryByEmail", 
		query = "SELECT d FROM device_history d WHERE d.email = :email")
})
@EqualsAndHashCode
@ToString
public class DeviceHistory {
	@Id
	@Getter @Setter	private String deviceId;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="groupId", nullable=false, updatable=true)
	@Getter @Setter private Group group;
	
	@Getter @Setter private long hwmChildPush;
	@Getter @Setter private long hwmChildMasterPush;
	@Getter @Setter private long hwmTransPush;
	@Getter @Setter private long hwmTransMasterPush;
	@Getter @Setter private long hwmChildPull;
	@Getter @Setter private long hwmTransPull;
	
	@Getter @Setter private String email;
	
	public DeviceHistory() {
		this(null, null, null, 0L, 0L, 0L, 0L, 0L, 0L);
	}
	
	public DeviceHistory(String deviceId, Group group, String email, 
			long hwmChildPush, long hwmChildMasterPush, long hwmChildPull, long hwmTransPush, long hwmTransMasterPush, long hwmTansPull) {
		this.deviceId = deviceId;
		this.group = group;
		this.hwmChildPush = hwmChildPush;
		this.hwmChildPull = hwmChildPull;
		this.hwmTransPush = hwmTransPush;
		this.hwmTransPull = hwmTansPull;
		this.email = email;
	}

	List<Child> getChildren(EntityManager em) {
		TypedQuery<Child> query = em.createNamedQuery("Child.FindAllChildren", Child.class);
		query.setParameter("deviceId", getDeviceId());
		return query.getResultList();
	}
}
