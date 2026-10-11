package jmri.jmrix.roco.z21;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.z21LocoNetTunnel class
 *
 * @author Paul Bender
 */
public class Z21LocoNetTunnelTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;
    private Z21LocoNetTunnel tunnel = null;

    @Test
    public void testCtor() {
        assertNotNull(tunnel);
    }

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

        tunnel = new Z21LocoNetTunnel(memo);
    }

    @AfterEach
    public void tearDown() {

        tunnel.dispose();
        memo.getTrafficController().terminateThreads();
        JUnitAppender.suppressWarnMessage("sendLocoNetMessage: IOException: java.io.IOException: Read end dead");
        tunnel = null;
        tc = null;
        memo.dispose();
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
