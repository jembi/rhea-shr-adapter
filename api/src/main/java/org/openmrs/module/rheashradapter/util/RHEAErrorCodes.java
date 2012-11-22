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

public class RHEAErrorCodes {

	public static final String INVALID_ID_TYPE = "Error : Invalid ID Type";
	
	public static final String ID_TYPE_DETAIL = "ID type not recognized. Currently, RHEA Supports only ID type ECID";
	
	public static final String INVALID_START_DATE = "Error : Invalid Start Date";
	
	public static final String START_DATE_DETAIL = "Start Date Formatted incorrectly. Should be in format 'DD-MM-YYYY'";
	
	public static final String INVALID_END_DATE = "Error : Invalid End Date";
	
	public static final String END_DATE_DETAIL = "End Date Formatted incorrectly. Should be in format 'DD-MM-YYYY'";
	
	public static final String INVALID_RESULTS = "Error : failed to resolve patient";
	
	public static final String INVALID_RESULTS_DETAIL = "Failed to retrieve a unique patient with the given ID";
	
	public static final String INVALID_SOURCE = "Specified source was not found";
	
	public static final String HL7_GENERATION_ERROR = "Erorr generating hl7 message";
	
	public static final String NOTIFICATION_TYPE_DETAIL = "Notification type not recognized. Currently, RHEA supports only notification types RISK, MAT and BIR";
}
