
package org.rifidi.edge.epcglobal.alelr;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 3.1.4
 * 2015-12-12T10:17:43.116-05:00
 * Generated source version: 3.1.4
 */

@WebFault(name = "DuplicateNameException", targetNamespace = "urn:epcglobal:alelr:wsdl:1")
public class DuplicateNameExceptionResponse extends Exception {
    
    private org.rifidi.edge.epcglobal.alelr.DuplicateNameException duplicateNameException;

    public DuplicateNameExceptionResponse() {
        super();
    }
    
    public DuplicateNameExceptionResponse(String message) {
        super(message);
    }
    
    public DuplicateNameExceptionResponse(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateNameExceptionResponse(String message, org.rifidi.edge.epcglobal.alelr.DuplicateNameException duplicateNameException) {
        super(message);
        this.duplicateNameException = duplicateNameException;
    }

    public DuplicateNameExceptionResponse(String message, org.rifidi.edge.epcglobal.alelr.DuplicateNameException duplicateNameException, Throwable cause) {
        super(message, cause);
        this.duplicateNameException = duplicateNameException;
    }

    public org.rifidi.edge.epcglobal.alelr.DuplicateNameException getFaultInfo() {
        return this.duplicateNameException;
    }
}