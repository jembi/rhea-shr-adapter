package org.openmrs.module.rheashradapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.rheashradapter.hpdmessage.HPDClient;
import org.openmrs.scheduler.tasks.AbstractTask;

public class ProviderValidationTask extends AbstractTask {
	
	// Logger 
	private Log log = LogFactory.getLog(ProviderValidationTask.class);
	
	public void execute() {
		try {
			// Authenticate
			if (!Context.isAuthenticated()) {
				authenticate();
			}
			
			// Send alert notifications to users who have unread alerts
			HPDClient hpdClient = new HPDClient();
			hpdClient.createHPDRequest();
		}
		catch (Exception e) {
			log.error(e);
		}
	}

}
