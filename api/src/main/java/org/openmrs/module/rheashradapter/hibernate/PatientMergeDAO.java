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
