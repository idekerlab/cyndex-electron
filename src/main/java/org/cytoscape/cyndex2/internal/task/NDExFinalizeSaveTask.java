/*
 * Copyright (c) 2014, the Cytoscape Consortium and the Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.cytoscape.cyndex2.internal.task;


import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


public class NDExFinalizeSaveTask extends AbstractTask {

	private long _delayTimeMillis;
	
	/**
	 * How often in milliseconds to update the progress bar
	 */
	private static final long UPDATE_INTERVAL = 200;
	
	/**
	 * 
	 * @param delayTimeMillis time in milliseconds to take to run this task
	 */
	public NDExFinalizeSaveTask(long delayTimeMillis)
			 {
		super();
		_delayTimeMillis = delayTimeMillis;
	}

	@Override
	public void cancel() {
		super.cancel();
	}
	
	/**
	 * This is a dummy task whose only purpose is to stick around long enough
	 * so Cytoscape will display the progress bar to let the user know we are
	 * saving the network to NDEx
	 * @param taskMonitor 
	 */
	@Override
	public void run(TaskMonitor taskMonitor) {
		
		taskMonitor.setTitle("Save network to NDEx");

		if (cancelled) {
			return;
		}
		
		if (_delayTimeMillis <= UPDATE_INTERVAL){
			return;
		}
		
		taskMonitor.setProgress(0.0);
		taskMonitor.setStatusMessage("Saving network to NDEx");
		float progressIncrementVal = 1.0f/((float)_delayTimeMillis/(float)UPDATE_INTERVAL);
		float progress = progressIncrementVal;
		for (long progressLoop = 0; progressLoop <= _delayTimeMillis ; progressLoop+=UPDATE_INTERVAL){
			try {
				Thread.sleep(UPDATE_INTERVAL);
			} catch(InterruptedException ie){
				
			}
			taskMonitor.setProgress(progress);
			progress += progressIncrementVal;
			if (cancelled) {
				return;
			}
		}
		taskMonitor.setStatusMessage("Save to NDEx complete");
		taskMonitor.setProgress(1.0f);
	}
}
