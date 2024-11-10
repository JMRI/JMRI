package jmri.jmrix.roco.z21;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
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
        Assert.assertNotNull(tunnel);
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
        jmri.jmrix.loconet.streamport.LnStreamPortController lnspc = tunnel.getStreamPortController();
        Assert.assertNotNull(lnspc);
        tunnel.dispose();
        memo.getTrafficController().terminateThreads();
        JUnitUtil.waitFor(() -> {  return !lnspc.status(); });
        JUnitAppender.suppressWarnMessage("sendLocoNetMessage: IOException: java.io.IOException: Read end dead");
        tunnel = null;
        tc = null;
        memo.dispose();
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
