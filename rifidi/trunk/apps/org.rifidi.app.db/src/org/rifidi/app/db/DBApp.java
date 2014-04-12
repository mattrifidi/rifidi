/*******************************************************************************
 * Copyright (c) 2014 Transcends, LLC.
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation; either version 2 of the 
 * License, or (at your option) any later version. This program is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You 
 * should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, 
 * USA. 
 * http://www.gnu.org/licenses/gpl-2.0.html
 *******************************************************************************/
package org.rifidi.app.db;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.rifidi.edge.api.AbstractRifidiApp;
import org.rifidi.edge.api.service.tagmonitor.ReadZone;
import org.rifidi.edge.api.service.tagmonitor.ReadZoneMonitoringService;
import org.rifidi.edge.api.service.tagmonitor.ReadZoneSubscriber;

/**
 * @author Matthew Dean - matt@transcends.co
 * 
 */
public class DBApp extends AbstractRifidiApp {

	// Property names
	public static final String TIMEOUT = "Timeout";

	private DatabaseConnection conn;

	/** The service for monitoring arrival and departure events */
	private ReadZoneMonitoringService readZoneMonitoringService;

	private float timeout;
	
	private List<ReadZoneSubscriber> subscriberList;

	public DBApp(String group, String name) {
		super(group, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.api.AbstractRifidiApp#_start()
	 */
	@Override
	public void _start() {
		super._start();
		this.conn.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.api.AbstractRifidiApp#_stop()
	 */
	@Override
	public void _stop() {
		for (ReadZoneSubscriber s : this.subscriberList) {
			this.readZoneMonitoringService.unsubscribe(s);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.edge.api.AbstractRifidiApp#initialize()
	 */
	@Override
	public void initialize() {
		this.timeout = Float.parseFloat(this.getProperty(TIMEOUT, "8.0"));
		DBSubscriber sub = new DBSubscriber(this.conn);
		this.subscriberList = new LinkedList<ReadZoneSubscriber>();
		this.subscriberList.add(sub);
		this.readZoneMonitoringService.subscribe(sub,
				new LinkedList<ReadZone>(), this.timeout, TimeUnit.SECONDS);
	}

	/**
	 * Called by spring.
	 * 
	 * @param conn
	 */
	public void setDatabaseConnection(DatabaseConnection conn) {
		this.conn = conn;
	}

	/**
	 * Called by spring. This method injects the ReadZoneMonitoringService into
	 * the application.
	 * 
	 * @param rzms
	 */
	public void setReadZoneMonitoringService(ReadZoneMonitoringService rzms) {
		this.readZoneMonitoringService = rzms;
	}

}
