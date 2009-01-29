package org.rifidi.edge.readerplugin.alien.properties.general;

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
@Form(name = Password.PASSWORD, formElements = { @FormElement(type = FormElementType.STRING, elementName = Password.PASSWORD_DATA, editable = true, defaultValue = "0", displayName = Password.PASSWORD_DISPLAY) })
public class Password implements Property {

	private static final String PASSWORD = "Password";

	private static final String PASSWORD_DATA = "Password_Data";

	private static final String PASSWORD_DISPLAY = "Password";

	private static final Log logger = LogFactory.getLog(Password.class);

	private static String command = "password";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.readerplugin.property.Property#getProperty(org.rifidi
	 * .edge.core.communication.Connection,
	 * org.rifidi.edge.core.messageQueue.MessageQueue, org.w3c.dom.Element)
	 */
	@Override
	public CommandConfiguration getProperty(Connection connection, MessageQueue errorQueue,
			CommandConfiguration propertyConfig) {
		AlienResponse response = new AlienResponse();
		String responseString = null;
		try {
			connection.sendMessage("\1get " + command + "\n");

			responseString = (String) connection.receiveMessage();

		} catch (IOException e) {
			logger.debug("IOException");
		}
		response.setResponseMessage(responseString);
		return response.formulateResponse(PASSWORD, PASSWORD_DATA);
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
	public CommandConfiguration setProperty(Connection connection, MessageQueue errorQueue,
			CommandConfiguration propertyConfig) {

		String comm = "\1set " + command + " = "
				+ propertyConfig.getArgValue(PASSWORD_DATA) + "\n";
		AlienResponse response = new AlienResponse();
		String responseString = null;
		try {
			connection.sendMessage(comm);
			responseString = (String) connection.receiveMessage();

		} catch (IOException e) {
			logger.debug("IOException");
		}
		response.setResponseMessage(responseString);
		return response.formulateResponse(PASSWORD, PASSWORD_DATA);
	}

}
