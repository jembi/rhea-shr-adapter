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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptNumeric;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.ConceptService;
import org.openmrs.hl7.HL7Constants;

import org.openmrs.Encounter;
import org.openmrs.api.context.Context;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.NM;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.parser.GenericParser;

public class GenerateORU_R01 implements Serializable {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private static final long serialVersionUID = 1L;
	private static Integer obxCount = 0;
	int orderObsCount = 0;
	
	private ORU_R01 r01 = new ORU_R01();
	
	public ORU_R01 generateORU_R01Message(Patient pat, List<Encounter> encounterList) throws Exception {
		
		MSH msh = r01.getMSH();
		
		// Get current date
		String dateFormat = "yyyyMMddHHmmss";
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		String formattedDate = formatter.format(new Date());
		
			msh.getFieldSeparator().setValue(RHEAHL7Constants.FIELD_SEPARATOR);
			msh.getEncodingCharacters().setValue(RHEAHL7Constants.ENCODING_CHARACTERS);
			msh.getVersionID().getInternationalizationCode().getIdentifier().setValue(
			    RHEAHL7Constants.INTERNATIONALIZATION_CODE);
			msh.getVersionID().getVersionID().setValue(RHEAHL7Constants.VERSION);
			msh.getDateTimeOfMessage().getTime().setValue(formattedDate);
			msh.getSendingFacility().getNamespaceID().setValue(Context.getAdministrationService().getGlobalProperty("rheashradapter.sendingFaculty"));
			msh.getMessageType().getMessageCode().setValue(RHEAHL7Constants.MESSAGE_TYPE);
			msh.getMessageType().getTriggerEvent().setValue(RHEAHL7Constants.TRIGGER_EVENT);
			msh.getMessageType().getMessageStructure().setValue(RHEAHL7Constants.MESSAGE_STRUCTURE);
			msh.getReceivingFacility().getNamespaceID().setValue(RHEAHL7Constants.RECEIVING_FACILITY);
			msh.getProcessingID().getProcessingID().setValue(RHEAHL7Constants.PROCESSING_ID);
			msh.getProcessingID().getProcessingMode().setValue(RHEAHL7Constants.PROCESSING_MODE);
			msh.getMessageControlID().setValue(UUID.randomUUID().toString());
			
			msh.getAcceptAcknowledgmentType().setValue(RHEAHL7Constants.ACK_TYPE);
			msh.getApplicationAcknowledgmentType().setValue(RHEAHL7Constants.APPLICATION_ACK_TYPE);
			msh.getMessageProfileIdentifier(0).getEntityIdentifier().setValue(RHEAHL7Constants.MSG_PROFILE_IDENTIFIER);
		
		Cohort singlePatientCohort = new Cohort();
		singlePatientCohort.addMember(pat.getId());
		
		Map<Integer, String> patientIdentifierMap = Context.getPatientSetService().getPatientIdentifierStringsByType(
		    singlePatientCohort,
		    Context.getPatientService().getPatientIdentifierTypeByName(RHEAHL7Constants.IDENTIFIER_TYPE));
		
		PID pid = r01.getPATIENT_RESULT().getPATIENT().getPID();
		
			pid.getPatientIdentifierList(0).getIDNumber().setValue(
			    patientIdentifierMap.get(patientIdentifierMap.keySet().iterator().next()));
			pid.getPatientIdentifierList(0).getIdentifierTypeCode().setValue(RHEAHL7Constants.IDENTIFIER_TYPE);
			pid.getPatientName(0).getFamilyName().getSurname().setValue(pat.getFamilyName());
			pid.getPatientName(0).getGivenName().setValue(pat.getGivenName());
			
			// dob
			Date dob = pat.getBirthdate();
			String dobStr = "";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			dobStr = sdf.format(dob);
			pid.getDateTimeOfBirth().getTime().setValue(dobStr);
			
		// populate ORC segments
		
		createORC(r01,encounterList);
		
		// populate OBR segments
		
			createOBREnc(r01,encounterList);
		
		// populate OBX segments
		
		ConceptService cs = Context.getConceptService();
		
		int counter = 1;
		
		return r01;
	}
	
