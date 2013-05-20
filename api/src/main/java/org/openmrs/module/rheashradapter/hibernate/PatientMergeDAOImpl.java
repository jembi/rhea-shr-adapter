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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.rheashradapter.model.MergedDataObject;
import org.openmrs.module.rheashradapter.model.PatientMergeLog;
import org.openmrs.module.rheashradapter.model.PatientRestoreRecord;
import org.openmrs.person.PersonMergeLog;
import org.openmrs.serialization.SerializationException;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientMergeDAOImpl implements PatientMergeDAO {

	
	/** The session factory. */
	@Autowired
	private SessionFactory sessionFactory;
	
	/**
	 * Sets the session factory.
	 *
	 * @param sessionFactory the new session factory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
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
	 * Instantiates a new log encounter dao impl.
	 */
	public PatientMergeDAOImpl() {
		super();
	}


	
	public Patient getRetiredPatient(int patientId){
		
		List<Patient> candidates = new ArrayList<Patient>();
		
		Query query = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select p from Patient p where p.patientId = :attr_id");
		query.setParameter("attr_id", patientId);

		if (query.list() != null) {

			candidates = (List<Patient>) query.list();
			if (candidates.size() != 0) {
				Patient p = candidates.get(0);
				return p;
			}
		} else {
			return null;
		}
		return null;
		
	}
	
	@Override
	public PatientMergeLog getPatientMergeLog(String retiredPatient, Boolean isRestored) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientMergeLog.class)
			    .add(Restrictions.eq("retiredPatient", retiredPatient))
				.add(Restrictions.eq("flag", isRestored));
		return (PatientMergeLog) criteria.list().get(0);
	}

	@Override
	public void savePatientRestore(PatientRestoreRecord patientRestoreRecord) {
		this.getSessionFactory().getCurrentSession().saveOrUpdate(patientRestoreRecord);

		
	}
	
	@Override
	public List<MergedDataObject> getMergedDataObjects(int id) {
List<MergedDataObject> candidates = new ArrayList<MergedDataObject>();
		Query query = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select p from MergedDataObject p where p.patientMergeRecord.mergeRecordId = :attr_id");
		query.setParameter("attr_id", id);

		if (query.list() != null) {

			candidates = (List<MergedDataObject>) query.list();
			if (candidates.size() != 0) {
				return candidates;
			}
		} else {
			return null;
		}
		return null;		
	}
	
	@Override
	public boolean validateRequest(String survivingPatient, String retiringPatient) {
		List<PatientMergeLog> patientMergeRecords = new ArrayList<PatientMergeLog>();
		Query query = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select p from PatientMergeLog p where p.survivingPatientId = :idOne AND p.survivingPatientId = :idTwo");
		query.setParameter("idOne", survivingPatient);
		query.setParameter("idTwo", retiringPatient);

		if (query.list() != null) {

			patientMergeRecords = (List<PatientMergeLog>) query.list();
			if (patientMergeRecords.size() == 1) {
				return true;
			}
		} else {
			return false;
		}
		return false;	
	}

	@Override
	public void savePatientMergeLog(PatientMergeLog patientMergeLog) throws SerializationException, APIException {
		//verify required fields
		if (Context.getSerializationService().getDefaultSerializer() == null)
			throw new APIException(
			        "A default serializer was not found. Cannot proceed without at least one installed serializer");
		/*log.debug("Auditing merging of non-preferred person " + patientMergeLog.getLooser().getUuid()
		        + " with preferred person " + patientMergeLog.getWinner().getId());*/
		//populate the mergedData XML from the PersonMergeLogData object
		String serialized = Context.getSerializationService().getDefaultSerializer().serialize(
				patientMergeLog.getPersonMergeLogData());
		patientMergeLog.setSerializedMergedData(serialized);
		/*log.debug(serialized);*/
		//save the bean to the database

		this.getSessionFactory().getCurrentSession().saveOrUpdate(patientMergeLog);
				
	}

	
}
