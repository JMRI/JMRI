package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import jmri.DccLocoAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
       Throwable thrown = catchThrowable( () -> {
          ((JmriSRCPThrottleServer)ats).initThrottle(1,42,false,128,28);
          confirmThrottleRequestSucceeded();
       });
       assertThat(thrown).withFailMessage("failed requesting throttle").isNull();
    }

    /**
     * confirm the throttle request succeeded and an appropriate response
     * was forwarded to the client.
     */
    @Override
    public void confirmThrottleRequestSucceeded(){
        assertThat(sb.toString()).withFailMessage("Throttle notification sent").endsWith("101 INFO 1 GL 42 N 1 28\n\r");
    }

    @Test
    public void requestThrottleBadBusTest(){
        Throwable thrown = catchThrowable( () -> ((JmriSRCPThrottleServer)ats).initThrottle(44,42,false,128,28));
        assertThat(thrown).withFailMessage("failed requesting throttle").isNull();
        assertThat(sb.toString()).withFailMessage("wrong value").endsWith("412 ERROR wrong value\n\r");
    }

    /**
     * confirm the error status was forwarded to the client.
     */
    @Override
    public void confirmThrottleErrorStatusSent(){
        assertThat(sb.toString()).withFailMessage("called in error").endsWith("499 ERROR unspecified error\n\r");
    }

    @Test
    public void sendStatusStandardTest(){
       Throwable thrown = catchThrowable( () -> {
          ((JmriSRCPThrottleServer)ats).initThrottle(1,42,false,128,28);
          ats.sendStatus(new DccLocoAddress(42,false));
       });
       assertThat(thrown).withFailMessage("failed sending status").isNull();
       confirmThrottleErrorStatusSent();
    }
  
    @Override
    @Test
    public void sendStatusTest(){
        Throwable thrown = catchThrowable( () -> {
          ((JmriSRCPThrottleServer)ats).initThrottle(1,42,false,128,28);
          confirmThrottleRequestSucceeded();
          ((JmriSRCPThrottleServer)ats).sendStatus(1,42);
          confirmThrottleStatusSent();
       });
       assertThat(thrown).withFailMessage("failed sending status").isNull();
    }

    /**
     * confirm the throttle status was forwarded to the client.
     */
    @Override
    public void confirmThrottleStatusSent(){
        assertThat(sb.toString()).withFailMessage("throttle status").endsWith("100 INFO 1 GL 42 1 0 126 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n\r");
    }


    @BeforeEach
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
        OutputStream output = new OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                };
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        ats = new JmriSRCPThrottleServer(input,output);
    }

    @AfterEach
    public void tearDown() {
        sb = null;
        ats = null;
        JUnitUtil.tearDown();
    }
}
