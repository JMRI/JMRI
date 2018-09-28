package jmri.jmris.srcp;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.util.JUnitUtil;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPThrottleServer class
 *
 * @author Paul Bender copyright (C) 2016
 */
public class JmriSRCPThrottleServerTest extends jmri.jmris.AbstractThrottleServerTestBase {

    private StringBuilder sb = null;
    
    @Test
    @Override
    public void requestThrottleTest(){
       try {
          ((JmriSRCPThrottleServer)ats).initThrottle(1,42,false,128,28);
          confirmThrottleRequestSucceeded();
       } catch (java.io.IOException ioe) {
          Assert.fail("failed requesting throttle");
       }
    }

    /**
     * confirm the throttle request succeeded and an appropirate response
     * was forwarded to the client.
     */
    public void confirmThrottleRequestSucceeded(){
        Assert.assertTrue("Throttle notification sent", sb.toString().endsWith("101 INFO 1 GL 42 N 1 28\n\r"));
    }

    @Test
    public void requestThrottleBadBusTest(){
       try {
          ((JmriSRCPThrottleServer)ats).initThrottle(44,42,false,128,28);
       } catch (java.io.IOException ioe) {
          Assert.fail("failed requesting throttle");
       }
       Assert.assertTrue("wrong value",sb.toString().endsWith("412 ERROR wrong value\n\r"));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        // verify the Internal System Connection memo is available.
        jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class);
 
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        ats = new JmriSRCPThrottleServer(input,output);
    }

    @After
    public void tearDown() {
	sb = null;
	ats = null;
	JUnitUtil.tearDown();
    }
}
