/*
 *  AbstractReaderInfo.java
 *
 *  Created:	Jun 19, 2008
 *  Project:	RiFidi Emulator - A Software Simulation Tool for RFID Devices
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	Lesser GNU Public License (LGPL)
 *  				http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.edge.core.readerPlugin;

import java.io.Serializable;

import org.rifidi.edge.core.communication.enums.CommunicationType;

public abstract class AbstractReaderInfo implements Serializable {

	private int port;

	private String ip;

	/**
	 * @return The IP address of the Reader
	 */
	public String getIPAddress() {
		return ip;
	}

	/**
	 * @param ipAddress
	 *            The IP address of the Reader
	 */
	public void setIPAddress(String ipAddress) {
		ip = ipAddress;
	}

	/**
	 * @return The IP port of the reader
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            The IP port of the reader
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return A string version of the reader.
	 */
	public abstract String getReaderType();

	/**
	 * @return the type of Communication
	 */
	public abstract CommunicationType getCommunicationType();

}
