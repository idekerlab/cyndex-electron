package org.cytoscape.cyndex2.task;

import java.util.UUID;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyndex2.internal.task.CanceledTask;
import org.cytoscape.cyndex2.internal.task.SaveNetworkToNDExTaskFactoryImpl;
import org.cytoscape.cyndex2.internal.ui.swing.ShowDialogUtil;
import org.cytoscape.cyndex2.internal.util.NDExNetworkManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author churas
 */
public class SaveNetworkToNDExTaskFactoryImplTests {
	
	
	
	@Test
	public void testIsReady(){
		SaveNetworkToNDExTaskFactoryImpl fac = new SaveNetworkToNDExTaskFactoryImpl(null, false, 0l);
		assertTrue(fac.isReady());
	}
	
	@Test
	public void testCreateTaskIteratorCurrentNetworkNull(){
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		
		CyApplicationManager mockAppManager = mock(CyApplicationManager.class);
		when(mockAppManager.getCurrentNetwork()).thenReturn(null);
		when(mockRegistrar.getService(CyApplicationManager.class)).thenReturn(mockAppManager);
		
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		when(mockSwing.getJFrame()).thenReturn(null);
		when(mockRegistrar.getService(CySwingApplication.class)).thenReturn(mockSwing);
		
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		SaveNetworkToNDExTaskFactoryImpl fac = new SaveNetworkToNDExTaskFactoryImpl(mockRegistrar, false, 0l, mockDialogUtil, null);
		TaskIterator ti = fac.createTaskIterator();
		assertNotNull(ti);
		assertEquals(1, ti.getNumTasks());
		assertTrue(ti.next() instanceof CanceledTask);
		verify(mockRegistrar).getService(CySwingApplication.class);
		verify(mockDialogUtil).showMessageDialog(null, "Please select a network to save\n");
		
	}
	
	/**
	@Test
	public void testCreateTaskIteratorNoNDExCredentials(){
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		
		CyApplicationManager mockAppManager = mock(CyApplicationManager.class);
		CyNetwork mockNetwork = mock(CyNetwork.class);
		CyRow mockRow = mock(CyRow.class);
		when(mockNetwork.getRow(mockNetwork)).thenReturn(mockRow);
		Long networkSUID = 1l;
		when(mockNetwork.getSUID()).thenReturn(1l);
		CyTable mockTable = mock(CyTable.class);
		CyRow mockSUIDROW = mock(CyRow.class);
		UUID ndexUUID = UUID.randomUUID();
		when(mockSUIDROW.get(NDExNetworkManager.UUID_COLUMN, String.class)).thenReturn(ndexUUID.toString());
		when(mockTable.getRow(networkSUID)).thenReturn(mockSUIDROW);
		when(mockNetwork.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS)).thenReturn(mockTable);
		when(mockRow.get(CyNetwork.NAME, String.class)).thenReturn(null);
		
		
		when(mockAppManager.getCurrentNetwork()).thenReturn(mockNetwork);
		
		when(mockRegistrar.getService(CyApplicationManager.class)).thenReturn(mockAppManager);
		
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		when(mockSwing.getJFrame()).thenReturn(null);
		when(mockRegistrar.getService(CySwingApplication.class)).thenReturn(mockSwing);
		
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		SaveNetworkToNDExTaskFactoryImpl fac = new SaveNetworkToNDExTaskFactoryImpl(mockRegistrar, false, 0l, mockDialogUtil, null);
		TaskIterator ti = fac.createTaskIterator();
		assertNotNull(ti);
		assertEquals(1, ti.getNumTasks());
		assertTrue(ti.next() instanceof CanceledTask);
		verify(mockRegistrar).getService(CySwingApplication.class);
		verify(mockDialogUtil).showMessageDialog(null, "Please select a network to save\n");
		
	}
	*/
	
}
