/*
 *  LLRPReaderSession.java
 *
 *  Created:	Mar 9, 2009
 *  Project:	Rifidi Edge Server - A middleware platform for RFID applications
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	GNU Public License (GPL)
 *  				http://www.opensource.org/licenses/gpl-3.0.html
 */
package org.rifidi.edge.readerplugin.llrp;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.llrp.ltk.generated.enumerations.AccessReportTriggerType;
import org.llrp.ltk.generated.enumerations.NotificationEventType;
import org.llrp.ltk.generated.enumerations.ROReportTriggerType;
import org.llrp.ltk.generated.enumerations.StatusCode;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.generated.messages.SET_READER_CONFIG;
import org.llrp.ltk.generated.messages.SET_READER_CONFIG_RESPONSE;
import org.llrp.ltk.generated.parameters.AccessReportSpec;
import org.llrp.ltk.generated.parameters.AntennaID;
import org.llrp.ltk.generated.parameters.C1G2EPCMemorySelector;
import org.llrp.ltk.generated.parameters.EPC_96;
import org.llrp.ltk.generated.parameters.EventNotificationState;
import org.llrp.ltk.generated.parameters.ROReportSpec;
import org.llrp.ltk.generated.parameters.ReaderEventNotificationSpec;
import org.llrp.ltk.generated.parameters.TagReportContentSelector;
import org.llrp.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.net.LLRPConnection;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedShort;
import org.rifidi.edge.api.SessionStatus;
import org.rifidi.edge.core.sensors.base.AbstractSensor;
import org.rifidi.edge.core.sensors.base.AbstractSensorSession;
import org.rifidi.edge.core.sensors.commands.Command;
import org.rifidi.edge.core.services.notification.NotifierService;
import org.rifidi.edge.core.services.notification.data.EPCGeneration2Event;
import org.rifidi.edge.core.services.notification.data.ReadCycle;
import org.rifidi.edge.core.services.notification.data.TagReadEvent;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * This class represents a session with an LLRP reader. It handles connecting
 * and disconnecting, as well as recieving tag data.
 * 
 * @author Matthew Dean
 */
