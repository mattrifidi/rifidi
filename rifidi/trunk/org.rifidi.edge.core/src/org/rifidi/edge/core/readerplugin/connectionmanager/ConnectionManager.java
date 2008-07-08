package org.rifidi.edge.core.readerplugin.connectionmanager;

import java.util.ArrayList;
import java.util.List;

import org.rifidi.edge.core.communication.service.ConnectionEventListener;
import org.rifidi.edge.core.exceptions.RifidiConnectionException;
import org.rifidi.edge.core.readerplugin.ReaderInfo;
import org.rifidi.edge.core.readerplugin.protocol.CommunicationProtocol;

public abstract class ConnectionManager implements ConnectionExceptionListener {

	public List<ConnectionEventListener> listeners = new ArrayList<ConnectionEventListener>();
	
	// This is not working
	// public ConnectionManager(ReaderInfo readerInfo) {
	// }

	public abstract ConnectionStreams createCommunication(ReaderInfo readerInfo)
			throws RifidiConnectionException;

	public abstract void connect(ReaderInfo readerInfo)
			throws RifidiConnectionException;

	public abstract void disconnect();

	public abstract int getMaxNumConnectionsAttemps();

	public abstract long getReconnectionIntervall();

	public abstract CommunicationProtocol getCommunicationProtocol();

	public abstract void startKeepAlive();

	public abstract void stopKeepAlive();

	public void addConnectionEventListener(
			ConnectionEventListener listener){
		listeners.add(listener);
	}

	public void removeConnectionEventListener(
			ConnectionEventListener listener){
		listeners.remove(listener);
	}
	
	protected abstract void fireEvent(); 
}
