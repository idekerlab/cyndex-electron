package org.cytoscape.cyndex2.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.cyndex2.internal.CyActivator;
import org.cytoscape.cyndex2.internal.CyServiceModule;
import org.cytoscape.cyndex2.internal.task.NetworkImportTask;
import org.cytoscape.cyndex2.internal.util.Server;
import org.cytoscape.cyndex2.internal.util.ServerManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.SynchronousTaskManager;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class TestUtil {
	private static Logger logger = LoggerFactory.getLogger("TestUtil");
	
	final SynchronousTaskManager<?> synchronousTaskManager = mock(SynchronousTaskManager.class);
	
	public static TestUtil INSTANCE;
	public static void init(boolean configDirExists) {
		INSTANCE = new TestUtil(configDirExists);
	}
	

	
	public TestUtil(boolean configDirExists) {
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyApplicationConfiguration config = mock(CyApplicationConfiguration.class);
		when(config.getAppConfigurationDirectoryLocation(CyActivator.class)).thenReturn(getResource(configDirExists ? "mockAppDir" : "nullAppDir"));
		when(reg.getService(CyApplicationConfiguration.class)).thenReturn(config);
	
		CyServiceModule.setServiceRegistrar(reg);
	}
	
	public void removeAllServers(){
		List<Server> list = ServerManager.INSTANCE.getAvailableServers().stream().collect(Collectors.toList());
		for (Server s : list){
			ServerManager.INSTANCE.getAvailableServers().delete(s);
		}
	}
	
	public static File getResource(String... dir) {
		File file = new File("src/test/resources/");
		for (String s : dir) {
			file = new File(file, s);
		}
		return file;
	}

	
}
