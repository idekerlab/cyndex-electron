/*
 * Copyright (c) 2014, the Cytoscape Consortium and the Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.cytoscape.cyndex2.internal.ui.swing;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import org.cytoscape.cyndex2.internal.rest.parameter.LoadParameters;
import org.cytoscape.cyndex2.internal.util.ErrorMessage;
import org.cytoscape.cyndex2.internal.util.Server;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.model.object.network.VisibilityType;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

/**
 *
 * @author David
 * @author David Otasek
 */
public class FindNetworksDialog extends javax.swing.JDialog implements PropertyChangeListener {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private List<NetworkSummary> networkSummaries;

	/**
	 * Creates new form SimpleSearch
	 */
	public FindNetworksDialog(Frame parent, LoadParameters loadParameters) {
		super(parent, false);
		ServerManager.INSTANCE.addPropertyChangeListener(this);
		initComponents();
		prepComponents(loadParameters.searchTerm);
	}

	public void setFocusOnDone() {
		this.getRootPane().setDefaultButton(done);
		done.requestFocus();
	}

	private void prepComponents(String searchTerm) {
		this.getRootPane().setDefaultButton(search);

		searchField.setText(searchTerm);
		Server selectedServer = ServerManager.INSTANCE.getServer();

		if (selectedServer.getUsername() != null && !selectedServer.getUsername().isEmpty()) {
			administeredByMe.setVisible(true);
		} else {
			if (selectedServer.getUsername() != null) {
				NdexRestClientModelAccessLayer mal = selectedServer.getModelAccessLayer();
				try {
					selectedServer.check(mal);
					administeredByMe.setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication + ": " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
					this.setVisible(false);
					return;
				}
			} else {
				administeredByMe.setVisible(false);
			}
		}

		NdexRestClientModelAccessLayer mal = selectedServer.getModelAccessLayer();
		try {
			if (selectedServer.check(mal)) {
				try {
					networkSummaries = mal.findNetworks(searchTerm, null, null, true, 0, 400).getNetworks();
				} catch (IOException | NdexException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "Error",
							JOptionPane.ERROR_MESSAGE);
					this.setVisible(false);
					return;
				}
				showSearchResults();
			} else {
				JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "Error", JOptionPane.ERROR_MESSAGE);
				this.setVisible(false);
			}
		} catch (HeadlessException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "Error", JOptionPane.ERROR_MESSAGE);

		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc="Generated
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jScrollPane1 = new javax.swing.JScrollPane();
		resultsTable = new javax.swing.JTable();
		done = new javax.swing.JButton();
		search = new javax.swing.JButton();
		searchField = new javax.swing.JTextField();
		administeredByMe = new javax.swing.JCheckBox();
		jSeparator1 = new javax.swing.JSeparator();
		jLabel1 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jButton1 = SignInButtonHelper.createSignInButton();
		ndexLogo = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Find Networks");

		resultsTable.setModel(new NetworkSummaryTableModel(List.of()));
		resultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		jScrollPane1.setViewportView(resultsTable);

		done.setText("Done Loading Networks");
		done.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				doneActionPerformed(evt);
			}
		});

		search.setText("Search");
		search.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				searchActionPerformed(evt);
			}
		});

		administeredByMe.setText("My Networks");
		administeredByMe.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				administeredByMeActionPerformed(evt);
			}
		});

		jLabel1.setText("Results");

		jLabel4.setText(
				"WARNING: In some cases, not all network information stored in NDEx will be available within Cytoscape after loading.");

		jButton1.setText(SignInButtonHelper.getSignInText());

		ndexLogo.setText(null);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addContainerGap()
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(jScrollPane1).addComponent(jSeparator1)
										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
												layout.createSequentialGroup().addComponent(searchField)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(search))
										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
												layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(done,
														javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
												layout.createSequentialGroup().addComponent(ndexLogo)
														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(jButton1))
										.addGroup(layout.createSequentialGroup().addComponent(jLabel1).addGap(0, 0, Short.MAX_VALUE))))
						.addGroup(layout.createSequentialGroup().addGap(377, 377, 377).addComponent(administeredByMe).addGap(0, 0,
								Short.MAX_VALUE)))
				.addContainerGap())
				.addGroup(layout.createSequentialGroup().addGap(98, 98, 98).addComponent(jLabel4).addContainerGap(121,
						Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				javax.swing.GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jButton1)
								.addComponent(ndexLogo))
						.addGap(18, 18, 18)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(searchField, javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(search, javax.swing.GroupLayout.Alignment.TRAILING))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(administeredByMe)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel1)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 299,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel4)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(done).addContainerGap()));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void selectNetworkActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_selectNetworkActionPerformed
	{// GEN-HEADEREND:event_selectNetworkActionPerformed
		int selectedIndex = resultsTable.getSelectedRow();
		if (selectedIndex == -1) {
			JOptionPane.showMessageDialog(this, ErrorMessage.noNetworkSelected, "Error", JOptionPane.ERROR_MESSAGE);
		}
		NetworkSummary ns = displayedNetworkSummaries.get(selectedIndex);
		// NetworkManager.INSTANCE.setSelectedNetworkSummary(ns);

		// load(ns);
	}// GEN-LAST:event_selectNetworkActionPerformed

	private void getMyNetworks() {
		Server selectedServer = ServerManager.INSTANCE.getSelectedServer();

		NdexRestClientModelAccessLayer mal = selectedServer.getModelAccessLayer();
		try {
			if (selectedServer.check(mal)) {

				try {
					networkSummaries = mal.getMyNetworks();
					showSearchResults();
				} catch (IOException | NdexException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

			} else {
				JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "ErrorY",
						JOptionPane.ERROR_MESSAGE);
				this.setVisible(false);
			}
		} catch (HeadlessException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "ErrorY", JOptionPane.ERROR_MESSAGE);

		}
	}

	private void doneActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);
	}

	private void search() {
		Server selectedServer = ServerManager.INSTANCE.getServer();

		/*
		 * if( administeredByMe.isSelected() ) permissions = Permissions.READ;
		 */

		String searchText = searchField.getText();
		if (searchText.isEmpty())
			searchText = "";

		NdexRestClientModelAccessLayer mal = selectedServer.getModelAccessLayer();
		try {
			if (selectedServer.check(mal)) {
				try {
					if (administeredByMe.isSelected()) {
						networkSummaries = mal.getMyNetworks();
					} else
						networkSummaries = mal.findNetworks(searchText, null, null, true, 0, 10000).getNetworks();
				} catch (IOException | NdexException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				showSearchResults();
			} else {
				JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "ErrorY",
						JOptionPane.ERROR_MESSAGE);
				this.setVisible(false);
			}
		} catch (HeadlessException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, ErrorMessage.failedServerCommunication, "ErrorY", JOptionPane.ERROR_MESSAGE);

		}
	}

	private void searchActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_searchActionPerformed
	{// GEN-HEADEREND:event_searchActionPerformed
		search();
	}

	private void administeredByMeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_administeredByMeActionPerformed
	{// GEN-HEADEREND:event_administeredByMeActionPerformed
		if (administeredByMe.isSelected()) {
			getMyNetworks();
		} else {
			search();
		}
	}// GEN-LAST:event_administeredByMeActionPerformed

	private List<NetworkSummary> displayedNetworkSummaries = new ArrayList<>();

	private void showSearchResults() {
		TableModel model = new NetworkSummaryTableModel(networkSummaries);
		displayedNetworkSummaries.clear();
		for (NetworkSummary networkSummary : networkSummaries) {
			displayedNetworkSummaries.add(networkSummary);
		}
		resultsTable.setModel(model);
		resultsTable.setDefaultRenderer(NetworkSummary.class, new NetworkSummaryTableModel.ImportButtonRenderer());
		resultsTable.setDefaultRenderer(VisibilityType.class, new NetworkSummaryTableModel.VisibilityTypeRenderer());
		resultsTable.setDefaultRenderer(Timestamp.class, new NetworkSummaryTableModel.TimestampRenderer());
		resultsTable.setDefaultEditor(NetworkSummary.class,
				new NetworkSummaryTableModel.ImportButtonEditor(new JCheckBox()));
		resultsTable.getSelectionModel().setSelectionInterval(0, 0);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
		// (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the default
		 * look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(FindNetworksDialog.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(FindNetworksDialog.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(FindNetworksDialog.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(FindNetworksDialog.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		// </editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				FindNetworksDialog dialog = new FindNetworksDialog(new javax.swing.JFrame(), null);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JCheckBox administeredByMe;
	private javax.swing.JButton done;
	private javax.swing.JButton jButton1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JLabel ndexLogo;
	private javax.swing.JTable resultsTable;
	private javax.swing.JButton search;
	private javax.swing.JTextField searchField;
	// End of variables declaration//GEN-END:variables

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {

		jButton1.setText(SignInButtonHelper.getSignInText());
		Server selectedServer = ServerManager.INSTANCE.getServer();
		if (administeredByMe.isSelected()) {
			administeredByMe.setSelected(selectedServer.getUsername() != null && !selectedServer.getUsername().isEmpty());
		}
		administeredByMe.setVisible(selectedServer.getUsername() != null && !selectedServer.getUsername().isEmpty());
		search();
	}
}
