package org.cytoscape.cyndex2.internal.util;

import java.util.Properties;
import org.cytoscape.cyndex2.internal.CyActivator;

import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for property change and checks if hotkeys properties for cyndex2
 * has been changed, if so, handler moves the hotkeys
 * 
 * @author churas
 */
public class CyNDExPropertyListener implements PropertyUpdatedListener {
	
	// Logger for this activator
	private static final Logger logger = LoggerFactory.getLogger(CyNDExPropertyListener.class);
	private boolean _controlHotKeys;
	private OpenSaveHotKeyChanger _changer;
	
	/**
	 * Constructor
	 * 
	 * @param changer object that moves the hotkeys
	 * @param controlHotKeys initial value of hotkey preference
	 */
	public CyNDExPropertyListener(OpenSaveHotKeyChanger changer, boolean controlHotKeys){
		_changer = changer;
		_controlHotKeys = controlHotKeys;
	}
	
	/**
	 * Moves hotkeys to network if cyndex2 disable hotkey control property is false,
	 * otherwise moves hotkeys to session. If property has not changed since last
	 * invocation, no change is performed
	 * 
	 * @param e Event denoting a property change
	 */
	@Override
	public void handleEvent(PropertyUpdatedEvent e) {
		boolean newControlHotKeysVal = CyActivator.disableAppControlOfHotKeys();
		if (_controlHotKeys != newControlHotKeysVal){
			logger.info("Hot Keys control changed to: " + Boolean.toString(newControlHotKeysVal));
			_controlHotKeys = newControlHotKeysVal;
			if (_controlHotKeys == false){
				_changer.putHotKeysOntoNetworkMenus();
			} else {
				_changer.putHotKeysOntoSessionMenus();
			}
		}
	}
	
}
