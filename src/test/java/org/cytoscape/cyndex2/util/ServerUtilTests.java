package org.cytoscape.cyndex2.util;

import static org.junit.Assert.assertEquals;
import org.cytoscape.cyndex2.internal.util.ServerUtil;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class ServerUtilTests {
	
	
	@Test
	public void testConstructor(){
		ServerUtil su = new ServerUtil();
		assertNotNull(su);
	}
	
	@Test
	public void testGetDisplayUsernameHTML() {
				
		// test with null
		assertEquals("<i>anonymous</i>", ServerUtil.getDisplayUsernameHTML(null));
		
		// test with empty str
		assertEquals("", ServerUtil.getDisplayUsernameHTML(""));

		// test with name
		assertEquals("bob", ServerUtil.getDisplayUsernameHTML("bob"));

	}
	
}



