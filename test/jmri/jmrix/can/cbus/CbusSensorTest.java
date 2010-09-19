// CbusSensorTest.java

package jmri.jmrix.can.cbus;

import jmri.Sensor;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusSensor class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version     $Revision: 1.7 $
 */
public class CbusSensorTest extends TestCase {

    public void testIncomingChange() {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();
        Assert.assertNotNull("exists", t );
        CbusSensor s = new CbusSensor("MS+1;-1");
        
        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                    new int[]{CbusConstants.CBUS_ACON,0x00,0x00,0x00,0x01}
        );
        CanMessage mInactive = new CanMessage(
                    new int[]{CbusConstants.CBUS_ACOF,0x00,0x00,0x00,0x01}
        );

        // check states
        Assert.assertTrue(s.getKnownState()==Sensor.UNKNOWN);
        
        s.message(mActive);
        Assert.assertTrue(s.getKnownState()==Sensor.ACTIVE);
        
        s.message(mInactive);
        Assert.assertTrue(s.getKnownState()==Sensor.INACTIVE);
        
    }

    public void testLocalChange() throws jmri.JmriException {
        // load dummy TrafficController
        TestTrafficController t = new TestTrafficController();
        
        CbusSensor s = new CbusSensor("MS+1;-1");
        t.rcvMessage = null;
        s.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(s.getKnownState()==Sensor.ACTIVE);
        Assert.assertTrue(new CbusAddress("+1").match(t.rcvMessage));
        
        t.rcvMessage = null;
        s.setKnownState(Sensor.INACTIVE);
        Assert.assertTrue(s.getKnownState()==Sensor.INACTIVE);
        Assert.assertTrue(new CbusAddress("-1").match(t.rcvMessage));
    }
    
    // from here down is testing infrastructure

    public CbusSensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
    	String[] testCaseName = {CbusSensorTest.class.getName()};
    	junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CbusSensorTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusSensorTest.class.getName());
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
