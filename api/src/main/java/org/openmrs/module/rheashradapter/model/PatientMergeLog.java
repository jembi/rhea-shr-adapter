package org.openmrs.module.rheashradapter.model;

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */


import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.module.rheashradapter.model.PersonMergeLogData;


/**
 * The Class GetEncounterLog.
 * POJO class used to log GET Encounter requests. 
 * The following information will be stored per each GET request.
 */
public class PatientMergeLog  {
	
	public PatientMergeLog(){
		
	}
	
	/**
	 * The unique identifier of the person merge log entity
	 */

	private String retiredPatient;
	
	private String survivingPatient;
	
	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}


	public Date getRestoredDate() {
		return restoredDate;
	}

	public void setRestoredDate(Date restoredDate) {
		this.restoredDate = restoredDate;
	}


	private Date createdDate;
	
	
	private Date restoredDate;

	public Boolean getFlag() {
		return flag;
	}

	public void setFlag(Boolean flag) {
		this.flag = flag;
	}

	private Boolean flag = Boolean.FALSE;
	
	public Integer getCreatedUserId() {
		return createdUserId;
	}

	public void setCreatedUserId(Integer createdUserId) {
		this.createdUserId = createdUserId;
	}

	public Integer getRestoredUserId() {
		return restoredUserId;
	}

	public void setRestoredUserId(Integer restoredUserId) {
		this.restoredUserId = restoredUserId;
	}

	private Integer createdUserId;
	
	private Integer restoredUserId;
	/**
	 * The object representing the preferred person of the merge
	 */
	private Patient winner;
	
	/**
	 * The object representing the non-preferred person of the merge
	 */
	private Patient looser;
	
	/**
	 * serialized data representing the details of the merge
	 */
	private String serializedMergedData;
	
	/**
	 * object representing the deserialized form of the merge data. This field is not directly
	 * mapped to the database.
	 */
	private transient PersonMergeLogData personMergeLogData;
	
	public Integer getPatientMergeLogId() {
		return patientMergeLogId;
	}
	
	public void setPatientMergeLogId(Integer patientMergeLogId) {
		this.patientMergeLogId = patientMergeLogId;
	}
	
	public Patient getWinner() {
		return winner;
	}
	
	public void setWinner(Patient winner) {
		this.winner = winner;
	}
	
	public Patient getLooser() {
		return looser;
	}
	
	public void setLooser(Patient looser) {
		this.looser = looser;
	}
	
	public String getSerializedMergedData() {
		return serializedMergedData;
	}
	
	public void setSerializedMergedData(String serializedMergedData) {
		this.serializedMergedData = serializedMergedData;
	}
	
	public PersonMergeLogData getPersonMergeLogData() {
		return personMergeLogData;
	}
	
	public void setPersonMergeLogData(PersonMergeLogData personMergeLogData) {
		this.personMergeLogData = personMergeLogData;
	}
	
	private Integer patientMergeLogId;
	
	public String getRetiredPatient() {
		return retiredPatient;
	}

	public void setRetiredPatient(String retiredPatient) {
		this.retiredPatient = retiredPatient;
	}

	public String getSurvivingPatient() {
		return survivingPatient;
	}

	public void setSurvivingPatient(String survivingPatient) {
		this.survivingPatient = survivingPatient;
	}
		
}

