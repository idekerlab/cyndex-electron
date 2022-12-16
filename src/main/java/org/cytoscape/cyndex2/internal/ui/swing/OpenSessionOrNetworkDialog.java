package org.cytoscape.cyndex2.internal.ui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import org.ndexbio.model.object.network.NetworkSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Open Dialog (OpenDialog)
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
public class OpenSessionOrNetworkDialog extends AbstractOpenSaveDialog {
	private final static Logger LOGGER = LoggerFactory.getLogger(OpenSessionOrNetworkDialog.class);
	public final static String OPEN_SESSION = "OpenSession";
	public final static String OPEN_NDEX = "OpenNDEx";
	public final static String SIGN_IN = "Sign in";
	public final static String SIGN_OUT = "Sign out";
	private boolean _guiLoaded;
	private JPanel _cards;
	private JButton _openSessionButton;
	private JButton _openNDExButton;
	private JButton _mainOpenButton;
	private JButton _mainCancelButton;
	private JFileChooser _sessionChooser;
	private JPanel _ndexPanel;
	private JTabbedPane _ndexTabbedPane;
	
	private Color _defaultButtonColor;
	private String _selectedCard;
	private int _selectedNDExNetworkIndex = -1;
	private int _selectedNDExSearchNetworkIndex = -1;
	MyNetworksWithOwnerTableModel _searchNetSummaryTable;
	private JTextField _ndexMyNetworksSearchField;
	private JTextField _ndexSearchField;
	private JButton _ndexMyNetworksSearchButton;
	private JButton _ndexSearchButton;
	
	private boolean _ndexNeverDisplayed = true;
	
	
	public OpenSessionOrNetworkDialog(){
		super();
		_guiLoaded = false;
	}
	
