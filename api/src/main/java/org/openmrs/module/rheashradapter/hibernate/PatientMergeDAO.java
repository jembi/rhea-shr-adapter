package org.openmrs.module.rheashradapter.hibernate;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.module.rheashradapter.model.GetEncounterLog;
import org.openmrs.module.rheashradapter.model.MergedDataObject;
import org.openmrs.module.rheashradapter.model.PatientMergeLog;
import org.openmrs.module.rheashradapter.model.PatientRestoreRecord;
import org.openmrs.serialization.SerializationException;

public interface PatientMergeDAO {

	public void savePatientMergeLog(PatientMergeLog personMergeLog) throws SerializationException, APIException;
	
	void savePatientRestore(PatientRestoreRecord patientRestoreRecord);
	
	public PatientMergeLog getPatientMergeLog(String retiredPatient, Boolean isRestored);
		
	public Patient getRetiredPatient(int patientId);

	List<MergedDataObject> getMergedDataObjects(int id);

	public boolean validateRequest(String survivingPatient, String retiringPatient);
	
	
}
