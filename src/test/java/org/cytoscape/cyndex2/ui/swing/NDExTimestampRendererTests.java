package org.cytoscape.cyndex2.ui.swing;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Locale;
import org.cytoscape.cyndex2.internal.ui.swing.NDExTimestampRenderer;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author churas
 */
public class NDExTimestampRendererTests {
	
	@Test
	public void testSetValueNull(){
		NDExTimestampRenderer renderer = new NDExTimestampRenderer();
		renderer.setValue(null);
		assertEquals("", renderer.getText());
	}
	
	@Test
	public void testInvalidObjectPassedIn(){
		NDExTimestampRenderer renderer = new NDExTimestampRenderer();
		renderer.setValue("foo");		
		assertEquals("", renderer.getText());
	}
	
	@Test
	public void testValidTime(){
		Timestamp myTime = new Timestamp(0);
		NDExTimestampRenderer renderer = new NDExTimestampRenderer();
		renderer.setValue(myTime);
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
		
		assertEquals(formatter.format(myTime), renderer.getText());
	}
	
}
