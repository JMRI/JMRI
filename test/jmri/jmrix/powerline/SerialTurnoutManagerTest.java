// SerialTurnoutManagerTest.java

package jmri.jmrix.powerline;

import junit.framework.Test;
import junit.framework.TestSuite;

import jmri.*;

/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	    tests for the SerialTurnoutManager class
 * @author			Bob Jacobsen Copyright 2004, 2008
 * @version  $Revision: 1.7 $
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest  {

	public void setUp() {
	    apps.tests.Log4JFixture.setUp();
	    
	    // replace the SerialTrafficController
	    SerialTrafficController t = new jmri.jmrix.powerline.cm11.SpecificTrafficController() {
	        SerialTrafficController test() {
	            setInstance();
	            return this;
	        }
	    }.test();
	    t.status();	// well it uses t
//		t.registerSerialNode(new SerialNode(0, SerialNode.DAUGHTER));
		// create and register the manager object
		l = new SerialTurnoutManager();
		jmri.InstanceManager.setTurnoutManager(l);
	}

	public String getSystemName(int n) {
		return "PT"+n;
	}

	public void testAsAbstractFactory () {
		// ask for a Turnout, and check type
		Turnout o = l.newTurnout("PT21", "my name");


		if (log.isDebugEnabled()) log.debug("received turnout value "+o);
		assertTrue( null != (SerialTurnout)o);

		// make sure loaded into tables
		if (log.isDebugEnabled()) log.debug("by system name: "+l.getBySystemName("PT21"));
		if (log.isDebugEnabled()) log.debug("by user name:   "+l.getByUserName("my name"));

		assertTrue(null != l.getBySystemName("PT21"));
		assertTrue(null != l.getByUserName("my name"));

	}


	// from here down is testing infrastructure

	public SerialTurnoutManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SerialTurnoutManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite(SerialTurnoutManagerTest.class);
		return suite;
	}
    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTurnoutManagerTest.class.getName());

}
