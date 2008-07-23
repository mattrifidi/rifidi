package org.rifidi.edge.core.readersession.impl.states;

import org.rifidi.edge.core.exceptions.RifidiCommandInterruptedException;
import org.rifidi.edge.core.exceptions.RifidiCommandNotFoundException;
import org.rifidi.edge.core.exceptions.RifidiConnectionException;
import org.rifidi.edge.core.exceptions.RifidiInvalidConfigurationException;
import org.rifidi.edge.core.readersession.impl.ReaderSessionState;
import org.w3c.dom.Document;

public class SessionStatePropertyExecuting implements ReaderSessionState{

	ReaderSessionImpl readerSessionImpl;
	
	public SessionStatePropertyExecuting(ReaderSessionImpl readerSessionImpl) {
		this.readerSessionImpl = readerSessionImpl;
	}
	
	@Override
	public void state_commandFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void state_error() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long state_executeCommand(Document configuration)
			throws RifidiConnectionException,
			RifidiCommandInterruptedException, RifidiCommandNotFoundException,
			RifidiInvalidConfigurationException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Document state_executeProperty(Document propertiesToExecute)
			throws RifidiConnectionException, RifidiCommandNotFoundException,
			RifidiCommandInterruptedException {
		return null;
		
	}

	@Override
	public void state_propertyFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void state_resetSession() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void state_stopCommand(boolean force) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void conn_connected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void conn_disconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void conn_error() {
		// TODO Auto-generated method stub
		
	}

}
