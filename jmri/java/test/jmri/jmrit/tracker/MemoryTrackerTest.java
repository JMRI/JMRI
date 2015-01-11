// MemoryTrackerTest.java

package jmri.jmrit.tracker;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.Block;
import jmri.MemoryManager;
import jmri.InstanceManager;

/**
 * Tests for the MemoryTracker class
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version $Revision$
 */
public class MemoryTrackerTest extends TestCase {

	public void testDirectCreate() {
	    MemoryManager m = InstanceManager.memoryManagerInstance();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
	    m.provideMemory("dummy");
	    // check for exception in ctor
        new MemoryTracker(new Block("dummy"),"");
	}

    
	// from here down is testing infrastructure

	public MemoryTrackerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {MemoryTrackerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(MemoryTrackerTest.class);
		return suite;
	}

}
