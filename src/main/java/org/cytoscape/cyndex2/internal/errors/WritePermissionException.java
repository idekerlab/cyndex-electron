
package org.cytoscape.cyndex2.internal.errors;

/**
 * Exception raised when caller does not have write permission
 * 
 * @author churas
 */
public class WritePermissionException extends Exception {
	public WritePermissionException() {
		super();
	}
	
	public WritePermissionException(String message){
		super(message);
	}
	
	public WritePermissionException(String message, Throwable cause){
		super(message, cause);
	}
	
	public WritePermissionException(Throwable cause){
		super(cause);
	}
}
