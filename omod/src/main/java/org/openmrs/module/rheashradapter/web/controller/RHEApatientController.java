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

package org.openmrs.module.rheashradapter.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Source;
import org.openmrs.module.rheashradapter.api.LogEncounterService;
import org.openmrs.module.rheashradapter.api.PatientMergeService;
import org.openmrs.module.rheashradapter.model.GetEncounterLog;
import org.openmrs.module.rheashradapter.model.MatchingEncounters;
import org.openmrs.module.rheashradapter.model.PatientMergeLog;
import org.openmrs.module.rheashradapter.model.PostEncounterLog;
import org.openmrs.module.rheashradapter.model.RequestOutcome;
import org.openmrs.module.rheashradapter.util.GenerateORU_R01;
import org.openmrs.module.rheashradapter.util.HL7Receiver;
import org.openmrs.module.rheashradapter.util.LoggerUtil;
import org.openmrs.module.rheashradapter.util.RHEAErrorCodes;
import org.openmrs.module.rheashradapter.util.RsmsHl7Receiver;
import org.openmrs.module.rheashradapter.util.XmlMessageWriter;
import org.openmrs.serialization.SerializationException;
import org.openmrs.web.WebConstants;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;

@Controller
@RequestMapping(value = "/rest/RHEA/patient/")
public class RHEApatientController {

	private Log log = LogFactory.getLog(this.getClass());
	private HL7Receiver receiver = new HL7Receiver();
	private RsmsHl7Receiver notificationReceiver = new RsmsHl7Receiver();
	private LoggerUtil util = new LoggerUtil();

