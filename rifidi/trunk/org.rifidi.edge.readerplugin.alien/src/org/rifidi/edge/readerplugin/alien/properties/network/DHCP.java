package org.rifidi.edge.readerplugin.alien.properties.network;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.dynamicswtforms.xml.annotaions.Form;
import org.rifidi.dynamicswtforms.xml.annotaions.FormElement;
import org.rifidi.dynamicswtforms.xml.constants.FormElementType;
import org.rifidi.edge.core.api.communication.Connection;
import org.rifidi.edge.core.api.messageQueue.MessageQueue;
import org.rifidi.edge.core.api.readerplugin.commands.CommandConfiguration;
import org.rifidi.edge.core.api.readerplugin.property.api.Property;
import org.rifidi.edge.readerplugin.alien.properties.AlienResponse;

/**
 * 
 * @author Matthew Dean
 */
@Form(name = DHCP.NAME, formElements = { @FormElement(type = FormElementType.STRING, elementName = DHCP.DATA, editable = false, defaultValue = "0", displayName = DHCP.DISPLAY) })
public class DHCP implements Property {

	private static final String NAME = "DHCP";

	private static final String DATA = "DHCP_Data";

	private static final String DISPLAY = "DHCP";

	private static final Log logger = LogFactory.getLog(DHCP.class);

	private static final String command = "DHCP";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.readerplugin.property.Property#getProperty(org.rifidi
	 * .edge.core.communication.Connection,
	 * org.rifidi.edge.core.messageQueue.MessageQueue, org.w3c.dom.Element)
	 */
	@Override
	public CommandConfiguration getProperty(Connection connection,
			MessageQueue errorQueue, CommandConfiguration propertyConfig) {
		AlienResponse response = new AlienResponse();
		String responseString = null;
		try {
			connection.sendMessage("\1get " + command + "\n");

			responseString = (String) connection.receiveMessage();

		} catch (IOException e) {
			logger.debug("IOException");
		}
		response.setResponseMessage(responseString);
		return response.formulateResponse(NAME, DATA);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.readerplugin.property.Property#setProperty(org.rifidi
	 * .edge.core.communication.Connection,
	 * org.rifidi.edge.core.messageQueue.MessageQueue, org.w3c.dom.Element)
	 */
	@Override
	public CommandConfiguration setProperty(Connection connection,
			MessageQueue errorQueue, CommandConfiguration propertyConfig) {
		return getProperty(connection, errorQueue, propertyConfig);
	}

}
