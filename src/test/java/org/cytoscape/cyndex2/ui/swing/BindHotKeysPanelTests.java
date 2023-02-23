package org.cytoscape.cyndex2.ui.swing;

import java.awt.event.ActionEvent;
import java.util.Properties;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.cytoscape.cyndex2.internal.ui.swing.BindHotKeysPanel;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class BindHotKeysPanelTests {
	
	@Test
	public void testGetHotKeysPanel(){
	
		BindHotKeysPanel panelFac = new BindHotKeysPanel();
		JPanel panel = panelFac.getHotKeysPanel();
		assertNotNull(panel);
	}
	
	@Test
	public void testActionPerformedInvalidButton(){
	
		BindHotKeysPanel panelFac = new BindHotKeysPanel();
		JPanel panel = panelFac.getHotKeysPanel();
		assertNotNull(panel);
		JRadioButton testButton = new JRadioButton();
		testButton.setName("foo");
		panelFac.actionPerformed(new ActionEvent(testButton, 0, "foo"));
	}
	
	@Test
	public void testActionPerformedNotAButton(){
	
		BindHotKeysPanel panelFac = new BindHotKeysPanel();
		JPanel panel = panelFac.getHotKeysPanel();
		assertNotNull(panel);
		JLabel notButton = new JLabel("not a button");
		notButton.setName("network");
		panelFac.actionPerformed(new ActionEvent(notButton, 0, "foo"));
	}
	
	@Test
	public void testActionPerformedNetworkButton(){
	
		BindHotKeysPanel panelFac = new BindHotKeysPanel();
		JPanel panel = panelFac.getHotKeysPanel();
		assertNotNull(panel);
		JRadioButton testButton = new JRadioButton();
		testButton.setName("network");
		panelFac.actionPerformed(new ActionEvent(testButton, 0, "foo"));
		testButton.setSelected(true);
		panelFac.actionPerformed(new ActionEvent(testButton, 0, "foo"));

	}
	
	@Test
	public void testActionPerformedSessionButton(){
		BindHotKeysPanel panelFac = new BindHotKeysPanel();
		JPanel panel = panelFac.getHotKeysPanel();
		assertNotNull(panel);
		JRadioButton testButton = new JRadioButton();
		testButton.setName("session");
		panelFac.actionPerformed(new ActionEvent(testButton, 0, "foo"));
		testButton.setSelected(true);
		panelFac.actionPerformed(new ActionEvent(testButton, 0, "foo"));
	}

}
