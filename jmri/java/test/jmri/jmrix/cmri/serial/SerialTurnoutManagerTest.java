// SerialTurnoutManagerTest.java

package jmri.jmrix.cmri.serial;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;

import jmri.*;

/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	    tests for the jmri.jmrix.cmri.SerialTurnoutManager class
 * @author			Bob Jacobsen
 * @version  $Revision$
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest  {

	public void setUp() {
	    // replace the SerialTrafficController
	    SerialTrafficController t = new SerialTrafficController() {
	        SerialTrafficController test() {
	            setInstance();
	            return this;
	        }
	    }.test();
		t.registerNode(new SerialNode());
		// create and register the turnout manager object
		l = new SerialTurnoutManager() {
			public void notifyTurnoutCreationError(String conflict,int bitNum) {}
		};	
		jmri.InstanceManager.setTurnoutManager(l);
	}

	public String getSystemName(int n) {
		return "CT"+n;
	}

	public void testAsAbstractFactory () {
		// ask for a Turnout, and check type
		Turnout o = l.newTurnout("CT21", "my name");


		if (log.isDebugEnabled()) log.debug("received turnout value "+o);
		assertTrue( null != (SerialTurnout)o);

		// make sure loaded into tables
		if (log.isDebugEnabled()) log.debug("by system name: "+l.getBySystemName("CT21"));
		if (log.isDebugEnabled()) log.debug("by user name:   "+l.getByUserName("my name"));

		assertTrue(null != l.getBySystemName("CT21"));
		assertTrue(null != l.getByUserName("my name"));

	}


	// from here down is testing infrastructure

	public SerialTurnoutManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", SerialTurnoutManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite(SerialTurnoutManagerTest.class);
		return suite;
	}

	 static Logger log = Logger.getLogger(SerialTurnoutManagerTest.class.getName());

}
