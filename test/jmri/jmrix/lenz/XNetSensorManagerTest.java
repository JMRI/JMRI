// XNetSensorManagerTest.java

package jmri.jmrix.lenz;

import jmri.Sensor;
import jmri.SensorManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.XNetSensorManager class.
 * @author	Paul Bender Copyright (c) 2003
 * @version     $Revision: 2.1 $
 */
public class XNetSensorManagerTest extends TestCase  {

    public void testXNetSensorCreate() {
        // prepare an interface
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        XNetSensorManager l = new XNetSensorManager();
        jmri.InstanceManager.setSensorManager(l);

    }

    public void testByAddress() {
        // prepare an interface
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        XNetSensorManager l = new XNetSensorManager();

        // sample sensor object
        Sensor t = l.newSensor("XS22", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("XS22"));
    }

    public void testMisses() {
        // prepare an interface
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());
		// create and register the manager object
		XNetSensorManager l = new XNetSensorManager();

		// sample turnout object
		Sensor t = l.newSensor("XS22", "test");

		// try to get nonexistant turnouts
		Assert.assertTrue(null == l.getByUserName("foo"));
		Assert.assertTrue(null == l.getBySystemName("bar"));
	}

	public void testXNetMessages() {
		// prepare an interface, register
		XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());

		// create and register the manager object
		XNetSensorManager l = new XNetSensorManager();

		// send messages for feedbak encoder 22
		// notify the XPressNet that somebody else changed it...
		XNetReply m1 = new XNetReply();
		m1.setElement(0, 0x42);     // Opcode for feedback response
		m1.setElement(1, 0x02);     // The feedback encoder address
		m1.setElement(2, 0x51);     // A bit pattern telling which 
                                            // bits of the upper nibble 
                                            // are on in the message.
		m1.setElement(3, 0x11);     // The XOR of everything above
		xnis.sendTestMessage(m1);

		// see if sensor exists
		Assert.assertTrue(null != l.getBySystemName("XS22"));
	}

	public void testAsAbstractFactory () {
		// create and register the manager object
		XNetSensorManager l = new XNetSensorManager();
		jmri.InstanceManager.setSensorManager(l);

		// ask for a Sensor, and check type
		SensorManager t = jmri.InstanceManager.sensorManagerInstance();

		Sensor o = t.newSensor("XS21", "my name");


		if (log.isDebugEnabled()) log.debug("received sensor value "+o);
		Assert.assertTrue( null != (XNetSensor)o);

		// make sure loaded into tables
		if (log.isDebugEnabled()) log.debug("by system name: "+t.getBySystemName("XS21"));
		if (log.isDebugEnabled()) log.debug("by user name:   "+t.getByUserName("my name"));

		Assert.assertTrue(null != t.getBySystemName("XS21"));
		Assert.assertTrue(null != t.getByUserName("my name"));

	}


	// from here down is testing infrastructure

	public XNetSensorManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {XNetSensorManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XNetSensorManagerTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetSensorManagerTest.class.getName());

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
