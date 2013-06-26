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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.module.rheashradapter.api.LogEncounterService;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

public class GenerateORU_R01Alert implements Serializable {

	private Log log = LogFactory.getLog(this.getClass());

	public static String username = Context.getAdministrationService().getGlobalProperty("rheashradapter.hie.username");
	public static String password = Context.getAdministrationService().getGlobalProperty("rheashradapter.hie.password");
	public static String keystorePassword = Context.getAdministrationService().getGlobalProperty("rheashradapter.keystore.password");


	private LogEncounterService service = Context
			.getService(LogEncounterService.class);
	
	public static String hostname = Context.getAdministrationService().getGlobalProperty("rheashradapter.hie.url");

	public static SSLSocketFactory sslFactory;

	private static final long serialVersionUID = 1L;
	private ORU_R01 r01 = new ORU_R01();

	public ORU_R01 generateORU_R01Message(Encounter encounter) throws Exception {	
		
		MSH msh = r01.getMSH();

		// Get current date
		String dateFormat = "yyyyMMddHHmmss";
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		String formattedDate = formatter.format(new Date());

		msh.getFieldSeparator().setValue(RHEAHL7Constants.FIELD_SEPARATOR);//
		msh.getEncodingCharacters().setValue(
				RHEAHL7Constants.ENCODING_CHARACTERS);//
		msh.getVersionID().getInternationalizationCode().getIdentifier()
				.setValue(RHEAHL7Constants.INTERNATIONALIZATION_CODE);//
		msh.getVersionID().getVersionID().setValue(RHEAHL7Constants.VERSION);//
		msh.getDateTimeOfMessage().getTime().setValue(formattedDate);//
		msh.getSendingApplication()
				.getNamespaceID()
				.setValue("316");
		msh.getSendingFacility().getNamespaceID().setValue("RwandaMOH");//
		msh.getMessageType().getMessageCode()
				.setValue(RHEAHL7Constants.MESSAGE_TYPE);//
		msh.getMessageType().getTriggerEvent()
				.setValue(RHEAHL7Constants.TRIGGER_EVENT);//
		msh.getMessageType().getMessageStructure()
				.setValue(RHEAHL7Constants.MESSAGE_STRUCTURE);//
		msh.getReceivingFacility().getNamespaceID()
				.setValue(RHEAHL7Constants.RECEIVING_FACILITY);//
		msh.getProcessingID().getProcessingID()
				.setValue(RHEAHL7Constants.PROCESSING_ID);//
		msh.getProcessingID().getProcessingMode()
				.setValue(RHEAHL7Constants.PROCESSING_MODE);//
		msh.getMessageControlID().setValue(UUID.randomUUID().toString());//

		msh.getAcceptAcknowledgmentType().setValue(RHEAHL7Constants.ACK_TYPE);
		msh.getApplicationAcknowledgmentType().setValue(
				RHEAHL7Constants.APPLICATION_ACK_TYPE);
		msh.getMessageProfileIdentifier(0).getEntityIdentifier()
				.setValue("ALERT");

		Cohort singlePatientCohort = new Cohort();
		singlePatientCohort.addMember(encounter.getPatient().getId());

		Map<Integer, String> patientIdentifierMap = Context
				.getPatientSetService().getPatientIdentifierStringsByType(
						singlePatientCohort,
						Context.getPatientService()
								.getPatientIdentifierTypeByName(
										RHEAHL7Constants.IDENTIFIER_TYPE));

		PID pid = r01.getPATIENT_RESULT().getPATIENT().getPID();

		pid.getPatientIdentifierList(0)
				.getIDNumber()
				.setValue(
						patientIdentifierMap.get(patientIdentifierMap.keySet()
								.iterator().next()));
		pid.getPatientIdentifierList(0).getIdentifierTypeCode()
				.setValue(RHEAHL7Constants.IDENTIFIER_TYPE);
		pid.getPatientName(0).getFamilyName().getSurname()
				.setValue(encounter.getPatient().getFamilyName());
		pid.getPatientName(0).getGivenName()
				.setValue(encounter.getPatient().getGivenName());

		// gender
		// pid.getAdministrativeSex().setValue(pat.getGender());

		// dob
		Date dob = encounter.getPatient().getBirthdate();
		String dobStr = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		dobStr = sdf.format(dob);
		pid.getDateTimeOfBirth().getTime().setValue(dobStr);

		PV1 pv1 = r01.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();

		pv1.getPatientClass().setValue(RHEAHL7Constants.PATIENT_CLASS);
		
		if(encounter.getLocation() != null){
		pv1.getAssignedPatientLocation().getFacility().getNamespaceID()
				.setValue(encounter.getLocation().getName());
		}
		
		pv1.getAssignedPatientLocation().getPointOfCare()
				.setValue("364");
		pv1.getAdmissionType().setValue("ALERT");

		//Map<Integer, String> providerIdentifierMap = null;

		//pv1.getAttendingDoctor(0).getIDNumber().setValue("e8597a14-436f-1031-8b61-8d373bf4f88f");
		pv1.getAttendingDoctor(0).getIDNumber().setValue(service.getPersonAttributesByPerson(encounter.getProvider(), "EPID"));

		pv1.getAttendingDoctor(0).getFamilyName().getSurname()
				.setValue(encounter.getProvider().getFamilyName());
		pv1.getAttendingDoctor(0).getGivenName()
				.setValue(encounter.getProvider().getGivenName());
		pv1.getAdmitDateTime()
				.getTime()
				.setValue(
						new SimpleDateFormat("yyyyMMddhhmm").format(encounter
								.getDateCreated()));

		pv1.getAttendingDoctor(0).getIdentifierTypeCode().setValue("EPID");

		r01 = createOBRSegment(r01);

		return r01;

	}

