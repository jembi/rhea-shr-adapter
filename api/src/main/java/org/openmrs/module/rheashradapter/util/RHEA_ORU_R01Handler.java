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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.IdentifierType;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptName;
import org.openmrs.ConceptProposal;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7InQueueProcessor;
import org.openmrs.hl7.HL7Service;
import org.openmrs.hl7.handler.ProposingConceptException;
import org.openmrs.module.rheashradapter.api.LogEncounterService;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.util.StringUtils;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.CWE;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.DT;
import ca.uhn.hl7v2.model.v25.datatype.DTM;
import ca.uhn.hl7v2.model.v25.datatype.FT;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.datatype.NM;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.datatype.TM;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;

import java.util.UUID;

/**
 * Parses ORUR01 messages into openmrs Encounter objects Usage: GenericParser parser = new
 * GenericParser(); MessageTypeRouter router = new MessageTypeRouter();
 * router.registerApplication("ORU", "R01", new ORUR01Handler()); Message hl7message =
 * parser.parse(somehl7string);
 * 
 * @see HL7InQueueProcessor
 */
public class RHEA_ORU_R01Handler implements Application {
	
	private Log log = LogFactory.getLog(RHEA_ORU_R01Handler.class);
	
	private LogEncounterService service = Context
			.getService(LogEncounterService.class);
	
	private String enterpriseId;

	public RHEA_ORU_R01Handler(String enterpriseId) {
	    super();
	    this.enterpriseId = enterpriseId;
    }

	/**
	 * Always returns true, assuming that the router calling this handler will only call this
	 * handler with ORU_R01 messages.
	 * 
	 * @return true
	 */
	public boolean canProcess(Message message) {
		return message != null && "ORU_R01".equals(message.getName());
	}
	
	/**
	 * Processes an ORU R01 event message
	 * 
	 * @should create encounter and obs from hl7 message
	 * @should create basic concept proposal
	 * @should create concept proposal and with obs alongside
	 * @should not create problem list observation with concept proposals
	 * @should append to an existing encounter
	 * @should create obs group for OBRs
	 * @should create obs valueCodedName
	 * @should fail on empty concept proposals
	 * @should fail on empty concept answers
	 * @should set value_Coded matching a boolean concept for obs if the answer is 0 or 1 and
	 *         Question datatype is coded
	 * @should set value as boolean for obs if the answer is 0 or 1 and Question datatype is Boolean
	 * @should set value_Numeric for obs if Question datatype is Numeric and the answer is either 0
	 *         or 1
	 * @should set value_Numeric for obs if Question datatype is Numeric
	 * @should fail if question datatype is coded and a boolean is not a valid answer
	 * @should fail if question datatype is neither Boolean nor numeric nor coded
	 */
	public Message processMessage(Message message) throws ApplicationException {
		
		if (!(message instanceof ORU_R01))
			throw new ApplicationException("Invalid message sent to ORU_R01 handler");
		
		log.debug("Processing ORU_R01 message");
		
		Message response;
		try {
			ORU_R01 oru = (ORU_R01) message;
			response = processORU_R01(oru);
		}
		catch (ClassCastException e) {
			log.error("Error casting " + message.getClass().getName() + " to ORU_R01", e);
			throw new ApplicationException("Invalid message type for handler");
		}
		catch (HL7Exception e) {
			log.error("Error while processing ORU_R01 message", e);
			throw new ApplicationException(e);
		}
		
		log.debug("Finished processing ORU_R01 message");
		
		return response;
	}
	
