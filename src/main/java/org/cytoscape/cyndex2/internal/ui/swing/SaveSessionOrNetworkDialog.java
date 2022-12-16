package org.cytoscape.cyndex2.internal.ui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import org.cytoscape.cyndex2.internal.util.ErrorMessage;
import org.cytoscape.cyndex2.internal.util.Server;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import org.ndexbio.model.exceptions.NdexException;

import org.ndexbio.model.object.network.NetworkSummary;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Save Session Or Network Dialog (SaveSessionOrNetworkDialog)
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
@SuppressWarnings("serial")
public class SaveSessionOrNetworkDialog extends JPanel implements PropertyChangeListener {
	//private final static Logger LOGGER = LoggerFactory.getLogger(SaveDialog.class);
	public final static String SAVE_SESSION = "SaveSession";
	public final static String SAVE_NDEX = "SaveNDEx";
	private boolean _guiLoaded;
	private JPanel _cards;
	private JButton _saveSessionButton;
	private JButton _saveNDExButton;
	private JButton _mainSaveButton;
	private JButton _mainCancelButton;
	private JFileChooser _sessionChooser;
	private JTextField _saveAsTextField;
	private JPanel _ndexPanel;
	private JTabbedPane _ndexTabbedPane;
	private Dimension _dialogDimension = new Dimension(800, 400);
	private Dimension _leftPanelDimension = new Dimension(150, _dialogDimension.height);
	private Dimension _leftButtonsDimensions = new Dimension(_leftPanelDimension.width-5,_leftPanelDimension.width-5);
	private Dimension _rightPanelDimension = new Dimension(600, _dialogDimension.height-50);
	private Dimension _ndexTopPanelDimension = new Dimension(_rightPanelDimension.width, 35);
	private Dimension _ndexPanelDimension = new Dimension(_rightPanelDimension.width, 100);
	private Color _NDExButtonBlue = new Color(0,255, 255);
	private Color _SessionButtonOrange = new Color(255,213,128);
	private Color _defaultButtonColor;
	private JButton _ndexSignInButton;
	JTextField _ndexSaveAsTextField;
	private String _selectedCard;
	private String _initialNetworkName;
	
	private int _selectedNDExNetworkIndex = -1;
	private int _selectedNDExSearchNetworkIndex = -1;
	private MyNetworksTableModel _myNetworksTableModel;
	private TableRowSorter _myNetworksTableSorter;
	private ShowDialogUtil _dialogUtil;
	private NetworkSummary _ndexNetworkToOverwrite;
	
	public SaveSessionOrNetworkDialog(ShowDialogUtil dialogUtil){
		_guiLoaded = false;
		_dialogUtil = dialogUtil;
		_ndexNetworkToOverwrite = null;
	}
	
	public void setInitialNetworkName(final String name){
		if (name == null){
			_initialNetworkName = "";
			return;
		} 
		_initialNetworkName = name;
	}
	/**
	 * Gets open button for main dialog so caller can add it to
	 * the JOptionPane dialog
	 * @return 
	 */
	public JButton getMainSaveButton(){
		return _mainSaveButton;
	}
	
	/**
	 * Gets cancel button for main dialog so caller can add it to
	 * the JOptionPane dialog
	 * @return 
	 */
	public JButton getMainCancelButton(){
		return _mainCancelButton;
	}

