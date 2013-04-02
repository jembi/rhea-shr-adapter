package org.openmrs.module.rheashradapter.model;

public class EncounterDataObject implements Comparable<EncounterDataObject> {
	
	public PatientMergeRecord getPatientMergeRecord() {
		return patientMergeRecord;
	}

	public void setPatientMergeRecord(PatientMergeRecord patientMergeRecord) {
		this.patientMergeRecord = patientMergeRecord;
	}

	private PatientMergeRecord patientMergeRecord;
	
	public Integer getEncounterDataObjectId() {
		return encounterDataObjectId;
	}

	public void setEncounterDataObjectId(Integer encounterDataObjectId) {
		this.encounterDataObjectId = encounterDataObjectId;
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

	private Integer encounterDataObjectId;
	
	private Integer encounterId;
	
	private Integer obsId;

	@Override
	public int compareTo(EncounterDataObject o) {
		return obsId.compareTo(o.getObsId());
	}
	

}