	public String getMessage(ORU_R01 r01) {
		GenericParser parser = new GenericParser();
		String msg = null;
		try {
			msg = parser.encode(r01,"XML");
		}
		catch (HL7Exception e) {
			log.error("Exception parsing constructed message.");
		}
		return msg;
	}	
	
	private static void createORC(ORU_R01 r01, List<Encounter> encounterList) throws Exception{
		int orderORCCount = 0;
		
		ORC orc = null;

			orc = r01.getPATIENT_RESULT().getORDER_OBSERVATION(orderORCCount).getORC();
			orc.getOrderControl().setValue(RHEAHL7Constants.ORDER_CONTROL);
	        orc.getOrderingProvider(0).getIDNumber().setValue(encounterList.get(0).getProvider().getId().toString());
	        
	        orc.getOrderControlCodeReason().getIdentifier().setValue("Identifier");
	        orc.getOrderControlCodeReason().getText().setValue("Text");
	        orc.getOrderControlCodeReason().getNameOfCodingSystem().setValue("Name of Coding System");
	        
	        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
	        String dateStr = "";
	        Date d = new Date();
	        dateStr = df.format(d);
	        
	        orc.getDateTimeOfTransaction().getTime().setValue(dateStr);
		
		orderORCCount++;
		
	}
	
