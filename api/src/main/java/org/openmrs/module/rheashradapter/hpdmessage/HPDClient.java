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
package org.openmrs.module.rheashradapter.hpdmessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class HPDClient {

	public static String xmlMessageHeader = "<?xml version='1.0' encoding='UTF-8'?>";
	public static String soapEnvelopeMessageHeader = "<soap-env:Envelope xmlns:soap-env=\'http://schemas.xmlsoap.org/soap/envelope/\'>";
	public static String soapEnvelopeBody = "<soap-env:Body>";
	public static String batchRequest = "<batchRequest xmlns=\'urn:oasis:names:tc:DSML:2:0:core\'>";
	public static String batchRequestEnd = "</batchRequest>";
	public static String searchRequest = "<searchRequest dn='ou=HCProfessional,dc=hpd,dc=mirth,dc=com' scope='singleLevel' derefAliases='derefFindingBaseObj'>";
	public static String searchRequestEnd = "</searchRequest>";
	public static String filter = "<filter>";
	public static String filterEnd = "</filter>";
	public static String match = "<equalityMatch name='HcIdentifier'>";
	public static String matchEnd = "</equalityMatch>";
	public static String attributes = "<attributes>";
	public static String attributesEnd = "</attributes>";
	public static String value = "<value>";
	public static String valueEnd = "</value>";
	public static String attributeName1 = "<attribute name='HcIdentifier'/> ";
	public static String attributeName2 = "<attribute name='hpdProviderStatus'/>";
	public static String attributeName3 = "<attribute name='givenName'/>";
	public static String soapEnvelopeBodyEnd = "</soap-env:Body>";
	public static String soapEnvelopeEnd = "</soap-env:Envelope>";
	
	
	public SOAPMessage createHPDRequest(){
	String messageEnvelopeStart = xmlMessageHeader + soapEnvelopeMessageHeader + soapEnvelopeBody + batchRequest;
	String messageEnvelopeEnd = batchRequestEnd + soapEnvelopeBodyEnd + soapEnvelopeEnd;
	
	HPDClient client = new HPDClient();

	List<String> providerIds = new ArrayList<String>();
	providerIds.add("123");
	providerIds.add("234");
	providerIds.add("456");
	String searchRequestString = client.generateSearchRequestString(providerIds);
	
	String dsmlSoapMessage = messageEnvelopeStart + searchRequestString + messageEnvelopeEnd;
	
	System.out.println(dsmlSoapMessage);
	
	
	   SOAPConnectionFactory sfc;
	   SOAPMessage request = null;
	try {
		sfc = SOAPConnectionFactory.newInstance();
	    SOAPConnection connection = sfc.createConnection();
	    InputStream is = new ByteArrayInputStream(dsmlSoapMessage.getBytes());
	    request = MessageFactory.newInstance().createMessage(null, is);

	    request.writeTo(System.out);
	    
	} catch (UnsupportedOperationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SOAPException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return request;
	    
	}


	private String generateSearchRequestString(List<String> providerIds) {
		String searchMessage = "";
		for(String string : providerIds){

			String valueContents = value + string + valueEnd;
			String message = searchRequest + filter + match + valueContents + matchEnd + filterEnd + attributes + attributeName1 + attributeName2 + attributeName3 + attributesEnd + searchRequestEnd;
			searchMessage = searchMessage + message;
		}
		
		return searchMessage;
	}	
}

