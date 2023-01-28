package org.cytoscape.cyndex2.ui.swing;

import java.sql.Timestamp;
import java.util.ArrayList;
import org.cytoscape.cyndex2.internal.ui.swing.NetworkSummaryTableModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.model.object.network.VisibilityType;

/**
 *
 * @author churas
 */
public class NetworkSummaryTableModelTests {
	
	@Test
	public void testClearReplaceNetworkSummaries(){
		NetworkSummaryTableModel model = new NetworkSummaryTableModel(new ArrayList<>(), null, false);
		assertEquals(0, model.getNetworkSummaries().size());
		assertEquals(0, model.getRowCount());
		ArrayList<NetworkSummary> newList = new ArrayList<>();
		NetworkSummary ns = new NetworkSummary();
		newList.add(ns);
		model.replaceNetworkSummaries(newList);
		assertEquals(1, model.getNetworkSummaries().size());
		assertEquals(1, model.getRowCount());
		model.clearNetworkSummaries();
		assertEquals(0, model.getNetworkSummaries().size());
		assertEquals(0, model.getRowCount());
	}
	
	@Test
	public void testGetColumnCount(){
		NetworkSummaryTableModel model = new NetworkSummaryTableModel(new ArrayList<>(), null, false);
		assertEquals(7, model.getColumnCount());
		
		model = new NetworkSummaryTableModel(new ArrayList<>(), null, true);
		assertEquals(6, model.getColumnCount());
	}
	
	@Test
	public void testGetColumnClassHideImportFalse(){
		NetworkSummaryTableModel model = new NetworkSummaryTableModel(new ArrayList<>(), null, false);
		assertEquals(NetworkSummary.class, model.getColumnClass(0));
		assertEquals(String.class, model.getColumnClass(1));
		assertEquals(String.class, model.getColumnClass(2));
		assertEquals(VisibilityType.class, model.getColumnClass(3));
		assertEquals(Integer.class, model.getColumnClass(4));
		assertEquals(Integer.class, model.getColumnClass(5));
		assertEquals(Timestamp.class, model.getColumnClass(6));
		
		try {
			model.getColumnClass(7);
			fail("Expected Exception");
		} catch(IllegalArgumentException iae){
			assertTrue(iae.getMessage().contains("Column at index"));
		}
	}
	
	@Test
	public void testGetColumnClassHideImportTrue(){
		NetworkSummaryTableModel model = new NetworkSummaryTableModel(new ArrayList<>(), null, true);
		assertEquals(String.class, model.getColumnClass(0));
		assertEquals(String.class, model.getColumnClass(1));
		assertEquals(VisibilityType.class, model.getColumnClass(2));
		assertEquals(Integer.class, model.getColumnClass(3));
		assertEquals(Integer.class, model.getColumnClass(4));
		assertEquals(Timestamp.class, model.getColumnClass(5));
		
		try {
			model.getColumnClass(6);
			fail("Expected Exception");
		} catch(IllegalArgumentException iae){
			assertTrue(iae.getMessage().contains("Column at index"));
		}
	}
	
	@Test
	public void testGetValueAtNoNetworkSummaries(){
		NetworkSummaryTableModel model = new NetworkSummaryTableModel(new ArrayList<>(), null, true);
		try {
			model.getValueAt(0, 0);
			fail("Expected IndexOutOfBounds");
		} catch(IndexOutOfBoundsException ie){
			
		}
	}
	
	@Test
	public void testGetValueAtHideImportFalse(){
		ArrayList<NetworkSummary> newList = new ArrayList<>();
		NetworkSummary ns = new NetworkSummary();
		ns.setName("name");
		ns.setOwner("owner");
		ns.setVisibility(VisibilityType.PUBLIC);
		ns.setNodeCount(1);
		ns.setEdgeCount(2);
		ns.setModificationTime(new Timestamp(1));
		newList.add(ns);
		NetworkSummaryTableModel model = new NetworkSummaryTableModel(newList, null);
		
		assertEquals(ns, model.getValueAt(0, 0));
		assertEquals("name", model.getValueAt(0, 1));
		assertEquals("owner", model.getValueAt(0, 2));
		assertEquals(VisibilityType.PUBLIC, model.getValueAt(0, 3));
		assertEquals(1, model.getValueAt(0, 4));
		assertEquals(2, model.getValueAt(0, 5));
		assertEquals(ns.getModificationTime(), model.getValueAt(0, 6));
		
		try {
			model.getValueAt(0, 7);
			fail("Expected IllegalArgumentException");
		} catch(IllegalArgumentException iae){
			assertTrue(iae.getMessage().contains("Column at index"));
		}
	}
	
	@Test
	public void testGetValueAtHideImportTrue(){
		ArrayList<NetworkSummary> newList = new ArrayList<>();
		NetworkSummary ns = new NetworkSummary();
		ns.setName("name");
		ns.setOwner("owner");
		ns.setVisibility(VisibilityType.PUBLIC);
		ns.setNodeCount(1);
		ns.setEdgeCount(2);
		ns.setModificationTime(new Timestamp(1));
		newList.add(ns);
		NetworkSummaryTableModel model = new NetworkSummaryTableModel(newList, null, true);
		
		assertEquals("name", model.getValueAt(0, 0));
		assertEquals("owner", model.getValueAt(0, 1));
		assertEquals(VisibilityType.PUBLIC, model.getValueAt(0, 2));
		assertEquals(1, model.getValueAt(0, 3));
		assertEquals(2, model.getValueAt(0, 4));
		assertEquals(ns.getModificationTime(), model.getValueAt(0, 5));
		
		try {
			model.getValueAt(0, 6);
			fail("Expected IllegalArgumentException");
		} catch(IllegalArgumentException iae){
			assertTrue(iae.getMessage().contains("Column at index"));
		}
	}
	
}
