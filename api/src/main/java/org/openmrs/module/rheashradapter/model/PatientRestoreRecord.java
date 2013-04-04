package org.openmrs.module.rheashradapter.model;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class PatientRestoreRecord {

private Integer restoreRecordId;

	public int getRetiredPatient() {
		return retiredPatient;
	}

	public void setRetiredPatient(int retiredPatient) {
		this.retiredPatient = retiredPatient;
	}
	
	private int retiredPatient;
		
	private String retiredPatientId;
	
	private Date logTime;
	
	private Integer userId;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private String status;
	
	private SortedSet<RestoredDataObject> restoredDataObjects;
	
    public Set<RestoredDataObject> getRestoredDataObjects() {
		if (restoredDataObjects == null)
			restoredDataObjects = new TreeSet<RestoredDataObject>();
        return restoredDataObjects;
    }
    
	public void setRestoredDataObjects(Set<RestoredDataObject> mergedDataObjects) {
		if (this.restoredDataObjects == null) {
			this.restoredDataObjects = (SortedSet<RestoredDataObject>) mergedDataObjects;
		} else {
			this.restoredDataObjects.retainAll(mergedDataObjects);
			this.restoredDataObjects.addAll(mergedDataObjects);
		}
	}
	

	public String getRetiredPatientId() {
		return retiredPatientId;
	}

	public void setRetiredPatientId(String retiredPatientId) {
		this.retiredPatientId = retiredPatientId;
	}

	public Date getLogTime() {
		return logTime;
	}

	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}
	
	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getRestoreRecordId() {
		return restoreRecordId;
	}

	public void setRestoreRecordId(Integer restoreRecordId) {
		this.restoreRecordId = restoreRecordId;
	}


	
}
