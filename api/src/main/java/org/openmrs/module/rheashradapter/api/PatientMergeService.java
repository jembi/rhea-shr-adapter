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
