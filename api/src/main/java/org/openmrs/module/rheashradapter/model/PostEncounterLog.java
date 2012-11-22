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


/**
 * The Class PostEncounterLog.
 * POJO class used to log POST Encounter requests. 
 * The following information will be stored per each POST request.
 */
public class PostEncounterLog {
	
	/** The post request id. */
	private Integer postRequestId;
	
	/** The enterprise id. */
	private String patientId;
	
	/** The hl7data. */
	private String hl7data;
	
	/** The valid. */
	private boolean valid;
	
	/** The date created. */
	private Date dateCreated;
	
	/** The user id. */
	private Integer userId;
	
	/** The result. */
	private String result;
	
	/** The error. */
	private String error;

	/**
	 * Gets the post request id.
	 *
	 * @return the post request id
	 */
	public Integer getPostRequestId() {
    	return postRequestId;
    }

	
    /**
     * Sets the post request id.
     *
     * @param createRequestId the new post request id
     */
    public void setPostRequestId(Integer createRequestId) {
    	this.postRequestId = createRequestId;
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
     * Gets the hl7data.
     *
     * @return the hl7data
     */
    public String getHl7data() {
    	return hl7data;
    }

	
    /**
     * Sets the hl7data.
     *
     * @param hl7data the new hl7data
     */
    public void setHl7data(String hl7data) {
    	this.hl7data = hl7data;
    }

    /**
     * Checks if is valid.
     *
     * @return true, if is valid
     */
    public boolean isValid() {
    	return valid;
    }
	
    /**
     * Sets the valid.
     *
     * @param valid the new valid
     */
    public void setValid(boolean valid) {
    	this.valid = valid;
    }
	
    /**
     * Gets the date created.
     *
     * @return the date created
     */
    public Date getDateCreated() {
    	return dateCreated;
    }

	
    /**
     * Sets the date created.
     *
     * @param dateCreated the new date created
     */
    public void setDateCreated(Date dateCreated) {
    	this.dateCreated = dateCreated;
    }
    
    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public Integer getUserId() {
		return userId;
	}


	/**
	 * Sets the user id.
	 *
	 * @param userId the new user id
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
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

	
}
