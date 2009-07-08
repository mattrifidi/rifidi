package org.rifidi.edge.readerplugin.alien.commands;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.edge.core.services.notification.data.EPCGeneration1Event;
import org.rifidi.edge.core.services.notification.data.EPCGeneration2Event;
import org.rifidi.edge.core.services.notification.data.ReadCycle;
import org.rifidi.edge.core.services.notification.data.TagReadEvent;
import org.rifidi.edge.readerplugin.alien.AbstractAlien9800Command;
import org.rifidi.edge.readerplugin.alien.Alien9800ReaderSession;
import org.rifidi.edge.readerplugin.alien.commandobject.AlienCommandObject;
import org.rifidi.edge.readerplugin.alien.commandobject.AlienException;
import org.rifidi.edge.readerplugin.alien.commandobject.AlienSetCommandObject;
import org.rifidi.edge.readerplugin.alien.commandobject.GetTagListCommandObject;
import org.springframework.jms.core.MessageCreator;

/**
 * An Command that runs on an AlienSession to get Tags back from an AlienReader
 * 
 * @author Jochen Mader - jochen@pramari.com
 * @author Kyle Neumeier - kyle@pramari.com
 */
public class AlienGetTagListCommand extends AbstractAlien9800Command {

	/** Logger for this class. */
	private static final Log logger = LogFactory
			.getLog(AlienGetTagListCommand.class);
	/** Tagtypes. */
	private Integer[] tagTypes = new Integer[] { 7, 16, 31 };
	/** Tagtypes to query for: 0 - GEN1 1 - GEN2 2 - ALL */
	private Integer tagType = 2;
	/** Timezone from the sensorSession. */
	// private TimeZone timeZone;
	/** Calendar for creating timestamps. */
	// private Calendar calendar;
	private String antennasequence = "0";

	private final String reader;

	/**
	 * Constructor
	 * 
	 * @param commandID
	 */
	public AlienGetTagListCommand(String commandID, String readerID) {
		super(commandID);
		this.reader = readerID;
	}

	/**
	 * @return the tagType
	 */
	public int getTagType() {
		return tagType;
	}

	/**
	 * @param antennasequence
	 *            the antennasequence to set
	 */
	public void setAntennasequence(String antennasequence) {
		this.antennasequence = antennasequence;
	}

	/**
	 * @param tagType
	 *            the tagType to set
	 */
	public void setTagType(int tagType) {
		this.tagType = tagType;
		if (tagType > 2 || tagType < 0) {
			throw new RuntimeException("Tagtype must be 0, 1, or 2");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			// AlienCommandObject timeZoneCommand = new AlienGetCommandObject(
			// Alien9800ReaderSession.COMMAND_TIME_ZONE,
			// (Alien9800ReaderSession) this.sensorSession);
			// String tz = timeZoneCommand.execute();
			// String timeZoneString = "GMT" + tz;
			// timeZone = TimeZone.getTimeZone(timeZoneString);
			// calendar = Calendar.getInstance(timeZone);

			AlienCommandObject antennaCommand = new AlienSetCommandObject(
					Alien9800ReaderSession.ANTENNA_SEQUENCE_COMMAND,
					this.antennasequence,
					(Alien9800ReaderSession) this.sensorSession);
			antennaCommand.execute();

			// sending TagType
			AlienCommandObject tagTypeCommand = new AlienSetCommandObject(
					Alien9800ReaderSession.COMMAND_TAG_TYPE,
					tagTypes[getTagType()].toString(),
					(Alien9800ReaderSession) sensorSession);
			tagTypeCommand.execute();

			// sending TagListFormat
			AlienCommandObject tagListFormat = new AlienSetCommandObject(
					Alien9800ReaderSession.COMMAND_TAG_LIST_FORMAT, "custom",
					(Alien9800ReaderSession) sensorSession);
			tagListFormat.execute();
			// sending TagListFormat
			AlienCommandObject tagListCustomFormat = new AlienSetCommandObject(
					Alien9800ReaderSession.COMMAND_TAG_LIST_CUSTOM_FORMAT,
					"%k|%T|%a|%p", (Alien9800ReaderSession) sensorSession);
			tagListCustomFormat.execute();
			GetTagListCommandObject getTagListCommandObject = new GetTagListCommandObject(
					(Alien9800ReaderSession) sensorSession);
			ReadCycle cycle=new ReadCycle(parseString(getTagListCommandObject.executeGet()), reader, System
					.currentTimeMillis());
			sensorSession.getSensor().send(cycle);
			template.send(destination, new ObjectMessageCreator(cycle));

		} catch (AlienException e) {
			logger.warn("Exception while executing command: " + e);
		} catch (IOException e) {
			logger.warn("IOException while executing command: " + e);
		} catch (Exception e) {
			logger.error("Exception while executing command: " + e);
		}
	}

