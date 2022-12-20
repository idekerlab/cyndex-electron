package org.cytoscape.cyndex2.util;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

import org.cytoscape.cyndex2.internal.CyServiceModule;
import org.cytoscape.cyndex2.internal.util.UpdateUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Test;
import org.mockito.Mockito;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

import java.util.Map;
import org.cytoscape.cyndex2.internal.errors.CheckPermissionException;

import org.cytoscape.cyndex2.internal.errors.NetworkNotFoundInNDExException;
import org.cytoscape.cyndex2.internal.errors.ReadOnlyException;
import org.cytoscape.cyndex2.internal.errors.ReadPermissionException;
import org.cytoscape.cyndex2.internal.errors.RemoteModificationException;
import org.cytoscape.cyndex2.internal.errors.WritePermissionException;
import static org.mockito.Matchers.eq;

public class UpdateUtilTests {
	
	
	@Test
	public void updateWhenUUIDIsNull() throws Exception {
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		try{
			UpdateUtil.updateIsPossible(network, null, nc, mal);
			fail("UpdateUtil did not throw expected exception");
		}
		catch(NetworkNotFoundInNDExException e){
			assertEquals("Exception message did not match", "UUID unknown. Can't find current Network in NDEx.",
					e.getMessage());
		}	
	}
	
	@Test
	public void updateWhenPermissionTableIsNull() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		when(mal.getUserNetworkPermission(any(UUID.class), any(UUID.class),
					Mockito.anyBoolean())).thenReturn(null);
		
