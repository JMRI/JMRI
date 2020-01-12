package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.roco.z21.z21XPressNetTunnel class
 *
 * @author	Paul Bender
 */
public class Z21XPressNetTunnelTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;
    private Z21XPressNetTunnel tunnel = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(tunnel);
    }

    @Test
    public void testGetStreamPortController() {
        Assert.assertNotNull(tunnel.getStreamPortController());
    }

    jmri.jmrix.lenz.XNetTrafficController packets;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initConfigureManager();

        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold() {
            @Override
            protected void terminate() {
            }
        };
        memo.setTrafficController(tc);

        tunnel = new Z21XPressNetTunnel(memo) {
            @Override
            void setStreamPortController(jmri.jmrix.lenz.XNetStreamPortController x) {
                xsc = new Z21XNetStreamPortController(x.getInputStream(), x.getOutputStream(), x.getCurrentPortName()) {
                    @Override
                    public void configure() {
                        // connect to a packetizing traffic controller
                        packets = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
                        packets.connectPort(this);
                        this.getSystemConnectionMemo().setXNetTrafficController(packets);
                    }
                };
            }
        };
    }

    @After
    public void tearDown() {
        packets.terminateThreads();
        tunnel.dispose();
        tunnel = null;
        tc.terminateThreads();
        tc = null;
        memo.getTrafficController().terminateThreads();
        memo = null;
        JUnitUtil.tearDown();
    }

}
