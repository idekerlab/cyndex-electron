package org.cytoscape.cyndex2.internal.task;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyndex2.internal.errors.NetworkNotFoundInNDExException;
import org.cytoscape.cyndex2.internal.errors.RemoteModificationException;
import org.cytoscape.cyndex2.internal.rest.parameter.NDExBasicSaveParameters;
import org.cytoscape.cyndex2.internal.ui.swing.SaveSessionOrNetworkDialog;
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

public class SaveSessionOrNetworkToNDExTaskFactoryImpl extends AbstractTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;
	private boolean _alwaysPromptUser;
	private SaveSessionOrNetworkDialog _dialog;
	private ShowDialogUtil _dialogUtil;
	private Server _ndexServer;
	
	public SaveSessionOrNetworkToNDExTaskFactoryImpl(CyServiceRegistrar serviceRegistrar, boolean alwaysPromptUser) {
		this.serviceRegistrar = serviceRegistrar;
		_alwaysPromptUser = alwaysPromptUser;
		_dialogUtil = new ShowDialogUtil();
		_dialog = new SaveSessionOrNetworkDialog(_dialogUtil);
		
	}

	public SaveSessionOrNetworkToNDExTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this(serviceRegistrar, false);
	}

	@Override
	public boolean isReady() {
		final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		// check if network is already on NDEx and we have valid credentials...
		//if (appManager.getCurrentNetwork() != null){
		//		return true;
		//}
		//always return true cause the user may want to save a session
		return true;
	}
	
	/**
	 * For selected network in session.
	 * 
	 * 1) If network has NDEx UUID and we have credentials that write permission 
	 *    a) If last modified is the same then just save the network
	 *    b) If last modified differs, warn the user the original network on NDEx
	 *       has been modified, so if you want to save you have to manually override UUID
	 *       (TODO need to display new gui for this)
	 * 2) If network lacks NDEx UUID display save as dialog so user can choose how to
	 *    save the network and depending on gui response save network to ndex or save session
	 * @return 
	 */
	@Override
	public synchronized TaskIterator createTaskIterator() {
		final CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);

		final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		// check if network is already on NDEx and we have valid credentials...
		CyNetwork currentNetwork = appManager.getCurrentNetwork();
		UUID savedUUID = null;
		if (currentNetwork != null){
			savedUUID = NDExNetworkManager.getUUID(currentNetwork);
			_dialog.setNDExSaveEnabled(true);
			String desiredRawName = currentNetwork.getRow(currentNetwork).get(CyNetwork.NAME, String.class);
			String desiredName = desiredRawName == null ? "" : desiredRawName;
			_dialog.setDesiredNetworkName(desiredName);
		} else {
			_dialog.setNDExSaveEnabled(false);
		}
		
		if (currentNetwork != null && _alwaysPromptUser == false && savedUUID != null){
			try {
				Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
				if (selectedServer != null){
					UpdateUtil.updateIsPossible(currentNetwork, savedUUID,
							selectedServer.getModelAccessLayer().getNdexRestClient(), 
							selectedServer.getModelAccessLayer());
					NDExBasicSaveParameters params = new NDExBasicSaveParameters();
					params.username = ServerManager.INSTANCE.getSelectedServer().getUsername();
					params.password = ServerManager.INSTANCE.getSelectedServer().getPassword();
					params.serverUrl = ServerManager.INSTANCE.getSelectedServer().getUrl();
					params.metadata = new HashMap<>();

					NDExExportTaskFactory fac = new NDExExportTaskFactory(params, true);
					return fac.createTaskIterator(currentNetwork);
				}
			} catch(RemoteModificationException rme){
					Object[] options = {"Yes", "No"};
					int res = _dialogUtil.showOptionDialog(swingApplication.getJFrame(), "Network was modified on remote NDEx server.\n\n"
							+ "Do you wish to overwrite anyways?",
							"NDEx Overwrite",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
							options, options[1]);
					if (res == 1){
						System.out.println("User did not want to overwrite. just return");
						return new TaskIterator(1, new CanceledTask());
					}
					if (res == 0){
						System.out.println("User does want to overwrite");
						NDExNetworkManager.updateModificationTimeStamp(currentNetwork, rme.getRemoteModification());
						if (_dialog.getDesiredNetworkName() != null){
							currentNetwork.getRow(currentNetwork).set(CyNetwork.NAME, _dialog.getDesiredNetworkName());
						}
						NDExExportTaskFactory fac = new NDExExportTaskFactory(getNDExBasicSaveParameters(), true);
						return fac.createTaskIterator(currentNetwork);
					}
			} catch(NetworkNotFoundInNDExException nfe){
				_dialogUtil.showMessageDialog(swingApplication.getJFrame(), "Network is linked to network in NDEx, but that network\n("
					+ savedUUID.toString()	+ ")\ndoes not exist or is not accessible on " + ServerManager.INSTANCE.getSelectedServer().getUrl() + " server\n" + 
							"for user " + ServerManager.INSTANCE.getSelectedServer().getUsername() + "\n\nClick ok to display Save Network As dialog");
			} catch(Exception ex){
				ex.printStackTrace();

				_dialogUtil.showMessageDialog(swingApplication.getJFrame(), "Save error, due to this error:\n\n"
						+ ex.getMessage() + "\n\nGoing to bring up save as dialog, but let Cytoscape developers know how you want to proceed?");         
			}
		}
		
		if (_dialog.createGUI() == false){
			return new TaskIterator(1, new CanceledTask());
		}
		
		
		Object[] options = {_dialog.getMainSaveButton(), _dialog.getMainCancelButton()};
		int res = _dialogUtil.showOptionDialog(swingApplication.getJFrame(),
                                           this._dialog,
                               "Save as",
                           JOptionPane.YES_NO_OPTION,
                           JOptionPane.PLAIN_MESSAGE,
                           null,
                           options,
                           options[0]);
		// if res is 0 then the user wants to save the network or session
        if (res == 0){
			// if open session card was displayed see if there is a file to load
			if (_dialog.getSelectedCard().equals(SaveSessionOrNetworkDialog.SAVE_SESSION)){
				File sessionFile = _dialog.getSelectedSessionFile();
				if (sessionFile != null){
					System.out.println("selected session file is: " + sessionFile.getAbsolutePath());
					return new TaskIterator(1, new SaveSessionAsTask(sessionFile, serviceRegistrar));
				} else {
					System.out.println("selected session file is null");
				}
			} else if (_dialog.getSelectedCard().equals(SaveSessionOrNetworkDialog.SAVE_NDEX)){

				// Need to determine if an overwrite is desired
                NetworkSummary overwriteNetwork = _dialog.getNDExNetworkUserWantsToOverwrite();
				if (overwriteNetwork != null){
					NDExNetworkManager.updateModificationTimeStamp(currentNetwork, overwriteNetwork.getModificationTime());
				}
				currentNetwork.getRow(currentNetwork).set(CyNetwork.NAME, _dialog.getDesiredNetworkName());
                NDExExportTaskFactory fac = new NDExExportTaskFactory(getNDExBasicSaveParameters(), overwriteNetwork != null);
				return fac.createTaskIterator(currentNetwork);   
			}
			
        }
		return new TaskIterator(1, new CanceledTask());
	}
	
	private NDExBasicSaveParameters getNDExBasicSaveParameters(){
		NDExBasicSaveParameters params = new NDExBasicSaveParameters();
                                        params.username = ServerManager.INSTANCE.getSelectedServer().getUsername();
                                        params.password = ServerManager.INSTANCE.getSelectedServer().getPassword();
                                        params.serverUrl = ServerManager.INSTANCE.getSelectedServer().getUrl();
                                        params.metadata = new HashMap<>();
		return params;
	}
}
