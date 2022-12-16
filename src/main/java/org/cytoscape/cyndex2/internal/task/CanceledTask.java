
package org.cytoscape.cyndex2.internal.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author churas
 */
public class CanceledTask extends AbstractTask {

	@Override
	public void cancel() {
		super.cancel(); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		return;
	}
}
