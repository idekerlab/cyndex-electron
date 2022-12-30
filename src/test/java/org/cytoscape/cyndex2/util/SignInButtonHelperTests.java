package org.cytoscape.cyndex2.util;

import java.util.List;
import java.util.stream.Collectors;
import org.cytoscape.cyndex2.internal.ui.swing.SignInButtonHelper;
import org.cytoscape.cyndex2.internal.util.Server;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class SignInButtonHelperTests {
	
	@Test
	public void testGetSignInTextNoServersAvailable() throws Exception {
		TestUtil testUtil = new TestUtil(true);
		testUtil.removeAllServers();
		assertEquals("Sign in", SignInButtonHelper.getSignInText());
	}
	
	@Test
	public void testGetSignInText() throws Exception {
		TestUtil testUtil = new TestUtil(true);
		testUtil.removeAllServers();
		assertEquals("Sign in", SignInButtonHelper.getSignInText());
	}
}
