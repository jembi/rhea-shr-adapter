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

package org.openmrs.module.rheashradapter.model;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The Class GetEncounterLog.
 * POJO class used to log GET Encounter requests. 
 * The following information will be stored per each GET request.
 */
public class GetEncounterLog {
	
	/** The get request id. */
	private Integer getRequestId;
	
	/** The enterprise id. */
	private String patientId;
	
	/** The encounter unique id. */
	private String encounterUniqueId;
	
	/** The enterprise location id. */
	private String enterpriseLocationId;
	
	
	/** The date start. */
	private Date dateStart;
	
	/** The date end. */
	private Date dateEnd;
	
	/** The log time. */
	private Date logTime;
	
	/** The result. */
	private String result;

	/** The error. */
	private String error;
	
	/** The error details. */
	private String errorDetails;
	
	/** The matching encounters. */
	private SortedSet<MatchingEncounters> matchingEncounters;
	
	
    /**
     * Gets the gets the request id.
     *
     * @return the gets the request id
     */
    public Integer getGetRequestId() {
    	return getRequestId;
    }

	
    /**
     * Sets the gets the request id.
     *
     * @param postRequestId the new gets the request id
     */
    public void setGetRequestId(Integer postRequestId) {
    	this.getRequestId = postRequestId;
    }

	
    /**
     * Gets the enterprise id.
     *
     * @return the enterprise id
     */
    public String getPatientId() {
    	return patientId;
    }

	
    /**
     * Sets the enterprise id.
     *
     * @param enterpriseId the new enterprise id
     */
    public void setPatientId(String patientId) {
    	this.patientId = patientId;
    }

	
    /**
     * Gets the encounter unique id.
     *
     * @return the encounter unique id
     */
    public String getEncounterUniqueId() {
    	return encounterUniqueId;
    }
    
    
    /**
     * Sets the encounter unique id.
     *
     * @param encounterUniqueId the new encounter unique id
     */
    public void setEncounterUniqueId(String encounterUniqueId) {
    	this.encounterUniqueId = encounterUniqueId;
    }

	
    /**
     * Gets the enterprise location id.
     *
     * @return the enterprise location id
     */
    public String getEnterpriseLocationId() {
    	return enterpriseLocationId;
    }

	
    /**
     * Sets the enterprise location id.
     *
     * @param enterpriseLocationId the new enterprise location id
     */
    public void setEnterpriseLocationId(String enterpriseLocationId) {
    	this.enterpriseLocationId = enterpriseLocationId;
    }

	
    /**
     * Gets the date start.
     *
     * @return the date start
     */
    public Date getDateStart() {
    	return dateStart;
    }

	
    /**
     * Sets the date start.
     *
     * @param dateStart the new date start
     */
    public void setDateStart(Date dateStart) {
    	this.dateStart = dateStart;
    }

	
    /**
     * Gets the date end.
     *
     * @return the date end
     */
    public Date getDateEnd() {
    	return dateEnd;
    }

	
    /**
     * Sets the date end.
     *
     * @param dateEnd the new date end
     */
    public void setDateEnd(Date dateEnd) {
    	this.dateEnd = dateEnd;
    }
    
    /**
     * Gets the log time.
     *
     * @return the log time
     */
    public Date getLogTime() {
    	return logTime;
    }

	
    /**
     * Sets the log time.
     *
     * @param logTime the new log time
     */
    public void setLogTime(Date logTime) {
    	this.logTime = logTime;
    }
    
    /**
     * Gets the result.
     *
     * @return the result
     */
    public String getResult() {
    	return result;
    }

    /**
     * Sets the result.
     *
     * @param result the new result
     */
    public void setResult(String result) {
    	this.result = result;
    }
    
	/**
	 * Gets the error.
	 *
	 * @return the error
	 */
	public String getError() {
		return error;
	}


	/**
	 * Sets the error.
	 *
	 * @param error the new error
	 */
	public void setError(String error) {
		this.error = error;
	}


	/**
	 * Gets the error details.
	 *
	 * @return the error details
	 */
	public String getErrorDetails() {
		return errorDetails;
	}


	/**
	 * Sets the error details.
	 *
	 * @param errorDetails the new error details
	 */
	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}
	
    /**
     * Gets the matching encounters.
     *
     * @return the matching encounters
     */
    public Set<MatchingEncounters> getMatchingEncounters() {
		if (matchingEncounters == null)
			matchingEncounters = new TreeSet<MatchingEncounters>();
        return matchingEncounters;
    }
    
	
	/**
	 * Sets the matching encounters.
	 *
	 * @param matchingEncounters the new matching encounters
	 */
	public void setMatchingEncounters(Set<MatchingEncounters> matchingEncounters) {
		if (this.matchingEncounters == null) {
			this.matchingEncounters = (SortedSet<MatchingEncounters>) matchingEncounters;
		} else {
			this.matchingEncounters.retainAll(matchingEncounters);
			this.matchingEncounters.addAll(matchingEncounters);
		}
	}
	
}
