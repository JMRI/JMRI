package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleSignalHeadServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleSignalHeadServerTest {

    @Test public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) {
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        assertThat(a).isNotNull();
    }

    @Test public void testConnectionCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) {
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);        
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(jcs);
        assertThat(a).isNotNull();
    }

    // test sending a message.
    @Test public void testSendMessageWithConnection() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);        
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable(() -> {
            java.lang.reflect.Method sendMessageMethod;
            sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a,"Hello World");
        });
        assertThat(thrown).withFailMessage("failed to execute send message using refleciton: {} ",thrown ).isNull();
        assertThat(jcs.getOutput()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // test sending a message.
    @Test public void testSendMessage() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable(() -> {
           java.lang.reflect.Method sendMessageMethod;
           sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
           // override the default permissions.
            sendMessageMethod.setAccessible(true);
           sendMessageMethod.invoke(a,"Hello World");
         });
        assertThat(thrown).withFailMessage("failed to execute send message using refleciton: {} ",thrown ).isNull();
        assertThat(sb.toString()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // test sending an error message.
    @Test public void testSendErrorStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {

                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        Throwable thrown = catchThrowable( () -> a.sendErrorStatus("IT1"));
        assertThat(thrown).withFailMessage("failed to execute send error status: {}",thrown ).isNull();
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("SIGNALHEAD ERROR\n");
    }

    // test intializing a SignalHead status message.
    @Test public void checkInitSignalHead() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        new SimpleSignalHeadServer(input, output);
        assertThat(sb.toString()).withFailMessage("no status set for new signal head unless asked for").isEqualTo("");
    }

    // test sending DARK status message.
    @Test public void CheckSendDarkStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        Throwable thrown = catchThrowable( () -> a.sendStatus("IH1",jmri.SignalHead.DARK));
        assertThat(thrown).withFailMessage("Exception sending DARK Status").isNull();
        assertThat(sb.toString()).withFailMessage("sendStatus check").isEqualTo("SIGNALHEAD IH1 DARK\n");
    }

    // test sending an RED status message.
    @Test public void CheckSendRedStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        Throwable thrown = catchThrowable( () -> a.sendStatus("IH1",jmri.SignalHead.RED));
        assertThat(thrown).withFailMessage("Exception sending RED Status").isNull();
        assertThat(sb.toString()).withFailMessage("sendStatus check").isEqualTo("SIGNALHEAD IH1 RED\n");
    }

    // test sending an UNKNOWN status message.
    @Test public void CheckSendUnkownStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        Throwable thrown = catchThrowable( () -> a.sendStatus("IH1",jmri.SignalHead.UNKNOWN));
        assertThat(thrown).withFailMessage("Exception sending UNKNOWN Status").isNull();
        assertThat(sb.toString()).withFailMessage("sendStatus check").isEqualTo("SIGNALHEAD IH1 RED\n");
    }

    // test Parsing an DARK status message.
    @Test public void parseDarkStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        Throwable thrown = catchThrowable( () -> a.parseStatus("SIGNALHEAD IH1 DARK\n"));
        assertThat(thrown).withFailMessage("Exception parsing DARK Status").isNull();
        jmri.SignalHead signalHead = (jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class)).getSignalHead("IH1");
        assertThat(signalHead.getAppearance()).withFailMessage("Parse Active Status Check").isEqualTo(jmri.SignalHead.DARK);
        // parsing the status also causes a message to return to the client.
        assertThat(sb.toString()).withFailMessage("parse Dark check").isEqualTo("SIGNALHEAD IH1 DARK\n");
    }

    // test Parsing an RED status message.
    @Test public void parseRedStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        Throwable thrown = catchThrowable( ()->  a.parseStatus("SIGNALHEAD IH1 RED\n"));
        assertThat(thrown).withFailMessage("Exception parsing RED Status").isNull();
        jmri.SignalHead signalHead = (jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class)).getSignalHead("IH1");
        assertThat(signalHead.getAppearance()).withFailMessage("Parse Inactive Status Check").isEqualTo(jmri.SignalHead.RED);
        // parsing the status also causes a message to return to the client.
        assertThat(sb.toString()).withFailMessage("parse Inactive check").isEqualTo("SIGNALHEAD IH1 RED\n");
    }

    // test Parsing an blank status message.
    @Test public void parseBlankStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        Throwable thrown = catchThrowable( () -> a.parseStatus("SIGNALHEAD IH1\n"));
        assertThat(thrown).withFailMessage("Exception parsing RED Status").isNull();
        // nothing has changed the Signal Head, so it should be DARK.
        assertThat(sb.toString()).withFailMessage("parse blank check").isEqualTo("SIGNALHEAD IH1 DARK\n");
    }

    // test Parsing an other status message.
    @Test public void parseOtherStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleSignalHeadServer a = new SimpleSignalHeadServer(input, output);
        Throwable thrown = catchThrowable( () ->  a.parseStatus("SIGNALHEAD IH1 UNKNOWN\n"));
        assertThat(thrown).withFailMessage("Exception parsing UNKNOWN Status").isNull();
        // this isn't a known state, so it should be just like blank.
        // nothing has changed the Signal Head, so it should be DARK.
        assertThat(sb.toString()).withFailMessage("parse blank check").isEqualTo("SIGNALHEAD IH1 DARK\n");
    }

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDebugThrottleManager();
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(new jmri.implementation.VirtualSignalHead("IH1","Head 1"));
    }

    @AfterEach public void tearDown() {
        JUnitUtil.tearDown();
    }

}
