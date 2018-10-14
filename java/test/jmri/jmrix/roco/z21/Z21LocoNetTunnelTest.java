package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.roco.z21.z21LocoNetTunnel class
 *
 * @author	Paul Bender
 */
public class Z21LocoNetTunnelTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;
    private Z21LocoNetTunnel tunnel = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(tunnel);
    }

    @Test
    public void testGetStreamPortController() {
        Assert.assertNotNull(tunnel.getStreamPortController());
    }

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

        tunnel = new Z21LocoNetTunnel(memo);
    }

    @After
    public void tearDown() {
        tunnel.dispose();
        tunnel = null;
        tc = null;
        memo.getTrafficController().terminateThreads();
        memo = null;
        JUnitUtil.tearDown();
    }

}
