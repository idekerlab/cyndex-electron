package org.cytoscape.cyndex2.internal;

import java.awt.Component;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.io.File;
import java.util.Dictionary;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyndex2.internal.rest.NdexClient;
import org.cytoscape.cyndex2.internal.rest.endpoints.NdexBaseResource;
import org.cytoscape.cyndex2.internal.rest.endpoints.NdexNetworkResource;
import org.cytoscape.cyndex2.internal.rest.endpoints.NdexStatusResource;
import org.cytoscape.cyndex2.internal.rest.endpoints.impl.NdexBaseResourceImpl;
import org.cytoscape.cyndex2.internal.rest.endpoints.impl.NdexNetworkResourceImpl;
import org.cytoscape.cyndex2.internal.rest.endpoints.impl.NdexStatusResourceImpl;
import org.cytoscape.cyndex2.internal.rest.errors.ErrorBuilder;
import org.cytoscape.cyndex2.internal.task.OpenBrowseTaskFactory;
import org.cytoscape.cyndex2.internal.task.OpenSaveCollectionTaskFactory;
import org.cytoscape.cyndex2.internal.task.OpenSaveTaskFactory;
import org.cytoscape.cyndex2.internal.task.OpenNetworkFromNDExTaskFactoryImpl;
import org.cytoscape.cyndex2.internal.task.SaveNetworkToNDExTaskFactoryImpl;
import org.cytoscape.cyndex2.internal.ui.ImportUserNetworkFromNDExTaskFactory;
import org.cytoscape.cyndex2.internal.ui.ImportNetworkFromNDExTaskFactory;
import org.cytoscape.cyndex2.internal.ui.MainToolBarAction;
import org.cytoscape.cyndex2.internal.ui.SaveNetworkToNDExTaskFactory;
import org.cytoscape.cyndex2.internal.ui.swing.BindHotKeysPanel;
import org.cytoscape.cyndex2.internal.ui.swing.OpenNetworkDialog;
import org.cytoscape.cyndex2.internal.ui.swing.ShowDialogUtil;
import org.cytoscape.cyndex2.internal.util.CIServiceManager;
import org.cytoscape.cyndex2.internal.util.CyNDExPropertyListener;
import org.cytoscape.cyndex2.internal.util.ExternalAppManager;
import org.cytoscape.cyndex2.internal.util.IconUtil;
import org.cytoscape.cyndex2.internal.util.OpenSaveHotKeyChanger;
import org.cytoscape.cyndex2.internal.util.StringResources;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.RootNetworkCollectionTaskFactory;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;
import static org.cytoscape.work.ServiceProperties.TOOLTIP_LONG_DESCRIPTION;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyActivator extends AbstractCyActivator {

	public static final String CYNDEX2_OWNER = "cyndex2";
	
	public static final String OPEN_SESSION = "Open Session...";
	public static final String SAVE_SESSION = "Save Session";
	public static final String SAVE_SESSION_AS = "Save Session As...";
	
	
	public static final String OPEN_NETWORK = "Open Network";
	public static final String SAVE_NETWORK = "Save Network";
	public static final String SAVE_NETWORK_AS = "Save Network As...";
	
	public static final String FILE_MENU_NAME = "File";
	
	public static final String DISABLE_HOTKEY_CONTROL_PROPERTY = "cyndex2.disable.hotkey.control";
	// Logger for this activator
	private static final Logger logger = LoggerFactory.getLogger(CyActivator.class);
	
	private static CyProperty<Properties> cyProps;

	private static String appVersion;
	private static String cytoscapeVersion;
	private static String appName;
	private static boolean hasCyNDEx1;

	private CIServiceManager ciServiceManager;
	public static TaskManager<?, ?> taskManager;
	private static CyEventHelper _cyEventHelper;

	public CyActivator() {
		super();
		
		hasCyNDEx1 = false;
	}
	
	public static String getProperty(String prop) {
		return cyProps.getProperties().getProperty(prop);
	}

	public static String getCyRESTPort() {
		String port = cyProps.getProperties().getProperty("rest.port");
		if (port == null) {
			return "1234";
		}
		return port;
	}
	
	public static String getCytoscapeVersion() {
		String version = cyProps.getProperties().getProperty("cytoscape.version.number");
		return version;
	}
	
	public static boolean useDefaultBrowser() {
		String val = cyProps.getProperties().getProperty("cyndex2.defaultBrowser");
		return Boolean.parseBoolean(val);
	}
	
	/**
	 * If does not exist then return true, if exists then return value of
	 * property as a boolean 
	 * @return 
	 */
	public static boolean persistSelectedNodesEdges(){
		String val = cyProps.getProperties().getProperty("cyndex2.persistSelectedNodesEdges");
		if (val == null){
			return true;
		}
		return Boolean.parseBoolean(val);
	}
	
	/**
	 * Gets the value of the property cyndex2.progressDisplayDuration which 
	 * denotes the minimum time in ms the save dialog should be displayed when 
	 * saving networks to NDEx. 
	 * @return Value of property or 3000 (3 seconds) if unable to parse property
	 */
	public static long progressDisplayDuration(){
		String val = cyProps.getProperties().getProperty("cyndex2.progressDisplayDuration");
		if (val == null){
			return 3000l;
		}
		try {
			return Long.parseLong(val);
		} catch(NumberFormatException nfe){
			logger.warn("Unable to convert value of cyndex2.progressDisplayDuration property to long, using default value of 3000: " + nfe.getMessage());
			return 3000l;
		}
	}
	
	/**
	 * Number of networks to display in "My Networks" and "Search NDEx" tables
	 * as dictated by cyndex2.numberOfNDExNetworksToList property
	 * @return Value of property or 400 if unset or there was a parsing error
	 */
	public static int numberOfNDExNetworksToList(){
		String val = cyProps.getProperties().getProperty("cyndex2.numberOfNDExNetworksToList");
		if (val == null){
			return 400;
		}
		try {
			return Integer.parseInt(val);
		} catch(NumberFormatException nfe){
			logger.warn("Unable to convert value of cyndex2.numberOfNDExNetworksToList property to integer, using default value of 400: " + nfe.getMessage());
			return 400;
		}
	}
	
	/**
	 * Checks property to see if this app should NOT take over control of hotkeys
	 * by examining cyndex2.disable.hotkey.control property which needs to be set to true
	 * or 1
	 * @return true if App should NOT take over hotkeys, false otherwise
	 */
	public static boolean disableAppControlOfHotKeys(){
		if (cyProps == null){
			return false;
		}
		String val = cyProps.getProperties().getProperty(DISABLE_HOTKEY_CONTROL_PROPERTY);
		if (val == null || val.trim().isEmpty()){
			return false;
		}
		return Boolean.parseBoolean(val);
	}
	
	/**
	 * Updates cyndex2.disable.hotkey.control property with **val** passed in
	 * or does nothing if cyProps was not initialized in this object
	 * @param val 
	 */
	public static void setDisableAppControlOfHotKeys(boolean val){
		if (cyProps == null){
			return;
		}
		cyProps.getProperties().setProperty("cyndex2.disable.hotkey.control", Boolean.toString(val));
		// need to fire event
		_cyEventHelper.fireEvent(new PropertyUpdatedEvent(cyProps));
	}
	
	/**
	 * Renames menus, appending Session to Open, Save, Save As, and Close. This is only really
	 * needed for Cytoscape 3.9.1 and earlier since 3.10 will have this change already
	 * 
	 * @param menu 
	 */
	private void renameOpenSaveAndSaveAsMenus(JMenu menu){
		if (menu == null){
			logger.info("MENU IS NULL");
		}
		logger.info("Number of menu items: " + menu.getMenuComponentCount());

		for (Component c : menu.getMenuComponents()){
			logger.debug("Menu component: " + c.toString());
			if (c instanceof JMenuItem){
				JMenuItem curMenuItem = (JMenuItem)c;
				if (curMenuItem.getText().equals("Open...") || curMenuItem.getText().equals(CyActivator.OPEN_SESSION)){
					curMenuItem.setText(CyActivator.OPEN_SESSION);
				} else if (curMenuItem.getText().equals("Save") || curMenuItem.getText().equals(CyActivator.SAVE_SESSION)){
					curMenuItem.setText(CyActivator.SAVE_SESSION);
				} else if (curMenuItem.getText().equals("Save As...") || curMenuItem.getText().equals(CyActivator.SAVE_SESSION_AS)){
					curMenuItem.setText(CyActivator.SAVE_SESSION_AS);
				} else if (curMenuItem.getText().equals("Open Recent")){
					curMenuItem.setText("Open Recent Session");
				} else if (curMenuItem.getText().equals("Close")){
					curMenuItem.setText("Close Session");
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void start(BundleContext bc) throws InvalidSyntaxException {
		
		for (Bundle b : bc.getBundles()) {
			// System.out.println(b.getSymbolicName());
			if (b.getSymbolicName().equals("org.cytoscape.api-bundle")) {
				cytoscapeVersion = b.getVersion().toString();
				// break;
			} else if (b.getSymbolicName().equals("org.cytoscape.ndex.cyNDEx")) {
				/*
				 * Version v = b.getVersion(); System.out.println(v); int st = b.getState();
				 * System.out.println(st);
				 */
				hasCyNDEx1 = true;
			}
		}
		Bundle currentBundle = bc.getBundle();

		appVersion = currentBundle.getVersion().toString();

		Dictionary<?, ?> d = currentBundle.getHeaders();
		appName = (String) d.get("Bundle-name");

		
		// Import dependencies
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(serviceRegistrar);
		final CyApplicationConfiguration config = getService(bc, CyApplicationConfiguration.class);
		final CyApplicationManager appManager = getService(bc, CyApplicationManager.class);
		final CySwingApplication swingApplication = getService(bc, CySwingApplication.class);
		CyServiceModule.setSwingApplication(swingApplication);
		
		
		cyProps = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		taskManager = getService(bc, TaskManager.class);
		
		_cyEventHelper = getService(bc, CyEventHelper.class);
		
		// For Cytoscape versions earlier then 3.10, append Session to menu names
		renameOpenSaveAndSaveAsMenus(swingApplication.getJMenu(CyActivator.FILE_MENU_NAME));
		
		// Check preferences to see if we should move the hot keys
		boolean disableHotKeyControl = CyActivator.disableAppControlOfHotKeys();
		
		
	    File configDir = config.getAppConfigurationDirectoryLocation(CyActivator.class); 
	    configDir.mkdirs(); 
		
		// For loading network
	    CxTaskFactoryManager tfManager = CxTaskFactoryManager.INSTANCE;
		registerServiceListener(bc, tfManager, "addReaderFactory", "removeReaderFactory", InputStreamTaskFactory.class);
		registerServiceListener(bc, tfManager, "addWriterFactory", "removeWriterFactory",
				CyNetworkViewWriterFactory.class);

		ciServiceManager = new CIServiceManager(bc);

		// Create subdirectories in config dir for jxbrowser
		final CyNetworkManager netmgr = getService(bc, CyNetworkManager.class);
		//File jxBrowserDir = new File(config.getConfigurationDirectoryLocation(), "jxbrowser");
		//jxBrowserDir.mkdir();
		//BrowserManager.setDataDirectory(new File(jxBrowserDir, "data"));
		
		// TF for NDEx Save Network
		final OpenSaveTaskFactory ndexSaveNetworkTaskFactory = new OpenSaveTaskFactory(appManager);
		final Properties ndexSaveNetworkTaskFactoryProps = new Properties();

		ndexSaveNetworkTaskFactoryProps.setProperty(PREFERRED_MENU, "File.Export");
		ndexSaveNetworkTaskFactoryProps.setProperty(MENU_GRAVITY, "0.0");
		ndexSaveNetworkTaskFactoryProps.setProperty(TITLE, "Network to NDEx...");
		registerService(bc, ndexSaveNetworkTaskFactory, TaskFactory.class, ndexSaveNetworkTaskFactoryProps);
		ShowDialogUtil dialogUtil = new ShowDialogUtil();
		OpenNetworkDialog openDialog = new OpenNetworkDialog(CyActivator.numberOfNDExNetworksToList(), 
				new BindHotKeysPanel());
		
		final OpenNetworkFromNDExTaskFactoryImpl openNetworkFac = new OpenNetworkFromNDExTaskFactoryImpl(serviceRegistrar, openDialog, dialogUtil);
		final Properties openNetworkTaskFactoryProps = new Properties();
		openNetworkTaskFactoryProps.setProperty(PREFERRED_MENU, CyActivator.FILE_MENU_NAME);
		openNetworkTaskFactoryProps.setProperty(TOOLTIP, "Open Network from NDEx");
		openNetworkTaskFactoryProps.setProperty(TOOLTIP_LONG_DESCRIPTION, "Open Network from NDEx");
        openNetworkTaskFactoryProps.setProperty(MENU_GRAVITY, "0.1");
		openNetworkTaskFactoryProps.setProperty(TITLE, CyActivator.OPEN_NETWORK);
		
		registerService(bc, openNetworkFac, TaskFactory.class, openNetworkTaskFactoryProps);
		
		long progressDisplayDuration = CyActivator.progressDisplayDuration();
		
		final SaveNetworkToNDExTaskFactoryImpl saveNetworkFac = new SaveNetworkToNDExTaskFactoryImpl(serviceRegistrar, progressDisplayDuration);
		final Properties saveNetworkTaskFactoryProps = new Properties();
		saveNetworkTaskFactoryProps.setProperty(ID, "saveNetworkToCloudTaskFactory");
		saveNetworkTaskFactoryProps.setProperty(PREFERRED_MENU, CyActivator.FILE_MENU_NAME);
		saveNetworkTaskFactoryProps.setProperty(TITLE, CyActivator.SAVE_NETWORK);
		
		saveNetworkTaskFactoryProps.setProperty(MENU_GRAVITY, "0.2");
		saveNetworkTaskFactoryProps.setProperty(TOOLTIP, "Save a network to NDEx or a session to a file");
		saveNetworkTaskFactoryProps.setProperty(TOOLTIP_LONG_DESCRIPTION, "Saves a network to NDEx or a session to file (.cys)");
		
		registerService(bc, saveNetworkFac, TaskFactory.class, saveNetworkTaskFactoryProps);
		
		final SaveNetworkToNDExTaskFactoryImpl saveNetworkFacAlwaysPrompt = new SaveNetworkToNDExTaskFactoryImpl(serviceRegistrar, true, progressDisplayDuration);
		final Properties saveNetworkTaskFactoryPropsPrompt = new Properties();
		saveNetworkTaskFactoryPropsPrompt.setProperty(ID, "saveNetworkToCloudAlwaysPromptTaskFactory");
		saveNetworkTaskFactoryPropsPrompt.setProperty(PREFERRED_MENU, CyActivator.FILE_MENU_NAME);
		
		saveNetworkTaskFactoryPropsPrompt.setProperty(TITLE, CyActivator.SAVE_NETWORK_AS);
		saveNetworkTaskFactoryPropsPrompt.setProperty(MENU_GRAVITY, "0.3");
		saveNetworkTaskFactoryPropsPrompt.setProperty(INSERT_SEPARATOR_AFTER, "true");
		saveNetworkTaskFactoryPropsPrompt.setProperty(TOOLTIP, "Save a network to NDEx or a session to a file");
		saveNetworkTaskFactoryPropsPrompt.setProperty(TOOLTIP_LONG_DESCRIPTION, "Saves a network to NDEx or a session to file (.cys)");
		
		registerService(bc, saveNetworkFacAlwaysPrompt, TaskFactory.class, saveNetworkTaskFactoryPropsPrompt);
		
		// TF for NDEx Save Collection
		final OpenSaveCollectionTaskFactory ndexSaveCollectionTaskFactory = new OpenSaveCollectionTaskFactory(appManager);
		final Properties ndexSaveCollectionTaskFactoryProps = new Properties();

		ndexSaveCollectionTaskFactoryProps.setProperty(PREFERRED_MENU, "File.Export");
		ndexSaveCollectionTaskFactoryProps.setProperty(MENU_GRAVITY, "0.1");
		ndexSaveCollectionTaskFactoryProps.setProperty(TITLE, "Collection to NDEx...");
		registerService(bc, ndexSaveCollectionTaskFactory, TaskFactory.class, ndexSaveCollectionTaskFactoryProps);
		
		ImportNetworkFromNDExTaskFactory importFromNDExTaskFactory = new ImportNetworkFromNDExTaskFactory(ExternalAppManager.APP_NAME_LOAD);
		ImportUserNetworkFromNDExTaskFactory importUserNetworkTaskFactory = new ImportUserNetworkFromNDExTaskFactory(ExternalAppManager.APP_NAME_LOAD);
		
		SaveNetworkToNDExTaskFactory saveToNDExTaskFactory = new SaveNetworkToNDExTaskFactory(appManager, ExternalAppManager.APP_NAME_SAVE);

		
		MainToolBarAction action = new MainToolBarAction(importFromNDExTaskFactory, importUserNetworkTaskFactory, saveToNDExTaskFactory, serviceRegistrar);
		registerService(bc, action, CyAction.class);
		
		// TF for NDEx Load
		Icon icon = IconUtil.getNdexIcon();
		
		final OpenBrowseTaskFactory ndexTaskFactory = new OpenBrowseTaskFactory(icon);
		final Properties ndexTaskFactoryProps = new Properties();
		// ndexTaskFactoryProps.setProperty(IN_MENU_BAR, "false");
		ndexTaskFactoryProps.setProperty(PREFERRED_MENU, "File.Import");
		ndexTaskFactoryProps.setProperty(MENU_GRAVITY, "0.0");
		ndexTaskFactoryProps.setProperty(TITLE, "Network from NDEx...");
		registerAllServices(bc, ndexTaskFactory, ndexTaskFactoryProps);

		// Expose CyREST endpoints
		final ErrorBuilder errorBuilder = new ErrorBuilder(ciServiceManager, config);
		final NdexClient ndexClient = new NdexClient(errorBuilder);
		CyServiceModule.setErrorBuilder(errorBuilder);
		
		// Base
		registerService(bc,
				new NdexBaseResourceImpl(bc.getBundle().getVersion().toString(), ciServiceManager),
				NdexBaseResource.class, new Properties());

		// Status
		registerService(bc, new NdexStatusResourceImpl(ciServiceManager), NdexStatusResource.class,
				new Properties());

		// Network IO
		registerService(bc, new NdexNetworkResourceImpl(ndexClient, appManager, netmgr, ciServiceManager),
				NdexNetworkResource.class, new Properties());

		OpenSaveTaskFactory saveNetworkToNDExContextMenuTaskFactory = new OpenSaveTaskFactory(appManager);
		Properties saveNetworkToNDExContextMenuProps = new Properties();
		saveNetworkToNDExContextMenuProps.setProperty(ID, "exportToNDEx");
		saveNetworkToNDExContextMenuProps.setProperty(TITLE, StringResources.NDEX_SAVE.concat("..."));
		saveNetworkToNDExContextMenuProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
		saveNetworkToNDExContextMenuProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		saveNetworkToNDExContextMenuProps.setProperty(ENABLE_FOR, "network");

		registerService(bc, saveNetworkToNDExContextMenuTaskFactory, NetworkCollectionTaskFactory.class,
				saveNetworkToNDExContextMenuProps);
		
		OpenSaveCollectionTaskFactory saveCollectionToNDExContextMenuTaskFactory = new OpenSaveCollectionTaskFactory(appManager);
		Properties saveCollectionToNDExContextMenuProps = new Properties();
		saveNetworkToNDExContextMenuProps.setProperty(ID, "saveCollectionToNDEx");
		saveCollectionToNDExContextMenuProps.setProperty(TITLE, StringResources.NDEX_SAVE_COLLECTION.concat("..."));
		saveCollectionToNDExContextMenuProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
		saveCollectionToNDExContextMenuProps.setProperty(MENU_GRAVITY, "1.0");
		registerService(bc, saveCollectionToNDExContextMenuTaskFactory, RootNetworkCollectionTaskFactory.class,
				saveCollectionToNDExContextMenuProps);
		
		OpenSaveHotKeyChanger hotKeyChanger = new OpenSaveHotKeyChanger(swingApplication.getJMenu(CyActivator.FILE_MENU_NAME));
		
		// if disableHotKeyControl is false then we CAN put the
		// hotkeys onto the network menus, otherwise leave things as
		// is 
		if (disableHotKeyControl == false){
			hotKeyChanger.putHotKeysOntoNetworkMenus();
		}
		registerAllServices(bc, new CyNDExPropertyListener(hotKeyChanger, disableHotKeyControl));
	}

	@Override
	public void shutDown() {
		logger.info("Shutting down CyNDEx-2...");

		if (ciServiceManager != null) {
			ciServiceManager.close();
		}
		
		super.shutDown();
	}

	public static String getCyVersion() {
		return cytoscapeVersion;
	}

	public static String getAppName() {
		return appName;
	}

	public static boolean hasCyNDEx1() {
		return hasCyNDEx1;
	}

	public static void setHasCyNDEX1(boolean hasCyNDEx1) {
		CyActivator.hasCyNDEx1 = hasCyNDEx1;
	}

	public static String getAppVersion() {
		return appVersion;
	}

}