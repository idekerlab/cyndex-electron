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

package org.cytoscape.cyndex2.internal.util;

import java.io.IOException;
import java.util.UUID;

import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.rest.client.NdexRestClient;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

/**
 *
 * @author David Welker
 * @author David Otasek
 */
public class Server
{
    public enum Type {DEFAULT, CREDENTIALS, ADDED}
    
    private String name;  // unique in the ServerList
    private String url;
    private String username;
    private String password;

    private Type type;
    private UUID userId;
    
    private boolean authenticated;
    
    /**
     * Default constructor,
     */
    public Server()
    {
    }
    
    /**
     * Copy constructor/
     * @param s The server to be copied.
     */
    public Server(Server s)
    {
        name = s.name;
        url = s.url;
        username = s.username;
        password = s.password;
        type = s.type;
      
        authenticated = s.authenticated;
        userId = s.getUserId();
    }

    public boolean isRunningNdexServer(NdexRestClientModelAccessLayer mal)
    {
    		try {
					final NdexStatus ndexStatus = mal.getServerStatus();
					return ndexStatus != null;
				} catch (IOException | NdexException e) {
					e.printStackTrace();
					return false;
				}
    	}
    
    public boolean check(NdexRestClientModelAccessLayer mal) throws IOException
    {
        boolean usernamePresent = username != null && !username.isEmpty();
        boolean passwordPresent = password != null && !username.isEmpty();
        if( !usernamePresent && !passwordPresent )
        {
            authenticated = false;
            return true;
        }
        else
        {
            return true;
        }
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o instanceof Server) {
    		final Server b = (Server) o;
    		final boolean sameUrl = this.url == null ? b.url == null : this.url.equals(b.url);
    		final boolean sameUser = this.username == null ? b.username == null : this.username.equals(b.username);
    		return sameUrl && sameUser; 
    	}
    	return false;
    }
    
    public boolean hasSameName(Server s)
    {
        return name.equals(s.name);
    }
    
    public void useCredentialsOf(Server s)
    {
        username = s.username;
        password = s.password;
      
    }
    
 /*   public String show()
    {
        String result = "";
        result += "Name: " + name +"\n";
        result += "URL: " + url +"\n";
        result += "Username: " + username +"\n";
        result += "Password: " + password +"\n";
        result += "Type: " + type +"\n";
        result += "Authenticated: " + authenticated +"\n";
        result += "UUID: " + userId +"\n";
        return result;
    } */
    
    public NdexRestClientModelAccessLayer getModelAccessLayer()
    {
    	
        NdexRestClient client;
				try {
					client = new NdexRestClient(username,password,url);
				  return new NdexRestClientModelAccessLayer(client);
				} catch (IOException | NdexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
      
    }
   
 /*   public NdexRestClientModelAccessLayer getModelAccessLayer(String userName, String password)
    {
        NdexRestClient client = new NdexRestClient(username,password,url);
        return new NdexRestClientModelAccessLayer(client);
    } */

    public boolean isDefault()
    {
        return type == Type.DEFAULT;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
    
    public String display()
    {
        return name + " ("+url+")";
    }
       
    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    //Getters
    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public Type getType()
    {
        return type;
    }
    
    //Setters
    public void setName(String name)
    {
        this.name = name;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setUsername(String username)
    {
        if( username != null && username.trim().equals("") )
            this.username = null;
        else
            this.username = username;
    }

    public void setPassword(String password)
    {
        if( password != null && password.trim().equals("") )
            this.password = null;
        else
            this.password = password;
    }

    public void setType(Type type)
    {
        this.type = type;
    }
    
    public boolean isAuthenticated() 
    {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) 
    {
        this.authenticated = authenticated;
    }
    // </editor-fold>

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	
	
	// give each server an unique ID 
	/* static final AtomicLong NEXT_ID = new AtomicLong(0);
	 final long id = NEXT_ID.getAndIncrement();

	 public long getId() {
	         return id;
	 } */
    
}
