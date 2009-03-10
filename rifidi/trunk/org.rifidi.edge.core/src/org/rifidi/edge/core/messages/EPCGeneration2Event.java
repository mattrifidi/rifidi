/**
 * 
 */
package org.rifidi.edge.core.messages;

import java.math.BigInteger;

/**
 * @author Jochen Mader - jochen@pramari.com
 * 
 */
public class EPCGeneration2Event extends DatacontainerEvent {

	protected Integer epcLength;
	
	public EPCGeneration2Event() {
		super(4);
	}

	public void setReservedMemory(BigInteger memory) {
		setMemory(0, memory);
	}

	public void setEPCMemory(BigInteger memory) {
		setMemory(1, memory);
	}

	public void setTIDMemory(BigInteger memory) {
		setMemory(2, memory);
	}

	public void setUserMemory(BigInteger memory) {
		setMemory(3, memory);
	}

	public String getEPC_HEX() {
		return getMemoryBank(1).toString(16);
	}
	
	public String getEPC_BIN() {
		return getMemoryBank(1).toString(2);
	}

	/**
	 * @return the epcLength
	 */
	public Integer getEpcLength() {
		return epcLength;
	}

	/**
	 * @param epcLength the epcLength to set
	 */
	public void setEpcLength(Integer epcLength) {
		this.epcLength = epcLength;
	}
	
}
