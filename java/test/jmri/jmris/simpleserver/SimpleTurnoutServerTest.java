package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleTurnoutServer class
 *
 * @author Paul Bender Copyright (C) 2012,2018
 */
public class SimpleTurnoutServerTest extends jmri.jmris.AbstractTurnoutServerTestBase {

    private StringBuilder sb = null;

    @Test
    public void testConnectionCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleTurnoutServer a = new SimpleTurnoutServer(jcs);
        assertThat(a).isNotNull();
    }

    @Test
    // test sending a message.
    public void testSendMessage() {
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable( () -> {
            java.lang.reflect.Method sendMessageMethod = ts.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(ts, "Hello World");
        });
        assertThat(thrown).withFailMessage("Error calling sendMessage with reflection").isNull();
        assertThat(sb.toString()).isEqualTo("Hello World").withFailMessage("SendMessage Check");
    }

    @Test
    // test sending a message.
    public void testSendMessageWithConnection() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleTurnoutServer a = new SimpleTurnoutServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable( () -> {
            java.lang.reflect.Method sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a,"Hello World");
        });
        assertThat(thrown).withFailMessage("Error calling sendMessage with reflection").isNull();
        assertThat(jcs.getOutput()).isEqualTo("Hello World").withFailMessage("SendMessage Check");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkErrorStatusSent(){
            assertThat(sb.toString()).isEqualTo("TURNOUT ERROR\n").withFailMessage("Send Error Status check");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkTurnoutThrownSent(){
            assertThat(sb.toString()).isEqualTo("TURNOUT IT1 THROWN\n").withFailMessage("Send Thrown Status check");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkTurnoutClosedSent() {
            assertThat(sb.toString()).isEqualTo("TURNOUT IT1 CLOSED\n").withFailMessage("Send Closed Status check");
    }

    /**
     * {@inhertDoc} 
     */
    @Override
    public void checkTurnoutUnknownSent() {
            assertThat(sb.toString()).isEqualTo("TURNOUT IT1 UNKNOWN\n").withFailMessage("Send Error Status check");
    }

    // The minimal setup for log4J
    @BeforeEach
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
        ts = new SimpleTurnoutServer(input, output);
    }

    @AfterEach
    public void tearDown() throws Exception {
        ts = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
