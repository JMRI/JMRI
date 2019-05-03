package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPTurnoutServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016,2018
 */
public class JmriSRCPTurnoutServerTest extends jmri.jmris.AbstractTurnoutServerTestBase {

    private StringBuilder sb = null;

    // test the property change sequence for an THROWN property change.
    @Test
    @Override
    @Ignore("This isn't triggering the right property change listener")
    public void testPropertyChangeThrownStatus() {
        try {
            ((JmriSRCPTurnoutServer) ts).initTurnout(1,1,"N");
            jmri.InstanceManager.getDefault(jmri.TurnoutManager.class)
                            .provideTurnout("IT1").setState(jmri.Turnout.THROWN);
            Assert.assertTrue("Thrown Message Sent", sb.toString().endsWith("101 INFO 1 GA 1 N\n\r"));
        } catch (java.io.IOException | jmri.JmriException je){
            Assert.fail("Exception setting Status");
        }
    }

    // test the property change sequence for an CLOSED property change.
    @Test
    @Override
    @Ignore("This isn't triggering the right property change listener")
    public void testPropertyChangeClosedStatus() {
        try {
            ((JmriSRCPTurnoutServer) ts).initTurnout(1,1,"N");
            jmri.InstanceManager.getDefault(jmri.TurnoutManager.class)
                            .provideTurnout("IT1").setState(jmri.Turnout.CLOSED);
            Assert.assertTrue("Closed Message Sent", sb.toString().endsWith("101 INFO 1 GA 0 N\n\r"));
        } catch (java.io.IOException | jmri.JmriException je){
            Assert.fail("Exception setting Status");
        }
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkErrorStatusSent(){
         Assert.assertTrue("Active Message Sent", sb.toString().endsWith("499 ERROR unspecified error\n\r"));
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkTurnoutThrownSent(){
         Assert.assertTrue("Active Message Sent", sb.toString().endsWith("499 ERROR unspecified error\n\r"));
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkTurnoutClosedSent() {
         Assert.assertTrue("Active Message Sent", sb.toString().endsWith("499 ERROR unspecified error\n\r"));
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkTurnoutUnknownSent() {
         Assert.assertTrue("Active Message Sent", sb.toString().endsWith("499 ERROR unspecified error\n\r"));
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        // verify the Internal System Connection memo is available.
        jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class);
 
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        ts = new JmriSRCPTurnoutServer(input, output);
    }

    @After public void tearDown() throws Exception {
        ts.dispose();
        ts = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