	/**
	 * Parse the given string for results.
	 * 
	 * @param input
	 * @return
	 */
	private Set<TagReadEvent> parseString(List<String> input) {

		Set<TagReadEvent> retVal = new HashSet<TagReadEvent>();

		try {
			for (String s : input) {
				s = s.trim();
				String[] splitString2 = s.split("\\|");
				if (splitString2.length > 1) {
					String tagData = splitString2[0];
					// String timeStamp = splitString2[1];
					String antennaID = splitString2[2];
					String epcID = splitString2[3];
					int epcLength = tagData.length();
					BigInteger data = new BigInteger(tagData, 16);

					int epcInt = 2;
					try {
						epcInt = Integer.valueOf(epcID);
					} catch (NumberFormatException ex) {
					}

					EPCGeneration1Event genEvent = null;

					if (epcInt == 1) {
						EPCGeneration1Event gen1event = new EPCGeneration1Event();
						if (epcLength > 96) {
							gen1event.setEPCMemory(data, 192);
						} else if (data.bitLength() > 64) {
							gen1event.setEPCMemory(data, 96);
						} else {
							gen1event.setEPCMemory(data, 64);
						}
						genEvent = gen1event;
					} else {
						EPCGeneration2Event gen2event = new EPCGeneration2Event();
						// make some wild guesses on the length of the epc field
						if (epcLength > 96) {
							gen2event.setEPCMemory(data, 192);
						} else if (data.bitLength() > 64) {
							gen2event.setEPCMemory(data, 96);
						} else {
							gen2event.setEPCMemory(data, 64);
						}
						genEvent = gen2event;
					}

					int antennaID_int = 0;

					try {
						antennaID_int = Integer.parseInt(antennaID);
					} catch (NumberFormatException ex) {

					}

					// TODO: parse timestamp

					TagReadEvent tag = new TagReadEvent(reader, genEvent,
							antennaID_int, System.currentTimeMillis());
					retVal.add(tag);

				} else {
					// logger.error("Something isreaders invalid: " +
					// splitString2[0]);
				}
			}
		} catch (Exception e) {
			logger.warn("There was a problem when parsing Alien Tags.  "
					+ "tag has not been added", e);
		}
		return retVal;
	}

	/**
	 * Used to create a JMS message to send to the Queue that collects Tag Data
	 * 
	 * @author Kyle Neumeier - kyle@pramari.com
	 * 
	 */
	private class ObjectMessageCreator implements MessageCreator {

		/** Message to send */
		private ActiveMQObjectMessage objectMessage;

		/**
		 * Constructor.
		 * 
		 * @param tags
		 *            the tags to add to this message
		 */
		public ObjectMessageCreator(ReadCycle readCycle) {
			super();
			objectMessage = new ActiveMQObjectMessage();

			try {
				objectMessage.setObject(readCycle);
			} catch (JMSException e) {
				logger.warn("Unable to set tag event: " + e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.jms.core.MessageCreator#createMessage(javax.jms
		 * .Session)
		 */
		@Override
		public Message createMessage(Session arg0) throws JMSException {
			return objectMessage;
		}

	}

}
