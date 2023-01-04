package org.cytoscape.cyndex2.internal.util;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.cytoscape.cyndex2.internal.CyServiceModule;
import org.cytoscape.cyndex2.internal.errors.CheckPermissionException;
import org.cytoscape.cyndex2.internal.errors.NetworkNotFoundInNDExException;
import org.cytoscape.cyndex2.internal.errors.ReadOnlyException;
import org.cytoscape.cyndex2.internal.errors.ReadPermissionException;
import org.cytoscape.cyndex2.internal.errors.RemoteModificationException;
import org.cytoscape.cyndex2.internal.errors.WritePermissionException;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

public class UpdateUtil {

	private static CyNetwork getRootNetwork(final Long suid, final CyNetworkManager networkManager,
			final CyRootNetworkManager rootNetworkManager) {
		Optional<CyRootNetwork> optional = networkManager.getNetworkSet().stream()
				.map(net -> rootNetworkManager.getRootNetwork(net)).findFirst();
		return optional.isPresent() ? optional.get() : null;
	}

	public static CyNetwork getNetworkForSUID(final Long suid, final boolean isCollection) {
		final CyNetworkManager network_manager = CyServiceModule.getService(CyNetworkManager.class);
		final CyRootNetworkManager root_network_manager = CyServiceModule.getService(CyRootNetworkManager.class);
		return isCollection ? getRootNetwork(suid, network_manager, root_network_manager)
				: network_manager.getNetwork(suid);

	}

	public static UUID updateIsPossible(CyNetwork network, UUID uuid, final NdexRestClient nc,
			final NdexRestClientModelAccessLayer mal) throws Exception {
		return updateIsPossible(network, uuid, nc, mal, true);
	}

	public static UUID updateIsPossible(CyNetwork network, UUID uuid, final NdexRestClient nc,
			final NdexRestClientModelAccessLayer mal, final boolean checkTimestamp) throws Exception {

		if (uuid == null) {
			throw new NetworkNotFoundInNDExException("UUID unknown. Can't find current Network in NDEx.");
		}
		NetworkSummary ns = null;
		
		try {

			Map<String, Permissions> permissionTable = mal.getUserNetworkPermission(nc.getUserUid(), uuid, false);
			if (permissionTable == null || permissionTable.isEmpty()){
				// see if network even exists
				try {
					ns = mal.getNetworkSummaryById(uuid);
					if (ns != null){
						throw new WritePermissionException("You don't have permission to write to this network.");
	
					} else {
						throw new NetworkNotFoundInNDExException("Network does not exist.");
					}
				} catch(IOException | NdexException e) {
					throw new CheckPermissionException("An error occurred while checking permissions. " + e.getMessage());
				}
			} else if (permissionTable.get(uuid.toString()) == Permissions.READ) {
				throw new WritePermissionException("You don't have permission to write to this network.");
			}

		} catch (IOException | NdexException e) {
			throw new ReadPermissionException("Unable to read network permissions. " + e.getMessage());
		}

		
		try {
			ns = mal.getNetworkSummaryById(uuid);

			if (ns.getIsReadOnly())
				throw new ReadOnlyException("The network is read only.");

			if (checkTimestamp) {

				final Timestamp serverTimestamp = ns.getModificationTime();
				final Timestamp localTimestamp = NDExNetworkManager.getModificationTimeStamp(network);

				if (localTimestamp == null) {
					throw new Exception("Session file is missing timestamp.");
				}

				final int timestampCompare = serverTimestamp.compareTo(localTimestamp);

				if (timestampCompare > 0) {
					throw new RemoteModificationException("Network was modified on remote server.", serverTimestamp);
				}
			}
		} catch (IOException | NdexException e) {
			throw new CheckPermissionException("An error occurred while checking permissions. " + e.getMessage());
		}

		return uuid;
	}

	public static UUID updateIsPossibleHelper(final Long suid, final boolean isCollection, final NdexRestClient nc,
			final NdexRestClientModelAccessLayer mal) throws Exception {
		return updateIsPossibleHelper(suid, isCollection, nc, mal, true);
	}
	
	public static UUID updateIsPossibleHelper(final Long suid, final boolean isCollection, final NdexRestClient nc,
			final NdexRestClientModelAccessLayer mal, final boolean checkTimestamp) throws Exception {

		final CyNetwork network = getNetworkForSUID(suid, isCollection);

		UUID ndexNetworkId = NDExNetworkManager.getUUID(network);

		return updateIsPossible(network, ndexNetworkId, nc, mal, checkTimestamp);
	}
}
