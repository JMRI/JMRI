/**
 * NceTurnoutManagerTest.java
 *
 * Description:	    tests for the jmri.jmrix.nce.NceTurnoutManager class
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix.nce;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.*;

public class NceTurnoutManagerTest extends jmri.AbstractTurnoutMgrTest  {

	public void setUp() {
		// create and register the manager object
		l = new NceTurnoutManager();
		jmri.InstanceManager.setTurnoutManager(l);
		assertTrue(l == jmri.InstanceManager.turnoutManagerInstance());

	}

	public String getSystemName(int n) {
		return "NT"+n;
	}

	public void testAsAbstractFactory () {
		// ask for a Turnout, and check type
		Turnout o = l.newTurnout("NT21", "my name");


		if (log.isDebugEnabled()) log.debug("received turnout value "+o);
		assertTrue( null != (NceTurnout)o);

		// make sure loaded into tables
		if (log.isDebugEnabled()) log.debug("by system name: "+l.getBySystemName("NT21"));
		if (log.isDebugEnabled()) log.debug("by user name:   "+l.getByUserName("my name"));

		assertTrue(null != l.getBySystemName("NT21"));
		assertTrue(null != l.getByUserName("my name"));

	}


	// from here down is testing infrastructure

	public NceTurnoutManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NceTurnoutManager.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite(NceTurnoutManagerTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutManagerTest.class.getName());

}
