package org.rifidi.edge.core.readersession.impl;

import org.rifidi.edge.core.communication.service.CommunicationStateListener;
import org.rifidi.edge.core.exceptions.RifidiCannotRestartCommandException;
import org.rifidi.edge.core.exceptions.RifidiCommandInterruptedException;
import org.rifidi.edge.core.exceptions.RifidiCommandNotFoundException;
import org.rifidi.edge.core.exceptions.RifidiConnectionException;
import org.rifidi.edge.core.exceptions.RifidiInvalidConfigurationException;
import org.rifidi.edge.core.readerplugin.ReaderInfo;
import org.rifidi.edge.core.readerplugin.commands.CommandConfiguration;
import org.rifidi.edge.core.readersession.impl.enums.ReaderSessionStatus;
import org.w3c.dom.Document;

public interface ReaderSessionState extends CommunicationStateListener {

	public long state_executeCommand(CommandConfiguration configuration)
			throws RifidiConnectionException,
			RifidiCommandInterruptedException, RifidiCommandNotFoundException;

	/**
	 * 
	 * @param propertiesToExecute
	 * @param set
	 *            if true, set the property. If false, get the value of the
	 *            property
	 * @return
	 * @throws RifidiConnectionException
	 * @throws RifidiCommandNotFoundException
	 * @throws RifidiCommandInterruptedException
	 * @throws RifidiInvalidConfigurationException
	 * @throws RifidiCannotRestartCommandException
	 */
	public Document state_executeProperty(Document propertiesToExecute,
			boolean set) throws RifidiConnectionException,
			RifidiCommandNotFoundException, RifidiCommandInterruptedException,
			RifidiCannotRestartCommandException, RifidiInvalidConfigurationException;

	public void state_resetSession();

	public void state_stopCommand(boolean force);

	public void state_commandFinished();

	public void state_propertyFinished()
			throws RifidiCannotRestartCommandException;

	public void state_error();

	/**
	 * Get the current status of the ReaderSession
	 * 
	 * @return the status of the ReaderSession
	 */
	public ReaderSessionStatus state_getStatus();

	public void state_enable();

	public void state_disable();

	public boolean state_setReaderInfo(ReaderInfo readerInfo);

}
