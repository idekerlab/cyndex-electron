
package org.cytoscape.cyndex2.internal.errors;

/**
 * Exception raised when there was an error checking network
 * permissions
 * 
 * @author churas
 */
public class CheckPermissionException extends Exception {
	public CheckPermissionException() {
		super();
	}
	
	public CheckPermissionException(String message){
		super(message);
	}
	
	public CheckPermissionException(String message, Throwable cause){
		super(message, cause);
	}
	
	public CheckPermissionException(Throwable cause){
		super(cause);
	}
}
