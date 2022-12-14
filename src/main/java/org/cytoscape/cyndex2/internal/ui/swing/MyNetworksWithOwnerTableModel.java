package org.cytoscape.cyndex2.internal.ui.swing;


import java.sql.Timestamp;
import java.util.List;
import org.ndexbio.model.object.network.NetworkSummary;

/**
 * 
 * @author churas
 */
public class MyNetworksWithOwnerTableModel extends MyNetworksTableModel {

	public static final int NAME_COL = 0;
	public static final int OWNER_COL = 1;
	public static final int MODIFIED_COL = 2;
	
	public static final String OWNER_COL_LABEL = "owner";
	
	public MyNetworksWithOwnerTableModel(List<NetworkSummary> networkSummaries) {
		super(networkSummaries);
	}

	@Override
	public int getColumnCount() {
		return 3;
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
		case OWNER_COL:
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
		case OWNER_COL:
			return networkSummary.getOwner();
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
		case OWNER_COL:
			return OWNER_COL_LABEL;
		case MODIFIED_COL:
			return MODIFIED_COL_LABEL;
		default:
			throw new IllegalArgumentException("Column at index " + columnIndex + " does not exist.");
		}
	}
}
