package org.cytoscape.cyndex2.ui.swing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.cytoscape.cyndex2.internal.ui.swing.MyNetworksTableModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.ndexbio.model.object.network.NetworkSummary;

/**
 *
 * @author churas
 */
public class MyNetworksTableModelTests {
	
	@Test
	public void testClearReplaceNetworkSummaries(){
		MyNetworksTableModel model = new MyNetworksTableModel(new ArrayList<>());
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
	public void testGetNetworksMatchingNameNoNetworks(){
		MyNetworksTableModel model = new MyNetworksTableModel(new ArrayList<>());
		assertTrue(model.getNetworksMatchingName(null).isEmpty());
		assertTrue(model.getNetworksMatchingName("foo").isEmpty());
		
	}
	
	@Test
	public void testGetNetworksMatchingNameOneMatch(){
		ArrayList<NetworkSummary> netSummaries = new ArrayList<>();
		NetworkSummary nsone = new NetworkSummary();
		nsone.setName("Foo");
		netSummaries.add(nsone);
		
		NetworkSummary nstwo = new NetworkSummary();
		nstwo.setName(null);
		netSummaries.add(nstwo);
		
		NetworkSummary nsthree = new NetworkSummary();
		nsthree.setName("blah");
		netSummaries.add(nsthree);
		
		MyNetworksTableModel model = new MyNetworksTableModel(netSummaries);
		List<NetworkSummary> res = model.getNetworksMatchingName(null);
		assertEquals(1, res.size());
		assertEquals(null, res.get(0).getName());
		
		assertTrue(model.getNetworksMatchingName("foo").isEmpty());
		
		res = model.getNetworksMatchingName("Foo");
		assertEquals(1, res.size());
		assertEquals("Foo", res.get(0).getName());

		
	}

	
	@Test
	public void testGetColumnCount(){
		MyNetworksTableModel model = new MyNetworksTableModel(new ArrayList<>());
		assertEquals(2, model.getColumnCount());
	}
	
	@Test
	public void testGetColumnClass(){
		MyNetworksTableModel model = new MyNetworksTableModel(new ArrayList<>());
		assertEquals(String.class, model.getColumnClass(0));
		assertEquals(Timestamp.class, model.getColumnClass(1));
		
		try {
			model.getColumnClass(2);
			fail("Expected Exception");
		} catch(IllegalArgumentException iae){
			assertTrue(iae.getMessage().contains("Column at index"));
		}
	}
	
	@Test
	public void testGetValueAtNoNetworkSummaries(){
		MyNetworksTableModel model = new MyNetworksTableModel(new ArrayList<>());
		try {
			model.getValueAt(0, 0);
			fail("Expected IndexOutOfBounds");
		} catch(IndexOutOfBoundsException ie){
			
		}
	}
	
	
	
	@Test
	public void testGetValueAt(){
		ArrayList<NetworkSummary> newList = new ArrayList<>();
		NetworkSummary ns = new NetworkSummary();
		ns.setName("name");
		ns.setOwner("owner");
		ns.setModificationTime(new Timestamp(1));
		newList.add(ns);
		MyNetworksTableModel model = new MyNetworksTableModel(newList);
		
		assertEquals("name", model.getValueAt(0, 0));
		assertEquals(ns.getModificationTime(), model.getValueAt(0, 1));
		
		try {
			model.getValueAt(0, 2);
			fail("Expected IllegalArgumentException");
		} catch(IllegalArgumentException iae){
			assertTrue(iae.getMessage().contains("Column at index"));
		}
	}
	
	@Test
	public void testGetColumnName(){
		ArrayList<NetworkSummary> newList = new ArrayList<>();
		
		MyNetworksTableModel model = new MyNetworksTableModel(newList);
		assertEquals("name", model.getColumnName(0));
		assertEquals("modified", model.getColumnName(1));
		try {
			model.getColumnName(2);
			fail("Expected IllegalArgumentException");
		} catch(IllegalArgumentException iae){
			assertTrue(iae.getMessage().contains("Column at index"));
		}
	}
	
	@Test
	public void testIsCellEditable(){
		ArrayList<NetworkSummary> newList = new ArrayList<>();
		
		MyNetworksTableModel model = new MyNetworksTableModel(newList);
		assertFalse(model.isCellEditable(0, 0));
		assertFalse(model.isCellEditable(1, 5));
		
	}
}
