package org.openmrs.module.rheashradapter.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openmrs.User;

public class PatientMergeRecord {
	
	private Integer mergeRecordId;
	
	public int getSurvivingPatient() {
		return survivingPatient;
	}

	public void setSurvivingPatient(int survivingPatient) {
		this.survivingPatient = survivingPatient;
	}

	public int getRetiredPatient() {
		return retiredPatient;
	}

	public void setRetiredPatient(int retiredPatient) {
		this.retiredPatient = retiredPatient;
	}

	private int survivingPatient;
	
	private int retiredPatient;
	
	private String survivingPatientId;
	
	private String retiredPatientId;
	
	private Date logTime;
	
	private Integer userId;
	
	private String status;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	private SortedSet<EncounterDataObject> encounterDataObjects;
	
    public Set<EncounterDataObject> getEncounterDataObjects() {
		if (encounterDataObjects == null)
			encounterDataObjects = new TreeSet<EncounterDataObject>();
        return encounterDataObjects;
    }
    
	public void setEncounterDataObjects(Set<EncounterDataObject> encounterDataObjects) {
		if (this.encounterDataObjects == null) {
			this.encounterDataObjects = (SortedSet<EncounterDataObject>) encounterDataObjects;
		} else {
			this.encounterDataObjects.retainAll(encounterDataObjects);
			this.encounterDataObjects.addAll(encounterDataObjects);
		}
	}
	
	public String getSurvivingPatientId() {
		return survivingPatientId;
	}

	public void setSurvivingPatientId(String survivingPatientId) {
		this.survivingPatientId = survivingPatientId;
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

	public Integer getMergeRecordId() {
		return mergeRecordId;
	}

	public void setMergeRecordId(Integer mergeRecordId) {
		this.mergeRecordId = mergeRecordId;
	}




}
