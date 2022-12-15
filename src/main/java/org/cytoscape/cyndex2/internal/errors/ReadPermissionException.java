
package org.cytoscape.cyndex2.internal.errors;

/**
 * Exception raised when there was an error getting network
 * permissions from the server
 * 
 * @author churas
 */
public class ReadPermissionException extends Exception {
	public ReadPermissionException() {
		super();
	}
	
	public ReadPermissionException(String message){
		super(message);
	}
	
	public ReadPermissionException(String message, Throwable cause){
		super(message, cause);
	}
	
	public ReadPermissionException(Throwable cause){
		super(cause);
	}
}
