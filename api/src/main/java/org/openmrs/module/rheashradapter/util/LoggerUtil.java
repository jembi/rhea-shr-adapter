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
package org.openmrs.module.rheashradapter.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.openmrs.module.rheashradapter.model.GetEncounterLog;
import org.openmrs.module.rheashradapter.model.PostEncounterLog;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBR;

public class LoggerUtil {
	
	public GetEncounterLog getLogger(GetEncounterLog log, String error, String errorDetails, String result, Date startDate, Date endDate, Exception e){
		if(error != null)
			log.setError(error);
		if(e != null){
			StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
			log.setErrorDetails(stackTrace);
		}else{
			log.setErrorDetails(errorDetails);
		}
		if(result != null)
			log.setResult(result);
		if(startDate != null)
			log.setDateStart(startDate);
		if(endDate != null)
			log.setDateEnd(endDate);			
		return log;
	}
	
	public PostEncounterLog postLogger(PostEncounterLog log, String result, String error){
		if(result != null)
			log.setResult(result);
		if(error != null)
			log.setError(error);
		log.setValid(false);		
		return log;
	}

	public String getEncounterId(Message response) {
		ORU_R01 oru = (ORU_R01) response;
		ORU_R01_PATIENT_RESULT patientResult = oru.getPATIENT_RESULT();
		ORU_R01_ORDER_OBSERVATION orderObs;
		OBR obr = null;
		try {
			orderObs = patientResult.getORDER_OBSERVATION(0);
			obr = orderObs.getOBR();
		} catch (HL7Exception e) {
			e.printStackTrace();
		}
		return obr.getFillerOrderNumber().getEntityIdentifier().getValue();
	}

}
