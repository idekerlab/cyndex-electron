package org.cytoscape.cyndex2.internal.task;

import java.io.File;
import javax.swing.JOptionPane;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyndex2.internal.ui.OpenNetworkDialog;
import org.cytoscape.cyndex2.internal.util.Server;
import org.cytoscape.cyndex2.internal.util.ServerManager;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.ndex.ui.ShowDialogUtil;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.ndexbio.model.object.network.NetworkSummary;
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

public class OpenSessionOrNetworkFromNDExTaskFactoryImpl extends AbstractTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	private OpenNetworkDialog _dialog;
	private ShowDialogUtil _dialogUtil;
	
	public OpenSessionOrNetworkFromNDExTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		_dialog = new OpenNetworkDialog();
		_dialogUtil = new ShowDialogUtil();
	}

	@Override
	public boolean isReady() {
		return super.isReady();
	}
	
	@Override
	public synchronized TaskIterator createTaskIterator() {
		final CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);
		TaskIterator taskIterator = null;
		
		if (_dialog.createGUI() == false){
			return new TaskIterator();
		}
		
		Object[] options = {_dialog.getMainOpenButton(), _dialog.getMainCancelButton()};
		int res = _dialogUtil.showOptionDialog(swingApplication.getJFrame(),
                                           this._dialog,
                               "Open",
                           JOptionPane.YES_NO_OPTION,
                           JOptionPane.PLAIN_MESSAGE,
                           null,
                           options,
                           options[0]);
		// if res is 0 then the user wants to open the network
        if (res == 0){
			// if open session card was displayed see if there is a file to load
			if (_dialog.getSelectedCard().equals(OpenNetworkDialog.OPEN_SESSION)){
				File sessionFile = _dialog.getSelectedSessionFile();
				if (sessionFile != null){
					return new TaskIterator(1, new OpenSessionTask(sessionFile, serviceRegistrar));
				}
			// else if ndex card was displayed, see if there is a network to load
			} else if (_dialog.getSelectedCard().equals(OpenNetworkDialog.OPEN_NDEX)){
				NetworkSummary netSummary = _dialog.getNDExSelectedNetwork();
				if (netSummary != null){
					try{
						NdexRestClientModelAccessLayer client = ServerManager.INSTANCE.getSelectedServer().getModelAccessLayer();
						
						return new TaskIterator(1, new NetworkImportTask(client,
								netSummary.getExternalId(), null, true));
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			}
        }
		return new TaskIterator();
		//return new TaskIterator(1, new OpenNetworkFromNDExTask(serviceRegistrar));
	}
}
