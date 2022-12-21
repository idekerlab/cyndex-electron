/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cytoscape.cyndex2.internal.ui;

import javax.swing.RowFilter;
import org.cytoscape.cyndex2.internal.ui.swing.MyNetworksTableModel;

/**
 *
 * @author churas
 */
public class StringMatchRowFilter {
	
	public static RowFilter<MyNetworksTableModel, Object> getStringMatchRowFilter(final String saveAsText){
		return new RowFilter<MyNetworksTableModel, Object>(){
			@Override
			public boolean include(Entry<? extends MyNetworksTableModel, ? extends Object> entry){
				int rowID = (Integer)entry.getIdentifier();
				MyNetworksTableModel model = entry.getModel();
				String networkName = (String)model.getValueAt(rowID, 0);
				if (networkName == null && (saveAsText == null || saveAsText.trim().isEmpty())){
					return true;
				}
				if (networkName != null && networkName.contains(saveAsText)){
					return true;
				}
				return false;
			}
		};
	}
}
