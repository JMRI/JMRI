package jmri.jmrix.roco.z21;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.roco.z21.z21XPressNetTunnel class
 *
 * @author	Paul Bender
 */
public class Z21XPressNetTunnelTest extends TestCase {

    public void testCtor() {
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        Z21InterfaceScaffold tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        
        Z21XPressNetTunnel a = new Z21XPressNetTunnel(memo){
           @Override
           void setStreamPortController(jmri.jmrix.lenz.XNetStreamPortController x) {
           xsc = new Z21XNetStreamPortController(x.getInputStream(),x.getOutputStream(),x.getCurrentPortName()){
                 @Override
                 public void configure(){
                 }
           };
           }
        };
        Assert.assertNotNull(a);



    }

    public void testGetStreamPortController() {
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        Z21TrafficController tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);

        Z21XPressNetTunnel a = new Z21XPressNetTunnel(memo){
           @Override
           void setStreamPortController(jmri.jmrix.lenz.XNetStreamPortController x) {
           xsc = new Z21XNetStreamPortController(x.getInputStream(),x.getOutputStream(),x.getCurrentPortName()){
                 @Override
                 public void configure(){
                 }
           };
           }
        };
        Assert.assertNotNull(a.getStreamPortController());
    }

    // from here down is testing infrastructure
    public Z21XPressNetTunnelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Z21XPressNetTunnelTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Z21XPressNetTunnelTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
    }

    protected void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
