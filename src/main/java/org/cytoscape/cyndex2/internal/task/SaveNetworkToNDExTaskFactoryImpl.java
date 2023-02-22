package org.cytoscape.cyndex2.internal.task;

import java.util.HashMap;
import java.util.UUID;
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyndex2.internal.errors.NetworkNotFoundInNDExException;
import org.cytoscape.cyndex2.internal.errors.ReadOnlyException;
import org.cytoscape.cyndex2.internal.errors.RemoteModificationException;
import org.cytoscape.cyndex2.internal.errors.WritePermissionException;
import org.cytoscape.cyndex2.internal.rest.parameter.NDExBasicSaveParameters;
import org.cytoscape.cyndex2.internal.ui.swing.SaveNetworkDialog;
import org.cytoscape.cyndex2.internal.util.NDExNetworkManager;
import org.cytoscape.cyndex2.internal.util.Server;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import org.cytoscape.cyndex2.internal.util.UpdateUtil;
import org.cytoscape.model.CyNetwork;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.cyndex2.internal.ui.swing.ShowDialogUtil;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.ndexbio.model.object.network.NetworkSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class SaveNetworkToNDExTaskFactoryImpl extends AbstractTaskFactory {
	private final static Logger LOGGER = LoggerFactory.getLogger(SaveNetworkToNDExTaskFactoryImpl.class);
	private final CyServiceRegistrar serviceRegistrar;
	private boolean _alwaysPromptUser;
	private SaveNetworkDialog _dialog;
	private ShowDialogUtil _dialogUtil;
	private long _progressDisplayDurationMillis;
	
	
	/**
	 * This constructor is mainly here to make testing easier by enabling caller to 
	 * set alternate ShowDialogUtil and SaveNetworkDialog objects
	 * 
	 * @param serviceRegistrar Used to get CySwingApplication, CyAppManager in createTaskIterator method
	 * @param alwaysPromptUser If true, user will be prompted with save as dialog 
	 *                         even if network was loaded from NDEx
	 * @param progressDisplayDurationMillis Time in millis to display save progress dialog
	 * @param dialogUtil Wrapper around JOptionPane show calls to make testing easier
	 * @param dialog Save dialog
	 */
	public SaveNetworkToNDExTaskFactoryImpl(CyServiceRegistrar serviceRegistrar, boolean alwaysPromptUser, long progressDisplayDurationMillis,
			ShowDialogUtil dialogUtil, SaveNetworkDialog dialog) {
		this.serviceRegistrar = serviceRegistrar;
		_alwaysPromptUser = alwaysPromptUser;
		_dialogUtil = dialogUtil;
		_dialog = dialog;
		_progressDisplayDurationMillis = progressDisplayDurationMillis;
		
	}
	
	/**
	 * Constructor that exposes alwaysPromptUser and creates default ShowDialogUtil and SaveNetworkDialog objects
	 * 
	 * @param serviceRegistrar Used to get CySwingApplication, CyAppManager in createTaskIterator method
	 * @param alwaysPromptUser If true, user will be prompted with save as dialog 
	 *                         even if network was loaded from NDEx
	 * @param progressDisplayDurationMillis Time in millis to display save progress dialog
	 */
	public SaveNetworkToNDExTaskFactoryImpl(CyServiceRegistrar serviceRegistrar, boolean alwaysPromptUser, long progressDisplayDurationMillis) {
		this(serviceRegistrar, alwaysPromptUser, progressDisplayDurationMillis, null, null);
		_dialogUtil = new ShowDialogUtil();
		_dialog = new SaveNetworkDialog(_dialogUtil);
	}

	/**
	 * Constructor where alwaysPromptUser is set to false
	 * 
	 * @param serviceRegistrar Used to get CySwingApplication, CyAppManager in createTaskIterator method
	 * @param progressDisplayDurationMillis Time in millis to display save progress dialog
	 */
	public SaveNetworkToNDExTaskFactoryImpl(CyServiceRegistrar serviceRegistrar, long progressDisplayDurationMillis) {
		this(serviceRegistrar, false,progressDisplayDurationMillis);
	}

	/**
	 * Always return true and if no network is selected the user will get a 
	 * dialog explaining why no save is possible
	 * @return 
	 */
	@Override
	public boolean isReady() {
		return true;
	}
	
	/**
	 * For selected network in session.
	 * 
	 * 1) If network has NDEx UUID and we have credentials that write permission 
	 *    a) If last modified is the same then just save the network
	 *    b) If last modified differs, warn the user the original network on NDEx
	 *       has been modified and ask if they want to overwrite
	 * 2) If network lacks NDEx UUID display save as dialog so user can choose how to
	 *    save the network
	 * @return 
	 */
	@Override
	public synchronized TaskIterator createTaskIterator() {
		final CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);
		final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);

		// check for a selected current network and if none found display dialog and
		// return
		CyNetwork currentNetwork = appManager.getCurrentNetwork();
		if (currentNetwork == null){
			_dialogUtil.showMessageDialog(swingApplication.getJFrame(), "Please select a network to save\n");
			return new TaskIterator(1, new CanceledTask());
		}
		
		// Set the save button to enabled for dialog
		_dialog.setNDExSaveEnabled(true);
		
		// Set the desired network name in the save dialog
		setDesiredNetworkNameInDialog(currentNetwork);
		
		// savedUUID will be non null if network came from NDEx
		UUID savedUUID = NDExNetworkManager.getUUID(currentNetwork);
		
		if (_alwaysPromptUser == false && savedUUID != null){
			// if network is from NDEx and we dont have to prompt the user, attempt to save
		    // the network and if that succeeds just return the TaskIterator 
			TaskIterator ti = createSaveOverwriteTaskIterator(swingApplication, currentNetwork, savedUUID);
			if (ti != null){
				return ti;
			}
		}
		
		// setup the gui dialog and exit if that fails
		if (_dialog.createGUI(savedUUID) == false){
			return new TaskIterator(1, new CanceledTask());
		}
		
		// display the save as dialog
		Object[] options = {_dialog.getMainSaveButton(), _dialog.getMainCancelButton()};
		int res = _dialogUtil.showOptionDialog(swingApplication.getJFrame(),
                                           this._dialog,
                               "Save as",
                           JOptionPane.YES_NO_OPTION,
                           JOptionPane.PLAIN_MESSAGE,
                           null,
                           options,
                           options[0]);
		
		// if res is 0 then the user wants to save the network
        if (res == 0){
			// Need to determine if an overwrite is desired
			NetworkSummary overwriteNetwork = _dialog.getNDExNetworkUserWantsToOverwrite();
			if (overwriteNetwork != null){
				NDExNetworkManager.updateModificationTimeStamp(currentNetwork, overwriteNetwork.getModificationTime());
			}
			// make sure the network name the user wants to save as is set as the network name
			// in Cytoscape
			currentNetwork.getRow(currentNetwork).set(CyNetwork.NAME, _dialog.getDesiredNetworkName());
			
			// create the save tasks and return the TaskIterator
			return createExportAndFinalizeTasks(currentNetwork, overwriteNetwork != null); 			
        }
		// If we are here the user did not wish to save the network. just return
		// the canceled task
		return new TaskIterator(1, new CanceledTask());
	}
	
	/**
	 * Gets the current network name and sets that name as the desired name
	 * in the save as dialog. If the raw name is null then an empty string 
	 * is set as the network name
	 * @param currentNetwork 
	 */
	private void setDesiredNetworkNameInDialog(CyNetwork currentNetwork){
		String desiredRawName = currentNetwork.getRow(currentNetwork).get(CyNetwork.NAME, String.class);
		String desiredName = desiredRawName == null ? "" : desiredRawName;
		_dialog.setDesiredNetworkName(desiredName);
	}
	
	/**
	 * Creates export task that saves the network and a finalize task that merely 
	 * adds a delay so the progress dialog is displayed to the user
	 * @param currentNetwork Network to save
	 * @param overwrite true if the save is an overwrite
	 * @return tasks to save the network
	 */
	private TaskIterator createExportAndFinalizeTasks(CyNetwork currentNetwork, boolean overwrite){
		NDExExportTaskFactory fac = new NDExExportTaskFactory(getNDExBasicSaveParameters(), overwrite);
		TaskIterator ti = fac.createTaskIterator(currentNetwork);
		ti.append(new NDExFinalizeSaveTask(_progressDisplayDurationMillis));
		return ti;
	}
	/**
	 * Checks if overwrite save to NDEx is possible and if it is, returns a TaskIterator
	 * with tasks to save the network to NDEx or to just cancel the process
	 * 
	 * The user is asked (via dialog) if they wish to overwrite the network if the network
	 * has a more recent modification time on the NDEx server. 
	 * 
	 * Otherwise the user is shown a dialog if the save can not be performed due to:
	 *  - Permissions
	 *  - Read only flag 
	 *  - Network is not actually on NDEx
	 *  - Any other error
	 * 
	 * @param swingApplication Cytoscape Desktop GUI
	 * @param currentNetwork Currently selected network
	 * @param savedUUID NDEX uuid found in hidden table 
	 * @return TaskIterator upon if overwrite is desired or save is not desired otherwise null
	 */
	private TaskIterator createSaveOverwriteTaskIterator(CySwingApplication swingApplication,
			CyNetwork currentNetwork, UUID savedUUID){
		String dialogMessage = null;
		try {
			Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
			// if no NDEx server is selected, just return cause we have no
			// way of connecting to NDEx
			if (selectedServer == null){
				LOGGER.debug("No NDEx credentials selected");
				return null;
			}
			
			// See if update is possible, this function raises exceptions if
			// there is an issue
			UpdateUtil.updateIsPossible(currentNetwork, savedUUID,
					selectedServer.getModelAccessLayer().getNdexRestClient(), 
					selectedServer.getModelAccessLayer());
			
			// Update is possible, create the save task with overwrite flag set to true
			return createExportAndFinalizeTasks(currentNetwork, true); 
		} catch(RemoteModificationException rme){
			// Network was modified on NDEx, ask the user if they want to overwrite
			Object[] options = {"Yes", "No"};
			int res = _dialogUtil.showOptionDialog(swingApplication.getJFrame(),
					"Network was modified on remote NDEx server.\n\n"
					+ "Do you wish to overwrite anyways?",
					"NDEx Overwrite",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[1]);
			if (res == 1){
				LOGGER.debug("User did not want to overwrite.");
				return new TaskIterator(1, new CanceledTask());
			}
			if (res == 0){
				LOGGER.debug("User does want to overwrite");
				NDExNetworkManager.updateModificationTimeStamp(currentNetwork,
						rme.getRemoteModification());
				if (_dialog.getDesiredNetworkName() != null){
					currentNetwork.getRow(currentNetwork).set(CyNetwork.NAME,
							_dialog.getDesiredNetworkName());
				}
				return createExportAndFinalizeTasks(currentNetwork, true); 
			}
		} catch(NetworkNotFoundInNDExException nfe){
			dialogMessage = nfe.getMessage() + "\n\nNetwork is linked to a network in NDEx, but that network\n"
					+ "\ndoes not exist or is not accessible on "
					+ ServerManager.INSTANCE.getSelectedServer().getUrl() + " server\n"
					+ "for user " + ServerManager.INSTANCE.getSelectedServer().getUsername();
			LOGGER.info(dialogMessage, nfe);
		} catch(ReadOnlyException re){
			dialogMessage = "Network is set to read only on "
					+ ServerManager.INSTANCE.getSelectedServer().getUrl() + " NDEx server\n"
					+ "for user " + ServerManager.INSTANCE.getSelectedServer().getUsername()
					+ " and cannot be saved.";
			
			LOGGER.info(dialogMessage, re);
		} catch(WritePermissionException wpe){
			dialogMessage = "You do not have permission to overwrite this network on "
					+ ServerManager.INSTANCE.getSelectedServer().getUrl() + " NDEx server\n"
					+ "as user " + ServerManager.INSTANCE.getSelectedServer().getUsername();
			LOGGER.info(dialogMessage, wpe);
		}catch(Exception ex){
			dialogMessage =  "Unable to save due to this error:\n\n" + ex.getMessage();
			LOGGER.info(dialogMessage, ex);
		}
		_dialogUtil.showMessageDialog(swingApplication.getJFrame(),
				dialogMessage + "\n\nClick ok to display Save Network As dialog");
		return null;
	}
	
	/**
	 * Gets NDEx credentials of the user currently selected profile
	 * 
	 * @return 
	 */
	private NDExBasicSaveParameters getNDExBasicSaveParameters(){
		NDExBasicSaveParameters params = new NDExBasicSaveParameters();
                                        params.username = ServerManager.INSTANCE.getSelectedServer().getUsername();
                                        params.password = ServerManager.INSTANCE.getSelectedServer().getPassword();
                                        params.serverUrl = ServerManager.INSTANCE.getSelectedServer().getUrl();
                                        params.metadata = new HashMap<>();
		return params;
	}
}
