package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
    @Disabled("This isn't triggering the right property change listener")
    public void testPropertyChangeThrownStatus() {
        Throwable thrown = catchThrowable( () -> {
            ((JmriSRCPTurnoutServer) ts).initTurnout(1,1,"N");
            jmri.InstanceManager.getDefault(jmri.TurnoutManager.class)
                            .provideTurnout("IT1").setState(jmri.Turnout.THROWN);
            assertThat(sb.toString()).withFailMessage("Thrown Message Sent").endsWith("101 INFO 1 GA 1 N\n\r");
        });
        assertThat(thrown).withFailMessage("Exception setting Status").isNull();
    }

    // test the property change sequence for an CLOSED property change.
    @Test
    @Override
    @Disabled("This isn't triggering the right property change listener")
    public void testPropertyChangeClosedStatus() {
        Throwable thrown = catchThrowable( () -> {
            ((JmriSRCPTurnoutServer) ts).initTurnout(1,1,"N");
            jmri.InstanceManager.getDefault(jmri.TurnoutManager.class)
                            .provideTurnout("IT1").setState(jmri.Turnout.CLOSED);
            assertThat(sb.toString()).withFailMessage("Closed Message Sent").endsWith("101 INFO 1 GA 0 N\n\r");
        });
        assertThat(thrown).withFailMessage("Exception setting Status").isNull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkErrorStatusSent(){
        assertThat(sb.toString()).withFailMessage("Active Message Sent").endsWith("499 ERROR unspecified error\n\r");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkTurnoutThrownSent(){
        assertThat(sb.toString()).withFailMessage("Active Message Sent").endsWith("499 ERROR unspecified error\n\r");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkTurnoutClosedSent() {
        assertThat(sb.toString()).withFailMessage("Active Message Sent").endsWith("499 ERROR unspecified error\n\r");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkTurnoutUnknownSent() {
        assertThat(sb.toString()).withFailMessage("Active Message Sent").endsWith("499 ERROR unspecified error\n\r");
    }

    @BeforeEach
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
        OutputStream output = new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                };
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        ts = new JmriSRCPTurnoutServer(input, output);
    }

    @AfterEach
    public void tearDown() {
        ts.dispose();
        ts = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
