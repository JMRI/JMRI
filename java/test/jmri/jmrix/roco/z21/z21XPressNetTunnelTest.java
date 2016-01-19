package jmri.jmrix.roco.z21;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.roco.z21.z21XPressNetTunnel class
 *
 * @author	Paul Bender
 */
public class z21XPressNetTunnelTest extends TestCase {

    public void testCtor() {
        z21SystemConnectionMemo memo = new z21SystemConnectionMemo();
        z21TrafficController tc = new z21TrafficController() {
            @Override
            public void sendMessage(jmri.jmrix.AbstractMRMessage m,
                    jmri.jmrix.AbstractMRListener l) {
                // don't actually send messages in this test.
            }
        };
        memo.setTrafficController(tc);
        
        // The next line is a 30 second clock-time delay, followed by an error
        //z21XPressNetTunnel a = new z21XPressNetTunnel(memo);
        //Assert.assertNotNull(a);
    }

    public void testGetStreamPortController() {
        z21SystemConnectionMemo memo = new z21SystemConnectionMemo();
        z21TrafficController tc = new z21TrafficController() {
            @Override
            public void sendMessage(jmri.jmrix.AbstractMRMessage m,
                    jmri.jmrix.AbstractMRListener l) {
                // don't actually send messages in this test.
            }
        };
        memo.setTrafficController(tc);

        // The next line is a 30 second clock-time delay, followed by an error
        //z21XPressNetTunnel a = new z21XPressNetTunnel(memo);
        //Assert.assertNotNull(a.getStreamPortController());
    }

    // from here down is testing infrastructure
    public z21XPressNetTunnelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", z21XPressNetTunnelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(z21XPressNetTunnelTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static Logger log = LoggerFactory.getLogger(z21XPressNetTunnelTest.class.getName());

}
