package org.cytoscape.cyndex2.rest.impl;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ci.CIResponseFactory;
import org.cytoscape.cyndex2.internal.CxTaskFactoryManager;
import org.cytoscape.cyndex2.internal.CyServiceModule;
import org.cytoscape.cyndex2.internal.rest.NdexClient;
import org.cytoscape.cyndex2.internal.rest.SimpleNetworkSummary;
import org.cytoscape.cyndex2.internal.rest.endpoints.NdexNetworkResource.CINdexBaseResponse;
import org.cytoscape.cyndex2.internal.rest.endpoints.NdexNetworkResource.CISummaryResponse;
import org.cytoscape.cyndex2.internal.rest.endpoints.impl.NdexNetworkResourceImpl;
import org.cytoscape.cyndex2.internal.rest.response.SummaryResponse;
import org.cytoscape.cyndex2.internal.util.CIServiceManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.cyndex2.internal.rest.response.NdexBaseResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NdexNetworkResourceTest {
	
	protected NetworkTestSupport nts = new NetworkTestSupport();
	protected NetworkViewTestSupport nvts = new NetworkViewTestSupport();
	
	NdexClient client = mock(NdexClient.class);
	CyApplicationManager appManager = mock(CyApplicationManager.class);
	CyNetworkManager networkManager = mock(CyNetworkManager.class);
	CIServiceManager ciServiceManager = mock(CIServiceManager.class);

	CIResponseFactory ciResponseFactory;
	CySubNetwork currentSubNetwork;
	CyRow subNetworkRow;
	CyTable subNetworkTable;
	
	List<CyColumn> subNetworkTableColumns;
	CyColumn subMockColumn;
	
	CyRow hiddenSubNetworkRow;
	CyTable hiddenSubNetworkTable;
	
	CyRow localSubNetworkRow;
	CyTable localSubNetworkTable;
	
	CyRootNetwork currentRootNetwork;
	
	CyRow rootNetworkRow;
	CyTable rootNetworkTable;
	List<CyColumn> rootNetworkTableColumns = new ArrayList<CyColumn>();
	CyColumn cyRootMockColumn = mock(CyColumn.class);
	
	CyRow hiddenRootRow;
	CyTable hiddenRootTable;
		
	CyRow localRootRow;
	CyTable localRootTable;
	
	List<CySubNetwork> subNetworkList;
	
	
	@Before
	public void prepMocks() {
		client = mock(NdexClient.class);
		appManager = mock(CyApplicationManager.class);
		networkManager = mock(CyNetworkManager.class);
		ciServiceManager = mock(CIServiceManager.class);

		ciResponseFactory = mock(CIResponseFactory.class);
		
		//when(ciResponseFactory.getCIResponse(data, CISummaryResponse.class))
		try {
			doAnswer(new Answer<CISummaryResponse>() {
				public CISummaryResponse answer(InvocationOnMock invocation) {
					final CISummaryResponse output = new CISummaryResponse();
					output.data = (SummaryResponse) invocation.getArguments()[0];
					return output;
				}
			}).when(ciResponseFactory).getCIResponse(org.mockito.Matchers.any(), eq(CISummaryResponse.class));
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		try {
			doAnswer(new Answer<CINdexBaseResponse>() {
				public CINdexBaseResponse answer(InvocationOnMock invocation) {
					final CINdexBaseResponse output = new CINdexBaseResponse();
					output.data = (NdexBaseResponse) invocation.getArguments()[0];
					return output;
				}
			}).when(ciResponseFactory).getCIResponse(org.mockito.Matchers.any(), eq(CINdexBaseResponse.class));
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		when(ciServiceManager.getCIResponseFactory()).thenReturn(ciResponseFactory);
		
		currentSubNetwork = mock(CySubNetwork.class);
		when(currentSubNetwork.getSUID()).thenReturn(669l);
		
		subNetworkRow = mock(CyRow.class);
		when(subNetworkRow.get("mocksubkey", String.class)).thenReturn("mocksubvalue");
		subNetworkTable = mock(CyTable.class);
		
		subNetworkTableColumns = new ArrayList<CyColumn>();
		subMockColumn = mock(CyColumn.class);
		when(subMockColumn.getName()).thenReturn("mocksubkey");
		when(subMockColumn.getType()).thenReturn((Class)String.class);
		
		subNetworkTableColumns.add(subMockColumn);
		
		when(subNetworkTable.getColumns()).thenReturn(subNetworkTableColumns);
	
		when(currentSubNetwork.getDefaultNetworkTable()).thenReturn(subNetworkTable);
		when(currentSubNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS)).thenReturn(subNetworkTable);
		
		when(subNetworkTable.getRow(669l)).thenReturn(subNetworkRow);
		
		hiddenSubNetworkRow = mock(CyRow.class);
		when(hiddenSubNetworkRow.get("NDEx UUID", String.class)).thenReturn((new UUID(1l,2l)).toString());
		
		
		hiddenSubNetworkTable = mock(CyTable.class);
		when(hiddenSubNetworkTable.getRow(669l)).thenReturn(hiddenSubNetworkRow);
		when(currentSubNetwork.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS)).thenReturn(hiddenSubNetworkTable);
		
		localSubNetworkRow = mock(CyRow.class);
		localSubNetworkTable = mock(CyTable.class);
		
		when(localSubNetworkTable.getRow(669l)).thenReturn(localSubNetworkRow);
		when(localSubNetworkRow.get("name", String.class)).thenReturn("mock sub name");
		
		when(currentSubNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS)).thenReturn(localSubNetworkTable);
		
		currentRootNetwork = mock(CyRootNetwork.class);
		when(currentRootNetwork.getSUID()).thenReturn(668l);
		
		rootNetworkRow = mock(CyRow.class);
		when(rootNetworkRow.get("NDEx UUID", String.class)).thenReturn((new UUID(3l,4l)).toString());
		when(rootNetworkRow.get("mockrootkey", String.class)).thenReturn("mockrootvalue");
		
		rootNetworkTable = mock(CyTable.class);
		
		rootNetworkTableColumns = new ArrayList<CyColumn>();
		cyRootMockColumn = mock(CyColumn.class);
		when(cyRootMockColumn.getName()).thenReturn("mockrootkey");
		when(cyRootMockColumn.getType()).thenReturn((Class)String.class);
		
		rootNetworkTableColumns.add(cyRootMockColumn);
		
		when(rootNetworkTable.getColumns()).thenReturn(rootNetworkTableColumns);
		when(rootNetworkTable.getRow(668l)).thenReturn(rootNetworkRow);
		when(currentRootNetwork.getDefaultNetworkTable()).thenReturn(rootNetworkTable);
		when(currentRootNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS)).thenReturn(rootNetworkTable);
	
		hiddenRootRow = mock(CyRow.class);
		hiddenRootTable = mock(CyTable.class);
		
		when(hiddenRootTable.getRow(668l)).thenReturn(hiddenRootRow);
		when(hiddenRootRow.get("NDEx UUID", String.class)).thenReturn((new UUID(3l,4l)).toString());
		
		when(currentRootNetwork.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS)).thenReturn(hiddenRootTable);
		
		localRootRow = mock(CyRow.class);
		localRootTable = mock(CyTable.class);
		
		when(localRootTable.getRow(668l)).thenReturn(localRootRow);
		when(localRootRow.get("name", String.class)).thenReturn("mock root name");
		
		when(currentRootNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS)).thenReturn(localRootTable);
		
		subNetworkList = new ArrayList<CySubNetwork>();
		subNetworkList.add(currentSubNetwork);
	
		when(currentRootNetwork.getSubNetworkList()).thenReturn(subNetworkList);
		
		when(currentSubNetwork.getRootNetwork()).thenReturn(currentRootNetwork);
		when(currentSubNetwork.getSUID()).thenReturn(669l);
		when(appManager.getCurrentNetwork()).thenReturn(currentSubNetwork);
		
		when(networkManager.getNetwork(669l)).thenReturn(currentSubNetwork);
		//when(networkManager.getNetwork(668l)).thenReturn(currentRootNetwork);
		
		Set<CyNetwork> networks = new HashSet<CyNetwork>();
		networks.add(currentSubNetwork);
		networks.add(currentRootNetwork);
		when(networkManager.getNetworkSet()).thenReturn(networks);
		
	}
	
	@Test
	public void testGetCurrentNetworkSummary() {
		
		NdexNetworkResourceImpl impl = new NdexNetworkResourceImpl(client, appManager, networkManager, ciServiceManager);
		CISummaryResponse ciSummaryResponse = impl.getCurrentNetworkSummary();
		
		assertEquals(Long.valueOf(669l),ciSummaryResponse.data.currentNetworkSuid);
		assertEquals(new UUID(3l,4l).toString(),ciSummaryResponse.data.currentRootNetwork.uuid);
		assertEquals("mock root name",ciSummaryResponse.data.currentRootNetwork.name);
		assertEquals(Long.valueOf(668l),ciSummaryResponse.data.currentRootNetwork.suid);
		
		assertEquals(1, ciSummaryResponse.data.members.size());
		
	  assertEquals("mockrootvalue", ciSummaryResponse.data.currentRootNetwork.props.get("mockrootkey"));
		
		SimpleNetworkSummary subNetworkSummary = ciSummaryResponse.data.members.iterator().next();
		
		assertEquals(Long.valueOf(669l), subNetworkSummary.suid);
		assertEquals(new UUID(1l,2l).toString(), subNetworkSummary.uuid);
		assertEquals("mock sub name", subNetworkSummary.name);
		
		assertEquals("mocksubvalue", subNetworkSummary.props.get("mocksubkey"));
	}
	
	@Test
	public void testCreateNetworkFromCX() {
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		DialogTaskManager dtm = mock(DialogTaskManager.class);
		
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				final ExecutorService service = Executors.newSingleThreadExecutor();
				service.submit(()-> {
					Object[] args = invocation.getArguments();

					TaskIterator taskIterator = (TaskIterator) args[0];
					TaskObserver observer = (TaskObserver) args[1];
					// System.out.println("SyncTaskManager.execute");
					
			    Task task = null;
					try {
						while (taskIterator.hasNext()) {
							task = taskIterator.next();
					
							task.run(mock(TaskMonitor.class));

							if (task instanceof ObservableTask && observer != null) {
								observer.taskFinished((ObservableTask)task);
							} 
						}
			            if (observer != null) observer.allFinished(FinishStatus.getSucceeded());

					} catch (Exception exception) {
			            if (observer != null && task != null) observer.allFinished(FinishStatus.newFailed(task, exception));
					}
				});
				return null;
			}
		}).when(dtm).execute(any(TaskIterator.class), any(TaskObserver.class));
		
		when(reg.getService(DialogTaskManager.class)).thenReturn(dtm);
		CyServiceModule.setServiceRegistrar(reg);
		
		InputStreamTaskFactory inputStreamTaskFactory = mock(InputStreamTaskFactory.class);
		AbstractCyNetworkReader readerTask = mock(AbstractCyNetworkReader.class); 
		CyNetwork cyNetwork = mock(CyNetwork.class);
		CyNetwork[] cyNetworks = {cyNetwork};
		when(readerTask.getNetworks()).thenReturn(cyNetworks);
		
		TaskIterator taskIterator = new TaskIterator();
		taskIterator.append(readerTask);
	
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("id", "cytoscapeCxNetworkReaderFactory");
		
		when(inputStreamTaskFactory.createTaskIterator(any(InputStream.class), anyObject())).thenReturn(taskIterator);
	
		CxTaskFactoryManager.INSTANCE.addReaderFactory(inputStreamTaskFactory, properties);
		
		NdexNetworkResourceImpl impl = new NdexNetworkResourceImpl(client, appManager, networkManager, ciServiceManager);
		InputStream inputStream = mock(InputStream.class);
		
		CINdexBaseResponse response = impl.createNetworkFromCx(inputStream);
		
		verify(inputStreamTaskFactory).createTaskIterator(inputStream, null);
		
		verify(networkManager).addNetwork(cyNetwork);
		verify(readerTask).buildCyNetworkView(cyNetwork);
		
	}
	
	@Test
	public void testGetNetworkSummary() {
		
		NdexNetworkResourceImpl impl = new NdexNetworkResourceImpl(client, appManager, networkManager, ciServiceManager);
		CISummaryResponse ciSummaryResponse = impl.getNetworkSummary(669l);
		
		assertEquals(Long.valueOf(669l),ciSummaryResponse.data.currentNetworkSuid);
		assertEquals(new UUID(3l,4l).toString(),ciSummaryResponse.data.currentRootNetwork.uuid);
		assertEquals("mock root name",ciSummaryResponse.data.currentRootNetwork.name);
		assertEquals(Long.valueOf(668l),ciSummaryResponse.data.currentRootNetwork.suid);
		
		assertEquals(1, ciSummaryResponse.data.members.size());
		
	  assertEquals("mockrootvalue", ciSummaryResponse.data.currentRootNetwork.props.get("mockrootkey"));
		
		SimpleNetworkSummary subNetworkSummary = ciSummaryResponse.data.members.iterator().next();
		
		assertEquals(Long.valueOf(669l), subNetworkSummary.suid);
		assertEquals(new UUID(1l,2l).toString(), subNetworkSummary.uuid);
		assertEquals("mock sub name", subNetworkSummary.name);
		
		assertEquals("mocksubvalue", subNetworkSummary.props.get("mocksubkey"));
	}
	
	@Test
	public void testGetRootNetworkSummary() {
		
		NdexNetworkResourceImpl impl = new NdexNetworkResourceImpl(client, appManager, networkManager, ciServiceManager);
		CISummaryResponse ciSummaryResponse = impl.getNetworkSummary(668l);
		
		assertEquals(null,ciSummaryResponse.data.currentNetworkSuid);
		assertEquals(new UUID(3l,4l).toString(),ciSummaryResponse.data.currentRootNetwork.uuid);
		assertEquals("mock root name",ciSummaryResponse.data.currentRootNetwork.name);
		assertEquals(Long.valueOf(668l),ciSummaryResponse.data.currentRootNetwork.suid);
		
		assertEquals(1, ciSummaryResponse.data.members.size());
		
	  assertEquals("mockrootvalue", ciSummaryResponse.data.currentRootNetwork.props.get("mockrootkey"));
		
		SimpleNetworkSummary subNetworkSummary = ciSummaryResponse.data.members.iterator().next();
		
		assertEquals(Long.valueOf(669l), subNetworkSummary.suid);
		assertEquals(new UUID(1l,2l).toString(), subNetworkSummary.uuid);
		assertEquals("mock sub name", subNetworkSummary.name);
		
		assertEquals("mocksubvalue", subNetworkSummary.props.get("mocksubkey"));
	}
}



