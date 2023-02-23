package org.cytoscape.cyndex2.internal.util;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
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
	
	/**
	 * Names of the session menu items
	 */
	public final static Set<String> SESSION_MENU_NAMES = new HashSet<>(Arrays.asList(CyActivator.OPEN_SESSION,
			CyActivator.SAVE_SESSION, CyActivator.SAVE_SESSION_AS));
	
	/**
	 * Names of the network menu items
	 */
	public final static Set<String> NETWORK_MENU_NAMES = new HashSet<>(Arrays.asList(CyActivator.OPEN_NETWORK,
			CyActivator.SAVE_NETWORK, CyActivator.SAVE_NETWORK_AS));

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(OpenSaveHotKeyChanger.class);
	
	/**
	 * Menu passed in via the constructor which contains
	 * the menu items to modify
	 */
	private JMenu _fileMenu;
	
	/**
	 * Cmd o or Ctrl o hotkey
	 */
	private static final KeyStroke OPEN_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_O,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	
	/**
	 * Cmd s or Ctrl s hotkey
	 */
	private static final KeyStroke SAVE_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_S,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	
	/**
	 * Cmd shift s or Ctrl shift s hotkey
	 */
	private static final KeyStroke SAVEAS_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_S,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK);
	
	
	public OpenSaveHotKeyChanger(JMenu fileMenu){
		_fileMenu = fileMenu;
	}

	/**
	 * Removes accelerators from session open, save, and save as file menu 
	 * items and puts accelerators onto the network open, save, and save as 
	 * file menu items
	 * 
	 * @param menu 
	 */
	public void putHotKeysOntoNetworkMenus(){
		if (_fileMenu == null){
			logger.debug("MENU IS NULL");
			return;
		}
				
		logger.debug("Number of menu items: " + _fileMenu.getMenuComponentCount());
		for (Component c : _fileMenu.getMenuComponents()){
			logger.debug("Menu component: " + c.toString());
			if (c instanceof JMenuItem){
				JMenuItem curMenuItem = (JMenuItem)c;				
				if (SESSION_MENU_NAMES.contains(curMenuItem.getText())){
					curMenuItem.setAccelerator(null);
				} else if (curMenuItem.getText().equals(CyActivator.OPEN_NETWORK)){
					curMenuItem.setAccelerator(OPEN_KEYSTROKE);
				} else if (curMenuItem.getText().equals(CyActivator.SAVE_NETWORK)){
					curMenuItem.setAccelerator(SAVE_KEYSTROKE);
				} else if (curMenuItem.getText().equals(CyActivator.SAVE_NETWORK_AS)){
					curMenuItem.setAccelerator(SAVEAS_KEYSTROKE);
				} 
			}
		}
	}
	
	/**
	 * Removes accelerators from network open, save, and save as file menu 
	 * items and puts accelerators onto the session open, save, and save as 
	 * file menu items
	 * 
	 * @param menu 
	 */
	public void putHotKeysOntoSessionMenus(){
		if (_fileMenu == null){
			logger.debug("MENU IS NULL");
			return;
		}
		
		logger.debug("Number of menu items: " + _fileMenu.getMenuComponentCount());
		for (Component c : _fileMenu.getMenuComponents()){
			logger.debug("Menu component: " + c.toString());
			if (c instanceof JMenuItem){
				JMenuItem curMenuItem = (JMenuItem)c;				
				if (NETWORK_MENU_NAMES.contains(curMenuItem.getText())){
					curMenuItem.setAccelerator(null);
				} else if (curMenuItem.getText().equals(CyActivator.OPEN_SESSION)){
					curMenuItem.setAccelerator(OPEN_KEYSTROKE);
				} else if (curMenuItem.getText().equals(CyActivator.SAVE_SESSION)){
					curMenuItem.setAccelerator(SAVE_KEYSTROKE);
				} else if (curMenuItem.getText().equals(CyActivator.SAVE_SESSION_AS)){
					curMenuItem.setAccelerator(SAVEAS_KEYSTROKE);
				} 
			}
		}
	}
}