	/**
	 * Bulk of the processing done here. Called by the main processMessage method
	 * 
	 * @param oru the message to process
	 * @return the processed message
	 * @throws HL7Exception
	 * @should process multiple NK1 segments
	 */
	@SuppressWarnings("deprecation")
	private Message processORU_R01(ORU_R01 oru) throws HL7Exception {
		
		// extract segments for convenient use below
		MSH msh = getMSH(oru);
		PID pid = getPID(oru);
		PV1 pv1 = getPV1(oru);
		ORC orc = getORC(oru);
		
		addIdentifiers(pid);
		
		ORU_R01_PATIENT_RESULT result = oru.getPATIENT_RESULT();
		result.getORDER_OBSERVATIONReps();
			ORU_R01_ORDER_OBSERVATION order = result.getORDER_OBSERVATION(0);
			
			// the parent obr
			OBR parentObr = order.getOBR();
		
		// Obtain message control id (unique ID for message from sending
		// application)
		String messageControlId = msh.getMessageControlID().getValue();
		if (log.isDebugEnabled())
			log.debug("Found HL7 message in inbound queue with control id = " + messageControlId);
		
		HL7Service hl7Service = Context.getHL7Service();
		
		// create the encounter
		// Patient is retrieved by enterpriseId only
		Patient patient = getPatient();
		Encounter encounter = createEncounter(msh, patient, pv1, parentObr);
		
		// list of concepts proposed in the obs of this encounter.
		// these proposals need to be created after the encounter
		// has been created
		List<ConceptProposal> conceptProposals = new ArrayList<ConceptProposal>();
		
		// create observations
		
		ORU_R01_PATIENT_RESULT patientResult = oru.getPATIENT_RESULT();
		int numObr = patientResult.getORDER_OBSERVATIONReps();
		for (int i = 1; i < numObr; i++) {
			if (log.isDebugEnabled())
				log.debug("Processing OBR (" + i + " of " + numObr + ")");
			ORU_R01_ORDER_OBSERVATION orderObs = patientResult.getORDER_OBSERVATION(i);
			
			// the parent obr
			OBR obr = orderObs.getOBR();
			if(obr.getPlacerField1() != null){
			
			// if we're not ignoring this obs group, create an
			// Obs grouper object that the underlying obs objects will use
			Obs obsGrouper = null;
			
			if(obr.getUniversalServiceIdentifier().getIdentifier().getValue() != null){
			Concept obrConcept = getConcept(obr.getUniversalServiceIdentifier(), messageControlId);
				
				// create an obs for this obs group too
				obsGrouper = new Obs();
				obsGrouper.setConcept(obrConcept);
				obsGrouper.setPerson(encounter.getPatient());
				obsGrouper.setEncounter(encounter);
				Date datetime = getDatetime(obr);
				if (datetime == null)
					datetime = encounter.getEncounterDatetime();
				obsGrouper.setObsDatetime(datetime);
				obsGrouper.setLocation(encounter.getLocation());
				obsGrouper.setCreator(encounter.getCreator());
				obsGrouper.setDateCreated(new Date());
				obsGrouper.setUuid(UUID.randomUUID().toString());
				
				// set comments if there are any
				StringBuilder comments = new StringBuilder();
				ORU_R01_ORDER_OBSERVATION parent = (ORU_R01_ORDER_OBSERVATION) obr.getParent();
				int totalNTEs = parent.getNTEReps();
				for (int iNTE = 0; iNTE < totalNTEs; iNTE++)
					for (FT obxComment : parent.getNTE(iNTE).getComment()) {
						if (comments.length() > 0)
							comments.append(" ");
						comments.append(obxComment.getValue());
					}
				// only set comments if there are any
				if (StringUtils.hasText(comments.toString()))
					obsGrouper.setComment(comments.toString());
				
				// add this obs as another row in the obs table
				encounter.addObs(obsGrouper);
			}
			
			// loop over the obs and create each object, adding it to the encounter
			int numObs = orderObs.getOBSERVATIONReps();
			HL7Exception errorInHL7Queue = null;
			for (int j = 0; j < numObs; j++) {
				if (log.isDebugEnabled())
					log.debug("Processing OBS (" + j + " of " + numObs + ")");
				
				OBX obx = orderObs.getOBSERVATION(j).getOBX();
				try {
					log.debug("Parsing observation");
					Obs obs = parseObs(encounter, obx, obr, messageControlId);
					if (obs != null) {
						
						// if we're backfilling an encounter, don't use
						// the creator/dateCreated from the encounter
						if (encounter.getEncounterId() != null) {
							obs.setCreator(Context.getAuthenticatedUser());
							obs.setDateCreated(new Date());
						}
						
						// set the obsGroup on this obs
						if (obsGrouper != null)
							// set the obs to the group.  This assumes the group is already
							// on the encounter and that when the encounter is saved it will
							// propagate to the children obs
							obsGrouper.addGroupMember(obs);
						
						else {
							// set this obs on the encounter object that we
							// will be saving later
							log.debug("Obs is not null. Adding to encounter object");
							encounter.addObs(obs);
							log.debug("Done with this obs");
						}
					}
				}
				catch (HL7Exception e) {
					errorInHL7Queue = e;
				}
				finally {
					// Handle obs-level exceptions
					if (errorInHL7Queue != null) {
						throw new HL7Exception("Improperly formatted OBX: "
						        + PipeParser.encode(obx, new EncodingCharacters('|', "^~\\&")),
						        HL7Exception.DATA_TYPE_ERROR, errorInHL7Queue);
					}
				}
			}
		}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Finished creating observations");
			log.debug("Current thread: " + Thread.currentThread());
			log.debug("Creating the encounter object");
		}
		Encounter enc = Context.getEncounterService().saveEncounter(encounter);
		
		hl7Service.encounterCreated(encounter);
		
		// loop over the proposed concepts and save each to the database
		// now that the encounter is saved
		for (ConceptProposal proposal : conceptProposals) {
			Context.getConceptService().saveConceptProposal(proposal);
		}
		
		ORU_R01_ORDER_OBSERVATION orderObs = patientResult.getORDER_OBSERVATION(0);
		OBR obr = orderObs.getOBR();
		obr.getFillerOrderNumber().getEntityIdentifier().setValue(encounter.getId().toString());
		return oru;
		
	}
	