	private ORU_R01 createOBRSegment(ORU_R01 r01) throws HL7Exception {
		OBR obr = null;

		obr = r01.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR();
		obr.getSetIDOBR().setValue(String.valueOf(0));

		obr.getUniversalServiceIdentifier().getText().setValue("ALERT");

		OBX obx = r01.getPATIENT_RESULT().getORDER_OBSERVATION(0)
				.getOBSERVATION(0).getOBX();

		obx.getSetIDOBX().setValue("0");

		obx.getObservationIdentifier().getIdentifier().setValue("rsms_rm");
		obx.getObservationIdentifier().getText().setValue("rsms_rm REMINDER");
		obx.getObservationIdentifier().getNameOfCodingSystem().setValue("RSMS");

		obx.getValueType().setValue(HL7Constants.HL7_CODED);
		CE ce = new CE(r01);
		ce.getText().setValue("rsms_pmr PATIENT MISSED REFERRAL");
		ce.getIdentifier().setValue("rsms_pmr");
		ce.getNameOfCodingSystem().setValue("RSMS");

		obx.getObservationValue(0).setData(ce);

		return r01;

	}

	public void sendRequest(String msg, Encounter e) throws IOException,
			TransformerFactoryConfigurationError, TransformerException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException,
			KeyManagementException {
		// Get the key store that includes self-signed cert as a "trusted"
		// entry.
		InputStream keyStoreStream = GenerateORU_R01Alert.class
				.getResourceAsStream("/truststore-prod.jks");

		// Load the keyStore

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(keyStoreStream, keystorePassword.toCharArray());
		log.info("KeyStoreStream = " + IOUtils.toString(keyStoreStream));
		keyStoreStream.close();

		TrustManagerFactory tmf = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);

		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(null, tmf.getTrustManagers(), null);

		// set SSL Factory to be used for all HTTPS connections
		sslFactory = ctx.getSocketFactory();

		callQueryFacility(msg, e);

	}

	private static void addHTTPBasicAuthProperty(HttpsURLConnection conn) {
		String userpass = username + ":" + password;
		@SuppressWarnings("restriction")
		String basicAuth = "Basic "
				+ new String(DatatypeConverter.printBase64Binary(userpass
						.getBytes()));
		conn.setRequestProperty("Authorization", basicAuth);
	}

	public String callQueryFacility(String msg, Encounter e) throws IOException,
			TransformerFactoryConfigurationError, TransformerException {
		
		Cohort singlePatientCohort = new Cohort();
		singlePatientCohort.addMember(e.getPatient().getId());
		
		Map<Integer, String> patientIdentifierMap = Context.getPatientSetService().getPatientIdentifierStringsByType(
		    singlePatientCohort,
		    Context.getPatientService().getPatientIdentifierTypeByName(RHEAHL7Constants.IDENTIFIER_TYPE));
			
		// Setup connection
		String id = patientIdentifierMap.get(patientIdentifierMap.keySet().iterator().next());
		URL url = new URL(hostname + "/ws/rest/v1/alerts");
		System.out.println("full url " + url);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);

		// This is important to get the connection to use our trusted
		// certificate
		conn.setSSLSocketFactory(sslFactory);

		addHTTPBasicAuthProperty(conn);
		// conn.setConnectTimeout(timeOut);
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		log.error("body" + msg);
		out.write(msg);
		out.close();
		conn.connect();
		String headerValue = conn.getHeaderField("http.status");

		// Test response code
		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}

		String result = convertInputStreamToString(conn.getInputStream());
		conn.disconnect();

		return result;
	}

	private static String convertInputStreamToString(InputStream is)
			throws IOException {
		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line + "\n");
		}
		rd.close();
		return sb.toString();
	}

}
