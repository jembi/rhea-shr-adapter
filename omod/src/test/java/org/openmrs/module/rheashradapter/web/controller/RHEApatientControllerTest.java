package org.openmrs.module.rheashradapter.web.controller;



import static org.junit.Assert.*;

import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;

import org.openmrs.module.rheashradapter.util.RHEAHL7Constants;
import org.openmrs.module.rheashradapter.web.controller.RHEApatientController;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.PID;


public class RHEApatientControllerTest extends BaseModuleContextSensitiveTest {
	
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet("src/test/resources/RHEAControllerTest-initialData.xml");
	}
	
	@Test
	@Verifies(value = "should Get Matching Encounters by patient ECID From Database(", method = "getEncounters(...)")
	public void getEncounters_shouldGetMatchingEncountersByPatientECIDFromDatabase() throws Exception {
		
		Context.getAdministrationService().addGlobalProperty("rheashradapter.sendingFaculty", "Shared Health Record");

		RHEApatientController controller = new RHEApatientController();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		HttpServletResponse response = new MockHttpServletResponse();
		String enterpriseId = "1234";
		String idType = "ECID";
		
		Object object = controller.getEncounters(enterpriseId, idType, null, null, null, null, request, response);
		
		ORU_R01 r01 = (ORU_R01) object;
		assertNotNull(r01);
		assertTrue(r01 instanceof ORU_R01);	
		
		assertEquals(r01.getMSH().getVersionID().getInternationalizationCode().getIdentifier().getValue(),RHEAHL7Constants.INTERNATIONALIZATION_CODE);
		assertEquals(r01.getMSH().getSendingFacility().getNamespaceID().getValue(),RHEAHL7Constants.SENDING_FACILITY);
		
		PID pid = r01.getPATIENT_RESULT().getPATIENT().getPID();
		
	}
	
	@Test
	@Verifies(value = "should Get Matching Encounters by patient ECID From encounterUniqueId (", method = "getEncounters(...)")
	public void getEncounters_shouldGetMatchingEncountersByEncounterUniqueId() throws Exception {
		
		Context.getAdministrationService().addGlobalProperty("rheashradapter.sendingFaculty", "Shared Health Record");

		RHEApatientController controller = new RHEApatientController();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		HttpServletResponse response = new MockHttpServletResponse();
		String enterpriseId = "1234";
		String idType = "ECID";
		String encounterUniqueId = "f13d6fae-baa9-4553-955d-920098bec08f";
		
		Object object = controller.getEncounters(enterpriseId, idType, encounterUniqueId, null, null, null, request, response);
		
		ORU_R01 r01 = (ORU_R01) object;
		assertNotNull(r01);
		assertTrue(r01 instanceof ORU_R01);	
		
		assertEquals(r01.getMSH().getVersionID().getInternationalizationCode().getIdentifier().getValue(),RHEAHL7Constants.INTERNATIONALIZATION_CODE);
		assertEquals(r01.getMSH().getSendingFacility().getNamespaceID().getValue(),RHEAHL7Constants.SENDING_FACILITY);
		
		PID pid = r01.getPATIENT_RESULT().getPATIENT().getPID();
		
	}
	
	@Test
	@Verifies(value = "should Get Matching Encounters by date created (", method = "getEncounters(...)")
	public void getEncounters_shouldGetMatchingEncountersByDateCreated() throws Exception {
				
		RHEApatientController controller = new RHEApatientController();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		HttpServletResponse response = new MockHttpServletResponse();
		String enterpriseId = "1234";
		String idType = "ECID";
		
		Object object = controller.getEncounters(enterpriseId, idType, null, "2005-04-31T12:30:23", null, null, request, response);
		
		ORU_R01 r01 = (ORU_R01) object;
		assertNotNull(r01);
		assertEquals("1234", r01.getPATIENT_RESULT().getPATIENT().getPID().getPatientIdentifierList(0).getIDNumber().toString());

		
	}
	
	@Test
	@Verifies(value = "should Get Matching Encounters by date end (", method = "getEncounters(...)")
	public void getEncounters_shouldGetMatchingEncountersByDateEnd() throws Exception {
		
		RHEApatientController controller = new RHEApatientController();
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		HttpServletResponse response = new MockHttpServletResponse();
		String enterpriseId = "1234";
		String idType = "ECID";
		
		Object object = controller.getEncounters(enterpriseId, idType, null, null, "2007-05-02T12:30:23", null, request, response);
		
		ORU_R01 r01 = (ORU_R01) object;
		
	}
	
	@Test
	@Verifies(value = "should Parse HL7 Message Into POJOs(", method = "createEncounters(...)")
	public void createEncounters_shouldParseHL7MessageIntoPOJOs() throws Exception {
		RHEApatientController controller = new RHEApatientController();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		String enterpriseId = "1234";
		String idType = "ECID";
		
		assertEquals(Context.getEncounterService().getEncountersByPatientId(2).size(),1);
		
		String hl7 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
			    "sample-orur01.xml"));		
		Encounter encounter = (Encounter) controller.createEncounters(hl7, enterpriseId, idType, null, request, response);
		assertNotNull(encounter);
		assertEquals(Context.getEncounterService().getEncountersByPatientId(2).size(),2);	
		assertEquals(encounter.getPatient().getFamilyName(),"Doe");
		
	}	
	
	
	@Test
	@Verifies(value = "should fail on unknown notification type(", method = "createEncounters(...)")
	public void createEncounters_shouldfailOnUnknownNotificationType() throws Exception {
		RHEApatientController controller = new RHEApatientController();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		String enterpriseId = "1234";
		String idType = "ECID";
		
		assertEquals(Context.getEncounterService().getEncountersByPatientId(2).size(),1);
		
		String hl7 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
			    "sample-orur01.xml"));		
		Object object = controller.createEncounters(hl7, enterpriseId, idType, "UNKNOWN", request, response);
		assertNull(object);

		
	}
	
	@Test
	@Verifies(value = "should create ORUR01 message for known notification type(", method = "createEncounters(...)")
	public void createEncounters_shouldCreateORUR01MessageForKnownNotificationType() throws Exception {
		RHEApatientController controller = new RHEApatientController();
		
		MockHttpServletRequest request = new MockHttpServletRequest("RPOST", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		String enterpriseId = "1234";
		String idType = "ECID";
		
		assertEquals(Context.getEncounterService().getEncountersByPatientId(2).size(),1);
		
		String hl7 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
			    "sample-orur01.xml"));		
		assertNotNull(hl7);
		Encounter encounter = (Encounter) controller.createEncounters(hl7, enterpriseId, idType, "RISK", request, response);
		assertNotNull(encounter);
		assertTrue(encounter instanceof Encounter);

		
	}
	
	@Test
	@Verifies(value = "should Parse Merge Message Accurately (", method = "mergePatients(...)")
	public void mergePatients_shouldParseMergeMessageAccurately() throws Exception {
		RHEApatientController controller = new RHEApatientController();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		HttpServletResponse response = new MockHttpServletResponse();

		String mergeMessage = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
			    "sample-openempi-merge-message.xml"));	
		
		controller.mergePatients(mergeMessage, request, response);
	}
		
}

