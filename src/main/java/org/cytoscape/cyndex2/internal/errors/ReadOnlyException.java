
package org.cytoscape.cyndex2.internal.errors;

/**
 * Exception raised when network is read only
 * 
 * @author churas
 */
public class ReadOnlyException extends Exception {
	public ReadOnlyException() {
		super();
	}
	
	public ReadOnlyException(String message){
		super(message);
	}
	
	public ReadOnlyException(String message, Throwable cause){
		super(message, cause);
	}
	
	public ReadOnlyException(Throwable cause){
		super(cause);
	}
}
