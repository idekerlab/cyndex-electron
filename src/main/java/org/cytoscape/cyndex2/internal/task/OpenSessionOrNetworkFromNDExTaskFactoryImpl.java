package org.cytoscape.cyndex2.internal.task;

import java.io.File;
import javax.swing.JOptionPane;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyndex2.internal.ui.swing.OpenSessionOrNetworkDialog;
import org.cytoscape.cyndex2.internal.util.ServerManager;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.cyndex2.internal.ui.swing.ShowDialogUtil;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;
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

public class OpenSessionOrNetworkFromNDExTaskFactoryImpl extends AbstractTaskFactory {
	private final static Logger LOGGER = LoggerFactory.getLogger(OpenSessionOrNetworkFromNDExTaskFactoryImpl.class);
	
	private final CyServiceRegistrar serviceRegistrar;

	private OpenSessionOrNetworkDialog _dialog;
	private ShowDialogUtil _dialogUtil;
	
	public OpenSessionOrNetworkFromNDExTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		_dialog = new OpenSessionOrNetworkDialog();
		_dialogUtil = new ShowDialogUtil();
	}

	@Override
	public boolean isReady() {
		return super.isReady();
	}
	
	@Override
	public synchronized TaskIterator createTaskIterator() {
		final CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);		
		if (_dialog.createGUI() == false){
			return new TaskIterator(1, new CanceledTask());
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
			if (_dialog.getSelectedCard().equals(OpenSessionOrNetworkDialog.OPEN_SESSION)){
				File sessionFile = _dialog.getSelectedSessionFile();
				if (sessionFile != null){
					return new TaskIterator(1, new OpenSessionTask(sessionFile, serviceRegistrar));
				}
			// else if ndex card was displayed, see if there is a network to load
			} else if (_dialog.getSelectedCard().equals(OpenSessionOrNetworkDialog.OPEN_NDEX)){
				NetworkSummary netSummary = _dialog.getNDExSelectedNetwork();
				if (netSummary != null){
					try{
						NdexRestClientModelAccessLayer client = ServerManager.INSTANCE.getSelectedServer().getModelAccessLayer();
						return new TaskIterator(1, new NetworkImportTask(client,
								netSummary.getExternalId(), null, true));
					} catch(Exception e){
						LOGGER.error("Error importing network from NDEx", e);
						_dialogUtil.showMessageDialog(swingApplication.getJFrame(), "Error importing network from NDEx: " + e.getMessage());
					}
				}
			}
        }
		return new TaskIterator(1, new CanceledTask());
	}
}