		try {
			UpdateUtil.updateIsPossible(network, uuid, nc, mal); 
			fail("UpdateUtil did not throw expected exception");
		} catch(NetworkNotFoundInNDExException e){
			assertEquals("exception message mismatch", "Cannot find network.", e.getMessage());
		}
	}
	
	@Test
	public void updateWhenPermissionTableIsEmpty() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		
		when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		Map<String, Permissions> permissionTable = new HashMap<String, Permissions>();
		when(mal.getUserNetworkPermission(eq(user), eq(uuid),
					Mockito.anyBoolean())).thenReturn(permissionTable);
		
		try {
			UpdateUtil.updateIsPossible(network, uuid, nc, mal); 
			fail("UpdateUtil did not throw expected exception");
		} catch(NetworkNotFoundInNDExException e){
			assertEquals("exception message mismatch", "Cannot find network.", e.getMessage());
		}
	}
	
	@Test
	public void updateWhenPermissionTableReadOnly() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		
		when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		Map<String, Permissions> permissionTable = new HashMap<String, Permissions>();
		permissionTable.put(uuid.toString(), Permissions.READ);
		when(mal.getUserNetworkPermission(eq(user), eq(uuid),
					Mockito.anyBoolean())).thenReturn(permissionTable);
		
		try {
			UpdateUtil.updateIsPossible(network, uuid, nc, mal); 
			fail("UpdateUtil did not throw expected exception");
		} catch(WritePermissionException e){
			assertEquals("exception message mismatch", "You don't have permission to write to this network.", e.getMessage());
		}
	}
	
	@Test
	public void updateWhenNDExExceptionOnPermissionCheck() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		
		when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		
		when(mal.getUserNetworkPermission(eq(user), eq(uuid),
					Mockito.anyBoolean())).thenThrow(new IOException("error"));
		
		try {
			UpdateUtil.updateIsPossible(network, uuid, nc, mal); 
			fail("UpdateUtil did not throw expected exception");
		} catch(ReadPermissionException e){
			assertEquals("exception message mismatch", "Unable to read network permissions. error", e.getMessage());
		}
	}
	
	@Test
	public void updateWhenNetworkSummarySaysReadOnly() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		when(ns.getIsReadOnly()).thenReturn(true);
		when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		Map<String, Permissions> permissionTable = new HashMap<String, Permissions>();
		permissionTable.put(uuid.toString(), Permissions.WRITE);
		when(mal.getUserNetworkPermission(eq(user), eq(uuid),
					Mockito.anyBoolean())).thenReturn(permissionTable);
		
		try {
			UpdateUtil.updateIsPossible(network, uuid, nc, mal); 
			fail("UpdateUtil did not throw expected exception");
		} catch(ReadOnlyException e){
			assertEquals("exception message mismatch", "The network is read only.", e.getMessage());
		}
	}
	
	@Test
	public void updateWhenCheckTimestampFalse() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		when(ns.getIsReadOnly()).thenReturn(false);
		when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		Map<String, Permissions> permissionTable = new HashMap<String, Permissions>();
		permissionTable.put(uuid.toString(), Permissions.WRITE);
		when(mal.getUserNetworkPermission(eq(user), eq(uuid),
					Mockito.anyBoolean())).thenReturn(permissionTable);
		
		assertEquals(uuid, UpdateUtil.updateIsPossible(network, uuid, nc, mal, false)); 

	}
	
	@Test
	public void updateWhenLocalTimestampIsNull() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		CyRow networkRow = mock(CyRow.class);
		when(networkRow.get("NDEx UUID", String.class)).thenReturn((new UUID(1l,2l)).toString());
		//when(networkRow.get("NDEx Modification Timestamp", String.class)).thenReturn((new Timestamp(0)).toString());
		
		
		CyTable networkTable = mock(CyTable.class);
		when(network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS)).thenReturn(networkTable);
		when(networkTable.getRow(669l)).thenReturn(networkRow);

		CyNetworkManager nm = mock(CyNetworkManager.class);
		when(nm.getNetwork(669l)).thenReturn(network);
		when(reg.getService(CyNetworkManager.class)).thenReturn(nm);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		when(ns.getIsReadOnly()).thenReturn(false);
		when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		Map<String, Permissions> permissionTable = new HashMap<String, Permissions>();
		permissionTable.put(uuid.toString(), Permissions.WRITE);
		when(mal.getUserNetworkPermission(eq(user), eq(uuid),
					Mockito.anyBoolean())).thenReturn(permissionTable);
		
		try {
			UpdateUtil.updateIsPossible(network, uuid, nc, mal, true); 
			fail("Expected Exception");
		} catch(Exception e){
			assertEquals("mismatch exception", "Session file is missing timestamp.", e.getMessage());
		}

	}
	
	@Test
	public void updateWhenGetNetworkSummaryByIdRaisesIOException() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		CyRow networkRow = mock(CyRow.class);
		when(networkRow.get("NDEx UUID", String.class)).thenReturn((new UUID(1l,2l)).toString());
		//when(networkRow.get("NDEx Modification Timestamp", String.class)).thenReturn((new Timestamp(0)).toString());
		
		
		CyTable networkTable = mock(CyTable.class);
		when(network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS)).thenReturn(networkTable);
		when(networkTable.getRow(669l)).thenReturn(networkRow);

		CyNetworkManager nm = mock(CyNetworkManager.class);
		when(nm.getNetwork(669l)).thenReturn(network);
		when(reg.getService(CyNetworkManager.class)).thenReturn(nm);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		when(ns.getIsReadOnly()).thenReturn(false);
		when(mal.getNetworkSummaryById(uuid)).thenThrow(new IOException("error"));
		Map<String, Permissions> permissionTable = new HashMap<String, Permissions>();
		permissionTable.put(uuid.toString(), Permissions.WRITE);
		when(mal.getUserNetworkPermission(eq(user), eq(uuid),
					Mockito.anyBoolean())).thenReturn(permissionTable);
		
		try {
			UpdateUtil.updateIsPossible(network, uuid, nc, mal); 
			fail("Expected Exception");
		} catch(CheckPermissionException e){
			assertEquals("mismatch exception", "An error occurred while checking permissions. error", e.getMessage());
		}

	}
	
	@Test
	public void updateIsPosibleFourArgSuccess() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		CyRow networkRow = mock(CyRow.class);
		when(networkRow.get("NDEx UUID", String.class)).thenReturn((new UUID(1l,2l)).toString());
		when(networkRow.get("NDEx Modification Timestamp", String.class)).thenReturn((new Timestamp(0)).toString());
		
		
		CyTable networkTable = mock(CyTable.class);
		when(network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS)).thenReturn(networkTable);
		when(networkTable.getRow(669l)).thenReturn(networkRow);

		CyNetworkManager nm = mock(CyNetworkManager.class);
		when(nm.getNetwork(669l)).thenReturn(network);
		when(reg.getService(CyNetworkManager.class)).thenReturn(nm);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		when(ns.getIsReadOnly()).thenReturn(true);
		when(ns.getModificationTime()).thenReturn(new Timestamp(0));
		when(ns.getIsReadOnly()).thenReturn(false);
		when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		Map<String, Permissions> permissionTable = new HashMap<String, Permissions>();
		permissionTable.put(uuid.toString(), Permissions.WRITE);
		when(mal.getUserNetworkPermission(eq(user), eq(uuid),
					Mockito.anyBoolean())).thenReturn(permissionTable);
		
		assertEquals(uuid, UpdateUtil.updateIsPossible(network, uuid, nc, mal));
	}
	
	@Test (expected = RemoteModificationException.class)
	public void updateWhenSessionIsOutdated() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		
		CyRow networkRow = mock(CyRow.class);
		when(networkRow.get("NDEx UUID", String.class)).thenReturn((new UUID(1l,2l)).toString());
		when(networkRow.get("NDEx Modification Timestamp", String.class)).thenReturn((new Timestamp(0)).toString());
		
		
		CyTable networkTable = mock(CyTable.class);
		when(network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS)).thenReturn(networkTable);
		when(networkTable.getRow(669l)).thenReturn(networkRow);

		CyNetworkManager nm = mock(CyNetworkManager.class);
		when(nm.getNetwork(669l)).thenReturn(network);
		when(reg.getService(CyNetworkManager.class)).thenReturn(nm);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		Map<String, Permissions> permissionTable = new HashMap<String, Permissions>();
		permissionTable.put(user.toString(), Permissions.WRITE);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		
		when(ns.getModificationTime()).thenReturn(new Timestamp(1));
		when(ns.getIsReadOnly()).thenReturn(false);
		
		try {
			when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		} catch (IOException | NdexException e2) {
			e2.printStackTrace();
			fail();
		}
	
		
		try {
			when(mal.getUserNetworkPermission(any(UUID.class), any(UUID.class),
					Mockito.anyBoolean())).thenReturn(permissionTable);
		} catch (IOException | NdexException e1) {
			e1.printStackTrace();
		}
		
		UpdateUtil.updateIsPossibleHelper(669l, false, nc, mal);
		 
		fail("UpdateUtil did not throw expected exception");
	}
	
	@Test 
	public void updateWhenModifiable() throws Exception { 
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		UUID uuid = new UUID(1l,2l);
		UUID user = new UUID(1l,3l);
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(669l);
		
		CyRow networkRow = mock(CyRow.class);
		when(networkRow.get("NDEx UUID", String.class)).thenReturn((new UUID(1l,2l)).toString());
		when(networkRow.get("NDEx Modification Timestamp", String.class)).thenReturn((new Timestamp(0l)).toString());
		
		
		CyTable networkTable = mock(CyTable.class);
		when(network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS)).thenReturn(networkTable);
		when(networkTable.getRow(669l)).thenReturn(networkRow);

		CyNetworkManager nm = mock(CyNetworkManager.class);
		when(nm.getNetwork(669l)).thenReturn(network);
		when(reg.getService(CyNetworkManager.class)).thenReturn(nm);
		
		NdexRestClient nc = mock(NdexRestClient.class);
		when(nc.getUserUid()).thenReturn(user);
		NdexRestClientModelAccessLayer mal = mock(NdexRestClientModelAccessLayer.class);
		
		Map<String, Permissions> permissionTable = new HashMap<String, Permissions>();
		permissionTable.put(user.toString(), Permissions.WRITE);
		
		NetworkSummary ns = mock(NetworkSummary.class);
		
		when(ns.getModificationTime()).thenReturn(new Timestamp(0));
		when(ns.getIsReadOnly()).thenReturn(false);
		
		try {
			when(mal.getNetworkSummaryById(uuid)).thenReturn(ns);
		} catch (IOException | NdexException e2) {
			e2.printStackTrace();
			fail();
		}
	
		
		try {
			when(mal.getUserNetworkPermission(any(UUID.class), any(UUID.class),
					Mockito.anyBoolean())).thenReturn(permissionTable);
		} catch (IOException | NdexException e1) {
			e1.printStackTrace();
		}
		
		UpdateUtil.updateIsPossibleHelper(669l, false, nc, mal);
		 
		
	}
	
}



