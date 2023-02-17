package org.cytoscape.cyndex2.internal.util;

import java.awt.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.cytoscape.cyndex2.internal.CyActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to migrate accelerator hotkeys between session and network open and save
 * File menu items
 * @author churas
 */
public class OpenSaveHotKeyChanger {	
	
	public final static Set<String> SESSION_MENU_NAMES = new HashSet<>(Arrays.asList(CyActivator.OPEN_SESSION, CyActivator.SAVE_SESSION, CyActivator.SAVE_SESSION_AS));
	public final static Set<String> NETWORK_MENU_NAMES = new HashSet<>(Arrays.asList(CyActivator.OPEN_NETWORK, CyActivator.SAVE_NETWORK, CyActivator.SAVE_NETWORK_AS));

	// Logger for this activator
	private static final Logger logger = LoggerFactory.getLogger(OpenSaveHotKeyChanger.class);
	
	
	private Map<String, KeyStroke> getMenuHotKeys(JMenu menu, Set<String> menuNames){
		HashMap<String, KeyStroke> acceleratorMap = new HashMap<>();
		if (menu == null){
			logger.info("MENU IS NULL");
			return acceleratorMap;
		}
		logger.info("Number of menu items: " + menu.getMenuComponentCount());
		for (Component c : menu.getMenuComponents()){
			logger.debug("Menu component: " + c.toString());
			if (c instanceof JMenuItem){
				JMenuItem curMenuItem = (JMenuItem)c;
				if (menuNames.contains(curMenuItem.getText())){
					acceleratorMap.put(curMenuItem.getText(), curMenuItem.getAccelerator());
				}
			}
		}
		return acceleratorMap;
	}

	/**
	 * Takes accelerators found on session open, save, and save as file menu 
	 * items and moves them to the network open, save, and save as file menu items
	 * @param menu 
	 */
	public void putHotKeysOntoNetworkMenus(JMenu menu){
		if (menu == null){
			logger.info("MENU IS NULL");
		}
		
		Map<String, KeyStroke> acceleratorMap = getMenuHotKeys(menu, SESSION_MENU_NAMES);
		
		logger.info("Number of menu items: " + menu.getMenuComponentCount());
		for (Component c : menu.getMenuComponents()){
			logger.debug("Menu component: " + c.toString());
			if (c instanceof JMenuItem){
				JMenuItem curMenuItem = (JMenuItem)c;				
				if (acceleratorMap.containsKey(curMenuItem.getText())){
					curMenuItem.setAccelerator(null);
				} else if (curMenuItem.getText().equals(CyActivator.OPEN_NETWORK)){
					curMenuItem.setAccelerator(acceleratorMap.get(CyActivator.OPEN_SESSION));
				} else if (curMenuItem.getText().equals(CyActivator.SAVE_NETWORK)){
					curMenuItem.setAccelerator(acceleratorMap.get(CyActivator.SAVE_SESSION));
				} else if (curMenuItem.getText().equals(CyActivator.SAVE_NETWORK_AS)){
					curMenuItem.setAccelerator(acceleratorMap.get(CyActivator.SAVE_SESSION_AS));
				} 
			}
		}
	}
	
	/**
	 * Takes accelerators found on network open, save, and save as file menu 
	 * items and moves them to the session open, save, and save as file menu items
	 * @param menu 
	 */
	public void putHotKeysOntoSessionMenus(JMenu menu){
		if (menu == null){
			logger.info("MENU IS NULL");
		}

		Map<String, KeyStroke> acceleratorMap = getMenuHotKeys(menu, NETWORK_MENU_NAMES);
		
		logger.info("Number of menu items: " + menu.getMenuComponentCount());
		for (Component c : menu.getMenuComponents()){
			logger.debug("Menu component: " + c.toString());
			if (c instanceof JMenuItem){
				JMenuItem curMenuItem = (JMenuItem)c;				
				if (acceleratorMap.containsKey(curMenuItem.getText())){
					curMenuItem.setAccelerator(null);
				} else if (curMenuItem.getText().equals(CyActivator.OPEN_SESSION)){
					curMenuItem.setAccelerator(acceleratorMap.get(CyActivator.OPEN_NETWORK));
				} else if (curMenuItem.getText().equals(CyActivator.SAVE_SESSION)){
					curMenuItem.setAccelerator(acceleratorMap.get(CyActivator.SAVE_NETWORK));
				} else if (curMenuItem.getText().equals(CyActivator.SAVE_SESSION_AS)){
					curMenuItem.setAccelerator(acceleratorMap.get(CyActivator.SAVE_NETWORK_AS));
				} 
			}
		}
	}
}
