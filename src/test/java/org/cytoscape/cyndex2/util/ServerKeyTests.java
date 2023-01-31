package org.cytoscape.cyndex2.util;

import org.cytoscape.cyndex2.internal.util.ServerKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ServerKeyTests {
	
	
	@Test
	public void testHashCode(){
		// try with empty server key instance
		ServerKey s = new ServerKey();
		assertEquals(0, s.hashCode());
		
		s = new ServerKey("hi", null);
		assertEquals("hi".hashCode(), s.hashCode());
		
		s = new ServerKey(null, "bye");
		assertEquals("bye".hashCode(), s.hashCode());
		
		s = new ServerKey("hi", "bye");
		assertEquals("hi@bye".hashCode(), s.hashCode());
	}
	
	@Test
	public void testEquals(){
		ServerKey s = new ServerKey();
		
		// different object types
		assertFalse(s.equals("not even right object"));
		
		// null username and url, but should match
		assertTrue(s.equals(s));
		
		// either user or url is null comparisons
		ServerKey a = new ServerKey("user", null);
		ServerKey b = new ServerKey(null, "url");
		assertTrue(a.equals(a));
		assertTrue(b.equals(b));
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		
		// usernames are null
		a = new ServerKey(null, "url1");
		b = new ServerKey(null, null);
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		assertTrue(a.equals(a));
		
		// urls are null
		a = new ServerKey("user", null);
		b = new ServerKey("otheruser", null);
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		
		//null against only url null
		b = new ServerKey(null, null);
		assertFalse(b.equals(a));
		assertFalse(a.equals(b));
		
		
		// neigher is null
		a = new ServerKey("foo", "urla");
		b = new ServerKey("Foo", "urlb");
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		assertTrue(a.equals(a));
		assertTrue(b.equals(b));
		
		ServerKey c = new ServerKey("foo","urla");
		assertTrue(a.equals(c));
		assertTrue(c.equals(a));
		
		
		
	}
	
	
}



