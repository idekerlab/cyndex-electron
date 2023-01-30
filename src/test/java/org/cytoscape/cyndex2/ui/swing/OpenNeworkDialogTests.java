package org.cytoscape.cyndex2.ui.swing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.cytoscape.cyndex2.internal.ui.swing.OpenNetworkDialog;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import org.cytoscape.cyndex2.util.TestUtil;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class OpenNeworkDialogTests {
	
	@Test
	public void testGetNDExSelectedNetworkBeforeCreateGUI(){
		OpenNetworkDialog dialog = new OpenNetworkDialog();
		assertNull(dialog.getNDExSelectedNetwork());
	}
	
	@Test
	public void testcreateGUI(){
		TestUtil testUtil = new TestUtil(true);
		testUtil.removeAllServers();
		OpenNetworkDialog dialog = new OpenNetworkDialog();
		try {
			assertTrue(dialog.createGUI());
			assertNull(dialog.getNDExSelectedNetwork());
		} catch(Exception ex){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String utf8 = StandardCharsets.UTF_8.name();
			try {
				try (PrintStream ps = new PrintStream(baos, true, utf8)){
					ex.printStackTrace(ps);
					fail("Stack trace: " + baos.toString(utf8));
				}
			} catch(Exception uex){
				
			}
			ex.printStackTrace();
		}
	}
}
