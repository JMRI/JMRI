package jmri.jmrix.dcc;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.implementation.AbstractTurnoutTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.dcc.DccTurnout class
 *
 * @author	Bob Jacobsen
 */
public class DccTurnoutTest extends AbstractTurnoutTestBase {

    CommandStationScaffold tcis;

    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tcis = new CommandStationScaffold();
        InstanceManager.setDefault(CommandStation.class, tcis);
        t = new DccTurnout(4);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "[-127, -2, 127]", java.util.Arrays.toString(tcis.outbound.get(tcis.outbound.size() - 1)));  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "[-127, -1, 126]", java.util.Arrays.toString(tcis.outbound.get(tcis.outbound.size() - 1)));  // CLOSED message
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
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