	@RequestMapping(value = "/identifier", method = RequestMethod.PUT)
	@ResponseBody
    public ResponseEntity<String> mergePatients(@RequestBody String mergeMessage,
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> postUpdateIdentifiers = null;
		Map<String, String> preUpdateIdentifiers = null;
		
		HttpSession httpSession = request.getSession();
		if (Context.isAuthenticated()) {
            NodeList node = identifyMessageType(mergeMessage);
            String typeName = node.item(0).getTextContent();

            postUpdateIdentifiers = identifyPostUpdateIdentifiers(mergeMessage);
            preUpdateIdentifiers = identifyPreUpdateIdentifiers(mergeMessage);

            if (typeName.equals("JOIN")) {
                Object httpResponse = mergePatient(postUpdateIdentifiers, preUpdateIdentifiers);
                response.setStatus((Integer) httpResponse);

                if (response.equals(HttpServletResponse.SC_OK)) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.merged");
                    validatePostidentifiers(postUpdateIdentifiers);

                    return new ResponseEntity<String>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);

                }
            } else if (typeName.equals("LEAVE")) {
                Object httpResponse = restorePatient(postUpdateIdentifiers, preUpdateIdentifiers);
                response.setStatus((Integer) httpResponse);

                if (response.equals(HttpServletResponse.SC_OK)) {
                    httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.restored");
                    validatePostidentifiers(postUpdateIdentifiers);

                    return new ResponseEntity<String>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
            return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
        }

	private void validatePostidentifiers(Map<String, String> validatePostidentifiers){
		
		PatientMergeService service = Context
				.getService(PatientMergeService.class);		
		service.validatePostidentifiers(validatePostidentifiers);
		
	}
	
	private Object mergePatient(Map<String, String> postUpdateIdentifiers, Map<String, String> preUpdateIdentifiers) {
		
		String preUpdateIdentifier = null;
		String postUpdateIdentifier = null;
		
		PatientMergeService service = Context
				.getService(PatientMergeService.class);
		
		Iterator i = preUpdateIdentifiers.entrySet().iterator();
	    while (i.hasNext()) {
	        Map.Entry pairs = (Map.Entry)i.next();
		       if(pairs.getKey().equals("ECID")){
		    	   preUpdateIdentifier = (String) pairs.getValue();
		       }
	    }
	    
		Iterator i2 = postUpdateIdentifiers.entrySet().iterator();
	    while (i2.hasNext()) {
	        Map.Entry pairs = (Map.Entry)i2.next();
		       if(pairs.getKey().equals("ECID")){
		    	   postUpdateIdentifier = (String) pairs.getValue();
		       }
	    }
		
	    if(preUpdateIdentifier == null || postUpdateIdentifier == null){
	    	return HttpServletResponse.SC_NOT_FOUND;
	    }
	    
		String survivingPatientId = postUpdateIdentifier;
		String retiringPatientId = preUpdateIdentifier;
		
		PatientIdentifierType identifierType = Context.getPatientService()
				.getPatientIdentifierTypeByName("ECID");
		
		List<PatientIdentifierType> identifiers = new ArrayList<PatientIdentifierType>();
		identifiers.add(identifierType);
		
		List<Patient> patientsToKeep = Context.getPatientService()
				.getPatients(null, survivingPatientId, identifiers, false);
		List<Patient> patientsToRetire = Context.getPatientService()
				.getPatients(null, retiringPatientId, identifiers, false);
		
		if(patientsToKeep.isEmpty() || patientsToRetire.isEmpty()){
			return HttpServletResponse.SC_NOT_FOUND;
		}else{
		Patient preferred = patientsToKeep.get(0);
		List<Patient> notPreferred = new ArrayList<Patient>();
		
		patientsToRetire = Context.getPatientService()
				.getPatients(null, retiringPatientId, identifiers, false);
		
		notPreferred.add(patientsToRetire.get(0));
		
		try {
			service.mergePatients(preferred, notPreferred);
		} catch (APIException e) {
            log.error(e);
			return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		} catch (SerializationException e) {
            log.error(e);
			return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		}
		
		return HttpServletResponse.SC_OK;
	
	}
	
	private Object restorePatient(Map<String, String> postUpdateIdentifiers, Map<String, String> preUpdateIdentifiers){
		
		
		String preUpdateIdentifier = null;
		String postUpdateIdentifier = null;
		
		PatientMergeService service = Context
				.getService(PatientMergeService.class);
		
		Iterator i = preUpdateIdentifiers.entrySet().iterator();
	    while (i.hasNext()) {
	        Map.Entry pairs = (Map.Entry)i.next();
		       if(pairs.getKey().equals("ECID")){
		    	   preUpdateIdentifier = (String) pairs.getValue();
		       }
	    }
	    
		Iterator i2 = postUpdateIdentifiers.entrySet().iterator();
	    while (i2.hasNext()) {
	        Map.Entry pairs = (Map.Entry)i2.next();
		       if(pairs.getKey().equals("ECID")){
		    	   postUpdateIdentifier = (String) pairs.getValue();
		       }
	    }
		
		String survivingPatientId = postUpdateIdentifier;
		String retiringPatientId = preUpdateIdentifier;
		
		PatientMergeLog log = service.getPatientMergeLog(retiringPatientId, false);
		
		if(log == null){
			return HttpServletResponse.SC_NOT_FOUND;
		}
		
		try{
		service.restorePatients(survivingPatientId, retiringPatientId, log);
		} catch (APIException e) {
			return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		} 
		
		return HttpServletResponse.SC_OK;		
	}
	
	private Map<String,String> identifyPostUpdateIdentifiers(String message) {
		Map<String, String> postUpdateIdentifiers = new HashMap<String,String>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    org.w3c.dom.Document doc = null;
	    XPathExpression expr = null;
	    XPathExpression exprIdType = null;
	    
	    try{
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(new InputSource(new StringReader(message)));
	    	    
	    XPathFactory xFactory = XPathFactory.newInstance();

	    XPath xpath = xFactory.newXPath();
	    expr = xpath.compile("//postUpdateIdentifiers/postUpdateIdentifier/identifier/text()");
	    Object result = expr.evaluate(doc, XPathConstants.NODESET);
	    
	    XPath xpathIdType = xFactory.newXPath();
	    exprIdType = xpathIdType.compile("//postUpdateIdentifiers/postUpdateIdentifier/identifierDomain/universalIdentifierTypeCode/text()");
	    Object resultIdType = exprIdType.evaluate(doc, XPathConstants.NODESET);
	    NodeList nodes = (NodeList) result;
	    NodeList nodesIdType = (NodeList) resultIdType;
	    
	    for (int i=0; i<nodes.getLength();i++){
	    	postUpdateIdentifiers.put(nodesIdType.item(i).getTextContent(), nodes.item(i).getTextContent());
	    }
	    
	    } catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	return postUpdateIdentifiers; 
	}
	
	private Map<String,String> identifyPreUpdateIdentifiers(String message) {
		Map<String, String> preUpdateIdentifiers = new HashMap<String,String>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    org.w3c.dom.Document doc = null;
	    XPathExpression expr = null;
	    XPathExpression exprIdType = null;
	    
	  try{
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(new InputSource(new StringReader(message)));
	    	    
	    XPathFactory xFactory = XPathFactory.newInstance();

	    XPath xpath = xFactory.newXPath();
	    expr = xpath.compile("//preUpdateIdentifiers/preUpdateIdentifier/identifier/text()");
	    Object result = expr.evaluate(doc, XPathConstants.NODESET);
	    
	    XPath xpathIdType = xFactory.newXPath();
	    exprIdType = xpathIdType.compile("//preUpdateIdentifiers/preUpdateIdentifier/identifierDomain/universalIdentifierTypeCode/text()");
	    Object resultIdType = exprIdType.evaluate(doc, XPathConstants.NODESET);
	    NodeList nodes = (NodeList) result;
	    NodeList nodesIdType = (NodeList) resultIdType;
	    
	 
	    
	    for (int i=0; i<nodes.getLength();i++){
	    	preUpdateIdentifiers.put(nodesIdType.item(i).getTextContent(), nodes.item(i).getTextContent());
	    }
	    
	  	} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}			
	return preUpdateIdentifiers; 
	}
	
	private NodeList identifyMessageType(String message) {
		DocumentBuilder db = null;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(message));

		org.w3c.dom.Document doc = null;
		try {
			doc = db.parse(is);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        NodeList nodeList = doc.getElementsByTagName("transition");
        return nodeList;
	}

	@RequestMapping(value = "/encounters", method = RequestMethod.GET)
	@ResponseBody
	public Object getEncounters(
			@RequestParam(value = "patientId", required = true) String patientId,
			@RequestParam(value = "idType", required = true) String idType,
			@RequestParam(value = "encounterUniqueId", required = false) String encounterUniqueId,
			@RequestParam(value = "elid", required = false) String enterpriseLocationIdentifier,
			@RequestParam(value = "dateStart", required = false) String dateStart,
			@RequestParam(value = "dateEnd", required = false) String dateEnd,
			HttpServletRequest request, HttpServletResponse response) {

		LogEncounterService service = Context
				.getService(LogEncounterService.class);
		XmlMessageWriter xmlMessagewriter = new XmlMessageWriter();

		String hl7Msg = null;

		Date fromDate = null;
		Date toDate = null;
		Patient p = null;
		ORU_R01 r01 = null;

		log.info("RHEA Controller call detected...");
		log.info("Enterprise Patient Id is :" + patientId);
		log.info("Enterprise Id type is :" + idType);
		log.info("encounterUniqueId is :" + encounterUniqueId);

		GetEncounterLog getEncounterLog = new GetEncounterLog();
		getEncounterLog.setLogTime(new Date());
		getEncounterLog.setPatientId(patientId);
		getEncounterLog.setEncounterUniqueId(encounterUniqueId);

		// first, we create from and to data objects out of the String
		// parameters

		response.setContentType("text/xml");

		if (!idType.equals("ECID")) { // Later on we may need to manage multiple
										// types of ID's. In such a case, this
										// will become more complex
			log.info(RHEAErrorCodes.INVALID_ID_TYPE);
			getEncounterLog = util.getLogger(getEncounterLog,
					RHEAErrorCodes.INVALID_ID_TYPE,
					RHEAErrorCodes.ID_TYPE_DETAIL,
					RequestOutcome.BAD_REQUEST.getResultType(), null, null,
					null);
			service.saveGetEncounterLog(getEncounterLog);

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			try {
				xmlMessagewriter.parseMessage(response.getWriter(),
						RequestOutcome.BAD_REQUEST.getResultType(),
						RHEAErrorCodes.ID_TYPE_DETAIL);

			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			if (dateStart != null)
				fromDate = format.parse(dateStart);
		} catch (ParseException e) {
			log.info(RHEAErrorCodes.INVALID_START_DATE + dateStart);
			getEncounterLog = util.getLogger(getEncounterLog,
					RHEAErrorCodes.INVALID_START_DATE, null,
					RequestOutcome.BAD_REQUEST.getResultType(), null, null, e);
			service.saveGetEncounterLog(getEncounterLog);

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			try {
				xmlMessagewriter.parseMessage(response.getWriter(),
						RequestOutcome.BAD_REQUEST.getResultType(),
						e.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}

		log.info("fromDate is :" + fromDate);
		getEncounterLog.setDateStart(fromDate);

		try {
			if (dateEnd != null)
				toDate = format.parse(dateEnd);
		} catch (ParseException e) {
			log.info(RHEAErrorCodes.INVALID_END_DATE + dateEnd);

			getEncounterLog = util.getLogger(getEncounterLog,
					RHEAErrorCodes.INVALID_END_DATE, null,
					RequestOutcome.BAD_REQUEST.getResultType(), fromDate, null,
					e);
			service.saveGetEncounterLog(getEncounterLog);

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			try {
				xmlMessagewriter.parseMessage(response.getWriter(),
						RequestOutcome.BAD_REQUEST.getResultType(),
						e.toString());

			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return null;
		}

		log.info("toDate is :" + toDate);
		getEncounterLog.setDateEnd(toDate);

		// Next, we try to retrieve the matching patient object
		if (patientId != null) {
			PatientIdentifierType patientIdentifierType = Context
					.getPatientService().getPatientIdentifierTypeByName(idType);
			List<PatientIdentifierType> identifierTypeList = new ArrayList<PatientIdentifierType>();
			identifierTypeList.add(patientIdentifierType);

			List<Patient> patients = Context.getPatientService().getPatients(
					null, patientId, identifierTypeList, false);
			if (patients.size() == 1) {
				p = patients.get(0);
			} else {
				log.info(RHEAErrorCodes.INVALID_RESULTS + patientId);

				getEncounterLog = util.getLogger(getEncounterLog,
						RHEAErrorCodes.INVALID_RESULTS,
						RHEAErrorCodes.INVALID_RESULTS_DETAIL,
						RequestOutcome.BAD_REQUEST.getResultType(), fromDate,
						toDate, null);
				service.saveGetEncounterLog(getEncounterLog);

				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				try {
					xmlMessagewriter.parseMessage(response.getWriter(),
							RequestOutcome.BAD_REQUEST.getResultType(),
							RHEAErrorCodes.INVALID_RESULTS);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				return null;
			}
		}

		// if the patient doesn't exist, we need to return 400-BAD REQUEST
		if (p != null) {
			log.info("Patient id : " + p.getPatientId() + "was retreived...");

			if (p != null) {
				// get all the encounters for this patient
				List<Encounter> encounterList = Context.getEncounterService()
						.getEncounters(p, null, fromDate, toDate, null, null,
								null, false);

				// if the enconteruniqueId is not null, we can isolate the given
				// encounter

				if (encounterUniqueId != null) {
					Iterator<Encounter> i = encounterList.iterator();
					while (i.hasNext()) {
						if (!i.next().getUuid().equals(encounterUniqueId))
							i.remove();
					}
				}

				if (enterpriseLocationIdentifier != null) {

					getEncounterLog
							.setEnterpriseLocationId(enterpriseLocationIdentifier);

					Iterator<Encounter> i = encounterList.iterator();
					while (i.hasNext()) {

						String elidString = i.next().getLocation()
								.getDescription();
						String elid = null;

						if (elidString != null) {
							final Matcher matcher = Pattern.compile(":")
									.matcher(elidString);
							if (matcher.find()) {
								elid = elidString.substring(matcher.end())
										.trim();
							}
							if (elid != null) {
								if (elid.equals(enterpriseLocationIdentifier)) {
									i.remove();
								}
							}
						}
					}
				}

				log.info("Calling the ORU_R01 parser...");

				SortedSet<MatchingEncounters> encounterSet = new TreeSet<MatchingEncounters>();

				for (Encounter e : encounterList) {
					MatchingEncounters matchingEncounters = new MatchingEncounters();
					matchingEncounters.setGetEncounterLog(getEncounterLog);
					matchingEncounters.setEncounterId(e.getEncounterId());

					encounterSet.add(matchingEncounters);
				}

				if (encounterList.size() > 0)
					getEncounterLog.setResult(RequestOutcome.RESULTS_RETRIEVED
							.getResultType());
				if (encounterList.size() == 0) {
					getEncounterLog.setResult(RequestOutcome.NO_RESULTS
							.getResultType()); // Terrible logging methods !

					getEncounterLog = util.getLogger(getEncounterLog,
							RHEAErrorCodes.INVALID_RESULTS,
							RHEAErrorCodes.INVALID_RESULTS_DETAIL,
							RequestOutcome.NO_RESULTS.getResultType(),
							fromDate, toDate, null);
					service.saveGetEncounterLog(getEncounterLog);

					response.setStatus(HttpServletResponse.SC_OK); // If no
																	// matching
																	// encounters
																	// were
																	// retrived,
																	// we
																	// display
																	// 200. OK.
																	// Is this
																	// correct ?

					return null;
				}

				// Now we will generate the HL7 message

				GenerateORU_R01 R01Util = new GenerateORU_R01();
				try {
					r01 = R01Util.generateORU_R01Message(p, encounterList);
					hl7Msg = R01Util.getMessage(r01);

				} catch (Exception e) {

					getEncounterLog = util.getLogger(getEncounterLog,
							RequestOutcome.BAD_REQUEST.getResultType(), null,
							RequestOutcome.BAD_REQUEST.getResultType(),
							fromDate, toDate, e);
					service.saveGetEncounterLog(getEncounterLog);

					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					try {
						xmlMessagewriter.parseMessage(response.getWriter(),
								RequestOutcome.BAD_REQUEST.getResultType(),
								e.toString());
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					return null;
				}
				getEncounterLog.getMatchingEncounters().clear();
				getEncounterLog.setMatchingEncounters(encounterSet);
			}

			try {
				

				service.saveGetEncounterLog(getEncounterLog);
				response.setStatus(HttpServletResponse.SC_OK);
				
				return hl7Msg;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@RequestMapping(value = "/encounters", method = RequestMethod.POST)
	@ResponseBody
	public Object createEncounters(
			@RequestBody String hl7,
			@RequestParam(value = "patientId", required = true) String patientId,
			@RequestParam(value = "idType", required = true) String idType,
			@RequestParam(value = "notificationType", required = false) String notificationType,
			HttpServletRequest request, HttpServletResponse response) {

		log.info("RHEA HL7 Message Controller call detected...");
		XmlMessageWriter xmlMessagewriter = new XmlMessageWriter();

		Patient patient = null;
		String sourceKey = null;
		Encounter encounter = null;

		LogEncounterService service = Context
				.getService(LogEncounterService.class);
		PostEncounterLog postEncounterLog = new PostEncounterLog();
		postEncounterLog.setPatientId(patientId);
		postEncounterLog.setHl7data(hl7);
		postEncounterLog.setDateCreated(new Date());
		postEncounterLog.setUserId(Context.getUserContext()
				.getAuthenticatedUser().getUserId());

		if (!idType.equals("ECID")) {
			log.info("Error : Invalid ID Type");
			postEncounterLog = util.postLogger(postEncounterLog,
					RequestOutcome.BAD_REQUEST.getResultType(),
					RHEAErrorCodes.ID_TYPE_DETAIL);
			service.savePostEncounterLog(postEncounterLog);

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			try {
				xmlMessagewriter.parseMessage(response.getWriter(),
						RequestOutcome.BAD_REQUEST.getResultType(),
						RHEAErrorCodes.ID_TYPE_DETAIL);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		if (notificationType != null) {
			if (!notificationType.equals("BIR")
					&& !notificationType.equals("MAT")
					&& !notificationType.equals("RISK")) {
				log.info("Error : Invalid notification type");
				postEncounterLog = util.postLogger(postEncounterLog,
						RequestOutcome.BAD_REQUEST.getResultType(),
						RHEAErrorCodes.NOTIFICATION_TYPE_DETAIL);
				service.savePostEncounterLog(postEncounterLog);

				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				try {
					xmlMessagewriter.parseMessage(response.getWriter(),
							RequestOutcome.BAD_REQUEST.getResultType(),
							RHEAErrorCodes.NOTIFICATION_TYPE_DETAIL);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}

		SAXBuilder builder = new SAXBuilder();
		Document document = null;
		try {
			document = builder.build(new ByteArrayInputStream(hl7.getBytes()));
			Element root = document.getRootElement();

			List rows = root.getChildren("MSH");
			Element child = (Element) document.getRootElement().getChildren()
					.get(0);
			rows = child.getChildren();

			for (int i = 0; i < rows.size(); i++) {
				Element row = (Element) rows.get(i);
				if (row.getName().equals("MSH.10")) {
					sourceKey = row.getValue();
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<PatientIdentifierType> identifierTypeList = null;

		if (patientId != null) {
			PatientIdentifierType patientIdentifierType = Context
					.getPatientService().getPatientIdentifierTypeByName("ECID");

			identifierTypeList = new ArrayList<PatientIdentifierType>();
			identifierTypeList.add(patientIdentifierType);

			List<Patient> patients = Context.getPatientService().getPatients(
					null, patientId, identifierTypeList, false);
			// I am not checking the identifier type here. Need to come back and
			// add a check for this
			if (patients.size() == 1) {
				patient = patients.get(0);

				PatientIdentifier identifier = patient
						.getPatientIdentifier("ECID");
				identifier.setPreferred(true);
				Context.getPatientService().savePatient(patient);
			}

		}
		if (patient == null) {
			log.info("The specified patient was not found. A new patient wil be created..");
			String givenName = null;
			String familyName = null;
			String birthDateString = null;
			Date birthDate = null;
			String gender = null;
			
			String nameSpace = "urn:hl7-org:v2xml";

			Element root = document.getRootElement();

			Element pidRows = root
					.getChild("ORU_R01.PATIENT_RESULT",
							Namespace.getNamespace(nameSpace))
					.getChild("ORU_R01.PATIENT",
							Namespace.getNamespace(nameSpace))
					.getChild("PID", Namespace.getNamespace(nameSpace));

			List<Element> rows = pidRows.getChildren();

			for (int i = 0; i < rows.size(); i++) {
				Element row = (Element) rows.get(i);
				if (row.getName().equals("PID.5")) {
					familyName = row
							.getChild("XPN.1",
									Namespace.getNamespace(nameSpace))
							.getChild("FN.1", Namespace.getNamespace(nameSpace))
							.getValue().toString();
					givenName = row
							.getChild("XPN.2",
									Namespace.getNamespace(nameSpace))
							.getValue().toString();
				}
				if (row.getName().equals("PID.7")) {
					birthDateString = row
							.getChild("TS.1", Namespace.getNamespace(nameSpace))
							.getValue().toString();
					
					
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
					try {
						if (birthDateString != null)
							birthDate = format.parse(birthDateString);
					} catch (ParseException e) {
						
					}
				}
				if (row.getName().equals("PID.8")) {
					gender = row.getValue().toString();
				
				}
			}

			patient = new Patient();

			PersonName name = new PersonName();
			name.setGivenName(givenName);
			name.setFamilyName(familyName);

			patient.addName(name);

			PatientIdentifier identifier = new PatientIdentifier();
			identifier.setIdentifier(patientId);

			PatientIdentifierType pIdType = Context.getPatientService()
					.getPatientIdentifierTypeByName(idType);
			if (pIdType == null) {
				pIdType = new PatientIdentifierType();
				pIdType.setName("ECID");
				pIdType.setDescription("Enterprise ID");
				pIdType.setRequired(false);
				Context.getPatientService().savePatientIdentifierType(pIdType);
			}

			identifier.setIdentifierType(Context.getPatientService()
					.getPatientIdentifierTypeByName(idType));
			identifier.setLocation(Context.getLocationService().getLocation(1));
			identifier.setDateCreated(new Date());
			identifier.setVoided(false);
			identifier.setPreferred(true);
			patient.addIdentifier(identifier);

			if(gender != null)
				patient.setGender(gender);
			else
				patient.setGender("N/A");

			patient.setBirthdate(birthDate);

			// Save newly created patient into database
			Context.getPatientService().savePatient(patient);

		}

		log.info("Source key : " + sourceKey);
		// log.info("Source  : " + source);
		log.info("data :" + hl7);
		log.info("enterprise id :" + patientId);

		// For RHEA, should the source be a single static entity ?
		HL7Source hl7Source = Context.getHL7Service().getHL7SourceByName(
				"LOCAL");

		log.info("Creating HL7InQueue object...");

		HL7InQueue hl7InQueue = new HL7InQueue();

		hl7InQueue.setHL7Data(hl7);
		log.info("hl7 message is : " + hl7.toString());

		hl7InQueue.setHL7Source(hl7Source);
		log.info("hl7 source is : " + hl7Source.toString());

		hl7InQueue.setHL7SourceKey(sourceKey);
		log.info("hl7 source key is : " + sourceKey);

		Context.getHL7Service().saveHL7InQueue(hl7InQueue);

		String encId = null;
		try {
			// Call the processor method
			if (notificationType == null) {
				encId = processHL7InQueue(hl7InQueue, patientId);
			} else {
				encId = processNotification(hl7InQueue, patientId);
			}

			postEncounterLog.setResult(RequestOutcome.CREATED.getResultType());
			postEncounterLog.setValid(true);
			service.savePostEncounterLog(postEncounterLog);

			response.setHeader("Location", request.getRequestURL()
					+ "?enterpriseId=" + patientId + "&idType=" + idType
					+ "&encounterId=" + encId);

			encounter = Context.getEncounterService().getEncounter(
					Integer.parseInt(encId));

			response.setStatus(HttpServletResponse.SC_CREATED);
		} catch (HL7Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stackTrace = sw.toString();

			postEncounterLog = util.postLogger(postEncounterLog,
					RequestOutcome.BAD_REQUEST.getResultType(), stackTrace);
			service.savePostEncounterLog(postEncounterLog);

			HL7InError error = new HL7InError(hl7InQueue);
			error.setError("ERROR");
			error.setErrorDetails(ExceptionUtils.getFullStackTrace(e));
			Context.getHL7Service().saveHL7InError(error);

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			try {
				xmlMessagewriter.parseMessage(response.getWriter(),
						RequestOutcome.BAD_REQUEST.getResultType(),
						e.toString());

			} catch (Exception e2) {
				e2.printStackTrace();
			}

			return null;
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stackTrace = sw.toString();

			postEncounterLog = util.postLogger(postEncounterLog,
					RequestOutcome.BAD_REQUEST.getResultType(), stackTrace);
			service.savePostEncounterLog(postEncounterLog);

			HL7InError error = new HL7InError(hl7InQueue);
			error.setError("Exception");
			error.setErrorDetails(ExceptionUtils.getFullStackTrace(e));
			Context.getHL7Service().saveHL7InError(error);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				xmlMessagewriter.parseMessage(response.getWriter(),
						RequestOutcome.SERVER_ERROR.getResultType(),
						e.toString());

			} catch (Exception e2) {
				e2.printStackTrace();
			}

			return null;
		}

		log.info("Returning persisted encounter to the ReferralAlerts module "
				+ encounter.getId());
		return encounter;
	}

	public String processNotification(HL7InQueue hl7InQueue, String enterpriseId)
			throws HL7Exception, Exception {

		log.info("Processing HL7 inbound queue (id="
				+ hl7InQueue.getHL7InQueueId() + ",key="
				+ hl7InQueue.getHL7SourceKey() + ")");

		// Parse the HL7 into an HL7Message or abort with failure
		String hl7Message = hl7InQueue.getHL7Data();

		// Send the inbound HL7 message to our receiver routine for processing
		log.info("Sending HL7 message to HL7 receiver");

		Message response = notificationReceiver.processMessage(hl7Message,
				enterpriseId);

		// Move HL7 inbound queue entry into the archive before exiting
		log.info("Archiving HL7 inbound queue entry");
		HL7InArchive hl7InArchive = new HL7InArchive(hl7InQueue);
		Context.getHL7Service().saveHL7InArchive(hl7InArchive);

		log.info("Removing HL7 message from inbound queue");
		// NOTE : the purging of the HL7Queue is done in theOpenMRS Core
		// (hl7serviceimpl class). DONT call it here.

		// clean up memory after processing each queue entry (otherwise, the
		// memory-intensive process may crash or eat up all our memory)
		Context.getHL7Service().garbageCollect();

		return util.getEncounterId(response);
	}

	public String processHL7InQueue(HL7InQueue hl7InQueue, String enterpriseId)
			throws HL7Exception, Exception {

		log.info("Processing HL7 inbound queue (id="
				+ hl7InQueue.getHL7InQueueId() + ",key="
				+ hl7InQueue.getHL7SourceKey() + ")");

		// Parse the HL7 into an HL7Message or abort with failure
		String hl7Message = hl7InQueue.getHL7Data();

		// Send the inbound HL7 message to our receiver routine for processing
		log.info("Sending HL7 message to HL7 receiver");

		Message response = receiver.processMessage(hl7Message, enterpriseId);

		// Move HL7 inbound queue entry into the archive before exiting
		log.info("Archiving HL7 inbound queue entry");
		HL7InArchive hl7InArchive = new HL7InArchive(hl7InQueue);
		Context.getHL7Service().saveHL7InArchive(hl7InArchive);

		log.info("Removing HL7 message from inbound queue");
		// NOTE : the purging of the HL7Queue is done in theOpenMRS Core
		// (hl7serviceimpl class). DONT call it here.

		// clean up memory after processing each queue entry (otherwise, the
		// memory-intensive process may crash or eat up all our memory)
		Context.getHL7Service().garbageCollect();

		return util.getEncounterId(response);
	}

}