public class LLRPReaderSession extends AbstractSensorSession
		implements
			LLRPEndpoint {

	/** Logger for this class. */
	private static final Log logger = LogFactory
			.getLog(LLRPReaderSession.class);

	private LLRPConnection connection = null;
	/** Service used to send out notifications */
	private NotifierService notifierService;
	/** The ID of the reader this session belongs to */
	private String readerID;

	int messageID = 1;
	int maxConAttempts = -1;
	int reconnectionInterval = -1;

	/** atomic boolean that is true if we are inside the connection attempt loop */
	private AtomicBoolean connectingLoop = new AtomicBoolean(false);
	/** LLRP host */
	private String host;
	/** LLRP port */
	private int port;

	/**
	 * 
	 * @param sensor
	 * @param id
	 * @param host
	 * @param reconnectionInterval
	 * @param maxConAttempts
	 * @param destination
	 * @param template
	 * @param notifierService
	 * @param readerID
	 */
	public LLRPReaderSession(AbstractSensor<?> sensor, String id, String host,
			int port, int reconnectionInterval, int maxConAttempts,
			Destination destination, JmsTemplate template,
			NotifierService notifierService, String readerID) {
		super(sensor, id, destination, template);
		this.host = host;
		this.port = port;
		this.connection = new LLRPConnector(this, host, port);
		this.maxConAttempts = maxConAttempts;
		this.reconnectionInterval = reconnectionInterval;
		this.notifierService = notifierService;
		this.readerID = readerID;
		this.setStatus(SessionStatus.CLOSED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.core.readers.ReaderSession#connect()
	 */
	@Override
	public void connect() throws IOException {
		logger.info("LLRP Session " + this.getID() + " on sensor "
				+ this.getSensor().getID() + " attempting to connect to "
				+ host + ":" + port);
		this.setStatus(SessionStatus.CONNECTING);
		// conntected flag
		boolean connected = false;
		// try to connect up to MaxConAttempts number of times, unless
		// maxConAttempts is -1, in which case, try forever
		connectingLoop.set(true);
		try {
			for (int connCount = 0; connCount < maxConAttempts
					|| maxConAttempts == -1; connCount++) {

				// attempt to make the connection
				try {
					((LLRPConnector) connection).connect();
					connected = true;
					break;
				} catch (LLRPConnectionAttemptFailedException e) {
					logger.debug("Attempt to connect to LLRP reader failed: "
							+ connCount);
				} catch (org.apache.mina.common.RuntimeIOException e) {
					logger.debug("Attempt to connect to LLRP reader failed: "
							+ connCount);
				}

				// wait for a specified number of ms or until someone calls
				// notify on the connetingLoop object
				try {
					synchronized (connectingLoop) {
						connectingLoop.wait(reconnectionInterval);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}

				// if someone else wants us to stop, break from the for loop
				if (!connectingLoop.get())
					break;
			}
		} finally {
			// make sure connecting loop is false!
			connectingLoop.set(false);
		}

		// if not connected, exit
		if (!connected) {
			setStatus(SessionStatus.CLOSED);
			throw new IOException("Cannot connect");
		}

		setStatus(SessionStatus.LOGGINGIN);
		executor = new ScheduledThreadPoolExecutor(1);

		try {
			SET_READER_CONFIG config = createSetReaderConfig();
			config.setMessageID(new UnsignedInteger(messageID++));

			SET_READER_CONFIG_RESPONSE config_response = (SET_READER_CONFIG_RESPONSE) connection
					.transact(config);

			StatusCode sc = config_response.getLLRPStatus().getStatusCode();
			if (sc.intValue() != StatusCode.M_Success) {
				logger.debug("SET_READER_CONFIG_RESPONSE "
						+ "returned with status code " + sc.intValue());
			}

			if (!processing.compareAndSet(false, true)) {
				logger.warn("Executor was already active! ");
			}
			setStatus(SessionStatus.PROCESSING);

			// resubmit commands
			while (commandQueue.peek() != null) {
				executor.submit(commandQueue.poll());
			}
			synchronized (commands) {
				for (Integer id : commands.keySet()) {
					if (idToData.get(id).future == null) {
						idToData.get(id).future = executor
								.scheduleWithFixedDelay(commands.get(id), 0,
										idToData.get(id).interval, idToData
												.get(id).unit);
					}
				}
			}
		} catch (TimeoutException e) {
			logger.error(e.getMessage());
			disconnect();
		} catch (ClassCastException ex) {
			logger.error(ex.getMessage());
			disconnect();
		}

	}
	/**
	 * 
	 */
	@Override
	public void disconnect() {
		try {
			// if in the connecting loop, set atomic boolean to false and call
			// notify on the connectingLoop monitor
			if (connectingLoop.getAndSet(false)) {
				synchronized (connectingLoop) {
					connectingLoop.notify();
				}
			}
			// if already connected, disconnect
			if (processing.get()) {
				if (processing.compareAndSet(true, false)) {
					logger.debug("Disconnecting");
					((LLRPConnector) connection).disconnect();
				}
			}
			// make sure commands are canceled
			synchronized (commands) {
				for (Integer id : commands.keySet()) {
					if (idToData.get(id).future != null) {
						idToData.get(id).future.cancel(true);
						idToData.get(id).future = null;
					}
				}
			}
		} finally {
			// make sure executor is shutdown!
			if (executor != null) {
				executor.shutdownNow();
				executor = null;
			}
			// notify anyone who cares that session is now closed
			setStatus(SessionStatus.CLOSED);
		}

	}

	/**
	 * 
	 */
	public LLRPMessage transact(LLRPMessage message) {
		// System.out.println("Sending an LLRP message: " + message.getName());
		LLRPMessage retVal = null;
		try {
			retVal = this.connection.transact(message);
		} catch (TimeoutException e) {
			logger.error("Cannot send LLRP Message: ", e);
			disconnect();
		}

		return retVal;
	}

	/**
	 * 
	 * @param message
	 */
	public void send(LLRPMessage message) {
		connection.send(message);
	}

	/**
	 * 
	 */
	@Override
	public void errorOccured(String arg0) {
		logger.error("LLRP Error Occurred: " + arg0);
		// TODO: should we disconnect?
	}

	/**
	 * This method creates a SET_READER_CONFIG method.
	 * 
	 * @return The SET_READER_CONFIG object.
	 */
	public SET_READER_CONFIG createSetReaderConfig() {
		SET_READER_CONFIG setReaderConfig = new SET_READER_CONFIG();

		// Create a default RoReportSpec so that reports are sent at the end of
		// ROSpecs
		ROReportSpec roReportSpec = new ROReportSpec();
		roReportSpec.setN(new UnsignedShort(0));
		roReportSpec.setROReportTrigger(new ROReportTriggerType(
				ROReportTriggerType.None));
		TagReportContentSelector tagReportContentSelector = new TagReportContentSelector();
		tagReportContentSelector.setEnableAccessSpecID(new Bit(0));
		tagReportContentSelector.setEnableAntennaID(new Bit(1));
		tagReportContentSelector.setEnableChannelIndex(new Bit(0));
		tagReportContentSelector.setEnableFirstSeenTimestamp(new Bit(0));
		tagReportContentSelector.setEnableInventoryParameterSpecID(new Bit(0));
		tagReportContentSelector.setEnableLastSeenTimestamp(new Bit(0));
		tagReportContentSelector.setEnablePeakRSSI(new Bit(0));
		tagReportContentSelector.setEnableROSpecID(new Bit(1));
		tagReportContentSelector.setEnableSpecIndex(new Bit(0));
		tagReportContentSelector.setEnableTagSeenCount(new Bit(0));
		C1G2EPCMemorySelector epcMemSel = new C1G2EPCMemorySelector();
		epcMemSel.setEnableCRC(new Bit(0));
		epcMemSel.setEnablePCBits(new Bit(0));
		tagReportContentSelector
				.addToAirProtocolEPCMemorySelectorList(epcMemSel);
		roReportSpec.setTagReportContentSelector(tagReportContentSelector);
		setReaderConfig.setROReportSpec(roReportSpec);

		// Set default AccessReportSpec

		AccessReportSpec accessReportSpec = new AccessReportSpec();
		accessReportSpec.setAccessReportTrigger(new AccessReportTriggerType(
				AccessReportTriggerType.Whenever_ROReport_Is_Generated));
		setReaderConfig.setAccessReportSpec(accessReportSpec);

		// Set up reporting for AISpec events, ROSpec events, and GPI Events

		ReaderEventNotificationSpec eventNoteSpec = new ReaderEventNotificationSpec();
		EventNotificationState noteState = new EventNotificationState();
		noteState.setEventType(new NotificationEventType(
				NotificationEventType.AISpec_Event));
		noteState.setNotificationState(new Bit(0));
		eventNoteSpec.addToEventNotificationStateList(noteState);
		noteState = new EventNotificationState();
		noteState.setEventType(new NotificationEventType(
				NotificationEventType.ROSpec_Event));
		noteState.setNotificationState(new Bit(0));
		eventNoteSpec.addToEventNotificationStateList(noteState);
		noteState = new EventNotificationState();
		noteState.setEventType(new NotificationEventType(
				NotificationEventType.GPI_Event));
		noteState.setNotificationState(new Bit(0));
		eventNoteSpec.addToEventNotificationStateList(noteState);
		setReaderConfig.setReaderEventNotificationSpec(eventNoteSpec);

		setReaderConfig.setResetToFactoryDefault(new Bit(0));

		return setReaderConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.readers.impl.AbstractReaderSession#setStatus(org
	 * .rifidi.edge.core.api.SessionStatus)
	 */
	@Override
	protected synchronized void setStatus(SessionStatus status) {
		super.setStatus(status);
		// TODO: Remove this once we have aspectJ
		NotifierService service = notifierService;
		if (service != null) {
			service.sessionStatusChanged(this.readerID, this.getID(), status);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.readers.impl.AbstractReaderSession#submit(org.rifidi
	 * .edge.core.commands.Command, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public Integer submit(Command command, long interval, TimeUnit unit) {
		Integer retVal = super.submit(command, interval, unit);
		// TODO: Remove this once we have aspectJ
		try {
			NotifierService service = notifierService;
			if (service != null) {
				service.jobSubmitted(this.readerID, this.getID(), retVal,
						command.getCommandID());
			}
		} catch (Exception e) {
			// make sure the notification doesn't cause this method to exit
			// under any circumstances
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.core.readers.impl.AbstractReaderSession#killComand(java
	 * .lang.Integer)
	 */
	@Override
	public void killComand(Integer id) {
		super.killComand(id);
		// TODO: Remove this once we have aspectJ
		NotifierService service = notifierService;
		if (service != null) {
			service.jobDeleted(this.readerID, this.getID(), id);
		}
	}

	/**
	 * 
	 */
	@Override
	public void messageReceived(LLRPMessage arg0) {
		logger.debug("Asynchronous message recieved");
		// System.out.println("Asynchronous message recieved");
		if (arg0 instanceof RO_ACCESS_REPORT) {
			RO_ACCESS_REPORT rar = (RO_ACCESS_REPORT) arg0;
			List<TagReportData> trdl = rar.getTagReportDataList();

			// List<String> tagdatastring = new ArrayList<String>();
			Set<TagReadEvent> tagreaderevents = new HashSet<TagReadEvent>();

			for (TagReportData t : trdl) {
				AntennaID antid = t.getAntennaID();
				EPC_96 id = (EPC_96) t.getEPCParameter();
				// System.out.println("EPC data processed : "
				// + id.getEPC().toString(16));
				String EPCData = id.getEPC().toString(16);
				EPCGeneration2Event gen2event = new EPCGeneration2Event();
				gen2event.setEPCMemory(this.parseString(EPCData), 96);

				TagReadEvent tag = new TagReadEvent(readerID, gen2event, antid
						.getAntennaID().intValue(), System.currentTimeMillis());
				tagreaderevents.add(tag);
			}
			ReadCycle cycle = new ReadCycle(tagreaderevents, readerID, System
					.currentTimeMillis());
			sensor.send(cycle);
			this.getTemplate().send(this.getDestination(),
					new ObjectMessageCreator(cycle));
		}
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
		 * @param cycle
		 *            the tags to add to this message
		 */
		public ObjectMessageCreator(ReadCycle cycle) {
			super();
			objectMessage = new ActiveMQObjectMessage();

			try {
				objectMessage.setObject(cycle);
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

	/**
	 * Parse the given string for results.
	 * 
	 * @param input
	 * @return
	 */
	private BigInteger parseString(String input) {
		BigInteger retVal = null;

		try {
			input = input.trim();
			retVal = new BigInteger(input, 16);
		} catch (Exception e) {
			logger.warn("There was a problem when parsing LLRP Tags.  "
					+ "tag has not been added", e);
		}
		return retVal;
	}

}