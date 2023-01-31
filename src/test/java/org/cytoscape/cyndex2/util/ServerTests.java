package org.cytoscape.cyndex2.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cytoscape.cyndex2.internal.util.Server;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NdexStatus;
import org.ndexbio.rest.client.NdexRestClientModelAccessLayer;

public class ServerTests {
	@Test
	public void serverFields() {
		Server server = new Server();
		server.setUrl("aaaa");
		server.setUserId(new UUID(1l,2l));
		server.setUsername("cccc");
		server.setPassword("dddd");
		
		assertEquals("aaaa", server.getUrl());
		assertEquals(new UUID(1l, 2l), server.getUserId());
		assertEquals("cccc", server.getUsername());
		assertEquals("dddd", server.getPassword());
		assertEquals("cccc@aaaa", server.toString());
		
	}
	
	@Test
	public void testSetWhiteSpaceUserAndPass(){
		Server server = new Server();
		server.setPassword(" ");
		server.setUsername("");
		assertNull(server.getUsername());
		assertNull(server.getPassword());
		
	}
	
	@Test
	public void testCopyConstructor(){
		Server s = new Server();
		s.setUsername("user");
		s.setPassword("pass");
		s.setUrl("url");
		s.setUserId(new UUID(1l, 2l));
		
		Server sCopy = new Server(s);
		assertEquals("url", sCopy.getUrl());
		assertEquals(new UUID(1l, 2l), sCopy.getUserId());
		assertEquals("user", sCopy.getUsername());
		assertEquals("pass", sCopy.getPassword());
	}
	
	@Test
	public void testIsRunningNdexServerValidStatusReturned() throws Exception {
		NdexRestClientModelAccessLayer mockMal = mock(NdexRestClientModelAccessLayer.class);
		NdexStatus nStatus = new NdexStatus();
		when(mockMal.getServerStatus()).thenReturn(nStatus);
		Server s = new Server();
		assertTrue(s.isRunningNdexServer(mockMal));
		verify(mockMal).getServerStatus();
	}
	
	@Test
	public void testIsRunningNdexServerNullReturned() throws Exception {
		NdexRestClientModelAccessLayer mockMal = mock(NdexRestClientModelAccessLayer.class);
		when(mockMal.getServerStatus()).thenReturn(null);
		Server s = new Server();
		assertFalse(s.isRunningNdexServer(mockMal));
		verify(mockMal).getServerStatus();
	}
	
	@Test
	public void testIsRunningNdexServerExceptionThrown() throws Exception {
		NdexRestClientModelAccessLayer mockMal = mock(NdexRestClientModelAccessLayer.class);
		when(mockMal.getServerStatus()).thenThrow(new NdexException("error"));
		Server s = new Server();
		assertFalse(s.isRunningNdexServer(mockMal));
		verify(mockMal).getServerStatus();
	}
	
	@Test
	public void checkEmptyServerEquality() {
		Server a = new Server();
		Server b = new Server();
		
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		
		// incorrect type
		assertFalse(a.equals("foo"));
	}
	
	@Test 
	public void checkEmptyUsernameEquality() {
		Server a = new Server();
		a.setUrl("aaaa");
		
		Server b = new Server();
		b.setUrl("bbbb");
		
		Server c = new Server();
		c.setUrl("aaaa");
		
		assertFalse(a.equals(b));
		assertTrue(a.equals(c));
	}
	
	@Test 
	public void checkNonNullUsernameEquality() {
		Server a = new Server();
		a.setUrl("aaaa");
		a.setUsername("auser");
		
		Server b = new Server();
		b.setUrl("bbbb");
		b.setUsername("auser");
		
		Server c = new Server();
		c.setUrl("aaaa");
		c.setUsername("auser");
		
		Server d = new Server();
		d.setUrl("aaaa");
		d.setUsername("xuser");
		
		assertFalse(a.equals(b));
		assertFalse(a.equals(d));
		
		assertTrue(a.equals(c));
	}
	
	@Test
	public void testCheck() throws Exception {
		//this is a useless method, it always returns true
		// and the mal passed in is never used.
		NdexRestClientModelAccessLayer mockMal = mock(NdexRestClientModelAccessLayer.class);
		Server s = new Server();
		assertTrue(s.check(mockMal));
		assertTrue(s.check(null));
		s.setUsername("bob");
		assertTrue(s.check(mockMal));
		assertTrue(s.check(null));
		
		s.setPassword("somepass");
		assertTrue(s.check(mockMal));
		assertTrue(s.check(null));
		
		verifyZeroInteractions(mockMal);
		
		
	}
	
	
	
}



