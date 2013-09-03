package org.jembi.openhim;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;

public class TestPersister implements Callable {
	
	@PersistenceContext
	EntityManager entityManager;

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage msg = eventContext.getMessage();
		Report report = (Report) msg.getPayload();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(report);
		//entityManager.flush();
		//entityManager.close();
		transaction.commit();
		
		return null;
	}

	

}
