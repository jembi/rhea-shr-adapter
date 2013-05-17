package org.openmrs.module.rheashradapter.model;


public class MergedDataObject implements Comparable<MergedDataObject> {
	
/*	publiPaneRecordrd getPatientMergeRecord() {
		return patientMergeRecord;
	}

	public void setPatientMergeRecord(PatientMergeRecord patientMergeRecord) {
		this.patientMergeRecord = patientMergeRecord;
	}

	private PatientMergeRecord patientMergeRecord;
	
	public Integer getMergedDataObjectId() {
		return mergedDataObjectId;
	}*/

	public void setMergedDataObjectId(Integer mergedDataObjectId) {
		this.mergedDataObjectId = mergedDataObjectId;
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

	private Integer mergedDataObjectId;
	
	private Integer encounterId;
	
	private Integer obsId;

	@Override
	public int compareTo(MergedDataObject o) {
		return obsId.compareTo(o.getObsId());
	}
	

}
