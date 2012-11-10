/**
 * 
 */
package org.rifidi.edge.adapter.llrp;

import java.util.concurrent.TimeUnit;

import org.rifidi.edge.sensors.AbstractTagWritingService;
import org.rifidi.edge.sensors.CannotExecuteException;

/**
 * @author kyle
 *
 */
public class LLRPTagWritingService extends AbstractTagWritingService<LLRPReaderSession> {

	/* (non-Javadoc)
	 * @see org.rifidi.edge.sensors.management.AbstractTagWritingService#writeEPC(java.lang.String, int, byte[])
	 */
	@Override
	public void writeEPC(String readerID, int antenna, byte[] data)
			throws CannotExecuteException {
		WriteCommand command = new WriteCommand("Write Command ID");
		super.getSession(readerID).submitAndBlock(command, 5000, TimeUnit.MILLISECONDS);
		if(command.getException()!=null){
			throw command.getException();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.rifidi.edge.sensors.management.AbstractTagWritingService#writeUser(java.lang.String, int, byte[])
	 */
	@Override
	public void writeUser(String readerID, int antenna, byte[] data)
			throws CannotExecuteException {
		WriteCommand command = new WriteCommand("Write Command ID");
		super.getSession(readerID).submitAndBlock(command, 5000, TimeUnit.MILLISECONDS);
		if(command.getException()!=null){
			throw command.getException();
		}
		
	}
	
}