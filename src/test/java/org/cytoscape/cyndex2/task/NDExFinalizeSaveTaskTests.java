package org.cytoscape.cyndex2.task;

import org.cytoscape.cyndex2.internal.task.NDExFinalizeSaveTask;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 *
 * @author churas
 */
public class NDExFinalizeSaveTaskTests {
	
	@Test
	public void testCancel(){
		NDExFinalizeSaveTask task = new NDExFinalizeSaveTask(0l);
		task.cancel();
	}

	@Test
	public void testRunAlreadyCanceled() throws Exception {
		NDExFinalizeSaveTask task = new NDExFinalizeSaveTask(0l);
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		task.cancel();
		task.run(mockMonitor);
		verify(mockMonitor).setTitle("Save network to NDEx");
		verifyNoMoreInteractions(mockMonitor);
	}
	
	@Test
	public void testRunDelayBelowUpdateInterval() throws Exception {
		NDExFinalizeSaveTask task = new NDExFinalizeSaveTask(NDExFinalizeSaveTask.UPDATE_INTERVAL-1l);
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		
		task.run(mockMonitor);
		verify(mockMonitor).setTitle("Save network to NDEx");
		verifyNoMoreInteractions(mockMonitor);
	}
	
	@Test
	public void testRunDelayOneLoop() throws Exception {
		NDExFinalizeSaveTask task = new NDExFinalizeSaveTask(NDExFinalizeSaveTask.UPDATE_INTERVAL+1l);
		TaskMonitor mockMonitor = mock(TaskMonitor.class);
		
		task.run(mockMonitor);
		verify(mockMonitor).setTitle("Save network to NDEx");
		verify(mockMonitor).setStatusMessage("Saving network to NDEx");
		verify(mockMonitor, times(2)).setProgress(0.0f);
		verify(mockMonitor).setProgress(0.99502486f);
		verify(mockMonitor).setProgress(1.0f);
		verify(mockMonitor).setStatusMessage("Save to NDEx complete");
		
		verifyNoMoreInteractions(mockMonitor);
	}

}
