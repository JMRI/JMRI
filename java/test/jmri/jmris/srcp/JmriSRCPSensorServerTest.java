package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPSensorServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016,2018
 */
public class JmriSRCPSensorServerTest extends jmri.jmris.AbstractSensorServerTestBase {

    private StringBuilder sb = null;

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
    public void checkSensorActiveSent(){
         Assert.assertTrue("Active Message Sent", sb.toString().endsWith("100 INFO 0 FB 1 1\n\r"));
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorInActiveSent(){
         Assert.assertTrue("Active Message Sent", sb.toString().endsWith("100 INFO 0 FB 1 0\n\r"));
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkSensorUnknownSent(){
         Assert.assertTrue("Active Message Sent", sb.toString().endsWith("411 ERROR unknown value\n\r"));
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();

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
        ss = new JmriSRCPSensorServer(input, output);
    }

    @After public void tearDown() throws Exception {
        ss.dispose();
        ss = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