	/**
	 * Gets open button for main dialog so caller can add it to
	 * the JOptionPane dialog
	 * @return 
	 */
	public JButton getMainOpenButton(){
		return _mainOpenButton;
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
			_mainOpenButton = new JButton("Open");
			_mainCancelButton = new JButton("Cancel");
			_searchNetSummaryTable = new MyNetworksWithOwnerTableModel(new ArrayList<>());
			this.add(getOpenPanel());
			this.invalidate();
			
			_mainOpenButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane pane = getOptionPane((JComponent)e.getSource());
                        pane.setValue(_mainOpenButton);
                    }
                });
			_mainOpenButton.setEnabled(false);
			
			_mainCancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane pane = getOptionPane((JComponent)e.getSource());
                        pane.setValue(_mainCancelButton);
                    }
                });
			
			// TODO: need to remember desired behavior via preferences
			//_openSessionButton.setEnabled(true);
			_openNDExButton.doClick();
			// listen for changes to NDEx credentials
			ServerManager.INSTANCE.addPropertyChangeListener(this);
			_guiLoaded = true;
		} 
		updateMyNetworksTable();

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
		if (getSelectedCard().equals(OpenSessionOrNetworkDialog.OPEN_SESSION)){
			
			if (_sessionChooser.getSelectedFile() != null && _sessionChooser.getSelectedFile().isFile()){
				return _sessionChooser.getSelectedFile();
			}
			return null;
		}
		return null;
	}
	
	/**
	 * Gets the NDEx selected network if the open NDEx button/tab is selected and
	 * the user has selected a network
	 * @return selected network or {@code null}
	 */
	public NetworkSummary getNDExSelectedNetwork(){
		if (getSelectedCard() == null){
			return null;
		}
		if (getSelectedCard().equals(OpenSessionOrNetworkDialog.OPEN_NDEX)){
			if (_ndexTabbedPane.getSelectedIndex() == 0 && _selectedNDExNetworkIndex != -1){
				return _myNetworksTableModel.getNetworkSummaries().get(_selectedNDExNetworkIndex);
			}
			if (_ndexTabbedPane.getSelectedIndex() == 1 && _selectedNDExSearchNetworkIndex != -1){
				return _searchNetSummaryTable.getNetworkSummaries().get(_selectedNDExSearchNetworkIndex);
			}
		}
		return null;
	}
	
	private JPanel getOpenPanel(){
		JPanel openDialogPanel = new JPanel();
		openDialogPanel.setPreferredSize(_dialogDimension);
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(_leftPanelDimension);
        _openNDExButton = new JButton("<html><font color=\"#000000\">Open Network<br/><br/><font size=\"-2\">Open a network from NDEx</font></font></html>");
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
				cl.show(_cards, OpenSessionOrNetworkDialog.OPEN_NDEX);
				_openNDExButton.setBackground(_NDExButtonBlue);
				_openSessionButton.setBackground(_defaultButtonColor);
				_selectedCard = OpenSessionOrNetworkDialog.OPEN_NDEX;
				setButtonFocus(true, _openNDExButton);
				setButtonFocus(false, _openSessionButton);
				// need to figure out if this should be enabled or not
				_mainOpenButton.setEnabled(false);
				if (_ndexNeverDisplayed == true){
					_ndexNeverDisplayed = false;
					// if we have never displayed this panel
					// we need to fire a property change listener
					// to populate the table otherwise do nothing
					if (ServerManager.INSTANCE.getSelectedServer() != null){
						ServerManager.INSTANCE.firePropertyChangeEvent();
					}
				} else if (getNDExSelectedNetwork() != null){
					_mainOpenButton.setEnabled(true);					
				}
			}
		});
		
        leftPanel.add(_openNDExButton, BorderLayout.PAGE_START);

        _openSessionButton = new JButton("<html><font color=\"#000000\">Open Session<br/><br/><font size=\"-2\">Open a session (.cys) file from the local machine</font></font></html>");
		_openSessionButton.setOpaque(true);
        _openSessionButton.setPreferredSize(_leftButtonsDimensions);

		_openSessionButton.addActionListener(new ActionListener() {
			/**
			 * When a user clicks on the open session button need to change
			 * the background for the open ndex button and for open session 
			 * button. Also need to determine if the open button should be
			 * enabled or not
			 */
			@Override
			public void actionPerformed(ActionEvent e){
				CardLayout cl = (CardLayout)_cards.getLayout();
				cl.show(_cards, OpenSessionOrNetworkDialog.OPEN_SESSION);
				_openNDExButton.setBackground(_defaultButtonColor);
				_openSessionButton.setBackground(_SessionButtonOrange);
				_selectedCard = OpenSessionOrNetworkDialog.OPEN_SESSION;
				setButtonFocus(false, _openNDExButton);
				setButtonFocus(true, _openSessionButton);
				if (getSelectedSessionFile() != null){
					_mainOpenButton.setEnabled(true);
				} else {
					_mainOpenButton.setEnabled(false);
				}
			}
		});

		leftPanel.add(_openSessionButton, BorderLayout.PAGE_END);
        openDialogPanel.add(leftPanel, BorderLayout.LINE_START);

        JPanel rightPanel = getRightCardPanel();
        openDialogPanel.add(rightPanel, BorderLayout.LINE_END);
		
		return openDialogPanel;
		
	}
	
	private void createJFileChooser(){
		_sessionChooser = new JFileChooser(".");
		_sessionChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		_sessionChooser.setControlButtonsAreShown(false);
		_sessionChooser.addPropertyChangeListener(new PropertyChangeListener() {
			/**
			 * Watch for File changed event and if the user selected a file enable the
			 * open button otherwise disable the open button
			 * @param evt 
			 */
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())){
					if (evt.getNewValue() != null){
						// This event fires if a directory is selected
						// so we need to check for that
						File selectedFile = (File)evt.getNewValue();
					
						if (selectedFile.isFile()){
							_mainOpenButton.setEnabled(true);
						} else {
							_mainOpenButton.setEnabled(false);
						}
					} else {
						_mainOpenButton.setEnabled(false);
					}
				}
            }
		});

		_sessionChooser.addActionListener(new ActionListener(){
			
			/**
			 * Look for double click on a file in the Chooser, if found then assume
			 * the user wants to load that file so simulate a click of the Open button
			 * @param evt 
			 */
			@Override
			public void actionPerformed(ActionEvent evt){
				if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())){
					System.out.println(evt.getActionCommand() + " " + evt.getSource());
					if (getSelectedSessionFile() != null && getSelectedSessionFile().isFile()){
						_mainOpenButton.doClick();
					}
				}
			}
		});

	}
	
	private JPanel getRightCardPanel(){
		_cards = new JPanel(new CardLayout());
        //rightPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Right"),BorderFactory.createEmptyBorder(0,0,0,0)));
        _cards.setPreferredSize(this._rightPanelDimension);
		
		createJFileChooser();
		
		CardLayout cl = (CardLayout)_cards.getLayout();
		
		_cards.add(_sessionChooser, OpenSessionOrNetworkDialog.OPEN_SESSION);
		cl.addLayoutComponent(_sessionChooser, OpenSessionOrNetworkDialog.OPEN_SESSION);
		_selectedCard = OpenSessionOrNetworkDialog.OPEN_SESSION;
		
		createNDExPanel();
		_cards.add(_ndexPanel, OpenSessionOrNetworkDialog.OPEN_NDEX);
		cl.addLayoutComponent(_ndexPanel, OpenSessionOrNetworkDialog.OPEN_NDEX);
		
		return _cards;
	}
	
	private void createNDExMyNetworksTabbedPane(){
		_myNetworksTableModel = new MyNetworksWithOwnerTableModel(new ArrayList<>());

		JTable myNetworksTable = new JTable(_myNetworksTableModel);
		myNetworksTable.setAutoCreateRowSorter(true);
		myNetworksTable.setPreferredScrollableViewportSize(new Dimension(400, 250));
        myNetworksTable.setFillsViewportHeight(true);
		
		myNetworksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		myNetworksTable.setDefaultRenderer(Timestamp.class, new NDExTimestampRenderer());
		myNetworksTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
				// do some actions here, for example
				// print first column value from selected row
				
				if (myNetworksTable.getSelectedRow() == -1){
					System.out.println("Nothing selected");
					_mainOpenButton.setEnabled(false);
					_selectedNDExNetworkIndex = -1;
				} else {
					System.out.println(event.toString() + " " + myNetworksTable.getValueAt(myNetworksTable.getSelectedRow(), 0).toString());
					_mainOpenButton.setEnabled(true);
					_selectedNDExNetworkIndex = myNetworksTable.convertRowIndexToModel(myNetworksTable.getSelectedRow());
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
				   _mainOpenButton.doClick();
			   }
			   
            }
         }
      });
		
		// panel for my networks
		JPanel myNetPanel = new JPanel();
		
		// top part of 
		JPanel myNetSearchPanel = new JPanel();
		
		_ndexMyNetworksSearchField = new JTextField("");
		_ndexMyNetworksSearchField.setEnabled(false);
		_ndexMyNetworksSearchField.setToolTipText("Search within My Networks");
		_ndexMyNetworksSearchField.setPreferredSize(new Dimension(475,22));
		myNetSearchPanel.add(_ndexMyNetworksSearchField, BorderLayout.LINE_START);
		_ndexMyNetworksSearchButton = new JButton("search");
		_ndexMyNetworksSearchButton.setEnabled(false);
		_ndexMyNetworksSearchButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				_myNetworksTableModel.clearNetworkSummaries();
				_selectedNDExNetworkIndex = -1;
				if (_ndexMyNetworksSearchField.getText().length() == 0){
					return;
				} 
				try {
					System.out.println("need to run query here");
					//NetworkSearchResult nrs = _ndexAccessLayer.findNetworks(_ndexMyNetworksSearchField.getText(), _ndexServer.getUsername(), null, true, 0, 400);
					//_myNetSummaryTable.replaceNetworkSummaries(nrs.getNetworks());
				} catch(Exception jpe){
					jpe.printStackTrace();
				}
				
			}
		});
		_ndexMyNetworksSearchButton.setToolTipText("Search all of NDEx for matching networks");
		_ndexMyNetworksSearchButton.setPreferredSize(new Dimension(_ndexMyNetworksSearchButton.getPreferredSize().width,22));
		myNetSearchPanel.add(_ndexMyNetworksSearchButton, BorderLayout.LINE_END);
		
		myNetPanel.add(myNetSearchPanel, BorderLayout.PAGE_START);
		myNetPanel.setName("My Networks Tabbed Pane");
		JScrollPane scrollPane = new JScrollPane(myNetworksTable);
		scrollPane.setPreferredSize(new Dimension(570,250));
		myNetPanel.add(scrollPane, BorderLayout.PAGE_END);
		
		_ndexTabbedPane.add("My Networks", myNetPanel);
	}
	
	private void createNDExSearchAllTabbedPane(){
		
		JTable searchTable = new JTable(_searchNetSummaryTable);
		searchTable.setAutoCreateRowSorter(true);
		searchTable.setPreferredScrollableViewportSize(new Dimension(400, 250));
        searchTable.setFillsViewportHeight(true);
		searchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchTable.setDefaultRenderer(Timestamp.class, new NDExTimestampRenderer());
		searchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
				// do some actions here, for example
				// print first column value from selected row
				
				if (searchTable.getSelectedRow() == -1){
					System.out.println("Nothing selected");
					_mainOpenButton.setEnabled(false);
					_selectedNDExSearchNetworkIndex = -1;
				} else {
					System.out.println(event.toString() + " " + searchTable.getValueAt(searchTable.getSelectedRow(), 0).toString());
					System.out.println("\t" + _searchNetSummaryTable.getNetworkSummaries().get(searchTable.getSelectedRow()).getName());
					_mainOpenButton.setEnabled(true);
					_selectedNDExSearchNetworkIndex = searchTable.convertRowIndexToModel(searchTable.getSelectedRow());
				}
			}
        });
		searchTable.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() == 2) {     // to detect doble click events
               JTable target = (JTable)me.getSource();
               int row = target.getSelectedRow(); // select a row
			   if (row != -1){
	               System.out.println("Double click: " + searchTable.getValueAt(searchTable.getSelectedRow(), 0).toString());
				   _selectedNDExSearchNetworkIndex = searchTable.convertRowIndexToModel(searchTable.getSelectedRow());
				   _mainOpenButton.doClick();
			   }
			   
            }
         }
		});
		// panel for my networks
		JPanel searchPanel = new JPanel();
		
		// top part of 
		JPanel searchSearchPanel = new JPanel();
		
		_ndexSearchField = new JTextField("");
		_ndexSearchField.setEnabled(false);
		_ndexSearchField.setToolTipText("Search all of NDEx for networks");
		_ndexSearchField.setPreferredSize(new Dimension(475,22));
		searchSearchPanel.add(_ndexSearchField, BorderLayout.LINE_START);
		_ndexSearchButton = new JButton("search");
		_ndexSearchButton.setEnabled(false);
		_ndexSearchButton.setToolTipText("Search all of NDEx for networks");
		_ndexSearchButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				_searchNetSummaryTable.clearNetworkSummaries();
				if (_ndexSearchField.getText().length() == 0){
					return;
				} 
				try {
					System.out.println("not implemented");
					//NetworkSearchResult nrs = _ndexAccessLayer.findNetworks(_ndexSearchField.getText(), null, null, true, 0, 400);
					//_searchNetSummaryTable.replaceNetworkSummaries(nrs.getNetworks());
				} catch(Exception jpe){
					jpe.printStackTrace();
				}
				
			}
		});
		
		_ndexSearchButton.setPreferredSize(new Dimension(_ndexSearchButton.getPreferredSize().width,22));
		searchSearchPanel.add(_ndexSearchButton, BorderLayout.LINE_END);
		
		searchPanel.add(searchSearchPanel, BorderLayout.PAGE_START);
		searchPanel.setName("Search Networks Tabbed Pane");
		JScrollPane scrollPane = new JScrollPane(searchTable);
		scrollPane.setPreferredSize(new Dimension(570,250));
		searchPanel.add(scrollPane, BorderLayout.PAGE_END);
				
		_ndexTabbedPane.add("Search NDEx", searchPanel);
	}
	
	private void createNDExPanel(){
		_ndexPanel = new JPanel();
		_ndexPanel.setPreferredSize(_ndexPanelDimension);
		
		_ndexPanel.add(getNDExSignInPanel(), BorderLayout.PAGE_START);
		
		// lets add tabbed pane
		_ndexTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		_ndexTabbedPane.setPreferredSize(new Dimension(600, 350));
		_ndexTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("Tab: " + _ndexTabbedPane.getSelectedIndex());
				if (getNDExSelectedNetwork() != null){
					_mainOpenButton.setEnabled(true);
				} else {
					_mainOpenButton.setEnabled(false);
				}
				
			}
		});
		
		_ndexPanel.add(_ndexTabbedPane, BorderLayout.PAGE_END);
		createNDExMyNetworksTabbedPane();
		createNDExSearchAllTabbedPane();
	}
}
