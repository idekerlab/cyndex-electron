package org.cytoscape.cyndex2.task;

import java.util.ArrayList;
import javax.swing.RowFilter;
import org.cytoscape.cyndex2.internal.task.CanceledTask;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.ndexbio.model.object.network.NetworkSummary;

/**
 *
 * @author churas
 */
public class CanceledTaskTests {
	
	@Test
	public void testCancel(){
		CanceledTask ct = new CanceledTask();
		ct.cancel();
	}

	@Test
	public void testRun() throws Exception {
		CanceledTask ct = new CanceledTask();
		ct.run(null);
	}

}
