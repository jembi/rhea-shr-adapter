package org.openmrs.module.rheashradapter.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.module.rheashradapter.hibernate.LogEncounterDAO;
import org.openmrs.module.rheashradapter.hibernate.PatientMergeDAO;
import org.openmrs.module.rheashradapter.model.MergedDataObject;
import org.openmrs.module.rheashradapter.model.PatientRestoreRecord;
import org.openmrs.module.rheashradapter.model.PersonMergeLogData;
import org.openmrs.module.rheashradapter.model.RestoredDataObject;
import org.openmrs.module.rheashradapter.model.PatientMergeLog;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientMergeServiceImpl implements PatientMergeService {

	/** The log. */
	protected final Log log = LogFactory.getLog(this.getClass());

	/** The log encounter dao. */
	private PatientMergeDAO patientMergeDAO;

	/** The session factory. */
	@Autowired
	private SessionFactory sessionFactory;

	private LogEncounterDAO logEncounterDAO;

	/**
	 * Instantiates a new log encounter service impl.
	 */
	public PatientMergeServiceImpl() {
		super();
	}

	public void onShutdown() {

	}

	public void onStartup() {

	}
	
	public void mergePatients(Patient preferred, List<Patient> notPreferred) throws APIException, SerializationException {
		for (Patient nonPreferred : notPreferred) {
			mergePatients(preferred, nonPreferred);
		}
	}
	
	public void validatePostidentifiers(Map<String, String> postidentifiers){
		
		String ecidNo = null;
		List<Patient> patientsToKeep = null;
		Patient patient = null;
		
		Iterator i = postidentifiers.entrySet().iterator();
	    while (i.hasNext()) {
	        Map.Entry pairs = (Map.Entry)i.next();
		       if(pairs.getKey().equals("ECID")){
		    	   ecidNo = (String) pairs.getValue();
		    	   break;
		       }
	    }
	    
	    if(ecidNo != null){
			PatientIdentifierType identifierType = Context.getPatientService()
					.getPatientIdentifierTypeByName("ECID");
			
			List<PatientIdentifierType> identifiers = new ArrayList<PatientIdentifierType>();
			identifiers.add(identifierType);
			
			patientsToKeep = Context.getPatientService()
					.getPatients(null, ecidNo, identifiers, false);
	    }
	    
	    String idNo;
	    String idType;
	    if(patientsToKeep != null){
	    	patient = patientsToKeep.get(0);
	    	
	    	Iterator i2 = postidentifiers.entrySet().iterator();
	    	List<Patient> patientsToConsider = null;
		    while (i2.hasNext()) {
		        Map.Entry pairs = (Map.Entry)i2.next();
			       if(!pairs.getKey().equals("ECID")){
			    	   idNo = (String) pairs.getValue();
			    	   idType = (String) pairs.getKey();
			    	   
			    	   PatientIdentifierType identifierType = Context.getPatientService()
								.getPatientIdentifierTypeByName(idType);
						
						if(identifierType == null){
							identifierType = new PatientIdentifierType();
							identifierType.setName(idType);							
							identifierType.setDescription("Created by OpenEMPI Request");
							
							Context.getPatientService().savePatientIdentifierType(identifierType);
						}
						
						PatientIdentifier identifier = patient.getPatientIdentifier(idType);
						
						if(identifier == null){
							identifier = new PatientIdentifier();
							identifier.setIdentifier(idNo);
							identifier.setIdentifierType(identifierType);
							identifier.setLocation(Context.getLocationService().getLocation(1));
							identifier.setCreator(Context.getAuthenticatedUser());
							identifier.setDateCreated(new Date());
							identifier.setPreferred(false);
							identifier.setPatient(patient);
							Context.getPatientService().savePatientIdentifier(identifier);
						}
			    	   
			       }
		    }	
	    }  
	}

	public PatientMergeLog mergePatients(Patient preferred, Patient notPreferred) throws APIException, SerializationException {
		
		PatientIdentifier preferredPatientECID = preferred.getPatientIdentifier("ECID");
		String survivingPatientECID = preferredPatientECID.getIdentifier();

		PatientIdentifier notPreferredPatientECID = notPreferred.getPatientIdentifier("ECID");
		String retiredPatientECID = notPreferredPatientECID.getIdentifier();		
		
		log.debug("Merging patients: (preferred)" + preferred.getPatientId() + ", (notPreferred) "
		        + notPreferred.getPatientId());
		if (preferred.getPatientId().equals(notPreferred.getPatientId())) {
			log.debug("Merge operation cancelled: Cannot merge user" + preferred.getPatientId() + " to self");
			throw new APIException("Merge operation cancelled: Cannot merge user " + preferred.getPatientId() + " to self");
		}
		
		PersonMergeLogData mergedData = new PersonMergeLogData();
		
		mergeVisits(preferred, notPreferred, mergedData);
		Set<MergedDataObject> mergedDataObjects = mergeEncounters(preferred, notPreferred, mergedData); 
		mergeProgramEnrolments(preferred, notPreferred, mergedData);
		mergedDataObjects = mergeObservationsNotContainedInEncounters(preferred, notPreferred, mergedData, mergedDataObjects);
		mergeOrdersNotContainedInEncounters(preferred, notPreferred, mergedData);
		
		// void the non preferred patient
		Context.getPatientService().voidPatient(notPreferred, "Merged with patient #" + preferred.getPatientId());
		
		// void the person associated with not preferred patient
		Context.getPersonService().voidPerson(notPreferred,
		    "The patient corresponding to this person has been voided and Merged with patient #" + preferred.getPatientId());
		
		// associate the Users associated with the not preferred person, to the preferred person.
		//changeUserAssociations(preferred, notPreferred, mergedData);
		
		// Save the newly update preferred patient
		// This must be called _after_ voiding the nonPreferred patient so that
		//  a "Duplicate Identifier" error doesn't pop up.
				
		PatientIdentifierType pid = Context.getPatientService().getPatientIdentifierTypeByName("ECID");

		for(PatientIdentifier pi : preferred.getPatientIdentifiers(pid)){			
			pi.setPreferred(true);
		}
				
		Context.getPatientService().savePatient(preferred);
		//savePatient(preferred);
		
		PatientMergeLog patientMergeLog = new PatientMergeLog();
		patientMergeLog.setWinner(preferred);
		patientMergeLog.setRetiredPatient(retiredPatientECID);
		patientMergeLog.setSurvivingPatient(survivingPatientECID);
		patientMergeLog.setLooser(notPreferred);
		patientMergeLog.setCreatedDate(new Date());
		patientMergeLog.setCreatedUserId(Context.getAuthenticatedUser().getUserId());
		patientMergeLog.setPersonMergeLogData(mergedData);
		patientMergeLog.setFlag(false);
		patientMergeDAO.savePatientMergeLog(patientMergeLog);
		
		return null;

	}


	private Set<MergedDataObject> mergeObservationsNotContainedInEncounters(Patient preferred, Patient notPreferred,
	        PersonMergeLogData mergedData, Set<MergedDataObject> mergedDataObjects) {
		
		if(mergedDataObjects == null){
			mergedDataObjects = new TreeSet<MergedDataObject>();
		}
		
		// move all obs that weren't contained in encounters
		// TODO: this should be a copy, not a move
		ObsService obsService = Context.getObsService();
		for (Obs obs : obsService.getObservationsByPerson(notPreferred)) {
			if (obs.getEncounter() == null && !obs.isVoided()) {
				MergedDataObject mdo = new MergedDataObject();
				mdo.setEncounterId(null);
				mdo.setObsId(obs.getId());
				
				obs.setPerson(preferred);
				Obs persisted = obsService.saveObs(obs, "Merged from patient #" + notPreferred.getPatientId());
				mergedData.addMovedIndependentObservation(persisted.getUuid());
				//mergedDataObjects.add(mdo);
			}
		}
		
		return mergedDataObjects;
	}
	
	private void mergeOrdersNotContainedInEncounters(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// copy all orders that weren't contained in encounters
		OrderService os = Context.getOrderService();
		for (Order o : os.getOrdersByPatient(notPreferred)) {
			if (o.getEncounter() == null && !o.getVoided()) {
				Order tmpOrder = o.copy();
				tmpOrder.setPatient(preferred);
				Order persisted = os.saveOrder(tmpOrder);
				mergedData.addCreatedOrder(persisted.getUuid());
			}
		}
	}
	
	private void mergeProgramEnrolments(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// copy all program enrollments
		ProgramWorkflowService programService = Context.getProgramWorkflowService();
		for (PatientProgram pp : programService.getPatientPrograms(notPreferred, null, null, null, null, null, false)) {
			if (!pp.getVoided()) {
				PatientProgram enroll = pp.copy();
				enroll.setPatient(preferred);
				log.debug("Copying patientProgram " + pp.getPatientProgramId() + " to " + preferred.getPatientId());
				PatientProgram persisted = programService.savePatientProgram(enroll);
				mergedData.addCreatedProgram(persisted.getUuid());
			}
		}
	}
	
	private void mergeVisits(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		// move all visits, including voided ones (encounters will be handled below)
		//TODO: this should be a copy, not a move
		
		VisitService visitService = Context.getVisitService();
		
		for (Visit visit : visitService.getVisitsByPatient(notPreferred, true, true)) {
			if (log.isDebugEnabled()) {
				log.debug("Merging visit " + visit.getVisitId() + " to " + preferred.getPatientId());
			}
			visit.setPatient(preferred);
			Visit persisted = visitService.saveVisit(visit);
			mergedData.addMovedVisit(persisted.getUuid());
		}
	}
	
	private Set<MergedDataObject> mergeEncounters(Patient preferred, Patient notPreferred, PersonMergeLogData mergedData) {
		Set<MergedDataObject> mergedDataObjects = new TreeSet<MergedDataObject>();
		// change all encounters. This will cascade to obs and orders contained in those encounters
		// TODO: this should be a copy, not a move
		EncounterService es = Context.getEncounterService();
		for (Encounter e : es.getEncounters(notPreferred, null, null, null, null, null, null, null, null, true)) {
			
			for(Obs obs: e.getAllObs()){
				MergedDataObject mdo = new MergedDataObject();
				mdo.setEncounterId(e.getEncounterId());
				mdo.setObsId(obs.getObsId());
				mergedData.addMovedObservations(e.getUuid(), obs.getUuid());
				mergedDataObjects.add(mdo);
			}
			
			e.setPatient(preferred);
			log.debug("Merging encounter " + e.getEncounterId() + " to " + preferred.getPatientId());
			Encounter persisted = es.saveEncounter(e);
			mergedData.addMovedEncounter(persisted.getUuid());
		}
		return mergedDataObjects;
		
	}
	
	/**
	 * Gets the session factory.
	 * 
	 * @return the session factory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Sets the session factory.
	 * 
	 * @param sessionFactory
	 *            the new session factory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setPatientMergeDAO(PatientMergeDAO patientMergeDAO) {
		this.patientMergeDAO = patientMergeDAO;
	}

	public PatientMergeDAO getpatientMergeDAO() {
		return patientMergeDAO;
	}

	@Override
	public String restorePatients(String patientIdentifierType,
			String restorePatient, PatientMergeLog log) {
		
		try {
			log = deserialize(log);
		} catch (SerializationException e) {
			e.printStackTrace();
		}
		
		Person retiredPerson = log.getLooser();
		
		System.out.println(retiredPerson);	
		System.out.println(retiredPerson.getGivenName());
		
		PersonMergeLogData logData = log.getPersonMergeLogData();
		
		Map<String, String> data = logData.getMovedObservations(); 
		
		Patient patient = log.getLooser();
		patient.setVoided(false);
				
		PatientIdentifierType type = Context.getPatientService().getPatientIdentifierTypeByName("ECID");
		
		List<PatientIdentifier> identifiers = new Vector<PatientIdentifier>();
		identifiers.addAll(patient.getIdentifiers());
		
		try{
		Set<PersonName> names = patient.getNames();
		
		Iterator i =names.iterator();
		
		 while (i.hasNext()) {
			 PersonName name = (PersonName) i.next();
			 name.setVoided(false);
		 }
		 
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(PatientIdentifier pi : identifiers){
			pi.setVoided(false);
			if(pi.getIdentifierType().equals(type)){
				pi.setPreferred(true);
			}
			
			Context.getPatientService().updatePatient(patient);
		}
		
		Iterator i = data.entrySet().iterator();
	    while (i.hasNext()) {
	        Map.Entry pairs = (Map.Entry)i.next();
		      Encounter encounter = Context.getEncounterService().getEncounterByUuid((String) pairs.getKey());
		      
		        encounter.setPatient(log.getLooser());
				Context.getEncounterService().saveEncounter(encounter);
		       
	    }
	    	    
	    List<String> obsString = logData.getMovedIndependentObservations();
	    if(obsString != null){
	    for(String obsId : obsString){
	    	Obs obs = Context.getObsService().getObsByUuid(obsId);
	    	Context.getObsService().saveObs(obs, "Restored from patient #" + log.getWinner().getPatientId());
	    }
	    }
	    	    
	   log.setRestoredDate(new Date());
	   log.setRestoredUserId(Context.getAuthenticatedUser().getUserId());
	   log.setFlag(true);
	   try {
		patientMergeDAO.savePatientMergeLog(log);
	} catch (APIException e) {
		e.printStackTrace();
	} catch (SerializationException e) {
		e.printStackTrace();
	}
	    
		return null;
		
	}
	
	private PatientMergeLog deserialize(PatientMergeLog personMergeLog) throws SerializationException {
		PersonMergeLogData data = Context.getSerializationService().getDefaultSerializer().deserialize(
		    personMergeLog.getSerializedMergedData(), PersonMergeLogData.class);
			personMergeLog.setPersonMergeLogData(data);
			
			return personMergeLog;
	}

	@Override
	public PatientMergeLog getPatientMergeLog(String retiringPatientId){
			return patientMergeDAO.getPatientMergeLog(retiringPatientId);
	}
}
