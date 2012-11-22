package org.openmrs.module.rheashradapter.web.controller;

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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.rheashradapter.api.LogEncounterService;
import org.openmrs.module.rheashradapter.model.GetEncounterLog;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * The controller for the manage templates jsp
 */

@Controller
public class RheaGetRequestController extends SimpleFormController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@RequestMapping(value = "/module/rheashradapter/getRequestTemplate")
	public void showResultTemplates(ModelMap model,  HttpServletRequest request) {
		GetEncounterLog getEncounterLog = null;
		
		if (Context.isAuthenticated()) {
			LogEncounterService ls = Context.getService(LogEncounterService.class);
			String getRequestId = request.getParameter("getRequestId");
			if (getRequestId != null)
				getEncounterLog = ls.getGetEncounterLog(Integer.valueOf(getRequestId));
		}
		
		model.addAttribute("getEncounterLog", getEncounterLog);
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		
		GetEncounterLog getEncounterLog = (GetEncounterLog) obj;
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (Context.isAuthenticated()) {
			if (getEncounterLog != null)
				map.put("getEncounterLog", getEncounterLog);
		}
		
		return map;
	}
	
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		GetEncounterLog getEncounterLog = null;
		
		if (Context.isAuthenticated()) {
			LogEncounterService ls = Context.getService(LogEncounterService.class);
			String getRequestId = request.getParameter("getRequestId");
			if (getRequestId != null)
				getEncounterLog = ls.getGetEncounterLog(Integer.valueOf(getRequestId));
		}
		
		return getEncounterLog;
	}
}

