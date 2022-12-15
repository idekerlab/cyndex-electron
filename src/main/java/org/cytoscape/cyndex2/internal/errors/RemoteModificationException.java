
package org.cytoscape.cyndex2.internal.errors;

import java.sql.Timestamp;

/**
 * Exception raised when network on remote server has more recent
 * modification timestamp
 * 
 * @author churas
 */
public class RemoteModificationException extends Exception {
	Timestamp _remoteModification;
	
	public RemoteModificationException() {
		super();
	}
	
	public RemoteModificationException(String message){
		super(message);
	}
	
	public RemoteModificationException(String message, Timestamp remoteModification){
		super(message);
		_remoteModification = remoteModification;
	}
	
	public RemoteModificationException(String message, Throwable cause){
		super(message, cause);
	}
	
	public RemoteModificationException(Throwable cause){
		super(cause);
	}
	
	/**
	 * Timestamp of modification from remote server
	 * @return 
	 */
	public Timestamp getRemoteModification(){
		return _remoteModification;
	}
}