	private void createOBREnc(ORU_R01 r01, List<Encounter> encounterList) throws Exception{
		
		for(Encounter e : encounterList){
		OBR obr = null;
	
			obr = r01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObsCount).getOBR();
			int encIdNO = orderObsCount;
			int reps = r01.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
			
			Date encDt = e.getEncounterDatetime();
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
			SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
			
			String encDateStr = "";
			String encDateOnly = "";
			if (encDt != null) {
				encDateStr = df.format(encDt);
				encDateOnly = dayFormat.format(encDt);
			}
			obr.getObservationDateTime().getTime().setValue(encDateStr);
			obr.getSetIDOBR().setValue(String.valueOf(orderObsCount));
			obr.getUniversalServiceIdentifier().getText().setValue(e.getEncounterType().getName());
			
			Set<Obs> o = e.getAllObs();
			
			Person person = e.getProvider();
			PersonAttribute personAttribute = person.getAttribute(RHEAHL7Constants.PROVIDER_IDENTIFIER_TYPE);		
			
			if(personAttribute != null){
			obr.getOrderingProvider(0).getIDNumber().setValue(personAttribute.getValue());
			obr.getOrderingProvider(0).getIdentifierTypeCode().setValue(RHEAHL7Constants.PROVIDER_IDENTIFIER_TYPE);
			}
			
			obr.getOrderingProvider(0).getFamilyName().getSurname().setValue(e.getProvider().getFamilyName());
			obr.getOrderingProvider(0).getGivenName().setValue(e.getProvider().getGivenName());
			
			// Accession number
			String accessionNumber = String.valueOf(e.getEncounterId());

			obr.getFillerOrderNumber().getEntityIdentifier().setValue(accessionNumber);
			
			Location location = e.getLocation();
			String elidString = null;
			if(location != null)
			   elidString = e.getLocation().getDescription();
			String elid = null;
			
			if(elidString != null){
			final Matcher matcher = Pattern.compile(":").matcher(elidString);
			if(matcher.find()){						
			    elid = elidString.substring(matcher.end()).trim();
			}
			if(elid != null){
				obr.getFillerField1().setValue(elid);
			}
			}
			
			if(e.getLocation() != null){
			obr.getFillerField2().setValue(e.getLocation().getName());
			}
			
			
		orderObsCount++;
		orderObsCount = createOBRGroup(r01, orderObsCount, e, encIdNO);
		}

	}
	
	private int createOBRGroup(ORU_R01 r01, int orderObsCount, Encounter encounter, int encIdNO) throws HL7Exception{
		Set<Obs> allObs = encounter.getAllObs();
		Set<Obs> rejectedObs = new HashSet<Obs>(); 
		Set<Obs> unrelatedObs = new HashSet<Obs>();
		Set<Obs> acceptedObs = new HashSet<Obs>();
		
		Iterator<Obs> it = allObs.iterator();
		
		while(it.hasNext()){
			Obs obs = it.next();
			if(obs.getObsGroup() != null && !rejectedObs.contains(obs)){
				Obs parentObs = obs.getObsGroup();	
				Set<Obs> childObs = parentObs.getGroupMembers();

				acceptedObs.add(parentObs);
				acceptedObs.addAll(childObs);
				
				it.remove();
				rejectedObs.addAll(childObs);
				
				parentObs.setVoided(true);
				createOBRGroupSegment(r01, encounter,parentObs,childObs,orderObsCount, encIdNO);
				orderObsCount++;
				
			}
		}
	
		allObs.removeAll(acceptedObs);
		 int i = 0;
		if(allObs.size() > 0){
			 i = createOBRGroupSegment(r01, encounter,null,allObs,orderObsCount, encIdNO);
		}else{
			i =orderObsCount;
		}
		return i;
	}
	
	private int createOBRGroupSegment(ORU_R01 r01, Encounter encounter, Obs parentObs,
			Set<Obs> childObs, int orderObsCount, int encIdNO) throws HL7Exception {
		
		OBR obr = null;
		
		obr = r01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObsCount).getOBR();
		
		Date encDt = encounter.getEncounterDatetime();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
		
		String encDateStr = "";
		String encDateOnly = "";
		if (encDt != null) {
			encDateStr = df.format(encDt);
			encDateOnly = dayFormat.format(encDt);
		}
		obr.getSetIDOBR().setValue(String.valueOf(orderObsCount));
		
		if(parentObs != null){
			if(parentObs.isObsGrouping()){
				Collection<ConceptMap> conceptMappings =  parentObs.getConcept().getConceptMappings();
				Iterator<ConceptMap> itr = conceptMappings.iterator(); 
				boolean hasMapping = false;
				
				while(itr.hasNext() && hasMapping == false){
				ConceptMap map = itr.next();
				if(map.getSource().getName().toString().equals("RWCS") ||map.getSource().getName().toString().equals("ICD10")
						 ||map.getSource().getName().toString().equals("LOINC")){
				
				obr.getUniversalServiceIdentifier().getIdentifier().setValue(map.getSourceCode());
				obr.getUniversalServiceIdentifier().getText().setValue(parentObs.getConcept().getName().toString());
				obr.getUniversalServiceIdentifier().getNameOfCodingSystem().setValue(map.getSource().getName());
				hasMapping = true;
				}
				}
			}
		}
        
		// Accession number
		String accessionNumber = String.valueOf(encounter.getEncounterId());
		
		obr.getParentNumber().getFillerAssignedIdentifier().getUniversalID().setValue(accessionNumber);
		
		obr.getPlacerField1().setValue(Integer.toString(encIdNO));
		
		int counter = 0;
		for(Obs ob : childObs){
			createOBXSegment(ob, orderObsCount, counter);
			counter++;
		}
		
		orderObsCount = orderObsCount + 1;
		return orderObsCount;
		
	}

	private void createOBXSegment(Obs ob,int orderObsCount, int counter) throws HL7Exception, DataTypeException {
		ConceptService cs = Context.getConceptService();
		
		OBX obx = r01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObsCount).getOBSERVATION(counter).getOBX();
		obx.getSetIDOBX().setValue(counter + "");
		
		Collection<ConceptMap> conceptMappings =  ob.getConcept().getConceptMappings();
		Iterator<ConceptMap> itr = conceptMappings.iterator(); 
		boolean hasMapping = false;
		
		while(itr.hasNext() && hasMapping == false){
			ConceptMap map = itr.next();
			if(map.getSource().getName().toString().equals("RWCS") ||map.getSource().getName().toString().equals("ICD10")
					 ||map.getSource().getName().toString().equals("LOINC")){
			
			obx.getObservationIdentifier().getIdentifier().setValue(map.getSourceCode());
			obx.getObservationIdentifier().getText().setValue(ob.getConcept().getName().toString());
			obx.getObservationIdentifier().getNameOfCodingSystem().setValue(map.getSource().getName());
			hasMapping = true;
			}
			}
		
		ConceptDatatype datatype = ob.getConcept().getDatatype();
		
		if (ob.getConcept().isNumeric()) {
			obx.getValueType().setValue(HL7Constants.HL7_NUMERIC);
			
			NM nm = new NM(r01);
			nm.setValue(ob.getValueNumeric() + "");
			
			Concept concept = ob.getConcept();
			if (concept.isNumeric()) {
				ConceptNumeric conceptNumeric = cs.getConceptNumeric(concept.getId());
				if (conceptNumeric.getUnits() != null && !conceptNumeric.getUnits().equals("")) {
					obx.getUnits().getIdentifier().setValue(conceptNumeric.getUnits());
					obx.getUnits().getNameOfCodingSystem().setValue(RHEAHL7Constants.UNIT_CODING_SYSTEM);
				}
			}
			obx.getObservationValue(0).setData(nm);
			TS ts = new TS(r01);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			ts.getTime().setValue(sdf.format(ob.getDateCreated()));
			obx.getDateTimeOfTheObservation().getTime().setValue(sdf.format(ob.getDateCreated()));
			
		} else if (datatype.equals(cs.getConceptDatatypeByName(RHEAHL7Constants.CONCEPT_DATATYPE_DATETIME))
		        || datatype.equals(cs.getConceptDatatypeByName(RHEAHL7Constants.CONCEPT_DATATYPE_DATE))) {
			
			obx.getValueType().setValue(HL7Constants.HL7_DATETIME);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			TS ts = new TS(r01);
			ts.getTime().setValue(sdf.format(ob.getValueDatetime()));
			obx.getObservationValue(0).setData(ts);
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
			ts.getTime().setValue(sdf1.format(ob.getDateCreated()));
			obx.getDateTimeOfTheObservation().getTime().setValue(sdf.format(ob.getDateCreated()));
			
		} else if (datatype.equals(cs.getConceptDatatypeByName(RHEAHL7Constants.CONCEPT_DATATYPE_TEXT))) {
			
			obx.getValueType().setValue(HL7Constants.HL7_TEXT);
			ST st = new ST(r01);
			st.setValue(ob.getValueText());
			obx.getObservationValue(0).setData(st);
			
			TS ts = new TS(r01);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			ts.getTime().setValue(sdf.format(ob.getDateCreated()));
			obx.getDateTimeOfTheObservation().getTime().setValue(sdf.format(ob.getDateCreated()));
			
		} else if (datatype.equals(cs.getConceptDatatypeByName(RHEAHL7Constants.CONCEPT_DATATYPE_CODED))) {
			try{
			
			obx.getValueType().setValue(HL7Constants.HL7_CODED);
			
			CE ce = new CE(r01);
			Concept concept = ob.getValueCoded();
			
			Collection<ConceptMap> conceptValueMappings = concept.getConceptMappings();	
			
			Iterator<ConceptMap> itr2 = conceptValueMappings.iterator(); 
			boolean hasValueMapping = false;
			
			while(itr2.hasNext() && hasValueMapping == false){
				ConceptMap map = itr2.next();
				if(map.getSource().getName().toString().equals("RWCS") ||map.getSource().getName().toString().equals("ICD10")
						 ||map.getSource().getName().toString().equals("LOINC")){
					ce.getNameOfCodingSystem().setValue(map.getSource().getName());
					
					String nameStr = concept.getName().toString();
					
					ce.getText().setValue(nameStr);
					ce.getIdentifier().setValue(map.getSourceCode());
				hasValueMapping = true;
				}
				}
		
			
			obx.getObservationValue(0).setData(ce);
			
			TS ts = new TS(r01);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			ts.getTime().setValue(sdf.format(ob.getDateCreated()));
			obx.getDateTimeOfTheObservation().getTime().setValue(sdf.format(ob.getDateCreated()));
			}catch(Exception e){
			}
		}
		
		obxCount++;
		counter++;
			
		
	}
	
}
