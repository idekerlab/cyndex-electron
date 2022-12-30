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
import org.cytoscape.cyndex2.internal.task.OpenSessionOrNetworkFromNDExTaskFactoryImpl;
import org.cytoscape.cyndex2.internal.task.SaveSessionOrNetworkToNDExTaskFactoryImpl;
import org.cytoscape.cyndex2.internal.ui.ImportUserNetworkFromNDExTaskFactory;
import org.cytoscape.cyndex2.internal.ui.ImportNetworkFromNDExTaskFactory;
import org.cytoscape.cyndex2.internal.ui.MainToolBarAction;
import org.cytoscape.cyndex2.internal.ui.SaveNetworkToNDExTaskFactory;
import org.cytoscape.cyndex2.internal.ui.swing.OpenSessionOrNetworkDialog;
import org.cytoscape.cyndex2.internal.ui.swing.ShowDialogUtil;
import org.cytoscape.cyndex2.internal.util.CIServiceManager;
import org.cytoscape.cyndex2.internal.util.ExternalAppManager;
import org.cytoscape.cyndex2.internal.util.IconUtil;
import org.cytoscape.cyndex2.internal.util.StringResources;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.RootNetworkCollectionTaskFactory;
import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
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
	// Logger for this activator
	private static final Logger logger = LoggerFactory.getLogger(CyActivator.class);
	
	private static CyProperty<Properties> cyProps;

	private static String appVersion;
	private static String cytoscapeVersion;
	private static String appName;
	private static boolean hasCyNDEx1;

	private CIServiceManager ciServiceManager;
	public static TaskManager<?, ?> taskManager;

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
			logger.warn("Unable to convert value of cyndex2.progressDisplayDuration property to integer, using default value of 400: " + nfe.getMessage());
			return 400;
		}
	}
	
	public static boolean canThisAppControlSaveHotKey(){
		String val = cyProps.getProperties().getProperty("cytoscape.save.hotkey.owner");
		if (val == null || val.trim().isEmpty()){
			return false;
		}
		if (val.equalsIgnoreCase(CyActivator.CYNDEX2_OWNER)){
			return true;
		}
		return false;
	}
	
	public static boolean canThisAppControlSaveAsHotKey(){
		String val = cyProps.getProperties().getProperty("cytoscape.saveas.hotkey.owner");
		if (val == null || val.trim().isEmpty()){
			return false;
		}
		if (val.equalsIgnoreCase(CyActivator.CYNDEX2_OWNER)){
			return true;
		}
		return false;
	}
	
	public static boolean canThisAppControlOpenHotKey(){
		String val = cyProps.getProperties().getProperty("cytoscape.open.hotkey.owner");
		if (val == null || val.trim().isEmpty()){
			return false;
		}
		if (val.equalsIgnoreCase(CyActivator.CYNDEX2_OWNER)){
			return true;
		}
		return false;
	}
	
	private boolean renameOpenSaveAndSaveAsMenus(JMenu menu){
		if (menu == null){
			logger.info("MENU IS NULL");
			return false;
		}
		logger.info("Number of menu items: " + menu.getMenuComponentCount());
		boolean openUpdated = false;
		boolean saveUpdated = false;
		boolean saveAsUpdated = false;
		for (Component c : menu.getMenuComponents()){
			logger.debug("Menu component: " + c.toString());
			if (c instanceof JMenuItem){
				JMenuItem curMenuItem = (JMenuItem)c;
				if (curMenuItem.getText().equals("Open...")){
					curMenuItem.setText("Open Session...");
					curMenuItem.setAccelerator(null);
					openUpdated = true;
					
				} else if (curMenuItem.getText().equals("Save")){
					curMenuItem.setText("Save Session");
					curMenuItem.setAccelerator(null);
					saveUpdated = true;

				} else if (curMenuItem.getText().equals("Save As...")){
					curMenuItem.setText("Save Session As...");
					curMenuItem.setAccelerator(null);
					saveAsUpdated = true;
				}
			}
		}
		if (openUpdated == saveUpdated && saveUpdated == saveAsUpdated){
			logger.warn("Open... menu  renamed to Open Session..., "
					+ "Save menu renamed to Save Session, Save As... menu renamed to Save Session As... "
					+ "and accelerators have been removed");
			return true;
		}
		return false;
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
		
		boolean menusRenamed = this.renameOpenSaveAndSaveAsMenus(swingApplication.getJMenu("File"));
		
		cyProps = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		taskManager = getService(bc, TaskManager.class);
		
		
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
		OpenSessionOrNetworkDialog openDialog = new OpenSessionOrNetworkDialog(CyActivator.numberOfNDExNetworksToList());
		
		final OpenSessionOrNetworkFromNDExTaskFactoryImpl openSessionOrNetworkFac = new OpenSessionOrNetworkFromNDExTaskFactoryImpl(serviceRegistrar, openDialog, dialogUtil);
		final Properties ndexOpenNetworkTaskFactoryProps = new Properties();
		ndexOpenNetworkTaskFactoryProps.setProperty(PREFERRED_MENU, "File");
		ndexOpenNetworkTaskFactoryProps.setProperty(TOOLTIP, "Open Network from NDEx or Session");
		ndexOpenNetworkTaskFactoryProps.setProperty(TOOLTIP_LONG_DESCRIPTION, "Open Network from NDEx or Session");
		ndexOpenNetworkTaskFactoryProps.setProperty(MENU_GRAVITY, "1.0");

		String openName = "Open Network";
		if (menusRenamed == true){
			openName = "Open...";
			ndexOpenNetworkTaskFactoryProps.setProperty(MENU_GRAVITY, "0.9");
			ndexOpenNetworkTaskFactoryProps.setProperty(ACCELERATOR, "cmd o");
		}
		ndexOpenNetworkTaskFactoryProps.setProperty(TITLE, openName);
		//ndexOpenNetworkTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY, "1.1");
		//ndexOpenNetworkTaskFactoryProps.setProperty(IN_TOOL_BAR, "true");
		
		registerService(bc, openSessionOrNetworkFac, TaskFactory.class, ndexOpenNetworkTaskFactoryProps);
		
		long progressDisplayDuration = CyActivator.progressDisplayDuration();
		
		final SaveSessionOrNetworkToNDExTaskFactoryImpl saveSessionOrNetworkFac = new SaveSessionOrNetworkToNDExTaskFactoryImpl(serviceRegistrar, progressDisplayDuration);
		final Properties ndexSaveSessionOrNetworkTaskFactoryProps = new Properties();
		ndexSaveSessionOrNetworkTaskFactoryProps.setProperty(ID, "saveSessionOrNetworkToCloudTaskFactory");
		ndexSaveSessionOrNetworkTaskFactoryProps.setProperty(PREFERRED_MENU, "File");
		
		String saveName = "Save Network";
		if (menusRenamed == true){
			saveName = "Save";
			ndexSaveSessionOrNetworkTaskFactoryProps.setProperty(ACCELERATOR, "cmd s");
		}
		ndexSaveSessionOrNetworkTaskFactoryProps.setProperty(TITLE, saveName);
		
		ndexSaveSessionOrNetworkTaskFactoryProps.setProperty(MENU_GRAVITY, "1.4");
		ndexSaveSessionOrNetworkTaskFactoryProps.setProperty(TOOLTIP, "Save a network to NDEx or a session to a file");
		ndexSaveSessionOrNetworkTaskFactoryProps.setProperty(TOOLTIP_LONG_DESCRIPTION, "Saves a network to NDEx or a session to file (.cys)");
		registerService(bc, saveSessionOrNetworkFac, TaskFactory.class, ndexSaveSessionOrNetworkTaskFactoryProps);
		
		final SaveSessionOrNetworkToNDExTaskFactoryImpl saveSessionOrNetworkFacAlwaysPrompt = new SaveSessionOrNetworkToNDExTaskFactoryImpl(serviceRegistrar, true, progressDisplayDuration);
		final Properties ndexSaveSessionOrNetworkTaskFactoryPropsPrompt = new Properties();
		ndexSaveSessionOrNetworkTaskFactoryPropsPrompt.setProperty(ID, "saveSessionOrNetworkToCloudAlwaysPromptTaskFactory");
		ndexSaveSessionOrNetworkTaskFactoryPropsPrompt.setProperty(PREFERRED_MENU, "File");
		
		String saveAsName = "Save Network As...";
		if (menusRenamed == true){
			saveAsName = "Save As...";
			ndexSaveSessionOrNetworkTaskFactoryPropsPrompt.setProperty(ACCELERATOR, "cmd shift s");
		}
		ndexSaveSessionOrNetworkTaskFactoryPropsPrompt.setProperty(TITLE, saveAsName);
		ndexSaveSessionOrNetworkTaskFactoryPropsPrompt.setProperty(MENU_GRAVITY, "1.5");
		ndexSaveSessionOrNetworkTaskFactoryPropsPrompt.setProperty(TOOLTIP, "Save a network to NDEx or a session to a file");
		ndexSaveSessionOrNetworkTaskFactoryPropsPrompt.setProperty(TOOLTIP_LONG_DESCRIPTION, "Saves a network to NDEx or a session to file (.cys)");
		registerService(bc, saveSessionOrNetworkFacAlwaysPrompt, TaskFactory.class, ndexSaveSessionOrNetworkTaskFactoryPropsPrompt);
		
		
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
		
		if (menusRenamed){
			/**
			ShowDialogUtil dialogUtil = new ShowDialogUtil();
			dialogUtil.showMessageDialog(swingApplication.getJFrame(),
					"<html><center><font color=\"#ff0000\" size=\"+4\"><b>NOT FOR PRODUCTION</b></font></center><br/><br/>"
					+ "<font color=\"#ff0000\">THIS IMPLEMENTATION IS EXPERIMENTAL AND MAY CAUSE DATA LOSS</font><br/><br/>"
					+ "CyNDEx2 Core App has <font color=\"#ff0000\"><b>replaced</b></font> "
					+ "<b>Open..., Save</b>, & <b>Save As...</b> File menu items & associated hot keys<br/><br/>"
					+ "For legacy behavior, uninstall CyNDEx-2 App & restart Cytsocape <b>or</b> use <b>Open Session, Save Session,</b> or <b>Save Session As</b> menu items<br/><br/>"
					+ "<font color=\"#ff0000\">YOU HAVE BEEN WARNED!!!!</font><br/><br/>"
					+ "Have a nice day.</html>", "Warning", JOptionPane.ERROR_MESSAGE);
			*/
			logger.warn("Experimental CyNDEx-2 app has replaced open/save menus with internal versions!!!!");
		}
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