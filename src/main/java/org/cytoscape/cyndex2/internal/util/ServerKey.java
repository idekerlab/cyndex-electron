package org.cytoscape.cyndex2.internal.util;

public class ServerKey {
		
		public String username;
		public String url;
	
		public ServerKey() {}
		
		public ServerKey(String username, String url) {
			this.username = username;
			this.url = url;
		}
		
		public ServerKey(Server server) {
			this.username = server.getUsername();
			this.url = server.getUrl();
		}
		
		/**
		 * Hashcode of username and url concatenated by @ symbol
		 * unless one is null in which case the hashcode of the other
		 * is used. If both are null, 0 is returned
		 * 
		 * @return Hashcode or 0 if both username and url are null
		 */
		public int hashCode() {
			if (username == null && url == null){
				return 0;
			}
			if (username != null && url == null){
				return username.hashCode();
			}
			if (username == null){
				return url.hashCode();
			}
			return username.concat("@").concat(url).hashCode();
		}
		
		/**
		 * Considered equal if of same type and username and url match where
		 * if both are null is considered a match. When comparing the username and
		 * url the .equals() is used for string comparison.
		 * 
		 * @param object what to compare
		 * @return true if both usernames are equal or null and if both urls are null
		 *         or equal. otherwise false
		 */
		public boolean equals(Object object) {
			if (!(object instanceof ServerKey)) { return false; }
			
			ServerKey serverKey = (ServerKey) object;
			if (serverKey.username == null && this.username == null) {
				if (serverKey.url == null && this.url == null){
					return true;
				}
				if (serverKey.url == null || this.url == null){
					return false;
				}
				return serverKey.url.equals(this.url);
			} else if (serverKey.username == null || this.username == null) {
				return false;
			} else if (serverKey.url == null && this.url == null){
				if (serverKey.username == null || this.username == null){
					return false;
				}
				return serverKey.username.equals(this.username);
			} else {
				
				return this.username.equals(serverKey.username) && this.url.equals(serverKey.url);
			}
		}
}
