package org.rifidi.edge.testing.thingmagic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rifidi.edge.core.exceptions.RifidiCommandInterruptedException;
import org.rifidi.edge.core.exceptions.RifidiConnectionException;
import org.rifidi.edge.core.readerplugin.ReaderInfo;
import org.rifidi.edge.core.readerplugin.service.ReaderPluginService;
import org.rifidi.edge.core.readersession.ReaderSession;
import org.rifidi.edge.core.readersession.service.ReaderSessionService;
import org.rifidi.edge.readerplugin.thingmagic.plugin.ThingMagicReaderInfo;
import org.rifidi.services.annotations.Inject;
import org.rifidi.services.registry.ServiceRegistry;


/**
 * @author Jerry Maine - jerry@pramari.com
 *
 */
public class ThingMagicTagRead {

	private ReaderSessionService readerSessionService;
	private ReaderPluginService readerPluginService;

	ByteArrayOutputStream output = new ByteArrayOutputStream();
	
	@Before
	public void setUp() throws Exception {
		ServiceRegistry.getInstance().service(this);
		//System.out.println(CommunicationTest.class);
		// System.out.println(MessageQueueImpl.class.getName());
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTagReadAndConnect() {
		if (readerSessionService == null)
			Assert.fail();
//		List<String> infos = readerPluginService.getAllReaderPlugins();
//		
//		Class<?> klass = null;
//
//		for (String k: infos ){
//			if (k.equals("org.rifidi.edge.readerplugin.thingmagic.ThingMagicReaderInfo"))
//				try {
//					klass = Class.forName(k);
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					Assert.fail();
//				}
//		}
//		
//		Constructor<?> constructor = null;
//		try {
//			constructor = klass.getConstructor();
//		} catch (SecurityException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			Assert.fail();
//		} catch (NoSuchMethodException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			Assert.fail();
//		}
		ReaderInfo info = new ThingMagicReaderInfo();
//		try {
//			info = (ReaderInfo) constructor.newInstance();
//		} catch (IllegalArgumentException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			Assert.fail();
//		} catch (InstantiationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			Assert.fail();
//		} catch (IllegalAccessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			Assert.fail();
//		} catch (InvocationTargetException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			Assert.fail();
//		}
		info.setIpAddress("localhost");
		info.setPort(8080);
		ReaderSession readerSession = readerSessionService
				.createReaderSesssion(info);

		try {
			readerSession.executeCommand("GetTagsCurrentlyOnAntennas");
		} catch (RifidiConnectionException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//e.printStackTrace(new PrintStream(output));
			//Assert.fail(output.toString());
			throw new AssertionError(e);
		} catch (RifidiCommandInterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace(new PrintStream(output));
			//Assert.fail(output.toString());
			throw new AssertionError(e);
		}
	}

	@Inject
	public void setReaderSessionService(ReaderSessionService service) {
		readerSessionService = service;
	}
	
	@Inject 
	public void setReaderPluginService(ReaderPluginService service){
		readerPluginService = service;
	}
}
