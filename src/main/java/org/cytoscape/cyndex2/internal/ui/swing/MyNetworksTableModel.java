package org.cytoscape.cyndex2.internal.ui.swing;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.ndexbio.model.object.network.NetworkSummary;

/**
 * 
 * @author churas
 */
public class MyNetworksTableModel extends AbstractTableModel {

	public static final int NAME_COL = 0;
	public static final int MODIFIED_COL = 1;
	
	public static final String NAME_COL_LABEL = "name";
	public static final String MODIFIED_COL_LABEL = "modified";

	protected List<NetworkSummary> networkSummaries;
	
	public MyNetworksTableModel(List<NetworkSummary> networkSummaries) {
		this.networkSummaries = new ArrayList<NetworkSummary>(networkSummaries);
	}
	
	public void replaceNetworkSummaries(List<NetworkSummary> networkSummaries){
		this.networkSummaries = new ArrayList<>(networkSummaries);
		this.fireTableDataChanged();
	}
	
	public void clearNetworkSummaries(){
		this.networkSummaries.clear();
		this.fireTableDataChanged();
	}
	
	public List<NetworkSummary> getNetworkSummaries(){
		return networkSummaries;
	}
	
	public List<NetworkSummary> getNetworksMatchingName(final String name){
		List<NetworkSummary> matchingNetworks = new ArrayList<>();
		
		for (NetworkSummary ns: networkSummaries){
			if (ns.getName() == null && name == null){
				matchingNetworks.add(ns);
				continue;
			}
			if (ns.getName() == null && name != null){
				continue;
			}
			if (ns.getName() != null && name == null){
				continue;
			}
			if (ns.getName().equals(name)){
				matchingNetworks.add(ns);
			}
		}
		return matchingNetworks;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return networkSummaries.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case NAME_COL:
			return String.class;
		case MODIFIED_COL:
			return Timestamp.class;
		default:
			throw new IllegalArgumentException("Column at index " + columnIndex + " does not exist.");
		}
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		final NetworkSummary networkSummary = networkSummaries.get(arg0);
		switch (arg1) {
		case NAME_COL:
			return networkSummary.getName();
		case MODIFIED_COL:
			return networkSummary.getModificationTime();
		default:
			throw new IllegalArgumentException("Column at index " + arg1 + " does not exist.");
		}
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case NAME_COL:
			return NAME_COL_LABEL;
		case MODIFIED_COL:
			return MODIFIED_COL_LABEL;
		default:
			throw new IllegalArgumentException("Column at index " + columnIndex + " does not exist.");
		}
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
