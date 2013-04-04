package org.openmrs.module.rheashradapter.model;

public class RestoredDataObject implements Comparable<RestoredDataObject> {
	
	public PatientRestoreRecord getPatientRestoreRecord() {
		return patientRestoreRecord;
	}

	public void setPatientRestoreRecord(PatientRestoreRecord patientRestoreRecord) {
		this.patientRestoreRecord = patientRestoreRecord;
	}

	private PatientRestoreRecord patientRestoreRecord;
	
	public Integer getRestoredDataObjectId() {
		return restoredDataObjectId;
	}

	public void setRestoredDataObjectId(Integer restoredDataObjectId) {
		this.restoredDataObjectId = restoredDataObjectId;
	}

	public Integer getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}

	public Integer getObsId() {
		return obsId;
	}

	public void setObsId(Integer obsId) {
		this.obsId = obsId;
	}

	private Integer restoredDataObjectId;
	
	private Integer encounterId;
	
	private Integer obsId;

	@Override
	public int compareTo(RestoredDataObject o) {
		return obsId.compareTo(o.getObsId());
	}
	

}
