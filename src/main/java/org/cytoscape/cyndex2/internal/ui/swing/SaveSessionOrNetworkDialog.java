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
import java.util.ArrayList;
import javax.swing.BorderFactory;
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
	private JButton _openSessionButton;
	private JButton _openNDExButton;
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
	private JLabel _locationLabel;
	private JButton _ndexSignInButton;
	JTextField _ndexSaveAsTextField;
	private String _selectedCard;
	private String _initialNetworkName;
	
	private int _selectedNDExNetworkIndex = -1;
	private int _selectedNDExSearchNetworkIndex = -1;
	private NetworkSummaryTableModel _myNetSummaryTableModel;
	private TableRowSorter _myNetworksTableSorter;
	
	public SaveSessionOrNetworkDialog(){
		_guiLoaded = false;
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
			_openSessionButton.doClick();
			// listen for changes to NDEx credentials
			ServerManager.INSTANCE.addPropertyChangeListener(this);
			_guiLoaded = true;
		}
		return true;
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
			
			if (_sessionChooser.getSelectedFile() != null){
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
			if (_selectedNDExNetworkIndex != -1 && _selectedNDExNetworkIndex < _myNetSummaryTableModel.getRowCount()){
				return _myNetSummaryTableModel.getNetworkSummaries().get(_selectedNDExNetworkIndex);
			}
		}
		return null;
	}
	
	public String getDesiredNetworkName(){
		return _ndexSaveAsTextField.getText();
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
        _openNDExButton = new JButton("<html><font color=\"#000000\">Save Network<br/><br/><font size=\"-2\">Save the currently selected network to NDEx</font></font></html>");
		_openNDExButton.setOpaque(true);
        _openNDExButton.setPreferredSize(_leftButtonsDimensions);
		_defaultButtonColor = _openNDExButton.getBackground();
		_openNDExButton.addActionListener(new ActionListener() {
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
				_openNDExButton.setBackground(_NDExButtonBlue);
				_openSessionButton.setBackground(_defaultButtonColor);
				setButtonFocus(true, _openNDExButton);
				setButtonFocus(false, _openSessionButton);
				_selectedCard = SaveSessionOrNetworkDialog.SAVE_NDEX;
				_mainSaveButton.setEnabled(false);
			}
		});
		
        leftPanel.add(_openNDExButton, BorderLayout.PAGE_START);

        _openSessionButton = new JButton("<html><font color=\"#000000\">Save Session<br/><br/><font size=\"-2\">Save a session (.cys) file on this computer</font></html>");
		_openSessionButton.setOpaque(true);
        _openSessionButton.setPreferredSize(_leftButtonsDimensions);

        _openSessionButton.addActionListener(new ActionListener() {
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
                        _openNDExButton.setBackground(_defaultButtonColor);
                        _openSessionButton.setBackground(_SessionButtonOrange);
                        setButtonFocus(false, _openNDExButton);
                        setButtonFocus(true, _openSessionButton);

                        _selectedCard = SaveSessionOrNetworkDialog.SAVE_SESSION;
                        if (_saveAsTextField == null){
                                _mainSaveButton.setEnabled(true);
                        } else {
                                _mainSaveButton.setEnabled(_saveAsTextField.getText().length() > 0);
                        }
                }
        });

	leftPanel.add(_openSessionButton, BorderLayout.PAGE_END);
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
		topPanel.setBorder(BorderFactory.createTitledBorder("NDEx credentials (temporary authentication user interface)"));
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
		_myNetSummaryTableModel = new NetworkSummaryTableModel(new ArrayList<>(), null, true);

		JTable myNetworksTable = new JTable(_myNetSummaryTableModel);
		_myNetworksTableSorter = new TableRowSorter<NetworkSummaryTableModel>(_myNetSummaryTableModel);
		//myNetworksTable.setAutoCreateRowSorter(true);
		myNetworksTable.setRowSorter(_myNetworksTableSorter);
		myNetworksTable.setPreferredScrollableViewportSize(new Dimension(400, 150));
        myNetworksTable.setFillsViewportHeight(true);
		myNetworksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		myNetworksTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
				// do some actions here, for example
				// print first column value from selected row
				
				if (myNetworksTable.getSelectedRow() == -1){
					System.out.println("Nothing selected");
					//_mainSaveButton.setEnabled(false);
					_selectedNDExNetworkIndex = -1;
					_ndexSaveAsTextField.setText("");
				} else {
					System.out.println(event.toString() + " " + myNetworksTable.getValueAt(myNetworksTable.getSelectedRow(), 0).toString());
					_selectedNDExNetworkIndex = myNetworksTable.convertRowIndexToModel(myNetworksTable.getSelectedRow());
					_ndexSaveAsTextField.setText(_myNetSummaryTableModel.getNetworkSummaries().get(_selectedNDExNetworkIndex).getName());
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
				   _ndexSaveAsTextField.setText(_myNetSummaryTableModel.getNetworkSummaries().get(_selectedNDExNetworkIndex).getName());
				   _mainSaveButton.doClick();
			   }
			   
            }
         }
		});
		
		//populate the table
		Server selectedServer = ServerManager.INSTANCE.getSelectedServer();
		if (selectedServer.getUsername() != null){
			try {
				_myNetSummaryTableModel.replaceNetworkSummaries(selectedServer.getModelAccessLayer().getMyNetworks());
			} catch (IOException | NdexException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						ErrorMessage.failedServerCommunication + "\n\nError Message: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				}
		}
		return myNetworksTable;
	}
	
	private void newMyNetworksTableSorterFilter(){
		RowFilter<NetworkSummaryTableModel, Object> rf = null;
		// if current expression fails do not update
		try {
			rf = RowFilter.regexFilter(_ndexSaveAsTextField.getText(), 0);
		} catch(java.util.regex.PatternSyntaxException e){
			return;
		}
		_myNetworksTableSorter.setRowFilter(rf);
	}
	
	private void createNDExPanel(){
		_ndexPanel = new JPanel();
		_ndexPanel.setPreferredSize(_ndexPanelDimension);
	
		JPanel saveAsPanel = new JPanel();
		JLabel saveAsLabel = new JLabel("<html><font color=\"#000000\">Save As:</font></html>");
		saveAsPanel.add(saveAsLabel, BorderLayout.LINE_START);
		_ndexSaveAsTextField = new JTextField(_initialNetworkName);
		_ndexSaveAsTextField.setPreferredSize(new Dimension(300, 25));
		_ndexSaveAsTextField.getDocument().addDocumentListener(new DocumentListener(){
				@Override
				public void insertUpdate(DocumentEvent e){
					//newMyNetworksTableSorterFilter();
					_mainSaveButton.setEnabled(_ndexSaveAsTextField.getText().length() > 0);
				}
				@Override
				public void removeUpdate(DocumentEvent e){
					//newMyNetworksTableSorterFilter();
					_mainSaveButton.setEnabled(_ndexSaveAsTextField.getText().length() > 0);
					
				}
				@Override
				public void changedUpdate(DocumentEvent e){
					//newMyNetworksTableSorterFilter();
					_mainSaveButton.setEnabled(_ndexSaveAsTextField.getText().length() > 0);
				}
			});
		saveAsPanel.add(_ndexSaveAsTextField, BorderLayout.LINE_END);
		_ndexPanel.add(saveAsPanel, BorderLayout.PAGE_START);
		JScrollPane scrollPane = new JScrollPane(getMyNetworksJTable());
		scrollPane.setPreferredSize(new Dimension(570,150));
		_ndexPanel.add(scrollPane, BorderLayout.PAGE_START);
		
		
		_locationLabel = new JLabel("");
		if (ServerManager.INSTANCE.getSelectedServer() != null){
			updateLocationLabel(ServerManager.INSTANCE.getSelectedServer().getUrl());
		} else {
			updateLocationLabel(null);
		}
		_locationLabel.setPreferredSize(new Dimension(saveAsPanel.getPreferredSize().width-10, 25));
		_ndexPanel.add(_locationLabel, BorderLayout.PAGE_START);
		

		JPanel signedInPanel = getNDExSignInPanel();
		_ndexPanel.add(signedInPanel, BorderLayout.PAGE_START);
	}
	
	private void updateLocationLabel(final String newLocation){
		String nLocation;
		if (newLocation == null){
			nLocation = "unset";
		} else {
			nLocation = newLocation;
		}
		_locationLabel.setText("<html><font color=\"#808080\">Location: " + nLocation + "</font></html>");
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
				if (ServerManager.INSTANCE.getSelectedServer() != null){
					updateLocationLabel(ServerManager.INSTANCE.getSelectedServer().getUrl());
				} else {
					updateLocationLabel(null);
		}
				return 1;
			});
		}
	}
}