	private void addIdentifiers(PID pid) throws HL7Exception {
		CX[] cxs = pid.getPatientIdentifierList();
		Patient patient = getPatient();
		PatientService ps = Context.getPatientService();

		CX omrsCX = null;
		for(CX cx : cxs) {
			PatientIdentifierType idType = ps.getPatientIdentifierTypeByName(cx.getIdentifierTypeCode().getValue());
			if (idType==null)
				idType = (PatientIdentifierType) addIdentifierType(cx.getIdentifierTypeCode().getValue());

			if (patient.getPatientIdentifier(idType.getName()) == null) {
				PatientIdentifier identifier = new PatientIdentifier();
				identifier.setIdentifierType(idType);
				identifier.setLocation(Context.getLocationService().getLocation(1));
				identifier.setDateCreated(new Date());
				identifier.setVoided(false);
				identifier.setIdentifier(cx.getIDNumber().getValue());

				patient.addIdentifier(identifier);
			}
		}

		ps.savePatient(patient);
}


private PatientIdentifierType addIdentifierType(String idType) {
	PatientIdentifierType type = new PatientIdentifierType();
	type.setName(idType);
	type.setDescription(idType);
	type.setRequired(false);
	Context.getPatientService().savePatientIdentifierType(type);
	return type;
}

	private MSH getMSH(ORU_R01 oru) {
		return oru.getMSH();
	}
	
	private PID getPID(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getPATIENT().getPID();
	}
	
	private PV1 getPV1(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
	}
	
	private ORC getORC(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();
	}
	
	/**
	 * This method does not call the database to create the encounter row. The encounter is only
	 * created after all obs have been attached to it Creates an encounter pojo to be attached
	 * later. This method does not create an encounterId
	 * 
	 * @param msh
	 * @param patient
	 * @param pv1
	 * @param orc
	 * @return
	 * @throws HL7Exception
	 */
	private Encounter createEncounter(MSH msh, Patient patient, PV1 pv1, OBR obr) throws HL7Exception {
		
			// the encounter we will return
			Encounter encounter = null;
			EncounterRole encounterRole = null;
			
			encounter = new Encounter();
			
			Date encounterDate = getEncounterDate(obr);
			Provider provider = getProvider(pv1);
			Location location = getLocation(msh, obr);
			EncounterType encounterType = getEncounterType(pv1);
			User enterer = Context.getAuthenticatedUser();
			
			encounter.setEncounterDatetime(encounterDate);
			
			String uuid = Context.getAdministrationService().getGlobalProperty("rheashradapter.encounterrole.uuid");
			encounterRole = Context.getEncounterService().getEncounterRoleByUuid(uuid);
			
			if(encounterRole == null) {
			encounterRole = new EncounterRole();
			encounterRole.setName("SHR Provider");
			encounterRole.setDescription("Created by the SHR");
			
			encounterRole = Context.getEncounterService().saveEncounterRole(encounterRole);
			Context.getAdministrationService().setGlobalProperty("rheashradapter.encounterrole.uuid", encounterRole.getUuid());

			} 
			
			encounter.setProvider(encounterRole, provider);
			encounter.setPatient(patient);
			encounter.setLocation(location);
			encounter.setEncounterType(encounterType);
			encounter.setCreator(enterer);
			encounter.setDateCreated(new Date());
		
		return encounter;
	}
	
