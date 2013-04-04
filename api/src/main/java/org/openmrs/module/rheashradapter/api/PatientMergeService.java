package org.openmrs.module.rheashradapter.api;

import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface PatientMergeService extends OpenmrsService {

	public String mergePatient(String patientIdentifierType, String survivingPatient, String retiringPatient);
	
	public String restorePatient(String patientIdentifierType, String restorePatientId);
}
