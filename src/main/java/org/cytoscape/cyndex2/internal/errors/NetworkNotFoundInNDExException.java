
package org.cytoscape.cyndex2.internal.errors;

/**
 * Exception raised when network was NOT found in NDEx
 * 
 * @author churas
 */
public class NetworkNotFoundInNDExException extends Exception {
	public NetworkNotFoundInNDExException() {
		super();
	}
	
	public NetworkNotFoundInNDExException(String message){
		super(message);
	}
	
	public NetworkNotFoundInNDExException(String message, Throwable cause){
		super(message, cause);
	}
	
	public NetworkNotFoundInNDExException(Throwable cause){
		super(cause);
	}
}
