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
package org.rifidi.edge.sensors;

/**
 * Thrown if a reader with the same name already exists.
 * 
 * @author Jochen Mader - jochen@pramari.com
 * 
 */
public class DuplicateSensorNameException extends Exception {

	/** Default serial version. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public DuplicateSensorNameException() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 * @param cause
	 */
	public DuplicateSensorNameException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 */
	public DuplicateSensorNameException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 */
	public DuplicateSensorNameException(Throwable cause) {
		super(cause);
	}

}
