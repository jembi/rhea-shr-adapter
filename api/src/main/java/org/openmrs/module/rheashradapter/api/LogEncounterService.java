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

import org.openmrs.Person;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.rheashradapter.model.GetEncounterLog;
import org.openmrs.module.rheashradapter.model.PostEncounterLog;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Interface LogEncounterService.
 * Service layer interface used for logging requests
 */

@Transactional
public interface LogEncounterService extends OpenmrsService {
	
	/**
	 * Save get encounter log.
	 *
	 * @param getEncounterLog the get encounter log
	 */
	public void saveGetEncounterLog(GetEncounterLog getEncounterLog);
	
	/**
	 * Save post encounter log.
	 *
	 * @param postEncounterLog the post encounter log
	 */
	public void savePostEncounterLog(PostEncounterLog postEncounterLog);
	
	
	public List<GetEncounterLog> getGetEncounterLogs();
	
	public List<PostEncounterLog> getPostEncounterLogs();
	/**
	 * Gets the person by epid.
	 *
	 * @param EPID the epid
	 * @return the person by epid
	 */
	public Person getPersonByEPID(String EPID);
	
	/**
	 * 
	 * @param p
	 * @param EPID
	 * @return
	 */
	public String getPersonAttributesByPerson(Person p, String EPID);
	
	public GetEncounterLog getGetEncounterLog(Integer getRequestId);
	
	public PostEncounterLog getPostEncounterLog(Integer postRequestId);
}
