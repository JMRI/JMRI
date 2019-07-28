package jmri.jmris.srcp;

import org.junit.*;

import jmri.util.JUnitUtil;
import jmri.DccLocoAddress;

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
     * confirm the throttle request succeeded and an appropriate response
     * was forwarded to the client.
     */
    @Override
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

    /**
     * confirm the error status was forwarded to the client.
     */
    @Override
    public void confirmThrottleErrorStatusSent(){
       Assert.assertTrue("called in error",sb.toString().endsWith("499 ERROR unspecified error\n\r"));
    }

    @Test
    public void sendStatusStandardTest(){
       try {
          ((JmriSRCPThrottleServer)ats).initThrottle(1,42,false,128,28);
          ats.sendStatus(new DccLocoAddress(42,false));
       } catch (java.io.IOException ioe) {
          Assert.fail("failed sending status");
       }
       confirmThrottleErrorStatusSent();
    }
  
    @Override
    @Test
    public void sendStatusTest(){
       try {
          ((JmriSRCPThrottleServer)ats).initThrottle(1,42,false,128,28);
          confirmThrottleRequestSucceeded();
          ((JmriSRCPThrottleServer)ats).sendStatus(1,42);
          confirmThrottleStatusSent();
       } catch (java.io.IOException ioe) {
          Assert.fail("failed sending status");
       }
    }

    /**
     * confirm the throttle status was forwarded to the client.
     */
    @Override
    public void confirmThrottleStatusSent(){
       Assert.assertTrue("throttle status",sb.toString().endsWith("100 INFO 1 GL 42 1 0 126 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n\r"));
    }


    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        // ensure the Internal System Connection memo is available.
        jmri.InstanceManager.setDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class, new jmri.jmrix.internal.InternalSystemConnectionMemo(false));
 
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
