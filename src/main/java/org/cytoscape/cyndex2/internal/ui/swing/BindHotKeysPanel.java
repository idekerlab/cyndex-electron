package org.cytoscape.cyndex2.internal.ui.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.cytoscape.cyndex2.internal.CyActivator;
import org.cytoscape.cyndex2.internal.util.OpenSaveHotKeyChanger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a panel that lets a caller denote whether hotkeys should be 
 * bound to Session or Network File Menu options
 * 
 * Design
 *  --------------------------------------------------
 * | Bind Open/Save Hotkeys to:  * Network  O Session |
 *  --------------------------------------------------
 * 
 * @author churas
 */
public class BindHotKeysPanel implements ActionListener {
	
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(BindHotKeysPanel.class);
	
	JRadioButton _sessionButton;
	JRadioButton _networkButton;

	public JPanel getHotKeysPanel(){
		JPanel panel = new JPanel();
		BorderLayout layout = new BorderLayout();
		panel.setLayout(layout);
		JLabel label = new JLabel("Bind Open/Save Hotkeys to:");
		label.setToolTipText("Sets Open, Save, and Save As hotkeys to either Network or Session open/save options in File menu");
		panel.add(label, BorderLayout.WEST);
		
		_networkButton = new JRadioButton("Network");
		_networkButton.setName("network");
		_networkButton.setToolTipText("Click to assign Open, Save, and Save As hotkeys to Network Open and Save options in File menu");
		_networkButton.addActionListener(this);
		panel.add(_networkButton, BorderLayout.CENTER);
		_sessionButton = new JRadioButton("Session");
		_sessionButton.setToolTipText("Click to assign Open, Save, and Save As hotkeys to Session Open and Save options in File menu");
		_sessionButton.setName("session");
		panel.add(_sessionButton, BorderLayout.EAST);
		_sessionButton.addActionListener(this);
		
		
		ButtonGroup group = new ButtonGroup();
		group.add(_networkButton);
		group.add(_sessionButton);
		
		updateSelectionBasedOnPreferences();
		
		return panel;
	}
	
	public void updateSelectionBasedOnPreferences(){
		boolean sessionControl = CyActivator.disableAppControlOfHotKeys();
		_sessionButton.setSelected(sessionControl);
		_networkButton.setSelected(!sessionControl);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!(e.getSource() instanceof JRadioButton)){
			return;
		}
		JRadioButton button = (JRadioButton)e.getSource();
		if (button.isSelected() && button.getName().equals("network")){
			//_changer.putHotKeysOntoNetworkMenus();
			CyActivator.setDisableAppControlOfHotKeys(false);
		} else if (button.isSelected() && button.getName().equals("session")){
			//_changer.putHotKeysOntoSessionMenus();
			CyActivator.setDisableAppControlOfHotKeys(true);
		}
		logger.debug(e.toString());
	}
}