	/**
	 * Creates the Obs pojo from the OBX message
	 * 
	 * @param encounter The Encounter object this Obs is a member of
	 * @param obx The hl7 obx message
	 * @param obr The parent hl7 or message
	 * @param uid unique string for this message for any error reporting purposes
	 * @return Obs pojo with all values filled in
	 * @throws HL7Exception if there is a parsing exception
	 * @throws ProposingConceptException if the answer to this obs is a proposed concept
	 * @should add comments to an observation from NTE segments
	 * @should add multiple comments for an observation as one comment
	 * @should add comments to an observation group 
	 */
	private Obs parseObs(Encounter encounter, OBX obx, OBR obr, String uid) throws HL7Exception, ProposingConceptException {
		if (log.isDebugEnabled())
			log.debug("parsing observation: " + obx);
		Varies[] values = obx.getObservationValue();
		
		// bail out if no values were found
		if (values == null || values.length < 1)
			return null;
		
		String hl7Datatype = values[0].getName();
		if (log.isDebugEnabled())
			log.debug("  datatype = " + hl7Datatype);
		Concept concept = getConcept(obx.getObservationIdentifier(), uid);
		if (log.isDebugEnabled())
			log.debug("  concept = " + concept.getConceptId());
		ConceptName conceptName = getConceptName(obx.getObservationIdentifier());
		if (log.isDebugEnabled())
			log.debug("  concept-name = " + conceptName);
		
		Date datetime = getDatetime(obx);
		if (log.isDebugEnabled())
			log.debug("  timestamp = " + datetime);
		if (datetime == null)
			datetime = encounter.getEncounterDatetime();
		
		Obs obs = new Obs();
		obs.setUuid(UUID.randomUUID().toString());
		obs.setPerson(encounter.getPatient());
		obs.setConcept(concept);
		obs.setEncounter(encounter);
		obs.setObsDatetime(datetime);
		obs.setLocation(encounter.getLocation());
		obs.setCreator(encounter.getCreator());
		obs.setDateCreated(new Date());
		
		// set comments if there are any
		StringBuilder comments = new StringBuilder();
		ORU_R01_OBSERVATION parent = (ORU_R01_OBSERVATION) obx.getParent();
		// iterate over all OBX NTEs
		for (int i = 0; i < parent.getNTEReps(); i++)
			for (FT obxComment : parent.getNTE(i).getComment()) {
				if (comments.length() > 0)
					comments.append(" ");
				comments = comments.append(obxComment.getValue());
			}
		// only set comments if there are any
		if (StringUtils.hasText(comments.toString()))
			obs.setComment(comments.toString());
		
		Type obx5 = values[0].getData();
		if ("NM".equals(hl7Datatype)) {
			String value = ((NM) obx5).getValue();
			if (value == null || value.length() == 0) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			} else if (value.equals("0") || value.equals("1")) {
				concept = concept.hydrate(concept.getConceptId().toString());
				obs.setConcept(concept);
				if (concept.getDatatype().isBoolean())
					obs.setValueBoolean(value.equals("1"));
				else if (concept.getDatatype().isNumeric())
					try {
						obs.setValueNumeric(Double.valueOf(value));
					}
					catch (NumberFormatException e) {
						throw new HL7Exception("numeric (NM) value '" + value + "' is not numeric for concept #"
						        + concept.getConceptId() + " (" + conceptName.getName() + ") in message " + uid, e);
					}
				else if (concept.getDatatype().isCoded()) {
					Concept answer = value.equals("1") ? Context.getConceptService().getTrueConcept() : Context
					        .getConceptService().getFalseConcept();
					boolean isValidAnswer = false;
					Collection<ConceptAnswer> conceptAnswers = concept.getAnswers();
					if (conceptAnswers != null && conceptAnswers.size() > 0) {
						for (ConceptAnswer conceptAnswer : conceptAnswers) {
							if (conceptAnswer.getAnswerConcept().equals(answer)) {
								obs.setValueCoded(answer);
								isValidAnswer = true;
								break;
							}
						}
					}
					//answer the boolean answer concept was't found
					if (!isValidAnswer)
						throw new HL7Exception(answer.toString() + " is not a valid answer for obs with uuid " + uid);
				} else {
					//throw this exception to make sure that the handler doesn't silently ignore bad hl7 message
					throw new HL7Exception("Can't set boolean concept answer for concept with id "
					        + obs.getConcept().getConceptId());
				}
			} else {
				try {
					obs.setValueNumeric(Double.valueOf(value));
				}
				catch (NumberFormatException e) {
					throw new HL7Exception("numeric (NM) value '" + value + "' is not numeric for concept #"
					        + concept.getConceptId() + " (" + conceptName.getName() + ") in message " + uid, e);
				}
			}
		} else if ("CWE".equals(hl7Datatype)) {
			log.debug("  CWE observation");
			CWE value = (CWE) obx5;
			String valueIdentifier = value.getIdentifier().getValue();
			log.debug("    value id = " + valueIdentifier);
			String valueName = value.getText().getValue();
			log.debug("    value name = " + valueName);
			if (isConceptProposal(valueIdentifier)) {
				if (log.isDebugEnabled())
					log.debug("Proposing concept");
				throw new ProposingConceptException(concept, valueName);
			} else {
				log.debug("    not proposal");
				try {
					Concept valueConcept = getConcept(value, uid);
					obs.setValueCoded(valueConcept);
					if (HL7Constants.HL7_LOCAL_DRUG.equals(value.getNameOfAlternateCodingSystem().getValue())) {
						Drug valueDrug = new Drug();
						valueDrug.setDrugId(new Integer(value.getAlternateIdentifier().getValue()));
						obs.setValueDrug(valueDrug);
					} else {
						ConceptName valueConceptName = getConceptName(value);
						if (valueConceptName != null) {
							if (log.isDebugEnabled()) {
								log.debug("    value concept-name-id = " + valueConceptName.getConceptNameId());
								log.debug("    value concept-name = " + valueConceptName.getName());
							}
							obs.setValueCodedName(valueConceptName);
						}
					}
				}
				catch (NumberFormatException e) {
					throw new HL7Exception("Invalid concept ID '" + valueIdentifier + "' for OBX-5 value '" + valueName
					        + "'");
				}
			}
			if (log.isDebugEnabled())
				log.debug("  Done with CWE");
		} else if ("CE".equals(hl7Datatype)) {
			CE value = (CE) obx5;
			String valueIdentifier = value.getIdentifier().getValue();
			String valueName = value.getText().getValue();
			if (isConceptProposal(valueIdentifier)) {
				throw new ProposingConceptException(concept, valueName);
			} else {
				try {
					Concept c = getConcept(value, uid);
	
					obs.setValueCoded(c);
					ConceptName name = c.getName();
					obs.setValueCodedName(name);
				}
				catch (NumberFormatException e) {
					throw new HL7Exception("Invalid concept ID '" + valueIdentifier + "' for OBX-5 value '" + valueName
					        + "'");
				}
			}
		} else if ("DT".equals(hl7Datatype)) {
			DT value = (DT) obx5;
			Date valueDate = getDate(value.getYear(), value.getMonth(), value.getDay(), 0, 0, 0);
			if (value == null || valueDate == null) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			obs.setValueDatetime(valueDate);
		} else if ("TS".equals(hl7Datatype)) {
			DTM value = ((TS) obx5).getTime();
			Date valueDate = getDate(value.getYear(), value.getMonth(), value.getDay(), value.getHour(), value.getMinute(),
			    value.getSecond());
			if (value == null || valueDate == null) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			obs.setValueDatetime(valueDate);
		} else if ("TM".equals(hl7Datatype)) {
			TM value = (TM) obx5;
			Date valueTime = getDate(0, 0, 0, value.getHour(), value.getMinute(), value.getSecond());
			if (value == null || valueTime == null) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			obs.setValueDatetime(valueTime);
		} else if ("ST".equals(hl7Datatype)) {
			ST value = (ST) obx5;
			if (value == null || value.getValue() == null || value.getValue().trim().length() == 0) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			obs.setValueText(value.getValue());
		} else {
			// unsupported data type
			// TODO: support RP (report), SN (structured numeric)
			// do we need to support BIT just in case it slips thru?
			throw new HL7Exception("Unsupported observation datatype '" + hl7Datatype + "'");
		}
		
