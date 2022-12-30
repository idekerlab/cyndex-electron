package org.cytoscape.cyndex2.internal.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.cytoscape.cyndex2.internal.util.ErrorMessage;
import org.cytoscape.cyndex2.internal.util.Server;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NetworkSearchResult;

/**
 *
 * @author churas
 */
public class AbstractOpenSaveDialog extends JPanel implements PropertyChangeListener {
	
	protected Dimension _dialogDimension = new Dimension(800, 400);
	protected Dimension _leftPanelDimension = new Dimension(150, _dialogDimension.height);
	protected Dimension _leftButtonsDimensions = new Dimension(_leftPanelDimension.width-5,_leftPanelDimension.width-5);
	protected Dimension _rightPanelDimension = new Dimension(600, _dialogDimension.height);
	protected Dimension _ndexTopPanelDimension = new Dimension(_rightPanelDimension.width, 35);
	protected Dimension _ndexPanelDimension = new Dimension(_rightPanelDimension.width, 325);
	
	protected Color _NDExButtonBlue = new Color(0,255, 255);
	protected Color _SessionButtonOrange = new Color(255,213,128);
	
	protected JButton _ndexSignInButton;
	protected MyNetworksTableModel _myNetworksTableModel;
	protected MyNetworksWithOwnerTableModel _searchTableModel;
	protected int _networkTableLimit;
	
	
	
	public AbstractOpenSaveDialog(int networkTableLimit){
		_networkTableLimit = networkTableLimit;
	}
	
	public AbstractOpenSaveDialog(){
		this(400);
	} 

	
	/**
	 * Creates the horizontal panel at the top containing the NDEx sign in button
	 * with this rough structure:
	 * 
	 * ------------------------------------
	 * | NDEx             (sign in button)|
	 * ------------------------------------
	 * 
	 * @return 
	 */
	protected JPanel getNDExSignInPanel(){
		JPanel topPanel = new JPanel(new GridBagLayout());
		//topPanel.setBorder(BorderFactory.createTitledBorder("NDEx credentials (temporary authentication user interface)"));
		topPanel.setPreferredSize(new Dimension(_ndexPanelDimension.width, 50));

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.gridx = 0;
		c.gridy = 0;
		// @TODO add NDEx logo to this
		JLabel ndexLabel = new JLabel("NDEx");
		topPanel.add(ndexLabel, c);

		// could just use a filler but this works
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 1;
		c.gridy = 0;
		c.ipadx = 200;
		c.weightx = 0.5;
		topPanel.add(new JLabel(""), c);
		
		_ndexSignInButton = SignInButtonHelper.createSignInButton(null);
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridwidth = 2;
		c.gridy = 0;
		c.weightx = 1.0;
		topPanel.add(_ndexSignInButton, c);
		
		return topPanel;
	}
	
	/**
	 * Gets the dialog where the save and cancel buttons reside so 
	 * we can link the buttons to the dialog actions
	 * @param parent
	 * @return 
	 */
	protected JOptionPane getOptionPane(JComponent parent) {
        JOptionPane pane = null;
        if (!(parent instanceof JOptionPane)) {
            pane = getOptionPane((JComponent)parent.getParent());
        } else {
            pane = (JOptionPane) parent;
        }
        return pane;
    }
	
	protected void setButtonFocus(boolean focus, JButton button){
		if (focus == true){
			button.setText(button.getText().replaceAll("808080", "000000"));
		}
		else {
			button.setText(button.getText().replaceAll("000000", "808080"));
		}
		button.invalidate();
	}
	
	protected void updateMyNetworksTable(){
		if (_myNetworksTableModel == null){
			return;
		}
		Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
		_myNetworksTableModel.clearNetworkSummaries();
		if (selectedServer != null && selectedServer.getUsername() != null){
			try {
				_myNetworksTableModel.replaceNetworkSummaries(selectedServer.getModelAccessLayer().getMyNetworks(0, _networkTableLimit));
			} catch (IOException | NdexException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						ErrorMessage.failedServerCommunication + "\n\nError Message: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				}
		}
	}
	
	protected void updateSearchTable(final String searchString){
		if (_searchTableModel == null){
			return;
		}
		Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
		_searchTableModel.clearNetworkSummaries();
		if (selectedServer != null && selectedServer.getUsername() != null){
			try {
				NetworkSearchResult nrs = selectedServer.getModelAccessLayer().findNetworks(searchString, null, 0, _networkTableLimit);
				if (nrs.getNetworks() != null){
					_searchTableModel.replaceNetworkSummaries(nrs.getNetworks());
				}
			} catch (IOException | NdexException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						ErrorMessage.failedServerCommunication + "\n\nError Message: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				}
		}
	}
	
	/**
	 * This should be registered with ServerManager and will be notified
	 * when the user updates sign in credentials so we know to do a search
	 * or change the sign in text box
	 * @param arg0 
	 */
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (isVisible()) {
			ModalProgressHelper.runWorker(null, "Loading Profile", () -> {
				_ndexSignInButton.setText(SignInButtonHelper.getSignInText());
				_myNetworksTableModel.clearNetworkSummaries();
				updateMyNetworksTable();
				return 1;
			});
		}
	}
	
}
