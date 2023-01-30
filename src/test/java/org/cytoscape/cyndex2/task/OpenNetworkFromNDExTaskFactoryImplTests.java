package org.cytoscape.cyndex2.task;

import java.io.IOException;
import java.util.UUID;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyndex2.internal.task.CanceledTask;
import org.cytoscape.cyndex2.internal.task.NetworkImportTask;
import org.cytoscape.cyndex2.internal.task.OpenNetworkFromNDExTaskFactoryImpl;
import org.cytoscape.cyndex2.internal.ui.swing.OpenNetworkDialog;
import org.cytoscape.cyndex2.internal.ui.swing.ShowDialogUtil;
import org.cytoscape.cyndex2.internal.util.Server;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

/**
 *
 * @author churas
 */
public class OpenNetworkFromNDExTaskFactoryImplTests {
	
	@Test
	public void testIsReady(){
		OpenNetworkFromNDExTaskFactoryImpl fac = new OpenNetworkFromNDExTaskFactoryImpl(null, null, null);
		assertTrue(fac.isReady());
	}
	
	@Test
	public void testCreateTaskIteratorCreateGUIReturnedFalse(){
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		when(mockRegistrar.getService(CySwingApplication.class)).thenReturn(mockSwing);
		
		OpenNetworkDialog mockOpenDialog = mock(OpenNetworkDialog.class);
		when(mockOpenDialog.createGUI()).thenReturn(Boolean.FALSE);
		OpenNetworkFromNDExTaskFactoryImpl fac = new OpenNetworkFromNDExTaskFactoryImpl(mockRegistrar,
				mockOpenDialog, new ShowDialogUtil());
		
		TaskIterator ti = fac.createTaskIterator();
		assertNotNull(ti);
		assertEquals(1, ti.getNumTasks());
		assertTrue(ti.next() instanceof CanceledTask);
		verify(mockRegistrar).getService(CySwingApplication.class);
		verify(mockOpenDialog).createGUI();
	}
	
	@Test
	public void testCreateTaskIteratorSuccess() throws IOException, NdexException {
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		when(mockSwing.getJFrame()).thenReturn(null);
		when(mockRegistrar.getService(CySwingApplication.class)).thenReturn(mockSwing);
		
		Object[] options = {new JButton("Open"), new JButton("cancel")};
		
		OpenNetworkDialog mockOpenDialog = mock(OpenNetworkDialog.class);
		when(mockOpenDialog.createGUI()).thenReturn(Boolean.TRUE);
		when(mockOpenDialog.getMainOpenButton()).thenReturn((JButton)options[0]);
		when(mockOpenDialog.getMainCancelButton()).thenReturn((JButton)options[1]);
		
		NetworkSummary selectedNetwork = new NetworkSummary();
		selectedNetwork.setExternalId(UUID.randomUUID());
		when(mockOpenDialog.getNDExSelectedNetwork()).thenReturn(selectedNetwork);
		
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(null, mockOpenDialog, "Open", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(0);
		
		ServerManager mockServerManager = mock(ServerManager.class);
		Server mockServer = mock(Server.class);
		when(mockServerManager.getServer()).thenReturn(mockServer);
		NdexRestClientModelAccessLayer mockMal = mock(NdexRestClientModelAccessLayer.class);
		when(mockMal.getNetworkSummaryById(selectedNetwork.getExternalId(), null)).thenReturn(selectedNetwork);
		when(mockServer.getModelAccessLayer()).thenReturn(mockMal);
		ServerManager.INSTANCE = mockServerManager;
		
		
		OpenNetworkFromNDExTaskFactoryImpl fac = new OpenNetworkFromNDExTaskFactoryImpl(mockRegistrar,
				mockOpenDialog, mockDialogUtil);
		
		TaskIterator ti = fac.createTaskIterator();
		assertNotNull(ti);
		assertEquals(1, ti.getNumTasks());
		assertTrue(ti.next() instanceof NetworkImportTask);
		
	}
	
	@Test
	public void testCreateTaskIteratorGetMalThrowsException() throws IOException, NdexException {
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		when(mockSwing.getJFrame()).thenReturn(null);
		when(mockRegistrar.getService(CySwingApplication.class)).thenReturn(mockSwing);
		
		Object[] options = {new JButton("Open"), new JButton("cancel")};
		
		OpenNetworkDialog mockOpenDialog = mock(OpenNetworkDialog.class);
		when(mockOpenDialog.createGUI()).thenReturn(Boolean.TRUE);
		when(mockOpenDialog.getMainOpenButton()).thenReturn((JButton)options[0]);
		when(mockOpenDialog.getMainCancelButton()).thenReturn((JButton)options[1]);
		
		NetworkSummary selectedNetwork = new NetworkSummary();
		selectedNetwork.setExternalId(UUID.randomUUID());
		when(mockOpenDialog.getNDExSelectedNetwork()).thenReturn(selectedNetwork);
		
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(null, mockOpenDialog, "Open", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(0);
		
		ServerManager mockServerManager = mock(ServerManager.class);
		Server mockServer = mock(Server.class);
		when(mockServerManager.getServer()).thenReturn(mockServer);
		NdexRestClientModelAccessLayer mockMal = mock(NdexRestClientModelAccessLayer.class);
		when(mockMal.getNetworkSummaryById(selectedNetwork.getExternalId(), null)).thenThrow(new NdexException("error"));
		when(mockServer.getModelAccessLayer()).thenReturn(mockMal);
		ServerManager.INSTANCE = mockServerManager;
		
		
		OpenNetworkFromNDExTaskFactoryImpl fac = new OpenNetworkFromNDExTaskFactoryImpl(mockRegistrar,
				mockOpenDialog, mockDialogUtil);
		
		TaskIterator ti = fac.createTaskIterator();
		assertNotNull(ti);
		assertEquals(1, ti.getNumTasks());
		assertTrue(ti.next() instanceof CanceledTask);
		
	}
	
	@Test
	public void testCreateTaskIteratorNetworkSummaryIsNull() throws IOException, NdexException {
		CyServiceRegistrar mockRegistrar = mock(CyServiceRegistrar.class);
		CySwingApplication mockSwing = mock(CySwingApplication.class);
		when(mockSwing.getJFrame()).thenReturn(null);
		when(mockRegistrar.getService(CySwingApplication.class)).thenReturn(mockSwing);
		
		Object[] options = {new JButton("Open"), new JButton("cancel")};
		
		OpenNetworkDialog mockOpenDialog = mock(OpenNetworkDialog.class);
		when(mockOpenDialog.createGUI()).thenReturn(Boolean.TRUE);
		when(mockOpenDialog.getMainOpenButton()).thenReturn((JButton)options[0]);
		when(mockOpenDialog.getMainCancelButton()).thenReturn((JButton)options[1]);
		

		when(mockOpenDialog.getNDExSelectedNetwork()).thenReturn(null);
		
		ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
		when(mockDialogUtil.showOptionDialog(null, mockOpenDialog, "Open", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0])).thenReturn(0);
		
		OpenNetworkFromNDExTaskFactoryImpl fac = new OpenNetworkFromNDExTaskFactoryImpl(mockRegistrar,
				mockOpenDialog, mockDialogUtil);
		
		TaskIterator ti = fac.createTaskIterator();
		assertNotNull(ti);
		assertEquals(1, ti.getNumTasks());
		assertTrue(ti.next() instanceof CanceledTask);
		
	}
}
