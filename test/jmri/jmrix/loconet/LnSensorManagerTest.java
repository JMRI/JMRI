// LnSensorManagerTest.java

package jmri.jmrix.loconet;

import jmri.Sensor;
import jmri.SensorManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.LnSensorManagerTurnout class.
 * @author	Bob Jacobsen  Copyright 2001
 * @version     $Revision: 1.6 $
 */
public class LnSensorManagerTest extends TestCase  {

    public void testLnSensorCreate() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        // create and register the manager object
        LnSensorManager l = new LnSensorManager();
        jmri.InstanceManager.setSensorManager(l);

    }

    public void testByAddress() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        // create and register the manager object
        LnSensorManager l = new LnSensorManager();

        // sample turnout object
        Sensor t = l.newSensor("LS22", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("LS22"));
    }

    public void testMisses() {
        // prepare an interface
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
		// create and register the manager object
		LnSensorManager l = new LnSensorManager();

		// sample turnout object
		Sensor t = l.newSensor("LS22", "test");

		// try to get nonexistant turnouts
		Assert.assertTrue(null == l.getByUserName("foo"));
		Assert.assertTrue(null == l.getBySystemName("bar"));
	}

	public void testLocoNetMessages() {
		// prepare an interface, register
		LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();

		// create and register the manager object
		LnSensorManager l = new LnSensorManager();

		// send messages for 21, 22
		// notify the Ln that somebody else changed it...
		LocoNetMessage m1 = new LocoNetMessage(4);
		m1.setOpCode(0xb2);         // OPC_INPUT_REP
		m1.setElement(1, 0x15);     // all but lowest bit of address
		m1.setElement(2, 0x60);     // Aux (low addr bit high), sensor high
		m1.setElement(3, 0x38);
		lnis.sendTestMessage(m1);

		// see if sensor exists
		Assert.assertTrue(null != l.getBySystemName("LS44"));
	}

	public void testAsAbstractFactory () {
		// create and register the manager object
		LnSensorManager l = new LnSensorManager();
		jmri.InstanceManager.setSensorManager(l);

		// ask for a Sensor, and check type
		SensorManager t = jmri.InstanceManager.sensorManagerInstance();

		Sensor o = t.newSensor("LS21", "my name");


		if (log.isDebugEnabled()) log.debug("received sensor value "+o);
		Assert.assertTrue( null != (LnSensor)o);

		// make sure loaded into tables
		if (log.isDebugEnabled()) log.debug("by system name: "+t.getBySystemName("LS21"));
		if (log.isDebugEnabled()) log.debug("by user name:   "+t.getByUserName("my name"));

		Assert.assertTrue(null != t.getBySystemName("LS21"));
		Assert.assertTrue(null != t.getByUserName("my name"));

	}


	// from here down is testing infrastructure

	public LnSensorManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LnSensorManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LnSensorManagerTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorManagerTest.class.getName());

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