	/**
	 * Initializes gui once, subsequent calls do nothing
	 * @return 
	 */
	public boolean createGUI(){
		if (_guiLoaded == false){
			_mainSaveButton = new JButton("Save");
			_mainCancelButton = new JButton("Cancel");
			this.add(getSavePanel());
			this.invalidate();
			
			_mainSaveButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane pane = getOptionPane((JComponent)e.getSource());
						if (_selectedCard == SaveSessionOrNetworkDialog.SAVE_NDEX){
							List<NetworkSummary> matchingNetworks = SaveSessionOrNetworkDialog.this._myNetworksTableModel.getNetworksMatchingName(_ndexSaveAsTextField.getText());

							if (matchingNetworks != null && matchingNetworks.size() > 0){
								if (matchingNetworks.size() == 1 || SaveSessionOrNetworkDialog.this.getNDExSelectedNetwork() != null){

									NetworkSummary selectedNetwork = null;
									if (SaveSessionOrNetworkDialog.this.getNDExSelectedNetwork() != null){
										selectedNetwork = SaveSessionOrNetworkDialog.this.getNDExSelectedNetwork();
									} else {
										selectedNetwork = matchingNetworks.get(0);
									}
									String netName = selectedNetwork.getName() == null ? "" : selectedNetwork.getName(); 
									//user selected a network so lets verify they want to overwrite
									Object[] options = {"Yes", "No"};
									int res = _dialogUtil.showOptionDialog(pane, "Do you wish to overwrite " + 
													netName + "?","Overwrite:",
											JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
											options, options[1]);
									if (res == 1){
										System.out.println("User did not want to overwrite. just return");
										_ndexNetworkToOverwrite = null;
										return;
									}
									if (res == 0){
										System.out.println("User does want to overwrite");
										_ndexNetworkToOverwrite = selectedNetwork;
										// user does want to overwrite so click save button
										pane.setValue(_mainSaveButton);
										return;
									}
								}
								if (SaveSessionOrNetworkDialog.this.getNDExSelectedNetwork() == null){
									_dialogUtil.showMessageDialog(SaveSessionOrNetworkDialog.this, Integer.toString(matchingNetworks.size()) 
											+ " networks match that name.\nPlease click ok and select one to overwrite or choose a different name");
								}
								return;
							}
						}
						
                        pane.setValue(_mainSaveButton);
                    }
                });
			_mainSaveButton.setEnabled(false);
			
			_mainCancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane pane = getOptionPane((JComponent)e.getSource());
                        pane.setValue(_mainCancelButton);
                    }
                });
			// TODO: Need to remember previous behavior via preferences
			_saveSessionButton.setEnabled(false);
			_saveNDExButton.doClick();
			// listen for changes to NDEx credentials
			ServerManager.INSTANCE.addPropertyChangeListener(this);
			_guiLoaded = true;
		}
		_ndexNetworkToOverwrite = null;
		return true;
	}
	
	/**
	 * The NDEx network the user wishes to overwrite
	 * @return Network to overwrite or null
	 */
	public NetworkSummary getNDExNetworkUserWantsToOverwrite(){
		return _ndexNetworkToOverwrite;
	}
	
	/**
	 * Returns name of selected card on right side of dialog
	 * @return 
	 */
	public String getSelectedCard(){
		return _selectedCard;
	}
	
	/**
	 * If the selected card is open session return path to selected file or 
	 * null if no file is selected
	 * @return 
	 */
	public File getSelectedSessionFile(){
		if (getSelectedCard().equals(SaveSessionOrNetworkDialog.SAVE_SESSION)){
			System.out.println("Current directory: " + _sessionChooser.getCurrentDirectory().getAbsolutePath());
			System.out.println("Current selected file: " + _sessionChooser.getSelectedFile().getAbsolutePath());
			System.out.println("Current Save As Text Field: " + _saveAsTextField.getText());
			if (_sessionChooser.getSelectedFile() != null){
				// handle case where user types into save as dialog a different path
				// just go with that.
				if (!_sessionChooser.getSelectedFile().getName().equals(_saveAsTextField.getText())){
					if (_saveAsTextField.getText().startsWith("/")){
						return new File(_saveAsTextField.getText());
					}
					return new File(_sessionChooser.getCurrentDirectory() + File.separator + _saveAsTextField.getText());
				}
				return _sessionChooser.getSelectedFile();
			}
			return null;
		}
		return null;
	}
	
	public NetworkSummary getNDExSelectedNetwork(){
		if (getSelectedCard() == null){
			return null;
		}
		
		if (getSelectedCard().equals(SaveSessionOrNetworkDialog.SAVE_NDEX)){
			if (_selectedNDExNetworkIndex != -1 && _selectedNDExNetworkIndex < _myNetworksTableModel.getRowCount()){
				return _myNetworksTableModel.getNetworkSummaries().get(_selectedNDExNetworkIndex);
			}
		}
		return null;
	}
	
	public String getDesiredNetworkName(){
		if (_ndexSaveAsTextField == null){
			return null;
		}
		return _ndexSaveAsTextField.getText();
	}
	
	public void setDesiredNetworkName(final String desiredName){
		if (_ndexSaveAsTextField != null){
			_ndexSaveAsTextField.setText(desiredName);
			
			//should probably also refresh the network list at this time
			updateMyNetworksTable();
			
		}
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
	
	
	private void setButtonFocus(boolean focus, JButton button){
		if (focus == true){
			button.setText(button.getText().replace("808080", "000000"));
		}
		else {
			button.setText(button.getText().replace("000000", "808080"));
		}
		button.invalidate();
	}
	
	private JPanel getSavePanel(){
		JPanel openDialogPanel = new JPanel();
		openDialogPanel.setPreferredSize(_dialogDimension);
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(_leftPanelDimension);
        _saveNDExButton = new JButton("<html><font color=\"#000000\">Save Network<br/><br/><font size=\"-2\">Save the currently selected network to NDEx</font></font></html>");
		_saveNDExButton.setOpaque(true);
        _saveNDExButton.setPreferredSize(_leftButtonsDimensions);
		_defaultButtonColor = _saveNDExButton.getBackground();
		_saveNDExButton.addActionListener(new ActionListener() {
			/**
			 * When a user clicks on the open ndex button need to change
			 * the background for the open ndex button and for open session 
			 * button. Also need to determine if the open button should be
			 * enabled or not
			 */
			@Override
			public void actionPerformed(ActionEvent e){
				CardLayout cl = (CardLayout)_cards.getLayout();
				cl.show(_cards, SaveSessionOrNetworkDialog.SAVE_NDEX);
				_saveNDExButton.setBackground(_NDExButtonBlue);
				_saveSessionButton.setBackground(_defaultButtonColor);
				setButtonFocus(true, _saveNDExButton);
				setButtonFocus(false, _saveSessionButton);
				_selectedCard = SaveSessionOrNetworkDialog.SAVE_NDEX;
				_mainSaveButton.setEnabled(false);
			}
		});
		
        leftPanel.add(_saveNDExButton, BorderLayout.PAGE_START);

        _saveSessionButton = new JButton("<html><font color=\"#000000\">Save Session<br/><br/><font size=\"-2\">Save a session (.cys) file on this computer</font></html>");
		_saveSessionButton.setOpaque(true);
        _saveSessionButton.setPreferredSize(_leftButtonsDimensions);

        _saveSessionButton.addActionListener(new ActionListener() {
                /**
                 * When a user clicks on the save session button need to change
                 * the background for the save ndex button and for save session 
                 * button. Also need to determine if the open button should be
                 * enabled or not
                 */
                @Override
                public void actionPerformed(ActionEvent e){
                        CardLayout cl = (CardLayout)_cards.getLayout();
                        cl.show(_cards, SaveSessionOrNetworkDialog.SAVE_SESSION);
                        _saveNDExButton.setBackground(_defaultButtonColor);
                        _saveSessionButton.setBackground(_SessionButtonOrange);
                        setButtonFocus(false, _saveNDExButton);
                        setButtonFocus(true, _saveSessionButton);

                        _selectedCard = SaveSessionOrNetworkDialog.SAVE_SESSION;
                        if (_saveAsTextField == null){
                                _mainSaveButton.setEnabled(true);
                        } else {
                                _mainSaveButton.setEnabled(_saveAsTextField.getText().length() > 0);
                        }
                }
        });

	leftPanel.add(_saveSessionButton, BorderLayout.PAGE_END);
        openDialogPanel.add(leftPanel, BorderLayout.LINE_START);

        JPanel rightPanel = getRightCardPanel();
        openDialogPanel.add(rightPanel, BorderLayout.LINE_END);
            return openDialogPanel;
	}
	
	/**
	 * Finds the JTextField in the JChooser save dialog by recursively
	 * searching through the first JPanel found in the JChooser dialog
	 * @return 
	 */
	private JTextField getJChooserSaveAsTextField(){
		for (Component c : _sessionChooser.getComponents()){
			if (c instanceof JPanel){
				return searchForJTextFieldInJPanel((JPanel)c);
			}
		}
		return null;
	}
	
	/**
	 * Recursively finds the the first JTextField encountered
	 * @param panel
	 * @return 
	 */
	private JTextField searchForJTextFieldInJPanel(JPanel panel){
		for (Component subc : panel.getComponents()){
			if (subc instanceof JTextField){
				return (JTextField)subc;
			}
			if (subc instanceof JPanel){
				return searchForJTextFieldInJPanel((JPanel)subc);
			}
		}
		return null;
	}
	
	private void createJFileChooser(){
		_sessionChooser = new JFileChooser(".");
		_sessionChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		_sessionChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		_sessionChooser.setControlButtonsAreShown(false);

		// listen for any text changes in the save as text field so we know
		// to enable/disable the save button for the dialog
		_saveAsTextField = getJChooserSaveAsTextField();
		if (_saveAsTextField != null){
			_saveAsTextField.getDocument().addDocumentListener(new DocumentListener(){
				@Override
				public void insertUpdate(DocumentEvent e){
					_mainSaveButton.setEnabled(_saveAsTextField.getText().length() > 0);
				}
				@Override
				public void removeUpdate(DocumentEvent e){
					_mainSaveButton.setEnabled(_saveAsTextField.getText().length() > 0);
					
				}
				@Override
				public void changedUpdate(DocumentEvent e){
					_mainSaveButton.setEnabled(_saveAsTextField.getText().length() > 0);
				}
			});
		}

		_sessionChooser.addActionListener(new ActionListener(){
			
			/**
			 * Look for user hitting enter on the save as text field
			 * @param evt 
			 */
			@Override
			public void actionPerformed(ActionEvent evt){
				if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())){
					System.out.println(evt.getActionCommand() + " " + evt.getSource());
					if (getSelectedSessionFile() != null){
						_mainSaveButton.doClick();
					} else {
						_mainSaveButton.setEnabled(false);
					}
				}
			}
		});

	}
	
	private JPanel getRightCardPanel(){
		_cards = new JPanel(new CardLayout());
        _cards.setPreferredSize(this._rightPanelDimension);
		
		createJFileChooser();		
		CardLayout cl = (CardLayout)_cards.getLayout();
		_cards.add(_sessionChooser, SaveSessionOrNetworkDialog.SAVE_SESSION);
		cl.addLayoutComponent(_sessionChooser, SaveSessionOrNetworkDialog.SAVE_SESSION);
		_selectedCard = SaveSessionOrNetworkDialog.SAVE_SESSION;
		
		createNDExPanel();
		_cards.add(_ndexPanel, SaveSessionOrNetworkDialog.SAVE_NDEX);
		cl.addLayoutComponent(_ndexPanel, SaveSessionOrNetworkDialog.SAVE_NDEX);
		
		return _cards;
	}
	
	private JPanel getNDExSignInPanel(){
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
	
	private JTable getMyNetworksJTable(){
		_myNetworksTableModel = new MyNetworksTableModel(new ArrayList<>());

		JTable myNetworksTable = new JTable(_myNetworksTableModel);
		
		_myNetworksTableSorter = new TableRowSorter<MyNetworksTableModel>(_myNetworksTableModel);
		//myNetworksTable.setAutoCreateRowSorter(true);
		myNetworksTable.setRowSorter(_myNetworksTableSorter);
		myNetworksTable.setPreferredScrollableViewportSize(new Dimension(400, 150));
        myNetworksTable.setFillsViewportHeight(true);
		myNetworksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		myNetworksTable.setDefaultRenderer(Timestamp.class, new NDExTimestampRenderer());
		myNetworksTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
				// do some actions here, for example
				// print first column value from selected row
				
				if (myNetworksTable.getSelectedRow() == -1){
					System.out.println("Nothing selected");
					//_mainSaveButton.setEnabled(false);
					_selectedNDExNetworkIndex = -1;
					//_ndexSaveAsTextField.setText("");
				} else {
					System.out.println(event.toString() + " " + myNetworksTable.getValueAt(myNetworksTable.getSelectedRow(), 0).toString());
					_selectedNDExNetworkIndex = myNetworksTable.convertRowIndexToModel(myNetworksTable.getSelectedRow());
					//_ndexSaveAsTextField.setText(_myNetSummaryTableModel.getNetworkSummaries().get(_selectedNDExNetworkIndex).getName());
					//_mainSaveButton.setEnabled(true);
				}
			}
        });
		myNetworksTable.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() == 2) {     // to detect double click events
               JTable target = (JTable)me.getSource();
               int row = target.getSelectedRow(); // select a row
			   if (row != -1){
	               System.out.println("Double click: " + myNetworksTable.getValueAt(myNetworksTable.getSelectedRow(), 0).toString());
				   _selectedNDExNetworkIndex = myNetworksTable.convertRowIndexToModel(myNetworksTable.getSelectedRow());
				   _ndexSaveAsTextField.setText(_myNetworksTableModel.getNetworkSummaries().get(_selectedNDExNetworkIndex).getName());
				   //_mainSaveButton.doClick();
			   }
			   
            }
         }
		});
		
		//populate the table
		updateMyNetworksTable();
		return myNetworksTable;
	}
	
	private void updateMyNetworksTable(){
		if (_myNetworksTableModel == null){
			return;
		}
		Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
		if (selectedServer.getUsername() != null){
			try {
				_myNetworksTableModel.replaceNetworkSummaries(selectedServer.getModelAccessLayer().getMyNetworks());
			} catch (IOException | NdexException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						ErrorMessage.failedServerCommunication + "\n\nError Message: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				}
		}
	}
	
	private RowFilter<MyNetworksTableModel, Object> getStringMatchRowFilter(final String saveAsText){
		return new RowFilter<MyNetworksTableModel, Object>(){
			@Override
			public boolean include(Entry<? extends MyNetworksTableModel, ? extends Object> entry){
				int rowID = (Integer)entry.getIdentifier();
				MyNetworksTableModel model = entry.getModel();
				String networkName = (String)model.getValueAt(rowID, 0);
				if (networkName == null && (saveAsText == null || saveAsText.trim().isEmpty())){
					return true;
				}
				if (networkName != null && networkName.contains(saveAsText)){
					return true;
				}
				return false;
			}
		};
	}
	
	/**
	 * Filters networks table with regex set to value of ndex save as text field
	 * This has problems if one has regex characters in filename cause things
	 * will not match. There is also a concurrent modification issue
	 */
	private void newMyNetworksTableSorterFilter(){
		RowFilter<MyNetworksTableModel, Object> rf = null;
		// if current expression fails do not update
		try {
			rf = getStringMatchRowFilter(_ndexSaveAsTextField.getText());
		} catch(java.util.regex.PatternSyntaxException e){
			return;
		}
		_myNetworksTableSorter.setRowFilter(rf);
	}
	
	private void createNDExPanel(){
		_ndexPanel = new JPanel();
		_ndexPanel.setPreferredSize(_ndexPanelDimension);
		
		// add NDEx sign in panel to top of dialog
		_ndexPanel.add(getNDExSignInPanel(), BorderLayout.PAGE_START);
	
		JPanel saveAsPanel = new JPanel();
		JLabel saveAsLabel = new JLabel("<html><font color=\"#000000\">Save As:</font></html>");
		saveAsPanel.add(saveAsLabel, BorderLayout.LINE_START);
		_ndexSaveAsTextField = new JTextField(_initialNetworkName);
		_ndexSaveAsTextField.setPreferredSize(new Dimension(300, 25));
		_ndexSaveAsTextField.setToolTipText("Name to save network as to NDEx.\nNOTE: Changing the name here and savingwill change the name of the network in Cytoscape");
		_ndexSaveAsTextField.getDocument().addDocumentListener(new DocumentListener(){
				@Override
				public void insertUpdate(DocumentEvent e){
					// @TODO need to figure out how to filter
					// the results by value in save as text field without
					// causing concurrent modification exceptions
					newMyNetworksTableSorterFilter();
					_mainSaveButton.setEnabled(_ndexSaveAsTextField.getText().length() > 0);
				}
				@Override
				public void removeUpdate(DocumentEvent e){
					newMyNetworksTableSorterFilter();
					_mainSaveButton.setEnabled(_ndexSaveAsTextField.getText().length() > 0);
					
				}
				@Override
				public void changedUpdate(DocumentEvent e){
					newMyNetworksTableSorterFilter();
					_mainSaveButton.setEnabled(_ndexSaveAsTextField.getText().length() > 0);
				}
			});
		saveAsPanel.add(_ndexSaveAsTextField, BorderLayout.LINE_END);
		_ndexPanel.add(saveAsPanel, BorderLayout.PAGE_START);
		JScrollPane scrollPane = new JScrollPane(getMyNetworksJTable());
		scrollPane.setPreferredSize(new Dimension(570,150));
		_ndexPanel.add(scrollPane, BorderLayout.PAGE_START);
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
				System.out.println("Change to sign in credentials: " + arg0.toString());
				Server selectedServer = ServerManager.INSTANCE.getServer();

				_myNetworksTableModel.clearNetworkSummaries();

				updateMyNetworksTable();
				return 1;
			});
		}
	}
}
