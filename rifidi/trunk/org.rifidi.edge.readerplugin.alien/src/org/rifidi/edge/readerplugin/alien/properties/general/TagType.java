/* 
 * TagType.java
 *  Created:	Nov 18, 2008
 *  Project:	RiFidi Emulator - A Software Simulation Tool for RFID Devices
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	Lesser GNU Public License (LGPL)
 *  				http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.edge.readerplugin.alien.properties.general;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.dynamicswtforms.xml.annotaions.Form;
import org.rifidi.dynamicswtforms.xml.annotaions.FormElement;
import org.rifidi.dynamicswtforms.xml.constants.FormElementType;
import org.rifidi.edge.core.api.readerplugin.Property;
import org.rifidi.edge.core.api.readerplugin.commands.CommandConfiguration;
import org.rifidi.edge.core.api.readerplugin.communication.Connection;
import org.rifidi.edge.core.api.readerplugin.messageQueue.MessageQueue;
import org.rifidi.edge.readerplugin.alien.properties.AlienResponse;

/**
 * 
 * 
 * @author Matthew Dean - matt@pramari.com
 */
@Form(name = TagType.TAG_TYPE, formElements = { @FormElement(type = FormElementType.CHOICE, elementName = TagType.TAG_TYPE_DATA, editable = true, defaultValue = "ALL", displayName = TagType.TAG_TYPE_DISPLAY, enumClass = "org.rifidi.edge.readerplugin.alien.properties.AlienTagGenerations") })
public class TagType implements Property {

	private static final String TAG_TYPE = "TagType";

	private static final String TAG_TYPE_DATA = "TagTypeData";

	private static final String TAG_TYPE_DISPLAY = "Antenna Sequence";

	private static final Log logger = LogFactory.getLog(TagType.class);

	private static final String command = "tagtype";

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
		return response.formulateResponse(TAG_TYPE, TAG_TYPE_DATA);

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

		String command = "\1set " + TagType.command + " = "
				+ propertyConfig.getArgValue(TAG_TYPE_DATA) + "\n";
		AlienResponse response = new AlienResponse();
		String responseString = null;
		try {
			connection.sendMessage(command);
			responseString = (String) connection.receiveMessage();

		} catch (IOException e) {
			logger.debug("IOException");
		}
		response.setResponseMessage(responseString);
		return response.formulateResponse(TAG_TYPE, TAG_TYPE_DATA);
	}

}
