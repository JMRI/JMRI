package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21XPressNetTunnel class
 *
 * @author Paul Bender
 */
public class Z21XPressNetTunnelTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;
    private Z21XPressNetTunnel tunnel = null;

    @Test
    public void testCtor() {
        assertNotNull(tunnel);
    }

    @Test
    public void testGetStreamPortController() {
        assertNotNull(tunnel.getStreamPortController());
    }

    private jmri.jmrix.lenz.XNetTrafficController packets;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();

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

    @AfterEach
    public void tearDown() {
        if ( packets != null ) {
            packets.terminateThreads();
        }
        packets = null;
        tunnel.dispose();
        tunnel = null;
        tc.terminateThreads();
        tc = null;
        memo.getTrafficController().terminateThreads();
        memo = null;
        JUnitUtil.tearDown();
    }

}
