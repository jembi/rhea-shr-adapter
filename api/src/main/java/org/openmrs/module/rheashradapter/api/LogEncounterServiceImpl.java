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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Person;
import org.openmrs.module.rheashradapter.hibernate.LogEncounterDAO;
import org.openmrs.module.rheashradapter.model.GetEncounterLog;
import org.openmrs.module.rheashradapter.model.PostEncounterLog;


/**
 * The Class LogEncounterServiceImpl.
 * Service class used to manage GET and POST logging
 */
public class LogEncounterServiceImpl implements LogEncounterService{
	
	/** The log. */
	protected final Log log = LogFactory.getLog(this.getClass());
	
	/** The log encounter dao. */
	private LogEncounterDAO logEncounterDAO;
	
	/** The session factory. */
	private SessionFactory sessionFactory;
	
	/**
	 * Instantiates a new log encounter service impl.
	 */
	public LogEncounterServiceImpl(){
		super();
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
	 * @param sessionFactory the new session factory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * Sets the log encounter dao.
	 *
	 * @param logEncounterDAO the new log encounter dao
	 */
	public void setLogEncounterDAO(LogEncounterDAO logEncounterDAO) {
		this.logEncounterDAO = logEncounterDAO;
	}
	
	/**
	 * Gets the log encounter dao.
	 *
	 * @return the log encounter dao
	 */
	public LogEncounterDAO getLogEncounterDAO() {
		return logEncounterDAO;
	}
	
	/* (non-Javadoc)
	 * @see org.jembi.rhea.api.LogEncounterService#saveGetEncounterLog(org.jembi.rhea.module.model.GetEncounterLog)
	 */
	public void saveGetEncounterLog(GetEncounterLog getEncounterLog) {
		logEncounterDAO.saveGetEncounterLog(getEncounterLog);
    }
	
	/* (non-Javadoc)
	 * @see org.jembi.rhea.api.LogEncounterService#savePostEncounterLog(org.jembi.rhea.module.model.PostEncounterLog)
	 */
	public void savePostEncounterLog(PostEncounterLog postEncounterLog) {
		logEncounterDAO.savePostEncounterLog(postEncounterLog);
    }

	public void onShutdown() {
		// TODO Auto-generated method stub
		
	}

	public void onStartup() {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.jembi.rhea.api.LogEncounterService#getPersonByEPID(java.lang.String)
	 */
	public Person getPersonByEPID(String EPID){
		return logEncounterDAO.getPersonByEPID(EPID);
	}

	public String getPersonAttributesByPerson(Person p, String EPID) {
		return logEncounterDAO.getPersonAttributesByPerson(p, EPID);
	}

	@Override
	public List<GetEncounterLog> getGetEncounterLogs() {
		return logEncounterDAO.getGetEncounterLogs();
	}
	
	@Override
	public List<PostEncounterLog> getPostEncounterLogs() {
		return logEncounterDAO.getPostEncounterLogs();
	}

	@Override
	public GetEncounterLog getGetEncounterLog(Integer getRequestId) {
		return logEncounterDAO.getGetEncounterLog(getRequestId);
	}

	@Override
	public PostEncounterLog getPostEncounterLog(Integer postRequestId) {
		return logEncounterDAO.getPostEncounterLog(postRequestId);
	}
}