		return obs;
	}
	
	/**
	 * Derive a concept name from the CWE component of an hl7 message.
	 * 
	 * @param cwe
	 * @return
	 * @throws HL7Exception
	 */
	private ConceptName getConceptName(CWE cwe) throws HL7Exception {
		ST altIdentifier = cwe.getAlternateIdentifier();
		ID altCodingSystem = cwe.getNameOfAlternateCodingSystem();
		return getConceptName(altIdentifier, altCodingSystem);
	}
	
	/**
	 * Derive a concept name from the CE component of an hl7 message.
	 * 
	 * @param ce
	 * @return
	 * @throws HL7Exception
	 */
	private ConceptName getConceptName(CE ce) throws HL7Exception {
		ST altIdentifier = ce.getIdentifier();
		ID altCodingSystem = ce.getNameOfAlternateCodingSystem();
		return getConceptName(altIdentifier, altCodingSystem);
	}
	
	/**
	 * Derive a concept name from the CWE component of an hl7 message.
	 * 
	 * @param altIdentifier
	 * @param altCodingSystem
	 * @return
	 */
	private ConceptName getConceptName(ST altIdentifier, ID altCodingSystem) throws HL7Exception {
		if (altIdentifier != null) {
			if (HL7Constants.HL7_LOCAL_CONCEPT_NAME.equals(altCodingSystem.getValue())) {
				String hl7ConceptNameId = altIdentifier.getValue();
				return getConceptName(hl7ConceptNameId);
			}else{
				String hl7ConceptNameId = altIdentifier.getValue();
				return getConceptName(hl7ConceptNameId);
			}
		}
		
		return null;
	}
	
	/**
	 * Utility method to retrieve the openmrs ConceptName specified in an hl7 message observation
	 * segment. This method assumes that the check for 99NAM has been done already and is being
	 * given an openmrs conceptNameId
	 * 
	 * @param hl7ConceptNameId internal ConceptNameId to look up
	 * @return ConceptName from the database
	 * @throws HL7Exception
	 */
	private ConceptName getConceptName(String hl7ConceptNameId) throws HL7Exception {
		ConceptName specifiedConceptName = null;
		if (hl7ConceptNameId != null) {
			// get the exact concept name specified by the id
			try {
				Integer conceptNameId = new Integer(hl7ConceptNameId);
				specifiedConceptName = new ConceptName();
				specifiedConceptName.setConceptNameId(conceptNameId);
			}
			catch (NumberFormatException e) {
				// if it is not a valid number, more than likely it is a bad hl7 message
				log.debug("Invalid concept name ID '" + hl7ConceptNameId + "'", e);
			}
		}
		return specifiedConceptName;
		
	}
	
	private boolean isConceptProposal(String identifier) {
		return OpenmrsUtil.nullSafeEquals(identifier, OpenmrsConstants.PROPOSED_CONCEPT_IDENTIFIER);
	}
	
	private Date getDate(int year, int month, int day, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		// Calendar.set(MONTH, int) is zero-based, Hl7 is not
		cal.set(year, month - 1, day, hour, minute, second);
		return cal.getTime();
	}
	
	/**
	 * Get an openmrs Concept object out of the given hl7 coded element
	 * 
	 * @param codedElement ce to pull from
	 * @param uid unique string for this message for any error reporting purposes
	 * @return new Concept object
	 * @throws HL7Exception if parsing errors occur
	 */
	private Concept getConcept(CE codedElement, String uid) throws HL7Exception {
		String hl7ConceptId = codedElement.getIdentifier().getValue();
		
		String codingSystem = codedElement.getNameOfCodingSystem().getValue().toString();
		Concept concept = Context.getConceptService().getConceptByMapping(hl7ConceptId, codingSystem);

		return concept;
	}
	
	/**
	 * Get an openmrs Concept object out of the given hl7 coded with exceptions element
	 * 
	 * @param codedElement cwe to pull from
	 * @param uid unique string for this message for any error reporting purposes
	 * @return new Concept object
	 * @throws HL7Exception if parsing errors occur
	 */
	private Concept getConcept(CWE codedElement, String uid) throws HL7Exception {
		String hl7ConceptId = codedElement.getIdentifier().getValue();
		
		String codingSystem = codedElement.getNameOfCodingSystem().getValue();
		return getConcept(hl7ConceptId, codingSystem, uid);
	}
	
	/**
	 * Get a concept object representing this conceptId and coding system.<br/>
	 * If codingSystem is 99DCT, then a new Concept with the given conceptId is returned.<br/>
	 * Otherwise, the coding system is looked up in the ConceptMap for an openmrs concept mapped to
	 * that code.
	 * 
	 * @param hl7ConceptId the given hl7 conceptId
	 * @param codingSystem the coding system for this conceptid (e.g. 99DCT)
	 * @param uid unique string for this message for any error reporting purposes
	 * @return a Concept object or null if no conceptId with given coding system found
	 * @should return null if codingSystem not found
	 * @should return a Concept if given local coding system
	 * @should return a mapped Concept if given a valid mapping
	 */
	protected Concept getConcept(String hl7ConceptId, String codingSystem, String uid) throws HL7Exception {
		if (codingSystem == null || HL7Constants.HL7_LOCAL_CONCEPT.equals(codingSystem)) {
			// the concept is local
			try {
				Integer conceptId = new Integer(hl7ConceptId);
				Concept concept = new Concept(conceptId);
				return concept;
			}
			catch (NumberFormatException e) {
				throw new HL7Exception("Invalid concept ID '" + hl7ConceptId + "' in hl7 message with uid: " + uid);
			}
		} else {
			// the concept is not local, look it up in our mapping
			Concept concept = Context.getConceptService().getConceptByMapping(hl7ConceptId, codingSystem);
			if (concept == null){
				log.error("Unable to find concept with code: " + hl7ConceptId + " and mapping: " + codingSystem
				        + " in hl7 message with uid: " + uid);
			}
			return concept;
		}
	}
	
	/**
	 * Pull the timestamp for this obx out. if an invalid date is found, null is returned
	 * 
	 * @param obx the obs to parse and get the timestamp from
	 * @return an obx timestamp or null
	 * @throws HL7Exception
	 * @see {@link #getDatetime(TS)}
	 */
	private Date getDatetime(OBX obx) throws HL7Exception {
		TS ts = obx.getDateTimeOfTheObservation();
		return getDatetime(ts);
	}
	
	/**
	 * Pull the timestamp for this obr out. if an invalid date is found, null is returned
	 * 
	 * @param obr
	 * @return
	 * @throws HL7Exception
	 */
	private Date getDatetime(OBR obr) throws HL7Exception {
		TS ts = obr.getObservationDateTime();
		return getDatetime(ts);
		
	}
	
	/**
	 * Return a java date object for the given TS
	 * 
	 * @param ts TS to parse
	 * @return date object or null
	 * @throws HL7Exception
	 */
	private Date getDatetime(TS ts) throws HL7Exception {
		Date datetime = null;
		DTM value = ts.getTime();
		
		if (value.getYear() == 0 || value.getValue() == null)
			return null;
		
		try {
			datetime = getDate(value.getYear(), value.getMonth(), value.getDay(), value.getHour(), value.getMinute(), value
			        .getSecond());
		}
		catch (DataTypeException e) {

		}
		return datetime;
		
	}
	
	private Date getEncounterDate(OBR obr) throws HL7Exception {
		return tsToDate(obr.getObservationDateTime());
	}
	
	private Provider getProvider(PV1 pv1) throws HL7Exception {
		String providerId = pv1.getAttendingDoctor(0).getIDNumber().getValue();
		Provider providerObject = null;
		
		providerObject = Context.getProviderService().getProviderByIdentifier(providerId);
		
		if (providerObject == null) {
			Person person = service.getPersonByEPID(providerId);
			
			if(person != null) {
				providerObject = new Provider();
				providerObject.setIdentifier(providerId);
				providerObject.setName(pv1.getAttendingDoctor(0).getGivenName().getValue() +" "+ pv1.getAttendingDoctor(0).getFamilyName().getSurname().getValue());
				
			}else{
			providerObject = new Provider();
			providerObject.setIdentifier(providerId);
			providerObject.setName(pv1.getAttendingDoctor(0).getGivenName().getValue() +" "+ pv1.getAttendingDoctor(0).getFamilyName().getSurname().getValue());
			
			}
		}
		Context.getProviderService().saveProvider(providerObject);

		return providerObject;
	}
	
	private Patient getPatient() throws HL7Exception {
		Patient patient;
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByName("ECID");
		List<PatientIdentifierType> identifierTypeList = new ArrayList<PatientIdentifierType>();
		identifierTypeList.add(patientIdentifierType);
		
		List<Patient> patients = Context.getPatientService().getPatients(null, enterpriseId, identifierTypeList, false);
		//I am not checking the identifier type here. Need to come back and add a check for this
		if(patients.size() == 1 ){
			patient = patients.get(0);
		}else{
			throw new HL7Exception("Could not resolve patient");
		}
		return patient;
	}
	
	private Location getLocation(MSH msh, OBR obr) throws HL7Exception {
		String hl7Location = msh.getSendingFacility().getNamespaceID().toString();
		Location location = null;
		
		List<Location> locationsList = Context.getLocationService().getAllLocations();
		for(Location l : locationsList){
			String fosaid = l.getDescription();
			String elid = null;
			
			if(fosaid != null){
				final Matcher matcher = Pattern.compile(":").matcher(fosaid);
				if(matcher.find()){						
				    elid = fosaid.substring(matcher.end()).trim();
				    if(elid.equals(hl7Location)){
				    	location = l;
				    }
				}
			}
		}
		
		if(location == null){
			String locationName = obr.getFillerField2().getValue();
			if(locationName != null){
			location = Context.getLocationService().getLocation(locationName);
				if(location != null){
					location.setDescription("OpenMRS ELID:" + hl7Location );
					Context.getLocationService().saveLocation(location);
				}else{
					location = new Location();
					location.setName(obr.getFillerField2().getValue());
					location.setDescription("OpenMRS ELID:" + hl7Location );
					Context.getLocationService().saveLocation(location);
				}
			}else{
				location = new Location();
				location.setName(obr.getFillerField2().getValue());
				location.setDescription("OpenMRS ELID:" + hl7Location );
				Context.getLocationService().saveLocation(location);
			}
		}
		return location;
	}
	
	private EncounterType getEncounterType(PV1 pv1) {
			String admissionType = pv1.getAdmissionType().getValue();
			EncounterType encounterType = Context.getEncounterService().getEncounterType(admissionType);
			
			if(encounterType == null){
				log.info("EncounterType does not exsist, creating a new one for :" + admissionType);
				EncounterType newEncounterType = new EncounterType();
				newEncounterType.setName(admissionType);
				newEncounterType.setDescription("CREATED BY SHR MODULE");
				
				Context.getEncounterService().saveEncounterType(newEncounterType);
				log.info("Saved newly created encounter type");
				return newEncounterType;
			}else{
				return encounterType;
			}
	}
	
	//TODO: Debug (and use) methods in HL7Util instead
	private Date tsToDate(TS ts) throws HL7Exception {
		// need to handle timezone
		String dtm = ts.getTime().getValue();
		int year = Integer.parseInt(dtm.substring(0, 4));
		int month = (dtm.length() >= 6 ? Integer.parseInt(dtm.substring(4, 6)) - 1 : 0);
		int day = (dtm.length() >= 8 ? Integer.parseInt(dtm.substring(6, 8)) : 1);
		int hour = (dtm.length() >= 10 ? Integer.parseInt(dtm.substring(8, 10)) : 0);
		int min = (dtm.length() >= 12 ? Integer.parseInt(dtm.substring(10, 12)) : 0);
		int sec = (dtm.length() >= 14 ? Integer.parseInt(dtm.substring(12, 14)) : 0);
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, min, sec);
		// if (cal.getTimeZone().getRawOffset() != timeZoneOffsetMillis) {
		// TimeZone tz = (TimeZone)TimeZone.getDefault().clone();
		// tz.setRawOffset(timeZoneOffsetMillis);
		// cal.setTimeZone(tz);
		// }
		return cal.getTime();
	}
	
}

