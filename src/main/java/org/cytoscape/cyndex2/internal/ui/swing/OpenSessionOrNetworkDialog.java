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
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import org.cytoscape.cyndex2.internal.ui.StringMatchRowFilter;
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
	public final static String MY_NETWORKS_TABBED_PANE = "My Networks Tabbed Pane";
	public final static String SEARCH_NETWORKS_TABBED_PANE = "";
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
	
	/**
	 * selected row in NDEx my networks, if none is selected value is -1
	 */
	private int _selectedNDExNetworkIndex = -1;
	
	/**
	 * selected row in NDEx search networks, if none is selected value is -1
	 */
	private int _selectedNDExSearchNetworkIndex = -1;
	private JTextField _ndexMyNetworksSearchField;
	private JTextField _ndexSearchField;
	private TableRowSorter _myNetworksTableSorter;
	private JButton _ndexSearchButton;
	private JTable _myNetworksTable;
	
	/**
	 * Flag to denote whether the open NDEx panel has ever been displayed
	 * this is needed to fire a property change listener to populate
	 * the table the first time the display is loaded 
	 */
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
	 * Initializes gui once, subsequent calls update NDEx my networks table
	 * and search table
	 * @return 
	 */
	public boolean createGUI(){
		if (_guiLoaded == false){
			
			_mainOpenButton = new JButton("Open");
			_mainCancelButton = new JButton("Cancel");
			_searchTableModel = new MyNetworksWithOwnerTableModel(new ArrayList<>());
			this.add(getOpenPanel());
			this.invalidate();
			
			_mainOpenButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane pane = getOptionPane((JComponent)e.getSource());
						if (pane != null){
	                        pane.setValue(_mainOpenButton);
						}
                    }
                });
			_mainOpenButton.setEnabled(false);
			
			_mainCancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane pane = getOptionPane((JComponent)e.getSource());
						if (pane != null){
	                        pane.setValue(_mainCancelButton);
						}
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
		updateSearchTable();

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
		if (getSelectedCard() != null && getSelectedCard().equals(OpenSessionOrNetworkDialog.OPEN_SESSION)){
			
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
				return _searchTableModel.getNetworkSummaries().get(_selectedNDExSearchNetworkIndex);
			}
		}
		return null;
	}
	
	private JPanel getOpenPanel(){
		JPanel openDialogPanel = new JPanel();
		openDialogPanel.setPreferredSize(_dialogDimension);
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(_leftPanelDimension);
		
		//using html fragment to set color and size of text
        _openNDExButton = new JButton("<html><font color=\"#000000\">Open Network<br/><br/><font size=\"-2\">Open a network from NDEx</font></font></html>");
		_openNDExButton.setOpaque(true);
        _openNDExButton.setPreferredSize(_leftButtonsDimensions);
		_defaultButtonColor = _openNDExButton.getBackground();
		_openNDExButton.addActionListener(new ActionListener() {
			/**
			 * When a user clicks on the open NDEx button need to change
			 * the background for the open NDEx button and for open session 
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

		//using html fragment to set color and size of text
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
					LOGGER.debug(evt.getActionCommand() + " " + evt.getSource());
					if (getSelectedSessionFile() != null && getSelectedSessionFile().isFile()){
						_mainOpenButton.doClick();
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

		_myNetworksTable = new JTable(_myNetworksTableModel);
		_myNetworksTableSorter = new TableRowSorter<>(_myNetworksTableModel);
		_myNetworksTable.setRowSorter(_myNetworksTableSorter);
		_myNetworksTable.setPreferredScrollableViewportSize(new Dimension(400, 250));
        _myNetworksTable.setFillsViewportHeight(true);
		
		_myNetworksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_myNetworksTable.setDefaultRenderer(Timestamp.class, new NDExTimestampRenderer());
		_myNetworksTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
				// do some actions here, for example
				// print first column value from selected row
				
				if (_myNetworksTable.getSelectedRow() == -1){
					LOGGER.debug("Nothing selected");
					_mainOpenButton.setEnabled(false);
					_selectedNDExNetworkIndex = -1;
				} else {
					LOGGER.debug(event.toString() + " " + _myNetworksTable.getValueAt(_myNetworksTable.getSelectedRow(), 0).toString());
					_mainOpenButton.setEnabled(true);
					_selectedNDExNetworkIndex = _myNetworksTable.convertRowIndexToModel(_myNetworksTable.getSelectedRow());
				}
			}
        });
		_myNetworksTable.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() == 2) {     // to detect double click events
               JTable target = (JTable)me.getSource();
               int row = target.getSelectedRow(); // select a row
			   if (row != -1){
	               LOGGER.debug("Double click: " + _myNetworksTable.getValueAt(_myNetworksTable.getSelectedRow(), 0).toString());
				   _selectedNDExNetworkIndex = _myNetworksTable.convertRowIndexToModel(_myNetworksTable.getSelectedRow());
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
		_ndexMyNetworksSearchField.setToolTipText("Search by name within My Networks");
		_ndexMyNetworksSearchField.setPreferredSize(new Dimension(475,22));
		_ndexMyNetworksSearchField.getDocument().addDocumentListener(new DocumentListener(){
			//There are three events that need to be monitored to catch changes to a text
			//field
				@Override
				public void insertUpdate(DocumentEvent e){
					newMyNetworksTableSorterFilter();
					_mainOpenButton.setEnabled(_myNetworksTable.getSelectedRow() != -1);
				}
				@Override
				public void removeUpdate(DocumentEvent e){
					newMyNetworksTableSorterFilter();
					_mainOpenButton.setEnabled(_myNetworksTable.getSelectedRow() != -1);
					
				}
				@Override
				public void changedUpdate(DocumentEvent e){
					newMyNetworksTableSorterFilter();
					_mainOpenButton.setEnabled(_myNetworksTable.getSelectedRow() != -1);
				}
			});
		myNetSearchPanel.add(_ndexMyNetworksSearchField, BorderLayout.LINE_START);
				
		myNetPanel.add(myNetSearchPanel, BorderLayout.PAGE_START);
		myNetPanel.setName(OpenSessionOrNetworkDialog.MY_NETWORKS_TABBED_PANE);
		JScrollPane scrollPane = new JScrollPane(_myNetworksTable);
		scrollPane.setPreferredSize(new Dimension(570,250));
		myNetPanel.add(scrollPane, BorderLayout.PAGE_END);
		
		_ndexTabbedPane.add("My Networks", myNetPanel);
	}
	
	private void createNDExSearchAllTabbedPane(){
		
		JTable searchTable = new JTable(_searchTableModel);
		searchTable.setAutoCreateRowSorter(true);
		searchTable.setPreferredScrollableViewportSize(new Dimension(400, 250));
        searchTable.setFillsViewportHeight(true);
		searchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchTable.setDefaultRenderer(Timestamp.class, new NDExTimestampRenderer());
		searchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
				if (searchTable.getSelectedRow() == -1){
					LOGGER.debug("Nothing selected, disabling open button");
					_mainOpenButton.setEnabled(false);
					_selectedNDExSearchNetworkIndex = -1;
				} else {
					LOGGER.debug(event.toString() + " " + searchTable.getValueAt(searchTable.getSelectedRow(), 0).toString());
					LOGGER.debug("\t" + _searchTableModel.getNetworkSummaries().get(searchTable.getSelectedRow()).getName());
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
	               LOGGER.debug("Double click: " + searchTable.getValueAt(searchTable.getSelectedRow(), 0).toString());
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
		_ndexSearchField.setToolTipText("Search all of NDEx for networks");
		_ndexSearchField.setPreferredSize(new Dimension(475,22));
		_ndexSearchField.addActionListener(new ActionListener(){
			/**
			 * Catch user hitting enter key on search field
			 * @param e 
			 */
			@Override
			public void actionPerformed(ActionEvent e){
				updateSearchTable();
			}
		});
		searchSearchPanel.add(_ndexSearchField, BorderLayout.LINE_START);
		_ndexSearchButton = new JButton("search");
		_ndexSearchButton.setToolTipText("Search all of NDEx for networks");
		_ndexSearchButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				updateSearchTable();
				
			}
		});
		
		_ndexSearchButton.setPreferredSize(new Dimension(_ndexSearchButton.getPreferredSize().width,22));
		searchSearchPanel.add(_ndexSearchButton, BorderLayout.LINE_END);
		
		searchPanel.add(searchSearchPanel, BorderLayout.PAGE_START);
		searchPanel.setName(SEARCH_NETWORKS_TABBED_PANE);
		JScrollPane scrollPane = new JScrollPane(searchTable);
		scrollPane.setPreferredSize(new Dimension(570,250));
		searchPanel.add(scrollPane, BorderLayout.PAGE_END);		
		_ndexTabbedPane.add("Search NDEx", searchPanel);
	}
	
	private void updateSearchTable(){
		_searchTableModel.clearNetworkSummaries();
		if (_ndexSearchField.getText().isBlank()){
			return;
		} 
		try {
			updateSearchTable(_ndexSearchField.getText());
		} catch(Exception jpe){
			LOGGER.warn("Exception while running new search", jpe);
		}
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
				LOGGER.debug("Tab: " + _ndexTabbedPane.getSelectedIndex());
				_mainOpenButton.setEnabled(false);
				if (getSelectedCard().equals(OpenSessionOrNetworkDialog.OPEN_NDEX)){
					if (_ndexTabbedPane.getSelectedIndex() == 0 && _selectedNDExNetworkIndex != -1){
						_mainOpenButton.setEnabled(true);
					}
					if (_ndexTabbedPane.getSelectedIndex() == 1 && _selectedNDExSearchNetworkIndex != -1){
						_mainOpenButton.setEnabled(true);
					}
				}
			}
		});
		
		_ndexPanel.add(_ndexTabbedPane, BorderLayout.PAGE_END);
		createNDExMyNetworksTabbedPane();
		createNDExSearchAllTabbedPane();
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
			rf = StringMatchRowFilter.getStringMatchRowFilter(_ndexMyNetworksSearchField.getText());
		} catch(java.util.regex.PatternSyntaxException e){
			return;
		}
		_myNetworksTableSorter.setRowFilter(rf);
		
	}
}
