// SerialSensorManagerTest.java

package jmri.jmrix.grapevine;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the SerialSensorManager class.
 * @author	Bob Jacobsen  Copyright 2003, 2007, 2008
 * @version	$Revision: 1.3 $
 */
public class SerialSensorManagerTest extends TestCase {

    public void testSensorCreationAndRegistration() {
	    // replace the SerialTrafficController to get clean reset
	    SerialTrafficController t = new SerialTrafficController() {
	        SerialTrafficController test() {
	            setInstance();
	            return this;
	        }
	    }.test();

        // construct nodes
        SerialNode n1 = new SerialNode(1,SerialNode.NODE2002V6);
        SerialNode n2 = new SerialNode(2,SerialNode.NODE2002V6);
        SerialNode n3 = new SerialNode(3,SerialNode.NODE2002V1);
        
        SerialSensorManager s = new SerialSensorManager();
        Assert.assertTrue("none expected A1", !(n1.sensorsActive()) );
        Assert.assertTrue("none expected A2", !(n2.sensorsActive()) );
        Assert.assertTrue("none expected A3", !(n3.sensorsActive()) );
        s.provideSensor("1003");
        Assert.assertTrue("UA 1", n1.sensorsActive() );
        Assert.assertTrue("2nd none expected A2", !(n2.sensorsActive()) );
        Assert.assertTrue("2nd none expected A3", !(n3.sensorsActive()) );
        s.provideSensor("1011");
        s.provideSensor("1008");
        s.provideSensor("1009");
        s.provideSensor("1011");
        s.provideSensor("GS2006");
        Assert.assertTrue("2nd UA 1", n1.sensorsActive() );
        Assert.assertTrue("2nd UA 2", n2.sensorsActive() );
        Assert.assertTrue("2nd none expected UA 3", !(n3.sensorsActive()) );
        s.provideSensor("1010");
        s.provideSensor("3001");
        Assert.assertTrue("3rd UA 1", n1.sensorsActive() );
        Assert.assertTrue("3rd UA 2", n2.sensorsActive() );
        Assert.assertTrue("3nd UA 3", n3.sensorsActive() );
        s.provideSensor("1007");
        s.provideSensor("2007");
        s.provideSensor("3007");
        Assert.assertTrue("4th UA 1", n1.sensorsActive() );
        Assert.assertTrue("4th UA 2", n2.sensorsActive() );
        Assert.assertTrue("4th UA 3", n3.sensorsActive() );
    }

    // from here down is testing infrastructure
    public SerialSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialSensorManagerTest.class);
        return suite;
    }

    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
        // replace the SensorManager
        jmri.InstanceManager i = new jmri.InstanceManager(){
            protected void init() {
                super.init();
                root = this;
            }
        };
    }
    // The minimal setup for log4J
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
