package org.cytoscape.cyndex2.task;

import java.util.ArrayList;
import javax.swing.RowFilter;
import org.cytoscape.cyndex2.internal.ui.StringMatchRowFilter;
import org.cytoscape.cyndex2.internal.ui.swing.MyNetworksTableModel;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.ndexbio.model.object.network.NetworkSummary;

/**
 *
 * @author churas
 */
public class StringMatchRowFilterTests {
	
	@Test
	public void testConstructor(){
		// nothing really to test
		StringMatchRowFilter f = new StringMatchRowFilter();
		assertNotNull(f);
	}
	@Test
	public void saveAsIsNull(){
		ArrayList<NetworkSummary> summaries = new ArrayList<>();
		NetworkSummary ns = new NetworkSummary();
		ns.setName("foo");
		summaries.add(ns);
		MyNetworksTableModel model = new MyNetworksTableModel(summaries);
		
		RowFilter<MyNetworksTableModel, Object> rf = StringMatchRowFilter.getStringMatchRowFilter(null);
		RowFilter.Entry<MyNetworksTableModel, Object> mockEntry = mock(RowFilter.Entry.class);
		when(mockEntry.getIdentifier()).thenReturn(new Integer(0));
		when(mockEntry.getModel()).thenReturn(model);
		assertFalse(rf.include(mockEntry));
	}
	
	@Test
	public void networkNameIsNullSaveAsIsEmpty(){
		ArrayList<NetworkSummary> summaries = new ArrayList<>();
		NetworkSummary ns = new NetworkSummary();
		ns.setName(null);
		summaries.add(ns);
		MyNetworksTableModel model = new MyNetworksTableModel(summaries);
		
		RowFilter<MyNetworksTableModel, Object> rf = StringMatchRowFilter.getStringMatchRowFilter(" ");
		RowFilter.Entry<MyNetworksTableModel, Object> mockEntry = mock(RowFilter.Entry.class);
		when(mockEntry.getIdentifier()).thenReturn(new Integer(0));
		when(mockEntry.getModel()).thenReturn(model);
		assertTrue(rf.include(mockEntry));
	}
	
	@Test
	public void networkNameIsNullSaveAsIsNull(){
		ArrayList<NetworkSummary> summaries = new ArrayList<>();
		NetworkSummary ns = new NetworkSummary();
		ns.setName(null);
		summaries.add(ns);
		MyNetworksTableModel model = new MyNetworksTableModel(summaries);
		
		RowFilter<MyNetworksTableModel, Object> rf = StringMatchRowFilter.getStringMatchRowFilter(null);
		RowFilter.Entry<MyNetworksTableModel, Object> mockEntry = mock(RowFilter.Entry.class);
		when(mockEntry.getIdentifier()).thenReturn(new Integer(0));
		when(mockEntry.getModel()).thenReturn(model);
		assertTrue(rf.include(mockEntry));
	}
	
	@Test
	public void rowMatches(){
		ArrayList<NetworkSummary> summaries = new ArrayList<>();
		NetworkSummary ns = new NetworkSummary();
		ns.setName("why hello there");
		summaries.add(ns);
		MyNetworksTableModel model = new MyNetworksTableModel(summaries);
		
		RowFilter<MyNetworksTableModel, Object> rf = StringMatchRowFilter.getStringMatchRowFilter("Hello");
		RowFilter.Entry<MyNetworksTableModel, Object> mockEntry = mock(RowFilter.Entry.class);
		when(mockEntry.getIdentifier()).thenReturn(new Integer(0));
		when(mockEntry.getModel()).thenReturn(model);
		assertTrue(rf.include(mockEntry));
	}
	
	@Test
	public void rowDoesNotMatch(){
		ArrayList<NetworkSummary> summaries = new ArrayList<>();
		NetworkSummary ns = new NetworkSummary();
		ns.setName("why hello there");
		summaries.add(ns);
		MyNetworksTableModel model = new MyNetworksTableModel(summaries);
		
		RowFilter<MyNetworksTableModel, Object> rf = StringMatchRowFilter.getStringMatchRowFilter("foo");
		RowFilter.Entry<MyNetworksTableModel, Object> mockEntry = mock(RowFilter.Entry.class);
		when(mockEntry.getIdentifier()).thenReturn(new Integer(0));
		when(mockEntry.getModel()).thenReturn(model);
		assertFalse(rf.include(mockEntry));
	}
	
}
