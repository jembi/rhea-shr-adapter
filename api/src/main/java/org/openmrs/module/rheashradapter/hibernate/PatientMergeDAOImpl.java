package org.openmrs.module.rheashradapter.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.module.rheashradapter.model.PatientMergeRecord;
import org.openmrs.module.rheashradapter.model.PatientRestoreRecord;
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


	public void savePatientMergeRecord(PatientMergeRecord patientMergeRecord) {
		this.getSessionFactory().getCurrentSession().saveOrUpdate(patientMergeRecord);
		
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
	public PatientMergeRecord getPatientMergeRecord(String retiredPatient) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientMergeRecord.class)
			    .add(Restrictions.eq("retiredPatientId", retiredPatient));
		
		return (PatientMergeRecord) criteria.list().get(0);
	}

	@Override
	public void savePatientRestore(PatientRestoreRecord patientRestoreRecord) {
		this.getSessionFactory().getCurrentSession().saveOrUpdate(patientRestoreRecord);

		
	}
	
}
