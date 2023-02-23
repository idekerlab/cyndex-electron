package org.cytoscape.cyndex2.util;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.cytoscape.cyndex2.internal.CyActivator;
import org.junit.Test;
import org.cytoscape.cyndex2.internal.util.OpenSaveHotKeyChanger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OpenSaveHotKeyChangerTests {
	
	public static final KeyStroke OPEN_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_O,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke SAVE_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_S,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
	public static final KeyStroke SAVEAS_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_S,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK);
	
	
	@Test
	public void testPutHotKeysOntoNetworkNullMenus(){
		OpenSaveHotKeyChanger changer = new OpenSaveHotKeyChanger(null);
		changer.putHotKeysOntoNetworkMenus();
	}
	
	@Test
	public void testPutHotKeysOntoSessionNullMenus(){
		OpenSaveHotKeyChanger changer = new OpenSaveHotKeyChanger(null);
		changer.putHotKeysOntoSessionMenus();
	}
	
	@Test
	public void testPutHotKeysOntoNetworkMenus(){
		JMenu menu = new JMenu();
		HashMap<String, JMenuItem> menuMap = new HashMap<>();
		for (String menuName : OpenSaveHotKeyChanger.NETWORK_MENU_NAMES){
			JMenuItem menuItem = new JMenuItem(menuName);
			menu.add(menuItem);
			menuMap.put(menuName, menuItem);
		}
		for (String menuName : OpenSaveHotKeyChanger.SESSION_MENU_NAMES){
			JMenuItem menuItem = new JMenuItem(menuName);
			menu.add(menuItem);
			menuMap.put(menuName, menuItem);
		}
		JMenuItem otherMenuItem = new JMenuItem("Open");
		menu.add(otherMenuItem);
		
		OpenSaveHotKeyChanger changer = new OpenSaveHotKeyChanger(menu);
		changer.putHotKeysOntoNetworkMenus();
		
		for (String menuName : OpenSaveHotKeyChanger.SESSION_MENU_NAMES){
			assertNull(menuMap.get(menuName).getAccelerator());
		}
		assertEquals(OPEN_KEYSTROKE, menuMap.get(CyActivator.OPEN_NETWORK).getAccelerator());
		assertEquals(SAVE_KEYSTROKE, menuMap.get(CyActivator.SAVE_NETWORK).getAccelerator());
		assertEquals(SAVEAS_KEYSTROKE, menuMap.get(CyActivator.SAVE_NETWORK_AS).getAccelerator());
		assertNull(otherMenuItem.getAccelerator());
	}
	
	@Test
	public void testPutHotKeysOntoSessionMenus(){
		JMenu menu = new JMenu();
		HashMap<String, JMenuItem> menuMap = new HashMap<>();
		for (String menuName : OpenSaveHotKeyChanger.NETWORK_MENU_NAMES){
			JMenuItem menuItem = new JMenuItem(menuName);
			menu.add(menuItem);
			menuMap.put(menuName, menuItem);
		}
		for (String menuName : OpenSaveHotKeyChanger.SESSION_MENU_NAMES){
			JMenuItem menuItem = new JMenuItem(menuName);
			menu.add(menuItem);
			menuMap.put(menuName, menuItem);
		}
		JMenuItem otherMenuItem = new JMenuItem("Blah");
		menu.add(otherMenuItem);
		
		OpenSaveHotKeyChanger changer = new OpenSaveHotKeyChanger(menu);
		changer.putHotKeysOntoSessionMenus();
		
		for (String menuName : OpenSaveHotKeyChanger.NETWORK_MENU_NAMES){
			assertNull(menuMap.get(menuName).getAccelerator());
		}
		assertEquals(OPEN_KEYSTROKE, menuMap.get(CyActivator.OPEN_SESSION).getAccelerator());
		assertEquals(SAVE_KEYSTROKE, menuMap.get(CyActivator.SAVE_SESSION).getAccelerator());
		assertEquals(SAVEAS_KEYSTROKE, menuMap.get(CyActivator.SAVE_SESSION_AS).getAccelerator());
		assertNull(otherMenuItem.getAccelerator());
	}
	
	
	
}



