package org.openmrs.module.rheashradapter.api;

import java.util.List;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.serialization.SerializationException;
import org.springframework.transaction.annotation.Transactional;
import org.openmrs.module.rheashradapter.model.PatientMergeLog;


@Transactional
public interface PatientMergeService extends OpenmrsService {
	
	public void mergePatients(Patient preferred, List<Patient> notPreferred) throws APIException, SerializationException;
	
	public String restorePatients(String patientIdentifierType, String restorePatientId, PatientMergeLog log);
	
	public void validatePostidentifiers(Map<String, String> validatePostidentifiers);
	
	public PatientMergeLog getPatientMergeLog(String retiringPatientId, Boolean isRestored);
}
