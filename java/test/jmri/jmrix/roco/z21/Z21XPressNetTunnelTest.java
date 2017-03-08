package jmri.jmrix.roco.z21;

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

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold(){
            @Override
            protected void terminate(){}
        };
        memo.setTrafficController(tc);
        
        tunnel = new Z21XPressNetTunnel(memo){
           @Override
           void setStreamPortController(jmri.jmrix.lenz.XNetStreamPortController x) {
           xsc = new Z21XNetStreamPortController(x.getInputStream(),x.getOutputStream(),x.getCurrentPortName()){
                 @Override
                 public void configure(){
                     // connect to a packetizing traffic controller
                     jmri.jmrix.lenz.XNetTrafficController packets = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
                     packets.connectPort(this);
                     this.getSystemConnectionMemo().setXNetTrafficController(packets);
                 }
           };
           }
        };
    }

    @After
    public void tearDown() {
        int n = tc.outbound.size();
        tunnel = null;
        tc = null;
        memo = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
