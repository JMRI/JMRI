/**
 * DccTurnoutTest.java
 *
 * Description:	tests for the jmri.jmrix.dcc.DccTurnout class
 *
 * @author	Bob Jacobsen
 * @version
 */
package jmri.jmrix.dcc;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.implementation.AbstractTurnoutTest;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class DccTurnoutTest extends AbstractTurnoutTest {

    CommandStationScaffold tcis;

    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tcis = new CommandStationScaffold();
        InstanceManager.setDefault(CommandStation.class, tcis);
        t = new DccTurnout(4);
    }

    public int numListeners() {
        return tcis.numListeners();
    }

    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "[-127, -2, 127]", java.util.Arrays.toString(tcis.outbound.get(tcis.outbound.size() - 1)));  // THROWN message
    }

    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "[-127, -1, 126]", java.util.Arrays.toString(tcis.outbound.get(tcis.outbound.size() - 1)));  // CLOSED message
    }

    // from here down is testing infrastructure
    public DccTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DccTurnoutTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DccTurnoutTest.class);
        return suite;
    }

    class CommandStationScaffold implements CommandStation {

        java.util.ArrayList<byte[]> outbound = new java.util.ArrayList<byte[]>();

        public void sendPacket(byte[] packet, int repeats) {
            outbound.add(packet);
        }

        public String getUserName() {
            return "";
        }

        public String getSystemPrefix() {
            return "";
        }

        public int numListeners() {
            return 0;
        }
    }

    // The minimal setup for log4J
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
