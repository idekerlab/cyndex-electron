package org.cytoscape.cyndex2.internal.task;

import java.util.UUID;
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyndex2.internal.ui.swing.SaveSessionOrNetworkDialog;
import org.cytoscape.cyndex2.internal.util.NDExNetworkManager;
import org.cytoscape.cyndex2.internal.util.Server;
import org.cytoscape.model.CyNetwork;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.ndex.ui.ShowDialogUtil;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

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
		_dialog = new SaveSessionOrNetworkDialog();
		_dialogUtil = new ShowDialogUtil();
	}

	public SaveSessionOrNetworkToNDExTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this(serviceRegistrar, false);
	}

	@Override
	public boolean isReady() {
		//serviceRegistrar.getService(serviceClass)
		return super.isReady(); //To change body of generated methods, choose Tools | Templates.
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
		
		/*
		final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		// check if network is already on NDEx and we have valid credentials...
		CyNetwork currentNetwork = appManager.getCurrentNetwork();
		UUID ndexUUID = NDExNetworkManager.getUUID(currentNetwork);
		if (ndexUUID != null && _ndexServer.getUsername() != null){
			try {
				NdexRestClientModelAccessLayer ndexAccessLayer = _ndexServer.getModelAccessLayer();
				
			} catch(Exception e){
				e.printStackTrace();
			}
		}*/
		
		
		if (_dialog.createGUI() == false){
			return new TaskIterator();
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
			/**
			// if open session card was displayed see if there is a file to load
			if (_dialog.getSelectedCard().equals(OpenDialog.OPEN_SESSION)){
				File sessionFile = _dialog.getSelectedSessionFile();
				if (sessionFile != null){
					return new TaskIterator(1, new OpenSessionTask(sessionFile, serviceRegistrar));
				}
			// else if ndex card was displayed, see if there is a file to load
			} else if (_dialog.getSelectedCard().equals(OpenDialog.OPEN_NDEX)){
				NetworkSummary netSummary = _dialog.getNDExSelectedNetwork();
				if (netSummary != null){
					try{
						NdexRestClient raw_client = new NdexRestClient("cbass", "test12345", "public.ndexbio.org");
						NdexRestClientModelAccessLayer client = new NdexRestClientModelAccessLayer(raw_client);
						return new TaskIterator(1, new OpenNetworkFromNDExTask(netSummary, client, serviceRegistrar));
					} catch(Exception e){
					
					}
				}
			}
			*/
        }
		return new TaskIterator();
	}
}
