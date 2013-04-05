package org.openmrs.module.rheashradapter.hibernate;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.module.rheashradapter.model.GetEncounterLog;
import org.openmrs.module.rheashradapter.model.MergedDataObject;
import org.openmrs.module.rheashradapter.model.PatientMergeRecord;
import org.openmrs.module.rheashradapter.model.PatientRestoreRecord;

public interface PatientMergeDAO {

	public void savePatientMergeRecord(PatientMergeRecord patientMergeRecord);
	
	void savePatientRestore(PatientRestoreRecord patientRestoreRecord);
	
	public PatientMergeRecord getPatientMergeRecord(String retiredPatient);
	
	public Patient getRetiredPatient(int patientId);

	List<MergedDataObject> getMergedDataObjects(int id);
	
	
}
