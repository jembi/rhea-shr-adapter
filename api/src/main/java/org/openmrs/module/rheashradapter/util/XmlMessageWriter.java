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

import java.io.PrintWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XmlMessageWriter {
	public XMLStreamWriter parseMessage(PrintWriter writer, String errorCode, String errorMsg){
		XMLStreamWriter xmlwriter = null;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
	    try {
			xmlwriter = factory.createXMLStreamWriter(writer);

			xmlwriter.writeStartDocument("1.0");

			xmlwriter.writeStartElement("error");

			xmlwriter.writeStartElement("error_code");
			xmlwriter.writeCharacters(errorCode);
			xmlwriter.writeEndElement();

			xmlwriter.writeStartElement("error_msg");
	    	xmlwriter.writeCharacters(errorMsg);
	    	xmlwriter.writeEndElement();

	    	xmlwriter.writeEndDocument();

	    	xmlwriter.flush();
	    	xmlwriter.close();
	    
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}  
	    
		return xmlwriter;
	}


		
}
