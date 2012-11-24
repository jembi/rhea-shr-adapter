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
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.rheashradapter.model.GetEncounterLog;
import org.openmrs.module.rheashradapter.model.PostEncounterLog;


/**
 * The Class LogEncounterDAOImpl.
 * DAO layer class which logs GET and POST requests made on the SHR Adapter module
 */

public class LogEncounterDAOImpl implements LogEncounterDAO{

	/** The session factory. */
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
	public LogEncounterDAOImpl() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.jembi.rhea.hibernate.LogEncounterDAO#saveGetEncounterLog(org.jembi.rhea.module.model.GetEncounterLog)
	 */
	public void saveGetEncounterLog(GetEncounterLog getEncounterLog) {
		sessionFactory.getCurrentSession().saveOrUpdate(getEncounterLog);
    }
	
	/* (non-Javadoc)
	 * @see org.jembi.rhea.hibernate.LogEncounterDAO#savePostEncounterLog(org.jembi.rhea.module.model.PostEncounterLog)
	 */
	public void savePostEncounterLog(PostEncounterLog postEncounterLog) {
		sessionFactory.getCurrentSession().saveOrUpdate(postEncounterLog);
    }
	
	/* (non-Javadoc)
	 * @see org.jembi.rhea.hibernate.LogEncounterDAO#getPersonByEPID(java.lang.String)
	 */
	public Person getPersonByEPID(String EPID){
		List<Person> candidates = new ArrayList<Person>();
		PersonAttributeType pat = Context.getPersonService().getPersonAttributeTypeByName("EPID");
		if(pat == null){
			pat = new PersonAttributeType();
			pat.setName("EPID");
			pat.setDescription("Enterprise Provider ID");
			Context.getPersonService().savePersonAttributeType(pat);
			
		}
		
		Query query = sessionFactory.getCurrentSession().createQuery("select p.person from PersonAttribute p where value = :code and p.attributeType.personAttributeTypeId = :attr_id ");
		query.setParameter("code", EPID);
		query.setParameter("attr_id", pat.getPersonAttributeTypeId());
		
		if(query.list() != null){
		candidates = (List<Person>)query.list();
		if(candidates.size() != 0){
			return candidates.get(0);
			}
		} else{
			return null;
		}return null;
	}
	
	
	public String getPersonAttributesByPerson(Person p, String EPID) {
		List<String> candidates = new ArrayList<String>();
		PersonAttributeType pat = Context.getPersonService()
				.getPersonAttributeTypeByName(EPID);

		if (pat == null) {
			return null;
		}

		Query query = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select p.value from PersonAttribute p where p.attributeType.personAttributeTypeId = :attr_id and p.person = :pers");
		query.setParameter("attr_id", pat.getPersonAttributeTypeId());
		query.setParameter("pers", p);

		if (query.list() != null) {

			candidates = (List<String>) query.list();
			if (candidates.size() != 0) {
				String x = candidates.get(0);
				return x;
			}
		} else {
			return null;
		}
		return null;
	}

	@Override
	public List<GetEncounterLog> getGetEncounterLogs() {
		Criteria c = sessionFactory.getCurrentSession().createCriteria(GetEncounterLog.class);
		return c.list();
	}
	
	@Override
	public List<PostEncounterLog> getPostEncounterLogs() {
		List<PostEncounterLog> logs;
		logs = sessionFactory.getCurrentSession().createCriteria(PostEncounterLog.class).list();
		return logs;
	}

	@Override
	public GetEncounterLog getGetEncounterLog(Integer getRequestId) {
		return (GetEncounterLog) sessionFactory.getCurrentSession().get(GetEncounterLog.class, getRequestId);

	}

	@Override
	public PostEncounterLog getPostEncounterLog(Integer postRequestId) {
		return (PostEncounterLog) sessionFactory.getCurrentSession().get(PostEncounterLog.class, postRequestId);

	}
}
