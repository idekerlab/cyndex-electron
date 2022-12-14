/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cytoscape.cyndex2.internal.ui.swing;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Locale;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders time in short format which matches style in NDEx web app, but 
 * does adjust for locale of JVM
 * @author churas
 */
public class NDExTimestampRenderer  extends DefaultTableCellRenderer {
	DateFormat formatter;

	public NDExTimestampRenderer() {
		super();
	}

	/**
	 *  TODO modify to match NDEx: 12/13/22 6:42 AM
	 * @param value 
	 */
	public void setValue(Object value) {
		if (formatter == null) {
			formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
		}
		setText((value == null && value instanceof Timestamp) ? "" : formatter.format(((Timestamp) value)));
	}
}
