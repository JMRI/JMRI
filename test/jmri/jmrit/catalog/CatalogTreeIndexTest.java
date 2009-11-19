// CatalogTreeIndexTest.java

package jmri.jmrit.catalog;

import jmri.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the CatalogTreeIndex class
 * @author	Bob Jacobsen  Copyright (C) 2009
 * @version $Revision: 1.1 $
 */
public class CatalogTreeIndexTest extends TestCase {


    // class carries its own implementation of the
    // get/set parameter code, so we test that here

	public void testSetParameter() {
	    NamedBean n = new CatalogTreeIndex("sys", "usr"){
	        public int getState() {return 0;}
	        public void setState(int i) {}
	    };
	}

	public void testGetParameter() {
	    NamedBean n = new CatalogTreeIndex("sys", "usr"){
	        public int getState() {return 0;}
	        public void setState(int i) {}
	    };
	    
	    n.setParameter("foo", "bar");
	    Assert.assertEquals("bar", n.getParameter("foo"));
	}

	public void testGetSetNull() {
	    NamedBean n = new CatalogTreeIndex("sys", "usr"){
	        public int getState() {return 0;}
	        public void setState(int i) {}
	    };
	    
	    n.setParameter("foo", "bar");
	    Assert.assertEquals("bar", n.getParameter("foo"));
	    n.setParameter("foo", null);
	    Assert.assertEquals(null, n.getParameter("foo"));
	}

	// from here down is testing infrastructure

	public CatalogTreeIndexTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {CatalogTreeIndexTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(CatalogTreeIndexTest.class);
		return suite;
	}

}
