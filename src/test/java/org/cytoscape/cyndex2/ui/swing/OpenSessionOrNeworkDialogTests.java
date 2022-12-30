package org.cytoscape.cyndex2.ui.swing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.cytoscape.cyndex2.internal.ui.swing.OpenSessionOrNetworkDialog;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import org.cytoscape.cyndex2.util.TestUtil;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class OpenSessionOrNeworkDialogTests {
	
	@Test
	public void testGetSelectedSessionFileBeforeCreateGUI(){
		OpenSessionOrNetworkDialog dialog = new OpenSessionOrNetworkDialog();
		assertNull(dialog.getSelectedSessionFile());
	}
	
	@Test
	public void testGetNDExSelectedNetworkBeforeCreateGUI(){
		OpenSessionOrNetworkDialog dialog = new OpenSessionOrNetworkDialog();
		assertNull(dialog.getNDExSelectedNetwork());
	}
	
	@Test
	public void testcreateGUI(){
		TestUtil testUtil = new TestUtil(true);
		testUtil.removeAllServers();
		OpenSessionOrNetworkDialog dialog = new OpenSessionOrNetworkDialog();
		try {
			assertTrue(dialog.createGUI());
			assertEquals(OpenSessionOrNetworkDialog.OPEN_NDEX, dialog.getSelectedCard());
			assertNull(dialog.getSelectedSessionFile());
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
